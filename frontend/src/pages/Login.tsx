import { useState, type FormEvent } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { AuthAPI } from '../api/services';
import { useAuth } from '../context/AuthContext';
import { extractMessage } from '../api/axios';

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
    <div className="d-flex align-items-center justify-content-center vh-100" style=
    {{ background: 'linear-gradient(135deg, #146447, #1e8a5f)' }}>
      <div className="card-panel" style={{ width: 400 }}>
        <h3 className="mb-1 text-center" style={{ color: '#146447' }}>🏟️ Sports Arena</h3>
        <p className="text-center text-muted mb-4">Sign in to your account</p>
        {error && <div className="alert alert-danger py-2">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label className="form-label" htmlFor="login-email">Email</label>
            <input id="login-email" type="email" className="form-control" value={email} onChange={(e) => setEmail(e.target.value)} required />
          </div>
          <div className="mb-3">
            <label className="form-label" htmlFor="login-password">Password</label>
            <input id="login-password" type="password" className="form-control" value={password} onChange={(e) => setPassword(e.target.value)} 
            required />
          </div>
          <button className="btn btn-brand w-100" disabled={busy}>{busy ? 'Signing in…' : 'Sign In'}</button>
        </form>
        <div className="text-center mt-3 small">
          New here? <Link to="/register" className="link-plain">Create an account</Link>
        </div>
      </div>
    </div>
  );
};

export default Login;
