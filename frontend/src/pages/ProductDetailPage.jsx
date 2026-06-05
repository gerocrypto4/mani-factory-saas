import { useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { fetchProducts } from "../services/api";
import { ORDER_RULES, formatCurrency } from "../constants/orderRules";
import { findCatalogProductByKey, getCatalogCardState, getCatalogDisplayInfo, getFallbackProducts } from "../data/productCatalog";
import { useCart } from "../state/use-cart";

export default function ProductDetailPage() {
  const { productKey } = useParams();
  const { addItem } = useCart();
  const [catalog, setCatalog] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [quantity, setQuantity] = useState(ORDER_RULES.minKgPerFlavor);
  const [added, setAdded] = useState(false);

  useEffect(() => {
    async function load() {
      try {
        setLoading(true);
        setError("");
        const data = await fetchProducts(1);
        setCatalog(Array.isArray(data) && data.length > 0 ? data : getFallbackProducts());
      } catch {
        setCatalog(getFallbackProducts());
        setError("No pudimos cargar el producto desde el backend. Mostrando datos de respaldo.");
      } finally {
        setLoading(false);
      }
    }

    load();
  }, []);

  const product = useMemo(() => findCatalogProductByKey(catalog, productKey), [catalog, productKey]);
  const info = getCatalogDisplayInfo(product);
  const state = getCatalogCardState(product);
  const imageSrc = product?.imageUrl || info.image;
  const price = Number.isFinite(Number(product?.price)) ? Number(product.price) : 0;

  if (loading) {
    return (
      <section className="product-detail-page">
        <div className="product-detail-shell">
          <div className="dashboard-alert">Cargando producto...</div>
        </div>
      </section>
    );
  }

  if (!product) {
    return (
      <section className="product-detail-page">
        <div className="product-detail-shell">
          <div className="dashboard-panel product-detail-panel">
            <span className="dashboard-pill">Producto no encontrado</span>
            <h1 className="product-detail-title">No encontramos ese producto</h1>
            <p className="dashboard-muted">
              Volvé al catálogo para ver la línea completa o elegí otro producto.
            </p>
            <div className="product-detail-actions">
              <Link to="/pedido" className="btn-secondary">
                Volver al catálogo
              </Link>
            </div>
          </div>
        </div>
      </section>
    );
  }

  const handleAddToCart = () => {
    addItem(product, quantity);
    setAdded(true);
    setTimeout(() => setAdded(false), 2000);
  };

  const adjustQuantity = (delta) => {
    setQuantity((prev) => {
      const next = Number(prev) + delta;
      return next < ORDER_RULES.minKgPerFlavor ? ORDER_RULES.minKgPerFlavor : next;
    });
  };

  const handleQuantityInput = (event) => {
    const raw = event.target.value.replace(/[^\d]/g, "");
    setQuantity(raw === "" ? "" : Number(raw));
  };

  const normalizeQuantity = () => {
    const n = Number(quantity);
    if (!n || n < ORDER_RULES.minKgPerFlavor) {
      setQuantity(ORDER_RULES.minKgPerFlavor);
      return;
    }
    const rounded = Math.ceil(n / ORDER_RULES.minKgPerFlavor) * ORDER_RULES.minKgPerFlavor;
    setQuantity(rounded);
  };

  return (
    <section className="product-detail-page">
      <div className="product-detail-shell">
        {error && <div className="dashboard-alert dashboard-alert-error">{error}</div>}

        <div className="product-detail-topbar">
          <Link to="/pedido" className="product-detail-backlink">
            ← Volver al catálogo
          </Link>
          <Link to="/checkout" className="product-detail-cartlink">
            Ir a ventas ({ORDER_RULES.minKgPerFlavor} kg mínimo por sabor)
          </Link>
        </div>

        <article className="product-detail-panel dashboard-panel">
          <div className="product-detail-grid">
            <div className="product-detail-media" style={{ background: info.tone }}>
              <img
                className="product-detail-image"
                src={imageSrc}
                alt={product.name}
                onError={(event) => {
                  event.currentTarget.src = info.image;
                }}
              />
              <div className="product-detail-badges">
                <span className="product-detail-badge">{info.family}</span>
              </div>
            </div>

            <div className="product-detail-copy">
              <span className="dashboard-pill">Detalle de producto</span>
              <h1 className="product-detail-title">{info.displayName}</h1>
              <p className="product-detail-price">{formatCurrency(price)}</p>
              <p className="product-detail-description">{product.description || info.note}</p>

              <div className="product-detail-specs">
                <div>
                  <span>Familia</span>
                  <strong>{info.family}</strong>
                </div>
                <div>
                  <span>Entrega</span>
                  <strong>{info.deliveryDays} a {info.deliveryDays + 1} días</strong>
                </div>
                <div>
                  <span>Disponibilidad</span>
                  <strong>{state.stockHint}</strong>
                </div>
              </div>

              <div className="product-detail-quantity">
                <span className="product-detail-quantity-label">Cantidad (kg)</span>

                <div className="product-detail-quantity-chips">
                  {ORDER_RULES.quickAddKgOptions.map((opt) => (
                    <button
                      key={opt}
                      type="button"
                      className={`product-detail-chip ${Number(quantity) === opt ? "product-detail-chip-active" : ""}`}
                      onClick={() => setQuantity(opt)}
                    >
                      {opt} kg
                    </button>
                  ))}
                </div>

                <div className="product-detail-quantity-stepper">
                  <button type="button" className="product-detail-stepper-btn" onClick={() => adjustQuantity(-ORDER_RULES.minKgPerFlavor)} aria-label="Restar 10 kg">−</button>
                  <input
                    type="text"
                    inputMode="numeric"
                    className="product-detail-quantity-input"
                    value={quantity}
                    onChange={handleQuantityInput}
                    onBlur={normalizeQuantity}
                    aria-label="Cantidad en kilogramos"
                  />
                  <span className="product-detail-quantity-unit">kg</span>
                  <button type="button" className="product-detail-stepper-btn" onClick={() => adjustQuantity(ORDER_RULES.minKgPerFlavor)} aria-label="Sumar 10 kg">+</button>
                </div>

                <p className="product-detail-quantity-hint">Mínimo {ORDER_RULES.minKgPerFlavor} kg por sabor · se vende en múltiplos de {ORDER_RULES.minKgPerFlavor} kg</p>
              </div>

              <div className="product-detail-actions">
                <button type="button" className="btn-primary product-detail-add-btn" onClick={handleAddToCart} disabled={!state.canAdd || !quantity}>
                  {state.isOutOfStock ? "No disponible" : added ? "✓ Agregado al carrito" : `Agregar ${quantity || 0} kg al carrito`}
                </button>
                <Link to="/checkout" className="btn-secondary">Ir a ventas</Link>
              </div>

            </div>
          </div>
        </article>
      </div>
    </section>
  );
}
