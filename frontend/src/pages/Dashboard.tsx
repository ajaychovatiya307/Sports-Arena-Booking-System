import { useEffect, useState } from 'react';
import { BookingAPI } from '../api/services';
import type { DashboardSummary } from '../types';
import { useAuth } from '../context/AuthContext';
import { extractMessage } from '../api/axios';
import { Link } from 'react-router-dom';

const Stat = ({ label, value }: { label: string; value: number | string }) => (
  <div className="stat-card">
    <div className="label">{label}</div>
    <div className="value">{value}</div>
  </div>
);

const Dashboard = () => {
  const { isAdmin, session } = useAuth();
  const [d, setD] = useState<DashboardSummary | null>(null);
  const [err, setErr] = useState('');

  useEffect(() => {
    BookingAPI.dashboard().then(setD).catch((e) => setErr(extractMessage(e)));
  }, []);

  if (err) return <div className="alert alert-danger">{err}</div>;
  if (!d) return <div>Loading…</div>;

  return (
    <>
      <h4 className="mb-4">Welcome, {session?.fullName} 👋</h4>
      <div className="row g-3 mb-4">
        {isAdmin ? (
          <>
            <div className="col-md-3"><Stat label="Customers" value={d.totalCustomers ?? 0} /></div>
            <div className="col-md-3"><Stat label="Categories" value={d.totalCategories ?? 0} /></div>
            <div className="col-md-3"><Stat label="Active Packages" value={d.totalPackages ?? 0} /></div>
            <div className="col-md-3"><Stat label="Total Bookings" value={d.totalBookings} /></div>
          </>
        ) : (
          <>
            <div className="col-md-3"><Stat label="My Teams" value={d.totalTeams ?? 0} /></div>
            <div className="col-md-3"><Stat label="Total Bookings" value={d.totalBookings} /></div>
            <div className="col-md-3"><Stat label="Active" value={d.activeBookings ?? 0} /></div>
            <div className="col-md-3"><Stat label="Completed" value={d.completedBookings ?? 0} /></div>
          </>
        )}
      </div>

      <div className="card-panel mb-4">
        <h6 className="mb-3">Bookings by Status</h6>
        <div className="row g-2">
          {Object.entries(d.bookingsByStatus).map(([k, v]) => (
            <div className="col-6 col-md-2" key={k}>
              <div className="text-center border rounded p-2">
                <div className="small text-muted">{k}</div>
                <div className="fs-5 fw-bold">{v}</div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {isAdmin && (
        <div className="card-panel mb-4">
          <h6 className="mb-3">Recent Booking Activity</h6>
          <div className="table-responsive">
            <table className="table align-middle mb-0">
              <thead><tr><th>Reference</th><th>Customer</th><th>Package</th><th>Date</th><th>Slot</th><th>Status
                </th></tr></thead>
              <tbody>
                {(d.recentBookings ?? []).map((b) => (
                  <tr key={b.id}>
                    <td className="fw-semibold">{b.referenceNumber}</td>
                    <td>{b.customerName}</td>
                    <td>{b.packageName}</td>
                    <td>{b.bookingDate}</td>
                    <td>{b.timeSlot}</td>
                    <td><span className={`badge-status badge-${b.status}`}>{b.status}</span></td>
                  </tr>
                ))}
                {(d.recentBookings ?? []).length === 0 && (
                  <tr><td colSpan={6} className="text-center text-muted py-3">No recent bookings.</td></tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {!isAdmin && (
        <div className="d-flex gap-2">
          <Link to="/arenas" className="btn btn-brand"><i className="bi bi-search"></i> Browse Arenas</Link>
          <Link to="/bookings/new" className="btn btn-outline-secondary"><i className="bi bi-calendar-plus"></i> New Booking
          </Link>
        </div>
      )}
    </>
  );
};

export default Dashboard;
