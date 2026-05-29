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
