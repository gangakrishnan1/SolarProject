const ROOF_OPTIONS = [
  { value: 'flat_rcc', label: 'Flat RCC', desc: 'Concrete roof — common in apartments' },
  { value: 'sloped', label: 'Sloped', desc: 'Tiled or metal sheet roof' },
];

export default function StepRoof({ roofArea, roofType, onRoofAreaChange, onRoofTypeChange }) {
  return (
    <div data-testid="step-roof">
      <h2 className="step-title">Roof details</h2>
      <p className="step-subtitle">Optional roof area helps us check if your space fits the recommended panel layout.</p>
      <label className="field-label">
        Roof area (sq ft) <span style={{ fontWeight: 400, color: 'var(--color-text-muted)' }}>optional</span>
      </label>
      <input
        type="number"
        className="input-field"
        value={roofArea}
        min="0"
        onChange={(e) => onRoofAreaChange(e.target.value)}
        placeholder="e.g. 500"
      />
      <p className="helper-text">Leave blank if unsure — we will estimate from your bill.</p>
      <h3 className="step-title" style={{ fontSize: '1.15rem', marginTop: 24 }}>Roof type</h3>
      <div className="option-grid">
        {ROOF_OPTIONS.map((opt) => (
          <button
            key={opt.value}
            type="button"
            className="option-card"
            data-active={roofType === opt.value ? 'true' : 'false'}
            data-testid={`roof-${opt.value}`}
            onClick={() => onRoofTypeChange(opt.value)}
          >
            <div className="option-card__title">{opt.label}</div>
            <div className="option-card__desc">{opt.desc}</div>
          </button>
        ))}
      </div>
    </div>
  );
}
