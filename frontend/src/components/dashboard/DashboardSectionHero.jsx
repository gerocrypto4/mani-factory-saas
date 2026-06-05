export default function DashboardSectionHero({ kicker, title, copy, actions }) {
  return (
    <div className="dashboard-hero">
      <div className="dashboard-hero-copy">
        <p className="dashboard-kicker">{kicker}</p>
        <h2 className="dashboard-title">{title}</h2>
        <p className="dashboard-copy">{copy}</p>
      </div>

      <div className="dashboard-actions">{actions}</div>
    </div>
  );
}
