import { useEffect, useState, type FormEvent } from "react";
import { learningApi } from "../api/learningApi.ts";
import { ApiError } from "../api/client.ts";
import type { LearningProject, LearningSession } from "../types/learning.ts";

function formatMinutes(minutes: number): string {
  if (minutes < 60) return `${minutes} Min.`;
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  return m > 0 ? `${h} Std. ${m} Min.` : `${h} Std.`;
}

export default function LearningPage() {
  const [projects, setProjects] = useState<LearningProject[]>([]);
  const [sessions, setSessions] = useState<Record<number, LearningSession[]>>({});
  const [expandedProjectId, setExpandedProjectId] = useState<number | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [topic, setTopic] = useState("");

  const [sessionDuration, setSessionDuration] = useState("");
  const [sessionNotes, setSessionNotes] = useState("");
  const [sessionDate, setSessionDate] = useState(
    new Date().toISOString().split("T")[0],
  );
  const [addingSessionFor, setAddingSessionFor] = useState<number | null>(null);

  useEffect(() => {
    learningApi
      .getAll()
      .then(setProjects)
      .catch((err) =>
        setError(err instanceof ApiError ? err.message : "Projekte konnten nicht geladen werden."),
      )
      .finally(() => setIsLoading(false));
  }, []);

  async function handleCreateProject(event: FormEvent) {
    event.preventDefault();
    setError(null);
    try {
      const project = await learningApi.create({
        title,
        description: description || undefined,
        topic,
      });
      setProjects((prev) => [project, ...prev]);
      setTitle("");
      setDescription("");
      setTopic("");
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Projekt konnte nicht erstellt werden.");
    }
  }

  async function handleDeleteProject(id: number) {
    try {
      await learningApi.delete(id);
      setProjects((prev) => prev.filter((p) => p.id !== id));
      setSessions((prev) => {
        const updated = { ...prev };
        delete updated[id];
        return updated;
      });
      if (expandedProjectId === id) setExpandedProjectId(null);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Projekt konnte nicht gelöscht werden.");
    }
  }

  async function handleExpandProject(id: number) {
    if (expandedProjectId === id) {
      setExpandedProjectId(null);
      return;
    }
    setExpandedProjectId(id);
    if (!sessions[id]) {
      try {
        const result = await learningApi.getSessions(id);
        setSessions((prev) => ({ ...prev, [id]: result }));
      } catch (err) {
        setError(err instanceof ApiError ? err.message : "Sessions konnten nicht geladen werden.");
      }
    }
  }

  async function handleAddSession(projectId: number) {
    if (!sessionDuration) return;
    setError(null);
    try {
      const session = await learningApi.addSession(projectId, {
        durationMinutes: parseInt(sessionDuration),
        notes: sessionNotes || undefined,
        date: sessionDate,
      });
      setSessions((prev) => ({
        ...prev,
        [projectId]: [...(prev[projectId] ?? []), session],
      }));
      setProjects((prev) =>
        prev.map((p) =>
          p.id === projectId
            ? { ...p, totalMinutes: p.totalMinutes + parseInt(sessionDuration) }
            : p,
        ),
      );
      setSessionDuration("");
      setSessionNotes("");
      setAddingSessionFor(null);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Session konnte nicht hinzugefügt werden.");
    }
  }

  async function handleDeleteSession(projectId: number, sessionId: number, durationMinutes: number) {
    try {
      await learningApi.deleteSession(projectId, sessionId);
      setSessions((prev) => ({
        ...prev,
        [projectId]: prev[projectId]?.filter((s) => s.id !== sessionId) ?? [],
      }));
      setProjects((prev) =>
        prev.map((p) =>
          p.id === projectId
            ? { ...p, totalMinutes: p.totalMinutes - durationMinutes }
            : p,
        ),
      );
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Session konnte nicht gelöscht werden.");
    }
  }

  return (
    <div>
      <h1 className="text-2xl font-semibold mb-8">Learning</h1>

      <form onSubmit={handleCreateProject} className="border border-border p-5 mb-10 space-y-3">
        <div>
          <label htmlFor="title" className="block text-sm mb-1">Titel</label>
          <input
            id="title"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            required
            className="w-full border border-border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-accent"
          />
        </div>
        <div>
          <label htmlFor="topic" className="block text-sm mb-1">Thema</label>
          <input
            id="topic"
            value={topic}
            onChange={(e) => setTopic(e.target.value)}
            required
            className="w-full border border-border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-accent"
          />
        </div>
        <div>
          <label htmlFor="description" className="block text-sm mb-1">Beschreibung</label>
          <textarea
            id="description"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            rows={2}
            className="w-full border border-border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-accent"
          />
        </div>
        <button
          type="submit"
          className="bg-accent text-white rounded px-4 py-2"
        >
          Projekt anlegen
        </button>
      </form>

      {error && <p className="text-sm text-red-700 mb-4">{error}</p>}
      {isLoading && <p className="text-ink-muted">Lädt…</p>}

      {!isLoading && projects.length === 0 && (
        <p className="text-sm text-ink-muted">Noch keine Lernprojekte angelegt.</p>
      )}

      <ul className="divide-y divide-border border-t border-b border-border">
        {projects.map((project) => (
          <li key={project.id}>
            <div className="py-4 flex items-start justify-between gap-4">
              <div>
                <button
                  onClick={() => handleExpandProject(project.id)}
                  className="font-medium text-left hover:text-accent"
                >
                  {project.title}
                </button>
                <p className="text-sm text-ink-muted mt-1">
                  {project.topic} · {formatMinutes(project.totalMinutes)}
                </p>
                {project.description && (
                  <p className="text-sm text-ink-muted">{project.description}</p>
                )}
              </div>
              <div className="flex gap-3 shrink-0">
                <button
                  onClick={() =>
                    setAddingSessionFor(
                      addingSessionFor === project.id ? null : project.id,
                    )
                  }
                  className="text-sm text-accent underline"
                >
                  {addingSessionFor === project.id ? "Abbrechen" : "Session eintragen"}
                </button>
                <button
                  onClick={() => handleDeleteProject(project.id)}
                  className="text-sm text-ink-muted hover:text-red-700 underline"
                >
                  Löschen
                </button>
              </div>
            </div>

            {addingSessionFor === project.id && (
              <div className="pb-4 flex gap-2 flex-wrap">
                <input
                  type="number"
                  placeholder="Minuten"
                  value={sessionDuration}
                  onChange={(e) => setSessionDuration(e.target.value)}
                  className="w-28 border border-border rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-accent"
                />
                <input
                  type="date"
                  value={sessionDate}
                  onChange={(e) => setSessionDate(e.target.value)}
                  className="border border-border rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-accent"
                />
                <input
                  placeholder="Notizen (optional)"
                  value={sessionNotes}
                  onChange={(e) => setSessionNotes(e.target.value)}
                  className="flex-1 border border-border rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-accent"
                />
                <button
                  onClick={() => handleAddSession(project.id)}
                  className="bg-accent text-white rounded px-4 py-2 text-sm"
                >
                  Speichern
                </button>
              </div>
            )}

            {expandedProjectId === project.id && (
              <div className="pb-4">
                {!sessions[project.id] || sessions[project.id].length === 0 ? (
                  <p className="text-sm text-ink-muted">Noch keine Sessions.</p>
                ) : (
                  <ul className="space-y-2">
                    {sessions[project.id].map((session) => (
                      <li
                        key={session.id}
                        className="flex items-start justify-between gap-4 text-sm"
                      >
                        <div>
                          <span className="text-ink-muted">{session.date}</span>
                          {" · "}
                          <span>{formatMinutes(session.durationMinutes)}</span>
                          {session.notes && (
                            <span className="text-ink-muted"> · {session.notes}</span>
                          )}
                        </div>
                        <button
                          onClick={() =>
                            handleDeleteSession(project.id, session.id, session.durationMinutes)
                          }
                          className="text-ink-muted hover:text-red-700 underline shrink-0"
                        >
                          Löschen
                        </button>
                      </li>
                    ))}
                  </ul>
                )}
              </div>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}