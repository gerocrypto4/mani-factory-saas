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
    <section className="space-y-8">
      <div className="commerce-hero">
        <p className="commerce-kicker">Ventas</p>
        <h2>Confirmacion comercial de pedido</h2>
        <p>
          Pedido minimo {formatKg(ORDER_RULES.minOrderKg)}. Cada bolsa es de {formatKg(ORDER_RULES.bagWeightKg)} y cada sabor se vende
          con minimo de {formatKg(ORDER_RULES.minKgPerFlavor)}.
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

      <section className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <div className="commerce-card p-5">
          <h2 className="text-xl font-semibold text-[#1f2937]">Resumen del pedido</h2>
          <div className="mt-4 space-y-3">
            {items.length === 0 && <p className="text-sm text-[#64748b]">No hay productos en el pedido.</p>}
            {items.map((it) => (
              <div key={it.id} className="rounded-xl border border-[#e2e8f0] bg-[#f8fafc] p-3">
                <div className="flex items-center justify-between gap-3">
                  <div className="flex items-center gap-3">
                    <img
                      src={it.imageUrl || resolveProductVisual(it.name).image}
                      alt={it.name}
                      className="h-12 w-12 rounded-lg border border-[#dbe3ea] object-cover"
                      onError={(e) => {
                        e.currentTarget.src = resolveProductVisual(it.name).image;
                      }}
                    />
                    <p className="font-medium text-[#1f2937]">{it.name}</p>
                  </div>
                  <button className="text-xs text-[#b91c1c]" onClick={() => removeItem(it.id)}>
                    Quitar
                  </button>
                </div>
                <div className="mt-2 flex items-center justify-between text-sm">
                  <input
                    type="number"
                    min={ORDER_RULES.minKgPerFlavor}
                    step={ORDER_RULES.minKgPerFlavor}
                    value={it.quantity}
                    onChange={(e) => setQuantity(it.id, Number(e.target.value))}
                    className="w-20 rounded-lg border border-[#cbd5e1] bg-white px-2 py-1"
                  />
                  <p className="font-semibold text-[#b37422]">{formatCurrency(Number(it.price) * it.quantity)}</p>
                </div>
              </div>
            ))}
          </div>
          <div className="mt-4 border-t border-[#e2e8f0] pt-4">
            <p className="text-sm text-[#64748b]">Total estimado</p>
            <p className="text-3xl font-bold text-[#b37422]">{formatCurrency(total)}</p>
            <div className="pedido-progress mt-3">
              <div className="pedido-progress-track">
                <div className="pedido-progress-fill" style={{ width: `${progressPercent}%` }} />
              </div>
              <span>
                {formatKg(totalKg)} / {formatKg(ORDER_RULES.minOrderKg)}
              </span>
            </div>
            <p className="mt-2 text-sm text-[#64748b]">
              {remainingKg > 0 ? `Faltan ${formatKg(remainingKg)} para llegar al minimo de compra.` : "Ya superaste el minimo de compra."}
            </p>
          </div>
        </div>

        <form className="commerce-card p-5" onSubmit={handleSubmit}>
          <h2 className="text-xl font-semibold text-[#1f2937]">Datos comerciales</h2>
          <div className="mt-4 space-y-3">
            <Field label="Nombre" value={form.name} onChange={(value) => setForm({ ...form, name: value })} />
            <Field label="Negocio" value={form.businessName} onChange={(value) => setForm({ ...form, businessName: value })} />
            <Field label="Telefono" value={form.phone} onChange={(value) => setForm({ ...form, phone: value })} />
            <Field label="Ciudad" value={form.city} onChange={(value) => setForm({ ...form, city: value })} />
            <label className="block text-sm">
              <span className="mb-1 block text-[#64748b]">Transporte preferido</span>
              <select
                className="w-full rounded-xl border border-[#cbd5e1] bg-white px-3 py-2"
                value={form.preferredTransport}
                onChange={(e) => setForm({ ...form, preferredTransport: e.target.value })}
              >
                <option>Retiro en fabrica</option>
                <option>Transporte propio</option>
                <option>Logistica asociada</option>
              </select>
            </label>
          </div>
          {error && <p className="mt-3 text-sm text-[#b91c1c]">{error}</p>}
          {!canSubmit && remainingKg > 0 && (
            <p className="mt-3 text-sm text-[#b37422]">
              El pedido minimo es {formatKg(ORDER_RULES.minOrderKg)}. Te faltan {formatKg(remainingKg)} para poder confirmarlo.
            </p>
          )}
          <button disabled={!canSubmit || submitting} className="btn-primary mt-5 w-full disabled:opacity-60">
            {submitting ? "Enviando..." : "Confirmar pedido mayorista"}
          </button>
        </form>
      </section>
    </section>
  );
}

function Field({ label, value, onChange }) {
  return (
    <label className="block text-sm">
      <span className="mb-1 block text-[#64748b]">{label}</span>
      <input
        value={value}
        onChange={(e) => onChange(e.target.value)}
        className="w-full rounded-xl border border-[#cbd5e1] bg-white px-3 py-2"
      />
    </label>
  );
}
