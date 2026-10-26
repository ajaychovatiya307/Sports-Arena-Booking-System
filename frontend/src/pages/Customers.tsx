import { useEffect, useState } from 'react';
import { ProfileAPI } from '../api/services';
import type { CustomerProfile } from '../types';
import { extractMessage } from '../api/axios';
import { EmptyRow, PageHeader, Pager, initials } from '../components/ui';

const Customers = () => {
  const [rows, setRows] = useState<CustomerProfile[]>([]);
  const [q, setQ] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [err, setErr] = useState('');
  const [loading, setLoading] = useState(true);

  const load = () => {
    setLoading(true);
    ProfileAPI.list({ q, page, size: 10, sortBy: 'userId', direction: 'asc' })
      .then((p) => { setRows(p.content); setTotalPages(p.totalPages); })
      .catch((e) => setErr(extractMessage(e)))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); /* eslint-disable-next-line */ }, [page]);

  return (
    <>
      <PageHeader icon="bi-people" title="Customers" subtitle="Read-only directory of registered customer profiles" />
      {err && <div className="alert alert-danger py-2" role="alert">{err}</div>}

      <div className="card-panel filters mb-3">
        <div className="row g-2">
          <div className="col-md-8 col-lg-6">
            <div className="input-icon">
              <i className="bi bi-search" aria-hidden="true"></i>
              <input className="form-control" placeholder="Search by name / email / user id" aria-label="Search customers" value={q}
              onChange={(e) => setQ(e.target.value)} onKeyDown={(e) => e.key === 'Enter' && (setPage(0), load())} />
            </div>
          </div>
          <div className="col-md-4 col-lg-2"><button className="btn btn-outline-secondary w-100" onClick={() => { setPage(0); load(); }}>
            <i className="bi bi-funnel" aria-hidden="true"></i> Search</button></div>
        </div>
      </div>

      <div className="card-panel">
        <div className="table-responsive">
          <table className="table align-middle">
            <thead><tr><th>User ID</th><th>Full Name</th><th>Email</th><th>Contact</th><th>Address</th></tr></thead>
            <tbody>
              {rows.map((c) => (
                <tr key={c.id}>
                  <td className="mono">{c.userId}</td>
                  <td>
                    <div className="d-flex align-items-center gap-2">
                      <span className="user-chip"><span className="avatar" style={{ width: 30, height: 30, fontSize: '.72rem' }} aria-hidden="true">
                        {initials(c.fullName)}</span></span>
                      <span className="fw-semibold">{c.fullName}</span>
                    </div>
                  </td>
                  <td>{c.email}</td>
                  <td>{c.contactNumber || <span className="text-muted">—</span>}</td>
                  <td>{c.address || <span className="text-muted">—</span>}</td>
                </tr>
              ))}
              {rows.length === 0 && <EmptyRow colSpan={5} loading={loading} icon="bi-person-x" title="No customers" />}
            </tbody>
          </table>
        </div>
        <Pager page={page} totalPages={totalPages} onChange={setPage} />
      </div>
    </>
  );
};

export default Customers;
