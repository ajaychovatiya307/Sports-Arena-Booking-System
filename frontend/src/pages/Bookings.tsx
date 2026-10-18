import { useEffect, useState } from 'react';
import { BookingAPI, RatingAPI } from '../api/services';
import type { Booking, BookingStatus, Rating } from '../types';
import { extractMessage } from '../api/axios';
import { useAuth } from '../context/AuthContext';
import { Link } from 'react-router-dom';

const NEXT_STATES: Record<BookingStatus, BookingStatus[]> = {
  PENDING: ['CONFIRMED', 'CANCELLED'],
  CONFIRMED: ['CHECKED_IN', 'CANCELLED'],
  CHECKED_IN: ['COMPLETED'],
  COMPLETED: [],
  CANCELLED: []
};

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

  const load = () => {
    BookingAPI.search({
      reference: reference || undefined,
      customerName: isAdmin ? (customerName || undefined) : undefined,
      status: status || undefined,
      date: date || undefined,
      page, size: 10, sortBy, direction
    }).then((p) => { setRows(p.content); setTotalPages(p.totalPages); })
      .catch((e) => setErr(extractMessage(e)));
  };

  const loadMyRatings = () => {
    if (isAdmin) return;
    RatingAPI.mine().then(setMyRatings).catch(() => {});
  };

  useEffect(() => { load(); /* eslint-disable-next-line */ }, [page, sortBy, direction]);
  useEffect(() => { loadMyRatings(); /* eslint-disable-next-line */ }, []);

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
      alert('Thank you for your feedback!');
    } catch (e) { setErr(extractMessage(e)); }
  };

  return (
    <>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h5 className="m-0">{isAdmin ? 'All Bookings' : 'My Bookings'}</h5>
        <div className="d-flex gap-2">
          {!isAdmin && <button className="btn btn-outline-secondary btn-sm" onClick={() => setShowMyRatings((v) => !v)}>
            <i className="bi bi-star"></i> {showMyRatings ? 'Hide My Ratings' : 'My Ratings'}
          </button>}
          {!isAdmin && <Link to="/bookings/new" className="btn btn-brand btn-sm"><i className="bi bi-plus-lg"></i>
           New Booking</Link>}
        </div>
      </div>

      {err && <div className="alert alert-danger py-2">{err}</div>}

      {!isAdmin && showMyRatings && (
        <div className="card-panel mb-3">
          <h6 className="mb-3">My Ratings &amp; Reviews</h6>
          <div className="table-responsive">
            <table className="table align-middle mb-0">
              <thead><tr><th>Reference</th><th>Stars</th><th>Review</th></tr></thead>
              <tbody>
                {myRatings.map((r) => (
                  <tr key={r.id}>
                    <td className="fw-semibold">{r.referenceNumber}</td>
                    <td className="text-warning">{'★'.repeat(r.stars)}<span className="text-muted"> ({r.stars})</span></td>
                    <td>{r.review || <span className="text-muted">—</span>}</td>
                  </tr>
                ))}
                {myRatings.length === 0 && <tr><td colSpan={3} className="text-center text-muted py-3">
                  You haven't submitted any ratings yet.</td></tr>}
              </tbody>
            </table>
          </div>
        </div>
      )}

      <div className="card-panel mb-3">
        <div className="row g-2">
          <div className="col-md-3"><input className="form-control" placeholder="Reference" value={reference} onChange={(e) =>
             setReference(e.target.value)} /></div>
          {isAdmin && <div className="col-md-2"><input className="form-control" placeholder="Customer name" value={customerName}
           onChange={(e) => setCustomerName(e.target.value)} /></div>}
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
          <div className="col-md-2"><button className="btn btn-outline-secondary w-100" onClick={() => { setPage(0); load(); }}>
            Search</button></div>
        </div>
      </div>

      <div className="card-panel">
        <div className="table-responsive">
          <table className="table align-middle">
            <thead><tr>
              <th>Reference</th>{isAdmin && <th>Customer</th>}<th>Team</th><th>Package</th><th>Date</th><th>Slot</th><th>Amount
                </th><th>Status</th><th></th>
            </tr></thead>
            <tbody>
              {rows.map((b) => (
                <tr key={b.id}>
                  <td className="fw-semibold">{b.referenceNumber}</td>
                  {isAdmin && <td>{b.customerName}</td>}
                  <td>{b.teamName}</td>
                  <td>{b.packageName}</td>
                  <td>{b.bookingDate}</td>
                  <td>{b.timeSlot}</td>
                  <td>₹{b.totalAmount}</td>
                  <td><span className={`badge-status badge-${b.status}`}>{b.status}</span></td>
                  <td className="text-end">
                    {isAdmin && b.status && NEXT_STATES[b.status].length > 0 && (
                      <select className="form-select form-select-sm d-inline-block" style={{ width: 160 }}
                              defaultValue="" onChange={(e) => e.target.value && changeStatus(b.id!, e.target.value)}>
                        <option value="">Change status…</option>
                        {NEXT_STATES[b.status].map((s) => <option key={s} value={s}>{s}</option>)}
                      </select>
                    )}
                    {!isAdmin && b.status === 'PENDING' && (
                      <button className="btn btn-sm btn-outline-danger" onClick={() => cancel(b.id!)}>Cancel</button>
                    )}
                    {!isAdmin && b.status === 'COMPLETED' && (
                      ratedBookingIds.has(b.id!)
                        ? <button className="btn btn-sm btn-success ms-1" disabled><i className="bi bi-check2"></i> 
                        Rated</button>
                        : <button className="btn btn-sm btn-warning ms-1" onClick={() => setRatingFor(b)}>
                          <i className="bi bi-star"></i> Rate</button>
                    )}
                  </td>
                </tr>
              ))}
              {rows.length === 0 && <tr><td colSpan={isAdmin ? 9 : 8} className="text-center text-muted py-4">No bookings
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

      {ratingFor && (
        <div className="modal show d-block" tabIndex={-1} style={{ background: 'rgba(0,0,0,.4)' }}>
          <div className="modal-dialog"><div className="modal-content">
            <div className="modal-header">
              <h5 className="modal-title">Rate {ratingFor.referenceNumber}</h5>
              <button type="button" className="btn-close" onClick={() => setRatingFor(null)}></button>
            </div>
            <div className="modal-body">
              <div className="mb-3"><label className="form-label">Stars</label>
                <select className="form-select" value={stars} onChange={(e) => setStars(+e.target.value)}>
                  {[1,2,3,4,5].map((n) => <option key={n} value={n}>{'★'.repeat(n)} ({n})</option>)}
                </select>
              </div>
              <div className="mb-3"><label className="form-label">Review (optional)</label>
                <textarea className="form-control" rows={3} value={review} onChange={(e) => setReview(e.target.value)} />
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-outline-secondary" onClick={() => setRatingFor(null)}>Cancel</button>
              <button className="btn btn-brand" onClick={submitRating}>Submit</button>
            </div>
          </div></div>
        </div>
      )}
    </>
  );
};

export default Bookings;
