import { useEffect, useState } from 'react';
import { BookingAPI, RatingAPI } from '../api/services';
import type { Booking, BookingStatus, Rating } from '../types';
import { extractMessage } from '../api/axios';
import { useAuth } from '../context/AuthContext';
import { Link } from 'react-router-dom';
import { EmptyRow, PageHeader, Pager, Stars, StatusBadge } from '../components/ui';

const NEXT_STATES: Record<BookingStatus, BookingStatus[]> = {
  PENDING: ['CONFIRMED', 'CANCELLED'],
  CONFIRMED: ['CHECKED_IN', 'CANCELLED'],
  CHECKED_IN: ['COMPLETED'],
  COMPLETED: [],
  CANCELLED: []
};

const STAR_LABELS = ['', 'Poor', 'Fair', 'Good', 'Very good', 'Excellent'];

const Bookings = () => {
  const { isAdmin } = useAuth();
  const [rows, setRows] = useState<Booking[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [reference, setReference] = useState('');
  const [customerName, setCustomerName] = useState('');
  const [status, setStatus] = useState<string>('');
  const [date, setDate] = useState('');
  const [sortBy, setSortBy] = useState('createdAt');
  const [direction, setDirection] = useState<'asc' | 'desc'>('desc');
  const [err, setErr] = useState('');
  const [ratingFor, setRatingFor] = useState<Booking | null>(null);
  const [stars, setStars] = useState(5);
  const [review, setReview] = useState('');
  const [myRatings, setMyRatings] = useState<Rating[]>([]);
  const [showMyRatings, setShowMyRatings] = useState(false);
  const [loading, setLoading] = useState(true);
  const [toast, setToast] = useState('');

  const load = () => {
    setLoading(true);
    BookingAPI.search({
      reference: reference || undefined,
      customerName: isAdmin ? (customerName || undefined) : undefined,
      status: status || undefined,
      date: date || undefined,
      page, size: 10, sortBy, direction
    }).then((p) => { setRows(p.content); setTotalPages(p.totalPages); })
      .catch((e) => setErr(extractMessage(e)))
      .finally(() => setLoading(false));
  };

  const loadMyRatings = () => {
    if (isAdmin) return;
    RatingAPI.mine().then(setMyRatings).catch(() => {});
  };

  useEffect(() => { load(); /* eslint-disable-next-line */ }, [page, sortBy, direction]);
  useEffect(() => { loadMyRatings(); /* eslint-disable-next-line */ }, []);
  useEffect(() => {
    if (!toast) return;
    const t = setTimeout(() => setToast(''), 3500);
    return () => clearTimeout(t);
  }, [toast]);

  const ratedBookingIds = new Set(myRatings.map((r) => r.bookingId));

  const cancel = async (id: number) => {
    if (!confirm('Cancel this booking?')) return;
    try { await BookingAPI.cancel(id); load(); }
    catch (e) { setErr(extractMessage(e)); }
  };

  const changeStatus = async (id: number, s: string) => {
    try { await BookingAPI.updateStatus(id, s); load(); }
    catch (e) { setErr(extractMessage(e)); }
  };

  const submitRating = async () => {
    if (!ratingFor?.id) return;
    try {
      await RatingAPI.submit({ bookingId: ratingFor.id, stars, review });
      setRatingFor(null); setStars(5); setReview('');
      loadMyRatings();
      setToast('Thank you for your feedback!');
    } catch (e) { setErr(extractMessage(e)); }
  };

  return (
    <>
      <PageHeader icon="bi-calendar-check" title={isAdmin ? 'All Bookings' : 'My Bookings'}
        subtitle={isAdmin ? 'Search bookings and move them through the lifecycle' : 'Track, cancel and rate your arena bookings'}
        actions={!isAdmin && <>
          <button className="btn btn-outline-secondary" onClick={() => setShowMyRatings((v) => !v)} aria-expanded={showMyRatings}>
            <i className={`bi ${showMyRatings ? 'bi-star-fill' : 'bi-star'}`} aria-hidden="true"></i> {showMyRatings ? 'Hide My Ratings' : 'My Ratings'}
          </button>
          <Link to="/bookings/new" className="btn btn-brand"><i className="bi bi-plus-lg" aria-hidden="true"></i>
           New Booking</Link>
        </>} />

      {err && <div className="alert alert-danger py-2" role="alert">{err}</div>}

      {!isAdmin && showMyRatings && (
        <div className="card-panel mb-3">
          <h6 className="mb-3"><i className="bi bi-star" aria-hidden="true"></i> My Ratings &amp; Reviews</h6>
          <div className="table-responsive">
            <table className="table align-middle mb-0">
              <thead><tr><th>Reference</th><th>Stars</th><th>Review</th></tr></thead>
              <tbody>
                {myRatings.map((r) => (
                  <tr key={r.id}>
                    <td className="fw-semibold mono">{r.referenceNumber}</td>
                    <td><Stars n={r.stars} /><span className="text-muted"> ({r.stars})</span></td>
                    <td>{r.review || <span className="text-muted">—</span>}</td>
                  </tr>
                ))}
                {myRatings.length === 0 && <EmptyRow colSpan={3} icon="bi-star" title="You haven't submitted any ratings yet." />}
              </tbody>
            </table>
          </div>
        </div>
      )}

      <div className="card-panel filters mb-3">
        <div className="row g-2">
          <div className={`col-md-6 ${isAdmin ? 'col-lg-2' : 'col-lg-3'}`}>
            <div className="input-icon">
              <i className="bi bi-hash" aria-hidden="true"></i>
              <input className="form-control" placeholder="Reference" aria-label="Reference number" value={reference} onChange={(e) =>
                 setReference(e.target.value)} />
            </div>
          </div>
          {isAdmin && <div className="col-md-6 col-lg-2">
            <div className="input-icon">
              <i className="bi bi-person" aria-hidden="true"></i>
              <input className="form-control" placeholder="Customer name" aria-label="Customer name" value={customerName}
               onChange={(e) => setCustomerName(e.target.value)} />
            </div>
          </div>}
          <div className="col-6 col-md-4 col-lg-2"><select className="form-select" aria-label="Status" value={status} onChange={(e) => setStatus(e.target.value)}>
            <option value="">All statuses</option>
            {['PENDING', 'CONFIRMED', 'CHECKED_IN', 'COMPLETED', 'CANCELLED'].map((s) => <option key={s} value={s}>{s}</option>)}
          </select></div>
          <div className="col-6 col-md-4 col-lg-2"><input type="date" className="form-control" aria-label="Booking date" value={date} onChange={(e) =>
            setDate(e.target.value)} /></div>
          <div className="col-8 col-md-4 col-lg-2"><select className="form-select" aria-label="Sort by" value={sortBy} onChange={(e) =>
            { setSortBy(e.target.value); setPage(0); }}>
            <option value="createdAt">Sort: Created</option>
            <option value="bookingDate">Sort: Booking Date</option>
            <option value="status">Sort: Status</option>
            <option value="referenceNumber">Sort: Reference</option>
          </select></div>
          <div className="col-4 col-md-2 col-lg-1"><select className="form-select" aria-label="Sort direction" value={direction} onChange={(e) =>
            { setDirection(e.target.value as 'asc' | 'desc'); setPage(0); }}>
            <option value="desc">↓</option>
            <option value="asc">↑</option>
          </select></div>
          <div className={`col-12 col-md-4 ${isAdmin ? 'col-lg-1' : 'col-lg-2'}`}><button className="btn btn-outline-secondary w-100" aria-label="Search" onClick={() => { setPage(0); load(); }}>
            <i className="bi bi-funnel" aria-hidden="true"></i> <span className={isAdmin ? 'd-lg-none' : ''}>Search</span></button></div>
        </div>
      </div>

      <div className="card-panel">
        <div className="table-responsive">
          <table className={`table align-middle${isAdmin ? ' table-compact' : ''}`}>
            <thead><tr>
              <th>Reference</th>{isAdmin && <th>Customer</th>}<th>Team</th><th>Package</th><th>Date</th><th>Slot</th><th>Amount
                </th><th>Status</th><th><span className="visually-hidden">Actions</span></th>
            </tr></thead>
            <tbody>
              {rows.map((b) => (
                <tr key={b.id}>
                  <td className="fw-semibold mono">{b.referenceNumber}</td>
                  {isAdmin && <td>{b.customerName}</td>}
                  <td>{b.teamName}</td>
                  <td>{b.packageName}</td>
                  <td className="text-nowrap">{b.bookingDate}</td>
                  <td className="text-nowrap">{b.timeSlot}</td>
                  <td className="fw-semibold text-nowrap">₹{b.totalAmount}</td>
                  <td><StatusBadge status={b.status} /></td>
                  <td className="text-end row-actions">
                    {isAdmin && b.status && NEXT_STATES[b.status].length > 0 && (
                      <select className="form-select form-select-sm d-inline-block" style={{ width: 138 }}
                              aria-label={`Change status of ${b.referenceNumber}`}
                              defaultValue="" onChange={(e) => e.target.value && changeStatus(b.id!, e.target.value)}>
                        <option value="">Change status…</option>
                        {NEXT_STATES[b.status].map((s) => <option key={s} value={s}>{s}</option>)}
                      </select>
                    )}
                    {!isAdmin && b.status === 'PENDING' && (
                      <button className="btn btn-sm btn-outline-danger" onClick={() => cancel(b.id!)}>
                        <i className="bi bi-x-circle" aria-hidden="true"></i> Cancel</button>
                    )}
                    {!isAdmin && b.status === 'COMPLETED' && (
                      ratedBookingIds.has(b.id!)
                        ? <button className="btn btn-sm btn-success ms-1" disabled><i className="bi bi-check2" aria-hidden="true"></i>
                        Rated</button>
                        : <button className="btn btn-sm btn-warning ms-1" onClick={() => setRatingFor(b)}>
                          <i className="bi bi-star" aria-hidden="true"></i> Rate</button>
                    )}
                  </td>
                </tr>
              ))}
              {rows.length === 0 && <EmptyRow colSpan={isAdmin ? 9 : 8} loading={loading} icon="bi-calendar-x" title="No bookings"
                sub={!isAdmin ? <Link to="/bookings/new" className="link-plain">Make your first booking</Link> : 'Try adjusting the filters.'} />}
            </tbody>
          </table>
        </div>
        <Pager page={page} totalPages={totalPages} onChange={setPage} />
      </div>

      {ratingFor && (
        <div className="modal show d-block" tabIndex={-1} role="dialog" aria-modal="true" aria-labelledby="rate-modal-title">
          <div className="modal-dialog modal-dialog-centered"><div className="modal-content">
            <div className="modal-header">
              <h5 className="modal-title" id="rate-modal-title">Rate {ratingFor.referenceNumber}</h5>
              <button type="button" className="btn-close" aria-label="Close" onClick={() => setRatingFor(null)}></button>
            </div>
            <div className="modal-body">
              <p className="text-muted small mb-3">{ratingFor.packageName} · {ratingFor.bookingDate} · {ratingFor.timeSlot}</p>
              <div className="mb-3">
                <span className="form-label d-block" id="stars-label">Stars</span>
                <div className="d-flex align-items-center gap-1" role="radiogroup" aria-labelledby="stars-label">
                  {[1, 2, 3, 4, 5].map((n) => (
                    <button key={n} type="button" role="radio" aria-checked={stars === n} aria-label={`${n} star${n > 1 ? 's' : ''}`}
                      className="btn btn-link p-0 fs-3 text-decoration-none" style={{ color: n <= stars ? 'var(--accent)' : 'var(--border-strong)' }}
                      onClick={() => setStars(n)}>
                      <i className={`bi ${n <= stars ? 'bi-star-fill' : 'bi-star'}`} aria-hidden="true"></i>
                    </button>
                  ))}
                  <span className="ms-2 small fw-semibold text-muted">{STAR_LABELS[stars]} ({stars})</span>
                </div>
              </div>
              <div className="mb-1"><label className="form-label" htmlFor="rate-review">Review (optional)</label>
                <textarea id="rate-review" className="form-control" rows={3} placeholder="How was the session?" value={review} onChange={(e) => setReview(e.target.value)} />
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-outline-secondary" onClick={() => setRatingFor(null)}>Cancel</button>
              <button className="btn btn-brand" onClick={submitRating}><i className="bi bi-send" aria-hidden="true"></i> Submit</button>
            </div>
          </div></div>
        </div>
      )}

      {toast && <div className="toast-float" role="status" aria-live="polite"><i className="bi bi-check-circle-fill" aria-hidden="true"></i> {toast}</div>}
    </>
  );
};

export default Bookings;
