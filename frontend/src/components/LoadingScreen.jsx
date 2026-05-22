import AppShell from './AppShell.jsx';
import { Skeleton } from './ui/Skeleton.jsx';

export default function LoadingScreen({ city, state }) {
  return (
    <AppShell>
      <div className="loading-screen" data-testid="loading-screen">
        <div className="loading-screen__orb-wrap">
          <div className="loading-screen__ring" />
          <div className="loading-screen__orb" />
        </div>
        <h2>
          Analyzing solar potential for {city || 'your city'}
          {state ? `, ${state}` : ''}…
        </h2>
        <p>Calculating system size, subsidies, and 25-year returns.</p>
        <div className="loading-skeleton-lines">
          <Skeleton height={12} width="100%" />
          <Skeleton height={12} width="85%" />
          <Skeleton height={12} width="70%" />
        </div>
      </div>
    </AppShell>
  );
}
