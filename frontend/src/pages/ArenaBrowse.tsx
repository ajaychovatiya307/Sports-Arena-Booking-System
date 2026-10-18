import { useEffect, useState } from 'react';
import { CategoryAPI, PackageAPI, RatingAPI } from '../api/services';
import type { ArenaCategory, ArenaPackage } from '../types';
import { Link } from 'react-router-dom';
import { extractMessage } from '../api/axios';

const ArenaBrowse = () => {
  const [cats, setCats] = useState<ArenaCategory[]>([]);
  const [pkgs, setPkgs] = useState<ArenaPackage[]>([]);
  const [categoryId, setCategoryId] = useState<number | ''>('');
  const [q, setQ] = useState('');
  const [sortBy, setSortBy] = useState('name');
  const [direction, setDirection] = useState<'asc' | 'desc'>('asc');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [ratings, setRatings] = useState<Record<number, { average: number; count: number }>>({});
  const [err, setErr] = useState('');

  const PAGE_SIZE = 9;

  useEffect(() => { CategoryAPI.list().then(setCats).catch(() => { }); }, []);

  const load = () => {
    setErr('');
    PackageAPI.list({ q, categoryId: categoryId || undefined, activeOnly: true, sortBy, direction, page, size: PAGE_SIZE })
      .then((p) => {
        setPkgs(p.content);
        setTotalPages(p.totalPages);
        p.content.forEach((pkg) => {
          if (pkg.id != null && !ratings[pkg.id]) {
            RatingAPI.package(pkg.id).then((r) => setRatings((s) => 
              ({ ...s, [pkg.id!]: { average: r.average, count: r.count } }))).catch(() => { });
          }
        });
      })
      .catch((e) => setErr(extractMessage(e)));
  };

  // Reset to first page whenever a filter/sort changes.
  useEffect(() => { setPage(0); /* eslint-disable-next-line */ }, [categoryId, sortBy, direction]);
  useEffect(() => { load(); /* eslint-disable-next-line */ }, [categoryId, sortBy, direction, page]);

  return (
    <>
      <h5 className="mb-3">Browse Arena Packages</h5>
      {err && <div className="alert alert-danger py-2">{err}</div>}

      <div className="card-panel mb-3">
        <div className="row g-2">
          <div className="col-md-5">
            <input className="form-control" placeholder="Search by name" value={q} onChange={(e) => setQ(e.target.value)}
             onKeyDown={(e) => e.key === 'Enter' && (setPage(0), load())} />
          </div>
          <div className="col-md-3">
            <select className="form-select" value={categoryId} onChange={(e) => 
              setCategoryId(e.target.value ? +e.target.value : '')}>
              <option value="">All Categories</option>
              {cats.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
          </div>
          <div className="col-md-2">
            <select className="form-select" value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
              <option value="name">Sort by Name</option>
              <option value="pricePerHour">Sort by Price</option>
            </select>
          </div>
          <div className="col-md-2">
            <select className="form-select" value={direction} onChange={(e) => setDirection(e.target.value as 'asc' | 'desc')}>
              <option value="asc">Ascending</option>
              <option value="desc">Descending</option>
            </select>
          </div>
        </div>
      </div>

      <div className="row g-3">
        {pkgs.map((p) => (
          <div className="col-md-6 col-lg-4" key={p.id}>
            <div className="card h-100">
              <div className="card-body">
                <div className="d-flex justify-content-between mb-2">
                  <span className="badge bg-secondary">{p.categoryName}</span>
                  <span className="badge-status badge-ACTIVE">ACTIVE</span>
                </div>
                <h6 className="card-title">{p.name}</h6>
                <p className="text-muted small mb-2" style={{ minHeight: 40 }}>{p.description}</p>
                <div className="d-flex justify-content-between align-items-center mb-2">
                  <span className="fw-bold" style={{ color: '#146447' }}>₹{p.pricePerHour}/hr</span>
                  <span className="small text-muted">{p.durationHours}h slot</span>
                </div>
                {p.id != null && ratings[p.id] && ratings[p.id].count > 0 && (
                  <div className="small text-warning mb-2">
                    {'★'.repeat(Math.round(ratings[p.id].average))} <span className="text-muted">({ratings[p.id].average} · 
                      {ratings[p.id].count})</span>
                  </div>
                )}
                <Link to="/bookings/new" state={{ packageId: p.id }} className="btn btn-brand btn-sm w-100">Book This Arena
                </Link>
              </div>
            </div>
          </div>
        ))}
        {pkgs.length === 0 && <div className="text-center text-muted py-4">No arenas found</div>}
      </div>

      {totalPages > 1 && (
        <div className="d-flex justify-content-between align-items-center mt-3">
          <button className="btn btn-sm btn-outline-secondary" disabled={page === 0} onClick={() => setPage(page - 1)}>Prev

          </button>
          <span className="small text-muted">Page {page + 1} of {totalPages}</span>
          <button className="btn btn-sm btn-outline-secondary" disabled={page >= totalPages - 1} onClick={() => 
            setPage(page + 1)}>Next</button>
        </div>
      )}
    </>
  );
};

export default ArenaBrowse;
