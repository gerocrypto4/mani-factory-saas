import { Link } from "react-router-dom";
import { buildProductDetailPath } from "../../data/productCatalog";

export default function ProductCard({ product, delay = 0 }) {
  return (
    <div className="cj-prod-card" data-cj-reveal style={{ transitionDelay: `${delay}s` }}>
      <div className="cj-prod-img" style={{ background: product.bg }}>
        <img
          src={product.img}
          alt={product.name}
          onError={(event) => {
            event.currentTarget.style.display = "none";
          }}
        />
        <span className="cj-prod-badge">{product.badge}</span>
        <div className="cj-prod-overlay">
          <span className="cj-prod-brand">
            <span className="cj-brand-wordmark cj-brand-wordmark-brand">
              CRIS<span className="cj-brand-wordmark-separator">-</span>JOR
            </span>
          </span>
          <span className="cj-prod-name-overlay">{product.name}</span>
        </div>
      </div>
      <div className="cj-prod-body">
        <span className="cj-prod-line">{product.line}</span>
        <h3 className="cj-prod-title">{product.name}</h3>
        <p className="cj-prod-desc">{product.desc}</p>
        <div className="cj-prod-footer">
          <Link to={buildProductDetailPath(product)} className="cj-prod-link">
            Ver detalle →
          </Link>
        </div>
      </div>
    </div>
  );
}
