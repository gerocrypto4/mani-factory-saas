import { useMemo, useState } from "react";
import DashboardSectionHero from "../components/dashboard/DashboardSectionHero";
import { formatMoney, formatShortDate } from "../state/dashboard-context";
import {
  FINANCE_EXPENSE_CATEGORIES,
  useFinanceLedger
} from "../state/finance-ledger";

const INITIAL_FILTER = { category: "ALL", query: "" };
const MONTH_OPTIONS = [
  { value: "01", label: "Enero" },
  { value: "02", label: "Febrero" },
  { value: "03", label: "Marzo" },
  { value: "04", label: "Abril" },
  { value: "05", label: "Mayo" },
  { value: "06", label: "Junio" },
  { value: "07", label: "Julio" },
  { value: "08", label: "Agosto" },
  { value: "09", label: "Septiembre" },
  { value: "10", label: "Octubre" },
  { value: "11", label: "Noviembre" },
  { value: "12", label: "Diciembre" }
];

const YEAR_OPTIONS = createYearOptions();

export default function FinanzasPage() {
  const {
    records,
    addRecord,
    removeRecord,
    summary,
    expenseByCategory,
    period,
    setPeriod,
    loading,
    saving,
    error
  } = useFinanceLedger();
  const [expenseForm, setExpenseForm] = useState(createExpenseForm());
  const [filters, setFilters] = useState(INITIAL_FILTER);
  const [feedback, setFeedback] = useState(null);

  const filteredRecords = useMemo(() => {
    return [...records]
      .filter((record) => {
        if (filters.category !== "ALL" && record.category !== filters.category) return false;
        const haystack = `${record.category} ${record.note} ${record.entryDate}`.toLowerCase();
        if (filters.query && !haystack.includes(filters.query.toLowerCase())) return false;
        return true;
      })
      .sort((a, b) => new Date(b.entryDate).getTime() - new Date(a.entryDate).getTime());
  }, [records, filters]);

  async function handleExpenseSubmit(event) {
    event.preventDefault();
    const ok = await addRecord({
      type: "expense",
      amount: expenseForm.amount,
      category: expenseForm.category,
      note: expenseForm.note,
      entryDate: expenseForm.entryDate
    });

    if (!ok) {
      setFeedback({ type: "error", message: "Ingresá un monto válido para registrar el egreso." });
      return;
    }

    setExpenseForm(createExpenseForm());
    setFeedback({ type: "success", message: "Egreso registrado correctamente." });
  }

  const clearFilters = () => setFilters(INITIAL_FILTER);

  return (
    <>
      <DashboardSectionHero
        kicker="Finanzas"
        title="Ingresos y gastos del período"
        copy="Los ingresos se registran automáticamente desde pedidos confirmados. Registrá los gastos operativos y seguí el resultado neto del mes."
      />

      {loading && <div className="dashboard-alert">Cargando finanzas...</div>}
      {error && <div className="dashboard-alert dashboard-alert-error">{error}</div>}

      <article className="dashboard-panel dashboard-section">
        <div className="dashboard-panel-head">
          <div>
            <h3 className="dashboard-panel-title">Resumen del período</h3>
            <p className="dashboard-muted">Resultado financiero según ventas y gastos registrados.</p>
          </div>
          <div className="dashboard-finance-period-row">
            <label>
              <span className="dashboard-field-label">Mes</span>
              <select
                className="dashboard-select"
                value={period.month}
                onChange={(event) => setPeriod((prev) => ({ ...prev, month: event.target.value }))}
              >
                {MONTH_OPTIONS.map((month) => (
                  <option key={month.value} value={month.value}>
                    {month.label}
                  </option>
                ))}
              </select>
            </label>

            <label>
              <span className="dashboard-field-label">Año</span>
              <select
                className="dashboard-select"
                value={period.year}
                onChange={(event) => setPeriod((prev) => ({ ...prev, year: event.target.value }))}
              >
                {YEAR_OPTIONS.map((year) => (
                  <option key={year} value={year}>
                    {year}
                  </option>
                ))}
              </select>
            </label>
          </div>
        </div>

        <div className="dashboard-finance-summary-grid">
          <MetricCard
            label="Ventas del mes"
            value={`$${formatMoney(summary.systemSales)}`}
            hint={`${summary.salesOrdersCount} pedido${summary.salesOrdersCount !== 1 ? "s" : ""} confirmado${summary.salesOrdersCount !== 1 ? "s" : ""}`}
          />
          <MetricCard
            label="Gastos del mes"
            value={`$${formatMoney(summary.expenses)}`}
            hint="Gastos operativos registrados"
          />
          <MetricCard
            label="Resultado neto"
            value={`$${formatMoney(summary.netResult)}`}
            hint="Ventas menos gastos del período"
            accent
          />
        </div>
      </article>

      <article className="dashboard-panel dashboard-section">
        <div className="dashboard-panel-head">
          <div>
            <h3 className="dashboard-panel-title">Gastos por categoría</h3>
            <p className="dashboard-muted">Distribución del período seleccionado para controlar la estructura de gastos.</p>
          </div>
          <span className="dashboard-pill">{formatPeriodLabel(period)}</span>
        </div>

        <div className="dashboard-finance-category-grid">
          {expenseByCategory.map((item) => (
            <div key={item.category} className="dashboard-finance-category-card">
              <span>{capitalize(item.category)}</span>
              <strong>${formatMoney(item.total)}</strong>
            </div>
          ))}
        </div>
      </article>

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
            <h3 className="dashboard-panel-title">Registrar gasto</h3>
            <p className="dashboard-muted">Gasto operativo: insumos, sueldos, alquileres, transporte u otros.</p>
          </div>
          <span className="dashboard-pill">Egreso</span>
        </div>

        <form className="dashboard-finance-form" onSubmit={handleExpenseSubmit}>
          <div className="dashboard-finance-field-grid">
            <label>
              <span className="dashboard-field-label">Monto</span>
              <input
                className="dashboard-input"
                inputMode="decimal"
                placeholder="0"
                value={expenseForm.amount}
                onChange={(event) => setExpenseForm((prev) => ({ ...prev, amount: event.target.value }))}
              />
            </label>

            <label>
              <span className="dashboard-field-label">Categoría</span>
              <select
                className="dashboard-select"
                value={expenseForm.category}
                onChange={(event) => setExpenseForm((prev) => ({ ...prev, category: event.target.value }))}
              >
                {FINANCE_EXPENSE_CATEGORIES.map((category) => (
                  <option key={category} value={category}>
                    {capitalize(category)}
                  </option>
                ))}
              </select>
            </label>

            <label>
              <span className="dashboard-field-label">Fecha</span>
              <input
                className="dashboard-input"
                type="date"
                value={expenseForm.entryDate}
                onChange={(event) => setExpenseForm((prev) => ({ ...prev, entryDate: event.target.value }))}
              />
            </label>
          </div>

          <label>
            <span className="dashboard-field-label">Detalle libre</span>
            <textarea
              className="dashboard-input dashboard-textarea"
              rows="3"
              placeholder="Ej: compra de insumos, flete, sueldo, alquiler, etc."
              value={expenseForm.note}
              onChange={(event) => setExpenseForm((prev) => ({ ...prev, note: event.target.value }))}
            />
          </label>

          <button type="submit" className="btn-primary" disabled={saving}>
            {saving ? "Guardando..." : "Registrar gasto"}
          </button>
        </form>
      </article>

      <article className="dashboard-panel dashboard-section">
        <div className="dashboard-panel-head">
          <div>
            <h3 className="dashboard-panel-title">Movimientos</h3>
            <p className="dashboard-muted">Movimientos persistidos en el backend para seguir el flujo financiero.</p>
          </div>
          <span className="dashboard-pill">{filteredRecords.length} registros</span>
        </div>

        <div className="dashboard-finance-filters">
          <label>
            <span className="dashboard-field-label">Buscar</span>
            <input
              className="dashboard-input"
              value={filters.query}
              onChange={(event) => setFilters((prev) => ({ ...prev, query: event.target.value }))}
              placeholder="Categoría, nota o fecha..."
            />
          </label>

          <label>
            <span className="dashboard-field-label">Categoría</span>
            <select
              className="dashboard-select"
              value={filters.category}
              onChange={(event) => setFilters((prev) => ({ ...prev, category: event.target.value }))}
            >
              <option value="ALL">Todas</option>
              {FINANCE_EXPENSE_CATEGORIES.map((category) => (
                <option key={category} value={category}>
                  {capitalize(category)}
                </option>
              ))}
            </select>
          </label>

          <div className="dashboard-finance-filter-actions">
            <button
              type="button"
              className="btn-secondary"
              onClick={clearFilters}
              disabled={!filters.query && filters.category === "ALL"}
            >
              Limpiar filtros
            </button>
          </div>
        </div>

        <div className="dashboard-finance-ledger">
          {filteredRecords.length === 0 ? (
            <div className="dashboard-empty">No hay movimientos para los filtros seleccionados.</div>
          ) : (
            filteredRecords.map((record) => (
              <article key={record.id} className="dashboard-finance-ledger-item">
                <div className="dashboard-finance-ledger-main">
                  <div className="dashboard-finance-ledger-head">
                    <span className="dashboard-muted">{formatShortDate(record.entryDate)}</span>
                  </div>

                  <p className="dashboard-finance-ledger-title">{capitalize(record.category)}</p>
                  <p className="dashboard-muted">{record.note || "Sin detalle adicional"}</p>
                </div>

                <div className="dashboard-finance-ledger-side">
                  <strong className="dashboard-finance-negative">
                    -${formatMoney(record.amount)}
                  </strong>
                  <button type="button" className="dashboard-history-link" onClick={() => removeRecord(record.id)}>
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
    <article className={`dashboard-finance-metric-card ${accent ? "dashboard-finance-metric-card-accent" : ""}`}>
      <p className="dashboard-kpi-label">{label}</p>
      <p className="dashboard-finance-metric-value">{value}</p>
      <p className="dashboard-finance-metric-hint">{hint}</p>
    </article>
  );
}

function createExpenseForm() {
  return {
    amount: "",
    category: FINANCE_EXPENSE_CATEGORIES[0],
    entryDate: todayIsoDate(),
    note: ""
  };
}

function createYearOptions() {
  const currentYear = new Date().getFullYear();
  return [currentYear - 1, currentYear, currentYear + 1].map((year) => String(year));
}

function formatPeriodLabel(period) {
  if (!period?.month || !period?.year) return "Periodo actual";
  const monthIndex = Number(period.month) - 1;
  const label = MONTH_OPTIONS[monthIndex]?.label || "Mes";
  return `${label} ${period.year}`;
}

function todayIsoDate() {
  return new Date().toISOString().slice(0, 10);
}

function capitalize(value) {
  return String(value || "")
    .replace(/-/g, " ")
    .replace(/\b\w/g, (char) => char.toUpperCase());
}
