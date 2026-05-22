import ScoreBadge, { tierFor } from './ScoreBadge.jsx';
import EmptyState from './ui/EmptyState.jsx';
import { formatRupees, formatKw, formatDate } from '../utils/formatters.js';

const STATUS_STYLES = {
  new: { bg: '#ecfdf5', color: '#059669' },
  contacted: { bg: '#eff6ff', color: '#2563eb' },
  site_visit: { bg: '#f5f3ff', color: '#7c3aed' },
  converted: { bg: '#d1fae5', color: '#047857' },
  lost: { bg: '#f1f5f9', color: '#64748b' },
};

export default function LeadTable({ leads, selectedId, onSelectLead }) {
  if (!leads || leads.length === 0) {
    return (
      <EmptyState
        testId="leads-empty"
        icon="📊"
        title="No leads yet"
        description="New customer assessments will appear here once they complete the calculator."
      />
    );
  }

  return (
    <div className="lead-table-scroll" data-testid="lead-table-wrap">
      <div className="lead-table-wrap">
      <table className="lead-table" data-testid="lead-table">
        <thead>
          <tr>
            <th>Name</th>
            <th>Location</th>
            <th>Bill</th>
            <th>Score</th>
            <th>System</th>
            <th>Savings</th>
            <th>Status</th>
            <th>Date</th>
          </tr>
        </thead>
        <tbody>
          {leads.map((lead) => {
            const st = STATUS_STYLES[lead.status] || STATUS_STYLES.new;
            return (
              <tr
                key={lead.id}
                data-testid="lead-row"
                data-selected={selectedId === lead.id ? 'true' : 'false'}
                onClick={() => onSelectLead(lead)}
              >
                <td style={{ fontWeight: 700 }}>{lead.name || 'Anonymous'}</td>
                <td>{lead.city}, {lead.state}</td>
                <td>{formatRupees(lead.monthlyBill)}</td>
                <td>
                  <ScoreBadge score={lead.leadScore || 0} tier={tierFor(lead.leadScore || 0)} />
                </td>
                <td>{formatKw(lead.systemSizeKw)}</td>
                <td style={{ fontWeight: 600, color: '#059669' }}>{formatRupees(lead.monthlySavings)}</td>
                <td>
                  <span className="status-pill" style={{ background: st.bg, color: st.color }}>
                    {lead.status?.replace('_', ' ')}
                  </span>
                </td>
                <td style={{ color: 'var(--text-muted)' }}>{formatDate(lead.createdAt)}</td>
              </tr>
            );
          })}
        </tbody>
      </table>
      </div>
    </div>
  );
}
