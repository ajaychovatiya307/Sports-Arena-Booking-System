import { useEffect, useState, type FormEvent } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { BookingAPI, PackageAPI, SlotAPI, TeamAPI } from '../api/services';
import type { ArenaPackage, Booking, Team } from '../types';
import { TIME_SLOTS } from '../types';
import { extractMessage } from '../api/axios';
import { PageHeader } from '../components/ui';

const today = new Date().toISOString().substring(0, 10);

const BookingForm = () => {
  const nav = useNavigate();
  const loc = useLocation() as any;
  const preselectedPkg = loc.state?.packageId as number | undefined;

  const [teams, setTeams] = useState<Team[]>([]);
  const [pkgs, setPkgs] = useState<ArenaPackage[]>([]);
  const [blockedSlots, setBlockedSlots] = useState<string[]>([]);
  const [bookedSlots, setBookedSlots] = useState<string[]>([]);
  const [dto, setDto] = useState<Booking>({
    teamId: 0, packageId: preselectedPkg ?? 0, bookingDate: today, timeSlot: TIME_SLOTS[0]
  });
  const [err, setErr] = useState('');
  const [success, setSuccess] = useState<Booking | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    TeamAPI.list({ size: 100 }).then((p) => {
      setTeams(p.content);
      if (p.content[0]?.id && !dto.teamId) setDto((d) => ({ ...d, teamId: p.content[0].id! }));
    }).catch(() => {});
    PackageAPI.list({ activeOnly: true, size: 100 }).then((p) => {
      setPkgs(p.content);
      if (!preselectedPkg && p.content[0]?.id) setDto((d) => ({ ...d, packageId: p.content[0].id! }));
    }).catch(() => {});
    /* eslint-disable-next-line */
  }, []);

  // SRS US11: Blocked slots (admin) and slots already booked by another customer
  // shall not be selectable during booking.
  useEffect(() => {
    if (!dto.packageId || !dto.bookingDate) { setBlockedSlots([]); setBookedSlots([]); return; }
    Promise.all([
      SlotAPI.list(dto.packageId, dto.bookingDate).catch(() => []),
      BookingAPI.bookedSlots(dto.packageId, dto.bookingDate).catch(() => [])
    ]).then(([slots, booked]) => {
      const blocked = slots.filter((s) => s.blocked).map((s) => s.timeSlot);
      const taken = booked.map((b) => b.timeSlot);
      setBlockedSlots(blocked);
      setBookedSlots(taken);
      // If the currently selected slot is now unavailable, move to the first free one.
      const unavailable = new Set([...blocked, ...taken]);
      setDto((d) => unavailable.has(d.timeSlot)
        ? { ...d, timeSlot: TIME_SLOTS.find((t) => !unavailable.has(t)) ?? d.timeSlot }
        : d);
    });
    /* eslint-disable-next-line */
  }, [dto.packageId, dto.bookingDate]);

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    setErr('');
    if (blockedSlots.includes(dto.timeSlot)) {
      setErr('The selected time slot is blocked and cannot be booked. Please choose another slot.');
      return;
    }
    if (bookedSlots.includes(dto.timeSlot)) {
      setErr('The selected time slot is already booked. Please choose another slot.');
      return;
    }
    setSubmitting(true);
    try {
      const b = await BookingAPI.create(dto);
      setSuccess(b);
      setTimeout(() => nav('/bookings'), 2500);
    } catch (e2) { setErr(extractMessage(e2)); }
    finally { setSubmitting(false); }
  };

  if (success) {
    return (
      <div className="card-panel success-card mx-auto" style={{ maxWidth: 520 }} role="status">
        <div className="check" aria-hidden="true"><i className="bi bi-check-lg"></i></div>
        <h5 className="text-success">Booking Confirmed ✅</h5>
        <p className="text-muted mb-2">Your reference number:</p>
        <div className="ref">{success.referenceNumber}</div>
        <p className="text-muted mt-3 mb-3">Redirecting to your bookings…</p>
        <Link to="/bookings" className="btn btn-outline-secondary btn-sm">Go to My Bookings now</Link>
      </div>
    );
  }

  // Display-only summary derived from current form state.
  const selTeam = teams.find((t) => t.id === dto.teamId);
  const selPkg = pkgs.find((p) => p.id === dto.packageId);
  const unavailableCount = TIME_SLOTS.filter((s) => blockedSlots.includes(s) || bookedSlots.includes(s)).length;

  return (
    <>
      <PageHeader icon="bi-calendar-plus" title="Create Booking" subtitle="Choose a team, an arena and a time slot" />
      <div className="row g-4">
        <div className="col-lg-8">
          <div className="card-panel">
            {err && <div className="alert alert-danger py-2" role="alert">{err}</div>}
            <form onSubmit={submit}>
              <div className="row g-3">
                <div className="col-md-6">
                  <label className="form-label" htmlFor="bk-team">Team</label>
                  <select id="bk-team" className="form-select" value={dto.teamId} onChange={(e) => setDto({ ...dto, teamId: +e.target.value })}
                  required>
                    <option value={0} disabled>Select a team</option>
                    {teams.map((t) => <option key={t.id} value={t.id}>{t.teamName} ({t.sportType})</option>)}
                  </select>
                </div>
                <div className="col-md-6">
                  <label className="form-label" htmlFor="bk-pkg">Arena Package</label>
                  <select id="bk-pkg" className="form-select" value={dto.packageId} onChange={(e) =>
                     setDto({ ...dto, packageId: +e.target.value })} required>
                    <option value={0} disabled>Select an arena</option>
                    {pkgs.map((p) => <option key={p.id} value={p.id}>{p.name} — ₹{p.pricePerHour}/hr</option>)}
                  </select>
                </div>
                <div className="col-md-6">
                  <label className="form-label" htmlFor="bk-date">Date</label>
                  <input id="bk-date" type="date" min={today} className="form-control" value={dto.bookingDate} onChange={(e) =>
                    setDto({ ...dto, bookingDate: e.target.value })} required />
                  {(blockedSlots.length > 0 || bookedSlots.length > 0) && (
                    <div className="form-text text-muted"><i className="bi bi-info-circle" aria-hidden="true"></i> Note: Some slots are already booked for the above date.</div>
                  )}
                </div>
                <div className="col-md-6">
                  <label className="form-label" htmlFor="bk-slot">Time Slot</label>
                  <select id="bk-slot" className="form-select" value={dto.timeSlot} onChange={(e) => setDto({ ...dto, timeSlot: e.target.value })}
                   required>
                    {TIME_SLOTS.map((s) => {
                      const blocked = blockedSlots.includes(s);
                      const booked = bookedSlots.includes(s);
                      const label = blocked ? ' (Blocked)' : booked ? ' (Booked)' : '';
                      return <option key={s} value={s} disabled={blocked || booked}>{s}{label}</option>;
                    })}
                  </select>
                  <div className="form-text">{TIME_SLOTS.length - unavailableCount} of {TIME_SLOTS.length} slots available</div>
                </div>
                <div className="col-12">
                  <label className="form-label" htmlFor="bk-req">Special Request (optional)</label>
                  <textarea id="bk-req" className="form-control" rows={3} placeholder="e.g. extra bibs, equipment" value={dto.specialRequest ?? ''} onChange={(e) =>
                    setDto({ ...dto, specialRequest: e.target.value })} />
                </div>
              </div>
              <div className="mt-4 d-flex flex-wrap gap-2">
                <button className="btn btn-brand" type="submit" disabled={submitting}>
                  {submitting ? <><span className="spinner-border" aria-hidden="true"></span> Booking…</> : <><i className="bi bi-check2-circle" aria-hidden="true"></i> Confirm Booking</>}
                </button>
                <button type="button" className="btn btn-outline-secondary" onClick={() => nav(-1)}>Cancel</button>
              </div>
            </form>
          </div>
        </div>

        <div className="col-lg-4">
          <aside className="card-panel summary-card" aria-label="Booking summary">
            <h6 className="section-title mb-3"><i className="bi bi-receipt" aria-hidden="true"></i> Booking Summary</h6>
            <div className="sum-row"><span>Team</span><span>{selTeam?.teamName ?? '—'}</span></div>
            <div className="sum-row"><span>Arena</span><span>{selPkg?.name ?? '—'}</span></div>
            <div className="sum-row"><span>Category</span><span>{selPkg?.categoryName ?? '—'}</span></div>
            <div className="sum-row"><span>Date</span><span>{dto.bookingDate || '—'}</span></div>
            <div className="sum-row"><span>Time</span><span>{dto.timeSlot}</span></div>
            <div className="sum-total"><span className="text-muted">Price per hour</span><strong>{selPkg ? `₹${selPkg.pricePerHour}` : '—'}</strong></div>
            <p className="small text-muted mt-3 mb-0"><i className="bi bi-hourglass-split" aria-hidden="true"></i> New bookings start as PENDING until an administrator confirms them.</p>
          </aside>
        </div>
      </div>
    </>
  );
};

export default BookingForm;
