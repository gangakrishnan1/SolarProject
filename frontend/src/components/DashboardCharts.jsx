import {
  BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend,
} from 'recharts';

const STATUS_COLORS = {
  new: '#10b981',
  contacted: '#3b82f6',
  site_visit: '#a855f7',
  converted: '#059669',
  lost: '#94a3b8',
};

const SCORE_COLORS = ['#ef4444', '#f59e0b', '#10b981', '#3b82f6', '#8b5cf6'];

export default function DashboardCharts({ leads }) {
  const statusData = Object.entries(
    leads.reduce((acc, l) => {
      const s = l.status || 'new';
      acc[s] = (acc[s] || 0) + 1;
      return acc;
    }, {})
  ).map(([name, value]) => ({
    name: name.replace('_', ' '),
    value,
    fill: STATUS_COLORS[name] || '#64748b',
  }));

  const scoreBuckets = [
    { range: '0–24', min: 0, max: 24 },
    { range: '25–49', min: 25, max: 49 },
    { range: '50–74', min: 50, max: 74 },
    { range: '75–89', min: 75, max: 89 },
    { range: '90+', min: 90, max: 100 },
  ].map((b, i) => ({
    range: b.range,
    count: leads.filter((l) => {
      const s = l.leadScore || 0;
      return s >= b.min && s <= b.max;
    }).length,
    fill: SCORE_COLORS[i],
  }));

  if (leads.length === 0) return null;

  return (
    <div className="dashboard-charts" data-testid="dashboard-charts">
      <div className="chart-card glass-card glass-card--hover">
        <h3 className="chart-card__title">Leads by status</h3>
        <div className="chart-card__body" style={{ minHeight: 220 }}>
          <ResponsiveContainer width="100%" height="100%" minHeight={200}>
            <PieChart>
              <Pie
                data={statusData}
                dataKey="value"
                nameKey="name"
                cx="50%"
                cy="50%"
                innerRadius={50}
                outerRadius={80}
                paddingAngle={3}
              >
                {statusData.map((entry, i) => (
                  <Cell key={i} fill={entry.fill} />
                ))}
              </Pie>
              <Tooltip />
              <Legend wrapperStyle={{ fontSize: 12 }} />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>
      <div className="chart-card glass-card glass-card--hover">
        <h3 className="chart-card__title">Score distribution</h3>
        <div className="chart-card__body" style={{ minHeight: 220 }}>
          <ResponsiveContainer width="100%" height="100%" minHeight={200}>
            <BarChart data={scoreBuckets} margin={{ top: 8, right: 8, left: 0, bottom: 0 }}>
              <XAxis dataKey="range" tick={{ fontSize: 11 }} />
              <YAxis allowDecimals={false} tick={{ fontSize: 11 }} />
              <Tooltip />
              <Bar dataKey="count" radius={[8, 8, 0, 0]}>
                {scoreBuckets.map((entry, i) => (
                  <Cell key={i} fill={entry.fill} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
}
