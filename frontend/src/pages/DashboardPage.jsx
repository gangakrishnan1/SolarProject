import { useEffect, useMemo, useState } from 'react';
import AppShell from '../components/AppShell.jsx';
import PageTransition from '../components/ui/PageTransition.jsx';
import LeadTable from '../components/LeadTable.jsx';
import LeadDetail from '../components/LeadDetail.jsx';
import DashboardCharts from '../components/DashboardCharts.jsx';
import FloatingInput from '../components/ui/FloatingInput.jsx';
import Button from '../components/ui/Button.jsx';
import { SkeletonCards, SkeletonTable } from '../components/ui/Skeleton.jsx';
import { useToast } from '../context/ToastContext.jsx';
import { getLeads, updateLeadStatus } from '../services/api.js';

const STATUSES = ['all', 'new', 'contacted', 'site_visit', 'converted', 'lost'];

function readAuth() {
  try {
    const raw = sessionStorage.getItem('solariq_auth');
    if (!raw) return null;
    return JSON.parse(raw);
  } catch { return null; }
}

function LoginForm({ onAuth }) {
  const [username, setUsername] = useState('admin');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const { showToast } = useToast();

  const submit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await getLeads({ auth: { username, password } });
      sessionStorage.setItem('solariq_auth', JSON.stringify({ username, password }));
      onAuth({ username, password });
      showToast('Welcome back — dashboard loaded', 'success');
    } catch {
      setError('Invalid credentials');
      showToast('Invalid username or password', 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <AppShell wide>
      <PageTransition className="login-card">
        <div className="page-intro" style={{ marginBottom: 24 }}>
          <h1>Sales dashboard</h1>
          <p>Secure access for Lumenor lead management and pipeline analytics.</p>
        </div>
        <form onSubmit={submit} className="card glass-card--hover">
          <h2 className="step-title" style={{ marginTop: 0 }}>Team sign in</h2>
          <FloatingInput
            id="username"
            label="Username"
            icon="👤"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            error={error ? ' ' : undefined}
          />
          <FloatingInput
            id="password"
            label="Password"
            type="password"
            icon="🔒"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            error={error}
          />
          <Button type="submit" variant="primary" fullWidth glow loading={loading}>
            Sign in
          </Button>
        </form>
      </PageTransition>
    </AppShell>
  );
}

export default function DashboardPage() {
  const [auth, setAuth] = useState(readAuth());
  const [leads, setLeads] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selected, setSelected] = useState(null);
  const [statusFilter, setStatusFilter] = useState('all');
  const [minScore, setMinScore] = useState(0);
  const [error, setError] = useState(null);
  const { showToast } = useToast();

  const refresh = async (a = auth) => {
    if (!a) return;
    setLoading(true);
    setError(null);
    try {
      const data = await getLeads({ auth: a });
      setLeads(data);
    } catch (e) {
      if (e.response?.status === 401 || e.response?.status === 403) {
        sessionStorage.removeItem('solariq_auth');
        setAuth(null);
        showToast('Session expired — please sign in again', 'error');
      } else {
        setError(e.message || 'Failed to load leads');
        showToast('Could not load leads', 'error');
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (auth) refresh(auth);
  }, [auth]);

  const filtered = useMemo(() => leads.filter((l) => {
    if (statusFilter !== 'all' && l.status !== statusFilter) return false;
    if ((l.leadScore || 0) < minScore) return false;
    return true;
  }), [leads, statusFilter, minScore]);

  const stats = useMemo(() => {
    const total = leads.length;
    const hot = leads.filter((l) => (l.leadScore || 0) >= 75).length;
    const warm = leads.filter((l) => (l.leadScore || 0) >= 50 && (l.leadScore || 0) < 75).length;
    const avg = total === 0 ? 0 : Math.round(leads.reduce((s, l) => s + (l.leadScore || 0), 0) / total);
    const maxScore = 100;
    return { total, hot, warm, avg, hotPct: total ? (hot / total) * 100 : 0, avgPct: (avg / maxScore) * 100 };
  }, [leads]);

  if (!auth) return <LoginForm onAuth={setAuth} />;

  const handleStatusUpdate = async (leadId, payload) => {
    try {
      await updateLeadStatus(leadId, payload, auth);
      setSelected(null);
      await refresh();
      showToast('Lead status updated', 'success');
    } catch (e) {
      showToast(e.response?.data?.message || e.message || 'Update failed', 'error');
    }
  };

  return (
    <AppShell wide>
      <div className="dashboard-page">
        <div className="dashboard-hero">
          <div className="dashboard-hero__row">
            <div className="dashboard-hero__text">
              <h1>Lead command center</h1>
              <p>Pipeline health, score analytics, and conversion tracking.</p>
            </div>
            <Button variant="ghost" size="sm" onClick={() => { sessionStorage.removeItem('solariq_auth'); setAuth(null); }}>
              Sign out
            </Button>
          </div>
        </div>

        {loading && leads.length === 0 ? (
          <SkeletonCards count={4} />
        ) : (
          <div className="stat-grid">
            <StatCard label="Total leads" value={stats.total} color="#34d399" barPct={100} delay={0} />
            <StatCard label="Hot (≥75)" value={stats.hot} color="#f87171" barPct={stats.hotPct} delay={0.05} />
            <StatCard label="Warm (50–74)" value={stats.warm} color="#fbbf24" barPct={stats.total ? (stats.warm / stats.total) * 100 : 0} delay={0.1} />
            <StatCard label="Avg score" value={stats.avg} color="#38bdf8" barPct={stats.avgPct} delay={0.15} />
          </div>
        )}

        {!loading && leads.length > 0 && <DashboardCharts leads={leads} />}

        <div className="card filters-bar glass-card--hover">
          <div className="filters-bar__group">
            <span className="field-label">Status</span>
            <div className="filter-chip-group">
              {STATUSES.map((s) => (
                <button
                  key={s}
                  type="button"
                  className={`filter-chip ${statusFilter === s ? 'filter-chip--active' : ''}`}
                  onClick={() => setStatusFilter(s)}
                >
                  {s.replace('_', ' ')}
                </button>
              ))}
            </div>
          </div>
          <label className="filters-bar__group filters-bar__group--range">
            <span className="field-label">Min score: {minScore}</span>
            <input type="range" className="range-input" min="0" max="100" value={minScore} onChange={(e) => setMinScore(Number(e.target.value))} />
          </label>
          <div className="filters-bar__actions">
            <Button variant="secondary" size="sm" onClick={() => refresh()} loading={loading}>
              Refresh
            </Button>
          </div>
        </div>

        {error && <p className="error-text">{error}</p>}

        {loading ? (
          <SkeletonTable rows={6} cols={8} />
        ) : (
          <LeadTable leads={filtered} selectedId={selected?.id} onSelectLead={setSelected} />
        )}

        {selected && (
          <LeadDetail lead={selected} onStatusUpdate={handleStatusUpdate} onClose={() => setSelected(null)} />
        )}
      </div>
    </AppShell>
  );
}

function StatCard({ label, value, color, barPct, delay }) {
  return (
    <div className="stat-card" style={{ animationDelay: `${delay}s` }}>
      <div className="stat-card__label">{label}</div>
      <div className="stat-card__value" style={{ color }}>{value}</div>
      <div className="stat-card__bar">
        <div className="stat-card__bar-fill" style={{ width: `${Math.min(100, barPct)}%`, background: color }} />
      </div>
    </div>
  );
}
