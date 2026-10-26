import { useState } from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const linkClass = ({ isActive }: { isActive: boolean }) => (isActive ? 'active' : '');

const Sidebar = () => {
  const { isAdmin } = useAuth();
  // Mobile drawer state (purely presentational; desktop always shows the nav).
  const [open, setOpen] = useState(false);
  const close = () => setOpen(false);

  return (
    <aside className={`app-sidebar${open ? ' open' : ''}`}>
      <div className="sidebar-top">
        <div className="brand">
          <span className="brand-mark" aria-hidden="true"><i className="bi bi-controller"></i></span>
          <span>Sports Arena<small>Booking System</small></span>
        </div>
        <button type="button" className="sidebar-toggle" aria-label={open ? 'Close menu' : 'Open menu'}
          aria-expanded={open} aria-controls="app-nav" onClick={() => setOpen((v) => !v)}>
          <i className={`bi ${open ? 'bi-x-lg' : 'bi-list'}`} aria-hidden="true"></i>
        </button>
      </div>
      <nav id="app-nav" aria-label="Main navigation">
        <div className="nav-section">Main</div>
        <NavLink to="/dashboard" className={linkClass} onClick={close}><i className="bi bi-speedometer2"></i> Dashboard</NavLink>
        <NavLink to="/profile" className={linkClass} onClick={close}><i className="bi bi-person-circle"></i> Profile</NavLink>
        <NavLink to="/teams" className={linkClass} onClick={close}><i className="bi bi-people-fill"></i> Teams</NavLink>
        <NavLink to="/arenas" className={linkClass} onClick={close}><i className="bi bi-trophy"></i> Arenas</NavLink>
        <NavLink to="/bookings" className={linkClass} onClick={close}><i className="bi bi-calendar-check"></i> Bookings</NavLink>
        {isAdmin && <>
          <div className="nav-section">Administration</div>
          <NavLink to="/categories" className={linkClass} onClick={close}><i className="bi bi-tags"></i> Categories</NavLink>
          <NavLink to="/customers" className={linkClass} onClick={close}><i className="bi bi-people"></i> Customers</NavLink>
          <NavLink to="/slots" className={linkClass} onClick={close}><i className="bi bi-calendar3-week"></i> Slot Availability</NavLink>
          <NavLink to="/reports" className={linkClass} onClick={close}><i className="bi bi-graph-up"></i> Reports</NavLink>
        </>}
      </nav>
      <div className="sidebar-foot"><i className="bi bi-shield-check"></i> {isAdmin ? 'Administrator workspace' : 'Customer workspace'}</div>
    </aside>
  );
};

export default Sidebar;
