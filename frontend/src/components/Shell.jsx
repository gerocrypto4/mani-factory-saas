import { useEffect, useState } from "react";
import { Link, useLocation } from "react-router-dom";
import CartDrawer from "./CartDrawer";
import { useCart } from "../state/use-cart";
import { ORDER_RULES, formatKg } from "../constants/orderRules";

export default function Shell({ children }) {
  const { items } = useCart();
  const location = useLocation();
  const dashboard = location.pathname.startsWith("/dashboard");
  const showFloatingWhatsapp = !dashboard && location.pathname !== "/";
  const [scrolled, setScrolled] = useState(false);
  const [isCartOpen, setIsCartOpen] = useState(false);
  const isLanding = !dashboard;
  const showHeaderMinimum = location.pathname === "/pedido";
  const totalKg = items.reduce((acc, item) => acc + Number(item.quantity || 0), 0);
  const remainingKg = Math.max(0, ORDER_RULES.minOrderKg - totalKg);
  const progressPercent = Math.min(100, (totalKg / ORDER_RULES.minOrderKg) * 100);

  useEffect(() => {
    function onScroll() {
      setScrolled(window.scrollY > 12);
    }

    onScroll();
    window.addEventListener("scroll", onScroll, { passive: true });
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

  useEffect(() => {
    setIsCartOpen(false);
  }, [location.pathname]);

  return (
    <div className={`min-h-screen ${isLanding ? "landing-wrap" : ""}`}>
      <header className={`brand-header sticky top-0 z-40 transition-all ${scrolled ? "header-scrolled" : ""}`}>
        <div className={`${dashboard ? "mx-auto max-w-7xl px-4 py-4 md:px-8 xl:px-10" : "mx-auto max-w-[1600px] px-4 py-4 md:px-8 xl:px-10"}`}>
          <div className="cj-shell-bar">
            <span>Canal Mayorista</span>
            <span>Industria Argentina · Desde 1966</span>
          </div>

          <div className={`${dashboard ? "cj-shell-grid cj-shell-grid-dashboard" : "cj-shell-grid"}`}>
	            {dashboard ? (
	              <>
	                <div className="cj-shell-brand cj-shell-brand-dashboard">
	                  <p className="cj-shell-kicker">Factory Operations</p>
	                  <h1 className="cj-shell-title">
                    <span className="cj-brand-wordmark cj-brand-wordmark-header">
                      CRIS<span className="cj-brand-wordmark-separator">-</span>JOR
                    </span>
	                  </h1>
	                </div>

	                <nav className="cj-shell-nav cj-shell-nav-dashboard" aria-label="Secciones del dashboard">
	                  <Link
	                    className={location.pathname === "/dashboard/resumen" ? "nav-link-active" : "nav-link"}
	                    to="/dashboard/resumen"
	                  >
	                    Resumen
	                  </Link>
	                  <Link
	                    className={location.pathname === "/dashboard/pedidos" ? "nav-link-active" : "nav-link"}
	                    to="/dashboard/pedidos"
	                  >
	                    Pedidos
	                  </Link>
	                  <Link
	                    className={location.pathname === "/dashboard/clientes" ? "nav-link-active" : "nav-link"}
	                    to="/dashboard/clientes"
	                  >
	                    Clientes
	                  </Link>
	                  <Link
	                    className={location.pathname === "/dashboard/finanzas" ? "nav-link-active" : "nav-link"}
	                    to="/dashboard/finanzas"
	                  >
	                    Finanzas
	                  </Link>
	                  <Link
	                    className={location.pathname === "/dashboard/stock" ? "nav-link-active" : "nav-link"}
	                    to="/dashboard/stock"
	                  >
	                    Stock
	                  </Link>
	                  <Link
	                    className={location.pathname === "/dashboard/produccion" ? "nav-link-active" : "nav-link"}
	                    to="/dashboard/produccion"
	                  >
	                    Producción
	                  </Link>
	                </nav>

	                <div className="cj-shell-dashboard-actions">
	                  <Link className="nav-link nav-link-ghost" to="/pedido">
	                    Ver tienda
	                  </Link>
	                </div>
	              </>
	            ) : (
              <>
                <nav className="cj-shell-nav cj-shell-nav-left" aria-label="Navegacion de productos">
                  <Link className={location.pathname === "/pedido" ? "nav-link-active" : "nav-link"} to="/pedido">
                    Productos
                  </Link>
                  <Link className={location.pathname === "/checkout" ? "nav-link-active" : "nav-link"} to="/checkout">
                    Ventas
                  </Link>
                </nav>

                <div className="cj-shell-brand">
                  <Link to="/" className="logo-mark">
                    <span className="cj-brand-wordmark cj-brand-wordmark-logo">
                      CRIS<span className="cj-brand-wordmark-separator">-</span>JOR
                    </span>
                  </Link>
                  <span className="cj-shell-brandline">Mani saborizado mayorista</span>
                </div>

                <nav className="cj-shell-nav cj-shell-nav-right" aria-label="Navegacion comercial">
                  <a className="nav-link" href="/#nosotros">
                    Nosotros
                  </a>
                  <a className="nav-link" href="/#contacto">
                    Contactenos
                  </a>
                  {showHeaderMinimum && (
                    <div className="header-minimum" aria-label="Progreso de compra minima">
                      <span className="header-minimum-label">Minimo</span>
                      <strong>
                        {formatKg(totalKg)} / {formatKg(ORDER_RULES.minOrderKg)}
                      </strong>
                      <span>{remainingKg > 0 ? `Faltan ${formatKg(remainingKg)}` : "Listo para cerrar"}</span>
                      <div className="header-minimum-track" aria-hidden="true">
                        <div className="header-minimum-fill" style={{ width: `${progressPercent}%` }} />
                      </div>
                    </div>
                  )}
                  <button
                    type="button"
                    className="cart-link"
                    onClick={() => setIsCartOpen(true)}
                    aria-label={`Abrir carrito con ${items.length} productos`}
                    aria-haspopup="dialog"
                    aria-expanded={isCartOpen}
                  >
                    <span className="cart-icon">🛒</span>
                    <span className="cart-count">{items.length}</span>
                  </button>
                </nav>
              </>
            )}
          </div>
        </div>
      </header>

      <main className={dashboard ? "dashboard-main" : ""}>{children}</main>

      <CartDrawer open={isCartOpen} onClose={() => setIsCartOpen(false)} />

      {!dashboard && showFloatingWhatsapp && (
        <a
          href="https://wa.me/5490000000000?text=Hola%20CRIS-JOR%2C%20quiero%20hacer%20un%20pedido%20mayorista."
          target="_blank"
          rel="noreferrer"
          className="wa-float"
          aria-label="WhatsApp CRIS-JOR"
        >
          WA
        </a>
      )}
    </div>
  );
}
