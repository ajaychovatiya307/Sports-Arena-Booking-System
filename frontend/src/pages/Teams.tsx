import { useEffect, useState, type FormEvent } from 'react';
import { TeamAPI } from '../api/services';
import type { Team, SportType } from '../types';
import { SPORT_TYPES } from '../types';
import { extractMessage } from '../api/axios';
import { useAuth } from '../context/AuthContext';

const empty: Team = { teamName: '', sportType: 'FOOTBALL', numberOfPlayers: 11, coachName: '', 
  foundedYear: new Date().getFullYear() };

const Teams = () => {
  const { isAdmin } = useAuth();
  const [rows, setRows] = useState<Team[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage] = useState(0);
  const [q, setQ] = useState('');
  const [sportType, setSportType] = useState<string>('');
  const [editing, setEditing] = useState<Team | null>(null);
  const [err, setErr] = useState('');

  const load = () => {
    TeamAPI.list({ q, sportType, page, size: 10, sortBy: 'id', direction: 'desc' })
      .then((p) => { setRows(p.content); setTotalPages(p.totalPages); })
      .catch((e) => setErr(extractMessage(e)));
  };

  useEffect(() => { load(); /* eslint-disable-next-line */ }, [page, sportType]);

  const save = async (e: FormEvent) => {
    e.preventDefault();
    if (!editing) return;
    setErr('');
    try {
      if (editing.id) await TeamAPI.update(editing.id, editing);
      else await TeamAPI.create(editing);
      setEditing(null); load();
    } catch (e2) { setErr(extractMessage(e2)); }
  };

  const remove = async (id: number) => {
    if (!confirm('Delete this team?')) return;
    try { await TeamAPI.remove(id); load(); }
    catch (e) { setErr(extractMessage(e)); }
  };

  return (
    <>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h5 className="m-0">{isAdmin ? 'All Teams' : 'My Teams'}</h5>
        {!isAdmin && <button className="btn btn-brand btn-sm" onClick={() => setEditing({ ...empty })}>
          <i className="bi bi-plus-lg"></i> New Team</button>}
      </div>

      {err && <div className="alert alert-danger py-2">{err}</div>}

      <div className="card-panel mb-3">
        <div className="row g-2">
          <div className="col-md-5">
            <input className="form-control" placeholder="Search by name" value={q} onChange={(e) =>
               setQ(e.target.value)} onKeyDown={(e) => e.key === 'Enter' && (setPage(0), load())} />
          </div>
          <div className="col-md-3">
            <select className="form-select" value={sportType} onChange={(e) => { setSportType(e.target.value); setPage(0); }}>
              <option value="">All Sports</option>
              {SPORT_TYPES.map((s) => <option key={s} value={s}>{s}</option>)}
            </select>
          </div>
          <div className="col-md-2">
            <button className="btn btn-outline-secondary w-100" onClick={() => { setPage(0); load(); }}>Search</button>
          </div>
        </div>
      </div>

      <div className="card-panel">
        <div className="table-responsive">
          <table className="table align-middle">
            <thead><tr>
              <th>Team</th><th>Sport</th><th>Players</th><th>Coach</th><th>Founded</th>{isAdmin && <th>Owner</th>}<th></th>
            </tr></thead>
            <tbody>
              {rows.map((t) => (
                <tr key={t.id}>
                  <td className="fw-semibold">{t.teamName}</td>
                  <td><span className="badge bg-secondary">{t.sportType}</span></td>
                  <td>{t.numberOfPlayers}</td>
                  <td>{t.coachName || '—'}</td>
                  <td>{t.foundedYear}</td>
                  {isAdmin && <td>{t.userId}</td>}
                  <td className="text-end">
                    {!isAdmin && <>
                      <button className="btn btn-sm btn-outline-secondary me-1" onClick={() => setEditing({ ...t })}>
                        <i className="bi bi-pencil"></i></button>
                      <button className="btn btn-sm btn-outline-danger" onClick={() => remove(t.id!)}>
                        <i className="bi bi-trash"></i></button>
                    </>}
                  </td>
                </tr>
              ))}
              {rows.length === 0 && <tr><td colSpan={isAdmin ? 7 : 6} className="text-center text-muted py-4">
                No teams found</td></tr>}
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
                <h5 className="modal-title">{editing.id ? 'Edit Team' : 'New Team'}</h5>
                <button type="button" className="btn-close" onClick={() => setEditing(null)}></button>
              </div>
              <div className="modal-body">
                <div className="row g-3">
                  <div className="col-12">
                    <label className="form-label">Team Name</label>
                    <input className="form-control" value={editing.teamName} onChange={(e) =>
                       setEditing({ ...editing, teamName: e.target.value })} required />
                  </div>
                  <div className="col-md-6">
                    <label className="form-label">Sport Type</label>
                    <select className="form-select" value={editing.sportType} onChange={(e) => 
                      setEditing({ ...editing, sportType: e.target.value as SportType })}>
                      {SPORT_TYPES.map((s) => <option key={s} value={s}>{s}</option>)}
                    </select>
                  </div>
                  <div className="col-md-6">
                    <label className="form-label">Number of Players</label>
                    <input type="number" min={1} max={30} className="form-control" value={editing.numberOfPlayers} 
                    onChange={(e) => setEditing({ ...editing, numberOfPlayers: +e.target.value })} required />
                  </div>
                  <div className="col-md-6">
                    <label className="form-label">Coach Name</label>
                    <input className="form-control" value={editing.coachName ?? ''} onChange={(e) => 
                      setEditing({ ...editing, coachName: e.target.value })} />
                  </div>
                  <div className="col-md-6">
                    <label className="form-label">Founded Year</label>
                    <input type="number" min={1900} max={new Date().getFullYear()} className="form-control"
                     value={editing.foundedYear} onChange={(e) => setEditing({ ...editing, foundedYear: +e.target.value })} 
                     required />
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

export default Teams;
