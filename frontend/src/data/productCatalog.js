import { resolveProductVisual } from "./productVisuals";

const fallbackProducts = [
  { id: "fallback-blancheado-salado", name: "Blancheado Salado", price: 3000, description: "Bolsa de 1kg - blancheado salado." },
  { id: "fallback-blancheado-sin-sal", name: "Blancheado Sin Sal", price: 3000, description: "Bolsa de 1kg - blancheado sin sal." },
  { id: "fallback-blancheado-con-piel", name: "Blancheado Con Piel", price: 3000, description: "Bolsa de 1kg - blancheado con piel." },
  { id: "fallback-crocante-salado", name: "Crocante Salado", price: 3000, description: "Bolsa de 1kg - crocante salado." },
  { id: "fallback-jamon", name: "Crocante Sabor Jamon", price: 3000, description: "Bolsa de 1kg - sabor jamon." },
  { id: "fallback-queso", name: "Crocante Sabor Queso", price: 3000, description: "Bolsa de 1kg - sabor queso." },
  { id: "fallback-salame", name: "Crocante Sabor Salame", price: 3000, description: "Bolsa de 1kg - sabor salame." },
  { id: "fallback-frito", name: "Frito Salado Con Piel", price: 3000, description: "Bolsa de 1kg - frito salado con piel." },
  { id: "fallback-provenzal", name: "Crocante Sabor Provenzal", price: 3000, description: "Bolsa de 1kg - sabor provenzal." },
  { id: "fallback-envaina", name: "En Vaina", price: 3000, description: "Bolsa de 1kg - mani en vaina." }
];

const catalogMeta = [
  { key: "blancheado-salado", family: "Blancheado", deliveryDays: 1, availability: "high", featuredRank: 1, salesRank: 3 },
  { key: "blancheado-sin-sal", family: "Blancheado", deliveryDays: 1, availability: "high", featuredRank: 2, salesRank: 2 },
  { key: "blancheado-con-piel", family: "Blancheado", deliveryDays: 2, availability: "medium", featuredRank: 3, salesRank: 6 },
  { key: "crocante-salado", family: "Crocante", deliveryDays: 1, availability: "high", featuredRank: 4, salesRank: 1 },
  { key: "crocante-sabor-jamon", family: "Crocante", deliveryDays: 2, availability: "medium", featuredRank: 5, salesRank: 4 },
  { key: "crocante-sabor-queso", family: "Crocante", deliveryDays: 2, availability: "low", featuredRank: 6, salesRank: 7 },
  { key: "crocante-sabor-salame", family: "Crocante", deliveryDays: 2, availability: "low", featuredRank: 7, salesRank: 8 },
  { key: "frito-salado-con-piel", family: "Crocante", deliveryDays: 2, availability: "medium", featuredRank: 8, salesRank: 5 },
  { key: "crocante-sabor-provenzal", family: "Crocante", deliveryDays: 3, availability: "medium", featuredRank: 9, salesRank: 9 },
  { key: "en-vaina", family: "En Vaina", deliveryDays: 3, availability: "low", featuredRank: 10, salesRank: 10 }
];

export function getFallbackProducts() {
  return fallbackProducts.map((product) => ({ ...product }));
}

export function buildProductDetailPath(product) {
  return `/pedido/producto/${getProductKey(product)}`;
}

export function getProductKey(product) {
  return normalizeKey(product?.slug || product?.name || (typeof product?.id === "string" ? product.id : ""));
}

export function findCatalogProductByKey(products, key) {
  const normalizedKey = normalizeKey(key);
  return products.find((product) => {
    const candidates = [product?.id, product?.slug, product?.name, product?.displayName]
      .filter(Boolean)
      .map((value) => normalizeKey(value));
    return candidates.includes(normalizedKey);
  });
}

export function getCatalogDisplayInfo(product) {
  const visual = resolveProductVisual(product?.name || "");
  const key = getProductKey(product);
  const meta = catalogMeta.find((item) => item.key === key);
  const availability = getAvailabilityMeta(meta?.availability || "medium");
  return {
    ...visual,
    family: meta?.family || visual.family,
    deliveryDays: meta?.deliveryDays || 2,
    featuredRank: meta?.featuredRank || 999,
    salesRank: meta?.salesRank || 999,
    availabilityKey: meta?.availability || "medium",
    availabilityLabel: availability.label,
    availabilityTone: availability.tone,
    availabilityClass: availability.className,
    stockBadge: availability.badge,
    stockHint: availability.hint,
    isOutOfStock: meta?.availability === "none",
    isLowStock: meta?.availability === "low"
  };
}

