import { useEffect, useState, type FormEvent } from 'react';
import { CategoryAPI, PackageAPI } from '../api/services';
import type { ArenaCategory, ArenaPackage } from '../types';
import { extractMessage } from '../api/axios';

const empty: ArenaPackage = { name: '', description: '', pricePerHour: 500, durationHours: 1, categoryId: 0, status: 'ACTIVE' };

const ArenaPackages = () => {
  const [cats, setCats] = useState<ArenaCategory[]>([]);
  const [rows, setRows] = useState<ArenaPackage[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage] = useState(0);
  const [q, setQ] = useState('');
  const [editing, setEditing] = useState<ArenaPackage | null>(null);
  const [err, setErr] = useState('');

  const load = () => {
    PackageAPI.list({ q, page, size: 10, sortBy: 'name', direction: 'asc' })
      .then((p) => { setRows(p.content); setTotalPages(p.totalPages); })
      .catch((e) => setErr(extractMessage(e)));
  };

  useEffect(() => { CategoryAPI.list().then(setCats).catch(() => {}); }, []);
  useEffect(() => { load(); /* eslint-disable-next-line */ }, [page]);

  const save = async (e: FormEvent) => {
    e.preventDefault();
    if (!editing) return;
    setErr('');
    try {
      if (editing.id) await PackageAPI.update(editing.id, editing);
      else await PackageAPI.create(editing);
      setEditing(null); load();
    } catch (e2) { setErr(extractMessage(e2)); }
  };

  const remove = async (id: number) => {
    if (!confirm('Delete this arena package? Active bookings will trigger deactivation instead.')) return;
    try { await PackageAPI.remove(id); load(); }
    catch (e) { setErr(extractMessage(e)); load(); }
  };

  return (
    <>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h5 className="m-0">Arena Package Management</h5>
        <button className="btn btn-brand btn-sm" onClick={() => setEditing({ ...empty, categoryId: cats[0]?.id ?? 0 })}>
          <i className="bi bi-plus-lg"></i> New Package</button>
      </div>

      {err && <div className="alert alert-danger py-2">{err}</div>}

      <div className="card-panel mb-3">
        <div className="row g-2">
          <div className="col-md-6"><input className="form-control" placeholder="Search by name" value={q} onChange={(e) => 
            setQ(e.target.value)} onKeyDown={(e) => e.key === 'Enter' && (setPage(0), load())} /></div>
          <div className="col-md-2"><button className="btn btn-outline-secondary w-100" onClick={() => { setPage(0); load(); }}>
            Search</button></div>
        </div>
      </div>

      <div className="card-panel">
        <div className="table-responsive">
          <table className="table align-middle">
            <thead><tr><th>Code</th><th>Name</th><th>Category</th><th>₹/hr</th><th>Duration</th><th>Status
              </th><th></th></tr></thead>
            <tbody>
              {rows.map((p) => (
                <tr key={p.id}>
                  <td className="small text-muted">{p.packageCode}</td>
                  <td className="fw-semibold">{p.name}</td>
                  <td>{p.categoryName}</td>
                  <td>₹{p.pricePerHour}</td>
                  <td>{p.durationHours}h</td>
                  <td><span className={`badge-status badge-${p.status}`}>{p.status}</span></td>
                  <td className="text-end">
                    <button className="btn btn-sm btn-outline-secondary me-1" onClick={() => setEditing({ ...p })}>
                      <i className="bi bi-pencil"></i></button>
                    <button className="btn btn-sm btn-outline-danger" onClick={() => remove(p.id!)}>
                      <i className="bi bi-trash"></i></button>
                  </td>
                </tr>
              ))}
              {rows.length === 0 && <tr><td colSpan={7} className="text-center text-muted py-4">No packages</td></tr>}
            </tbody>
          </table>
        </div>
        {totalPages > 1 && (
          <div className="d-flex justify-content-between align-items-center">
            <button className="btn btn-sm btn-outline-secondary" disabled={page === 0} onClick={() => setPage(page - 1)}>
              Prev</button>
            <span className="small text-muted">Page {page + 1} of {totalPages}</span>
            <button className="btn btn-sm btn-outline-secondary" disabled={page >= totalPages - 1} onClick={() =>
               setPage(page + 1)}>Next</button>
          </div>
        )}
      </div>

      {editing && (
        <div className="modal show d-block" tabIndex={-1} style={{ background: 'rgba(0,0,0,.4)' }}>
          <div className="modal-dialog"><div className="modal-content">
            <form onSubmit={save}>
              <div className="modal-header">
                <h5 className="modal-title">{editing.id ? 'Edit Package' : 'New Package'}</h5>
                <button type="button" className="btn-close" onClick={() => setEditing(null)}></button>
              </div>
              <div className="modal-body">
                <div className="row g-3">
                  <div className="col-12"><label className="form-label">Name</label><input className="form-control" 
                  value={editing.name} onChange={(e) => setEditing({ ...editing, name: e.target.value })} required /></div>
                  <div className="col-12"><label className="form-label">Description</label><textarea className="form-control" 
                  rows={2} value={editing.description ?? ''} onChange={(e) => setEditing({ ...editing, description: e.target.value })} /></div>
                  <div className="col-md-6"><label className="form-label">Category</label>
                    <select className="form-select" value={editing.categoryId} onChange={(e) => 
                      setEditing({ ...editing, categoryId: +e.target.value })} required>
                      {cats.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
                    </select>
                  </div>
                  <div className="col-md-3"><label className="form-label">₹ / hour</label><input type="number" step="0.01"
                   className="form-control" value={editing.pricePerHour} onChange={(e) => 
                   setEditing({ ...editing, pricePerHour: +e.target.value })} required /></div>
                  <div className="col-md-3"><label className="form-label">Duration (h)
                    </label><input type="number" min={1} className="form-control" value={editing.durationHours} 
                    onChange={(e) => setEditing({ ...editing, durationHours: +e.target.value })} required /></div>
                  <div className="col-md-6"><label className="form-label">Status</label>
                    <select className="form-select" value={editing.status} onChange={(e) => 
                      setEditing({ ...editing, status: e.target.value as any })}>
                      <option value="ACTIVE">ACTIVE</option><option value="INACTIVE">INACTIVE</option>
                    </select>
                  </div>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-outline-secondary" onClick={() => setEditing(null)}>Cancel</button>
                <button type="submit" className="btn btn-brand">Save</button>
              </div>
            </form>
          </div></div>
        </div>
      )}
    </>
  );
};

export default ArenaPackages;
