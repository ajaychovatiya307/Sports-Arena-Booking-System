import { useEffect, useState, type FormEvent } from 'react';
import { CategoryAPI } from '../api/services';
import type { ArenaCategory } from '../types';
import { extractMessage } from '../api/axios';

const Categories = () => {
  const [rows, setRows] = useState<ArenaCategory[]>([]);
  const [q, setQ] = useState('');
  const [editing, setEditing] = useState<ArenaCategory | null>(null);
  const [err, setErr] = useState('');

  const load = () => CategoryAPI.list().then(setRows).catch((e) => setErr(extractMessage(e)));
  useEffect(() => { load(); }, []);

  const filtered = rows.filter((c) =>
    !q || c.name.toLowerCase().includes(q.toLowerCase()) || (c.description?.toLowerCase().includes(q.toLowerCase()) ?? false));

  const save = async (e: FormEvent) => {
    e.preventDefault();
    if (!editing) return;
    setErr('');
    try {
      if (editing.id) await CategoryAPI.update(editing.id, editing);
      else await CategoryAPI.create(editing);
      setEditing(null); load();
    } catch (e2) { setErr(extractMessage(e2)); }
  };

  const remove = async (id: number) => {
    if (!confirm('Delete this category?')) return;
    try { await CategoryAPI.remove(id); load(); }
    catch (e) { setErr(extractMessage(e)); }
  };

  return (
    <>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h5 className="m-0">Arena Categories</h5>
        <button className="btn btn-brand btn-sm" onClick={() => setEditing({ name: '', description: '' })}>
          <i className="bi bi-plus-lg"></i> New Category</button>
      </div>
      {err && <div className="alert alert-danger py-2">{err}</div>}

      <div className="card-panel mb-3">
        <input className="form-control" placeholder="Filter by name or description" value={q} onChange={(e) =>
           setQ(e.target.value)} />
      </div>

      <div className="card-panel">
        <table className="table align-middle">
          <thead><tr><th>Code</th><th>Name</th><th>Description</th><th></th></tr></thead>
          <tbody>
            {filtered.map((c) => (
              <tr key={c.id}>
                <td className="small text-muted">{c.categoryCode}</td>
                <td className="fw-semibold">{c.name}</td>
                <td>{c.description}</td>
                <td className="text-end">
                  <button className="btn btn-sm btn-outline-secondary me-1" onClick={() => setEditing({ ...c })}>
                    <i className="bi bi-pencil"></i></button>
                  <button className="btn btn-sm btn-outline-danger" onClick={() => remove(c.id!)}>
                    <i className="bi bi-trash"></i></button>
                </td>
              </tr>
            ))}
            {filtered.length === 0 && <tr><td colSpan={4} className="text-center text-muted py-4">No categories</td></tr>}
          </tbody>
        </table>
      </div>

      {editing && (
        <div className="modal show d-block" tabIndex={-1} style={{ background: 'rgba(0,0,0,.4)' }}>
          <div className="modal-dialog"><div className="modal-content">
            <form onSubmit={save}>
              <div className="modal-header">
                <h5 className="modal-title">{editing.id ? 'Edit Category' : 'New Category'}</h5>
                <button type="button" className="btn-close" onClick={() => setEditing(null)}></button>
              </div>
              <div className="modal-body">
                <div className="mb-3"><label className="form-label">Name</label><input className="form-control" 
                value={editing.name} onChange={(e) => setEditing({ ...editing, name: e.target.value })} required /></div>
                <div className="mb-3"><label className="form-label">Description
                  </label><textarea className="form-control" rows={3} value={editing.description ?? ''} onChange={(e) => 
                    setEditing({ ...editing, description: e.target.value })} /></div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-outline-secondary" onClick={() => setEditing(null)}>Cancel</button>
                <button className="btn btn-brand" type="submit">Save</button>
              </div>
            </form>
          </div></div>
        </div>
      )}
    </>
  );
};

export default Categories;
