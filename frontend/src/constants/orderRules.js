export const ORDER_RULES = {
  minOrderKg: 300,
  bagWeightKg: 1,
  minKgPerFlavor: 10,
  quickAddKgOptions: [10, 20, 50],
};

export function formatCurrency(value) {
  return Number(value || 0).toLocaleString("es-AR", {
    style: "currency",
    currency: "ARS",
    maximumFractionDigits: 0,
  });
}

export function formatKg(value) {
  return `${Number(value || 0)} kg`;
}
