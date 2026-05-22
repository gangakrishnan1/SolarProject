import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import LeadTable from '../components/LeadTable.jsx';
import ScoreBadge from '../components/ScoreBadge.jsx';

const sampleLead = {
  id: 'lead-1',
  name: 'Naveen',
  email: 'n@example.com',
  phone: '9876543210',
  city: 'Hyderabad',
  state: 'Telangana',
  monthlyBill: 4500,
  systemSizeKw: 5.0,
  monthlySavings: 3500,
  status: 'new',
  createdAt: '2026-05-21T10:00:00Z',
  leadScore: 78,
};

describe('LeadTable', () => {
  it('renders an empty state message when the leads array is empty', () => {
    render(<LeadTable leads={[]} selectedId={null} onSelectLead={() => {}} />);
    expect(screen.getByTestId('leads-empty')).toBeInTheDocument();
  });

  it('renders one row for each lead in the array', () => {
    const leads = [sampleLead, { ...sampleLead, id: 'lead-2', name: 'Other' }];
    render(<LeadTable leads={leads} selectedId={null} onSelectLead={() => {}} />);
    expect(screen.getAllByTestId('lead-row').length).toBe(2);
  });

  it('calls onSelectLead with the correct lead when a row is clicked', () => {
    const spy = vi.fn();
    render(<LeadTable leads={[sampleLead]} selectedId={null} onSelectLead={spy} />);
    fireEvent.click(screen.getAllByTestId('lead-row')[0]);
    expect(spy).toHaveBeenCalledWith(sampleLead);
  });
});

describe('ScoreBadge colors', () => {
  it('uses red tier for hot leads', () => {
    render(<ScoreBadge score={85} tier="hot" />);
    const badge = screen.getByTestId('score-badge');
    expect(badge.getAttribute('data-tier')).toBe('hot');
    expect(badge.style.color).toMatch(/rgb\(185, 28, 28\)|#b91c1c/i);
  });

  it('uses orange/amber tier for warm leads', () => {
    render(<ScoreBadge score={60} tier="warm" />);
    const badge = screen.getByTestId('score-badge');
    expect(badge.getAttribute('data-tier')).toBe('warm');
    expect(badge.style.color).toMatch(/rgb\(180, 83, 9\)|#b45309/i);
  });
});
