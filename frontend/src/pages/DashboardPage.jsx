import { Outlet } from "react-router-dom";
import DashboardOrderModal from "../components/dashboard/DashboardOrderModal";
import { DashboardProvider, useDashboard } from "../state/dashboard-context";

export default function DashboardPage() {
  return (
    <DashboardProvider>
      <DashboardShell />
    </DashboardProvider>
  );
}

function DashboardShell() {
  const { authed, loading, clientsLoading, error, success, doLogin, auth, setAuth, refreshDashboard, logout } =
    useDashboard();

  if (!authed) {
    return (
      <section className="dashboard-page dashboard-login-shell">
        <form className="dashboard-login-card" onSubmit={doLogin}>
          <p className="dashboard-kicker">Panel interno</p>
          <h2 className="dashboard-title">Acceso de fábrica</h2>
          <p className="dashboard-copy">Ingresá para gestionar pedidos, clientes y operación.</p>
          <div className="dashboard-form-stack">
            <input
              value={auth.username}
              onChange={(event) => setAuth({ ...auth, username: event.target.value })}
              className="dashboard-input"
              placeholder="Usuario"
            />
            <input
              type="password"
              value={auth.password}
              onChange={(event) => setAuth({ ...auth, password: event.target.value })}
              className="dashboard-input"
              placeholder="Contraseña"
            />
          </div>
          {error && <p className="dashboard-error">{error}</p>}
          <button className="btn-primary mt-5 w-full">{loading ? "Ingresando..." : "Entrar al panel"}</button>
        </form>
      </section>
    );
  }

  return (
    <section className="dashboard-page">
      <div className="dashboard-layout-toolbar">
        <div className="dashboard-layout-toolbar-copy">
          <p className="dashboard-section-label">Panel interno</p>
          <p className="dashboard-muted">Módulos reales por página, sin mezclar operación y estrategia.</p>
        </div>

        <div className="dashboard-actions">
          <button type="button" className="btn-secondary" onClick={refreshDashboard}>
            {loading || clientsLoading ? "Actualizando..." : "Actualizar todo"}
          </button>
          <button type="button" className="btn-secondary" onClick={logout}>
            Cerrar sesión
          </button>
        </div>
      </div>

      {error && <div className="dashboard-alert dashboard-alert-error">{error}</div>}
      {success && <div className="dashboard-alert dashboard-alert-success">{success}</div>}
      {loading && <div className="dashboard-alert">Cargando datos...</div>}

      <div className="dashboard-route-stack">
        <Outlet />
      </div>

      <DashboardOrderModal />
    </section>
  );
}
