import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Header = () => {
  const { session, logout } = useAuth();
  const nav = useNavigate();

  const handleLogout = () => {
    logout();
    nav('/login');
  };

  return (
    <header className="app-header">
      <div className="title">🏟️ Sports Arena Booking</div>
      <div className="d-flex align-items-center gap-3">
        {session && <>
          <span className="text-muted small">
            <i className="bi bi-person-circle"></i> {session.fullName}
            <span className="badge bg-secondary ms-2">{session.role}</span>
          </span>
          <button className="btn btn-outline-secondary btn-sm" onClick={handleLogout}>
            <i className="bi bi-box-arrow-right"></i> Logout
          </button>
        </>}
      </div>
    </header>
  );
};

export default Header;
