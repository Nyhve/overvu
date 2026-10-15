import { useEffect, useState, type FormEvent } from "react";
import { habitApi } from "../api/habitApi.ts";
import { ApiError } from "../api/client.ts";
import type { Habit, HabitEntry, HabitFrequency } from "../types/habit.ts";

export default function HabitsPage() {
  const [habits, setHabits] = useState<Habit[]>([]);
  const [entries, setEntries] = useState<Record<number, HabitEntry[]>>({});
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [frequency, setFrequency] = useState<HabitFrequency>("DAILY");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const today = new Date().toISOString().split("T")[0];

  useEffect(() => {
    Promise.all([habitApi.getAll(), habitApi.getTodaysEntries()])
        .then(([habitsResult, todaysEntries]) => {
        setHabits(habitsResult);
        const entriesByHabit: Record<number, HabitEntry[]> = {};
        for (const entry of todaysEntries) {
            entriesByHabit[entry.habitId] = [...(entriesByHabit[entry.habitId] ?? []), entry];
        }
        setEntries(entriesByHabit);
        })
        .catch((err) =>
        setError(err instanceof ApiError ? err.message : "Habits konnten nicht geladen werden."),
        )
        .finally(() => setIsLoading(false));
    }, []);

  async function handleCreate(event: FormEvent) {
    event.preventDefault();
    setIsSubmitting(true);
    setError(null);

    try {
      const newHabit = await habitApi.create({
        name,
        description: description || undefined,
        frequency,
      });
      setHabits((prev) => [...prev, newHabit]);
      setName("");
      setDescription("");
      setFrequency("DAILY");
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Habit konnte nicht erstellt werden.");
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleCompleteToday(habit: Habit) {
    try {
      const entry = await habitApi.completeToday(habit.id);
      setEntries((prev) => ({
        ...prev,
        [habit.id]: [...(prev[habit.id] ?? []), entry],
      }));
    } catch (err) {
      if (err instanceof ApiError && err.status === 409) {
        setError("Diesen Habit hast du heute bereits abgehakt.");
      } else {
        setError("Habit konnte nicht abgehakt werden.");
      }
    }
  }

  async function handleDelete(id: number) {
    try {
      await habitApi.delete(id);
      setHabits((prev) => prev.filter((h) => h.id !== id));
      setEntries((prev) => {
        const updated = { ...prev };
        delete updated[id];
        return updated;
      });
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Habit konnte nicht gelöscht werden.");
    }
  }

  function isCompletedToday(habitId: number): boolean {
    return (entries[habitId] ?? []).some((e) => e.date === today);
  }

  return (
    <div>
      <h1 className="text-2xl font-semibold mb-8">Habits</h1>

      <form onSubmit={handleCreate} className="border border-border p-5 mb-10 space-y-3">
        <div>
          <label htmlFor="name" className="block text-sm mb-1">
            Name
          </label>
          <input
            id="name"
            value={name}
            onChange={(e) => setName(e.target.value)}
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
          <label htmlFor="frequency" className="block text-sm mb-1">
            Häufigkeit
          </label>
          <select
            id="frequency"
            value={frequency}
            onChange={(e) => setFrequency(e.target.value as HabitFrequency)}
            className="border border-border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-accent"
          >
            <option value="DAILY">Täglich</option>
            <option value="WEEKLY">Wöchentlich</option>
          </select>
        </div>
        <button
          type="submit"
          disabled={isSubmitting}
          className="bg-accent text-white rounded px-4 py-2 disabled:opacity-50"
        >
          {isSubmitting ? "Speichert…" : "Habit hinzufügen"}
        </button>
      </form>

      {error && <p className="text-sm text-red-700 mb-4">{error}</p>}
      {isLoading && <p className="text-ink-muted">Lädt…</p>}

      {!isLoading && habits.length === 0 && (
        <p className="text-sm text-ink-muted">Noch keine Habits angelegt.</p>
      )}

      <ul className="divide-y divide-border border-t border-b border-border">
        {habits.map((habit) => (
          <li key={habit.id} className="py-4 flex items-start justify-between gap-4">
            <div>
              <p className={isCompletedToday(habit.id) ? "line-through text-ink-muted" : ""}>
                {habit.name}
              </p>
              {habit.description && (
                <p className="text-sm text-ink-muted mt-1">{habit.description}</p>
              )}
              <p className="text-xs text-ink-muted mt-1">
                {habit.frequency === "DAILY" ? "Täglich" : "Wöchentlich"}
              </p>
            </div>
            <div className="flex gap-3 shrink-0">
              {!isCompletedToday(habit.id) && (
                <button
                  onClick={() => handleCompleteToday(habit)}
                  className="text-sm text-accent underline"
                >
                  Heute abhaken
                </button>
              )}
              {isCompletedToday(habit.id) && (
                <span className="text-sm text-ink-muted">✓ Heute erledigt</span>
              )}
              <button
                onClick={() => handleDelete(habit.id)}
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