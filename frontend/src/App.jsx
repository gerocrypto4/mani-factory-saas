import { Navigate, Route, Routes } from "react-router-dom";
import HomePage from "./pages/HomePage";
import CatalogPage from "./pages/CatalogPage";
import CheckoutPage from "./pages/CheckoutPage";
import ConfirmationPage from "./pages/ConfirmationPage";
import DashboardPage from "./pages/DashboardPage";
import Shell from "./components/Shell";
import { CartProvider } from "./state/cart";

export default function App() {
  return (
    <CartProvider>
      <Shell>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/pedido" element={<CatalogPage />} />
          <Route path="/checkout" element={<CheckoutPage />} />
          <Route path="/confirmacion" element={<ConfirmationPage />} />
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Shell>
    </CartProvider>
  );
}
