import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthAPI } from '../api/services';
import { extractMessage } from '../api/axios';
import { AuthLayout } from '../components/ui';

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
    <AuthLayout>
      <h3>Create account</h3>
      <p className="sub">Join the Sports Arena Booking System</p>
      {err && <div className="alert alert-danger py-2" role="alert">{err}</div>}
      {msg && <div className="alert alert-success py-2" role="status">{msg}</div>}
      <form onSubmit={submit}>
        <div className="mb-3">
          <label className="form-label" htmlFor="reg-name">Full Name</label>
          <div className="input-icon">
            <i className="bi bi-person" aria-hidden="true"></i>
            <input id="reg-name" className="form-control" autoComplete="name" value={fullName}
              onChange={(e) => setFullName(e.target.value)} required />
          </div>
        </div>
        <div className="mb-3">
          <label className="form-label" htmlFor="reg-email">Email</label>
          <div className="input-icon">
            <i className="bi bi-envelope" aria-hidden="true"></i>
            <input id="reg-email" type="email" className="form-control" autoComplete="email" placeholder="you@example.com"
              value={email} onChange={(e) => setEmail(e.target.value)} required />
          </div>
        </div>
        <div className="mb-4">
          <label className="form-label" htmlFor="reg-password">Password</label>
          <div className="input-icon">
            <i className="bi bi-lock" aria-hidden="true"></i>
            <input id="reg-password" type="password" className="form-control" minLength={6} autoComplete="new-password"
              aria-describedby="reg-password-help" value={password} onChange={(e) => setPassword(e.target.value)} required />
          </div>
          <div id="reg-password-help" className="form-text">At least 6 characters.</div>
        </div>
        <button className="btn btn-brand w-100" disabled={busy}>
          {busy ? <><span className="spinner-border" aria-hidden="true"></span> Creating…</> : 'Register'}
        </button>
      </form>
      <div className="switch">
        Already have an account? <Link to="/login" className="link-plain">Sign In</Link>
      </div>
    </AuthLayout>
  );
};

export default Register;
