import { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import AppShell from '../components/AppShell.jsx';
import HeroBanner from '../components/HeroBanner.jsx';
import MetricGrid from '../components/MetricGrid.jsx';
import PanelRecommendationCard from '../components/PanelRecommendationCard.jsx';
import ProjectionChart from '../components/ProjectionChart.jsx';
import TipsList from '../components/TipsList.jsx';
import ContactModal from '../components/ContactModal.jsx';
import Button from '../components/ui/Button.jsx';

export default function ResultsPage() {
  const navigate = useNavigate();
  const [result, setResult] = useState(null);
  const [city, setCity] = useState('');
  const [state, setState] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [notice, setNotice] = useState(null);

  useEffect(() => {
    try {
      const raw = sessionStorage.getItem('solariq_result');
      if (!raw) {
        navigate('/', { replace: true });
        return;
      }
      const parsed = JSON.parse(raw);
      if (!parsed?.assessmentId) {
        navigate('/', { replace: true });
        return;
      }
      setResult(parsed);
      const meta = sessionStorage.getItem('solariq_input');
      if (meta) {
        try {
          const m = JSON.parse(meta);
          setCity(m.city || '');
          setState(m.state || '');
        } catch { /* ignore */ }
      }
    } catch {
      navigate('/', { replace: true });
    }
  }, [navigate]);

  if (!result) return null;

  return (
    <AppShell wide>
      <div className="results-page">
        <Link to="/" className="back-link">← Recalculate</Link>

        {notice && <div className="notice-banner" data-testid="notice">{notice}</div>}

        <HeroBanner
          monthlySavings={result.monthlySavings}
          systemSizeKw={result.recommendedSystemSizeKw || result.systemSizeKw}
          paybackYears={result.paybackYears}
          city={city || 'your city'}
          state={state}
        />

        <PanelRecommendationCard result={result} />

        <div className="ai-summary-box" data-testid="ai-summary">{result.aiSummary}</div>

        <MetricGrid result={result} />

        <h2 className="section-heading">25-Year Financial Projection</h2>
        <ProjectionChart projection={result.projection25yr} netCost={result.netCost} />

        <h2 className="section-heading">Personalized Insights</h2>
        <TipsList tips={result.aiTips} roofAreaSufficient={result.roofAreaSufficient} />

        <div className="cta-banner">
          <div className="cta-banner__title">Get your free detailed report</div>
          <div className="cta-banner__sub">
            Financing options, EMI from Rs 2,500/month, and installation timeline.
          </div>
          <Button variant="gold" glow onClick={() => setModalOpen(true)}>
            Send report to my email →
          </Button>
        </div>

        <ContactModal
          assessmentId={result.assessmentId}
          isOpen={modalOpen}
          onClose={() => setModalOpen(false)}
          onSuccess={() => setNotice('Report sent. Check your inbox.')}
        />
      </div>
    </AppShell>
  );
}
