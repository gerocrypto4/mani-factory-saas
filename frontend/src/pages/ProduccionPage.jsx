import { useMemo, useState } from "react";
import DashboardSectionHero from "../components/dashboard/DashboardSectionHero";
import { formatShortDate } from "../state/dashboard-context";
import { useProductionBatches } from "../state/production-batches";

export default function ProduccionPage() {
  const { batches, products, addBatch, removeBatch, loading, saving, error } = useProductionBatches();
  const [form, setForm] = useState(createForm);
  const [feedback, setFeedback] = useState(null);

  const currentYearMonth = new Date().toISOString().slice(0, 7);

  const batchesThisMonth = useMemo(
    () => batches.filter((b) => String(b.batchDate || "").startsWith(currentYearMonth)),
    [batches, currentYearMonth]
  );

  const totalKgThisMonth = useMemo(
    () => batchesThisMonth.reduce((acc, b) => acc + Number(b.quantityKg || 0), 0),
    [batchesThisMonth]
  );

  async function handleSubmit(event) {
    event.preventDefault();
    const ok = await addBatch(form);
    if (!ok) {
      setFeedback({ type: "error", message: "Verificá que el producto y la cantidad sean válidos." });
      return;
    }
    setForm(createForm());
    setFeedback({ type: "success", message: "Lote registrado correctamente. El stock fue actualizado." });
  }

  return (
    <>
      <DashboardSectionHero
        kicker="Producción"
        title="Lotes de producción"
        copy="Registrá cada lote producido. El stock se actualiza automáticamente al completar un lote."
      />

      {loading && <div className="dashboard-alert">Cargando producción...</div>}
      {error && <div className="dashboard-alert dashboard-alert-error">{error}</div>}

      <div className="dashboard-finance-summary-grid">
        <MetricCard
          label="Lotes este mes"
          value={String(batchesThisMonth.length)}
          hint="Lotes completados en el mes actual"
        />
        <MetricCard
          label="Total producido (kg)"
          value={`${formatKg(totalKgThisMonth)} kg`}
          hint="Kilogramos producidos este mes"
          accent
        />
      </div>

      {feedback && (
        <div
          className={`dashboard-alert ${
            feedback.type === "error" ? "dashboard-alert-error" : "dashboard-alert-success"
          }`}
        >
          {feedback.message}
        </div>
      )}

      <article className="dashboard-panel dashboard-finance-form-card">
        <div className="dashboard-panel-head">
          <div>
            <h3 className="dashboard-panel-title">Registrar lote</h3>
            <p className="dashboard-muted">Ingresá el producto, cantidad producida y fecha del lote.</p>
          </div>
          <span className="dashboard-pill">Lote</span>
        </div>

        <form className="dashboard-finance-form" onSubmit={handleSubmit}>
          <div className="dashboard-finance-field-grid">
            <label>
              <span className="dashboard-field-label">Producto</span>
              <select
                className="dashboard-select"
                value={form.productId}
                onChange={(e) => setForm((prev) => ({ ...prev, productId: e.target.value }))}
              >
                <option value="">Seleccioná un producto</option>
                {products.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.name}
                  </option>
                ))}
              </select>
            </label>

            <label>
              <span className="dashboard-field-label">Cantidad (kg)</span>
              <input
                className="dashboard-input"
                inputMode="decimal"
                placeholder="0"
                value={form.quantityKg}
                onChange={(e) => setForm((prev) => ({ ...prev, quantityKg: e.target.value }))}
              />
            </label>

            <label>
              <span className="dashboard-field-label">Fecha</span>
              <input
                className="dashboard-input"
                type="date"
                value={form.batchDate}
                onChange={(e) => setForm((prev) => ({ ...prev, batchDate: e.target.value }))}
              />
            </label>
          </div>

          <label>
            <span className="dashboard-field-label">Detalle libre</span>
            <textarea
              className="dashboard-input dashboard-textarea"
              rows="2"
              placeholder="Ej: lote de maní salado, turno mañana, etc."
              value={form.note}
              onChange={(e) => setForm((prev) => ({ ...prev, note: e.target.value }))}
            />
          </label>

          <button type="submit" className="btn-primary" disabled={saving || !form.productId}>
            {saving ? "Guardando..." : "Registrar lote"}
          </button>
        </form>
      </article>

      <article className="dashboard-panel dashboard-section">
        <div className="dashboard-panel-head">
          <div>
            <h3 className="dashboard-panel-title">Historial de lotes</h3>
            <p className="dashboard-muted">Todos los lotes de producción registrados.</p>
          </div>
          <span className="dashboard-pill">{batches.length} lotes</span>
        </div>

        <div className="dashboard-finance-ledger">
          {batches.length === 0 ? (
            <div className="dashboard-empty">No hay lotes registrados todavía.</div>
          ) : (
            batches.map((batch) => (
              <article key={batch.id} className="dashboard-finance-ledger-item">
                <div className="dashboard-finance-ledger-main">
                  <div className="dashboard-finance-ledger-head">
                    <span className="dashboard-muted">{formatShortDate(batch.batchDate)}</span>
                  </div>
                  <p className="dashboard-finance-ledger-title">{batch.productName}</p>
                  <p className="dashboard-muted">{batch.note || "Sin detalle adicional"}</p>
                </div>

                <div className="dashboard-finance-ledger-side">
                  <strong className="dashboard-finance-positive">
                    +{formatKg(batch.quantityKg)} kg
                  </strong>
                  <button
                    type="button"
                    className="dashboard-history-link"
                    onClick={() => removeBatch(batch.id)}
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
    quantityKg: "",
    batchDate: new Date().toISOString().slice(0, 10),
    note: ""
  };
}

function formatKg(value) {
  const num = Number(value || 0);
  if (num === 0) return "0";
  return num % 1 === 0 ? String(num) : num.toFixed(3).replace(/\.?0+$/, "");
}
