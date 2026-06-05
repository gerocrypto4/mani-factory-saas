import { createContext, useContext, useEffect, useMemo, useRef, useState } from "react";
import {
  clearToken,
  fetchDashboardClientOrders,
  fetchDashboardClients,
  fetchDashboardClientsPage,
  fetchDashboardOrder,
  fetchDashboardOrders,
  fetchDashboardProducts,
  hasToken,
  login,
  saveToken,
  updateDashboardOrderStatus
} from "../services/api";

const DashboardContext = createContext(null);
const tenantId = 1;

export function DashboardProvider({ children }) {
  const [auth, setAuth] = useState({ username: "", password: "" });
  const [authed, setAuthed] = useState(hasToken());
  const [loading, setLoading] = useState(false);
  const [clientsLoading, setClientsLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [updatingOrderId, setUpdatingOrderId] = useState(null);
  const [orders, setOrders] = useState([]);
  const [clientsTotal, setClientsTotal] = useState(0);
  const [clientsPage, setClientsPage] = useState({
    content: [],
    page: 0,
    size: 6,
    totalElements: 0,
    totalPages: 0,
    hasNext: false,
    hasPrevious: false,
    query: ""
  });
  const [clientQuery, setClientQuery] = useState("");
  const [clientHistories, setClientHistories] = useState({});
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [selectedOrderLoading, setSelectedOrderLoading] = useState(false);
  const [products, setProducts] = useState([]);
  const authExpiredRef = useRef(false);

  useEffect(() => {
    if (!authed) return;
    loadDashboard();
    // Clients load through their own query/page effect below.
  }, [authed]);

  useEffect(() => {
    function onAuthExpired() {
      authExpiredRef.current = true;
      logout(true);
      setError("Sesión vencida. Volvé a iniciar sesión.");
    }

    window.addEventListener("mf:auth-expired", onAuthExpired);
    return () => window.removeEventListener("mf:auth-expired", onAuthExpired);
  }, []);

  useEffect(() => {
    if (!authed) return;
    loadClients(clientQuery, clientsPage.page, clientsPage.size);
  }, [authed, clientQuery, clientsPage.page, clientsPage.size]);

  async function doLogin(event) {
    event.preventDefault();
    try {
      setLoading(true);
      setError("");
      setSuccess("");
      const token = await login(auth.username, auth.password);
      saveToken(token);
      authExpiredRef.current = false;
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
      const [dashboardOrders, dashboardProducts, dashboardClients] = await Promise.all([
        fetchDashboardOrders(tenantId),
        fetchDashboardProducts(tenantId),
        fetchDashboardClients(tenantId)
      ]);
      setOrders(dashboardOrders || []);
      setProducts(dashboardProducts || []);
      setClientsTotal(Array.isArray(dashboardClients) ? dashboardClients.length : 0);
    } catch {
      if (authExpiredRef.current) return;
      setError("No pudimos cargar datos del dashboard.");
    } finally {
      setLoading(false);
    }
  }

  async function loadClients(query = "", page = 0, size = clientsPage.size, silent = false) {
    try {
      if (!silent) setClientsLoading(true);
      const result = await fetchDashboardClientsPage(tenantId, query, page, size);
      setClientsPage({
        content: result?.content || [],
        page: result?.page ?? page,
        size: result?.size ?? size,
        totalElements: result?.totalElements ?? 0,
        totalPages: result?.totalPages ?? 0,
        hasNext: Boolean(result?.hasNext),
        hasPrevious: Boolean(result?.hasPrevious),
        query: result?.query || ""
      });
    } catch {
      if (authExpiredRef.current) return;
      setError("No pudimos cargar clientes.");
    } finally {
      if (!silent) setClientsLoading(false);
    }
  }

  async function refreshDashboard() {
    await Promise.all([
      loadDashboard(),
      loadClients(clientQuery, clientsPage.page, clientsPage.size, true)
    ]);
  }

  async function changeOrderStatus(orderId, nextStatus) {
    try {
      setUpdatingOrderId(orderId);
      setError("");
      setSuccess("");
      await updateDashboardOrderStatus(orderId, nextStatus, tenantId);
      setOrders((prev) =>
        prev.map((order) => (order.id === orderId ? { ...order, status: nextStatus } : order))
      );
      setSuccess(`Pedido #${orderId} actualizado a ${getStatusLabel(nextStatus)}.`);
      return { ok: true };
    } catch (err) {
      if (authExpiredRef.current) return { ok: false };
      if (err?.response?.status === 409 && err?.response?.data?.shortages) {
        return { ok: false, stockShortages: err.response.data.shortages };
      }
      setError(`No pudimos actualizar el estado del pedido #${orderId}.`);
      return { ok: false };
    } finally {
      setUpdatingOrderId(null);
    }
  }

  async function openOrderDetail(orderId) {
    try {
      setSelectedOrderLoading(true);
      setSelectedOrder(null);
      const order = await fetchDashboardOrder(orderId, tenantId);
      setSelectedOrder(order);
    } catch {
      if (authExpiredRef.current) return;
      setError("No pudimos cargar el detalle del pedido.");
    } finally {
      setSelectedOrderLoading(false);
    }
  }

  function closeOrderDetail() {
    setSelectedOrder(null);
    setSelectedOrderLoading(false);
  }

  async function toggleClientHistory(clientId) {
    const current = clientHistories[clientId];
    if (current?.open) {
      setClientHistories((prev) => ({
        ...prev,
        [clientId]: { ...prev[clientId], open: false }
      }));
      return;
    }

    if (current?.data) {
      setClientHistories((prev) => ({
        ...prev,
        [clientId]: { ...prev[clientId], open: true, error: "" }
      }));
      return;
    }

    await loadClientHistory(clientId);
  }

  async function loadClientHistory(clientId, limit = 3) {
    try {
      setClientHistories((prev) => ({
        ...prev,
        [clientId]: {
          ...(prev[clientId] || {}),
          loading: true,
          open: true,
          error: ""
        }
      }));
      const result = await fetchDashboardClientOrders(clientId, tenantId, limit);
      setClientHistories((prev) => ({
        ...prev,
        [clientId]: {
          loading: false,
          open: true,
          error: "",
          data: result
        }
      }));
    } catch {
      if (authExpiredRef.current) return;
      setClientHistories((prev) => ({
        ...prev,
        [clientId]: {
          ...(prev[clientId] || {}),
          loading: false,
          open: true,
          error: "No pudimos cargar el historial."
        }
      }));
    }
  }

  function logout(preserveAuthExpired = false) {
    clearToken();
    if (!preserveAuthExpired) {
      authExpiredRef.current = false;
    }
    setAuthed(false);
    setOrders([]);
    setClientsTotal(0);
    setClientsPage({
      content: [],
      page: 0,
      size: 6,
      totalElements: 0,
      totalPages: 0,
      hasNext: false,
      hasPrevious: false,
      query: ""
    });
    setClientQuery("");
    setClientHistories({});
    setSelectedOrder(null);
    setSelectedOrderLoading(false);
    setProducts([]);
    setError("");
    setSuccess("");
  }

  const kpis = useMemo(() => {
    const now = new Date();
    const monthOrders = orders.filter((order) => {
      if (!order.createdAt) return false;
      const createdAt = new Date(order.createdAt);
      return createdAt.getMonth() === now.getMonth() && createdAt.getFullYear() === now.getFullYear();
    });
    const ventasMes = monthOrders.reduce((acc, order) => acc + Number(order.total || 0), 0);
    const pendientes = orders.filter((order) => String(order.status).toUpperCase() === "PENDING").length;

    return {
      pedidosHoy: orders.filter((order) => isToday(order.createdAt)).length,
      ventasMes,
      clientesActivos: clientsTotal,
      pedidosPendientes: pendientes,
      productosActivos: products.filter((product) => product.active).length
    };
  }, [orders, clientsTotal, products]);

  const productLookup = useMemo(
    () => new Map(products.map((product) => [Number(product.id), product])),
    [products]
  );

  const value = {
    auth,
    setAuth,
    authed,
    loading,
    clientsLoading,
    error,
    success,
    orders,
    updatingOrderId,
    kpis,
    productLookup,
    clientsTotal,
    clientsPage,
    setClientsPage,
    clientQuery,
    setClientQuery,
    clientHistories,
    selectedOrder,
    selectedOrderLoading,
    doLogin,
    refreshDashboard,
    changeOrderStatus,
    openOrderDetail,
    closeOrderDetail,
    toggleClientHistory,
    logout
  };

  return <DashboardContext.Provider value={value}>{children}</DashboardContext.Provider>;
}

export function useDashboard() {
  const context = useContext(DashboardContext);
  if (!context) {
    throw new Error("useDashboard must be used inside a DashboardProvider.");
  }
  return context;
}

export function getStatusLabel(status) {
  if (status === "PENDING") return "Pendiente";
  if (status === "CONFIRMED") return "Confirmado";
  if (status === "SHIPPED") return "Despachado";
  if (status === "DELIVERED") return "Entregado";
  if (status === "CANCELLED") return "Cancelado";
  return "Sin estado";
}

export function getStatusFilterLabel(status) {
  const option = STATUS_FILTERS.find((item) => item.value === status);
  return option?.label || "Todos";
}

export const STATUS_FILTERS = [
  { value: "ALL", label: "Todos" },
  { value: "PENDING", label: "Pendientes" },
  { value: "CONFIRMED", label: "Confirmados" },
  { value: "SHIPPED", label: "Despachados" },
  { value: "DELIVERED", label: "Entregados" },
  { value: "CANCELLED", label: "Cancelados" }
];

export function isToday(iso) {
  if (!iso) return false;
  const now = new Date();
  const createdAt = new Date(iso);
  return (
    createdAt.getDate() === now.getDate() &&
    createdAt.getMonth() === now.getMonth() &&
    createdAt.getFullYear() === now.getFullYear()
  );
}

export function formatMoney(value) {
  return Number(value || 0).toLocaleString("es-AR");
}

export function formatKg(value) {
  return `${Number(value || 0).toLocaleString("es-AR", { maximumFractionDigits: 0 })} kg`;
}

export function formatShortDate(iso) {
  if (!iso) return "-";
  return new Date(iso).toLocaleString("es-AR", {
    day: "2-digit",
    month: "2-digit",
    hour: "2-digit",
    minute: "2-digit"
  });
}

export function getProductName(productLookup, productId) {
  const product = productLookup.get(Number(productId));
  return product?.name || `Producto #${productId}`;
}
