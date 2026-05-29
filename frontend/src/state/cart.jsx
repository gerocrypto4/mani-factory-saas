import { useMemo, useState } from "react";
import { CartContext } from "./cart-context";
import { ORDER_RULES } from "../constants/orderRules";

export function CartProvider({ children }) {
  const [items, setItems] = useState([]);
  const [lastOrder, setLastOrder] = useState(null);

  function addItem(product, quantity = ORDER_RULES.minKgPerFlavor) {
    const requestedQuantity = Number(quantity) || ORDER_RULES.minKgPerFlavor;
    const safeQuantity = Math.max(
      ORDER_RULES.minKgPerFlavor,
      Math.ceil(requestedQuantity / ORDER_RULES.minKgPerFlavor) * ORDER_RULES.minKgPerFlavor
    );
    setItems((prev) => {
      const existing = prev.find((p) => p.id === product.id);
      if (existing) {
        return prev.map((p) =>
          p.id === product.id ? { ...p, quantity: p.quantity + safeQuantity } : p
        );
      }
      return [...prev, { ...product, quantity: safeQuantity }];
    });
  }

  function removeItem(productId) {
    setItems((prev) => prev.filter((p) => p.id !== productId));
  }

  function setQuantity(productId, quantity) {
    const requestedQuantity = Number.isNaN(quantity) ? ORDER_RULES.minKgPerFlavor : quantity;
    const safeQty = Math.max(
      ORDER_RULES.minKgPerFlavor,
      Math.ceil(requestedQuantity / ORDER_RULES.minKgPerFlavor) * ORDER_RULES.minKgPerFlavor
    );
    setItems((prev) =>
      prev.map((p) => (p.id === productId ? { ...p, quantity: safeQty } : p))
    );
  }

  function clearCart() {
    setItems([]);
  }

  const total = useMemo(
    () => items.reduce((acc, item) => acc + Number(item.price) * item.quantity, 0),
    [items]
  );

  const value = {
    items,
    total,
    addItem,
    removeItem,
    setQuantity,
    clearCart,
    lastOrder,
    setLastOrder
  };

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}
