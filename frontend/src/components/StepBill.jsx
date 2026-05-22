import { formatRupees } from '../utils/formatters.js';

const MIN = 500;
const MAX = 50000;
const STEP = 500;

export default function StepBill({ value, onChange, error }) {
  return (
    <div data-testid="step-bill">
      <h2 className="step-title">Monthly electricity bill</h2>
      <p className="step-subtitle">Drag the slider to match your average power spend — we size your solar system from this.</p>
      <div className="bill-display" data-testid="bill-display">{formatRupees(value)}</div>
      <input
        type="range"
        className="range-input"
        min={MIN}
        max={MAX}
        step={STEP}
        value={value}
        onChange={(e) => onChange(Number(e.target.value))}
      />
      <div style={{ display: 'flex', justifyContent: 'space-between', color: 'var(--color-text-muted)', fontSize: 13, marginTop: 10 }}>
        <span>{formatRupees(MIN)}</span>
        <span>{formatRupees(MAX)}</span>
      </div>
      {error && <div className="error-text">{error}</div>}
    </div>
  );
}
