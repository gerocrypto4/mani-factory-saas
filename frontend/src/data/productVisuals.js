const visuals = [
  {
    match: ["blancheado salado"],
    image: "/products/blancheado-salado.jpg",
    displayName: "Blancheado Salado",
    family: "Blancheado",
    tone: "from-amber-500/28 via-orange-400/10 to-transparent",
    note: "Blancheado con sal en formato comercial."
  },
  {
    match: ["blancheado sin sal"],
    image: "/products/blancheado-sin-sal.jpg",
    displayName: "Blancheado Sin Sal",
    family: "Blancheado",
    tone: "from-amber-500/28 via-orange-400/10 to-transparent",
    note: "Blancheado sin sal, listo para distribución mayorista."
  },
  {
    match: ["blancheado con piel", "blancheado piel"],
    image: "/products/blancheado-salado.jpg",
    displayName: "Blancheado Con Piel",
    family: "Blancheado",
    tone: "from-amber-500/28 via-orange-400/10 to-transparent",
    note: "Blancheado con piel, con imagen provisoria."
  },
  {
    match: ["crocante salado"],
    image: "/products/crocante-salado.jpg",
    displayName: "Crocante Salado",
    family: "Crocante",
    tone: "from-orange-500/28 via-amber-400/10 to-transparent",
    note: "Crocante salado de alta rotación."
  },
  {
    match: ["crocante sabor jamon", "jamon"],
    image: "/products/jamon.jpg",
    displayName: "Crocante Sabor Jamon",
    family: "Crocante",
    tone: "from-red-500/28 via-rose-400/10 to-transparent",
    note: "Crocante sabor jamon con presencia premium."
  },
  {
    match: ["crocante sabor queso", "queso"],
    image: "/products/queso.jpg",
    displayName: "Crocante Sabor Queso",
    family: "Crocante",
    tone: "from-blue-500/28 via-cyan-400/10 to-transparent",
    note: "Crocante sabor queso con rotación estable."
  },
  {
    match: ["crocante sabor salame", "salame"],
    image: "/products/salame.jpg",
    displayName: "Crocante Sabor Salame",
    family: "Crocante",
    tone: "from-red-600/28 via-rose-400/10 to-transparent",
    note: "Crocante sabor salame, clásico de alta salida."
  },
  {
    match: ["frito salado con piel", "frito salado"],
    image: "/products/frito-salado.jpg",
    displayName: "Frito Salado Con Piel",
    family: "Crocante",
    tone: "from-slate-800/28 via-orange-400/10 to-transparent",
    note: "Frito salado con piel y perfil tradicional."
  },
  {
    match: ["crocante sabor provenzal", "provenzal"],
    image: "/products/provenzal.jpg",
    displayName: "Crocante Sabor Provenzal",
    family: "Crocante",
    tone: "from-lime-500/28 via-emerald-400/10 to-transparent",
    note: "Crocante sabor provenzal con perfil herbal."
  },
  {
    match: ["en vaina"],
    image: "/products/envaina.jpg",
    displayName: "En Vaina",
    family: "En Vaina",
    tone: "from-amber-500/28 via-yellow-300/10 to-transparent",
    note: "Maní en vaina para canal mayorista."
  }
];

function normalizeText(value = "") {
  return String(value)
    .toLowerCase()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/\s+/g, " ")
    .trim();
}

export function resolveProductVisual(name = "") {
  const normalized = normalizeText(name);
  const found = visuals.find((item) => item.match.some((key) => normalized.includes(key)));
  return found || {
    image: "/products/blancheado-sin-sal.jpg",
    displayName: "Blancheado Sin Sal",
    family: "Blancheado",
    tone: "from-amber-400/20 to-transparent",
    note: "Producto premium de fábrica."
  };
}
