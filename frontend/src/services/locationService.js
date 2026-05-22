import axios from 'axios';

const client = axios.create({
  baseURL: '/api/v1/location',
  timeout: 20000,
});

export async function searchLocations(query) {
  const { data } = await client.get('/search', { params: { q: query } });
  return data;
}

export async function reverseGeocode(lat, lon) {
  const { data } = await client.get('/reverse', { params: { lat, lon } });
  return data;
}

function geolocationErrorMessage(err) {
  if (!err) return 'Could not get your location';
  switch (err.code) {
    case 1:
      return 'Location permission denied. Allow location access in your browser and Windows settings, or search for your city.';
    case 2:
      return 'Location unavailable. Turn on Location in Windows Settings → Privacy, allow this site in the browser, then try again.';
    case 3:
      return 'Location request timed out. Try again or search your city manually.';
    default:
      return err.message || 'Could not get your location';
  }
}

/**
 * Best-effort GPS fix. Desktop browsers often lack GPS; we prefer a cached
 * low-accuracy fix first, optionally refine with high accuracy, and only fail
 * after the wait window if no coordinates were obtained.
 */
export function getCurrentPosition() {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(new Error('Geolocation is not supported in this browser'));
      return;
    }

    const REJECT_ACCURACY_M = 250000;
    const LOW_ACCURACY_M = 15000;
    const GOOD_ACCURACY_M = 500;
    const MAX_WAIT_MS = 20000;

    let best = null;
    let watchId = null;
    let settled = false;
    let lastError = null;

    const cleanup = () => {
      if (watchId != null) {
        navigator.geolocation.clearWatch(watchId);
        watchId = null;
      }
    };

    const finish = (pos) => {
      if (settled) return;
      settled = true;
      cleanup();
      clearTimeout(timer);

      const accuracy = pos.coords.accuracy ?? Infinity;
      if (accuracy > REJECT_ACCURACY_M) {
        reject(new Error(
          `Location accuracy is extremely low (~${Math.round(accuracy / 1000)} km). `
          + 'Please search for your city instead.'
        ));
        return;
      }

      resolve({
        lat: pos.coords.latitude,
        lon: pos.coords.longitude,
        accuracyMeters: Math.round(accuracy),
        lowAccuracy: accuracy > LOW_ACCURACY_M,
      });
    };

    const onPosition = (pos) => {
      if (!best || pos.coords.accuracy < best.coords.accuracy) {
        best = pos;
      }
      if (pos.coords.accuracy <= GOOD_ACCURACY_M) {
        finish(pos);
      }
    };

    const onError = (err) => {
      lastError = err;
    };

    const timer = setTimeout(() => {
      if (settled) return;
      if (best) {
        finish(best);
        return;
      }
      settled = true;
      cleanup();
      reject(new Error(geolocationErrorMessage(lastError)));
    }, MAX_WAIT_MS);

    const coarseOptions = {
      enableHighAccuracy: false,
      timeout: MAX_WAIT_MS,
      maximumAge: 300000,
    };
    const fineOptions = {
      enableHighAccuracy: true,
      timeout: 12000,
      maximumAge: 0,
    };

    watchId = navigator.geolocation.watchPosition(onPosition, onError, coarseOptions);
    navigator.geolocation.getCurrentPosition(onPosition, onError, coarseOptions);

    setTimeout(() => {
      if (settled) return;
      if (!best || best.coords.accuracy > GOOD_ACCURACY_M) {
        navigator.geolocation.getCurrentPosition(onPosition, onError, fineOptions);
      }
    }, 2500);
  });
}
