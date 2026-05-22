import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AppShell from '../components/AppShell.jsx';
import ProgressBar from '../components/ProgressBar.jsx';
import StepLocation from '../components/StepLocation.jsx';
import StepBill from '../components/StepBill.jsx';
import StepProperty from '../components/StepProperty.jsx';
import StepRoof from '../components/StepRoof.jsx';
import LoadingScreen from '../components/LoadingScreen.jsx';
import Button from '../components/ui/Button.jsx';
import { useToast } from '../context/ToastContext.jsx';
import { createAssessment } from '../services/api.js';

const TOTAL_STEPS = 4;

const DEFAULT_DATA = {
  state: '',
  city: '',
  areaType: 'urban',
  locationLabel: '',
  propertyType: 'residential',
  monthlyBill: 3000,
  roofType: 'flat_rcc',
  ownership: 'own',
  roofAreaSqft: '',
};

export default function CalculatorPage() {
  const [step, setStep] = useState(1);
  const [data, setData] = useState(DEFAULT_DATA);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [stepError, setStepError] = useState(null);
  const navigate = useNavigate();
  const { showToast } = useToast();

  const update = (key, value) => setData((prev) => ({ ...prev, [key]: value }));

  const validateStep = () => {
    if (step === 1) {
      if (!data.city?.trim()) {
        setStepError('Please detect or search for your location');
        return false;
      }
      if (!data.areaType) {
        setStepError('Please select your area type');
        return false;
      }
    }
    if (step === 2) {
      if (!data.monthlyBill || Number(data.monthlyBill) < 500) {
        setStepError('Bill must be at least Rs 500');
        return false;
      }
    }
    setStepError(null);
    return true;
  };

  const next = async () => {
    if (!validateStep()) return;
    if (step < TOTAL_STEPS) {
      setStep(step + 1);
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const payload = {
        city: data.city.trim(),
        state: data.state,
        areaType: data.areaType,
        propertyType: data.propertyType,
        monthlyBill: Number(data.monthlyBill),
        roofType: data.roofType,
        ownership: data.ownership,
      };
      if (data.roofAreaSqft && Number(data.roofAreaSqft) > 0) {
        payload.roofAreaSqft = Number(data.roofAreaSqft);
      }
      const result = await createAssessment(payload);
      sessionStorage.setItem('solariq_result', JSON.stringify(result));
      sessionStorage.setItem('solariq_input', JSON.stringify(payload));
      showToast('Assessment complete — view your savings', 'success');
      navigate('/results');
    } catch (e) {
      const msg = e.response?.data?.message || e.message || 'Something went wrong';
      setError(msg);
      showToast(msg, 'error');
      setLoading(false);
    }
  };

  const back = () => {
    setStepError(null);
    if (step > 1) setStep(step - 1);
  };

  if (loading) {
    return <LoadingScreen city={data.city} state={data.state} />;
  }

  return (
    <AppShell>
      <div className="container">
        <div className="page-intro">
          <h1>Discover your solar savings in 60 seconds</h1>
          <p>Location-aware sizing, MNRE subsidies, and personalized panel recommendations.</p>
        </div>

        <ProgressBar currentStep={step} totalSteps={TOTAL_STEPS} />

        <div className="card glass-card--hover">
          {step === 1 && (
            <StepLocation
              state={data.state}
              city={data.city}
              areaType={data.areaType}
              locationLabel={data.locationLabel}
              onStateChange={(v) => update('state', v)}
              onCityChange={(v) => update('city', v)}
              onAreaTypeChange={(v) => update('areaType', v)}
              onLocationLabelChange={(v) => update('locationLabel', v)}
              error={stepError}
            />
          )}
          {step === 2 && (
            <StepBill value={Number(data.monthlyBill)} onChange={(v) => update('monthlyBill', v)} error={stepError} />
          )}
          {step === 3 && (
            <StepProperty
              propertyType={data.propertyType}
              ownership={data.ownership}
              onPropertyTypeChange={(v) => update('propertyType', v)}
              onOwnershipChange={(v) => update('ownership', v)}
            />
          )}
          {step === 4 && (
            <StepRoof
              roofArea={data.roofAreaSqft}
              roofType={data.roofType}
              onRoofAreaChange={(v) => update('roofAreaSqft', v)}
              onRoofTypeChange={(v) => update('roofType', v)}
            />
          )}
        </div>

        {error && <p className="error-text">{error}</p>}

        <div className="nav-row">
          {step > 1 ? (
            <Button variant="secondary" onClick={back}>← Back</Button>
          ) : <span />}
          <Button variant="primary" glow onClick={next}>
            {step < TOTAL_STEPS ? 'Continue →' : 'Calculate My Savings →'}
          </Button>
        </div>
      </div>
    </AppShell>
  );
}
