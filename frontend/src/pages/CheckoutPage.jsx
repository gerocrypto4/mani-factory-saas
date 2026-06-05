import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { submitOrder } from "../services/api";
import { useCart } from "../state/use-cart";
import { resolveProductVisual } from "../data/productVisuals";
import { ORDER_RULES, formatCurrency, formatKg } from "../constants/orderRules";

export default function CheckoutPage() {
  const { items, total, setQuantity, removeItem, clearCart, setLastOrder } = useCart();
  const navigate = useNavigate();
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [form, setForm] = useState({
    name: "",
    businessName: "",
    phone: "",
    city: "",
    preferredTransport: "Retiro en fabrica",
  });

  const totalKg = useMemo(
    () => items.reduce((acc, item) => acc + Number(item.quantity || 0), 0),
    [items]
  );
  const canSubmit = useMemo(() => {
    return items.length > 0 && totalKg >= ORDER_RULES.minOrderKg && form.name.trim() && form.phone.trim() && form.city.trim();
  }, [items, form, totalKg]);
  const remainingKg = Math.max(0, ORDER_RULES.minOrderKg - totalKg);
  const progressPercent = Math.min(100, (totalKg / ORDER_RULES.minOrderKg) * 100);

  async function handleSubmit(e) {
    e.preventDefault();
    if (!canSubmit) return;
    setSubmitting(true);
    setError("");
    try {
      const payload = {
        name: form.name,
        businessName: form.businessName,
        phone: form.phone,
        city: form.city,
        preferredTransport: form.preferredTransport,
        items: items.map((it) => ({ productId: it.id, quantity: it.quantity })),
      };
      const order = await submitOrder(payload, 1);
      setLastOrder({
        order,
        customer: form,
        total,
      });
      clearCart();
      navigate("/confirmacion");
    } catch {
      setError("No pudimos enviar el pedido. Revisa backend y vuelve a intentar.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="pedido-page checkout-page space-y-8">
      <div className="commerce-hero commerce-hero-premium">
        <p className="commerce-kicker">Ventas</p>
        <h2>Confirmá tu compra mayorista</h2>
        <p>
          Revisá el carrito, completá los datos comerciales y cerrá el pedido cuando llegues al mínimo de compra.
        </p>
        <div className="commerce-badges">
          <span>1. Revisar carrito</span>
          <span>2. Completar datos</span>
          <span>3. Confirmar pedido</span>
        </div>
      </div>

      <div className="pedido-rule-strip">
        <div className="pedido-rule-card">
          <strong>{formatKg(ORDER_RULES.minOrderKg)}</strong>
          <span>Pedido minimo</span>
        </div>
        <div className="pedido-rule-card">
          <strong>{formatKg(ORDER_RULES.bagWeightKg)}</strong>
          <span>Bolsa comercial</span>
        </div>
        <div className="pedido-rule-card">
          <strong>{formatKg(ORDER_RULES.minKgPerFlavor)}</strong>
          <span>Minimo por sabor</span>
        </div>
      </div>

      <section className="checkout-grid">
        <div className="commerce-card commerce-card-dark">
          <div className="checkout-card-head">
            <div>
              <p className="checkout-card-kicker">Pedido actual</p>
              <h2 className="checkout-card-title">Resumen del carrito</h2>
            </div>
            <p className="checkout-card-total-label">Total estimado</p>
          </div>

          <div className="checkout-list">
            {items.length === 0 && <p className="checkout-empty">No hay productos en el pedido.</p>}
            {items.map((it) => (
              <article key={it.id} className="checkout-item">
                <div className="checkout-item-top">
                  <div className="checkout-item-media">
                    <img
                      src={it.imageUrl || resolveProductVisual(it.name).image}
                      alt={it.name}
                      className="checkout-item-image"
                      onError={(e) => {
                        e.currentTarget.src = resolveProductVisual(it.name).image;
                      }}
                    />
                    <div>
                      <p className="checkout-item-name">{it.name}</p>
                      <p className="checkout-item-meta">{formatKg(it.quantity)} · {formatCurrency(Number(it.price) * it.quantity)}</p>
                    </div>
                  </div>
                  <button className="checkout-remove" onClick={() => removeItem(it.id)}>
                    Quitar
                  </button>
                </div>

                <div className="checkout-item-bottom">
                  <input
                    type="number"
                    min={ORDER_RULES.minKgPerFlavor}
                    step={ORDER_RULES.minKgPerFlavor}
                    value={it.quantity}
                    onChange={(e) => setQuantity(it.id, Number(e.target.value))}
                    className="checkout-quantity-input"
                  />
                  <p className="checkout-item-price">{formatCurrency(Number(it.price) * it.quantity)}</p>
                </div>
              </article>
            ))}
          </div>

          <div className="checkout-summary">
            <div className="checkout-summary-row">
              <span>Total estimado</span>
              <strong>{formatCurrency(total)}</strong>
            </div>
            <div className="checkout-summary-row">
              <span>Kilos totales</span>
              <strong>{formatKg(totalKg)}</strong>
            </div>
            <div className="pedido-progress mt-3">
              <div className="pedido-progress-track">
                <div className="pedido-progress-fill" style={{ width: `${progressPercent}%` }} />
              </div>
              <span>
                {formatKg(totalKg)} / {formatKg(ORDER_RULES.minOrderKg)}
              </span>
            </div>
            <p className="checkout-note">
              {remainingKg > 0 ? `Faltan ${formatKg(remainingKg)} para llegar al minimo de compra.` : "Ya superaste el minimo de compra."}
            </p>
          </div>
        </div>

        <form className="commerce-card commerce-card-dark" onSubmit={handleSubmit}>
          <div className="checkout-card-head">
            <div>
              <p className="checkout-card-kicker">Datos comerciales</p>
              <h2 className="checkout-card-title">Confirmación del pedido</h2>
            </div>
          </div>

          <div className="checkout-form">
            <Field label="Nombre" value={form.name} onChange={(value) => setForm({ ...form, name: value })} />
            <Field label="Negocio" value={form.businessName} onChange={(value) => setForm({ ...form, businessName: value })} />
            <Field label="Teléfono" value={form.phone} onChange={(value) => setForm({ ...form, phone: value })} />
            <Field label="Ciudad" value={form.city} onChange={(value) => setForm({ ...form, city: value })} />
            <label className="checkout-field checkout-field-select">
              <span className="checkout-field-label">Transporte preferido</span>
              <select
                className="checkout-select"
                value={form.preferredTransport}
                onChange={(e) => setForm({ ...form, preferredTransport: e.target.value })}
              >
                <option>Retiro en fábrica</option>
                <option>Transporte propio</option>
                <option>Logistica asociada</option>
              </select>
            </label>
          </div>

          {error && <p className="checkout-error">{error}</p>}
          {!canSubmit && remainingKg > 0 && (
            <p className="mt-3 text-sm text-[#f4c66c]">
              El pedido minimo es {formatKg(ORDER_RULES.minOrderKg)}. Te faltan {formatKg(remainingKg)} para poder confirmarlo.
            </p>
          )}
          <button disabled={!canSubmit || submitting} className="btn-primary checkout-submit disabled:opacity-60">
            {submitting ? "Enviando..." : "Confirmar pedido mayorista"}
          </button>
        </form>
      </section>
    </section>
  );
}

function Field({ label, value, onChange }) {
  return (
    <label className="checkout-field">
      <span className="checkout-field-label">{label}</span>
      <input value={value} onChange={(e) => onChange(e.target.value)} />
    </label>
  );
}
