import { useEffect, useMemo, useRef, useState } from "react";
import { Link } from "react-router-dom";
import FaqItem from "../components/home/FaqItem";
import ProcessCard from "../components/home/ProcessCard";
import ProductCard from "../components/home/ProductCard";
import StatCard from "../components/home/StatCard";
import Ticker from "../components/home/Ticker";

export default function HomePage() {
  const [counts, setCounts] = useState({ years: 0, clients: 0, dispatch: 0, quality: 0 });
  const statsRef = useRef(null);
  const countedRef = useRef(false);

  useEffect(() => {
    const onScroll = () => {
      const h = document.documentElement;
      const total = h.scrollHeight - h.clientHeight;
      const p = total > 0 ? (h.scrollTop / total) * 100 : 0;
      h.style.setProperty("--cj-scroll", `${p}%`);
    };

    onScroll();
    window.addEventListener("scroll", onScroll, { passive: true });
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

  useEffect(() => {
    const nodes = document.querySelectorAll("[data-cj-reveal]");
    const io = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add("cj-visible");
          }
        });
      },
      { threshold: 0.1 }
    );

    nodes.forEach((node) => io.observe(node));
    return () => io.disconnect();
  }, []);

  useEffect(() => {
    if (!statsRef.current) return;

    const io = new IntersectionObserver(
      (entries) => {
        if (entries.some((entry) => entry.isIntersecting) && !countedRef.current) {
          countedRef.current = true;
          const goals = { years: 60, clients: 1200, dispatch: 48, quality: 99 };
          const start = performance.now();
          const duration = 1500;

          const tick = (now) => {
            const p = Math.min(1, (now - start) / duration);
            const ease = 1 - Math.pow(1 - p, 3);
            setCounts({
              years: Math.round(goals.years * ease),
              clients: Math.round(goals.clients * ease),
              dispatch: Math.round(goals.dispatch * ease),
              quality: Math.round(goals.quality * ease),
            });
            if (p < 1) requestAnimationFrame(tick);
          };

          requestAnimationFrame(tick);
          io.disconnect();
        }
      },
      { threshold: 0.3 }
    );

    io.observe(statsRef.current);
    return () => io.disconnect();
  }, []);

  const metrics = useMemo(
    () => [
      {
        n: counts.years,
        suffix: "+",
        label: "Años de trayectoria",
        icon: "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67V7z",
      },
      {
        n: counts.clients,
        suffix: "+",
        label: "Comercios abastecidos",
        icon: "M20 4H4c-1.11 0-2 .89-2 2v12c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V6c0-1.11-.89-2-2-2zm0 14H4v-6h16v6zm0-10H4V6h16v2z",
      },
      {
        n: counts.dispatch,
        suffix: "H",
        label: "Ventana de despacho",
        icon: "M20 8h-3V4H3c-1.1 0-2 .9-2 2v11h2c0 1.66 1.34 3 3 3s3-1.34 3-3h6c0 1.66 1.34 3 3 3s3-1.34 3-3h2v-5l-3-4zm-.5 1.5 1.96 2.5H17V9.5h2.5zM6 18c-.55 0-1-.45-1-1s.45-1 1-1 1 .45 1 1-.45 1-1 1zm13.5-1c0 .55-.45 1-1 1s-1-.45-1-1 .45-1 1-1 1 .45 1 1z",
      },
      {
        n: counts.quality,
        suffix: "%",
        label: "Control de calidad",
        icon: "M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm-2 16l-4-4 1.41-1.41L10 14.17l6.59-6.59L18 9l-8 8z",
      },
    ],
    [counts]
  );

  const tickerItems = [
    "Industria Argentina",
    "Elaborado artesanalmente en origen",
    "Venta al peso",
    "60 años en el mercado",
    "Canal B2B Mayorista",
  ];

  const productFrameBg = "linear-gradient(135deg,#3C2710,#24170B)";

  const products = [
    {
      slug: "blancheado",
      line: "Blancheado",
      name: "Blancheado Sin Sal",
      desc: "Maní pelado premium sin sal. Ideal para redistribución y uso industrial.",
      badge: "Blancheado",
      bg: productFrameBg,
      img: "/products/blancheado.jpg",
    },
    {
      slug: "envaina",
      line: "En Vaina",
      name: "Maní En Vaina",
      desc: "Presentación clásica para canal mayorista, con buena rotación y perfil tradicional.",
      badge: "En Vaina",
      bg: productFrameBg,
      img: "/products/envaina.jpg",
    },
    {
      slug: "crocante",
      line: "Crocante",
      name: "Crocante Sabor Pizza",
      desc: "Crocante sabor pizza, ideal para kioscos y almacenes con alta rotación.",
      badge: "Crocante",
      bg: productFrameBg,
      img: "/products/pizza.jpg",
    },
  ];

  const processSteps = [
    {
      n: "01",
      icon: "M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z",
      title: "Certificaciones y control",
      desc: "Lotes con trazabilidad y verificación interna antes de cada despacho.",
    },
    {
      n: "02",
      icon: "M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4",
      title: "Proceso comercial",
      desc: "Pedido digital, confirmación rápida y coordinación logística continua.",
    },
    {
      n: "03",
      icon: "M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z",
      title: "Soporte a distribuidores",
      desc: "Acompañamiento para apertura de zona y crecimiento del canal.",
    },
  ];

  const heroPeanuts = useMemo(() => {
    const rand = mulberry32(1966);
    const peanuts = [];
    for (let i = 0; i < 32; i++) {
      let x, y, validPos = false;
      while (!validPos) {
        x = -20 + rand() * 1040;
        y = -20 + rand() * 740;
        validPos = !peanuts.some(p => Math.hypot(p.x - x, p.y - y) < 140);
      }
      const scale = 0.58 + rand() * 0.38;
      const rotate = 55 + rand() * 65;
      const tone = 0.24 + rand() * 0.18;
      const rx = 28 + rand() * 8;
      const ry = 16 + rand() * 5;

      peanuts.push({
        id: i,
        x,
        y,
        scale,
        rotate,
        tone,
        flip: rand() > 0.5 ? 1 : -1,
        rx,
        ry,
      });
    }
    return peanuts;
  }, []);

  return (
    <div className="cj-page">
      <div className="cj-progress" />

      <section className="cj-hero">
        <div className="cj-hero-grid" />
        <div className="cj-hero-glow" />
        <svg className="cj-hero-peanut-mark" viewBox="0 0 1000 700" preserveAspectRatio="none" aria-hidden="true">
          <defs>
            <linearGradient id="cjHeroPeanutBody" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor="#FDF0CD" />
              <stop offset="35%" stopColor="#E7C37A" />
              <stop offset="68%" stopColor="#C98F44" />
              <stop offset="100%" stopColor="#8C5D24" />
            </linearGradient>
            <linearGradient id="cjHeroPeanutSeam" x1="0%" y1="0%" x2="100%" y2="0%">
              <stop offset="0%" stopColor="#FFF8E3" stopOpacity="0.18" />
              <stop offset="48%" stopColor="#8A581D" stopOpacity="0.68" />
              <stop offset="52%" stopColor="#FFF7E4" stopOpacity="0.5" />
              <stop offset="100%" stopColor="#8D5A1D" stopOpacity="0.76" />
            </linearGradient>
            <radialGradient id="cjHeroPeanutGlow" cx="72%" cy="38%" r="72%">
              <stop offset="0%" stopColor="#D29B3F" stopOpacity="0.16" />
              <stop offset="46%" stopColor="#A05E18" stopOpacity="0.08" />
              <stop offset="100%" stopColor="#1A1209" stopOpacity="0" />
            </radialGradient>
            <filter id="cjHeroPeanutShadow" x="-16%" y="-16%" width="132%" height="132%">
              <feDropShadow dx="0" dy="3" stdDeviation="2.25" floodColor="#120B04" floodOpacity="0.12" />
            </filter>
          </defs>
          <rect x="0" y="0" width="1000" height="700" fill="url(#cjHeroPeanutGlow)" />
          {heroPeanuts.map((peanut) => {
            const h = peanut.rx;
            const w = peanut.ry;
            const nw = w * 0.62;
            const ts = 0.88;
            const shellPath = [
              `M 0,${-h}`,
              `C ${w*ts*0.86},${-h} ${w*ts},${-h*0.5} ${w*ts},${-h*0.14}`,
              `C ${w*ts},${-h*0.02} ${nw*1.12},${h*0.02} ${nw},${h*0.09}`,
              `C ${nw*0.72},${h*0.16} ${w*0.88},${h*0.28} ${w*0.92},${h*0.47}`,
              `C ${w*0.94},${h*0.78} ${w*0.56},${h} 0,${h}`,
              `C ${-w*0.56},${h} ${-w*0.94},${h*0.78} ${-w*0.92},${h*0.47}`,
              `C ${-w*0.88},${h*0.28} ${-nw*0.72},${h*0.16} ${-nw},${h*0.09}`,
              `C ${-nw*1.12},${h*0.02} ${-w*ts},${-h*0.02} ${-w*ts},${-h*0.14}`,
              `C ${-w*ts},${-h*0.5} ${-w*ts*0.86},${-h} 0,${-h} Z`,
            ].join(" ");
            const tc = "rgba(58,28,6,0.44)";
            const tw = 1.0;

            return (
              <g
                key={peanut.id}
                transform={`translate(${peanut.x} ${peanut.y}) rotate(${peanut.rotate}) scale(${peanut.scale} ${peanut.scale}) scale(${peanut.flip} 1)`}
                filter="url(#cjHeroPeanutShadow)"
                opacity={peanut.tone}
              >
                <path d={shellPath} fill="url(#cjHeroPeanutBody)" />
                {/* sombra interior lóbulo superior */}
                <ellipse cx={-w*0.24} cy={-h*0.55} rx={w*0.32} ry={h*0.28} fill="rgba(0,0,0,0.08)" />
                <ellipse cx={w*0.24} cy={-h*0.55} rx={w*0.32} ry={h*0.28} fill="rgba(0,0,0,0.08)" />
                {/* lóbulo superior — arcos horizontales sutiles */}
                <path d={`M ${-w*0.6},${-h*0.68} C ${-w*0.2},${-h*0.76} ${w*0.2},${-h*0.76} ${w*0.6},${-h*0.68}`} fill="none" stroke="rgba(50,20,5,0.36)" strokeWidth="0.9" strokeLinecap="round" />
                <path d={`M ${-w*0.74},${-h*0.4} C ${-w*0.24},${-h*0.52} ${w*0.24},${-h*0.52} ${w*0.74},${-h*0.4}`} fill="none" stroke="rgba(50,20,5,0.32)" strokeWidth="0.85" strokeLinecap="round" />
                <path d={`M ${-w*0.68},${-h*0.14} C ${-w*0.22},${-h*0.26} ${w*0.22},${-h*0.26} ${w*0.68},${-h*0.14}`} fill="none" stroke="rgba(50,20,5,0.28)" strokeWidth="0.8" strokeLinecap="round" />
                {/* lóbulo superior — nervaduras diagonales suaves */}
                <path d={`M ${-w*0.2},${-h*0.82} C ${-w*0.36},${-h*0.54} ${-w*0.28},${-h*0.22} ${-nw*0.6},${-h*0.04}`} fill="none" stroke="rgba(50,20,5,0.24)" strokeWidth="0.75" strokeLinecap="round" />
                <path d={`M ${w*0.2},${-h*0.82} C ${w*0.36},${-h*0.54} ${w*0.28},${-h*0.22} ${nw*0.6},${-h*0.04}`} fill="none" stroke="rgba(50,20,5,0.24)" strokeWidth="0.75" strokeLinecap="round" />
                {/* lóbulo inferior — arcos horizontales */}
                <path d={`M ${-w*0.8},${h*0.26} C ${-w*0.28},${h*0.16} ${w*0.28},${h*0.16} ${w*0.8},${h*0.26}`} fill="none" stroke="rgba(50,20,5,0.3)" strokeWidth="0.85" strokeLinecap="round" />
                <path d={`M ${-w*0.88},${h*0.52} C ${-w*0.32},${h*0.42} ${w*0.32},${h*0.42} ${w*0.88},${h*0.52}`} fill="none" stroke="rgba(50,20,5,0.26)" strokeWidth="0.8" strokeLinecap="round" />
                <path d={`M ${-w*0.8},${h*0.76} C ${-w*0.28},${h*0.66} ${w*0.28},${h*0.66} ${w*0.8},${h*0.76}`} fill="none" stroke="rgba(50,20,5,0.22)" strokeWidth="0.75" strokeLinecap="round" />
                {/* lóbulo inferior — nervaduras diagonales */}
                <path d={`M ${-nw*0.6},${h*0.12} C ${-w*0.5},${h*0.38} ${-w*0.48},${h*0.64} ${-w*0.34},${h*0.86}`} fill="none" stroke="rgba(50,20,5,0.22)" strokeWidth="0.72" strokeLinecap="round" />
                <path d={`M ${nw*0.6},${h*0.12} C ${w*0.5},${h*0.38} ${w*0.48},${h*0.64} ${w*0.34},${h*0.86}`} fill="none" stroke="rgba(50,20,5,0.22)" strokeWidth="0.72" strokeLinecap="round" />
                {/* highlight suave lóbulo superior */}
                <path d={`M ${-w*0.3},${-h*0.82} C ${-w*0.1},${-h*0.88} ${w*0.06},${-h*0.76} ${w*0.14},${-h*0.62}`} fill="none" stroke="rgba(255,248,220,0.16)" strokeWidth="1.2" strokeLinecap="round" />
              </g>
            );
          })}
        </svg>

        <div className="cj-hero-panel cj-hero-left">
          <div className="cj-hero-chips">
            <span className="cj-chip-inv">Canal Mayorista</span>
            <span className="cj-chip-inv">Desde 1966</span>
          </div>
          <p className="cj-eyebrow-inv">Canal Mayorista</p>
          <h1 className="cj-hero-h1">
            <span className="cj-brand-wordmark cj-brand-wordmark-hero">
              CRIS<span className="cj-brand-wordmark-separator">-</span>JOR
            </span>
          </h1>
          <div className="cj-divider" />
          <p className="cj-hero-sub">Producción constante de maní saborizado para comercios de todo el país. Industria Argentina.</p>
          <div className="cj-hero-actions">
            <Link to="/pedido" className="cj-btn-gold">
              Hacer pedido →
            </Link>
            <Link to="/pedido" className="cj-btn-outline-inv">
              Ver catálogo
            </Link>
          </div>
        </div>

        <div className="cj-hero-panel cj-hero-right">
          <div className="cj-hero-year">1966</div>
          <div className="cj-hero-right-content">
            <h2 className="cj-hero-tagline">
              60 años de trabajo
              <br />
              <em className="cj-gold">industrial</em>
            </h2>
            <p className="cj-hero-desc">Trayectoria, control de calidad y foco comercial para que tu negocio nunca se quede sin stock.</p>
            <div className="cj-hero-mini-stats">
              <div>
                <span className="cj-mini-n">48H</span>
                <span className="cj-mini-l">Despacho</span>
              </div>
              <div>
                <span className="cj-mini-n">99%</span>
                <span className="cj-mini-l">Calidad</span>
              </div>
              <div>
                <span className="cj-mini-n">B2B</span>
                <span className="cj-mini-l">Mayorista</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <Ticker items={tickerItems} />

      <section className="cj-section cj-bg-cream" data-cj-reveal>
        <div className="cj-container">
          <div className="cj-two-col">
            <div className="cj-reveal-left" data-cj-reveal>
              <div className="cj-factory-frame">
                <img src="/about/fabrica.jpg" alt="Planta industrial CRIS-JOR" className="cj-factory-image" />
                <div className="cj-factory-label">Empresa familiar</div>
              </div>
            </div>
            <div className="cj-reveal-right" data-cj-reveal>
              <span className="cj-tag">Sobre nosotros</span>
              <div className="cj-divider" />
              <h2 className="cj-section-title">
                Empresa familiar con <em className="cj-gold">60 años</em> de historia
              </h2>
              <p className="cj-body-text">
                <span className="cj-brand-wordmark cj-brand-wordmark-inline">
                  CRIS<span className="cj-brand-wordmark-separator">-</span>JOR
                </span>{" "}
                es una empresa familiar argentina con más de seis décadas elaborando y distribuyendo maní saborizado para comercios de todo el país.
              </p>
              <p className="cj-body-text" style={{ marginTop: "0.75rem" }}>
                Cada lote pasa por control interno antes del despacho, garantizando que el producto llegue a tu negocio en condiciones óptimas, siempre.
              </p>
              <div className="cj-inline-stats">
                <div className="cj-inline-stat">
                  <strong>60+</strong>
                  <span>Años</span>
                </div>
                <div className="cj-inline-stat">
                  <strong>1200+</strong>
                  <span>Comercios</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section className="cj-section cj-bg-dark" ref={statsRef}>
        <div className="cj-container">
          <div className="cj-stats-head" data-cj-reveal>
            <span className="cj-tag-inv">En números</span>
            <div className="cj-divider" style={{ margin: "12px auto" }} />
            <h2 className="cj-section-title-inv">
              60 años hablando <em className="cj-gold">por sí solos</em>
            </h2>
          </div>
          <div className="cj-stats-grid">
            {metrics.map((metric, index) => (
              <StatCard key={metric.label} metric={metric} delay={index * 0.1} />
            ))}
          </div>
        </div>
      </section>

      <section className="cj-section cj-bg-darkest">
        <div className="cj-container">
          <div className="cj-section-header">
            <div className="cj-reveal-left" data-cj-reveal>
              <span className="cj-tag-inv">Catálogo mayorista</span>
              <div className="cj-divider" />
              <h2 className="cj-section-title-inv">
                Nuestras líneas <em className="cj-gold">de producto</em>
              </h2>
            </div>
            <div className="cj-reveal-right" data-cj-reveal>
              <Link to="/pedido" className="cj-btn-gold">
                Ver catálogo completo →
              </Link>
            </div>
          </div>
          <div className="cj-products-grid">
            {products.map((product, index) => (
              <ProductCard key={product.slug} product={product} delay={index * 0.12} />
            ))}
          </div>
        </div>
      </section>

      <section className="cj-section cj-bg-cream">
        <div className="cj-container">
          <div className="cj-center-head" data-cj-reveal>
            <span className="cj-tag">Nuestro proceso</span>
            <div className="cj-divider" style={{ margin: "12px auto" }} />
            <h2 className="cj-section-title">
              Calidad en cada <em className="cj-gold">etapa</em>
            </h2>
          </div>
          <div className="cj-process-grid">
            {processSteps.map((step, index) => (
              <ProcessCard key={step.n} step={step} delay={index * 0.12} />
            ))}
          </div>
        </div>
      </section>

      <section className="cj-section cj-bg-dark2">
        <div className="cj-container">
          <div className="cj-two-col">
            <div className="cj-reveal-left" data-cj-reveal>
              <span className="cj-tag-inv">Misión y Visión</span>
              <div className="cj-divider" />
              <h2 className="cj-section-title-inv">
                Nuestra dirección <em className="cj-gold">como empresa</em>
              </h2>
              <div className="cj-mv-block">
                <span className="cj-mv-label">Misión</span>
                <p className="cj-mv-text">
                  Producir y distribuir maní saborizado con calidad constante para que mayoristas y distribuidores operen con reposición confiable.
                </p>
              </div>
              <div className="cj-mv-block" style={{ marginTop: "1.25rem" }}>
                <span className="cj-mv-label">Visión</span>
                <p className="cj-mv-text">
                  Ser la marca argentina de referencia en canal snack mayorista, reconocida por trayectoria y cumplimiento logístico.
                </p>
              </div>
            </div>
            <div className="cj-mv-icons-grid" data-cj-reveal>
              {[
                {
                  icon: "M9 12l2 2 4-4M7.835 4.697a3.42 3.42 0 001.946-.806 3.42 3.42 0 014.438 0 3.42 3.42 0 001.946.806 3.42 3.42 0 013.138 3.138 3.42 3.42 0 00.806 1.946 3.42 3.42 0 010 4.438 3.42 3.42 0 00-.806 1.946 3.42 3.42 0 01-3.138 3.138 3.42 3.42 0 00-1.946.806 3.42 3.42 0 01-4.438 0 3.42 3.42 0 00-1.946-.806 3.42 3.42 0 01-3.138-3.138 3.42 3.42 0 00-.806-1.946 3.42 3.42 0 010-4.438 3.42 3.42 0 00.806-1.946 3.42 3.42 0 013.138-3.138z",
                  label: "Trayectoria",
                },
                {
                  icon: "M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z M15 11a3 3 0 11-6 0 3 3 0 016 0z",
                  label: "Cobertura nacional",
                },
                {
                  icon: "M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z",
                  label: "Producto natural",
                },
                {
                  icon: "M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z",
                  label: "Empresa familiar",
                },
              ].map((item) => (
                <div key={item.label} className="cj-mv-icon-card">
                  <svg className="cj-mv-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                    <path strokeLinecap="round" strokeLinejoin="round" d={item.icon} />
                  </svg>
                  <span className="cj-mv-icon-label">{item.label}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>

      <section id="nosotros" className="cj-section cj-bg-cream">
        <div className="cj-container">
          <div className="cj-two-col cj-faq-layout">
            <div className="cj-reveal-left" data-cj-reveal>
              <span className="cj-tag">Soporte comercial</span>
              <div className="cj-divider" />
              <h2 className="cj-section-title">
                Preguntas <em className="cj-gold">frecuentes</em>
              </h2>
              <p className="cj-body-text">Respuestas rápidas para definir compra, logística y operación mayorista.</p>
            </div>
            <div className="cj-faq-list" data-cj-reveal>
              {[
                { q: "¿Hay mínimo de compra?", a: "Sí, se define por canal y zona para garantizar operación eficiente." },
                { q: "¿Cómo se coordina el envío?", a: "Al confirmar el pedido se valida transporte, ventana de salida y recepción." },
                { q: "¿Puedo comprar como distribuidor?", a: "Sí, contamos con esquema comercial para distribuidores y reventa." },
                { q: "¿Cuánto demora el despacho?", a: "Depende de zona y volumen, pero manejamos confirmación comercial ágil." },
              ].map((item, index) => (
                <FaqItem key={index} q={item.q} a={item.a} />
              ))}
            </div>
          </div>
        </div>
      </section>

      <section id="contacto" className="cj-cta-band">
        <div className="cj-container">
          <div className="cj-cta-inner" data-cj-reveal>
            <div>
              <h2 className="cj-cta-title">
                ¿LISTO PARA EMPEZAR
                <br />
                A VENDER{" "}
                <span className="cj-brand-wordmark cj-brand-wordmark-inline">
                  CRIS<span className="cj-brand-wordmark-separator">-</span>JOR
                </span>
                ?
              </h2>
              <p className="cj-cta-sub">Entrá al módulo de pedidos y completá tu solicitud mayorista. Atención comercial directa.</p>
            </div>
            <div className="cj-cta-right">
              <div className="cj-cta-btns">
                <Link to="/pedido" className="cj-btn-dark">
                  Ir a pedidos →
                </Link>
                <a href="https://wa.me/5490000000000" target="_blank" rel="noreferrer" className="cj-btn-outline-dark">
                  WhatsApp comercial
                </a>
              </div>
              <div className="cj-cta-stats">
                <div>
                  <strong>48H</strong>
                  <span>Despacho</span>
                </div>
                <div>
                  <strong>60+</strong>
                  <span>Años</span>
                </div>
                <div>
                  <strong>B2B</strong>
                  <span>Mayorista</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <footer className="cj-footer">
        <div className="cj-container">
          <div className="cj-footer-grid">
            <div>
              <div className="cj-footer-logo">
                <span className="cj-brand-wordmark cj-brand-wordmark-footer">
                  CRIS<span className="cj-brand-wordmark-separator">-</span>JOR
                </span>
              </div>
              <div className="cj-footer-divider" />
              <p className="cj-footer-desc">Fabricación y distribución mayorista de maní saborizado. Industria Argentina. Desde 1966.</p>
            </div>
            <div>
              <h6 className="cj-footer-heading">Navegación</h6>
              <a href="/#nosotros" className="cj-footer-link">
                Nosotros
              </a>
              <Link to="/pedido" className="cj-footer-link">
                Productos
              </Link>
              <Link to="/pedido" className="cj-footer-link">
                Pedido
              </Link>
            </div>
            <div>
              <h6 className="cj-footer-heading">Comercial</h6>
              <a href="https://wa.me/5490000000000" target="_blank" rel="noreferrer" className="cj-footer-link">
                WhatsApp
              </a>
              <a href="/#contacto" className="cj-footer-link">
                Contacto
              </a>
              <span className="cj-footer-muted">Lun a Sab</span>
            </div>
            <div>
              <h6 className="cj-footer-heading">Empresa</h6>
              <span className="cj-footer-muted">Argentina</span>
              <span className="cj-footer-muted">Canal mayorista B2B</span>
              <span className="cj-footer-muted">Desde 1966</span>
            </div>
          </div>
          <div className="cj-footer-bottom">
            <span>
              <span className="cj-brand-wordmark cj-brand-wordmark-inline">
                CRIS<span className="cj-brand-wordmark-separator">-</span>JOR
              </span>{" "}
              · Sistema Comercial Mayorista
            </span>
            <span>© {new Date().getFullYear()} Todos los derechos reservados</span>
          </div>
        </div>
      </footer>

      <a href="https://wa.me/5490000000000" target="_blank" rel="noreferrer" className="wa-float" aria-label="WhatsApp">
        <svg viewBox="0 0 24 24" fill="currentColor" style={{ width: "1.6rem", height: "1.6rem" }}>
          <path d="M17.472 14.382c-.297-.149-1.758-.867-2.03-.967-.273-.099-.471-.148-.67.15-.197.297-.767.966-.94 1.164-.173.199-.347.223-.644.075-.297-.15-1.255-.463-2.39-1.475-.883-.788-1.48-1.761-1.653-2.059-.173-.297-.018-.458.13-.606.134-.133.298-.347.446-.52.149-.174.198-.298.298-.497.099-.198.05-.371-.025-.52-.075-.149-.669-1.612-.916-2.207-.242-.579-.487-.5-.669-.51-.173-.008-.371-.01-.57-.01-.198 0-.52.074-.792.372-.272.297-1.04 1.016-1.04 2.479 0 1.462 1.065 2.875 1.213 3.074.149.198 2.096 3.2 5.077 4.487.709.306 1.262.489 1.694.625.712.227 1.36.195 1.871.118.571-.085 1.758-.719 2.006-1.413.248-.694.248-1.289.173-1.413-.074-.124-.272-.198-.57-.347z" />
          <path d="M12 0C5.373 0 0 5.373 0 12c0 2.126.553 4.12 1.522 5.854L.044 23.222a.75.75 0 00.934.934l5.368-1.478A11.954 11.954 0 0012 24c6.627 0 12-5.373 12-12S18.627 0 12 0zm0 22c-1.907 0-3.698-.5-5.247-1.377l-.376-.214-3.906 1.075 1.075-3.906-.214-.376A9.956 9.956 0 012 12C2 6.477 6.477 2 12 2s10 4.477 10 10-4.477 10-10 10z" />
        </svg>
      </a>
    </div>
  );
}

function mulberry32(seed) {
  let t = seed >>> 0;
  return function random() {
    t += 0x6D2B79F5;
    let r = Math.imul(t ^ (t >>> 15), 1 | t);
    r ^= r + Math.imul(r ^ (r >>> 7), 61 | r);
    return ((r ^ (r >>> 14)) >>> 0) / 4294967296;
  };
}
