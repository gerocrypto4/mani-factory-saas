export default function Ticker({ items }) {
  return (
    <div className="cj-ticker-wrap">
      <div className="cj-ticker-track">
        {[...items, ...items].map((item, index) => (
          <span key={`${item}-${index}`} className="cj-ticker-item">
            {item} <span className="cj-ticker-dot">✦</span>
          </span>
        ))}
      </div>
    </div>
  );
}
