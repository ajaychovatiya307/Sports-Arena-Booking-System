import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthAPI } from '../api/services';
import { extractMessage } from '../api/axios';

const Register = () => {
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [msg, setMsg] = useState('');
  const [err, setErr] = useState('');
  const [busy, setBusy] = useState(false);
  const nav = useNavigate();

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    setErr(''); setMsg(''); setBusy(true);
    try {
      const r = await AuthAPI.register(fullName, email, password);
      setMsg(r.message + ' Redirecting to login…');
      setTimeout(() => nav('/login'), 1800);
    } catch (e2) { setErr(extractMessage(e2)); }
    finally { setBusy(false); }
  };

  return (
    <div className="d-flex align-items-center justify-content-center vh-100" style=
    {{ background: 'linear-gradient(135deg, #146447, #1e8a5f)' }}>
      <div className="card-panel" style={{ width: 420 }}>
        <h3 className="mb-1 text-center" style={{ color: '#146447' }}>Create Account</h3>
        <p className="text-center text-muted mb-4">Join the Sports Arena Booking System</p>
        {err && <div className="alert alert-danger py-2">{err}</div>}
        {msg && <div className="alert alert-success py-2">{msg}</div>}
        <form onSubmit={submit}>
          <div className="mb-3">
            <label className="form-label">Full Name</label>
            <input className="form-control" value={fullName} onChange={(e) => setFullName(e.target.value)} required />
          </div>
          <div className="mb-3">
            <label className="form-label">Email</label>
            <input type="email" className="form-control" value={email} onChange={(e) => setEmail(e.target.value)} required />
          </div>
          <div className="mb-3">
            <label className="form-label">Password</label>
            <input type="password" className="form-control" minLength={6} value={password}
             onChange={(e) => setPassword(e.target.value)} required />
          </div>
          <button className="btn btn-brand w-100" disabled={busy}>{busy ? 'Creating…' : 'Register'}</button>
        </form>
        <div className="text-center mt-3 small">
          Already have an account? <Link to="/login" className="link-plain">Sign In</Link>
        </div>
      </div>
    </div>
  );
};

export default Register;
