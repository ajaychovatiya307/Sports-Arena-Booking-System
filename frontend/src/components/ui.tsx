import type { ReactNode } from 'react';

// Presentational building blocks shared by the pages. No data fetching or state here.

export const PageHeader = ({ icon, title, subtitle, actions }:
  { icon: string; title: ReactNode; subtitle?: ReactNode; actions?: ReactNode }) => (
  <div className="page-header">
    <div className="ph-title">
      <div className="ph-icon" aria-hidden="true"><i className={`bi ${icon}`}></i></div>
      <div>
        <h5>{title}</h5>
        {subtitle && <p>{subtitle}</p>}
      </div>
    </div>
    {actions && <div className="ph-actions">{actions}</div>}
  </div>
);

export const Loader = ({ label = 'Loading…' }: { label?: string }) => (
  <div className="loader" role="status" aria-live="polite">
    <span className="spinner-border" aria-hidden="true"></span>
    <span>{label}</span>
  </div>
);

export const EmptyState = ({ icon = 'bi-inbox', title, sub }: { icon?: string; title: ReactNode; sub?: ReactNode }) => (
  <div className="empty-state">
    <div className="es-icon" aria-hidden="true"><i className={`bi ${icon}`}></i></div>
    <div className="es-title">{title}</div>
    {sub && <div className="es-sub">{sub}</div>}
  </div>
);

/** Full-width table row that shows a spinner while loading, otherwise an empty state. */
export const EmptyRow = ({ colSpan, loading, icon, title, sub }:
  { colSpan: number; loading?: boolean; icon?: string; title: ReactNode; sub?: ReactNode }) => (
  <tr>
    <td colSpan={colSpan} className="empty-cell p-0">
      {loading ? <Loader /> : <EmptyState icon={icon} title={title} sub={sub} />}
    </td>
  </tr>
);

export const Pager = ({ page, totalPages, onChange }:
  { page: number; totalPages: number; onChange: (p: number) => void }) =>
  totalPages > 1 ? (
    <nav className="pager" aria-label="Pagination">
      <button className="btn btn-sm btn-outline-secondary" disabled={page === 0} onClick={() => onChange(page - 1)}>
        <i className="bi bi-chevron-left" aria-hidden="true"></i> Prev</button>
      <span className="page-info">Page {page + 1} of {totalPages}</span>
      <button className="btn btn-sm btn-outline-secondary" disabled={page >= totalPages - 1} onClick={() => onChange(page + 1)}>
        Next <i className="bi bi-chevron-right" aria-hidden="true"></i></button>
    </nav>
  ) : null;

export const StatusBadge = ({ status }: { status?: string }) =>
  <span className={`badge-status badge-${status}`}>{status?.replace('_', ' ')}</span>;

export const Stars = ({ n }: { n: number }) =>
  <span className="stars" aria-label={`${n} out of 5 stars`}>{'★'.repeat(Math.round(n))}</span>;

/** Split-screen layout for the login and register pages. */
export const AuthLayout = ({ children }: { children: ReactNode }) => (
  <div className="auth-wrap">
    <section className="auth-hero" aria-hidden="true">
      <div className="brand"><span className="brand-mark"><i className="bi bi-controller"></i></span> Sports Arena</div>
      <div>
        <h1>Book your next game in a few clicks.</h1>
        <p className="lead">Find an arena, pick a time slot for your team and track every booking from one dashboard.</p>
        <ul>
          <li><i className="bi bi-trophy"></i> Turfs, courts and pools in one catalog</li>
          <li><i className="bi bi-calendar2-check"></i> Live slot availability, no double bookings</li>
          <li><i className="bi bi-people"></i> Manage your teams and rate every session</li>
        </ul>
      </div>
      <div className="hero-foot">© {new Date().getFullYear()} Sports Arena Booking System</div>
    </section>
    <main className="auth-panel">
      <div className="auth-card">
        <div className="auth-mobile-brand"><i className="bi bi-controller"></i> Sports Arena</div>
        {children}
      </div>
    </main>
  </div>
);

export const initials = (name?: string) =>
  (name ?? '').split(/\s+/).filter(Boolean).slice(0, 2).map((w) => w[0]!.toUpperCase()).join('') || '?';
