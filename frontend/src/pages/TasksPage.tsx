import { useEffect, useState, type FormEvent } from "react";
import { taskApi } from "../api/taskApi";
import { ApiError } from "../api/client";
import type { Task } from "../types/task";

export default function TasksPage() {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [dueDate, setDueDate] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    taskApi
      .getAll()
      .then(setTasks)
      .catch((err) =>
        setError(err instanceof ApiError ? err.message : "Aufgaben konnten nicht geladen werden."),
      )
      .finally(() => setIsLoading(false));
  }, []);

  async function handleCreate(event: FormEvent) {
    event.preventDefault();
    setIsSubmitting(true);
    setError(null);

    try {
      const newTask = await taskApi.create({
        title,
        description: description || undefined,
        dueDate: dueDate || undefined,
      });
      setTasks((prev) => [...prev, newTask]);
      setTitle("");
      setDescription("");
      setDueDate("");
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Aufgabe konnte nicht erstellt werden.");
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleToggleComplete(task: Task) {
    try {
      const updated = await taskApi.update(task.id, {
        title: task.title,
        description: task.description ?? undefined,
        dueDate: task.dueDate ?? undefined,
        completed: !task.completed,
      });
      setTasks((prev) => prev.map((g) => (g.id === updated.id ? updated : g)));
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Aufgabe konnte nicht aktualisiert werden.");
    }
  }

  async function handleDelete(id: number) {
    try {
      await taskApi.delete(id);
      setTasks((prev) => prev.filter((g) => g.id !== id));
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Aufgabe konnte nicht gelöscht werden.");
    }
  }

  return (
    <div>
      <h1 className="text-2xl font-semibold mb-8">Aufgaben</h1>

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
            Tätigkeit
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
            Fälligkeitsdatum
          </label>
          <input
            id="targetDate"
            type="date"
            value={dueDate}
            onChange={(e) => setDueDate(e.target.value)}
            className="border border-border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-accent"
          />
        </div>
        <button
          type="submit"
          disabled={isSubmitting}
          className="bg-accent text-white rounded px-4 py-2 disabled:opacity-50"
        >
          {isSubmitting ? "Speichert…" : "Aufgabe hinzufügen"}
        </button>
      </form>

      {error && <p className="text-sm text-red-700 mb-4">{error}</p>}
      {isLoading && <p className="text-ink-muted">Lädt…</p>}

      {!isLoading && tasks.length === 0 && (
        <p className="text-sm text-ink-muted">Noch keine Aufgaben angelegt.</p>
      )}

      <ul className="divide-y divide-border border-t border-b border-border">
        {tasks.map((task) => (
          <li key={task.id} className="py-4 flex items-start justify-between gap-4">
            <div className="flex items-start gap-3">
              <input type="checkbox" checked={task.completed} onChange={() => handleToggleComplete(task)} className="mt-1 accent-accent"/>
              <div>

              </div>
              <p className={task.completed ? "line-through text-ink-muted" : ""}>{task.title}</p>
                {task.description && (
                   <p className="text-sm text-ink-muted mt-1">
                   {task.description}</p>     
                )}
                {task.dueDate && (
                   <p className="text-xs text-ink-muted mt-1">Fällig: {task.dueDate}</p>
                )}
            </div>
              <button
                onClick={() => handleDelete(task.id)}
                className="text-sm text-ink-muted hover:text-red-700 underline"
              >
                Löschen
              </button>
          </li>
        ))}
      </ul>
    </div>
  );
}