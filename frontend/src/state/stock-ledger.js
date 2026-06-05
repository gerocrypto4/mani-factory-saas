import { useCallback, useEffect, useState } from "react";
import {
  createStockEntry,
  deleteStockEntry,
  fetchDashboardProducts,
  fetchStockEntries,
  fetchStockSummary
} from "../services/api";

const TENANT_ID = 1;

export function useStockLedger() {
  const [entries, setEntries] = useState([]);
  const [levels, setLevels] = useState([]);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  const loadLedger = useCallback(async () => {
    try {
      setLoading(true);
      setError("");
      const [entriesData, levelsData, productsData] = await Promise.all([
        fetchStockEntries(TENANT_ID),
        fetchStockSummary(TENANT_ID),
        fetchDashboardProducts(TENANT_ID)
      ]);
      setEntries(Array.isArray(entriesData) ? entriesData : []);
      setLevels(Array.isArray(levelsData) ? levelsData : []);
      setProducts(Array.isArray(productsData) ? productsData : []);
    } catch {
      setError("No pudimos cargar el módulo de stock.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadLedger();
  }, [loadLedger]);

  const addEntry = useCallback(
    async (input) => {
      const quantity = Number(input.quantity);
      if (!Number.isFinite(quantity) || quantity <= 0) {
        return false;
      }
      try {
        setSaving(true);
        setError("");
        await createStockEntry(
          {
            type: String(input.type || "").toUpperCase(),
            productId: Number(input.productId),
            quantity: Math.round(quantity * 1000) / 1000,
            note: normalizeText(input.note),
            entryDate: input.entryDate || todayIsoDate()
          },
          TENANT_ID
        );
        await loadLedger();
        return true;
      } catch {
        setError("No pudimos registrar el movimiento de stock.");
        return false;
      } finally {
        setSaving(false);
      }
    },
    [loadLedger]
  );

  const removeEntry = useCallback(
    async (id) => {
      try {
        setSaving(true);
        setError("");
        await deleteStockEntry(id, TENANT_ID);
        await loadLedger();
      } catch {
        setError("No pudimos eliminar el movimiento de stock.");
      } finally {
        setSaving(false);
      }
    },
    [loadLedger]
  );

  return {
    entries,
    levels,
    products,
    loading,
    saving,
    error,
    refresh: loadLedger,
    addEntry,
    removeEntry
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
