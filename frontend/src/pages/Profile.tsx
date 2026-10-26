import { useEffect, useState, type FormEvent } from 'react';
import { ProfileAPI } from '../api/services';
import type { CustomerProfile } from '../types';
import { extractMessage } from '../api/axios';
import { useAuth } from '../context/AuthContext';
import { Loader, PageHeader, initials } from '../components/ui';

const Profile = () => {
  const { session, isAdmin, updateSession } = useAuth();
  const [p, setP] = useState<CustomerProfile | null>(null);
  const [msg, setMsg] = useState('');
  const [err, setErr] = useState('');
  const [saving, setSaving] = useState(false);

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
    setErr(''); setMsg(''); setSaving(true);
    try {
      const updated = await ProfileAPI.update(p);
      setP(updated);
      // Reflect the new name immediately in the header + dashboard.
      updateSession({ fullName: updated.fullName });
      setMsg('Profile updated');
    } catch (e2) { setErr(extractMessage(e2)); }
    finally { setSaving(false); }
  };

  if (isAdmin && session) {
    return (
      <>
        <PageHeader icon="bi-person-circle" title="Administrator Profile" subtitle="Account details for this administrator" />
        <div className="card-panel" style={{ maxWidth: 720 }}>
          <div className="profile-hero">
            <div className="avatar-lg" aria-hidden="true">{initials(session.fullName)}</div>
            <div>
              <div className="ph-name">{session.fullName}</div>
              <div className="ph-mail">{(session as any).email ?? ''}</div>
              <span className="role-pill admin mt-1">{session.role}</span>
            </div>
          </div>
          <div className="alert alert-info py-2">
            Administrator accounts do not have a customer profile. Details below are read-only.
          </div>
          <div className="row g-3">
            <div className="col-md-6">
              <label className="form-label" htmlFor="adm-id">User ID</label>
              <input id="adm-id" className="form-control" value={session.userId} disabled />
            </div>
            <div className="col-md-6">
              <label className="form-label" htmlFor="adm-role">Role</label>
              <input id="adm-role" className="form-control" value={session.role} disabled />
            </div>
            <div className="col-md-6">
              <label className="form-label" htmlFor="adm-name">Full Name</label>
              <input id="adm-name" className="form-control" value={session.fullName} disabled />
            </div>
            <div className="col-md-6">
              <label className="form-label" htmlFor="adm-email">Email</label>
              <input id="adm-email" className="form-control" value={(session as any).email ?? ''} disabled />
            </div>
          </div>
        </div>
      </>
    );
  }

  if (err && !p) return <div className="alert alert-danger" role="alert">{err}</div>;
  if (!p) return <Loader label="Loading profile…" />;

  return (
    <>
      <PageHeader icon="bi-person-circle" title="My Profile" subtitle="Keep your contact details up to date for your bookings" />
      <div className="card-panel" style={{ maxWidth: 720 }}>
        <div className="profile-hero">
          <div className="avatar-lg" aria-hidden="true">{initials(p.fullName)}</div>
          <div>
            <div className="ph-name">{p.fullName}</div>
            <div className="ph-mail">{p.email}</div>
            <span className="role-pill mt-1">{p.userId}</span>
          </div>
        </div>
        {err && <div className="alert alert-danger py-2" role="alert">{err}</div>}
        {msg && <div className="alert alert-success py-2" role="status">{msg}</div>}
        <form onSubmit={save}>
          <div className="row g-3">
            <div className="col-md-6">
              <label className="form-label" htmlFor="pf-id">User ID</label>
              <input id="pf-id" className="form-control" value={p.userId} disabled />
            </div>
            <div className="col-md-6">
              <label className="form-label" htmlFor="pf-email">Email</label>
              <input id="pf-email" className="form-control" value={p.email} disabled />
            </div>
            <div className="col-md-12">
              <label className="form-label" htmlFor="pf-name">Full Name</label>
              <input id="pf-name" className="form-control" autoComplete="name" value={p.fullName} onChange={(e) => setP({ ...p, fullName: e.target.value })}
               required />
            </div>
            <div className="col-md-6">
              <label className="form-label" htmlFor="pf-phone">Contact Number</label>
              <div className="input-icon">
                <i className="bi bi-telephone" aria-hidden="true"></i>
                <input id="pf-phone" className="form-control" autoComplete="tel" value={p.contactNumber ?? ''} onChange={(e) =>
                   setP({ ...p, contactNumber: e.target.value })} />
              </div>
            </div>
            <div className="col-md-6">
              <label className="form-label" htmlFor="pf-addr">Address</label>
              <div className="input-icon">
                <i className="bi bi-geo-alt" aria-hidden="true"></i>
                <input id="pf-addr" className="form-control" autoComplete="street-address" value={p.address ?? ''} onChange={(e) =>
                   setP({ ...p, address: e.target.value })} />
              </div>
            </div>
          </div>
          <button className="btn btn-brand mt-4" type="submit" disabled={saving}>
            {saving ? <><span className="spinner-border" aria-hidden="true"></span> Saving…</> : <><i className="bi bi-check2" aria-hidden="true"></i> Save Changes</>}
          </button>
        </form>
      </div>
    </>
  );
};

export default Profile;
