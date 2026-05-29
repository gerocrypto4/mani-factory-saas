export default function StatCard({ metric, delay = 0 }) {
  return (
    <div className="cj-stat-card" data-cj-reveal style={{ transitionDelay: `${delay}s` }}>
      <svg className="cj-stat-icon" viewBox="0 0 24 24">
        <path d={metric.icon} fill="currentColor" />
      </svg>
      <div className="cj-stat-number">
        {metric.n}
        <span className="cj-gold">{metric.suffix}</span>
      </div>
      <div className="cj-stat-label">{metric.label}</div>
    </div>
  );
}
