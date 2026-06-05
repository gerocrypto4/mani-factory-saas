import { useMemo, useState } from "react";
import DashboardSectionHero from "../components/dashboard/DashboardSectionHero";
import { formatShortDate } from "../state/dashboard-context";
import { useStockLedger } from "../state/stock-ledger";

const INITIAL_FILTER = "ALL";

export default function StockPage() {
  const { entries, levels, products, addEntry, removeEntry, loading, saving, error } = useStockLedger();
  const [entryForm, setEntryForm] = useState(createForm);
  const [typeFilter, setTypeFilter] = useState(INITIAL_FILTER);
  const [feedback, setFeedback] = useState(null);

  const totalKg = useMemo(
    () => levels.reduce((acc, l) => acc + Number(l.currentQuantityKg || 0), 0),
    [levels]
  );
  const productsWithStock = useMemo(
    () => levels.filter((l) => Number(l.currentQuantityKg || 0) > 0).length,
    [levels]
  );

  const filteredEntries = useMemo(() => {
    if (typeFilter === "ALL") return entries;
    return entries.filter((e) => e.type === typeFilter);
  }, [entries, typeFilter]);

  async function handleEntrySubmit(event) {
    event.preventDefault();
    const ok = await addEntry({ ...entryForm, type: "ENTRY" });
    if (!ok) {
      setFeedback({ type: "error", message: "Ingresá una cantidad válida para registrar la entrada." });
      return;
    }
    setEntryForm(createForm());
    setFeedback({ type: "success", message: "Entrada de stock registrada correctamente." });
  }

  return (
    <>
      <DashboardSectionHero
        kicker="Stock"
        title="Control de inventario"
        copy="Saldo actual por producto. Las entradas vienen de Producción y las salidas de pedidos confirmados. Usá esta sección solo para ajustes y correcciones manuales."
      />

      {loading && <div className="dashboard-alert">Cargando stock...</div>}
      {error && <div className="dashboard-alert dashboard-alert-error">{error}</div>}

      <div className="dashboard-finance-summary-grid">
        <MetricCard
          label="Productos con stock"
          value={String(productsWithStock)}
          hint="Con al menos 1 kg disponible"
        />
        <MetricCard
          label="Total en inventario"
          value={`${formatKg(totalKg)} kg`}
          hint="Suma de todos los productos"
          accent
        />
      </div>

      {levels.length > 0 && (
        <article className="dashboard-panel dashboard-section">
          <div className="dashboard-panel-head">
            <div>
              <h3 className="dashboard-panel-title">Niveles actuales</h3>
              <p className="dashboard-muted">Stock disponible por producto según movimientos registrados.</p>
            </div>
            <span className="dashboard-pill">{levels.length} productos</span>
          </div>

          <div className="dashboard-finance-category-grid">
            {levels.map((level) => (
              <div key={level.productId} className="dashboard-finance-category-card">
                <span>{level.productName}</span>
                <strong>{formatKg(level.currentQuantityKg)} kg</strong>
                {level.lastMovementDate && (
                  <span className="dashboard-muted" style={{ fontSize: "0.75rem" }}>
                    {formatShortDate(level.lastMovementDate)}
                  </span>
                )}
              </div>
            ))}
          </div>
        </article>
      )}

      {feedback && (
        <div
          className={`dashboard-alert ${
            feedback.type === "error" ? "dashboard-alert-error" : "dashboard-alert-success"
          }`}
        >
          {feedback.message}
        </div>
      )}

      <div className="dashboard-finance-forms">
        <article className="dashboard-panel dashboard-finance-form-card">
          <div className="dashboard-panel-head">
            <div>
              <h3 className="dashboard-panel-title">Ajuste de inventario</h3>
              <p className="dashboard-muted">Para correcciones manuales, stock de proveedor externo o carga inicial. La producción propia se registra en el módulo Producción.</p>
            </div>
            <span className="dashboard-pill">Ajuste</span>
          </div>

          <form className="dashboard-finance-form" onSubmit={handleEntrySubmit}>
            <div className="dashboard-finance-field-grid">
              <label>
                <span className="dashboard-field-label">Producto</span>
                <select
                  className="dashboard-select"
                  value={entryForm.productId}
                  onChange={(e) => setEntryForm((prev) => ({ ...prev, productId: e.target.value }))}
                >
                  <option value="">Seleccioná un producto</option>
                  {products.map((p) => (
                    <option key={p.id} value={p.id}>{p.name}</option>
                  ))}
                </select>
              </label>

              <label>
                <span className="dashboard-field-label">Cantidad (kg)</span>
                <input
                  className="dashboard-input"
                  inputMode="decimal"
                  placeholder="0"
                  value={entryForm.quantity}
                  onChange={(e) => setEntryForm((prev) => ({ ...prev, quantity: e.target.value }))}
                />
              </label>

              <label>
                <span className="dashboard-field-label">Fecha</span>
                <input
                  className="dashboard-input"
                  type="date"
                  value={entryForm.entryDate}
                  onChange={(e) => setEntryForm((prev) => ({ ...prev, entryDate: e.target.value }))}
                />
              </label>
            </div>

            <label>
              <span className="dashboard-field-label">Detalle libre</span>
              <textarea
                className="dashboard-input dashboard-textarea"
                rows="2"
                placeholder="Ej: corrección de inventario, compra a proveedor externo, carga inicial, etc."
                value={entryForm.note}
                onChange={(e) => setEntryForm((prev) => ({ ...prev, note: e.target.value }))}
              />
            </label>

            <button type="submit" className="btn-primary" disabled={saving || !entryForm.productId}>
              {saving ? "Guardando..." : "Registrar entrada"}
            </button>
          </form>
        </article>

      </div>

      <article className="dashboard-panel dashboard-section">
        <div className="dashboard-panel-head">
          <div>
            <h3 className="dashboard-panel-title">Movimientos</h3>
            <p className="dashboard-muted">Historial completo de entradas y salidas de inventario.</p>
          </div>
          <span className="dashboard-pill">{filteredEntries.length} registros</span>
        </div>

        <div className="dashboard-finance-filters">
          <label>
            <span className="dashboard-field-label">Tipo</span>
            <select
              className="dashboard-select"
              value={typeFilter}
              onChange={(e) => setTypeFilter(e.target.value)}
            >
              <option value="ALL">Todos</option>
              <option value="ENTRY">Entradas</option>
              <option value="WITHDRAWAL">Salidas</option>
            </select>
          </label>

          <div className="dashboard-finance-filter-actions">
            <button
              type="button"
              className="btn-secondary"
              onClick={() => setTypeFilter(INITIAL_FILTER)}
              disabled={typeFilter === "ALL"}
            >
              Limpiar filtros
            </button>
          </div>
        </div>

        <div className="dashboard-finance-ledger">
          {filteredEntries.length === 0 ? (
            <div className="dashboard-empty">No hay movimientos para el filtro seleccionado.</div>
          ) : (
            filteredEntries.map((entry) => (
              <article key={entry.id} className="dashboard-finance-ledger-item">
                <div className="dashboard-finance-ledger-main">
                  <div className="dashboard-finance-ledger-head">
                    <span
                      className={`dashboard-finance-type ${
                        entry.type === "ENTRY" ? "dashboard-finance-type-income" : "dashboard-finance-type-expense"
                      }`}
                    >
                      {entry.type === "ENTRY" ? "Entrada" : "Salida"}
                    </span>
                    <span className="dashboard-muted">{formatShortDate(entry.entryDate)}</span>
                  </div>

                  <p className="dashboard-finance-ledger-title">{entry.productName}</p>
                  <p className="dashboard-muted">{entry.note || "Sin detalle adicional"}</p>
                </div>

                <div className="dashboard-finance-ledger-side">
                  <strong
                    className={
                      entry.type === "WITHDRAWAL"
                        ? "dashboard-finance-negative"
                        : "dashboard-finance-positive"
                    }
                  >
                    {entry.type === "WITHDRAWAL" ? "-" : "+"}
                    {formatKg(entry.quantity)} kg
                  </strong>
                  <button
                    type="button"
                    className="dashboard-history-link"
                    onClick={() => removeEntry(entry.id)}
                  >
                    Eliminar
                  </button>
                </div>
              </article>
            ))
          )}
        </div>
      </article>
    </>
  );
}

function MetricCard({ label, value, hint, accent = false }) {
  return (
    <article
      className={`dashboard-finance-metric-card ${accent ? "dashboard-finance-metric-card-accent" : ""}`}
    >
      <p className="dashboard-kpi-label">{label}</p>
      <p className="dashboard-finance-metric-value">{value}</p>
      <p className="dashboard-finance-metric-hint">{hint}</p>
    </article>
  );
}

function createForm() {
  return {
    productId: "",
    quantity: "",
    entryDate: new Date().toISOString().slice(0, 10),
    note: ""
  };
}

function formatKg(value) {
  const num = Number(value || 0);
  if (num === 0) return "0";
  return num % 1 === 0 ? String(num) : num.toFixed(3).replace(/\.?0+$/, "");
}
