import { useEffect, useMemo, useState } from "react";
import {
  clearToken,
  fetchDashboardClients,
  fetchDashboardOrders,
  fetchDashboardProducts,
  hasToken,
  login,
  saveToken,
  updateDashboardOrderStatus
} from "../services/api";

const tenantId = 1;

export default function DashboardPage() {
  const [auth, setAuth] = useState({ username: "", password: "" });
  const [authed, setAuthed] = useState(hasToken());
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [updatingOrderId, setUpdatingOrderId] = useState(null);
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [orders, setOrders] = useState([]);
  const [clients, setClients] = useState([]);
  const [products, setProducts] = useState([]);

  useEffect(() => {
    if (!authed) return;
    loadDashboard();
  }, [authed]);

  async function doLogin(e) {
    e.preventDefault();
    try {
      setLoading(true);
      setError("");
      setSuccess("");
      const token = await login(auth.username, auth.password);
      saveToken(token);
      setAuthed(true);
    } catch {
      setError("No pudimos iniciar sesion. Verifica usuario y password.");
    } finally {
      setLoading(false);
    }
  }

  async function loadDashboard() {
    try {
      setLoading(true);
      setError("");
      setSuccess("");
      const [o, c, p] = await Promise.all([
        fetchDashboardOrders(tenantId),
        fetchDashboardClients(tenantId),
        fetchDashboardProducts(tenantId)
      ]);
      setOrders(o || []);
      setClients(c || []);
      setProducts(p || []);
    } catch {
      setError("No pudimos cargar datos del dashboard.");
    } finally {
      setLoading(false);
    }
  }

  async function changeOrderStatus(orderId, nextStatus) {
    try {
      setUpdatingOrderId(orderId);
      setError("");
      setSuccess("");
      await updateDashboardOrderStatus(orderId, nextStatus, tenantId);
      setOrders((prev) =>
        prev.map((o) => (o.id === orderId ? { ...o, status: nextStatus } : o))
      );
      setSuccess(`Pedido #${orderId} actualizado a ${nextStatus}.`);
    } catch {
      setError(`No pudimos actualizar el estado del pedido #${orderId}.`);
    } finally {
      setUpdatingOrderId(null);
    }
  }

  const kpis = useMemo(() => {
    const now = new Date();
    const monthOrders = orders.filter((o) => {
      if (!o.createdAt) return false;
      const d = new Date(o.createdAt);
      return d.getMonth() === now.getMonth() && d.getFullYear() === now.getFullYear();
    });
    const ventasMes = monthOrders.reduce((acc, o) => acc + Number(o.total || 0), 0);
    const pendientes = orders.filter((o) => String(o.status).toUpperCase() === "PENDING").length;
    return {
      pedidosHoy: orders.filter((o) => isToday(o.createdAt)).length,
      ventasMes,
      clientesActivos: clients.length,
      pedidosPendientes: pendientes,
      productosActivos: products.filter((p) => p.active).length
    };
  }, [orders, clients, products]);

  const visibleOrders = useMemo(() => {
    if (statusFilter === "ALL") return orders;
    return orders.filter((o) => String(o.status || "").toUpperCase() === statusFilter);
  }, [orders, statusFilter]);

  if (!authed) {
    return (
      <section className="mx-auto max-w-md">
        <form className="panel-premium rounded-2xl p-6" onSubmit={doLogin}>
          <p className="text-xs uppercase tracking-[0.22em] text-brand-mint">Panel Interno</p>
          <h2 className="mt-2 text-2xl font-bold">Acceso de Fabrica</h2>
          <p className="mt-2 text-sm text-brand-soft">Ingresa para gestionar pedidos, clientes y operacion.</p>
          <div className="mt-5 space-y-3">
            <input
              value={auth.username}
              onChange={(e) => setAuth({ ...auth, username: e.target.value })}
              className="w-full rounded-xl border border-white/20 bg-transparent px-3 py-2"
              placeholder="Usuario"
            />
            <input
              type="password"
              value={auth.password}
              onChange={(e) => setAuth({ ...auth, password: e.target.value })}
              className="w-full rounded-xl border border-white/20 bg-transparent px-3 py-2"
              placeholder="Password"
            />
          </div>
          {error && <p className="mt-3 text-sm text-red-300">{error}</p>}
          <button className="btn-primary mt-5 w-full">{loading ? "Ingresando..." : "Entrar al Dashboard"}</button>
        </form>
      </section>
    );
  }

  return (
    <section className="space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-2xl font-bold">Dashboard Operativo</h2>
        <div className="flex gap-2">
          <button className="btn-secondary" onClick={loadDashboard}>Actualizar</button>
          <button
            className="btn-secondary"
            onClick={() => {
              clearToken();
              setAuthed(false);
            }}
          >
            Cerrar sesion
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-5">
        <Kpi label="Pedidos Hoy" value={kpis.pedidosHoy} />
        <Kpi label="Ventas del Mes" value={`$${formatMoney(kpis.ventasMes)}`} />
        <Kpi label="Clientes Activos" value={kpis.clientesActivos} />
        <Kpi label="Pendientes" value={kpis.pedidosPendientes} />
        <Kpi label="Productos Activos" value={kpis.productosActivos} />
      </div>

      {error && <div className="panel-premium rounded-xl p-3 text-red-300">{error}</div>}
      {success && <div className="panel-premium rounded-xl p-3 text-emerald-300">{success}</div>}
      {loading && <div className="panel-premium rounded-xl p-3 text-brand-soft">Cargando datos...</div>}

      <div className="grid grid-cols-1 gap-6 xl:grid-cols-3">
        <article className="panel-premium rounded-2xl p-6 xl:col-span-2">
          <div className="flex items-center justify-between gap-3">
            <h3 className="text-lg font-semibold">Pedidos Recientes</h3>
            <select
              className="rounded-lg border border-white/20 bg-transparent px-3 py-2 text-sm"
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
            >
              <option value="ALL">Todos</option>
              <option value="PENDING">Pendientes</option>
              <option value="CONFIRMED">Confirmados</option>
              <option value="SHIPPED">Despachados</option>
              <option value="DELIVERED">Entregados</option>
              <option value="CANCELLED">Cancelados</option>
            </select>
          </div>
          <div className="mt-4 space-y-3">
            {visibleOrders.slice(0, 10).map((o) => (
              <div key={o.id} className="flex items-center justify-between rounded-xl border border-white/10 p-3">
                <div>
                  <p className="font-semibold">#{o.id}</p>
                  <p className="text-sm text-brand-soft">{o.createdAt ? new Date(o.createdAt).toLocaleString() : "-"}</p>
                </div>
                <div className="text-right">
                  <Badge estado={o.status} />
                  <p className="mt-1 font-semibold text-brand-gold">${formatMoney(Number(o.total || 0))}</p>
                  <div className="mt-2 flex flex-wrap justify-end gap-2">
                    <StatusAction
                      label="Confirmar"
                      onClick={() => changeOrderStatus(o.id, "CONFIRMED")}
                      disabled={updatingOrderId === o.id || String(o.status).toUpperCase() === "CONFIRMED"}
                    />
                    <StatusAction
                      label="Despachar"
                      onClick={() => changeOrderStatus(o.id, "SHIPPED")}
                      disabled={updatingOrderId === o.id || String(o.status).toUpperCase() === "SHIPPED"}
                    />
                    <StatusAction
                      label="Entregar"
                      onClick={() => changeOrderStatus(o.id, "DELIVERED")}
                      disabled={updatingOrderId === o.id || String(o.status).toUpperCase() === "DELIVERED"}
                    />
                    <StatusAction
                      label="Cancelar"
                      onClick={() => {
                        const ok = window.confirm(`Vas a cancelar el pedido #${o.id}. Queres continuar?`);
                        if (!ok) return;
                        changeOrderStatus(o.id, "CANCELLED");
                      }}
                      disabled={updatingOrderId === o.id || String(o.status).toUpperCase() === "CANCELLED"}
                    />
                  </div>
                </div>
              </div>
            ))}
            {visibleOrders.length === 0 && (
              <div className="rounded-xl border border-white/10 p-4 text-sm text-brand-soft">
                No hay pedidos para el filtro seleccionado.
              </div>
            )}
          </div>
        </article>

        <article className="panel-premium rounded-2xl p-6">
          <h3 className="text-lg font-semibold">Clientes Recientes</h3>
          <div className="mt-4 space-y-3">
            {clients.slice(0, 8).map((c) => (
              <div key={c.id} className="rounded-xl border border-white/10 p-3">
                <p className="font-medium">{c.name}</p>
                <p className="text-sm text-brand-soft">{c.phone || "Sin telefono"}</p>
                <p className="text-xs text-brand-soft">{c.city || "Sin ciudad"}</p>
              </div>
            ))}
          </div>
        </article>
      </div>
    </section>
  );
}

