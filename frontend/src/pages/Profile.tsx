import { useEffect, useState, type FormEvent } from 'react';
import { ProfileAPI } from '../api/services';
import type { CustomerProfile } from '../types';
import { extractMessage } from '../api/axios';
import { useAuth } from '../context/AuthContext';

const Profile = () => {
  const { session, isAdmin, updateSession } = useAuth();
  const [p, setP] = useState<CustomerProfile | null>(null);
  const [msg, setMsg] = useState('');
  const [err, setErr] = useState('');

  useEffect(() => {
    // Admin has no customer_profile row (SRS US02: admin has read-only access
    // to customer profiles). Show a session-backed admin card instead of
    // calling /api/users/me which returns 404 for A1.
    if (isAdmin) {
      setP(null);
      return;
    }
    ProfileAPI.me().then(setP).catch((e) => setErr(extractMessage(e)));
  }, [isAdmin]);

  const save = async (e: FormEvent) => {
    e.preventDefault();
    if (!p) return;
    setErr(''); setMsg('');
    try {
      const updated = await ProfileAPI.update(p);
      setP(updated);
      // Reflect the new name immediately in the header + dashboard.
      updateSession({ fullName: updated.fullName });
      setMsg('Profile updated');
    } catch (e2) { setErr(extractMessage(e2)); }
  };

  if (isAdmin && session) {
    return (
      <div className="card-panel" style={{ maxWidth: 640 }}>
        <h5 className="mb-3">Administrator Profile</h5>
        <div className="alert alert-info py-2">
          Administrator accounts do not have a customer profile. Details below are read-only.
        </div>
        <div className="row g-3">
          <div className="col-md-6">
            <label className="form-label">User ID</label>
            <input className="form-control" value={session.userId} disabled />
          </div>
          <div className="col-md-6">
            <label className="form-label">Role</label>
            <input className="form-control" value={session.role} disabled />
          </div>
          <div className="col-md-6">
            <label className="form-label">Full Name</label>
            <input className="form-control" value={session.fullName} disabled />
          </div>
          <div className="col-md-6">
            <label className="form-label">Email</label>
            <input className="form-control" value={(session as any).email ?? ''} disabled />
          </div>
        </div>
      </div>
    );
  }

  if (err && !p) return <div className="alert alert-danger">{err}</div>;
  if (!p) return <div>Loading…</div>;

  return (
    <div className="card-panel" style={{ maxWidth: 640 }}>
      <h5 className="mb-3">My Profile</h5>
      {err && <div className="alert alert-danger py-2">{err}</div>}
      {msg && <div className="alert alert-success py-2">{msg}</div>}
      <form onSubmit={save}>
        <div className="row g-3">
          <div className="col-md-6">
            <label className="form-label">User ID</label>
            <input className="form-control" value={p.userId} disabled />
          </div>
          <div className="col-md-6">
            <label className="form-label">Email</label>
            <input className="form-control" value={p.email} disabled />
          </div>
          <div className="col-md-12">
            <label className="form-label">Full Name</label>
            <input className="form-control" value={p.fullName} onChange={(e) => setP({ ...p, fullName: e.target.value })}
             required />
          </div>
          <div className="col-md-6">
            <label className="form-label">Contact Number</label>
            <input className="form-control" value={p.contactNumber ?? ''} onChange={(e) =>
               setP({ ...p, contactNumber: e.target.value })} />
          </div>
          <div className="col-md-6">
            <label className="form-label">Address</label>
            <input className="form-control" value={p.address ?? ''} onChange={(e) =>
               setP({ ...p, address: e.target.value })} />
          </div>
        </div>
        <button className="btn btn-brand mt-4" type="submit">Save Changes</button>
      </form>
    </div>
  );
};

export default Profile;
