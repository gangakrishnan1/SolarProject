export function Skeleton({ width, height, className = '', circle = false }) {
  return (
    <div
      className={`skeleton ${circle ? 'skeleton--circle' : ''} ${className}`}
      style={{ width, height }}
      aria-hidden
    />
  );
}

export function SkeletonTable({ rows = 5, cols = 6 }) {
  return (
    <div className="skeleton-table glass-card" data-testid="skeleton-table">
      <div className="skeleton-table__head">
        {Array.from({ length: cols }).map((_, i) => (
          <Skeleton key={i} height={14} className="skeleton-table__cell" />
        ))}
      </div>
      {Array.from({ length: rows }).map((_, r) => (
        <div key={r} className="skeleton-table__row">
          {Array.from({ length: cols }).map((_, c) => (
            <Skeleton key={c} height={12} className="skeleton-table__cell" />
          ))}
        </div>
      ))}
    </div>
  );
}

export function SkeletonCards({ count = 4 }) {
  return (
    <div className="skeleton-cards">
      {Array.from({ length: count }).map((_, i) => (
        <div key={i} className="glass-card skeleton-card">
          <Skeleton height={12} width="60%" />
          <Skeleton height={28} width="40%" className="skeleton-card__value" />
        </div>
      ))}
    </div>
  );
}
