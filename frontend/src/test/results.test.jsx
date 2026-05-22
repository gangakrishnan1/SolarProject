import { describe, it, expect } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import HeroBanner from '../components/HeroBanner.jsx';
import PanelRecommendationCard from '../components/PanelRecommendationCard.jsx';
import MetricGrid from '../components/MetricGrid.jsx';
import TipsList from '../components/TipsList.jsx';
import ScoreBadge from '../components/ScoreBadge.jsx';
import ContactModal from '../components/ContactModal.jsx';

const sampleResult = {
  systemSizeKw: 5.0,
  installCost: 250000,
  monthlySavings: 4500,
  annualSavings: 54000,
  paybackYears: 3.9,
  co2OffsetKg: 6140,
  subsidyAmount: 40000,
  netCost: 210000,
};

describe('HeroBanner', () => {
  it('displays the monthly savings amount', () => {
    render(<HeroBanner monthlySavings={4500} systemSizeKw={5} paybackYears={3.9} city="Hyderabad" state="Telangana" />);
    expect(screen.getByTestId('hero-savings').textContent).toMatch(/4,500/);
  });
});

describe('PanelRecommendationCard', () => {
  it('renders when panel summary is present', () => {
    render(
      <PanelRecommendationCard
        result={{
          recommendedSystemSizeKw: 6,
          idealSystemSizeKw: 10,
          panelRecommendationSummary: 'Semi-urban cap applied.',
          recommendedPanelCount: 12,
          panelWattage: 540,
          areaLabel: 'Semi-Urban',
        }}
      />
    );
    expect(screen.getByTestId('panel-recommendation')).toBeInTheDocument();
  });
});

describe('MetricGrid', () => {
  it('renders 6 cards', () => {
    render(<MetricGrid result={sampleResult} />);
    const grid = screen.getByTestId('metric-grid');
    expect(grid.children.length).toBe(6);
  });
});

describe('TipsList', () => {
  it('renders 3 tip items', () => {
    render(<TipsList tips={['a', 'b', 'c']} roofAreaSufficient={true} />);
    expect(screen.getAllByTestId('tip-item').length).toBe(3);
  });

  it('shows the warning row when roofAreaSufficient is false', () => {
    render(<TipsList tips={['a', 'b', 'c']} roofAreaSufficient={false} />);
    expect(screen.getByTestId('roof-warning')).toBeInTheDocument();
  });
});

describe('ScoreBadge', () => {
  it('shows Hot text for score 85', () => {
    render(<ScoreBadge score={85} tier="hot" />);
    expect(screen.getByTestId('score-badge').textContent).toContain('Hot');
  });

  it('shows Warm text for score 60', () => {
    render(<ScoreBadge score={60} tier="warm" />);
    expect(screen.getByTestId('score-badge').textContent).toContain('Warm');
  });

  it('shows Cold text for score 25', () => {
    render(<ScoreBadge score={25} tier="cold" />);
    expect(screen.getByTestId('score-badge').textContent).toContain('Cold');
  });
});

describe('ContactModal', () => {
  it('renders when isOpen is true', () => {
    render(<ContactModal assessmentId="abc" isOpen={true} onClose={() => {}} onSuccess={() => {}} />);
    expect(screen.getByTestId('contact-modal')).toBeInTheDocument();
  });

  it('does not render when isOpen is false', () => {
    const { container } = render(<ContactModal assessmentId="abc" isOpen={false} onClose={() => {}} onSuccess={() => {}} />);
    expect(container.firstChild).toBeNull();
  });

  it('shows a validation error when both email and phone are empty on submit', () => {
    render(<ContactModal assessmentId="abc" isOpen={true} onClose={() => {}} onSuccess={() => {}} />);
    fireEvent.click(screen.getByRole('button', { name: /send my report/i }));
    expect(screen.getByTestId('contact-error')).toBeInTheDocument();
  });
});
