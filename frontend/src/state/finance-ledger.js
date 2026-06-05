import { useCallback, useEffect, useMemo, useState } from "react";
import {
  createFinanceEntry,
  deleteFinanceEntry,
  fetchFinanceEntries,
  fetchFinanceSummary
} from "../services/api";

export const FINANCE_EXPENSE_CATEGORIES = [
  "insumos",
  "sueldos",
  "alquileres",
  "transporte",
  "otros gastos"
];

const TENANT_ID = 1;

export function useFinanceLedger() {
  const [records, setRecords] = useState([]);
  const [summary, setSummary] = useState(defaultSummary());
  const [period, setPeriod] = useState(getCurrentPeriod);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  const loadLedger = useCallback(async () => {
    try {
      setLoading(true);
      setError("");
      const params = toPeriodParams(period);
      const [entries, financeSummary] = await Promise.all([
        fetchFinanceEntries(TENANT_ID, params),
        fetchFinanceSummary(TENANT_ID, params)
      ]);
      setRecords(Array.isArray(entries) ? entries : []);
      setSummary(normalizeSummary(financeSummary));
    } catch {
      setError("No pudimos cargar el módulo de finanzas.");
    } finally {
      setLoading(false);
    }
  }, [period]);

  useEffect(() => {
    loadLedger();
  }, [loadLedger]);

  const addRecord = useCallback(
    async (input) => {
      const amount = Number(input.amount);
      if (!Number.isFinite(amount) || amount <= 0) {
        return false;
      }

      try {
        setSaving(true);
        setError("");
        await createFinanceEntry(
          {
            type: String(input.type || "").toUpperCase(),
            amount: roundAmount(amount),
            category: normalizeText(input.category),
            note: normalizeText(input.note),
            entryDate: input.entryDate || todayIsoDate()
          },
          TENANT_ID
        );
        await loadLedger();
        return true;
      } catch {
        setError("No pudimos registrar el movimiento financiero.");
        return false;
      } finally {
        setSaving(false);
      }
    },
    [loadLedger]
  );

  const removeRecord = useCallback(
    async (id) => {
      try {
        setSaving(true);
        setError("");
        await deleteFinanceEntry(id, TENANT_ID);
        await loadLedger();
      } catch {
        setError("No pudimos eliminar el movimiento financiero.");
      } finally {
        setSaving(false);
      }
    },
    [loadLedger]
  );

  const expenseByCategory = useMemo(() => {
    const remote = summary.expenseByCategory || [];
    return FINANCE_EXPENSE_CATEGORIES.map((category) => {
      const found = remote.find((item) => normalizeText(item.category) === category);
      return {
        category,
        total: Number(found?.total || 0)
      };
    });
  }, [summary.expenseByCategory]);

  return {
    records,
    summary,
    expenseByCategory,
    period,
    setPeriod,
    loading,
    saving,
    error,
    refresh: loadLedger,
    addRecord,
    removeRecord
  };
}

function normalizeSummary(payload) {
  const fallback = defaultSummary();
  if (!payload) return fallback;
  return {
    systemSales: Number(payload.systemSales || 0),
    salesOrdersCount: Number(payload.salesOrdersCount || 0),
    manualIncome: Number(payload.manualIncome || 0),
    expenses: Number(payload.expenses || 0),
    totalIncome: Number(payload.totalIncome || 0),
    netResult: Number(payload.netResult || 0),
    expenseByCategory: Array.isArray(payload.expenseByCategory) ? payload.expenseByCategory : []
  };
}

function defaultSummary() {
  return {
    systemSales: 0,
    salesOrdersCount: 0,
    manualIncome: 0,
    expenses: 0,
    totalIncome: 0,
    netResult: 0,
    expenseByCategory: []
  };
}

function getCurrentPeriod() {
  const now = new Date();
  return {
    month: String(now.getMonth() + 1).padStart(2, "0"),
    year: String(now.getFullYear())
  };
}

function toPeriodParams(period) {
  return {
    month: Number(period?.month),
    year: Number(period?.year)
  };
}

function normalizeText(value) {
  if (value == null) return null;
  const normalized = String(value).trim().toLowerCase();
  return normalized.length ? normalized : null;
}

function roundAmount(amount) {
  return Math.round(Number(amount) * 100) / 100;
}

function todayIsoDate() {
  return new Date().toISOString().slice(0, 10);
}
