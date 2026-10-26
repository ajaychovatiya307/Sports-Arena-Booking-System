import { useEffect, useState } from 'react';
import { BookingAPI } from '../api/services';
import type { DashboardSummary } from '../types';
import { useAuth } from '../context/AuthContext';
import { extractMessage } from '../api/axios';
import { Link } from 'react-router-dom';
import { EmptyRow, PageHeader, StatusBadge } from '../components/ui';

const Stat = ({ label, value, icon, tone }: { label: string; value: number | string; icon: string; tone: string }) => (
  <div className={`stat-card tone-${tone}`}>
    <div className="stat-icon" aria-hidden="true"><i className={`bi ${icon}`}></i></div>
    <div>
      <div className="label">{label}</div>
      <div className="value">{value}</div>
    </div>
  </div>
);

const greeting = () => {
  const h = new Date().getHours();
  return h < 12 ? 'Good morning' : h < 17 ? 'Good afternoon' : 'Good evening';
};

const DashboardSkeleton = () => (
  <div aria-busy="true" aria-label="Loading dashboard">
    <div className="skeleton mb-4" style={{ height: 46, width: 320 }} />
    <div className="row g-3 mb-4">
      {[0, 1, 2, 3].map((i) => <div className="col-6 col-lg-3" key={i}><div className="skeleton" style={{ height: 92 }} /></div>)}
    </div>
    <div className="skeleton" style={{ height: 150 }} />
  </div>
);

const Dashboard = () => {
  const { isAdmin, session } = useAuth();
  const [d, setD] = useState<DashboardSummary | null>(null);
  const [err, setErr] = useState('');

  useEffect(() => {
    BookingAPI.dashboard().then(setD).catch((e) => setErr(extractMessage(e)));
  }, []);

  if (err) return <div className="alert alert-danger" role="alert">{err}</div>;
  if (!d) return <DashboardSkeleton />;

  const statusTotal = Object.values(d.bookingsByStatus).reduce((a, b) => a + b, 0);

  return (
    <>
      <PageHeader icon="bi-speedometer2" title={<>Welcome, {session?.fullName} 👋</>}
        subtitle={`${greeting()} — here's what's happening ${isAdmin ? 'across the platform' : 'with your bookings'}.`} />

      <div className="row g-3 mb-4">
        {isAdmin ? (
          <>
            <div className="col-6 col-lg-3"><Stat label="Customers" value={d.totalCustomers ?? 0} icon="bi-people" tone="blue" /></div>
            <div className="col-6 col-lg-3"><Stat label="Categories" value={d.totalCategories ?? 0} icon="bi-tags" tone="purple" /></div>
            <div className="col-6 col-lg-3"><Stat label="Active Packages" value={d.totalPackages ?? 0} icon="bi-trophy" tone="amber" /></div>
            <div className="col-6 col-lg-3"><Stat label="Total Bookings" value={d.totalBookings} icon="bi-calendar-check" tone="green" /></div>
          </>
        ) : (
          <>
            <div className="col-6 col-lg-3"><Stat label="My Teams" value={d.totalTeams ?? 0} icon="bi-people-fill" tone="blue" /></div>
            <div className="col-6 col-lg-3"><Stat label="Total Bookings" value={d.totalBookings} icon="bi-calendar-check" tone="green" /></div>
            <div className="col-6 col-lg-3"><Stat label="Active" value={d.activeBookings ?? 0} icon="bi-lightning-charge" tone="amber" /></div>
            <div className="col-6 col-lg-3"><Stat label="Completed" value={d.completedBookings ?? 0} icon="bi-check2-circle" tone="purple" /></div>
          </>
        )}
      </div>

      <div className="card-panel mb-4">
        <h6 className="mb-3"><i className="bi bi-bar-chart" aria-hidden="true"></i> Bookings by Status</h6>
        <div className="row g-2">
          {Object.entries(d.bookingsByStatus).map(([k, v]) => (
            <div className="col-6 col-md-4 col-xl" key={k}>
              <div className={`status-tile st-${k}`}>
                <div className="st-label">{k}</div>
                <div className="st-value">{v}</div>
                <div className="st-bar" aria-hidden="true">
                  <span style={{ width: `${statusTotal ? Math.round((v / statusTotal) * 100) : 0}%` }} />
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {isAdmin && (
        <div className="card-panel mb-4">
          <div className="d-flex justify-content-between align-items-center mb-3">
            <h6 className="section-title m-0"><i className="bi bi-clock-history" aria-hidden="true"></i> Recent Booking Activity</h6>
            <Link to="/bookings" className="link-plain small">View all <i className="bi bi-arrow-right" aria-hidden="true"></i></Link>
          </div>
          <div className="table-responsive">
            <table className="table align-middle mb-0">
              <thead><tr><th>Reference</th><th>Customer</th><th>Package</th><th>Date</th><th>Slot</th><th>Status
                </th></tr></thead>
              <tbody>
                {(d.recentBookings ?? []).map((b) => (
                  <tr key={b.id}>
                    <td className="fw-semibold mono">{b.referenceNumber}</td>
                    <td>{b.customerName}</td>
                    <td>{b.packageName}</td>
                    <td>{b.bookingDate}</td>
                    <td>{b.timeSlot}</td>
                    <td><StatusBadge status={b.status} /></td>
                  </tr>
                ))}
                {(d.recentBookings ?? []).length === 0 && (
                  <EmptyRow colSpan={6} icon="bi-calendar-x" title="No recent bookings." />
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {!isAdmin && (
        <div className="row g-3">
          <div className="col-md-6">
            <Link to="/arenas" className="quick-action">
              <span className="qa-icon" aria-hidden="true"><i className="bi bi-search"></i></span>
              <span><span className="qa-title d-block">Browse Arenas</span><span className="qa-sub">Explore turfs, courts and pools</span></span>
              <i className="bi bi-arrow-right" aria-hidden="true"></i>
            </Link>
          </div>
          <div className="col-md-6">
            <Link to="/bookings/new" className="quick-action secondary">
              <span className="qa-icon" aria-hidden="true"><i className="bi bi-calendar-plus"></i></span>
              <span><span className="qa-title d-block">New Booking</span><span className="qa-sub">Reserve a slot for one of your teams</span></span>
              <i className="bi bi-arrow-right" aria-hidden="true"></i>
            </Link>
          </div>
        </div>
      )}
    </>
  );
};

export default Dashboard;
