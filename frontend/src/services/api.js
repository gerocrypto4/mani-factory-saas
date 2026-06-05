import axios from "axios";

const baseURL = import.meta.env.VITE_API_URL || "http://localhost:8080";
const tokenKey = "mf_admin_token";

export const api = axios.create({
  baseURL,
  timeout: 12000
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem(tokenKey);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status;
    const url = String(error?.config?.url || "");
    const isLoginRequest = url.includes("/api/v1/auth/login");

    if (status === 401 && !isLoginRequest) {
      clearToken();
      window.dispatchEvent(new CustomEvent("mf:auth-expired"));
      error.code = "AUTH_EXPIRED";
    }

    return Promise.reject(error);
  }
);

export function saveToken(token) {
  localStorage.setItem(tokenKey, token);
}

export function clearToken() {
  localStorage.removeItem(tokenKey);
}

export function hasToken() {
  return Boolean(localStorage.getItem(tokenKey));
}

export async function login(username, password) {
  const res = await api.post("/api/v1/auth/login", { username, password });
  return res.data?.token;
}

export async function fetchProducts(tenantId = 1) {
  const res = await api.get(`/api/v1/public/catalog/products`, { params: { tenantId } });
  return res.data;
}

export async function submitOrder(payload, tenantId = 1) {
  const res = await api.post(`/api/v1/public/orders`, payload, { params: { tenantId } });
  return res.data;
}

export async function fetchDashboardOrders(tenantId = 1) {
  const res = await api.get("/api/v1/orders", { params: { tenantId } });
  return res.data;
}

export async function fetchDashboardClients(tenantId = 1) {
  const res = await api.get("/api/v1/clients", { params: { tenantId } });
  return res.data;
}

export async function fetchDashboardClientsPage(tenantId = 1, q = "", page = 0, size = 6) {
  const res = await api.get("/api/v1/clients/search", {
    params: { tenantId, q, page, size }
  });
  return res.data;
}

export async function fetchDashboardClientOrders(clientId, tenantId = 1, limit = 3) {
  const res = await api.get(`/api/v1/clients/${clientId}/orders`, {
    params: { tenantId, limit }
  });
  return res.data;
}

export async function fetchDashboardOrder(orderId, tenantId = 1) {
  const res = await api.get(`/api/v1/orders/${orderId}`, {
    params: { tenantId }
  });
  return res.data;
}

export async function fetchDashboardProducts(tenantId = 1) {
  const res = await api.get("/api/v1/products", { params: { tenantId } });
  return res.data;
}

export async function updateDashboardOrderStatus(orderId, status, tenantId = 1) {
  const res = await api.put(
    `/api/v1/orders/${orderId}/status`,
    { status },
    { params: { tenantId } }
  );
  return res.data;
}

function buildFinancePeriodParams(tenantId, period) {
  const params = { tenantId };
  if (period?.month) params.month = period.month;
  if (period?.year) params.year = period.year;
  return params;
}

export async function fetchFinanceEntries(tenantId = 1, period = {}) {
  const res = await api.get("/api/v1/finances", {
    params: buildFinancePeriodParams(tenantId, period)
  });
  return res.data;
}

export async function fetchFinanceSummary(tenantId = 1, period = {}) {
  const res = await api.get("/api/v1/finances/summary", {
    params: buildFinancePeriodParams(tenantId, period)
  });
  return res.data;
}

export async function createFinanceEntry(payload, tenantId = 1) {
  const res = await api.post("/api/v1/finances", payload, { params: { tenantId } });
  return res.data;
}

export async function deleteFinanceEntry(entryId, tenantId = 1) {
  const res = await api.delete(`/api/v1/finances/${entryId}`, { params: { tenantId } });
  return res.data;
}

export async function fetchStockEntries(tenantId = 1) {
  const res = await api.get("/api/v1/stock", { params: { tenantId } });
  return res.data;
}

export async function fetchStockSummary(tenantId = 1) {
  const res = await api.get("/api/v1/stock/summary", { params: { tenantId } });
  return res.data;
}

export async function createStockEntry(payload, tenantId = 1) {
  const res = await api.post("/api/v1/stock", payload, { params: { tenantId } });
  return res.data;
}

export async function deleteStockEntry(entryId, tenantId = 1) {
  const res = await api.delete(`/api/v1/stock/${entryId}`, { params: { tenantId } });
  return res.data;
}

export async function fetchProductionBatches(tenantId = 1) {
  const res = await api.get("/api/v1/production", { params: { tenantId } });
  return res.data;
}

export async function createProductionBatch(payload, tenantId = 1) {
  const res = await api.post("/api/v1/production", payload, { params: { tenantId } });
  return res.data;
}

export async function deleteProductionBatch(batchId, tenantId = 1) {
  const res = await api.delete(`/api/v1/production/${batchId}`, { params: { tenantId } });
  return res.data;
}
