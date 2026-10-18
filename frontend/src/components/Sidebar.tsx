import { NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const linkClass = ({ isActive }: { isActive: boolean }) => (isActive ? 'active' : '');

const Sidebar = () => {
  const { isAdmin } = useAuth();
  return (
    <aside className="app-sidebar">
      <div className="brand">
        <i className="bi bi-controller"></i> Sports Arena
      </div>
      <nav>
        <NavLink to="/dashboard" className={linkClass}><i className="bi bi-speedometer2"></i> Dashboard</NavLink>
        <NavLink to="/profile" className={linkClass}><i className="bi bi-person-circle"></i> Profile</NavLink>
        <NavLink to="/teams" className={linkClass}><i className="bi bi-people-fill"></i> Teams</NavLink>
        <NavLink to="/arenas" className={linkClass}><i className="bi bi-trophy"></i> Arenas</NavLink>
        <NavLink to="/bookings" className={linkClass}><i className="bi bi-calendar-check"></i> Bookings</NavLink>
        {isAdmin && <>
          <NavLink to="/categories" className={linkClass}><i className="bi bi-tags"></i> Categories</NavLink>
          <NavLink to="/customers" className={linkClass}><i className="bi bi-people"></i> Customers</NavLink>
          <NavLink to="/slots" className={linkClass}><i className="bi bi-calendar3-week"></i> Slot Availability</NavLink>
          <NavLink to="/reports" className={linkClass}><i className="bi bi-graph-up"></i> Reports</NavLink>
        </>}
      </nav>
    </aside>
  );
};

export default Sidebar;
