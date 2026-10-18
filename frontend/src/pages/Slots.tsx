import { useEffect, useState } from 'react';
import { BookingAPI, PackageAPI, SlotAPI } from '../api/services';
import type { ArenaPackage, SlotAvailability } from '../types';
import { TIME_SLOTS } from '../types';
import { extractMessage } from '../api/axios';

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

  return (
    <>
      <h5 className="mb-3">Slot Availability Management</h5>
      {err && <div className="alert alert-danger py-2">{err}</div>}

      <div className="card-panel mb-3">
        <div className="row g-2 align-items-end">
          <div className="col-md-6">
            <label className="form-label">Arena Package</label>
            <select className="form-select" value={packageId} onChange={(e) => 
              setPackageId(e.target.value ? +e.target.value : '')}>
              {pkgs.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}
            </select>
          </div>
          <div className="col-md-4">
            <label className="form-label">Date</label>
            <input type="date" className="form-control" value={date} min={today} onChange={(e) => setDate(e.target.value)} />
          </div>
        </div>
      </div>

      <div className="card-panel">
        <h6 className="mb-2">Slots for {date}</h6>
        <div className="d-flex flex-wrap gap-3 mb-3 small text-muted">
          <span><span className="badge bg-success">&nbsp;</span> Booked by customer</span>
          <span><span className="badge bg-danger">&nbsp;</span> Blocked by admin</span>
          <span><span className="badge bg-white border border-success">&nbsp;</span> Available</span>
        </div>
        <div className="row g-2">
          {TIME_SLOTS.map((s) => {
            const row = rows.find((r) => r.timeSlot === s);
            const blocked = row?.blocked ?? false;
            const bookedStatus = booked[s];
            const cls = bookedStatus ? 'btn-success' : blocked ? 'btn-danger' : 'btn-outline-success';
            return (
              <div className="col-6 col-md-3" key={s}>
                <button className={`btn w-100 ${cls}`} onClick={() => toggle(s)}>
                  <div className="fw-bold">{s}</div>
                  {bookedStatus ? (
                    <div className="small">Booked · {bookedStatus}</div>
                  ) : blocked ? (
                    <div className="small">Blocked · click to unblock</div>
                  ) : (
                    <div className="small">Available · click to block</div>
                  )}
                  {!bookedStatus && blocked && row?.reason && <div className="small fst-italic">“{row.reason}”</div>}
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
