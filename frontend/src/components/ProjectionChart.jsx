import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip,
  ReferenceLine, ResponsiveContainer, Legend, Area, ComposedChart,
} from 'recharts';
import { formatRupees } from '../utils/formatters.js';

function yAxisFormatter(value) {
  const abs = Math.abs(value);
  if (abs >= 10000000) return `Rs ${(value / 10000000).toFixed(1)}Cr`;
  if (abs >= 100000) return `Rs ${(value / 100000).toFixed(1)}L`;
  if (abs >= 1000) return `Rs ${(value / 1000).toFixed(0)}k`;
  return `Rs ${value}`;
}

function findPaybackYear(projection) {
  for (const p of projection) {
    if (p.netPosition >= 0) return p.year;
  }
  return null;
}

export default function ProjectionChart({ projection, netCost }) {
  const data = projection.map((p) => ({
    year: p.year,
    cumulativeSavings: p.cumulativeSavings,
    netPosition: p.netPosition,
  }));
  const paybackYear = findPaybackYear(projection);
  const total25 = projection.length > 0 ? projection[projection.length - 1].cumulativeSavings : 0;

  return (
    <div data-testid="projection-chart" className="chart-card glass-card--hover" style={{ marginBottom: 28 }}>
      <h3 className="chart-card__title">Cumulative savings vs net position</h3>
      <div className="chart-card__body">
      <ResponsiveContainer width="100%" height="100%" minHeight={280}>
        <ComposedChart data={data} margin={{ top: 16, right: 24, left: 8, bottom: 8 }}>
          <defs>
            <linearGradient id="savingsGrad" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="#10b981" stopOpacity={0.35} />
              <stop offset="100%" stopColor="#10b981" stopOpacity={0} />
            </linearGradient>
          </defs>
          <CartesianGrid strokeDasharray="3 3" stroke="rgba(148,163,184,0.25)" />
          <XAxis dataKey="year" tick={{ fontSize: 12, fill: '#64748b' }} />
          <YAxis tickFormatter={yAxisFormatter} tick={{ fontSize: 11, fill: '#64748b' }} />
          <Tooltip
            formatter={(v) => formatRupees(v)}
            labelFormatter={(l) => `Year ${l}`}
            contentStyle={{ borderRadius: 12, border: '1px solid #e2e8f0', boxShadow: '0 8px 24px rgba(0,0,0,0.1)' }}
          />
          <Legend wrapperStyle={{ fontSize: 13, paddingTop: 12 }} />
          <ReferenceLine y={0} stroke="#94a3b8" strokeDasharray="4 4" />
          {paybackYear && (
            <ReferenceLine
              x={paybackYear}
              stroke="#10b981"
              strokeDasharray="4 4"
              label={{ value: `Break-even Y${paybackYear}`, position: 'top', fill: '#059669', fontSize: 12, fontWeight: 700 }}
            />
          )}
          <Area type="monotone" dataKey="cumulativeSavings" fill="url(#savingsGrad)" stroke="none" />
          <Line type="monotone" dataKey="cumulativeSavings" stroke="#10b981" strokeWidth={3} dot={false} name="Cumulative savings" />
          <Line type="monotone" dataKey="netPosition" stroke="#3b82f6" strokeWidth={2.5} dot={false} name="Net position" />
        </ComposedChart>
      </ResponsiveContainer>
      </div>
      <div className="chart-card__footer">
        <div className="chart-card__footer-label">Total 25-year savings</div>
        <div className="chart-card__footer-value">{formatRupees(total25)}</div>
        <div className="chart-card__footer-meta">Net investment after subsidy: {formatRupees(netCost)}</div>
      </div>
    </div>
  );
}
