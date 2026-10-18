import { useEffect, useState, type FormEvent } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { BookingAPI, PackageAPI, SlotAPI, TeamAPI } from '../api/services';
import type { ArenaPackage, Booking, Team } from '../types';
import { TIME_SLOTS } from '../types';
import { extractMessage } from '../api/axios';

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
    try {
      const b = await BookingAPI.create(dto);
      setSuccess(b);
      setTimeout(() => nav('/bookings'), 2500);
    } catch (e2) { setErr(extractMessage(e2)); }
  };

  if (success) {
    return (
      <div className="card-panel" style={{ maxWidth: 520 }}>
        <h5 className="text-success">Booking Confirmed ✅</h5>
        <p>Your reference number:</p>
        <div className="display-6 fw-bold" style={{ color: '#146447' }}>{success.referenceNumber}</div>
        <p className="text-muted mt-2">Redirecting to your bookings…</p>
      </div>
    );
  }

  return (
    <div className="card-panel" style={{ maxWidth: 720 }}>
      <h5 className="mb-3">Create Booking</h5>
      {err && <div className="alert alert-danger py-2">{err}</div>}
      <form onSubmit={submit}>
        <div className="row g-3">
          <div className="col-md-6">
            <label className="form-label">Team</label>
            <select className="form-select" value={dto.teamId} onChange={(e) => setDto({ ...dto, teamId: +e.target.value })} 
            required>
              <option value={0} disabled>Select a team</option>
              {teams.map((t) => <option key={t.id} value={t.id}>{t.teamName} ({t.sportType})</option>)}
            </select>
          </div>
          <div className="col-md-6">
            <label className="form-label">Arena Package</label>
            <select className="form-select" value={dto.packageId} onChange={(e) =>
               setDto({ ...dto, packageId: +e.target.value })} required>
              <option value={0} disabled>Select an arena</option>
              {pkgs.map((p) => <option key={p.id} value={p.id}>{p.name} — ₹{p.pricePerHour}/hr</option>)}
            </select>
          </div>
          <div className="col-md-6">
            <label className="form-label">Date</label>
            <input type="date" min={today} className="form-control" value={dto.bookingDate} onChange={(e) => 
              setDto({ ...dto, bookingDate: e.target.value })} required />
            {(blockedSlots.length > 0 || bookedSlots.length > 0) && (
              <div className="form-text text-muted">Note: Some slots are already booked for the above date.</div>
            )}
          </div>
          <div className="col-md-6">
            <label className="form-label">Time Slot</label>
            <select className="form-select" value={dto.timeSlot} onChange={(e) => setDto({ ...dto, timeSlot: e.target.value })}
             required>
              {TIME_SLOTS.map((s) => {
                const blocked = blockedSlots.includes(s);
                const booked = bookedSlots.includes(s);
                const label = blocked ? ' (Blocked)' : booked ? ' (Booked)' : '';
                return <option key={s} value={s} disabled={blocked || booked}>{s}{label}</option>;
              })}
            </select>
          </div>
          <div className="col-12">
            <label className="form-label">Special Request (optional)</label>
            <textarea className="form-control" rows={3} value={dto.specialRequest ?? ''} onChange={(e) => 
              setDto({ ...dto, specialRequest: e.target.value })} />
          </div>
        </div>
        <div className="mt-4 d-flex gap-2">
          <button className="btn btn-brand" type="submit">Confirm Booking</button>
          <button type="button" className="btn btn-outline-secondary" onClick={() => nav(-1)}>Cancel</button>
        </div>
      </form>
    </div>
  );
};

export default BookingForm;
