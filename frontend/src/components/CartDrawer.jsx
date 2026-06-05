import { useEffect, useId, useRef } from "react";
import { Link } from "react-router-dom";
import { resolveProductVisual } from "../data/productVisuals";
import { useCart } from "../state/use-cart";
import { ORDER_RULES, formatCurrency, formatKg } from "../constants/orderRules";

export default function CartDrawer({ open, onClose }) {
  const { items, total, removeItem, setQuantity, addItem } = useCart();
  const panelRef = useRef(null);
  const closeButtonRef = useRef(null);
  const titleId = useId();
  const descriptionId = useId();
  const totalKg = items.reduce((acc, item) => acc + Number(item.quantity || 0), 0);
  const remainingKg = Math.max(0, ORDER_RULES.minOrderKg - totalKg);
  const progressPercent = Math.min(100, (totalKg / ORDER_RULES.minOrderKg) * 100);

  useEffect(() => {
    if (!open) return undefined;

    const previousOverflow = document.body.style.overflow;
    const previousActive = document.activeElement;
    document.body.style.overflow = "hidden";

    const frame = window.requestAnimationFrame(() => {
      closeButtonRef.current?.focus();
    });

    function onKeyDown(event) {
      if (event.key === "Escape") {
        event.preventDefault();
        onClose();
        return;
      }

      if (event.key !== "Tab" || !panelRef.current) {
        return;
      }

      const focusableElements = panelRef.current.querySelectorAll(
        'button:not([disabled]), [href], input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])'
      );

      if (focusableElements.length === 0) {
        event.preventDefault();
        return;
      }

      const firstElement = focusableElements[0];
      const lastElement = focusableElements[focusableElements.length - 1];
      const activeElement = document.activeElement;

      if (event.shiftKey && activeElement === firstElement) {
        event.preventDefault();
        lastElement.focus();
      } else if (!event.shiftKey && activeElement === lastElement) {
        event.preventDefault();
        firstElement.focus();
      }
    }

    document.addEventListener("keydown", onKeyDown);

    return () => {
      window.cancelAnimationFrame(frame);
      document.removeEventListener("keydown", onKeyDown);
      document.body.style.overflow = previousOverflow;
      if (previousActive && typeof previousActive.focus === "function") {
        previousActive.focus();
      }
    };
  }, [open, onClose]);

  if (!open) return null;

  return (
    <div className="cart-drawer-layer">
      <button
        type="button"
        className="cart-drawer-overlay"
        onClick={onClose}
        aria-label="Cerrar carrito"
        tabIndex={-1}
      />

      <aside
        ref={panelRef}
        className="cart-drawer-panel"
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        aria-describedby={descriptionId}
      >
        <div className="cart-drawer-header">
          <div>
            <p className="cart-drawer-kicker">Carrito mayorista</p>
            <h2 id={titleId} className="cart-drawer-title">
              Revisión de pedido
            </h2>
          </div>

          <button
            ref={closeButtonRef}
            type="button"
            className="cart-drawer-close"
            onClick={onClose}
            aria-label="Cerrar carrito"
          >
            ×
          </button>
        </div>

        <div id={descriptionId} className="cart-drawer-summary">
          <div className="cart-drawer-summary-row">
            <span>Total estimado</span>
            <strong>{formatCurrency(total)}</strong>
          </div>
          <div className="cart-drawer-summary-row">
            <span>Kilos totales</span>
            <strong>{formatKg(totalKg)}</strong>
          </div>
          <div className="cart-drawer-progress" aria-label="Progreso hacia el mínimo de compra">
            <div className="cart-drawer-progress-top">
              <span>Avance a mínimo de compra</span>
              <strong>
                {formatKg(totalKg)} / {formatKg(ORDER_RULES.minOrderKg)}
              </strong>
            </div>
            <div className="cart-drawer-progress-track" aria-hidden="true">
              <div className="cart-drawer-progress-fill" style={{ width: `${progressPercent}%` }} />
            </div>
            <p className="cart-drawer-progress-copy">
              {remainingKg > 0
                ? `Faltan ${formatKg(remainingKg)} para llegar a ${formatKg(ORDER_RULES.minOrderKg)}.`
                : "Ya alcanzaste el mínimo de compra."}
            </p>
          </div>
        </div>

        <div className="cart-drawer-body">
          {items.length === 0 ? (
            <div className="cart-drawer-empty">
              <p className="cart-drawer-empty-title">El carrito está vacío</p>
              <p className="cart-drawer-empty-copy">
                Sumá productos desde el catálogo para verlos acá y seguir el avance del pedido.
              </p>
            </div>
          ) : (
            <ul className="cart-drawer-list">
              {items.map((item) => {
                const quantity = Number(item.quantity || 0);
                const unitPrice = Number(item.price || 0);
                const itemTotal = unitPrice * quantity;
                const visual = resolveProductVisual(item.name);
                const imageSrc = item.imageUrl || visual.image;
                return (
                  <li key={item.id} className="cart-drawer-item">
                    <div className="cart-drawer-item-head">
                      <img
                        src={imageSrc}
                        alt={item.name}
                        className="cart-drawer-item-image"
                        onError={(event) => {
                          event.currentTarget.src = visual.image;
                        }}
                      />

                      <div className="cart-drawer-item-copy">
                        <div>
                          <p className="cart-drawer-item-name">{item.name}</p>
                          <p className="cart-drawer-item-meta">{formatKg(quantity)} · {formatCurrency(itemTotal)}</p>
                        </div>

                        <div className="cart-drawer-item-footer">
                          <span>Minimo {formatKg(ORDER_RULES.minKgPerFlavor)}</span>
                          <strong>{formatCurrency(itemTotal)}</strong>
                        </div>
                      </div>

                      <button
                        type="button"
                        className="cart-drawer-remove"
                        onClick={() => removeItem(item.id)}
                        aria-label={`Eliminar ${item.name} del carrito`}
                      >
                        ×
                      </button>
                    </div>

                    <div className="cart-drawer-item-controls">
                      <button
                        type="button"
                        className="cart-drawer-stepper"
                        onClick={() =>
                          setQuantity(
                            item.id,
                            Math.max(ORDER_RULES.minKgPerFlavor, quantity - ORDER_RULES.minKgPerFlavor)
                          )
                        }
                        aria-label={`Restar ${formatKg(ORDER_RULES.minKgPerFlavor)} de ${item.name}`}
                        disabled={quantity <= ORDER_RULES.minKgPerFlavor}
                      >
                        -
                      </button>

                      <div className="cart-drawer-quantity">
                        <span>Kilos</span>
                        <strong>{formatKg(quantity)}</strong>
                      </div>

                      <button
                        type="button"
                        className="cart-drawer-stepper"
                        onClick={() => addItem(item, ORDER_RULES.minKgPerFlavor)}
                        aria-label={`Sumar ${formatKg(ORDER_RULES.minKgPerFlavor)} a ${item.name}`}
                      >
                        +
                      </button>
                    </div>

                  </li>
                );
              })}
            </ul>
          )}
        </div>

        <div className="cart-drawer-footer">
          <Link
            to="/checkout"
            className="btn-primary cart-drawer-checkout"
            onClick={onClose}
            aria-label="Ir a confirmar compra"
          >
            Confirmar compra
          </Link>
          <button type="button" className="btn-secondary cart-drawer-continue" onClick={onClose}>
            Seguir comprando
          </button>
        </div>
      </aside>
    </div>
  );
}
