import { useState, type FormEvent } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { AuthAPI } from '../api/services';
import { useAuth } from '../context/AuthContext';
import { extractMessage } from '../api/axios';
import { AuthLayout } from '../components/ui';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const nav = useNavigate();
  const loc = useLocation() as any;
  const { login } = useAuth();

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(''); setBusy(true);
    try {
      const session = await AuthAPI.login(email, password);
      login(session);
      nav(loc.state?.from || '/dashboard');
    } catch (err) { setError(extractMessage(err)); }
    finally { setBusy(false); }
  };

  return (
    <AuthLayout>
      <h3>Welcome back</h3>
      <p className="sub">Sign in to your account</p>
      {error && <div className="alert alert-danger py-2" role="alert">{error}</div>}
      <form onSubmit={handleSubmit}>
        <div className="mb-3">
          <label className="form-label" htmlFor="login-email">Email</label>
          <div className="input-icon">
            <i className="bi bi-envelope" aria-hidden="true"></i>
            <input id="login-email" type="email" className="form-control" autoComplete="email" placeholder="you@example.com"
              value={email} onChange={(e) => setEmail(e.target.value)} required />
          </div>
        </div>
        <div className="mb-4">
          <label className="form-label" htmlFor="login-password">Password</label>
          <div className="input-icon">
            <i className="bi bi-lock" aria-hidden="true"></i>
            <input id="login-password" type="password" className="form-control" autoComplete="current-password"
              value={password} onChange={(e) => setPassword(e.target.value)} required />
          </div>
        </div>
        <button className="btn btn-brand w-100" disabled={busy}>
          {busy ? <><span className="spinner-border" aria-hidden="true"></span> Signing in…</> : <>Sign In <i className="bi bi-arrow-right" aria-hidden="true"></i></>}
        </button>
      </form>
      <div className="switch">
        New here? <Link to="/register" className="link-plain">Create an account</Link>
      </div>
    </AuthLayout>
  );
};

export default Login;
