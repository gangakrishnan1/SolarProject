import { useState } from 'react';
import FloatingInput from './ui/FloatingInput.jsx';
import Button from './ui/Button.jsx';
import { formatRupees, formatKw } from '../utils/formatters.js';

const STATUSES = ['new', 'contacted', 'site_visit', 'converted', 'lost'];

export default function LeadDetail({ lead, onStatusUpdate, onClose }) {
  const [status, setStatus] = useState(lead.status || 'new');
  const [notes, setNotes] = useState(lead.notes || '');
  const [saving, setSaving] = useState(false);

  const handleUpdate = async () => {
    setSaving(true);
    try {
      await onStatusUpdate(lead.id, { status, notes });
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="card glass-card--hover lead-detail" data-testid="lead-detail">
      <div className="lead-detail__header">
        <div className="lead-detail__header-text">
          <h2 className="step-title" style={{ marginTop: 0 }}>{lead.name || 'Anonymous lead'}</h2>
          <p className="helper-text lead-detail__contact">
            {lead.email && <span>{lead.email}</span>}
            {lead.email && lead.phone && ' · '}
            {lead.phone && <span>{lead.phone}</span>}
            {!lead.email && !lead.phone && 'No contact captured yet'}
          </p>
        </div>
        <Button variant="secondary" size="sm" onClick={onClose}>Close</Button>
      </div>

      <div className="metric-grid lead-detail__metrics">
        {[
          ['Location', `${lead.city}, ${lead.state}`],
          ['Monthly bill', formatRupees(lead.monthlyBill)],
          ['System', formatKw(lead.systemSizeKw)],
          ['Install', formatRupees(lead.installCost)],
          ['Subsidy', formatRupees(lead.subsidyAmount)],
          ['Net cost', formatRupees(lead.netCost)],
          ['Savings/mo', formatRupees(lead.monthlySavings)],
          ['Payback', `${lead.paybackYears} yrs`],
        ].map(([label, value]) => (
          <div key={label} className="metric-card">
            <div className="metric-card__label">{label}</div>
            <div className="metric-card__value metric-card__value--compact">{value}</div>
          </div>
        ))}
      </div>

      {lead.aiSummary && (
        <div className="ai-summary-box lead-detail__summary">{lead.aiSummary}</div>
      )}

      {Array.isArray(lead.aiTips) && lead.aiTips.length > 0 && (
        <ul className="lead-detail__tips">
          {lead.aiTips.map((tip, i) => <li key={i}>{tip}</li>)}
        </ul>
      )}

      <div className="lead-detail__form">
        <label className="field-label" htmlFor="status-select">Status</label>
        <select
          id="status-select"
          data-testid="status-select"
          className="select-field"
          value={status}
          onChange={(e) => setStatus(e.target.value)}
          style={{ marginBottom: 16 }}
        >
          {STATUSES.map((s) => <option key={s} value={s}>{s.replace('_', ' ')}</option>)}
        </select>

        <FloatingInput
          id="lead-notes"
          as="textarea"
          label="Notes"
          value={notes}
          onChange={(e) => setNotes(e.target.value)}
          rows={3}
          className="float-field--textarea-wrap"
        />

        <Button variant="primary" glow loading={saving} onClick={handleUpdate}>
          Update status
        </Button>
      </div>
    </div>
  );
}
