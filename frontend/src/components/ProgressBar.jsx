export default function ProgressBar({ currentStep, totalSteps }) {
  const segments = Array.from({ length: totalSteps }, (_, i) => i + 1);
  return (
    <div className="progress-wrap" data-testid="progress-bar">
      <div className="progress-segments">
        {segments.map((n) => (
          <div
            key={n}
            className="progress-segment"
            data-step={n}
            data-active={n === currentStep ? 'true' : 'false'}
            data-complete={n < currentStep ? 'true' : 'false'}
          />
        ))}
      </div>
      <div className="progress-label">Step {currentStep} of {totalSteps}</div>
    </div>
  );
}
