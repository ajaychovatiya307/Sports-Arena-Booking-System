import { useEffect, useState } from 'react';
import { CategoryAPI, PackageAPI, RatingAPI } from '../api/services';
import type { ArenaCategory, ArenaPackage } from '../types';
import { Link } from 'react-router-dom';
import { extractMessage } from '../api/axios';
import { EmptyState, PageHeader, Pager, Stars } from '../components/ui';

// Display-only: pick an icon and colour tone from the category name.
const lookFor = (category?: string): [string, number] => {
  const c = (category ?? '').toLowerCase();
  if (/aqua|pool|swim/.test(c)) return ['bi-water', 2];
  if (/indoor|court/.test(c)) return ['bi-building', 1];
  if (/outdoor|turf|field/.test(c)) return ['bi-tree', 0];
  return ['bi-trophy', 4];
};

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
  const [loading, setLoading] = useState(true);

  const PAGE_SIZE = 9;

  useEffect(() => { CategoryAPI.list().then(setCats).catch(() => { }); }, []);

  const load = () => {
    setErr('');
    setLoading(true);
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
      .catch((e) => setErr(extractMessage(e)))
      .finally(() => setLoading(false));
  };

  // Reset to first page whenever a filter/sort changes.
  useEffect(() => { setPage(0); /* eslint-disable-next-line */ }, [categoryId, sortBy, direction]);
  useEffect(() => { load(); /* eslint-disable-next-line */ }, [categoryId, sortBy, direction, page]);

  return (
    <>
      <PageHeader icon="bi-trophy" title="Browse Arena Packages" subtitle="Find the right venue for your team and book a slot" />
      {err && <div className="alert alert-danger py-2" role="alert">{err}</div>}

      <div className="card-panel filters mb-4">
        <div className="row g-2">
          <div className="col-md-12 col-lg-5">
            <div className="input-icon">
              <i className="bi bi-search" aria-hidden="true"></i>
              <input className="form-control" placeholder="Search by name — press Enter" aria-label="Search arenas by name" value={q}
                onChange={(e) => setQ(e.target.value)} onKeyDown={(e) => e.key === 'Enter' && (setPage(0), load())} />
            </div>
          </div>
          <div className="col-md-4 col-lg-3">
            <select className="form-select" aria-label="Filter by category" value={categoryId} onChange={(e) =>
              setCategoryId(e.target.value ? +e.target.value : '')}>
              <option value="">All Categories</option>
              {cats.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
          </div>
          <div className="col-6 col-md-4 col-lg-2">
            <select className="form-select" aria-label="Sort by" value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
              <option value="name">Sort by Name</option>
              <option value="pricePerHour">Sort by Price</option>
            </select>
          </div>
          <div className="col-6 col-md-4 col-lg-2">
            <select className="form-select" aria-label="Sort direction" value={direction} onChange={(e) => setDirection(e.target.value as 'asc' | 'desc')}>
              <option value="asc">Ascending</option>
              <option value="desc">Descending</option>
            </select>
          </div>
        </div>
      </div>

      <div className="row g-3">
        {loading && pkgs.length === 0 && [0, 1, 2].map((i) => (
          <div className="col-md-6 col-lg-4" key={`sk-${i}`} aria-hidden="true">
            <div className="skeleton" style={{ height: 290, borderRadius: 12 }} />
          </div>
        ))}
        {pkgs.map((p) => { const [glyph, tone] = lookFor(p.categoryName); return (
          <div className="col-md-6 col-lg-4" key={p.id}>
            <article className="arena-card">
              <div className={`ac-media ac-tone-${tone}`}>
                <i className={`bi ${glyph} ac-glyph`} aria-hidden="true"></i>
                <span className="chip"><i className="bi bi-tag" aria-hidden="true"></i> {p.categoryName}</span>
                <span className="badge-status badge-ACTIVE">ACTIVE</span>
              </div>
              <div className="ac-body">
                <h6 className="ac-title">{p.name}</h6>
                <p className="ac-desc">{p.description}</p>
                <div className="ac-meta">
                  <span className="ac-price">₹{p.pricePerHour}<small>/hr</small></span>
                  <span className="chip"><i className="bi bi-clock" aria-hidden="true"></i> {p.durationHours}h slot</span>
                </div>
                <div className="ac-rating">
                  {p.id != null && ratings[p.id] && ratings[p.id].count > 0
                    ? <><Stars n={ratings[p.id].average} /> <span>{ratings[p.id].average} · {ratings[p.id].count} review{ratings[p.id].count === 1 ? '' : 's'}</span></>
                    : <span><i className="bi bi-star" aria-hidden="true"></i> No ratings yet</span>}
                </div>
                <Link to="/bookings/new" state={{ packageId: p.id }} className="btn btn-brand w-100">
                  <i className="bi bi-calendar-plus" aria-hidden="true"></i> Book This Arena</Link>
              </div>
            </article>
          </div>
        ); })}
        {!loading && pkgs.length === 0 && (
          <div className="col-12"><div className="card-panel">
            <EmptyState icon="bi-search" title="No arenas found" sub="Try a different search term or category." />
          </div></div>
        )}
      </div>

      <Pager page={page} totalPages={totalPages} onChange={setPage} />
    </>
  );
};

export default ArenaBrowse;
