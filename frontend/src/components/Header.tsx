import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { initials } from './ui';

// Display-only page titles for the header, keyed by route path.
const TITLES: Record<string, [string, string]> = {
  '/dashboard': ['Dashboard', 'bi-speedometer2'],
  '/profile': ['Profile', 'bi-person-circle'],
  '/teams': ['Teams', 'bi-people-fill'],
  '/arenas': ['Arenas', 'bi-trophy'],
  '/bookings': ['Bookings', 'bi-calendar-check'],
  '/bookings/new': ['New Booking', 'bi-calendar-plus'],
  '/categories': ['Categories', 'bi-tags'],
  '/customers': ['Customers', 'bi-people'],
  '/slots': ['Slot Availability', 'bi-calendar3-week'],
  '/reports': ['Reports', 'bi-graph-up']
};

const Header = () => {
  const { session, logout } = useAuth();
  const nav = useNavigate();
  const { pathname } = useLocation();
  const [pageTitle, pageIcon] = TITLES[pathname] ?? ['Sports Arena Booking', 'bi-controller'];

  const handleLogout = () => {
    logout();
    nav('/login');
  };

  return (
    <header className="app-header">
      <div className="title">
        <i className={`bi ${pageIcon}`} aria-hidden="true"></i>
        <span className="text-truncate">{pageTitle}</span>
        <span className="crumb">· Sports Arena Booking</span>
      </div>
      <div className="d-flex align-items-center gap-3">
        {session && <>
          <div className="user-chip">
            <span className="avatar" aria-hidden="true">{initials(session.fullName)}</span>
            <div className="who">
              <div className="name">{session.fullName}</div>
              <span className={`role-pill${session.role === 'ADMIN' ? ' admin' : ''}`}>{session.role}</span>
            </div>
          </div>
          <button className="btn btn-outline-secondary btn-sm" onClick={handleLogout} aria-label="Logout">
            <i className="bi bi-box-arrow-right" aria-hidden="true"></i><span className="d-none d-sm-inline">Logout</span>
          </button>
        </>}
      </div>
    </header>
  );
};

export default Header;
