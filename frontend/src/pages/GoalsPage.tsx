import { useEffect, useState, type FormEvent } from "react";
import { goalApi } from "../api/goalApi";
import { ApiError } from "../api/client";
import type { Goal } from "../types/goal";

export default function GoalsPage() {
  const [goals, setGoals] = useState<Goal[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [targetDate, setTargetDate] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    goalApi
      .getAll()
      .then(setGoals)
      .catch((err) =>
        setError(err instanceof ApiError ? err.message : "Ziele konnten nicht geladen werden."),
      )
      .finally(() => setIsLoading(false));
  }, []);

  async function handleCreate(event: FormEvent) {
    event.preventDefault();
    setIsSubmitting(true);
    setError(null);

    try {
      const newGoal = await goalApi.create({
        title,
        description: description || undefined,
        targetDate: targetDate || undefined,
      });
      setGoals((prev) => [...prev, newGoal]);
      setTitle("");
      setDescription("");
      setTargetDate("");
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Ziel konnte nicht erstellt werden.");
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleComplete(goal: Goal) {
    try {
      const updated = await goalApi.update(goal.id, {
        title: goal.title,
        description: goal.description ?? undefined,
        targetDate: goal.targetDate ?? undefined,
        status: "COMPLETED",
      });
      setGoals((prev) => prev.map((g) => (g.id === updated.id ? updated : g)));
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Ziel konnte nicht aktualisiert werden.");
    }
  }

  async function handleDelete(id: number) {
    try {
      await goalApi.delete(id);
      setGoals((prev) => prev.filter((g) => g.id !== id));
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Ziel konnte nicht gelöscht werden.");
    }
  }

  return (
    <div>
      <h1 className="text-2xl font-semibold mb-8">Ziele</h1>

      <form onSubmit={handleCreate} className="border border-border p-5 mb-10 space-y-3">
        <div>
          <label htmlFor="title" className="block text-sm mb-1">
            Titel
          </label>
          <input
            id="title"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            required
            className="w-full border border-border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-accent"
          />
        </div>
        <div>
          <label htmlFor="description" className="block text-sm mb-1">
            Beschreibung
          </label>
          <textarea
            id="description"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            rows={2}
            className="w-full border border-border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-accent"
          />
        </div>
        <div>
          <label htmlFor="targetDate" className="block text-sm mb-1">
            Zieldatum
          </label>
          <input
            id="targetDate"
            type="date"
            value={targetDate}
            onChange={(e) => setTargetDate(e.target.value)}
            className="border border-border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-accent"
          />
        </div>
        <button
          type="submit"
          disabled={isSubmitting}
          className="bg-accent text-white rounded px-4 py-2 disabled:opacity-50"
        >
          {isSubmitting ? "Speichert…" : "Ziel hinzufügen"}
        </button>
      </form>

      {error && <p className="text-sm text-red-700 mb-4">{error}</p>}
      {isLoading && <p className="text-ink-muted">Lädt…</p>}

      {!isLoading && goals.length === 0 && (
        <p className="text-sm text-ink-muted">Noch keine Ziele angelegt.</p>
      )}

      <ul className="divide-y divide-border border-t border-b border-border">
        {goals.map((goal) => (
          <li key={goal.id} className="py-4 flex items-start justify-between gap-4">
            <div>
              <p className={goal.status === "COMPLETED" ? "line-through text-ink-muted" : ""}>
                {goal.title}
              </p>
              {goal.description && <p className="text-sm text-ink-muted mt-1">{goal.description}</p>}
              <p className="text-xs text-ink-muted mt-1">
                {goal.status === "ACTIVE" ? "Aktiv" : goal.status === "COMPLETED" ? "Erledigt" : "Archiviert"}
                {goal.targetDate && ` · Ziel: ${goal.targetDate}`}
              </p>
            </div>
            <div className="flex gap-3 shrink-0">
              {goal.status === "ACTIVE" && (
                <button onClick={() => handleComplete(goal)} className="text-sm text-accent underline">
                  Erledigt
                </button>
              )}
              <button
                onClick={() => handleDelete(goal.id)}
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