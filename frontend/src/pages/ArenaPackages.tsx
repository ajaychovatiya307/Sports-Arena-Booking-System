import { useEffect, useState, type FormEvent } from 'react';
import { CategoryAPI, PackageAPI } from '../api/services';
import type { ArenaCategory, ArenaPackage } from '../types';
import { extractMessage } from '../api/axios';
import { EmptyRow, PageHeader, Pager, StatusBadge } from '../components/ui';

const empty: ArenaPackage = { name: '', description: '', pricePerHour: 500, durationHours: 1, categoryId: 0, status: 'ACTIVE' };

const ArenaPackages = () => {
  const [cats, setCats] = useState<ArenaCategory[]>([]);
  const [rows, setRows] = useState<ArenaPackage[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage] = useState(0);
  const [q, setQ] = useState('');
  const [editing, setEditing] = useState<ArenaPackage | null>(null);
  const [err, setErr] = useState('');
  const [loading, setLoading] = useState(true);

  const load = () => {
    setLoading(true);
    PackageAPI.list({ q, page, size: 10, sortBy: 'name', direction: 'asc' })
      .then((p) => { setRows(p.content); setTotalPages(p.totalPages); })
      .catch((e) => setErr(extractMessage(e)))
      .finally(() => setLoading(false));
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
      <PageHeader icon="bi-trophy" title="Arena Package Management" subtitle="Create, price and activate the arenas customers can book"
        actions={<button className="btn btn-brand" onClick={() => setEditing({ ...empty, categoryId: cats[0]?.id ?? 0 })}>
          <i className="bi bi-plus-lg" aria-hidden="true"></i> New Package</button>} />

      {err && <div className="alert alert-danger py-2" role="alert">{err}</div>}

      <div className="card-panel filters mb-3">
        <div className="row g-2">
          <div className="col-md-8 col-lg-6">
            <div className="input-icon">
              <i className="bi bi-search" aria-hidden="true"></i>
              <input className="form-control" placeholder="Search by name" aria-label="Search packages by name" value={q} onChange={(e) =>
                setQ(e.target.value)} onKeyDown={(e) => e.key === 'Enter' && (setPage(0), load())} />
            </div>
          </div>
          <div className="col-md-4 col-lg-2"><button className="btn btn-outline-secondary w-100" onClick={() => { setPage(0); load(); }}>
            <i className="bi bi-funnel" aria-hidden="true"></i> Search</button></div>
        </div>
      </div>

      <div className="card-panel">
        <div className="table-responsive">
          <table className="table align-middle">
            <thead><tr><th>Code</th><th>Name</th><th>Category</th><th>₹/hr</th><th>Duration</th><th>Status
              </th><th><span className="visually-hidden">Actions</span></th></tr></thead>
            <tbody>
              {rows.map((p) => (
                <tr key={p.id}>
                  <td><span className="chip mono">{p.packageCode}</span></td>
                  <td className="fw-semibold">{p.name}</td>
                  <td>{p.categoryName}</td>
                  <td className="fw-semibold">₹{p.pricePerHour}</td>
                  <td>{p.durationHours}h</td>
                  <td><StatusBadge status={p.status} /></td>
                  <td className="text-end row-actions">
                    <button className="btn btn-sm btn-outline-secondary btn-icon me-1" onClick={() => setEditing({ ...p })}
                      aria-label={`Edit ${p.name}`} title="Edit">
                      <i className="bi bi-pencil" aria-hidden="true"></i></button>
                    <button className="btn btn-sm btn-outline-danger btn-icon" onClick={() => remove(p.id!)}
                      aria-label={`Delete ${p.name}`} title="Delete">
                      <i className="bi bi-trash" aria-hidden="true"></i></button>
                  </td>
                </tr>
              ))}
              {rows.length === 0 && <EmptyRow colSpan={7} loading={loading} icon="bi-trophy" title="No packages" />}
            </tbody>
          </table>
        </div>
        <Pager page={page} totalPages={totalPages} onChange={setPage} />
      </div>

      {editing && (
        <div className="modal show d-block" tabIndex={-1} role="dialog" aria-modal="true" aria-labelledby="pkg-modal-title">
          <div className="modal-dialog modal-dialog-centered"><div className="modal-content">
            <form onSubmit={save}>
              <div className="modal-header">
                <h5 className="modal-title" id="pkg-modal-title">{editing.id ? 'Edit Package' : 'New Package'}</h5>
                <button type="button" className="btn-close" aria-label="Close" onClick={() => setEditing(null)}></button>
              </div>
              <div className="modal-body">
                <div className="row g-3">
                  <div className="col-12"><label className="form-label" htmlFor="pkg-name">Name</label><input id="pkg-name" className="form-control"
                  value={editing.name} onChange={(e) => setEditing({ ...editing, name: e.target.value })} required /></div>
                  <div className="col-12"><label className="form-label" htmlFor="pkg-desc">Description</label><textarea id="pkg-desc" className="form-control"
                  rows={2} value={editing.description ?? ''} onChange={(e) => setEditing({ ...editing, description: e.target.value })} /></div>
                  <div className="col-md-6"><label className="form-label" htmlFor="pkg-cat">Category</label>
                    <select id="pkg-cat" className="form-select" value={editing.categoryId} onChange={(e) =>
                      setEditing({ ...editing, categoryId: +e.target.value })} required>
                      {cats.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
                    </select>
                  </div>
                  <div className="col-6 col-md-3"><label className="form-label" htmlFor="pkg-price">₹ / hour</label><input id="pkg-price" type="number" step="0.01"
                   className="form-control" value={editing.pricePerHour} onChange={(e) =>
                   setEditing({ ...editing, pricePerHour: +e.target.value })} required /></div>
                  <div className="col-6 col-md-3"><label className="form-label" htmlFor="pkg-dur">Duration (h)
                    </label><input id="pkg-dur" type="number" min={1} className="form-control" value={editing.durationHours}
                    onChange={(e) => setEditing({ ...editing, durationHours: +e.target.value })} required /></div>
                  <div className="col-md-6"><label className="form-label" htmlFor="pkg-status">Status</label>
                    <select id="pkg-status" className="form-select" value={editing.status} onChange={(e) =>
                      setEditing({ ...editing, status: e.target.value as any })}>
                      <option value="ACTIVE">ACTIVE</option><option value="INACTIVE">INACTIVE</option>
                    </select>
                  </div>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-outline-secondary" onClick={() => setEditing(null)}>Cancel</button>
                <button type="submit" className="btn btn-brand"><i className="bi bi-check2" aria-hidden="true"></i> Save</button>
              </div>
            </form>
          </div></div>
        </div>
      )}
    </>
  );
};

export default ArenaPackages;
