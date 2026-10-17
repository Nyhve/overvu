import { useEffect, useState } from "react";
import { goalApi } from "../api/goalApi";
import { taskApi } from "../api/taskApi";
import { ApiError } from "../api/client";
import type { Goal } from "../types/goal";
import type { Task } from "../types/task";
import { habitApi } from "../api/habitApi.ts";
import type { Habit } from "../types/habit.ts";
import { fitnessApi } from "../api/fitnessApi.ts";
import type { Workout } from "../types/fitness.ts";
import { learningApi } from "../api/learningApi.ts";
import type { LearningProject } from "../types/learning.ts";
import { financeApi } from "../api/financeApi.ts";
import type { MonthlyStats } from "../types/finance.ts";

export default function DashboardPage() {
  const [goals, setGoals] = useState<Goal[]>([]);
  const [tasks, setTasks] = useState<Task[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [habits, setHabits] = useState<Habit[]>([]);
  const [workouts, setWorkouts] = useState<Workout[]>([]);
  const [learningProjects, setLearningProjects] = useState<LearningProject[]>([]);
  const [monthlyStats, setMonthlyStats] = useState<MonthlyStats | null>(null);

  useEffect(() => {
    const now = new Date();
    Promise.all([
      goalApi.getAll(),
      taskApi.getAll(),
      habitApi.getAll(),
      fitnessApi.getWorkouts(),
      learningApi.getAll(),
      financeApi.getMonthlyStats(now.getFullYear(), now.getMonth() + 1),
    ])
      .then(([goalsResult, tasksResult, habitsResult, workoutsResult, learningResult, statsResult]) => {
        setGoals(goalsResult);
        setTasks(tasksResult);
        setHabits(habitsResult);
        setWorkouts(workoutsResult);
        setLearningProjects(learningResult);
        setMonthlyStats(statsResult);
      })
      .catch((err) => {
        setError(err instanceof ApiError ? err.message : "Daten konnten nicht geladen werden.");
      })
      .finally(() => setIsLoading(false));
  }, []);

  const activeGoals = goals.filter((g) => g.status === "ACTIVE");
  const openTasks = tasks.filter((t) => !t.completed);

  const upcomingTasks = [...openTasks]
    .sort((a, b) => {
      if (!a.dueDate) return 1;
      if (!b.dueDate) return -1;
      return a.dueDate.localeCompare(b.dueDate);
    })
    .slice(0, 5);
  
  return (
    <>
      {isLoading && <p className="text-ink-muted">Lädt…</p>}
      {error && <p className="text-sm text-red-700">{error}</p>}

      {!isLoading && !error && (
        <>
          <div className="grid grid-cols-2 gap-4 mb-10">
            <div className="border border-border p-5">
              <p className="text-3xl font-semibold">{activeGoals.length}</p>
              <p className="text-sm text-ink-muted mt-1">Aktive Ziele</p>
            </div>
            <div className="border border-border p-5">
              <p className="text-3xl font-semibold">{openTasks.length}</p>
              <p className="text-sm text-ink-muted mt-1">Offene Aufgaben</p>
            </div>
            <div className="border border-border p-5">
              <p className="text-3xl font-semibold">{habits.length}</p>
              <p className="text-sm text-ink-muted mt-1">Aktive Habits</p>
            </div>
            <div className="border border-border p-5">
              <p className="text-3xl font-semibold">
                {workouts.filter((w) => w.completed).length}
              </p>
              <p className="text-sm text-ink-muted mt-1">Abgeschlossene Workouts</p>
            </div>
            <div className="border border-border p-5">
              <p className="text-3xl font-semibold">
                {learningProjects.reduce((sum, p) => sum + p.totalMinutes, 0)}
              </p>
              <p className="text-sm text-ink-muted mt-1">Lernminuten gesamt</p>
            </div>
            {monthlyStats && (
            <div className="border border-border p-5">
              <p className={`text-3xl font-semibold ${monthlyStats.balance >= 0 ? "text-green-700" : "text-red-700"}`}>
                {new Intl.NumberFormat("de-DE", { style: "currency", currency: "EUR" }).format(monthlyStats.balance)}
              </p>
              <p className="text-sm text-ink-muted mt-1">Bilanz diesen Monat</p>
            </div>
          )}
          </div>

          <section className="mb-10">
            <h2 className="text-sm font-medium text-ink-muted mb-3">Nächste Aufgaben</h2>
            {upcomingTasks.length === 0 ? (
              <p className="text-sm text-ink-muted">Keine offenen Aufgaben.</p>
            ) : (
              <ul className="divide-y divide-border border-t border-b border-border">
                {upcomingTasks.map((task) => (
                  <li key={task.id} className="py-3 flex items-center justify-between">
                    <span>{task.title}</span>
                    {task.dueDate && (
                      <span className="text-sm text-ink-muted">{task.dueDate}</span>
                    )}
                  </li>
                ))}
              </ul>
            )}
          </section>

          <section>
            <h2 className="text-sm font-medium text-ink-muted mb-3">Aktive Ziele</h2>
            {activeGoals.length === 0 ? (
              <p className="text-sm text-ink-muted">Noch keine aktiven Ziele.</p>
            ) : (
              <ul className="divide-y divide-border border-t border-b border-border">
                {activeGoals.slice(0, 3).map((goal) => (
                  <li key={goal.id} className="py-3 flex items-center justify-between">
                    <span>{goal.title}</span>
                    {goal.targetDate && (
                      <span className="text-sm text-ink-muted">{goal.targetDate}</span>
                    )}
                  </li>
                ))}
              </ul>
            )}
          </section>
        </>
      )}
    </>
  );
}