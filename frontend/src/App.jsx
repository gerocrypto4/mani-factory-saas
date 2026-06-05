import { Navigate, Route, Routes } from "react-router-dom";
import HomePage from "./pages/HomePage";
import CatalogPage from "./pages/CatalogPage";
import ProductDetailPage from "./pages/ProductDetailPage";
import CheckoutPage from "./pages/CheckoutPage";
import ConfirmationPage from "./pages/ConfirmationPage";
import DashboardPage from "./pages/DashboardPage";
import ResumenPage from "./pages/ResumenPage";
import PedidosPage from "./pages/PedidosPage";
import ClientesPage from "./pages/ClientesPage";
import FinanzasPage from "./pages/FinanzasPage";
import StockPage from "./pages/StockPage";
import ProduccionPage from "./pages/ProduccionPage";
import Shell from "./components/Shell";
import { CartProvider } from "./state/cart";

export default function App() {
  return (
    <CartProvider>
      <Shell>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/pedido" element={<CatalogPage />} />
          <Route path="/pedido/producto/:productKey" element={<ProductDetailPage />} />
          <Route path="/checkout" element={<CheckoutPage />} />
          <Route path="/confirmacion" element={<ConfirmationPage />} />
          <Route path="/dashboard" element={<DashboardPage />}>
            <Route index element={<Navigate to="resumen" replace />} />
            <Route path="resumen" element={<ResumenPage />} />
            <Route path="pedidos" element={<PedidosPage />} />
            <Route path="clientes" element={<ClientesPage />} />
            <Route path="finanzas" element={<FinanzasPage />} />
            <Route path="stock" element={<StockPage />} />
            <Route path="produccion" element={<ProduccionPage />} />
          </Route>
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Shell>
    </CartProvider>
  );
}
