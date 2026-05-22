import { formatKw } from '../utils/formatters.js';

export default function PanelRecommendationCard({ result }) {
  if (!result?.panelRecommendationSummary) return null;

  return (
    <section className="panel-rec-card" data-testid="panel-recommendation">
      <div className="panel-rec-card__header">
        <span className="panel-rec-card__badge">☀ Personalized system</span>
        <span className="panel-rec-card__area">{result.areaLabel || result.areaType} area</span>
      </div>
      <h2 className="panel-rec-card__title">
        {formatKw(result.recommendedSystemSizeKw || result.systemSizeKw)} recommended for you
      </h2>
      <p className="panel-rec-card__summary">{result.panelRecommendationSummary}</p>
      <div className="panel-rec-card__stats">
        <div className="panel-rec-stat">
          <span className="panel-rec-stat__label">Panels</span>
          <span className="panel-rec-stat__value">{result.recommendedPanelCount} × {result.panelWattage}W</span>
        </div>
        <div className="panel-rec-stat">
          <span className="panel-rec-stat__label">Usage-based size</span>
          <span className="panel-rec-stat__value">{formatKw(result.idealSystemSizeKw)}</span>
        </div>
        {result.sizeCappedForArea && (
          <div className="panel-rec-stat panel-rec-stat--highlight">
            <span className="panel-rec-stat__label">Area cap applied</span>
            <span className="panel-rec-stat__value">Optimized for locality</span>
          </div>
        )}
      </div>
    </section>
  );
}
