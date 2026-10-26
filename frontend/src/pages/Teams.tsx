import { useEffect, useState, type FormEvent } from 'react';
import { TeamAPI } from '../api/services';
import type { Team, SportType } from '../types';
import { SPORT_TYPES } from '../types';
import { extractMessage } from '../api/axios';
import { useAuth } from '../context/AuthContext';
import { EmptyRow, PageHeader, Pager } from '../components/ui';

const empty: Team = { teamName: '', sportType: 'FOOTBALL', numberOfPlayers: 11, coachName: '',
  foundedYear: new Date().getFullYear() };

const SPORT_EMOJI: Record<string, string> = { FOOTBALL: '⚽', CRICKET: '🏏', BADMINTON: '🏸', TENNIS: '🎾', BASKETBALL: '🏀' };

const Teams = () => {
  const { isAdmin } = useAuth();
  const [rows, setRows] = useState<Team[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage] = useState(0);
  const [q, setQ] = useState('');
  const [sportType, setSportType] = useState<string>('');
  const [editing, setEditing] = useState<Team | null>(null);
  const [err, setErr] = useState('');
  const [loading, setLoading] = useState(true);

  const load = () => {
    setLoading(true);
    TeamAPI.list({ q, sportType, page, size: 10, sortBy: 'id', direction: 'desc' })
      .then((p) => { setRows(p.content); setTotalPages(p.totalPages); })
      .catch((e) => setErr(extractMessage(e)))
      .finally(() => setLoading(false));
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
      <PageHeader icon="bi-people-fill" title={isAdmin ? 'All Teams' : 'My Teams'}
        subtitle={isAdmin ? 'Read-only view of every registered team' : 'Create and manage the teams you book arenas for'}
        actions={!isAdmin && <button className="btn btn-brand" onClick={() => setEditing({ ...empty })}>
          <i className="bi bi-plus-lg" aria-hidden="true"></i> New Team</button>} />

      {err && <div className="alert alert-danger py-2" role="alert">{err}</div>}

      <div className="card-panel filters mb-3">
        <div className="row g-2">
          <div className="col-md-6 col-lg-5">
            <div className="input-icon">
              <i className="bi bi-search" aria-hidden="true"></i>
              <input className="form-control" placeholder="Search by name" aria-label="Search teams by name" value={q} onChange={(e) =>
                 setQ(e.target.value)} onKeyDown={(e) => e.key === 'Enter' && (setPage(0), load())} />
            </div>
          </div>
          <div className="col-7 col-md-3">
            <select className="form-select" aria-label="Filter by sport" value={sportType} onChange={(e) => { setSportType(e.target.value); setPage(0); }}>
              <option value="">All Sports</option>
              {SPORT_TYPES.map((s) => <option key={s} value={s}>{s}</option>)}
            </select>
          </div>
          <div className="col-5 col-md-3 col-lg-2">
            <button className="btn btn-outline-secondary w-100" onClick={() => { setPage(0); load(); }}>
              <i className="bi bi-funnel" aria-hidden="true"></i> Search</button>
          </div>
        </div>
      </div>

      <div className="card-panel">
        <div className="table-responsive">
          <table className="table align-middle">
            <thead><tr>
              <th>Team</th><th>Sport</th><th>Players</th><th>Coach</th><th>Founded</th>{isAdmin && <th>Owner</th>}<th><span className="visually-hidden">Actions</span></th>
            </tr></thead>
            <tbody>
              {rows.map((t) => (
                <tr key={t.id}>
                  <td className="fw-semibold">{t.teamName}</td>
                  <td><span className="chip"><span aria-hidden="true">{SPORT_EMOJI[t.sportType] ?? '🏅'}</span> {t.sportType}</span></td>
                  <td>{t.numberOfPlayers}</td>
                  <td>{t.coachName || <span className="text-muted">—</span>}</td>
                  <td>{t.foundedYear}</td>
                  {isAdmin && <td className="mono">{t.userId}</td>}
                  <td className="text-end row-actions">
                    {!isAdmin && <>
                      <button className="btn btn-sm btn-outline-secondary btn-icon me-1" onClick={() => setEditing({ ...t })}
                        aria-label={`Edit ${t.teamName}`} title="Edit">
                        <i className="bi bi-pencil" aria-hidden="true"></i></button>
                      <button className="btn btn-sm btn-outline-danger btn-icon" onClick={() => remove(t.id!)}
                        aria-label={`Delete ${t.teamName}`} title="Delete">
                        <i className="bi bi-trash" aria-hidden="true"></i></button>
                    </>}
                  </td>
                </tr>
              ))}
              {rows.length === 0 && <EmptyRow colSpan={isAdmin ? 7 : 6} loading={loading} icon="bi-people"
                title="No teams found" sub={!isAdmin ? 'Create your first team to start booking.' : undefined} />}
            </tbody>
          </table>
        </div>
        <Pager page={page} totalPages={totalPages} onChange={setPage} />
      </div>

      {editing && (
        <div className="modal show d-block" tabIndex={-1} role="dialog" aria-modal="true" aria-labelledby="team-modal-title">
          <div className="modal-dialog modal-dialog-centered"><div className="modal-content">
            <form onSubmit={save}>
              <div className="modal-header">
                <h5 className="modal-title" id="team-modal-title">{editing.id ? 'Edit Team' : 'New Team'}</h5>
                <button type="button" className="btn-close" aria-label="Close" onClick={() => setEditing(null)}></button>
              </div>
              <div className="modal-body">
                <div className="row g-3">
                  <div className="col-12">
                    <label className="form-label" htmlFor="team-name">Team Name</label>
                    <input id="team-name" className="form-control" value={editing.teamName} onChange={(e) =>
                       setEditing({ ...editing, teamName: e.target.value })} required />
                  </div>
                  <div className="col-md-6">
                    <label className="form-label" htmlFor="team-sport">Sport Type</label>
                    <select id="team-sport" className="form-select" value={editing.sportType} onChange={(e) =>
                      setEditing({ ...editing, sportType: e.target.value as SportType })}>
                      {SPORT_TYPES.map((s) => <option key={s} value={s}>{s}</option>)}
                    </select>
                  </div>
                  <div className="col-md-6">
                    <label className="form-label" htmlFor="team-players">Number of Players</label>
                    <input id="team-players" type="number" min={1} max={30} className="form-control" value={editing.numberOfPlayers}
                    onChange={(e) => setEditing({ ...editing, numberOfPlayers: +e.target.value })} required />
                  </div>
                  <div className="col-md-6">
                    <label className="form-label" htmlFor="team-coach">Coach Name</label>
                    <input id="team-coach" className="form-control" value={editing.coachName ?? ''} onChange={(e) =>
                      setEditing({ ...editing, coachName: e.target.value })} />
                  </div>
                  <div className="col-md-6">
                    <label className="form-label" htmlFor="team-founded">Founded Year</label>
                    <input id="team-founded" type="number" min={1900} max={new Date().getFullYear()} className="form-control"
                     value={editing.foundedYear} onChange={(e) => setEditing({ ...editing, foundedYear: +e.target.value })}
                     required />
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

export default Teams;
