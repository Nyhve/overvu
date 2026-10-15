import { useEffect, useState, type FormEvent } from "react";
import { financeApi } from "../api/financeApi.ts";
import { ApiError } from "../api/client.ts";
import type {
  Transaction,
  TransactionCategory,
  TransactionType,
  MonthlyStats,
} from "../types/finance.ts";

const CATEGORY_LABELS: Record<TransactionCategory, string> = {
  SALARY: "Gehalt",
  FREELANCE: "Freiberuflich",
  OTHER_INCOME: "Sonstige Einnahmen",
  FOOD: "Lebensmittel",
  TRANSPORT: "Transport",
  HOUSING: "Wohnen",
  HEALTH: "Gesundheit",
  ENTERTAINMENT: "Freizeit",
  SHOPPING: "Einkaufen",
  EDUCATION: "Bildung",
  OTHER_EXPENSE: "Sonstige Ausgaben",
};

const INCOME_CATEGORIES: TransactionCategory[] = [
  "SALARY", "FREELANCE", "OTHER_INCOME",
];

const EXPENSE_CATEGORIES: TransactionCategory[] = [
  "FOOD", "TRANSPORT", "HOUSING", "HEALTH",
  "ENTERTAINMENT", "SHOPPING", "EDUCATION", "OTHER_EXPENSE",
];

function formatEuro(amount: number): string {
  return new Intl.NumberFormat("de-DE", {
    style: "currency",
    currency: "EUR",
  }).format(amount);
}

