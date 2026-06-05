import { useEffect, useMemo, useRef, useState } from "react";
import DashboardSectionHero from "../components/dashboard/DashboardSectionHero";
import {
  STATUS_FILTERS,
  formatKg,
  formatMoney,
  formatShortDate,
  getStatusFilterLabel,
  getStatusLabel,
  useDashboard
} from "../state/dashboard-context";

export default function PedidosPage() {
  const { orders, changeOrderStatus, openOrderDetail, updatingOrderId } = useDashboard();
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [statusMenuOpen, setStatusMenuOpen] = useState(false);
  const [stockError, setStockError] = useState(null);
  const statusMenuRef = useRef(null);

  useEffect(() => {
    if (!statusMenuOpen) return undefined;

    function onPointerDown(event) {
      if (statusMenuRef.current && !statusMenuRef.current.contains(event.target)) {
        setStatusMenuOpen(false);
      }
    }

    function onKeyDown(event) {
      if (event.key === "Escape") {
        setStatusMenuOpen(false);
      }
    }

    document.addEventListener("mousedown", onPointerDown);
    document.addEventListener("keydown", onKeyDown);

    return () => {
      document.removeEventListener("mousedown", onPointerDown);
      document.removeEventListener("keydown", onKeyDown);
    };
  }, [statusMenuOpen]);

  async function handleConfirmOrder(orderId) {
    setStockError(null);
    const result = await changeOrderStatus(orderId, "CONFIRMED");
    if (result && !result.ok && result.stockShortages) {
      setStockError({ orderId, shortages: result.stockShortages });
    }
  }

  const filteredOrders = useMemo(() => {
    if (statusFilter === "ALL") return orders;
    return orders.filter((order) => String(order.status || "").toUpperCase() === statusFilter);
  }, [orders, statusFilter]);

  return (
    <>
      <DashboardSectionHero
        kicker="Pedidos"
        title="Gestión de pedidos"
        copy="Filtrá por estado, actualizá el flujo y resolvé la operación diaria con una pantalla dedicada."
      />

      <article className="dashboard-panel dashboard-panel-wide dashboard-section">
        <div className="dashboard-panel-head dashboard-panel-head-orders">
          <div className="dashboard-panel-head-copy">
            <h3 className="dashboard-panel-title">Pedidos recientes</h3>
            <p className="dashboard-muted">{filteredOrders.length} pedido{filteredOrders.length !== 1 ? "s" : ""} · Filtrá por estado y seguí el flujo operativo.</p>
          </div>

          <div className="dashboard-filter-group" ref={statusMenuRef}>
            <span className="dashboard-filter-label">Estado</span>
            <button
              type="button"
              className={`dashboard-status-select dashboard-status-select-${statusFilter.toLowerCase()}`}
              onClick={() => setStatusMenuOpen((prev) => !prev)}
              aria-haspopup="listbox"
              aria-expanded={statusMenuOpen}
            >
              <span
                className={`dashboard-status-dot dashboard-status-dot-${statusFilter.toLowerCase()}`}
                aria-hidden="true"
              />
              <span>{getStatusFilterLabel(statusFilter)}</span>
              <span className="dashboard-status-caret" aria-hidden="true">
                v
              </span>
            </button>

            {statusMenuOpen && (
              <div className="dashboard-status-menu" role="listbox" aria-label="Filtros de pedidos">
                {STATUS_FILTERS.map((option) => {
                  const active = statusFilter === option.value;
                  return (
                    <button
                      key={option.value}
                      type="button"
                      className={`dashboard-status-menu-item dashboard-status-menu-item-${option.value.toLowerCase()} ${
                        active ? "dashboard-status-menu-item-active" : ""
                      }`}
                      onClick={() => {
                        setStatusFilter(option.value);
                        setStatusMenuOpen(false);
                      }}
                      role="option"
                      aria-selected={active}
                    >
                      <span
                        className={`dashboard-status-dot dashboard-status-dot-${option.value.toLowerCase()}`}
                        aria-hidden="true"
                      />
                      <span>{option.label}</span>
                    </button>
                  );
                })}
              </div>
            )}
          </div>
        </div>

        {stockError && (
          <div className="dashboard-alert dashboard-alert-error" style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", gap: "1rem" }}>
            <div>
              <strong>Sin stock suficiente para confirmar el Pedido #{stockError.orderId}</strong>
              <ul style={{ margin: "8px 0 0", paddingLeft: "1.2rem" }}>
                {stockError.shortages.map((s) => (
                  <li key={s.productId}>
                    <strong>{s.productName}</strong> — necesitás {formatKg(s.required)}, disponibles {formatKg(s.available)}
                  </li>
                ))}
              </ul>
            </div>
            <button type="button" className="dashboard-history-link" onClick={() => setStockError(null)}>
              Cerrar
            </button>
          </div>
        )}

        <div className="dashboard-list">
          {filteredOrders.map((order) => (
            <div key={order.id} className="dashboard-order-card">
              <div className="dashboard-order-meta">
                <p className="dashboard-order-id">Pedido #{order.id}</p>
                <p className="dashboard-muted">{formatShortDate(order.createdAt)}</p>
              </div>

              <div className="dashboard-order-side">
                <div className="dashboard-order-topline">
                  <Badge estado={order.status} />
                  <p className="dashboard-order-total">${formatMoney(Number(order.total || 0))}</p>
                </div>

                <div className="dashboard-actions-row">
                  <StatusAction
                    label="Confirmar"
                    variant="confirm"
                    onClick={() => handleConfirmOrder(order.id)}
                    disabled={updatingOrderId === order.id || String(order.status).toUpperCase() === "CONFIRMED"}
                  />
                  <StatusAction
                    label="Despachar"
                    variant="dispatch"
                    onClick={() => changeOrderStatus(order.id, "SHIPPED")}
                    disabled={updatingOrderId === order.id || String(order.status).toUpperCase() === "SHIPPED"}
                  />
                  <StatusAction
                    label="Entregar"
                    variant="deliver"
                    onClick={() => changeOrderStatus(order.id, "DELIVERED")}
                    disabled={updatingOrderId === order.id || String(order.status).toUpperCase() === "DELIVERED"}
                  />
                  <StatusAction
                    label="Cancelar"
                    variant="cancel"
                    onClick={() => {
                      const ok = window.confirm(`Vas a cancelar el pedido #${order.id}. Querés continuar?`);
                      if (!ok) return;
                      changeOrderStatus(order.id, "CANCELLED");
                    }}
                    disabled={updatingOrderId === order.id || String(order.status).toUpperCase() === "CANCELLED"}
                  />
                </div>

                <button type="button" className="dashboard-history-link" onClick={() => openOrderDetail(order.id)}>
                  Ver detalle completo
                </button>
              </div>
            </div>
          ))}

          {filteredOrders.length === 0 && (
            <div className="dashboard-empty">No hay pedidos para el filtro seleccionado.</div>
          )}
        </div>
      </article>
    </>
  );
}

function StatusAction({ label, onClick, disabled, variant = "default" }) {
  return (
    <button
      type="button"
      onClick={onClick}
      disabled={disabled}
      className={`dashboard-action-btn dashboard-action-btn-${variant}`}
    >
      {label}
    </button>
  );
}

function Badge({ estado }) {
  const status = String(estado || "").toUpperCase();
  const label = getStatusLabel(status);
  const cls =
    status === "PENDING"
      ? "dashboard-badge-pending"
      : status === "CONFIRMED"
        ? "dashboard-badge-confirmed"
        : status === "SHIPPED"
          ? "dashboard-badge-shipped"
          : status === "DELIVERED"
            ? "dashboard-badge-delivered"
            : status === "CANCELLED"
              ? "dashboard-badge-cancelled"
              : "dashboard-badge-neutral";

  return <span className={`dashboard-badge ${cls}`}>{label}</span>;
}
