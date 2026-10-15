import { useEffect, useState } from "react";
import {
  BarChart,
  Bar,
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts";
import { activityApi, type DailyActivityCount } from "../api/activityApi.ts";
import { financeApi } from "../api/financeApi.ts";
import { habitApi, type HabitConsistency } from "../api/habitApi.ts";
import { learningApi } from "../api/learningApi.ts";
import { ApiError } from "../api/client.ts";
import type { MonthlyFinanceSummary } from "../types/finance.ts";
import type { LearningProject } from "../types/learning.ts";

export default function AnalyticsPage() {
  const [dailyActivity, setDailyActivity] = useState<DailyActivityCount[]>([]);
  const [financeSummary, setFinanceSummary] = useState<MonthlyFinanceSummary[]>([]);
  const [habitConsistency, setHabitConsistency] = useState<HabitConsistency[]>([]);
  const [learningProjects, setLearningProjects] = useState<LearningProject[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    Promise.all([
      activityApi.getDailyCounts(30),
      financeApi.getLastMonthsSummary(6),
      habitApi.getConsistency(28),
      learningApi.getAll(),
    ])
      .then(([activity, finance, habits, learning]) => {
        setDailyActivity(activity);
        setFinanceSummary(finance);
        setHabitConsistency(habits);
        setLearningProjects(learning);
      })
      .catch((err) =>
        setError(err instanceof ApiError ? err.message : "Daten konnten nicht geladen werden."),
      )
      .finally(() => setIsLoading(false));
  }, []);

  const accentColor = "#3A5A40";
  const redColor = "#B91C1C";
  const mutedColor = "#6B6862";

  return (
    <div>
      <h1 className="text-2xl font-semibold mb-8">Analytics</h1>

      {error && <p className="text-sm text-red-700 mb-4">{error}</p>}
      {isLoading && <p className="text-ink-muted">Lädt…</p>}

      {!isLoading && (
        <div className="space-y-12">

          {/* Aktivitäts-Timeline */}
          <section>
            <h2 className="text-sm font-medium text-ink-muted mb-4">
              Aktivitäten – letzte 30 Tage
            </h2>
            {dailyActivity.length === 0 ? (
              <p className="text-sm text-ink-muted">Noch keine Aktivitäten vorhanden.</p>
            ) : (
              <ResponsiveContainer width="100%" height={200}>
                <BarChart data={dailyActivity}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#E4E1DA" />
                  <XAxis
                    dataKey="date"
                    tick={{ fontSize: 11, fill: mutedColor }}
                    tickFormatter={(val) => val.slice(5)}
                  />
                  <YAxis tick={{ fontSize: 11, fill: mutedColor }} allowDecimals={false} />
                  <Tooltip
                    labelFormatter={(label) => `Datum: ${label}`}
                    formatter={(value) => [`${value} Aktivitäten`, ""]}
                  />
                  <Bar dataKey="count" fill={accentColor} radius={[3, 3, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            )}
          </section>

          {/* Monatsfinanzen */}
          <section>
            <h2 className="text-sm font-medium text-ink-muted mb-4">
              Finanzen – letzte 6 Monate
            </h2>
            {financeSummary.every((m) => m.income === 0 && m.expenses === 0) ? (
              <p className="text-sm text-ink-muted">Noch keine Finanzdaten vorhanden.</p>
            ) : (
              <ResponsiveContainer width="100%" height={220}>
                <LineChart data={financeSummary}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#E4E1DA" />
                  <XAxis
                    dataKey="monthLabel"
                    tick={{ fontSize: 11, fill: mutedColor }}
                  />
                  <YAxis tick={{ fontSize: 11, fill: mutedColor }} />
                  <Tooltip
                    formatter={(value) =>
                      new Intl.NumberFormat("de-DE", {
                        style: "currency",
                        currency: "EUR",
                      }).format(Number(value))
                    }
                  />
                  <Legend />
                  <Line
                    type="monotone"
                    dataKey="income"
                    name="Einnahmen"
                    stroke={accentColor}
                    strokeWidth={2}
                    dot={{ r: 4 }}
                  />
                  <Line
                    type="monotone"
                    dataKey="expenses"
                    name="Ausgaben"
                    stroke={redColor}
                    strokeWidth={2}
                    dot={{ r: 4 }}
                  />
                </LineChart>
              </ResponsiveContainer>
            )}
          </section>

          {/* Habit-Konsistenz */}
          <section>
            <h2 className="text-sm font-medium text-ink-muted mb-4">
              Habit-Konsistenz – letzte 28 Tage
            </h2>
            {habitConsistency.length === 0 ? (
              <p className="text-sm text-ink-muted">Noch keine Habits vorhanden.</p>
            ) : (
              <div className="space-y-3">
                {habitConsistency.map((habit) => (
                  <div key={habit.habitId}>
                    <div className="flex justify-between text-sm mb-1">
                      <span>{habit.name}</span>
                      <span className="text-ink-muted">
                        {habit.completedDays}/{habit.totalDays} · {habit.consistencyPercent}%
                      </span>
                    </div>
                    <div className="h-2 bg-border rounded-full overflow-hidden">
                      <div
                        className="h-full rounded-full"
                        style={{
                          width: `${habit.consistencyPercent}%`,
                          backgroundColor: accentColor,
                        }}
                      />
                    </div>
                  </div>
                ))}
              </div>
            )}
          </section>

          {/* Lernzeit pro Projekt */}
          <section>
            <h2 className="text-sm font-medium text-ink-muted mb-4">
              Lernzeit pro Projekt
            </h2>
            {learningProjects.length === 0 ? (
              <p className="text-sm text-ink-muted">Noch keine Lernprojekte vorhanden.</p>
            ) : (
              <ResponsiveContainer width="100%" height={Math.max(150, learningProjects.length * 50)}>
                <BarChart
                  data={learningProjects.map((p) => ({
                    name: p.title,
                    minutes: p.totalMinutes,
                  }))}
                  layout="vertical"
                >
                  <CartesianGrid strokeDasharray="3 3" stroke="#E4E1DA" />
                  <XAxis
                    type="number"
                    tick={{ fontSize: 11, fill: mutedColor }}
                    tickFormatter={(val) => `${val} Min.`}
                  />
                  <YAxis
                    type="category"
                    dataKey="name"
                    tick={{ fontSize: 11, fill: mutedColor }}
                    width={120}
                  />
                  <Tooltip
                    formatter={(value) => [`${value} Minuten`, "Lernzeit"]}
                  />
                  <Bar dataKey="minutes" fill={accentColor} radius={[0, 3, 3, 0]} />
                </BarChart>
              </ResponsiveContainer>
            )}
          </section>

        </div>
      )}
    </div>
  );
}