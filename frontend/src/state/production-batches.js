import { useCallback, useEffect, useState } from "react";
import {
  createProductionBatch,
  deleteProductionBatch,
  fetchDashboardProducts,
  fetchProductionBatches
} from "../services/api";

const TENANT_ID = 1;

export function useProductionBatches() {
  const [batches, setBatches] = useState([]);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  const loadBatches = useCallback(async () => {
    try {
      setLoading(true);
      setError("");
      const [batchesData, productsData] = await Promise.all([
        fetchProductionBatches(TENANT_ID),
        fetchDashboardProducts(TENANT_ID)
      ]);
      setBatches(Array.isArray(batchesData) ? batchesData : []);
      setProducts(Array.isArray(productsData) ? productsData : []);
    } catch {
      setError("No pudimos cargar el módulo de producción.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadBatches();
  }, [loadBatches]);

  const addBatch = useCallback(
    async (input) => {
      const quantity = Number(input.quantityKg);
      if (!input.productId || !Number.isFinite(quantity) || quantity <= 0) {
        return false;
      }
      try {
        setSaving(true);
        setError("");
        await createProductionBatch(
          {
            productId: Number(input.productId),
            quantityKg: Math.round(quantity * 1000) / 1000,
            batchDate: input.batchDate || todayIsoDate(),
            note: normalizeText(input.note)
          },
          TENANT_ID
        );
        await loadBatches();
        return true;
      } catch {
        setError("No pudimos registrar el lote de producción.");
        return false;
      } finally {
        setSaving(false);
      }
    },
    [loadBatches]
  );

  const removeBatch = useCallback(
    async (id) => {
      try {
        setSaving(true);
        setError("");
        await deleteProductionBatch(id, TENANT_ID);
        await loadBatches();
      } catch {
        setError("No pudimos eliminar el lote de producción.");
      } finally {
        setSaving(false);
      }
    },
    [loadBatches]
  );

  return {
    batches,
    products,
    loading,
    saving,
    error,
    refresh: loadBatches,
    addBatch,
    removeBatch
  };
}

function normalizeText(value) {
  if (value == null) return null;
  const normalized = String(value).trim();
  return normalized.length ? normalized : null;
}

function todayIsoDate() {
  return new Date().toISOString().slice(0, 10);
}
