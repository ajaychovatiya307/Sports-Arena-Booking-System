import { useEffect, useState, type FormEvent } from 'react';
import { CategoryAPI } from '../api/services';
import type { ArenaCategory } from '../types';
import { extractMessage } from '../api/axios';
import { EmptyRow, PageHeader } from '../components/ui';

const Categories = () => {
  const [rows, setRows] = useState<ArenaCategory[]>([]);
  const [q, setQ] = useState('');
  const [editing, setEditing] = useState<ArenaCategory | null>(null);
  const [err, setErr] = useState('');
  const [loading, setLoading] = useState(true);

  const load = () => CategoryAPI.list().then(setRows).catch((e) => setErr(extractMessage(e))).finally(() => setLoading(false));
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
      <PageHeader icon="bi-tags" title="Arena Categories" subtitle="Group arena packages so customers can browse them easily"
        actions={<button className="btn btn-brand" onClick={() => setEditing({ name: '', description: '' })}>
          <i className="bi bi-plus-lg" aria-hidden="true"></i> New Category</button>} />
      {err && <div className="alert alert-danger py-2" role="alert">{err}</div>}

      <div className="card-panel filters mb-3">
        <div className="input-icon">
          <i className="bi bi-search" aria-hidden="true"></i>
          <input className="form-control" placeholder="Filter by name or description" aria-label="Filter categories" value={q} onChange={(e) =>
             setQ(e.target.value)} />
        </div>
      </div>

      <div className="card-panel">
        <div className="table-responsive">
          <table className="table align-middle">
            <thead><tr><th>Code</th><th>Name</th><th>Description</th><th><span className="visually-hidden">Actions</span></th></tr></thead>
            <tbody>
              {filtered.map((c) => (
                <tr key={c.id}>
                  <td><span className="chip mono">{c.categoryCode}</span></td>
                  <td className="fw-semibold">{c.name}</td>
                  <td className="text-muted">{c.description}</td>
                  <td className="text-end row-actions">
                    <button className="btn btn-sm btn-outline-secondary btn-icon me-1" onClick={() => setEditing({ ...c })}
                      aria-label={`Edit ${c.name}`} title="Edit">
                      <i className="bi bi-pencil" aria-hidden="true"></i></button>
                    <button className="btn btn-sm btn-outline-danger btn-icon" onClick={() => remove(c.id!)}
                      aria-label={`Delete ${c.name}`} title="Delete">
                      <i className="bi bi-trash" aria-hidden="true"></i></button>
                  </td>
                </tr>
              ))}
              {filtered.length === 0 && <EmptyRow colSpan={4} loading={loading} icon="bi-tags" title="No categories" />}
            </tbody>
          </table>
        </div>
      </div>

      {editing && (
        <div className="modal show d-block" tabIndex={-1} role="dialog" aria-modal="true" aria-labelledby="cat-modal-title">
          <div className="modal-dialog modal-dialog-centered"><div className="modal-content">
            <form onSubmit={save}>
              <div className="modal-header">
                <h5 className="modal-title" id="cat-modal-title">{editing.id ? 'Edit Category' : 'New Category'}</h5>
                <button type="button" className="btn-close" aria-label="Close" onClick={() => setEditing(null)}></button>
              </div>
              <div className="modal-body">
                <div className="mb-3"><label className="form-label" htmlFor="cat-name">Name</label><input id="cat-name" className="form-control"
                value={editing.name} onChange={(e) => setEditing({ ...editing, name: e.target.value })} required /></div>
                <div className="mb-3"><label className="form-label" htmlFor="cat-desc">Description
                  </label><textarea id="cat-desc" className="form-control" rows={3} value={editing.description ?? ''} onChange={(e) =>
                    setEditing({ ...editing, description: e.target.value })} /></div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-outline-secondary" onClick={() => setEditing(null)}>Cancel</button>
                <button className="btn btn-brand" type="submit"><i className="bi bi-check2" aria-hidden="true"></i> Save</button>
              </div>
            </form>
          </div></div>
        </div>
      )}
    </>
  );
};

export default Categories;
