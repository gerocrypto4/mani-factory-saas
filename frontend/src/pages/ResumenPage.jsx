import { Link } from "react-router-dom";
import DashboardSectionHero from "../components/dashboard/DashboardSectionHero";
import { formatMoney, useDashboard } from "../state/dashboard-context";

export default function ResumenPage() {
  const { kpis } = useDashboard();

  return (
    <>
      <DashboardSectionHero
        kicker="Resumen ejecutivo"
        title="Centro de control mayorista"
        copy="Una vista clara para operar, vender y priorizar el negocio con una estética premium y ordenada."
        actions={
          <>
            <Link className="btn-secondary" to="/dashboard/pedidos">
              Ver pedidos
            </Link>
            <Link className="btn-secondary" to="/dashboard/clientes">
              Ver clientes
            </Link>
            <Link className="btn-secondary" to="/dashboard/finanzas">
              Ver finanzas
            </Link>
          </>
        }
      />

      <div className="dashboard-kpis">
        <Kpi label="Pedidos Hoy" value={kpis.pedidosHoy} />
        <Kpi label="Ventas del Mes" value={`$${formatMoney(kpis.ventasMes)}`} />
        <Kpi label="Clientes Activos" value={kpis.clientesActivos} />
        <Kpi label="Pendientes" value={kpis.pedidosPendientes} />
        <Kpi label="Productos Activos" value={kpis.productosActivos} />
      </div>

      <article className="dashboard-panel dashboard-summary-panel">
        <div className="dashboard-panel-head">
          <div>
            <h3 className="dashboard-panel-title">Accesos rápidos</h3>
            <p className="dashboard-muted">Entrá a cada módulo sin mezclar la operación.</p>
          </div>
        </div>

        <div className="dashboard-summary-grid">
          <Link className="dashboard-summary-card" to="/dashboard/pedidos">
            <span className="dashboard-field-label">Pedidos</span>
            <strong>{kpis.pedidosPendientes} pendientes</strong>
            <p>Estados, filtros y acciones rápidas.</p>
          </Link>
          <Link className="dashboard-summary-card" to="/dashboard/clientes">
            <span className="dashboard-field-label">Clientes</span>
            <strong>{kpis.clientesActivos} activos</strong>
            <p>Búsqueda, historial y último pedido.</p>
          </Link>
          <Link className="dashboard-summary-card" to="/dashboard/finanzas">
            <span className="dashboard-field-label">Finanzas</span>
            <strong>Ingresos / egresos</strong>
            <p>Resultado neto y categorías operativas.</p>
          </Link>
        </div>
      </article>
    </>
  );
}

function Kpi({ label, value }) {
  return (
    <article className="dashboard-kpi">
      <p className="dashboard-kpi-label">{label}</p>
      <p className="dashboard-kpi-value">{value}</p>
    </article>
  );
}
