import { useState, useEffect, useRef } from 'react';
import FloatingInput from './ui/FloatingInput.jsx';
import { searchLocations, reverseGeocode, getCurrentPosition } from '../services/locationService.js';

const STATES = [
  'Telangana', 'Andhra Pradesh', 'Karnataka', 'Tamil Nadu', 'Kerala',
  'Maharashtra', 'Gujarat', 'Rajasthan', 'Delhi', 'Uttar Pradesh',
];

const AREA_TYPES = [
  { value: 'rural', label: 'Rural', icon: '🌾', cap: 'Up to 5 kW' },
  { value: 'semi_urban', label: 'Semi-Urban', icon: '🏘️', cap: 'Up to 8 kW' },
  { value: 'urban', label: 'Urban', icon: '🏙️', cap: 'Up to 12 kW' },
  { value: 'metro', label: 'Metro', icon: '🌆', cap: 'Up to 20 kW' },
];

export default function StepLocation({
  state,
  city,
  areaType,
  locationLabel,
  onStateChange,
  onCityChange,
  onAreaTypeChange,
  onLocationLabelChange,
  error,
}) {
  const [query, setQuery] = useState(locationLabel || '');
  const [suggestions, setSuggestions] = useState([]);
  const [searching, setSearching] = useState(false);
  const [locating, setLocating] = useState(false);
  const [locError, setLocError] = useState(null);
  const [locHint, setLocHint] = useState(null);
  const [locationSource, setLocationSource] = useState(null);
  const debounceRef = useRef(null);

  useEffect(() => {
    if (!query || query.length < 2 || (locationLabel && query === locationLabel)) {
      setSuggestions([]);
      return;
    }
    clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(async () => {
      setSearching(true);
      try {
        const results = await searchLocations(query);
        setSuggestions(results);
      } catch {
        setSuggestions([]);
      } finally {
        setSearching(false);
      }
    }, 350);
    return () => clearTimeout(debounceRef.current);
  }, [query, locationLabel]);

  const applyLocation = (loc, { accuracyMeters, lowAccuracy, source = 'search' } = {}) => {
    onCityChange(loc.city || '');
    if (loc.state) onStateChange(loc.state);
    if (loc.suggestedAreaType) onAreaTypeChange(loc.suggestedAreaType);
    const label = (loc.displayName && loc.displayName.trim())
      || (loc.city && loc.state ? `${loc.city}, ${loc.state}` : (loc.city || ''));
    onLocationLabelChange(label);
    setQuery(label);
    setSuggestions([]);
    setLocError(null);
    setLocationSource(source);

    if (source === 'gps' && accuracyMeters != null) {
      const km = accuracyMeters >= 1000 ? `${(accuracyMeters / 1000).toFixed(0)} km` : `${accuracyMeters} m`;
      if (lowAccuracy || accuracyMeters > 15000) {
        setLocHint(
          `Approximate area only (~${km} accuracy on this device). `
          + 'Please confirm city and state below, or search for your exact locality.'
        );
      } else if (accuracyMeters <= 500) {
        setLocHint(`GPS accurate to ~${accuracyMeters} m. Adjust state or area type if needed.`);
      } else {
        setLocHint(`GPS approximate (~${km}). Verify city and state below.`);
      }
    } else {
      setLocHint(null);
    }
  };

  const handleUseMyLocation = async () => {
    setLocating(true);
    setLocError(null);
    setLocHint(null);
    try {
      const { lat, lon, accuracyMeters, lowAccuracy } = await getCurrentPosition();
      const loc = await reverseGeocode(lat, lon);
      applyLocation(loc, { accuracyMeters, lowAccuracy, source: 'gps' });
    } catch (e) {
      const msg = e.response?.data?.message || e.message || 'Could not detect location';
      setLocError(msg);
      setLocHint(null);
    } finally {
      setLocating(false);
    }
  };

  return (
    <div data-testid="step-location">
      <h2 className="step-title">Where is your property?</h2>
      <p className="step-subtitle">
        Use GPS or search — we pull city & state from OpenStreetMap and tailor panel sizes to your area type.
      </p>

      <button
        type="button"
        className="btn btn-location"
        onClick={handleUseMyLocation}
        disabled={locating}
        data-testid="use-my-location"
      >
        {locating ? '⏳ Detecting location…' : '📍 Use my current location'}
      </button>
      {locError && <div className="error-text">{locError}</div>}
      {locHint && !locError && <p className="helper-text location-hint">{locHint}</p>}

      <div className="divider-row">or search</div>

      <div className="location-search-wrap">
        <FloatingInput
          id="location-search"
          label="Search city or locality"
          icon="🔍"
          value={query}
          onChange={(e) => {
            setQuery(e.target.value);
            onLocationLabelChange(e.target.value);
          }}
          autoComplete="off"
          data-testid="location-search"
        />
        {searching && <p className="helper-text">Searching…</p>}
        {suggestions.length > 0 && (
          <ul className="location-dropdown" data-testid="location-suggestions">
            {suggestions.map((loc, i) => (
              <li key={`${loc.latitude}-${loc.longitude}-${i}`}>
                <button type="button" onClick={() => applyLocation(loc)}>
                  {loc.city && loc.state ? `${loc.city}, ${loc.state}` : loc.displayName}
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>

      {city && locationSource && (
        <div className="location-selected" data-testid="location-selected">
          <div className="location-selected__city">
            {query || (city && state ? `${city}, ${state}` : city)}
          </div>
          <div className="location-selected__meta">
            {city}{state ? ` · ${state}` : ''}
            {locationSource === 'gps'
              ? ' · from your GPS'
              : ' · from search'}
          </div>
        </div>
      )}

      <label className="field-label" style={{ marginTop: 8 }}>
        State <span className="field-label__hint">(adjust if needed)</span>
      </label>
      <select
        className="select-field"
        value={state}
        onChange={(e) => onStateChange(e.target.value)}
        data-testid="state-select"
      >
        <option value="" disabled>Select your state</option>
        {STATES.map((s) => (
          <option key={s} value={s}>{s}</option>
        ))}
      </select>

      <div style={{ marginTop: 24 }}>
        <label className="field-label">Area type</label>
        <p className="helper-text" style={{ marginTop: 4 }}>
          We recommend panel capacity based on locality — semi-urban areas typically use systems under 8 kW.
        </p>
        <div className="area-type-grid">
          {AREA_TYPES.map((opt) => (
            <button
              key={opt.value}
              type="button"
              className="area-type-card"
              data-active={areaType === opt.value ? 'true' : 'false'}
              data-testid={`area-${opt.value}`}
              onClick={() => onAreaTypeChange(opt.value)}
            >
              <span className="area-type-card__icon">{opt.icon}</span>
              <span className="area-type-card__label">{opt.label}</span>
              <span className="area-type-card__cap">{opt.cap}</span>
            </button>
          ))}
        </div>
      </div>

      {error && <div className="error-text">{error}</div>}
    </div>
  );
}