export default function FinancePage() {
  const now = new Date();
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [stats, setStats] = useState<MonthlyStats | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedYear, setSelectedYear] = useState(now.getFullYear());
  const [selectedMonth, setSelectedMonth] = useState(now.getMonth() + 1);

  const [amount, setAmount] = useState("");
  const [description, setDescription] = useState("");
  const [type, setType] = useState<TransactionType>("EXPENSE");
  const [category, setCategory] = useState<TransactionCategory>("FOOD");
  const [date, setDate] = useState(now.toISOString().split("T")[0]);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    Promise.all([
      financeApi.getAll(),
      financeApi.getMonthlyStats(selectedYear, selectedMonth),
    ])
      .then(([t, s]) => {
        setTransactions(t);
        setStats(s);
      })
      .catch((err) =>
        setError(err instanceof ApiError ? err.message : "Daten konnten nicht geladen werden."),
      )
      .finally(() => setIsLoading(false));
  }, [selectedYear, selectedMonth]);

  const availableCategories = type === "INCOME" ? INCOME_CATEGORIES : EXPENSE_CATEGORIES;

  function handleTypeChange(newType: TransactionType) {
    setType(newType);
    setCategory(newType === "INCOME" ? "SALARY" : "FOOD");
  }

  async function handleCreate(event: FormEvent) {
    event.preventDefault();
    setIsSubmitting(true);
    setError(null);
    try {
      const transaction = await financeApi.create({
        amount: parseFloat(amount),
        description,
        category,
        type,
        date,
      });
      setTransactions((prev) => [transaction, ...prev]);
      if (
        transaction.date.startsWith(
          `${selectedYear}-${String(selectedMonth).padStart(2, "0")}`,
        )
      ) {
        financeApi.getMonthlyStats(selectedYear, selectedMonth).then(setStats);
      }
      setAmount("");
      setDescription("");
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Transaktion konnte nicht erstellt werden.");
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleDelete(id: number) {
    try {
      await financeApi.delete(id);
      setTransactions((prev) => prev.filter((t) => t.id !== id));
      financeApi.getMonthlyStats(selectedYear, selectedMonth).then(setStats);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Transaktion konnte nicht gelöscht werden.");
    }
  }

  const monthNames = [
    "Januar", "Februar", "März", "April", "Mai", "Juni",
    "Juli", "August", "September", "Oktober", "November", "Dezember",
  ];

  return (
    <div>
      <h1 className="text-2xl font-semibold mb-8">Finanzen</h1>

      {error && <p className="text-sm text-red-700 mb-4">{error}</p>}

      <form onSubmit={handleCreate} className="border border-border p-5 mb-8 space-y-3">
        <div className="flex gap-3">
          <button
            type="button"
            onClick={() => handleTypeChange("EXPENSE")}
            className={`flex-1 rounded px-3 py-2 text-sm border ${
              type === "EXPENSE"
                ? "bg-accent text-white border-accent"
                : "border-border"
            }`}
          >
            Ausgabe
          </button>
          <button
            type="button"
            onClick={() => handleTypeChange("INCOME")}
            className={`flex-1 rounded px-3 py-2 text-sm border ${
              type === "INCOME"
                ? "bg-accent text-white border-accent"
                : "border-border"
            }`}
          >
            Einnahme
          </button>
        </div>
        <div className="grid grid-cols-2 gap-3">
          <div>
            <label htmlFor="amount" className="block text-sm mb-1">Betrag (€)</label>
            <input
              id="amount"
              type="number"
              step="0.01"
              min="0.01"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              required
              className="w-full border border-border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-accent"
            />
          </div>
          <div>
            <label htmlFor="date" className="block text-sm mb-1">Datum</label>
            <input
              id="date"
              type="date"
              value={date}
              onChange={(e) => setDate(e.target.value)}
              className="w-full border border-border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-accent"
            />
          </div>
        </div>
        <div>
          <label htmlFor="description" className="block text-sm mb-1">Beschreibung</label>
          <input
            id="description"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            required
            className="w-full border border-border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-accent"
          />
        </div>
        <div>
          <label htmlFor="category" className="block text-sm mb-1">Kategorie</label>
          <select
            id="category"
            value={category}
            onChange={(e) => setCategory(e.target.value as TransactionCategory)}
            className="w-full border border-border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-accent"
          >
            {availableCategories.map((cat) => (
              <option key={cat} value={cat}>{CATEGORY_LABELS[cat]}</option>
            ))}
          </select>
        </div>
        <button
          type="submit"
          disabled={isSubmitting}
          className="bg-accent text-white rounded px-4 py-2 disabled:opacity-50"
        >
          {isSubmitting ? "Speichert…" : "Hinzufügen"}
        </button>
      </form>

      <div className="flex items-center gap-3 mb-6">
        <select
          value={selectedMonth}
          onChange={(e) => setSelectedMonth(Number(e.target.value))}
          className="border border-border rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-accent"
        >
          {monthNames.map((name, i) => (
            <option key={i + 1} value={i + 1}>{name}</option>
          ))}
        </select>
        <select
          value={selectedYear}
          onChange={(e) => setSelectedYear(Number(e.target.value))}
          className="border border-border rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-accent"
        >
          {[2024, 2025, 2026, 2027].map((y) => (
            <option key={y} value={y}>{y}</option>
          ))}
        </select>
      </div>

      {stats && (
        <div className="grid grid-cols-3 gap-4 mb-8">
          <div className="border border-border p-4">
            <p className="text-2xl font-semibold text-green-700">
              {formatEuro(stats.totalIncome)}
            </p>
            <p className="text-sm text-ink-muted mt-1">Einnahmen</p>
          </div>
          <div className="border border-border p-4">
            <p className="text-2xl font-semibold text-red-700">
              {formatEuro(stats.totalExpenses)}
            </p>
            <p className="text-sm text-ink-muted mt-1">Ausgaben</p>
          </div>
          <div className="border border-border p-4">
            <p className={`text-2xl font-semibold ${stats.balance >= 0 ? "text-green-700" : "text-red-700"}`}>
              {formatEuro(stats.balance)}
            </p>
            <p className="text-sm text-ink-muted mt-1">Bilanz</p>
          </div>
        </div>
      )}

      {isLoading && <p className="text-ink-muted">Lädt…</p>}

      {!isLoading && transactions.length === 0 && (
        <p className="text-sm text-ink-muted">Noch keine Transaktionen.</p>
      )}

      <ul className="divide-y divide-border border-t border-b border-border">
        {transactions.map((transaction) => (
          <li key={transaction.id} className="py-3 flex items-center justify-between gap-4">
            <div>
              <p>{transaction.description}</p>
              <p className="text-xs text-ink-muted mt-1">
                {CATEGORY_LABELS[transaction.category]} · {transaction.date}
              </p>
            </div>
            <div className="flex items-center gap-4 shrink-0">
              <span className={`font-medium ${transaction.type === "INCOME" ? "text-green-700" : "text-red-700"}`}>
                {transaction.type === "INCOME" ? "+" : "-"}{formatEuro(transaction.amount)}
              </span>
              <button
                onClick={() => handleDelete(transaction.id)}
                className="text-sm text-ink-muted hover:text-red-700 underline"
              >
                Löschen
              </button>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
}