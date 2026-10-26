import { Fragment, useEffect, useState } from 'react';
import { BookingAPI, RatingAPI } from '../api/services';
import type { Booking, DashboardSummary, Rating } from '../types';
import { extractMessage } from '../api/axios';
import { EmptyRow, Loader, PageHeader, Pager, Stars, StatusBadge } from '../components/ui';

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
  const [loading, setLoading] = useState(true);

  // ── Admin individual ratings drill-down (SRS US12) ──
  const [openPkg, setOpenPkg] = useState<number | null>(null);
  const [pkgRatings, setPkgRatings] = useState<Rating[]>([]);

  const loadBookings = () => {
    setLoading(true);
    BookingAPI.search({
      reference: reference || undefined,
      customerName: customerName || undefined,
      status: status || undefined,
      date: date || undefined,
      page, size: 10, sortBy, direction
    }).then((p) => { setRows(p.content); setTotalPages(p.totalPages); })
      .catch((e) => setErr(extractMessage(e)))
      .finally(() => setLoading(false));
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

  if (err) return <div className="alert alert-danger" role="alert">{err}</div>;
  if (!d) return <Loader label="Loading reports…" />;

  const statusTotal = Object.values(d.bookingsByStatus).reduce((a, b) => a + b, 0);

  return (
    <>
      <PageHeader icon="bi-graph-up" title="Reports" subtitle="Booking statistics, searchable records and customer ratings" />

      {/* Booking statistics */}
      <div className="card-panel mb-3">
        <div className="d-flex justify-content-between align-items-center mb-3">
          <h6 className="section-title m-0"><i className="bi bi-bar-chart" aria-hidden="true"></i> Bookings by Status</h6>
          <span className="chip"><i className="bi bi-calendar-check" aria-hidden="true"></i> {statusTotal} total</span>
        </div>
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

      {/* Booking search & reports (SRS US10) */}
      <div className="card-panel mb-3">
        <h6 className="mb-3"><i className="bi bi-search" aria-hidden="true"></i> Booking Search &amp; Reports</h6>
        <div className="row g-2 mb-3">
          <div className="col-md-6 col-lg-3"><input className="form-control" placeholder="Reference number" aria-label="Reference number" value={reference}
           onChange={(e) => setReference(e.target.value)} /></div>
          <div className="col-md-6 col-lg-2"><input className="form-control" placeholder="Customer name" aria-label="Customer name" value={customerName}
          onChange={(e) => setCustomerName(e.target.value)} /></div>
          <div className="col-6 col-md-4 col-lg-2"><select className="form-select" aria-label="Status" value={status} onChange={(e) => setStatus(e.target.value)}>
            <option value="">All statuses</option>
            {['PENDING', 'CONFIRMED', 'CHECKED_IN', 'COMPLETED', 'CANCELLED'].map((s) => <option key={s} value={s}>{s}</option>)}
          </select></div>
          <div className="col-6 col-md-4 col-lg-2"><input type="date" className="form-control" aria-label="Booking date" value={date} onChange={(e) =>
            setDate(e.target.value)} /></div>
          <div className="col-8 col-md-3 col-lg-2"><select className="form-select" aria-label="Sort by" value={sortBy} onChange={(e) =>
             { setSortBy(e.target.value); setPage(0); }}>
            <option value="createdAt">Sort: Created</option>
            <option value="bookingDate">Sort: Booking Date</option>
            <option value="status">Sort: Status</option>
            <option value="referenceNumber">Sort: Reference</option>
          </select></div>
          <div className="col-4 col-md-1 col-lg-1"><select className="form-select" aria-label="Sort direction" value={direction} onChange={(e) =>
             { setDirection(e.target.value as 'asc' | 'desc'); setPage(0); }}>
            <option value="desc">↓</option>
            <option value="asc">↑</option>
          </select></div>
        </div>
        <div className="mb-3">
          <button className="btn btn-outline-secondary btn-sm" onClick={() => { setPage(0); loadBookings(); }}>
            <i className="bi bi-funnel" aria-hidden="true"></i> Search</button>
        </div>

        <div className="table-responsive">
          <table className="table align-middle">
            <thead><tr><th>Reference Number</th><th>Date &amp; Time</th><th>Status</th><th>Special Request</th></tr></thead>
            <tbody>
              {rows.map((b) => (
                <tr key={b.id}>
                  <td className="fw-semibold mono">{b.referenceNumber}</td>
                  <td className="text-nowrap">{b.bookingDate} · {b.timeSlot}</td>
                  <td><StatusBadge status={b.status} /></td>
                  <td>{b.specialRequest || <span className="text-muted">—</span>}</td>
                </tr>
              ))}
              {rows.length === 0 && <EmptyRow colSpan={4} loading={loading} icon="bi-search" title="No matching records found." />}
            </tbody>
          </table>
        </div>
        <Pager page={page} totalPages={totalPages} onChange={setPage} />
      </div>

      {/* Ratings: aggregated + individual drill-down (SRS US12) */}
      <div className="card-panel">
        <h6 className="mb-3"><i className="bi bi-star" aria-hidden="true"></i> Ratings by Arena Package</h6>
        <div className="table-responsive">
          <table className="table align-middle">
            <thead><tr><th>Package ID</th><th>Average</th><th>Count</th><th><span className="visually-hidden">Actions</span></th></tr></thead>
            <tbody>
              {aggs.map((a) => (
                <Fragment key={a.packageId}>
                  <tr>
                    <td className="mono">#{a.packageId}</td>
                    <td><Stars n={a.average} /> <span className="text-muted">{a.average}</span></td>
                    <td>{a.count}</td>
                    <td className="text-end">
                      <button className="btn btn-sm btn-outline-secondary" onClick={() => viewPackageRatings(a.packageId)}
                        aria-expanded={openPkg === a.packageId}>
                        <i className={`bi ${openPkg === a.packageId ? 'bi-chevron-up' : 'bi-chevron-down'}`} aria-hidden="true"></i>
                        {openPkg === a.packageId ? 'Hide reviews' : 'View reviews'}
                      </button>
                    </td>
                  </tr>
                  {openPkg === a.packageId && (
                    <tr>
                      <td colSpan={4} style={{ background: 'var(--surface-2)' }}>
                        <table className="table table-sm mb-0">
                          <thead><tr><th>Reference</th><th>Customer</th><th>Stars</th><th>Review</th></tr></thead>
                          <tbody>
                            {pkgRatings.map((r) => (
                              <tr key={r.id}>
                                <td className="mono">{r.referenceNumber}</td>
                                <td className="mono">{r.userId}</td>
                                <td><Stars n={r.stars} /> <span className="text-muted">({r.stars})</span></td>
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
                </Fragment>
              ))}
              {aggs.length === 0 && <EmptyRow colSpan={4} icon="bi-star" title="No ratings submitted yet." />}
            </tbody>
          </table>
        </div>
      </div>
    </>
  );
};

export default Reports;
