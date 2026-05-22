import { useState } from 'react';
import FloatingInput from './ui/FloatingInput.jsx';
import Button from './ui/Button.jsx';
import { captureContact } from '../services/api.js';

export default function ContactModal({ assessmentId, isOpen, onClose, onSuccess }) {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [success, setSuccess] = useState(false);

  if (!isOpen) return null;

  const submit = async (e) => {
    e.preventDefault();
    setError(null);
    if (!email.trim() && !phone.trim()) {
      setError('Please share at least an email or phone number');
      return;
    }
    setSubmitting(true);
    try {
      const payload = {};
      if (name.trim()) payload.name = name.trim();
      if (email.trim()) payload.email = email.trim();
      if (phone.trim()) payload.phone = phone.trim();
      await captureContact(assessmentId, payload);
      setSuccess(true);
      setTimeout(() => {
        onSuccess?.();
        onClose?.();
      }, 2000);
    } catch (e) {
      setError(e.response?.data?.message || e.message || 'Something went wrong');
      setSubmitting(false);
    }
  };

  return (
    <div className="modal-overlay" data-testid="contact-modal" onClick={onClose} role="dialog" aria-modal="true">
      <div className="modal-panel" onClick={(e) => e.stopPropagation()}>
        <button type="button" className="modal-close" onClick={onClose} aria-label="Close">×</button>

        {success ? (
          <div data-testid="success-message" className="modal-success">
            <div className="modal-success__icon" aria-hidden>✅</div>
            <h2 className="step-title">Thank you!</h2>
            <p className="helper-text">Your detailed report is on its way.</p>
          </div>
        ) : (
          <form onSubmit={submit}>
            <h2 className="step-title" style={{ marginTop: 0 }}>Get your free report</h2>
            <p className="step-subtitle">
              PDF breakdown, financing options, and a Lumenor expert callback.
            </p>

            <FloatingInput id="contact-name" label="Name" icon="👤" value={name} onChange={(e) => setName(e.target.value)} />
            <FloatingInput id="contact-email" label="Email" type="email" icon="✉" value={email} onChange={(e) => setEmail(e.target.value)} />
            <FloatingInput id="contact-phone" label="Phone" type="tel" icon="📱" value={phone} onChange={(e) => setPhone(e.target.value)} />

            {error && <div data-testid="contact-error" className="float-field__error" style={{ marginBottom: 16 }}>{error}</div>}

            <Button type="submit" variant="primary" fullWidth glow loading={submitting}>
              Send my report
            </Button>
          </form>
        )}
      </div>
    </div>
  );
}
