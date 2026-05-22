const PROPERTY_OPTIONS = [
  { value: 'residential', label: 'Residential', icon: '🏠', desc: 'Home or apartment' },
  { value: 'commercial', label: 'Commercial', icon: '🏢', desc: 'Office or shop' },
  { value: 'industrial', label: 'Industrial', icon: '🏭', desc: 'Factory or plant' },
];

const OWNERSHIP_OPTIONS = [
  { value: 'own', label: 'I own this property' },
  { value: 'rented', label: 'I rent this property' },
];

export default function StepProperty({ propertyType, ownership, onPropertyTypeChange, onOwnershipChange }) {
  return (
    <div data-testid="step-property">
      <h2 className="step-title">Property type</h2>
      <p className="step-subtitle">Commercial and industrial sites use different tariff multipliers and subsidy rules.</p>
      <div className="option-grid">
        {PROPERTY_OPTIONS.map((opt) => (
          <button
            key={opt.value}
            type="button"
            className="option-card"
            data-active={propertyType === opt.value ? 'true' : 'false'}
            data-testid={`property-${opt.value}`}
            onClick={() => onPropertyTypeChange(opt.value)}
          >
            <div className="option-card__icon">{opt.icon}</div>
            <div className="option-card__title">{opt.label}</div>
            <div className="option-card__desc">{opt.desc}</div>
          </button>
        ))}
      </div>
      <h3 className="step-title" style={{ fontSize: '1.15rem' }}>Ownership</h3>
      <div className="option-grid">
        {OWNERSHIP_OPTIONS.map((opt) => (
          <button
            key={opt.value}
            type="button"
            className="option-card"
            data-active={ownership === opt.value ? 'true' : 'false'}
            data-testid={`ownership-${opt.value}`}
            onClick={() => onOwnershipChange(opt.value)}
          >
            <div className="option-card__title">{opt.label}</div>
          </button>
        ))}
      </div>
    </div>
  );
}
