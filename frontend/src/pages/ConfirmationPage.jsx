import { Link } from "react-router-dom";
import { useCart } from "../state/use-cart";

export default function ConfirmationPage() {
  const { lastOrder } = useCart();

  if (!lastOrder) {
    return (
      <section className="panel-premium rounded-3xl p-6 text-center">
        <p className="text-brand-soft">No hay una confirmacion activa.</p>
        <Link className="btn-secondary mt-4 inline-block" to="/">Volver al catalogo</Link>
      </section>
    );
  }

  return (
    <section className="panel-premium rounded-3xl p-6 text-center shadow-glow">
      <p className="text-sm uppercase tracking-[0.22em] text-brand-mint">Pedido recibido</p>
      <h2 className="mt-2 text-3xl font-bold">Orden registrada con exito</h2>
      <p className="mt-3 text-brand-soft">
        Pedido #{lastOrder.order?.orderId || "-"} confirmado para {lastOrder.customer.name}. Nuestro equipo coordina transporte y despacho.
      </p>
      <p className="mt-2 text-xl font-semibold text-brand-gold">${lastOrder.total.toFixed(2)}</p>
      <Link className="btn-primary mt-6 inline-block" to="/">Hacer otro pedido</Link>
    </section>
  );
}
