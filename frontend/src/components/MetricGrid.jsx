import { formatRupees, formatKw, formatTonnes } from '../utils/formatters.js';

function MetricCard({ label, value, sub, highlight, testId }) {
  return (
    <div
      data-testid={testId}
      className={`metric-card ${highlight ? 'metric-card--highlight' : ''}`}
    >
      <div className="metric-card__label">{label}</div>
      <div className="metric-card__value">{value}</div>
      {sub && <div className="metric-card__sub">{sub}</div>}
    </div>
  );
}

export default function MetricGrid({ result }) {
  const sizeKw = result.recommendedSystemSizeKw || result.systemSizeKw;
  return (
    <div className="metric-grid" data-testid="metric-grid">
      <MetricCard testId="metric-system-size" label="Recommended size" value={formatKw(sizeKw)} />
      <MetricCard testId="metric-install-cost" label="Installation cost" value={formatRupees(result.installCost)} />
      <MetricCard
        testId="metric-subsidy"
        label="MNRE subsidy"
        value={formatRupees(result.subsidyAmount)}
        sub={result.subsidyAmount > 0 ? 'PM Surya Ghar' : 'Not applicable'}
        highlight
      />
      <MetricCard testId="metric-net-cost" label="Net cost after subsidy" value={formatRupees(result.netCost)} />
      <MetricCard testId="metric-annual-savings" label="Annual savings" value={formatRupees(result.annualSavings)} />
      <MetricCard testId="metric-co2" label="CO₂ offset / year" value={formatTonnes(result.co2OffsetKg)} sub="grid equivalent" />
    </div>
  );
}
