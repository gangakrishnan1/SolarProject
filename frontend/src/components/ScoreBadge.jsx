export default function ScoreBadge({ score, tier }) {
  const t = (tier || '').toLowerCase();
  let color = '#6b7280';
  let bg = '#f1f5f9';
  let label = 'Cold';
  let cls = 'score-badge--cold';
  if (t === 'hot') { color = '#b91c1c'; bg = '#fee2e2'; label = 'Hot'; cls = 'score-badge--hot'; }
  else if (t === 'warm') { color = '#b45309'; bg = '#fef3c7'; label = 'Warm'; cls = 'score-badge--warm'; }
  return (
    <span
      data-testid="score-badge"
      data-tier={t}
      className={`score-badge ${cls}`}
      style={{ background: bg, color }}
    >
      <span>{label}</span>
      <span style={{ opacity: 0.85 }}>{score}</span>
    </span>
  );
}

export function tierFor(score) {
  if (score >= 75) return 'hot';
  if (score >= 50) return 'warm';
  return 'cold';
}
