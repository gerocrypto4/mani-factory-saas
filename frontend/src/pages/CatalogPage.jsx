import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { fetchProducts } from "../services/api";
import { ORDER_RULES, formatCurrency, formatKg } from "../constants/orderRules";
import { buildProductDetailPath, filterCatalogProducts, getAvailableFamilies, getAvailableFlavors, getCatalogCardState, getCatalogDisplayInfo, getFallbackProducts, sortCatalogProducts } from "../data/productCatalog";
import { useCart } from "../state/use-cart";

const INITIAL_FILTERS = {
  family: "all",
  flavor: "all",
  search: "",
  sort: "featured"
};

export default function CatalogPage() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [filters, setFilters] = useState(INITIAL_FILTERS);
  const { items } = useCart();

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

  const displayProducts = products.length > 0 ? products : getFallbackProducts();
  const families = useMemo(() => getAvailableFamilies(displayProducts), [displayProducts]);
  const flavors = useMemo(
    () => getAvailableFlavors(displayProducts, filters.family === "all" ? "" : filters.family),
    [displayProducts, filters.family]
  );

  const totalKg = items.reduce((acc, item) => acc + Number(item.quantity || 0), 0);
  const remainingToMin = Math.max(0, ORDER_RULES.minOrderKg - totalKg);
  const progressPercent = Math.min(100, (totalKg / ORDER_RULES.minOrderKg) * 100);

  const filteredProducts = useMemo(() => {
    const byText = filterCatalogProducts(
      displayProducts,
      filters.search,
      filters.family === "all" ? "" : filters.family,
      filters.flavor === "all" ? "" : filters.flavor
    );

    return sortCatalogProducts(byText, filters.sort);
  }, [displayProducts, filters]);

  const activeFilterCount =
    Number(filters.family !== "all") + Number(filters.flavor !== "all") + Number(Boolean(filters.search.trim()));

  return (
    <section className="pedido-page">
      <div className="pedido-hero">
        <div className="pedido-hero-shell">
          <div className="pedido-hero-copy">
            <span className="pedido-kicker">Catalogo mayorista</span>
            <h2 className="pedido-title">Buscá por familia, sabor o estado de disponibilidad</h2>
            <p className="pedido-subtitle">
              Pedido mínimo {formatKg(ORDER_RULES.minOrderKg)}. Cada bolsa es de {formatKg(ORDER_RULES.bagWeightKg)} y
              cada sabor se vende con mínimo de {formatKg(ORDER_RULES.minKgPerFlavor)}.
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
                <span>
                  {formatKg(totalKg)} / {formatKg(ORDER_RULES.minOrderKg)}
                </span>
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
        <div className="pedido-layout pedido-layout-catalog">
          <div className="pedido-main pedido-main-catalog">
            <div className="pedido-toolbar">
              <div className="pedido-toolbar-copy">
                <span className="pedido-toolbar-kicker">Seleccion de productos</span>
                <h3 className="pedido-toolbar-title">Filtros rápidos y cards más claras</h3>
              </div>

              <div className="pedido-toolbar-controls">
                <label className="pedido-search">
                  <span>Busqueda inteligente</span>
                  <input
                    type="search"
                    value={filters.search}
                    onChange={(event) => setFilters((prev) => ({ ...prev, search: event.target.value }))}
                    placeholder="Familia, sabor, uso o descripción..."
                  />
                </label>

                <label className="pedido-sort">
                  <span>Orden</span>
                  <select
                    value={filters.sort}
                    onChange={(event) => setFilters((prev) => ({ ...prev, sort: event.target.value }))}
                  >
                    <option value="featured">Destacados</option>
                    <option value="best-sellers">Más vendidos</option>
                    <option value="available-first">Disponibilidad</option>
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

            <div className="pedido-filter-strip">
              <label className="pedido-filter-chip">
                <span>Familia</span>
                <select
                  value={filters.family}
                  onChange={(event) =>
                    setFilters((prev) => ({
                      ...prev,
                      family: event.target.value,
                      flavor: event.target.value === "all" ? prev.flavor : "all"
                    }))
                  }
                >
                  <option value="all">Todas</option>
                  {families.map((family) => (
                    <option key={family} value={family}>
                      {family}
                    </option>
                  ))}
                </select>
              </label>

              <label className="pedido-filter-chip">
                <span>Sabor</span>
                <select
                  value={filters.flavor}
                  onChange={(event) => setFilters((prev) => ({ ...prev, flavor: event.target.value }))}
                >
                  <option value="all">Todos</option>
                  {flavors.map((flavor) => (
                    <option key={flavor} value={flavor}>
                      {flavor}
                    </option>
                  ))}
                </select>
              </label>

              <button
                type="button"
                className="btn-secondary pedido-filter-clear"
                onClick={() => setFilters(INITIAL_FILTERS)}
                disabled={activeFilterCount === 0 && filters.sort === "featured"}
              >
                Limpiar filtros
              </button>
            </div>

            {loading && <div className="pedido-feedback">Cargando productos...</div>}
            {error && <div className="pedido-feedback pedido-feedback-error">{error}</div>}

            <div className="pedido-grid">
              {filteredProducts.map((product, index) => {
                const info = getCatalogDisplayInfo(product);
                const state = getCatalogCardState(product);
                const price = Number.isFinite(Number(product.price)) ? Number(product.price) : 0;
                const imageSrc = product.imageUrl || info.image;

                return (
                  <article
                    key={product.id ?? product.name ?? index}
                    className={`pedido-card pedido-card-${state.toneClass}`}
                    style={{ transitionDelay: `${index * 0.06}s` }}
                  >
                    <Link to={buildProductDetailPath(product)} className="pedido-card-detail-link">
                      <div className="pedido-card-media" style={{ background: info.tone }}>
                        <img
                          src={imageSrc}
                          alt={product.name}
                          className="pedido-card-image"
                          onError={(event) => {
                            event.currentTarget.src = info.image;
                          }}
                        />
                        <div className="pedido-card-badges">
                          <span className={`pedido-card-badge pedido-card-badge-family`}>{info.family}</span>
                          <span className={`pedido-card-badge pedido-card-badge-${state.toneClass}`}>{state.statusLabel}</span>
                        </div>
                      </div>

                      <div className="pedido-card-body">
                        <h4 className="pedido-card-title">{info.displayName}</h4>
                        <p className="pedido-card-price">{formatCurrency(price)}</p>
                      </div>
                    </Link>

                    <div className="pedido-card-footer">
                      <Link
                        to={buildProductDetailPath(product)}
                        className={`pedido-card-cta-button ${!state.canAdd ? "pedido-card-cta-disabled" : ""}`}
                        aria-disabled={!state.canAdd}
                        onClick={(e) => { if (!state.canAdd) e.preventDefault(); }}
                      >
                        {state.isOutOfStock ? "No disponible" : "Ver producto"}
                        {!state.isOutOfStock && (
                          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" style={{ marginLeft: "6px" }}>
                            <path strokeLinecap="round" strokeLinejoin="round" d="M5 12h14M13 6l6 6-6 6" />
                          </svg>
                        )}
                      </Link>
                    </div>
                  </article>
                );
              })}
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
