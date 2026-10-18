import { useEffect, useState } from 'react';
import { ProfileAPI } from '../api/services';
import type { CustomerProfile } from '../types';
import { extractMessage } from '../api/axios';

const Customers = () => {
  const [rows, setRows] = useState<CustomerProfile[]>([]);
  const [q, setQ] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [err, setErr] = useState('');

  const load = () => {
    ProfileAPI.list({ q, page, size: 10, sortBy: 'userId', direction: 'asc' })
      .then((p) => { setRows(p.content); setTotalPages(p.totalPages); })
      .catch((e) => setErr(extractMessage(e)));
  };

  useEffect(() => { load(); /* eslint-disable-next-line */ }, [page]);

  return (
    <>
      <h5 className="mb-3">Customers</h5>
      {err && <div className="alert alert-danger py-2">{err}</div>}

      <div className="card-panel mb-3">
        <div className="row g-2">
          <div className="col-md-6"><input className="form-control" placeholder="Search by name / email / user id" value={q} 
          onChange={(e) => setQ(e.target.value)} onKeyDown={(e) => e.key === 'Enter' && (setPage(0), load())} /></div>
          <div className="col-md-2"><button className="btn btn-outline-secondary w-100" onClick={() => { setPage(0); load(); }}>
            Search</button></div>
        </div>
      </div>

      <div className="card-panel">
        <table className="table align-middle">
          <thead><tr><th>User ID</th><th>Full Name</th><th>Email</th><th>Contact</th><th>Address</th></tr></thead>
          <tbody>
            {rows.map((c) => (
              <tr key={c.id}>
                <td>{c.userId}</td>
                <td className="fw-semibold">{c.fullName}</td>
                <td>{c.email}</td>
                <td>{c.contactNumber || '—'}</td>
                <td>{c.address || '—'}</td>
              </tr>
            ))}
            {rows.length === 0 && <tr><td colSpan={5} className="text-center text-muted py-4">No customers</td></tr>}
          </tbody>
        </table>
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
    </>
  );
};

export default Customers;
