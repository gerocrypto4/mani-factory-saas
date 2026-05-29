import { useEffect, useState } from "react";
import { Link, useLocation } from "react-router-dom";
import { useCart } from "../state/use-cart";

export default function Shell({ children }) {
  const { items } = useCart();
  const location = useLocation();
  const dashboard = location.pathname.startsWith("/dashboard");
  const showFloatingWhatsapp = !dashboard && location.pathname !== "/";
  const [scrolled, setScrolled] = useState(false);
  const isLanding = !dashboard;

  useEffect(() => {
    function onScroll() {
      setScrolled(window.scrollY > 12);
    }

    onScroll();
    window.addEventListener("scroll", onScroll, { passive: true });
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

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

                <nav className="cj-shell-nav cj-shell-nav-dashboard" aria-label="Navegación principal">
                  <Link className={location.pathname === "/" ? "nav-link-active" : "nav-link"} to="/">
                    Inicio
                  </Link>
                  <Link className={location.pathname === "/pedido" ? "nav-link-active" : "nav-link"} to="/pedido">
                    Productos
                  </Link>
                  <Link className={location.pathname === "/checkout" ? "nav-link-active" : "nav-link"} to="/checkout">
                    Pedido ({items.length})
                  </Link>
                  <Link className={dashboard ? "nav-link-active" : "nav-link"} to="/dashboard">
                    Dashboard
                  </Link>
                </nav>
              </>
            ) : (
              <>
                <nav className="cj-shell-nav cj-shell-nav-left" aria-label="Navegación de productos">
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
                  <span className="cj-shell-brandline">Maní saborizado mayorista</span>
                </div>

                <nav className="cj-shell-nav cj-shell-nav-right" aria-label="Navegación comercial">
                  <a className="nav-link" href="/#nosotros">
                    Nosotros
                  </a>
                  <a className="nav-link" href="/#contacto">
                    Contactenos
                  </a>
                  <Link to="/checkout" className="cart-link" aria-label="Carrito">
                    <span className="cart-icon">🛒</span>
                    <span className="cart-count">{items.length}</span>
                  </Link>
                </nav>
              </>
            )}
          </div>
        </div>
      </header>

      <main className={`${dashboard ? "mx-auto max-w-7xl px-4 py-6 md:px-8" : ""}`}>{children}</main>

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
