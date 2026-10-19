import { useEffect, useState } from "react";
import {
  BarChart, Bar, LineChart, Line,
  XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer,
} from "recharts";
import { goalApi } from "../api/goalApi";
import { taskApi } from "../api/taskApi";
import { ApiError } from "../api/client";
import { habitApi, type HabitConsistency } from "../api/habitApi.ts";
import { fitnessApi } from "../api/fitnessApi.ts";
import { learningApi } from "../api/learningApi.ts";
import { financeApi } from "../api/financeApi.ts";
import { activityApi, type DailyActivityCount } from "../api/activityApi.ts";
import type { Goal } from "../types/goal";
import type { Task } from "../types/task";
import type { Habit } from "../types/habit.ts";
import type { Workout } from "../types/fitness.ts";
import type { LearningProject } from "../types/learning.ts";
import type { MonthlyStats, MonthlyFinanceSummary } from "../types/finance.ts";

const ACCENT = "#3A5A40";
const RED = "#B91C1C";
const MUTED = "#6B6862";

function getWorkoutsPerWeek(workouts: Workout[]) {
  const weeks: Record<string, number> = {};
  const now = new Date();

  for (let i = 7; i >= 0; i--) {
    const weekStart = new Date(now);
    weekStart.setDate(now.getDate() - i * 7);
    const label = `KW ${getWeekNumber(weekStart)}`;
    weeks[label] = 0;
  }

  workouts.filter((w) => w.completed).forEach((w) => {
    const d = new Date(w.date);
    const label = `KW ${getWeekNumber(d)}`;
    if (label in weeks) weeks[label]++;
  });

  return Object.entries(weeks).map(([week, count]) => ({ week, count }));
}

function getWeekNumber(date: Date): number {
  const d = new Date(date);
  d.setHours(0, 0, 0, 0);
  d.setDate(d.getDate() + 4 - (d.getDay() || 7));
  const yearStart = new Date(d.getFullYear(), 0, 1);
  return Math.ceil((((d.getTime() - yearStart.getTime()) / 86400000) + 1) / 7);
}

function getTotalVolume(workouts: Workout[]) {
  return workouts
    .filter((w) => w.completed)
    .slice(-10)
    .map((w) => {
      const volume = w.exercises.reduce((sum, ex) =>
        sum + ex.sets.reduce((s, set) => s + set.weightKg * set.reps, 0), 0
      );
      return { date: w.date, volume: Math.round(volume) };
    });
}

