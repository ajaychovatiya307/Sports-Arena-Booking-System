import { useEffect, useState } from 'react';
import { BookingAPI, PackageAPI, SlotAPI } from '../api/services';
import type { ArenaPackage, SlotAvailability } from '../types';
import { TIME_SLOTS } from '../types';
import { extractMessage } from '../api/axios';
import { PageHeader } from '../components/ui';

const today = new Date().toISOString().substring(0, 10);

const Slots = () => {
  const [pkgs, setPkgs] = useState<ArenaPackage[]>([]);
  const [packageId, setPackageId] = useState<number | ''>('');
  const [date, setDate] = useState(today);
  const [rows, setRows] = useState<SlotAvailability[]>([]);
  const [booked, setBooked] = useState<Record<string, string>>({}); // timeSlot -> status
  const [err, setErr] = useState('');

  useEffect(() => {
    PackageAPI.list({ size: 100 }).then((p) => {
      setPkgs(p.content);
      if (p.content[0]?.id) setPackageId(p.content[0].id);
    }).catch(() => {});
  }, []);

  const load = () => {
    if (!packageId) return;
    setErr('');
    SlotAPI.list(+packageId, date).then(setRows).catch((e) => setErr(extractMessage(e)));
    // Slots already held by customer bookings (shown as booked, not blockable).
    BookingAPI.bookedSlots(+packageId, date)
      .then((list) => {
        const map: Record<string, string> = {};
        list.forEach((b) => { map[b.timeSlot] = b.status; });
        setBooked(map);
      })
      .catch(() => setBooked({}));
  };

  useEffect(() => { load(); /* eslint-disable-next-line */ }, [packageId, date]);

  const toggle = async (slot: string) => {
    if (!packageId) return;

    // A slot held by a customer booking cannot be blocked directly (SRS US11).
    if (booked[slot]) {
      setErr('Please cancel the booking and then block the slot');
      return;
    }

    const existing = rows.find((r) => r.timeSlot === slot);
    const willBlock = !(existing?.blocked ?? false);
    setErr('');

    // When blocking a slot, capture an optional reason (SRS US11).
    let reason = existing?.reason;
    if (willBlock) {
      const entered = window.prompt('Reason for blocking this slot (optional):', existing?.reason ?? '');
      if (entered === null) return; // cancelled
      reason = entered.trim() || undefined;
    }

    try {
      await SlotAPI.upsert({
        id: existing?.id,
        packageId: +packageId,
        slotDate: date,
        timeSlot: slot,
        blocked: willBlock,
        reason
      });
      load();
    } catch (e) {
      setErr(extractMessage(e));
    }
  };

  // Display-only counts for the legend.
  const bookedCount = TIME_SLOTS.filter((s) => booked[s]).length;
  const blockedCount = TIME_SLOTS.filter((s) => !booked[s] && rows.find((r) => r.timeSlot === s)?.blocked).length;
  const freeCount = TIME_SLOTS.length - bookedCount - blockedCount;

  return (
    <>
      <PageHeader icon="bi-calendar3-week" title="Slot Availability Management"
        subtitle="Block slots for maintenance or events. Booked slots are locked until the booking is cancelled." />
      {err && <div className="alert alert-danger py-2" role="alert">{err}</div>}

      <div className="card-panel filters mb-3">
        <div className="row g-2 align-items-end">
          <div className="col-md-7 col-lg-6">
            <label className="form-label" htmlFor="slot-pkg">Arena Package</label>
            <select id="slot-pkg" className="form-select" value={packageId} onChange={(e) =>
              setPackageId(e.target.value ? +e.target.value : '')}>
              {pkgs.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}
            </select>
          </div>
          <div className="col-md-5 col-lg-3">
            <label className="form-label" htmlFor="slot-date">Date</label>
            <input id="slot-date" type="date" className="form-control" value={date} min={today} onChange={(e) => setDate(e.target.value)} />
          </div>
        </div>
      </div>

      <div className="card-panel">
        <div className="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
          <h6 className="section-title m-0"><i className="bi bi-clock" aria-hidden="true"></i> Slots for {date}</h6>
          <div className="slot-legend">
            <span><span className="sw" style={{ background: 'var(--completed)' }}></span>Booked by customer ({bookedCount})</span>
            <span><span className="sw" style={{ background: 'var(--cancelled-bg)', border: '1.5px solid #f2b8b0' }}></span>Blocked by admin ({blockedCount})</span>
            <span><span className="sw" style={{ background: '#fff', border: '1.5px solid #b9e3cd' }}></span>Available ({freeCount})</span>
          </div>
        </div>
        <div className="row g-2">
          {TIME_SLOTS.map((s) => {
            const row = rows.find((r) => r.timeSlot === s);
            const blocked = row?.blocked ?? false;
            const bookedStatus = booked[s];
            const cls = bookedStatus ? 'booked' : blocked ? 'blocked' : 'free';
            const icon = bookedStatus ? 'bi-person-check-fill' : blocked ? 'bi-lock-fill' : 'bi-unlock';
            const label = bookedStatus ? `${s}, booked (${bookedStatus})`
              : blocked ? `${s}, blocked. Click to unblock` : `${s}, available. Click to block`;
            return (
              <div className="col-6 col-md-4 col-xl-3" key={s}>
                <button type="button" className={`slot-tile ${cls}`} onClick={() => toggle(s)} aria-label={label}>
                  <div className="slot-time">{s} <i className={`bi ${icon}`} aria-hidden="true"></i></div>
                  {bookedStatus ? (
                    <div className="slot-state">Booked · {bookedStatus}</div>
                  ) : blocked ? (
                    <div className="slot-state">Blocked · click to unblock</div>
                  ) : (
                    <div className="slot-state">Available · click to block</div>
                  )}
                  {!bookedStatus && blocked && row?.reason && <div className="slot-reason">“{row.reason}”</div>}
                </button>
              </div>
            );
          })}
        </div>
      </div>
    </>
  );
};

export default Slots;
