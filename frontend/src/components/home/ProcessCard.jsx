export default function ProcessCard({ step, delay = 0 }) {
  return (
    <div className="cj-process-card" data-cj-reveal style={{ transitionDelay: `${delay}s` }}>
      <span className="cj-process-num">{step.n}</span>
      <svg className="cj-process-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
        <path strokeLinecap="round" strokeLinejoin="round" d={step.icon} />
      </svg>
      <h3 className="cj-process-title">{step.title}</h3>
      <p className="cj-process-desc">{step.desc}</p>
    </div>
  );
}