export function sortCatalogProducts(products, sort) {
  const items = [...products];
  return items.sort((a, b) => {
    const aInfo = getCatalogDisplayInfo(a);
    const bInfo = getCatalogDisplayInfo(b);

    if (sort === "featured") {
      return aInfo.featuredRank - bInfo.featuredRank || String(a.name).localeCompare(String(b.name));
    }
    if (sort === "price-asc") return Number(a.price) - Number(b.price);
    if (sort === "price-desc") return Number(b.price) - Number(a.price);
    if (sort === "available-first") {
      return availabilityWeight(aInfo) - availabilityWeight(bInfo) || aInfo.featuredRank - bInfo.featuredRank;
    }
    if (sort === "best-sellers") {
      return aInfo.salesRank - bInfo.salesRank || aInfo.featuredRank - bInfo.featuredRank;
    }
    if (sort === "name-asc") return String(a.name).localeCompare(String(b.name));
    if (sort === "name-desc") return String(b.name).localeCompare(String(a.name));
    return 0;
  });
}

export function filterCatalogProducts(products, query, family, flavor) {
  const normalizedQuery = normalizeSearch(query);
  const normalizedFamily = normalizeSearch(family);
  const normalizedFlavor = normalizeSearch(flavor);

  return products.filter((product) => {
    const info = getCatalogDisplayInfo(product);
    const name = normalizeSearch(product?.name);
    const description = normalizeSearch(product?.description);
    const visualName = normalizeSearch(info.displayName);
    const familyName = normalizeSearch(info.family);
    const textMatch =
      !normalizedQuery ||
      name.includes(normalizedQuery) ||
      description.includes(normalizedQuery) ||
      visualName.includes(normalizedQuery) ||
      familyName.includes(normalizedQuery);

    const familyMatch = !normalizedFamily || familyName === normalizedFamily;
    const flavorMatch = !normalizedFlavor || visualName.includes(normalizedFlavor);

    return textMatch && familyMatch && flavorMatch;
  });
}

export function getAvailableFamilies(products) {
  const families = new Set();
  products.forEach((product) => families.add(getCatalogDisplayInfo(product).family));
  return Array.from(families).sort((a, b) => a.localeCompare(b));
}

export function getAvailableFlavors(products, family = "") {
  return products
    .filter((product) => !family || getCatalogDisplayInfo(product).family === family)
    .map((product) => getCatalogDisplayInfo(product).displayName)
    .filter((value, index, array) => array.indexOf(value) === index)
    .sort((a, b) => a.localeCompare(b));
}

export function getCatalogCardState(product) {
  const info = getCatalogDisplayInfo(product);
  if (info.isOutOfStock) {
    return {
      toneClass: "out",
      statusLabel: "No disponible",
      statusHint: "Sin disponibilidad por el momento",
      deliveryLabel: "Consultar reposición",
      canAdd: false
    };
  }
  if (info.isLowStock) {
    return {
      toneClass: "low",
      statusLabel: "Poca disponibilidad",
      statusHint: "Conviene reservar",
      deliveryLabel: `Entrega ${info.deliveryDays} a ${info.deliveryDays + 1} días`,
      canAdd: true
    };
  }
  return {
    toneClass: "ready",
    statusLabel: "Disponible",
    statusHint: "Stock estable",
    deliveryLabel: `Entrega ${info.deliveryDays} a ${info.deliveryDays + 1} días`,
    canAdd: true
  };
}

function availabilityWeight(info) {
  if (info.isOutOfStock) return 3;
  if (info.isLowStock) return 2;
  return 1;
}

function getAvailabilityMeta(level) {
  if (level === "none") {
    return {
      label: "No disponible",
      tone: "out",
      className: "out",
      badge: "No disponible",
      hint: "Volverá pronto"
    };
  }
  if (level === "low") {
    return {
      label: "Poca disponibilidad",
      tone: "low",
      className: "low",
      badge: "Stock bajo",
      hint: "Conviene reservar"
    };
  }
  return {
    label: "Disponible",
    tone: "ready",
    className: "ready",
    badge: "Disponible",
    hint: "Rotación estable"
  };
}

function normalizeSearch(value) {
  return String(value || "")
    .toLowerCase()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/[^a-z0-9]+/g, " ")
    .trim();
}

function normalizeKey(value) {
  return String(value || "")
    .toLowerCase()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "");
}
