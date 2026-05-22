import { formatRupees, formatKw } from '../utils/formatters.js';

export default function HeroBanner({ monthlySavings, systemSizeKw, paybackYears, city, state }) {
  return (
    <div className="hero-banner" data-testid="hero-banner">
      <div className="hero-banner__label">Your estimated savings</div>
      <div className="hero-banner__amount" data-testid="hero-savings">
        {formatRupees(monthlySavings)}
        <span className="hero-banner__amount-suffix"> / month</span>
      </div>
      <div className="hero-banner__stats">
        <div className="hero-stat">
          <div className="hero-stat__label">Recommended system</div>
          <div className="hero-stat__value">{formatKw(systemSizeKw)}</div>
        </div>
        <div className="hero-stat">
          <div className="hero-stat__label">Payback period</div>
          <div className="hero-stat__value">{paybackYears} years</div>
        </div>
        <div className="hero-stat">
          <div className="hero-stat__label">Location</div>
          <div className="hero-stat__value">{city}{state ? `, ${state}` : ''}</div>
        </div>
      </div>
    </div>
  );
}
