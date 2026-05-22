export default function TipsList({ tips, roofAreaSufficient }) {
  return (
    <div data-testid="tips-list" className="tips-card">
      {tips.map((tip, idx) => (
        <div key={idx} className="tip-item" data-testid="tip-item">
          <div className="tip-item__icon">✓</div>
          <div className="tip-item__text">{tip}</div>
        </div>
      ))}
      {roofAreaSufficient === false && (
        <div data-testid="roof-warning" className="roof-warning">
          <strong>Roof space note:</strong> Your roof is smaller than recommended — we can size a smaller array to fit. Book a site survey for exact layout.
        </div>
      )}
    </div>
  );
}
