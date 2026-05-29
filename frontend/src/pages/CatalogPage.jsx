import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { fetchProducts } from "../services/api";
import { useCart } from "../state/use-cart";
import { resolveProductVisual } from "../data/productVisuals";
import { ORDER_RULES, formatCurrency, formatKg } from "../constants/orderRules";

export default function CatalogPage() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [query, setQuery] = useState("");
  const [sort, setSort] = useState("featured");
  const { addItem, items, removeItem } = useCart();

  const fallbackProducts = useMemo(
    () => [
      { id: "fallback-blancheado-salado", name: "Blancheado Salado", price: 3000, description: "Bolsa de 1kg - blancheado salado." },
      { id: "fallback-blancheado-sin-sal", name: "Blancheado Sin Sal", price: 3000, description: "Bolsa de 1kg - blancheado sin sal." },
      { id: "fallback-blancheado-con-piel", name: "Blancheado Con Piel", price: 3000, description: "Bolsa de 1kg - blancheado con piel." },
      { id: "fallback-crocante-salado", name: "Crocante Salado", price: 3000, description: "Bolsa de 1kg - crocante salado." },
      { id: "fallback-jamon", name: "Crocante Sabor Jamon", price: 3000, description: "Bolsa de 1kg - sabor jamon." },
      { id: "fallback-queso", name: "Crocante Sabor Queso", price: 3000, description: "Bolsa de 1kg - sabor queso." },
      { id: "fallback-salame", name: "Crocante Sabor Salame", price: 3000, description: "Bolsa de 1kg - sabor salame." },
      { id: "fallback-frito", name: "Frito Salado Con Piel", price: 3000, description: "Bolsa de 1kg - frito salado con piel." },
      { id: "fallback-provenzal", name: "Crocante Sabor Provenzal", price: 3000, description: "Bolsa de 1kg - sabor provenzal." },
      { id: "fallback-envaina", name: "En Vaina", price: 3000, description: "Bolsa de 1kg - mani en vaina." },
    ],
    []
  );

  useEffect(() => {
    async function load() {
      try {
        setLoading(true);
        setError("");
        const data = await fetchProducts(1);
        setProducts(data || []);
      } catch {
        setError("No pudimos cargar los productos. Verifica backend en 8080.");
      } finally {
        setLoading(false);
      }
    }

    load();
  }, []);

  const displayProducts = products.length > 0 ? products : fallbackProducts;
  const subtotal = items.reduce((acc, item) => acc + Number(item.price || 0) * Number(item.quantity || 1), 0);
  const totalKg = items.reduce((acc, item) => acc + Number(item.quantity || 0), 0);
  const remainingToMin = Math.max(0, ORDER_RULES.minOrderKg - totalKg);
  const progressPercent = Math.min(100, (totalKg / ORDER_RULES.minOrderKg) * 100);

  const visibleProducts = useMemo(() => {
    const normalizedQuery = query.trim().toLowerCase();
    return displayProducts.filter((product) => {
      const name = String(product.name || "").toLowerCase();
      const description = String(product.description || "").toLowerCase();
      const visual = resolveProductVisual(product.name);
      const line = String(visual.displayName || "").toLowerCase();
      return (
        !normalizedQuery ||
        name.includes(normalizedQuery) ||
        description.includes(normalizedQuery) ||
        line.includes(normalizedQuery)
      );
    });
  }, [displayProducts, query]);

  const sortedProducts = useMemo(() => {
    const featuredOrder = {
      "Blancheado Salado": 0,
      "Blancheado Sin Sal": 1,
      "Blancheado Con Piel": 2,
      "Crocante Salado": 3,
      "Crocante Sabor Jamon": 4,
      "Crocante Sabor Queso": 5,
      "Crocante Sabor Salame": 6,
      "Frito Salado Con Piel": 7,
      "Crocante Sabor Provenzal": 8,
      "En Vaina": 9,
    };

    return [...visibleProducts].sort((a, b) => {
      if (sort === "featured") {
        const aOrder = featuredOrder[resolveProductVisual(a.name).displayName] ?? 999;
        const bOrder = featuredOrder[resolveProductVisual(b.name).displayName] ?? 999;
        return aOrder - bOrder || String(a.name).localeCompare(String(b.name));
      }
      if (sort === "price-asc") return Number(a.price) - Number(b.price);
      if (sort === "price-desc") return Number(b.price) - Number(a.price);
      if (sort === "name-asc") return String(a.name).localeCompare(String(b.name));
      if (sort === "name-desc") return String(b.name).localeCompare(String(a.name));
      return 0;
    });
  }, [sort, visibleProducts]);

  return (
    <section className="pedido-page">
      <div className="pedido-hero">
        <div className="pedido-hero-shell">
          <div className="pedido-hero-copy">
            <span className="pedido-kicker">Catalogo mayorista</span>
            <h2 className="pedido-title">Selecciona productos y arma tu pedido</h2>
            <p className="pedido-subtitle">
              Pedido mínimo {formatKg(ORDER_RULES.minOrderKg)}. Cada bolsa es de {formatKg(ORDER_RULES.bagWeightKg)}
              y cada sabor se vende con mínimo de {formatKg(ORDER_RULES.minKgPerFlavor)}.
            </p>
            <div className="pedido-rule-strip">
              <div className="pedido-rule-card">
                <strong>{formatKg(ORDER_RULES.minOrderKg)}</strong>
                <span>Pedido mínimo</span>
              </div>
              <div className="pedido-rule-card">
                <strong>{formatKg(ORDER_RULES.bagWeightKg)}</strong>
                <span>Bolsa comercial</span>
              </div>
              <div className="pedido-rule-card">
                <strong>{formatKg(ORDER_RULES.minKgPerFlavor)}</strong>
                <span>Mínimo por sabor</span>
              </div>
            </div>
          </div>

          <div className="pedido-hero-panel">
            <span className="pedido-panel-label">Pedido actual</span>
            <h3 className="pedido-panel-title">Checkout mayorista</h3>
            <p className="pedido-panel-copy">
              Armá el pedido por kilos, revisá el avance y cerralo cuando llegues al mínimo.
            </p>
            <div className="pedido-panel-total">
              <span>Total actual</span>
              <strong>{formatKg(totalKg)}</strong>
              <p>{remainingToMin > 0 ? `Faltan ${formatKg(remainingToMin)} para llegar al mínimo.` : "Ya superaste el mínimo de compra."}</p>
              <div className="pedido-progress">
                <div className="pedido-progress-track">
                  <div className="pedido-progress-fill" style={{ width: `${progressPercent}%` }} />
                </div>
                <span>{formatKg(totalKg)} / {formatKg(ORDER_RULES.minOrderKg)}</span>
              </div>
            </div>
            <div className="pedido-panel-cta">
              <Link to="/checkout" className="pedido-btn-dark">
                Continuar a ventas ({items.length})
              </Link>
            </div>
          </div>
        </div>
      </div>

      <div className="pedido-content">
        <div className="pedido-layout">
          <div className="pedido-main">
            <div className="pedido-toolbar">
              <div className="pedido-toolbar-copy">
                <span className="pedido-toolbar-kicker">Seleccion de productos</span>
                <h3 className="pedido-toolbar-title">Busca, ordena y agrega sin perder el flujo de compra</h3>
              </div>

              <div className="pedido-toolbar-controls">
                <label className="pedido-search">
                  <span>Busqueda</span>
                  <input
                    type="search"
                    value={query}
                    onChange={(event) => setQuery(event.target.value)}
                    placeholder="Buscar productos..."
                  />
                </label>

                <label className="pedido-sort">
                  <span>Orden</span>
                  <select value={sort} onChange={(event) => setSort(event.target.value)}>
                    <option value="featured">Destacados</option>
                    <option value="price-asc">Precio: menor a mayor</option>
                    <option value="price-desc">Precio: mayor a menor</option>
                    <option value="name-asc">Nombre: A-Z</option>
                    <option value="name-desc">Nombre: Z-A</option>
                  </select>
                </label>

                <Link to="/checkout" className="pedido-btn-outline">
                  Ir a ventas ({items.length})
                </Link>
              </div>
            </div>

            <div className="pedido-rules-bar">
              <div className="pedido-rules-chip">Bolsa de 1 kg</div>
              <div className="pedido-rules-chip">Mínimo 10 kg por sabor</div>
              <div className="pedido-rules-chip">Pedido mínimo {formatKg(ORDER_RULES.minOrderKg)}</div>
            </div>

            {loading && <div className="pedido-feedback">Cargando productos...</div>}
            {error && <div className="pedido-feedback pedido-feedback-error">{error}</div>}

            <div className="pedido-grid">
              {sortedProducts.map((product, index) => {
                const visual = resolveProductVisual(product.name);
                const price = Number.isFinite(Number(product.price)) ? Number(product.price) : 0;
                const familyLabel = visual.family || "Crocante";
                const imageSrc = product.imageUrl || visual.image;

                return (
                  <article
                    key={product.id ?? product.name ?? index}
                    className="pedido-card"
                    style={{ transitionDelay: `${index * 0.08}s` }}
                  >
                    <div className="pedido-card-media" style={{ background: visual.tone }}>
                      <img
                        src={imageSrc}
                        alt={product.name}
                        className="pedido-card-image"
                        onError={(event) => {
                          event.currentTarget.src = visual.image;
                        }}
                      />
                      <span className="pedido-card-badge">{visual.displayName}</span>
                    </div>

                    <div className="pedido-card-body">
                      <div className="pedido-card-head">
                        <div>
                          <span className="pedido-card-line">{familyLabel}</span>
                          <h4 className="pedido-card-title">{visual.displayName}</h4>
                        </div>
                        <p className="pedido-card-price">{formatCurrency(price)}</p>
                      </div>

                      <p className="pedido-card-desc">{product.description || visual.note}</p>

                      <div className="pedido-card-footer">
                        <span className="pedido-card-meta">Bolsa x 1 kg · mínimo 10 kg</span>
                        <div className="pedido-card-actions">
                          {ORDER_RULES.quickAddKgOptions.map((kg) => (
                            <button
                              key={kg}
                              className="pedido-btn-quick"
                              onClick={() => addItem(product, kg)}
                            >
                              +{formatKg(kg)}
                            </button>
                          ))}
                          <button className="pedido-btn-brand" onClick={() => addItem(product, ORDER_RULES.minKgPerFlavor)}>
                            Agregar {formatKg(ORDER_RULES.minKgPerFlavor)}
                          </button>
                        </div>
                      </div>
                    </div>
                  </article>
                );
              })}
            </div>
          </div>

          <aside className="pedido-summary">
            <div className="pedido-summary-card">
              <span className="pedido-summary-kicker">Resumen</span>
              <div className="pedido-summary-total">
                <strong>{items.length}</strong>
                <span>Productos agregados</span>
              </div>
              <div className="pedido-summary-total">
                <strong>{formatKg(totalKg)}</strong>
                <span>Kilos acumulados</span>
              </div>
              <div className="pedido-summary-total">
                <strong>{formatKg(remainingToMin)}</strong>
                <span>Falta para mínimo</span>
              </div>
              <Link to="/checkout" className="pedido-btn-dark pedido-summary-cta">
                Continuar a ventas
              </Link>
            </div>

            <div className="pedido-summary-card">
              <span className="pedido-summary-kicker">Items del carrito</span>
              {items.length === 0 ? (
                <p className="pedido-summary-empty">Todavía no agregaste productos.</p>
              ) : (
                <div className="pedido-summary-items">
                  {items.map((item) => (
                    <div key={item.id} className="pedido-summary-item">
                      <div>
                        <strong>{item.name}</strong>
                        <span>{formatKg(item.quantity)} · bolsa {formatKg(ORDER_RULES.bagWeightKg)}</span>
                      </div>
                      <div className="pedido-summary-item-actions">
                        <span>{formatCurrency(Number(item.price) * item.quantity)}</span>
                        <button className="pedido-summary-remove" onClick={() => removeItem(item.id)}>
                          Quitar
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </aside>
        </div>
      </div>
    </section>
  );
}
