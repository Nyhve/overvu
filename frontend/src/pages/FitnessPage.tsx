import { useEffect, useState, type FormEvent } from "react";
import { fitnessApi } from "../api/fitnessApi.ts";
import { ApiError } from "../api/client.ts";
import type { Exercise, MuscleGroup, Workout } from "../types/fitness.ts";

const MUSCLE_GROUP_LABELS: Record<MuscleGroup, string> = {
  CHEST: "Brust",
  BACK: "Rücken",
  LEGS: "Beine",
  SHOULDERS: "Schultern",
  ARMS: "Arme",
  CORE: "Core",
  FULL_BODY: "Ganzkörper",
};

export default function FitnessPage() {
  const [workouts, setWorkouts] = useState<Workout[]>([]);
  const [exercises, setExercises] = useState<Exercise[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activeWorkoutId, setActiveWorkoutId] = useState<number | null>(null);

  const [workoutName, setWorkoutName] = useState("");
  const [workoutDate, setWorkoutDate] = useState(
    new Date().toISOString().split("T")[0],
  );

  const [selectedExerciseId, setSelectedExerciseId] = useState<number | null>(null);
  const [newExerciseName, setNewExerciseName] = useState("");
  const [newExerciseMuscle, setNewExerciseMuscle] = useState<MuscleGroup>("CHEST");

  const [setWeight, setSetWeight] = useState("");
  const [setReps, setSetReps] = useState("");
  const [activeExerciseId, setActiveExerciseId] = useState<number | null>(null);

  const today = new Date().toISOString().split("T")[0];

  useEffect(() => {
    Promise.all([fitnessApi.getWorkouts(), fitnessApi.getExercises()])
      .then(([w, e]) => {
        setWorkouts(w);
        setExercises(e);
        const active = w.find((wo) => !wo.completed);
        if (active) setActiveWorkoutId(active.id);
      })
      .catch((err) =>
        setError(err instanceof ApiError ? err.message : "Daten konnten nicht geladen werden."),
      )
      .finally(() => setIsLoading(false));
  }, []);

  const activeWorkout = workouts.find((w) => w.id === activeWorkoutId) ?? null;

  async function handleCreateWorkout(event: FormEvent) {
    event.preventDefault();
    try {
      const workout = await fitnessApi.createWorkout({
        name: workoutName,
        date: workoutDate,
      });
      setWorkouts((prev) => [workout, ...prev]);
      setActiveWorkoutId(workout.id);
      setWorkoutName("");
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Workout konnte nicht erstellt werden.");
    }
  }

  async function handleAddExercise() {
    if (!activeWorkoutId || !selectedExerciseId) return;
    try {
      const updated = await fitnessApi.addExercise(activeWorkoutId, selectedExerciseId);
      setWorkouts((prev) => prev.map((w) => (w.id === updated.id ? updated : w)));
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Übung konnte nicht hinzugefügt werden.");
    }
  }

  async function handleCreateExercise(event: FormEvent) {
    event.preventDefault();
    try {
      const exercise = await fitnessApi.createExercise(newExerciseName, newExerciseMuscle);
      setExercises((prev) => [...prev, exercise]);
      setSelectedExerciseId(exercise.id);
      setNewExerciseName("");
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Übung konnte nicht angelegt werden.");
    }
  }

  async function handleAddSet(workoutExerciseId: number) {
    if (!activeWorkoutId || !setWeight || !setReps) return;
    try {
      const updated = await fitnessApi.addSet(activeWorkoutId, workoutExerciseId, {
        weightKg: parseFloat(setWeight),
        reps: parseInt(setReps),
      });
      setWorkouts((prev) => prev.map((w) => (w.id === updated.id ? updated : w)));
      setSetWeight("");
      setSetReps("");
      setActiveExerciseId(null);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Satz konnte nicht hinzugefügt werden.");
    }
  }

  async function handleCompleteWorkout() {
    if (!activeWorkoutId) return;
    try {
      const updated = await fitnessApi.completeWorkout(activeWorkoutId);
      setWorkouts((prev) => prev.map((w) => (w.id === updated.id ? updated : w)));
      setActiveWorkoutId(null);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Workout konnte nicht abgeschlossen werden.");
    }
  }

  async function handleDeleteWorkout(id: number) {
    try {
      await fitnessApi.deleteWorkout(id);
      setWorkouts((prev) => prev.filter((w) => w.id !== id));
      if (activeWorkoutId === id) setActiveWorkoutId(null);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Workout konnte nicht gelöscht werden.");
    }
  }

  return (
    <div>
      <h1 className="text-2xl font-semibold mb-8">Fitness</h1>

      {error && <p className="text-sm text-red-700 mb-4">{error}</p>}
      {isLoading && <p className="text-ink-muted">Lädt…</p>}

      {!isLoading && (
        <div className="space-y-10">
          {!activeWorkout && (
            <form onSubmit={handleCreateWorkout} className="border border-border p-5 space-y-3">
              <h2 className="text-sm font-medium text-ink-muted">Neues Workout starten</h2>
              <div>
                <label htmlFor="wName" className="block text-sm mb-1">Name</label>
                <input
                  id="wName"
                  value={workoutName}
                  onChange={(e) => setWorkoutName(e.target.value)}
                  required
                  className="w-full border border-border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-accent"
                />
              </div>
              <div>
                <label htmlFor="wDate" className="block text-sm mb-1">Datum</label>
                <input
                  id="wDate"
                  type="date"
                  value={workoutDate}
                  onChange={(e) => setWorkoutDate(e.target.value)}
                  className="border border-border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-accent"
                />
              </div>
              <button
                type="submit"
                className="bg-accent text-white rounded px-4 py-2"
              >
                Workout starten
              </button>
            </form>
          )}

          {activeWorkout && (
            <div className="border border-border p-5 space-y-6">
              <div className="flex items-center justify-between">
                <div>
                  <h2 className="font-medium">{activeWorkout.name}</h2>
                  <p className="text-sm text-ink-muted">{activeWorkout.date}</p>
                </div>
                <button
                  onClick={handleCompleteWorkout}
                  className="bg-accent text-white rounded px-4 py-2 text-sm"
                >
                  Workout abschließen
                </button>
              </div>

              <div className="space-y-4">
                {activeWorkout.exercises.map((we) => (
                  <div key={we.id} className="border border-border p-4">
                    <div className="flex items-center justify-between mb-3">
                      <div>
                        <p className="font-medium">{we.exercise.name}</p>
                        <p className="text-xs text-ink-muted">
                          {MUSCLE_GROUP_LABELS[we.exercise.muscleGroup]}
                        </p>
                      </div>
                      <button
                        onClick={() => setActiveExerciseId(activeExerciseId === we.id ? null : we.id)}
                        className="text-sm text-accent underline"
                      >
                        {activeExerciseId === we.id ? "Abbrechen" : "Satz hinzufügen"}
                      </button>
                    </div>

                    {we.sets.length > 0 && (
                      <table className="w-full text-sm mb-3">
                        <thead>
                          <tr className="text-ink-muted text-left">
                            <th className="font-normal pb-1">Satz</th>
                            <th className="font-normal pb-1">Gewicht</th>
                            <th className="font-normal pb-1">Wdh.</th>
                          </tr>
                        </thead>
                        <tbody>
                          {we.sets.map((set) => (
                            <tr key={set.id}>
                              <td>{set.setNumber}</td>
                              <td>{set.weightKg} kg</td>
                              <td>{set.reps}</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    )}

                    {activeExerciseId === we.id && (
                      <div className="flex gap-2 mt-2">
                        <input
                          type="number"
                          placeholder="kg"
                          value={setWeight}
                          onChange={(e) => setSetWeight(e.target.value)}
                          className="w-20 border border-border rounded px-2 py-1 text-sm focus:outline-none focus:ring-2 focus:ring-accent"
                        />
                        <input
                          type="number"
                          placeholder="Wdh."
                          value={setReps}
                          onChange={(e) => setSetReps(e.target.value)}
                          className="w-20 border border-border rounded px-2 py-1 text-sm focus:outline-none focus:ring-2 focus:ring-accent"
                        />
                        <button
                          onClick={() => handleAddSet(we.id)}
                          className="bg-accent text-white rounded px-3 py-1 text-sm"
                        >
                          Speichern
                        </button>
                      </div>
                    )}
                  </div>
                ))}
              </div>

              <div className="space-y-3">
                <h3 className="text-sm font-medium text-ink-muted">Übung hinzufügen</h3>
                <div className="flex gap-2">
                  <select
                    value={selectedExerciseId ?? ""}
                    onChange={(e) => setSelectedExerciseId(Number(e.target.value))}
                    className="flex-1 border border-border rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-accent"
                  >
                    <option value="">Übung wählen…</option>
                    {exercises.map((ex) => (
                      <option key={ex.id} value={ex.id}>
                        {ex.name} ({MUSCLE_GROUP_LABELS[ex.muscleGroup]})
                      </option>
                    ))}
                  </select>
                  <button
                    onClick={handleAddExercise}
                    disabled={!selectedExerciseId}
                    className="bg-accent text-white rounded px-4 py-2 text-sm disabled:opacity-50"
                  >
                    Hinzufügen
                  </button>
                </div>

                <form onSubmit={handleCreateExercise} className="flex gap-2">
                  <input
                    placeholder="Neue Übung…"
                    value={newExerciseName}
                    onChange={(e) => setNewExerciseName(e.target.value)}
                    className="flex-1 border border-border rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-accent"
                  />
                  <select
                    value={newExerciseMuscle}
                    onChange={(e) => setNewExerciseMuscle(e.target.value as MuscleGroup)}
                    className="border border-border rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-accent"
                  >
                    {Object.entries(MUSCLE_GROUP_LABELS).map(([value, label]) => (
                      <option key={value} value={value}>{label}</option>
                    ))}
                  </select>
                  <button
                    type="submit"
                    className="bg-accent text-white rounded px-4 py-2 text-sm"
                  >
                    Anlegen
                  </button>
                </form>
              </div>
            </div>
          )}

          {workouts.length > 0 && (
            <section>
              <h2 className="text-sm font-medium text-ink-muted mb-3">Vergangene Workouts</h2>
              <ul className="divide-y divide-border border-t border-b border-border">
                {workouts.map((workout) => (
                  <li key={workout.id} className="py-3 flex items-center justify-between">
                    <div>
                      <p className={workout.completed ? "text-ink-muted" : ""}>
                        {workout.name}
                      </p>
                      <p className="text-xs text-ink-muted">
                        {workout.date} · {workout.exercises.length} Übungen ·{" "}
                        {workout.completed ? "Abgeschlossen" : "Aktiv"}
                      </p>
                    </div>
                    <button
                      onClick={() => handleDeleteWorkout(workout.id)}
                      className="text-sm text-ink-muted hover:text-red-700 underline"
                    >
                      Löschen
                    </button>
                  </li>
                ))}
              </ul>
            </section>
          )}
        </div>
      )}
    </div>
  );
}