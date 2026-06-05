import {
  formatKg,
  formatMoney,
  formatShortDate,
  getProductName,
  getStatusLabel,
  useDashboard
} from "../../state/dashboard-context";

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

function InfoBox({ label, value }) {
  return (
    <div className="dashboard-info-box">
      <p className="dashboard-info-label">{label}</p>
      <div className="dashboard-info-value">{value}</div>
    </div>
  );
}

export default function DashboardOrderModal() {
  const { selectedOrder, selectedOrderLoading, closeOrderDetail, productLookup } = useDashboard();

  if (!selectedOrderLoading && !selectedOrder) return null;

  return (
    <div className="dashboard-modal-overlay" onClick={closeOrderDetail}>
      <div
        className="dashboard-modal"
        role="dialog"
        aria-modal="true"
        aria-label="Detalle del pedido"
        onClick={(event) => event.stopPropagation()}
      >
        <div className="dashboard-modal-head">
          <div>
            <p className="dashboard-section-label">Pedido completo</p>
            <h3 className="dashboard-modal-title">
              {selectedOrder?.id ? `#${selectedOrder.id}` : "Cargando pedido..."}
            </h3>
          </div>
          <button type="button" className="dashboard-close-btn" onClick={closeOrderDetail}>
            Cerrar
          </button>
        </div>

        {selectedOrderLoading ? (
          <div className="dashboard-empty mt-6">Cargando detalle del pedido...</div>
        ) : selectedOrder ? (
          <>
            <div className="dashboard-modal-grid">
              <InfoBox label="Estado" value={<Badge estado={selectedOrder.status} />} />
              <InfoBox label="Total" value={`$${formatMoney(Number(selectedOrder.total || 0))}`} />
              <InfoBox label="Creado" value={formatShortDate(selectedOrder.createdAt)} />
              <InfoBox label="Cliente" value={`#${selectedOrder.clientId || "-"}`} />
            </div>

            <div className="dashboard-modal-items">
              <p className="dashboard-section-label">Productos del pedido</p>
              {selectedOrder.items?.length ? (
                selectedOrder.items.map((item) => (
                  <div key={`${selectedOrder.id}-${item.productId}`} className="dashboard-modal-item">
                    <div>
                      <p className="font-medium">{getProductName(productLookup, item.productId)}</p>
                      <p className="dashboard-muted text-xs">
                        {formatKg(item.quantity)} · ${formatMoney(Number(item.unitPrice || 0))} c/u
                      </p>
                    </div>
                    <p className="dashboard-accent">${formatMoney(Number(item.totalPrice || 0))}</p>
                  </div>
                ))
              ) : (
                <div className="dashboard-empty">No hay productos para mostrar.</div>
              )}
            </div>
          </>
        ) : null}
      </div>
    </div>
  );
}
