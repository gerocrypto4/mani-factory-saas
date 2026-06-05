import DashboardSectionHero from "../components/dashboard/DashboardSectionHero";
import {
  formatKg,
  formatMoney,
  formatShortDate,
  getStatusLabel,
  useDashboard
} from "../state/dashboard-context";

export default function ClientesPage() {
  const {
    clientsLoading,
    clientsPage,
    clientQuery,
    setClientQuery,
    setClientsPage,
    clientHistories,
    toggleClientHistory,
    openOrderDetail
  } = useDashboard();

  return (
    <>
      <DashboardSectionHero
        kicker="Clientes"
        title="Gestión de clientes"
        copy="Buscá clientes, revisá historial y seguí el último pedido sin salir de una página dedicada."
      />

      <article className="dashboard-panel dashboard-section">
        <div className="dashboard-panel-head">
          <div>
            <h3 className="dashboard-panel-title">Base comercial</h3>
            <p className="dashboard-muted">Búsqueda, listado, historial y acceso al pedido completo.</p>
          </div>
          <span className="dashboard-pill">{clientsPage.totalElements} total</span>
        </div>

        <div className="dashboard-stack">
          <div className="dashboard-search-row">
            <label className="flex-1">
              <span className="dashboard-field-label">Buscar</span>
              <input
                value={clientQuery}
                onChange={(event) => {
                  setClientQuery(event.target.value);
                  setClientsPage((prev) => ({ ...prev, page: 0 }));
                }}
                className="dashboard-input"
                placeholder="Nombre, teléfono, email, ciudad..."
              />
            </label>
            <div className="dashboard-search-actions">
              <button
                type="button"
                className="btn-secondary h-11"
                onClick={() => {
                  setClientQuery("");
                  setClientsPage((prev) => ({ ...prev, page: 0 }));
                }}
                disabled={!clientQuery.trim()}
              >
                Limpiar
              </button>
            </div>
          </div>

          {clientsLoading ? (
            <div className="dashboard-empty">Cargando clientes...</div>
          ) : clientsPage.content.length === 0 ? (
            <div className="dashboard-empty">No hay clientes para ese criterio de búsqueda.</div>
          ) : (
            <div className="dashboard-client-list">
              {clientsPage.content.map((client) => (
                <div key={client.id} className="dashboard-client-card">
                  <div className="dashboard-client-head">
                    <div>
                      <p className="dashboard-client-name">{client.name}</p>
                      <p className="dashboard-muted">{client.businessName || "Sin razón social"}</p>
                    </div>
                    <div className="dashboard-client-meta">
                      <span className="dashboard-chip">#{client.id}</span>
                      <button
                        type="button"
                        className="dashboard-history-link"
                        onClick={() => toggleClientHistory(client.id)}
                      >
                        {clientHistories[client.id]?.open ? "Ocultar historial" : "Ver historial"}
                      </button>
                    </div>
                  </div>

                  <div className="dashboard-client-grid">
                    <span>{client.phone || "Sin teléfono"}</span>
                    <span>{client.email || "Sin email"}</span>
                    <span>{client.city || "Sin ciudad"}</span>
                    <span>{client.preferredTransport || "Sin transporte"}</span>
                  </div>

                  {clientHistories[client.id]?.open && (
                    <div className="dashboard-history-box">
                      {clientHistories[client.id]?.loading ? (
                        <p className="dashboard-muted">Cargando historial...</p>
                      ) : clientHistories[client.id]?.error ? (
                        <p className="dashboard-error">{clientHistories[client.id].error}</p>
                      ) : (
                        <>
                          <div className="dashboard-history-head">
                            <div>
                              <p className="dashboard-section-label">Último pedido</p>
                              <p className="dashboard-history-id">
                                {clientHistories[client.id]?.data?.latestOrder
                                  ? `#${clientHistories[client.id].data.latestOrder.id}`
                                  : "Sin pedidos"}
                              </p>
                            </div>
                            <span className="dashboard-chip">
                              {clientHistories[client.id]?.data?.totalOrders || 0} pedidos
                            </span>
                          </div>

                          {clientHistories[client.id]?.data?.latestOrder ? (
                            <div className="dashboard-history-summary">
                              <div className="dashboard-history-summary-grid">
                                <span>{formatShortDate(clientHistories[client.id].data.latestOrder.createdAt)}</span>
                                <span className="text-right">
                                  <Badge estado={clientHistories[client.id].data.latestOrder.status} />
                                </span>
                                <span className="dashboard-accent">
                                  ${formatMoney(Number(clientHistories[client.id].data.latestOrder.total || 0))}
                                </span>
                                <span className="text-right">
                                  {clientHistories[client.id].data.latestOrder.totalItems || 0} productos
                                </span>
                                <span className="col-span-2">
                                  {formatKg(clientHistories[client.id].data.latestOrder.totalKg || 0)} totales
                                </span>
                              </div>
                              <button
                                type="button"
                                className="dashboard-history-link mt-3"
                                onClick={() => openOrderDetail(clientHistories[client.id].data.latestOrder.id)}
                              >
                                Ver pedido completo
                              </button>
                            </div>
                          ) : null}

                          {clientHistories[client.id]?.data?.recentOrders?.length > 0 ? (
                            <div className="dashboard-recent-list">
                              <p className="dashboard-section-label">Historial reciente</p>
                              {clientHistories[client.id].data.recentOrders.map((order) => (
                                <div key={order.id} className="dashboard-recent-item">
                                  <div>
                                    <p className="dashboard-order-id">#{order.id}</p>
                                    <p className="dashboard-muted">{formatShortDate(order.createdAt)}</p>
                                  </div>
                                  <div className="text-right">
                                    <Badge estado={order.status} />
                                    <p className="dashboard-accent mt-1">${formatMoney(Number(order.total || 0))}</p>
                                  </div>
                                </div>
                              ))}
                            </div>
                          ) : null}
                        </>
                      )}
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}

          <div className="dashboard-pagination">
            <p className="dashboard-muted">
              Página {clientsPage.page + 1} de {Math.max(1, clientsPage.totalPages)}
            </p>
            <div className="dashboard-pagination-actions">
              <button
                type="button"
                className="btn-secondary px-3 py-2 text-sm"
                onClick={() => setClientsPage((prev) => ({ ...prev, page: Math.max(0, prev.page - 1) }))}
                disabled={!clientsPage.hasPrevious || clientsLoading}
              >
                Anterior
              </button>
              <button
                type="button"
                className="btn-secondary px-3 py-2 text-sm"
                onClick={() => setClientsPage((prev) => ({ ...prev, page: prev.page + 1 }))}
                disabled={!clientsPage.hasNext || clientsLoading}
              >
                Siguiente
              </button>
            </div>
          </div>
        </div>
      </article>
    </>
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
