import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import ProgressBar from '../components/ProgressBar.jsx';
import StepLocation from '../components/StepLocation.jsx';
import StepBill from '../components/StepBill.jsx';
import StepProperty from '../components/StepProperty.jsx';
import StepRoof from '../components/StepRoof.jsx';

vi.mock('../services/locationService.js', () => ({
  searchLocations: vi.fn().mockResolvedValue([]),
  reverseGeocode: vi.fn(),
  getCurrentPosition: vi.fn(),
}));

const locationProps = {
  state: 'Telangana',
  city: 'Hyderabad',
  areaType: 'urban',
  locationLabel: 'Hyderabad, Telangana',
  onStateChange: () => {},
  onCityChange: () => {},
  onAreaTypeChange: () => {},
  onLocationLabelChange: () => {},
};

describe('ProgressBar', () => {
  it('renders the correct number of step segments', () => {
    const { container } = render(<ProgressBar currentStep={2} totalSteps={4} />);
    const segments = container.querySelectorAll('[data-step]');
    expect(segments.length).toBe(4);
  });

  it('marks the current step as active', () => {
    const { container } = render(<ProgressBar currentStep={2} totalSteps={4} />);
    const active = container.querySelector('[data-active="true"]');
    expect(active).not.toBeNull();
    expect(active.getAttribute('data-step')).toBe('2');
  });
});

describe('StepLocation', () => {
  it('renders state dropdown with 10 states', () => {
    render(<StepLocation {...locationProps} />);
    const dropdown = screen.getByTestId('state-select');
    expect(dropdown.querySelectorAll('option').length).toBe(11);
  });

  it('calls onStateChange when the dropdown value changes', () => {
    const spy = vi.fn();
    render(<StepLocation {...locationProps} onStateChange={spy} />);
    fireEvent.change(screen.getByTestId('state-select'), { target: { value: 'Karnataka' } });
    expect(spy).toHaveBeenCalledWith('Karnataka');
  });

  it('renders area type options including semi_urban', () => {
    render(<StepLocation {...locationProps} />);
    expect(screen.getByTestId('area-semi_urban')).toBeInTheDocument();
    expect(screen.getByTestId('use-my-location')).toBeInTheDocument();
  });

  it('calls onAreaTypeChange when area card clicked', () => {
    const spy = vi.fn();
    render(<StepLocation {...locationProps} onAreaTypeChange={spy} />);
    fireEvent.click(screen.getByTestId('area-rural'));
    expect(spy).toHaveBeenCalledWith('rural');
  });
});

describe('StepBill', () => {
  it('shows the formatted rupee amount', () => {
    render(<StepBill value={4500} onChange={() => {}} />);
    expect(screen.getByTestId('bill-display').textContent).toMatch(/4,500/);
  });

  it('calls onChange when the slider moves', () => {
    const spy = vi.fn();
    render(<StepBill value={3000} onChange={spy} />);
    const slider = screen.getByRole('slider');
    fireEvent.change(slider, { target: { value: '5000' } });
    expect(spy).toHaveBeenCalledWith(5000);
  });
});

describe('StepProperty', () => {
  it('renders 3 property type options', () => {
    render(<StepProperty propertyType="residential" ownership="own" onPropertyTypeChange={() => {}} onOwnershipChange={() => {}} />);
    expect(screen.getByTestId('property-residential')).toBeInTheDocument();
    expect(screen.getByTestId('property-commercial')).toBeInTheDocument();
    expect(screen.getByTestId('property-industrial')).toBeInTheDocument();
  });

  it('visually marks the selected property type', () => {
    render(<StepProperty propertyType="commercial" ownership="own" onPropertyTypeChange={() => {}} onOwnershipChange={() => {}} />);
    expect(screen.getByTestId('property-commercial').getAttribute('data-active')).toBe('true');
    expect(screen.getByTestId('property-residential').getAttribute('data-active')).toBe('false');
  });

  it('calls the handler when a different property type is clicked', () => {
    const spy = vi.fn();
    render(<StepProperty propertyType="residential" ownership="own" onPropertyTypeChange={spy} onOwnershipChange={() => {}} />);
    fireEvent.click(screen.getByTestId('property-industrial'));
    expect(spy).toHaveBeenCalledWith('industrial');
  });
});

describe('StepRoof', () => {
  it('renders the two roof type options', () => {
    render(<StepRoof roofArea="" roofType="flat_rcc" onRoofAreaChange={() => {}} onRoofTypeChange={() => {}} />);
    expect(screen.getByTestId('roof-flat_rcc')).toBeInTheDocument();
    expect(screen.getByTestId('roof-sloped')).toBeInTheDocument();
  });
});