export default function DashboardPage() {
  const [goals, setGoals] = useState<Goal[]>([]);
  const [tasks, setTasks] = useState<Task[]>([]);
  const [habits, setHabits] = useState<Habit[]>([]);
  const [workouts, setWorkouts] = useState<Workout[]>([]);
  const [learningProjects, setLearningProjects] = useState<LearningProject[]>([]);
  const [monthlyStats, setMonthlyStats] = useState<MonthlyStats | null>(null);
  const [dailyActivity, setDailyActivity] = useState<DailyActivityCount[]>([]);
  const [financeSummary, setFinanceSummary] = useState<MonthlyFinanceSummary[]>([]);
  const [habitConsistency, setHabitConsistency] = useState<HabitConsistency[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const now = new Date();
    Promise.all([
      goalApi.getAll(),
      taskApi.getAll(),
      habitApi.getAll(),
      fitnessApi.getWorkouts(),
      learningApi.getAll(),
      financeApi.getMonthlyStats(now.getFullYear(), now.getMonth() + 1),
      activityApi.getDailyCounts(30),
      financeApi.getLastMonthsSummary(6),
      habitApi.getConsistency(28),
    ])
      .then(([goalsR, tasksR, habitsR, workoutsR, learningR, statsR, activityR, financeR, consistencyR]) => {
        setGoals(goalsR);
        setTasks(tasksR);
        setHabits(habitsR);
        setWorkouts(workoutsR);
        setLearningProjects(learningR);
        setMonthlyStats(statsR);
        setDailyActivity(activityR);
        setFinanceSummary(financeR);
        setHabitConsistency(consistencyR);
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
        <div className="space-y-10">
          <div>
            <h1 className="text-2xl font-semibold mb-1">Deine Übersicht</h1>
            <p className="text-sm text-ink-muted">
              {new Date().toLocaleDateString("de-DE", {
                weekday: "long", year: "numeric",
                month: "long", day: "numeric",
              })}
            </p>
          </div>

          {/* Kennzahlen */}
          <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
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
              <p className="text-sm text-ink-muted mt-1">Workouts</p>
            </div>
            <div className="border border-border p-5">
              <p className="text-3xl font-semibold">
                {learningProjects.reduce((sum, p) => sum + p.totalMinutes, 0)}
              </p>
              <p className="text-sm text-ink-muted mt-1">Lernminuten</p>
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

          {/* Nächste Aufgaben + Aktive Ziele */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 md:gap-8">
            <section>
              <h2 className="text-sm font-medium text-ink-muted mb-3">Nächste Aufgaben</h2>
              {upcomingTasks.length === 0 ? (
                <p className="text-sm text-ink-muted">Keine offenen Aufgaben.</p>
              ) : (
                <ul className="divide-y divide-border border-t border-b border-border">
                  {upcomingTasks.map((task) => (
                    <li key={task.id} className="py-3 flex items-center justify-between">
                      <span className="text-sm">{task.title}</span>
                      {task.dueDate && (
                        <span className="text-xs text-ink-muted">{task.dueDate}</span>
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
                  {activeGoals.slice(0, 5).map((goal) => (
                    <li key={goal.id} className="py-3 flex items-center justify-between">
                      <span className="text-sm">{goal.title}</span>
                      {goal.targetDate && (
                        <span className="text-xs text-ink-muted">{goal.targetDate}</span>
                      )}
                    </li>
                  ))}
                </ul>
              )}
            </section>
          </div>

          {/* Aktivitäts-Timeline */}
          {dailyActivity.length > 0 && (
            <section>
              <h2 className="text-sm font-medium text-ink-muted mb-4">
                Aktivitäten – letzte 30 Tage
              </h2>
              <ResponsiveContainer width="100%" height={180}>
                <BarChart data={dailyActivity}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#E4E1DA" />
                  <XAxis
                    dataKey="date"
                    tick={{ fontSize: 10, fill: MUTED }}
                    tickFormatter={(v) => v.slice(5)}
                  />
                  <YAxis tick={{ fontSize: 10, fill: MUTED }} allowDecimals={false} />
                  <Tooltip
                    labelFormatter={(l) => `Datum: ${l}`}
                    formatter={(v) => [`${v} Aktivitäten`, ""]}
                  />
                  <Bar dataKey="count" fill={ACCENT} radius={[3, 3, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </section>
          )}

          {/* Habit-Konsistenz */}
          {habitConsistency.length > 0 && (
            <section>
              <h2 className="text-sm font-medium text-ink-muted mb-4">
                Habit-Konsistenz – letzte 28 Tage
              </h2>
              <div className="space-y-3">
                {habitConsistency.map((h) => (
                  <div key={h.habitId}>
                    <div className="flex justify-between text-sm mb-1">
                      <span>{h.name}</span>
                      <span className="text-ink-muted">
                        {h.completedDays}/{h.totalDays} · {h.consistencyPercent}%
                      </span>
                    </div>
                    <div className="h-2 bg-border rounded-full overflow-hidden">
                      <div
                        className="h-full rounded-full"
                        style={{ width: `${h.consistencyPercent}%`, backgroundColor: ACCENT }}
                      />
                    </div>
                  </div>
                ))}
              </div>
            </section>
          )}

          {/* Monatsfinanzen */}
          {financeSummary.some((m) => m.income > 0 || m.expenses > 0) && (
            <section>
              <h2 className="text-sm font-medium text-ink-muted mb-4">
                Finanzen – letzte 6 Monate
              </h2>
              <ResponsiveContainer width="100%" height={200}>
                <LineChart data={financeSummary}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#E4E1DA" />
                  <XAxis dataKey="monthLabel" tick={{ fontSize: 10, fill: MUTED }} />
                  <YAxis tick={{ fontSize: 10, fill: MUTED }} />
                  <Tooltip
                    formatter={(v) =>
                      new Intl.NumberFormat("de-DE", { style: "currency", currency: "EUR" }).format(Number(v))
                    }
                  />
                  <Line type="monotone" dataKey="income" name="Einnahmen" stroke={ACCENT} strokeWidth={2} dot={{ r: 3 }} />
                  <Line type="monotone" dataKey="expenses" name="Ausgaben" stroke={RED} strokeWidth={2} dot={{ r: 3 }} />
                </LineChart>
              </ResponsiveContainer>
            </section>
          )}

          {/* Lernzeit pro Projekt */}
          {learningProjects.length > 0 && (
            <section>
              <h2 className="text-sm font-medium text-ink-muted mb-4">Lernzeit pro Projekt</h2>
              <ResponsiveContainer width="100%" height={Math.max(120, learningProjects.length * 45)}>
                <BarChart
                  data={learningProjects.map((p) => ({ name: p.title, minutes: p.totalMinutes }))}
                  layout="vertical"
                >
                  <CartesianGrid strokeDasharray="3 3" stroke="#E4E1DA" />
                  <XAxis type="number" tick={{ fontSize: 10, fill: MUTED }} tickFormatter={(v) => `${v} Min.`} />
                  <YAxis type="category" dataKey="name" tick={{ fontSize: 10, fill: MUTED }} width={110} />
                  <Tooltip formatter={(v) => [`${v} Minuten`, "Lernzeit"]} />
                  <Bar dataKey="minutes" fill={ACCENT} radius={[0, 3, 3, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </section>
          )}

          {/* Fitness */}
          {workouts.filter((w) => w.completed).length > 0 && (
            <section>
              <h2 className="text-sm font-medium text-ink-muted mb-4">
                Fitness – letzte 8 Wochen
              </h2>
              <div className="grid grid-cols-2 gap-6">
                <div>
                  <p className="text-xs text-ink-muted mb-2">Trainings pro Woche</p>
                  <ResponsiveContainer width="100%" height={160}>
                    <BarChart data={getWorkoutsPerWeek(workouts)}>
                      <CartesianGrid strokeDasharray="3 3" stroke="#E4E1DA" />
                      <XAxis dataKey="week" tick={{ fontSize: 10, fill: MUTED }} />
                      <YAxis tick={{ fontSize: 10, fill: MUTED }} allowDecimals={false} />
                      <Tooltip formatter={(v) => [`${v} Trainings`, ""]} />
                      <Bar dataKey="count" fill={ACCENT} radius={[3, 3, 0, 0]} />
                    </BarChart>
                  </ResponsiveContainer>
                </div>

                <div>
                  <p className="text-xs text-ink-muted mb-2">Volumen (kg) pro Training</p>
                  <ResponsiveContainer width="100%" height={160}>
                    <LineChart data={getTotalVolume(workouts)}>
                      <CartesianGrid strokeDasharray="3 3" stroke="#E4E1DA" />
                      <XAxis dataKey="date" tick={{ fontSize: 10, fill: MUTED }} tickFormatter={(v) => v.slice(5)} />
                      <YAxis tick={{ fontSize: 10, fill: MUTED }} />
                      <Tooltip
                        labelFormatter={(l) => `Datum: ${l}`}
                        formatter={(v) => [`${v} kg`, "Volumen"]}
                      />
                      <Line type="monotone" dataKey="volume" stroke={ACCENT} strokeWidth={2} dot={{ r: 3 }} />
                    </LineChart>
                  </ResponsiveContainer>
                </div>
              </div>
            </section>
          )}
        </div>
      )}
    </>
  );
}