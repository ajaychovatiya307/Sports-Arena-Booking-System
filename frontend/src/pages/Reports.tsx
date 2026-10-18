import { useEffect, useState } from 'react';
import { BookingAPI, RatingAPI } from '../api/services';
import type { Booking, DashboardSummary, Rating } from '../types';
import { extractMessage } from '../api/axios';

const Reports = () => {
  const [d, setD] = useState<DashboardSummary | null>(null);
  const [aggs, setAggs] = useState<{ packageId: number; count: number; average: number }[]>([]);
  const [err, setErr] = useState('');

  // ── Booking search / filter / sort / pagination (SRS US10) ──
  const [rows, setRows] = useState<Booking[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [reference, setReference] = useState('');
  const [customerName, setCustomerName] = useState('');
  const [status, setStatus] = useState('');
  const [date, setDate] = useState('');
  const [sortBy, setSortBy] = useState('createdAt');
  const [direction, setDirection] = useState<'asc' | 'desc'>('desc');

  // ── Admin individual ratings drill-down (SRS US12) ──
  const [openPkg, setOpenPkg] = useState<number | null>(null);
  const [pkgRatings, setPkgRatings] = useState<Rating[]>([]);

  const loadBookings = () => {
    BookingAPI.search({
      reference: reference || undefined,
      customerName: customerName || undefined,
      status: status || undefined,
      date: date || undefined,
      page, size: 10, sortBy, direction
    }).then((p) => { setRows(p.content); setTotalPages(p.totalPages); })
      .catch((e) => setErr(extractMessage(e)));
  };

  useEffect(() => {
    BookingAPI.dashboard().then(setD).catch((e) => setErr(extractMessage(e)));
    RatingAPI.aggregate().then(setAggs).catch(() => {});
  }, []);

  useEffect(() => { loadBookings(); /* eslint-disable-next-line */ }, [page, sortBy, direction]);

  const viewPackageRatings = (packageId: number) => {
    if (openPkg === packageId) { setOpenPkg(null); return; }
    RatingAPI.packageList(packageId)
      .then((list) => { setPkgRatings(list); setOpenPkg(packageId); })
      .catch((e) => setErr(extractMessage(e)));
  };

  if (err) return <div className="alert alert-danger">{err}</div>;
  if (!d) return <div>Loading…</div>;

  return (
    <>
      <h5 className="mb-3">Reports</h5>

      {/* Booking statistics */}
      <div className="card-panel mb-3">
        <h6 className="mb-3">Bookings by Status</h6>
        <div className="row g-2">
          {Object.entries(d.bookingsByStatus).map(([k, v]) => (
            <div className="col-6 col-md-2" key={k}>
              <div className="text-center border rounded p-3">
                <div className="small text-muted">{k}</div>
                <div className="fs-4 fw-bold">{v}</div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Booking search & reports (SRS US10) */}
      <div className="card-panel mb-3">
        <h6 className="mb-3">Booking Search &amp; Reports</h6>
        <div className="row g-2 mb-3">
          <div className="col-md-3"><input className="form-control" placeholder="Reference number" value={reference}
           onChange={(e) => setReference(e.target.value)} /></div>
          <div className="col-md-2"><input className="form-control" placeholder="Customer name" value={customerName} 
          onChange={(e) => setCustomerName(e.target.value)} /></div>
          <div className="col-md-2"><select className="form-select" value={status} onChange={(e) => setStatus(e.target.value)}>
            <option value="">All statuses</option>
            {['PENDING', 'CONFIRMED', 'CHECKED_IN', 'COMPLETED', 'CANCELLED'].map((s) => <option key={s} value={s}>{s}</option>)}
          </select></div>
          <div className="col-md-2"><input type="date" className="form-control" value={date} onChange={(e) => 
            setDate(e.target.value)} /></div>
          <div className="col-md-2"><select className="form-select" value={sortBy} onChange={(e) =>
             { setSortBy(e.target.value); setPage(0); }}>
            <option value="createdAt">Sort: Created</option>
            <option value="bookingDate">Sort: Booking Date</option>
            <option value="status">Sort: Status</option>
            <option value="referenceNumber">Sort: Reference</option>
          </select></div>
          <div className="col-md-1"><select className="form-select" value={direction} onChange={(e) =>
             { setDirection(e.target.value as 'asc' | 'desc'); setPage(0); }}>
            <option value="desc">↓</option>
            <option value="asc">↑</option>
          </select></div>
        </div>
        <div className="mb-3">
          <button className="btn btn-outline-secondary btn-sm" onClick={() => { setPage(0); loadBookings(); }}>Search

          </button>
        </div>

        <div className="table-responsive">
          <table className="table align-middle">
            <thead><tr><th>Reference Number</th><th>Date &amp; Time</th><th>Status</th><th>Special Request</th></tr></thead>
            <tbody>
              {rows.map((b) => (
                <tr key={b.id}>
                  <td className="fw-semibold">{b.referenceNumber}</td>
                  <td>{b.bookingDate} · {b.timeSlot}</td>
                  <td><span className={`badge-status badge-${b.status}`}>{b.status}</span></td>
                  <td>{b.specialRequest || <span className="text-muted">—</span>}</td>
                </tr>
              ))}
              {rows.length === 0 && <tr><td colSpan={4} className="text-center text-muted py-4">No matching records found.
                </td></tr>}
            </tbody>
          </table>
        </div>
        {totalPages > 1 && (
          <div className="d-flex justify-content-between align-items-center">
            <button className="btn btn-sm btn-outline-secondary" disabled={page === 0} onClick={() => setPage(page - 1)}>Prev

            </button>
            <span className="small text-muted">Page {page + 1} of {totalPages}</span>
            <button className="btn btn-sm btn-outline-secondary" disabled={page >= totalPages - 1} onClick={() => 
              setPage(page + 1)}>Next</button>
          </div>
        )}
      </div>

      {/* Ratings: aggregated + individual drill-down (SRS US12) */}
      <div className="card-panel">
        <h6 className="mb-3">Ratings by Arena Package</h6>
        <table className="table align-middle">
          <thead><tr><th>Package ID</th><th>Average</th><th>Count</th><th></th></tr></thead>
          <tbody>
            {aggs.map((a) => (
              <>
                <tr key={a.packageId}>
                  <td>#{a.packageId}</td>
                  <td className="text-warning">{'★'.repeat(Math.round(a.average))} <span className="text-muted">{a.average}
                    </span></td>
                  <td>{a.count}</td>
                  <td className="text-end">
                    <button className="btn btn-sm btn-outline-secondary" onClick={() => viewPackageRatings(a.packageId)}>
                      {openPkg === a.packageId ? 'Hide reviews' : 'View reviews'}
                    </button>
                  </td>
                </tr>
                {openPkg === a.packageId && (
                  <tr key={`${a.packageId}-detail`}>
                    <td colSpan={4}>
                      <table className="table table-sm mb-0">
                        <thead><tr><th>Reference</th><th>Customer</th><th>Stars</th><th>Review</th></tr></thead>
                        <tbody>
                          {pkgRatings.map((r) => (
                            <tr key={r.id}>
                              <td>{r.referenceNumber}</td>
                              <td>{r.userId}</td>
                              <td className="text-warning">{'★'.repeat(r.stars)} <span className="text-muted">({r.stars})
                                </span></td>
                              <td>{r.review || <span className="text-muted">—</span>}</td>
                            </tr>
                          ))}
                          {pkgRatings.length === 0 && <tr><td colSpan={4} className="text-center text-muted">
                            No reviews for this package yet.</td></tr>}
                        </tbody>
                      </table>
                    </td>
                  </tr>
                )}
              </>
            ))}
            {aggs.length === 0 && <tr><td colSpan={4} className="text-center text-muted">No ratings submitted yet.</td></tr>}
          </tbody>
        </table>
      </div>
    </>
  );
};

export default Reports;