function StatusAction({ label, onClick, disabled }) {
  return (
    <button
      type="button"
      onClick={onClick}
      disabled={disabled}
      className="rounded-lg border border-white/15 px-2 py-1 text-xs text-brand-soft transition hover:border-brand-mint/60 hover:text-white disabled:cursor-not-allowed disabled:opacity-40"
    >
      {label}
    </button>
  );
}

function Kpi({ label, value }) {
  return (
    <article className="panel-premium rounded-2xl p-4">
      <p className="text-xs uppercase tracking-[0.12em] text-brand-soft">{label}</p>
      <p className="mt-2 text-2xl font-bold">{value}</p>
    </article>
  );
}

function Badge({ estado }) {
  const st = String(estado || "").toUpperCase();
  const cls =
    st === "PENDING"
      ? "bg-amber-500/20 text-amber-300"
      : st === "CONFIRMED"
        ? "bg-blue-500/20 text-blue-300"
        : st === "DELIVERED"
          ? "bg-emerald-500/20 text-emerald-300"
          : "bg-white/10 text-brand-soft";

  return <span className={`rounded-full px-3 py-1 text-xs font-semibold ${cls}`}>{st || "N/A"}</span>;
}

function isToday(iso) {
  if (!iso) return false;
  const now = new Date();
  const d = new Date(iso);
  return d.getDate() === now.getDate() && d.getMonth() === now.getMonth() && d.getFullYear() === now.getFullYear();
}

function formatMoney(n) {
  return Number(n || 0).toLocaleString("es-AR");
}
