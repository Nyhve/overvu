import { useEffect, useState } from "react";
import { userApi } from "../api/userApi.ts";
import { ApiError } from "../api/client.ts";
import type { UserPreferences } from "../types/user.ts";
import { useDarkMode } from "../hooks/useDarkMode.ts";
import { useAuth } from "../context/AuthContext.tsx";

const FEATURE_LABELS: { key: keyof UserPreferences; label: string; description: string }[] = [
  { key: "goalsEnabled", label: "Ziele", description: "Langfristige Lebensziele verfolgen" },
  { key: "tasksEnabled", label: "Aufgaben", description: "To-do Liste für kurzfristige Aufgaben" },
  { key: "habitsEnabled", label: "Habits", description: "Tägliche und wöchentliche Gewohnheiten" },
  { key: "fitnessEnabled", label: "Fitness", description: "Workouts und Trainingsfortschritt" },
  { key: "learningEnabled", label: "Learning", description: "Lernprojekte und Lernzeit tracken" },
  { key: "financeEnabled", label: "Finanzen", description: "Einnahmen und Ausgaben verwalten" },
];

export default function ProfilePage() {
  const [preferences, setPreferences] = useState<UserPreferences | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [saved, setSaved] = useState(false);
  const { isDark, toggle } = useDarkMode();
  const { refreshPreferences } = useAuth();

  useEffect(() => {
    userApi
      .getPreferences()
      .then(setPreferences)
      .catch((err) =>
        setError(err instanceof ApiError ? err.message : "Einstellungen konnten nicht geladen werden."),
      )
      .finally(() => setIsLoading(false));
  }, []);

  async function handleToggle(key: keyof UserPreferences) {
    if (!preferences) return;
    const updated = { ...preferences, [key]: !preferences[key] };
    setPreferences(updated);
    setIsSaving(true);
    setSaved(false);
    try {
        await userApi.updatePreferences(updated);
        await refreshPreferences();
        setSaved(true);
        setTimeout(() => setSaved(false), 2000);
    } catch (err) {
        setError(err instanceof ApiError ? err.message : "Einstellungen konnten nicht gespeichert werden.");
        setPreferences(preferences);
    } finally {
        setIsSaving(false);
    }
  }

  return (
    <div className="max-w-lg">
      <h1 className="text-2xl font-semibold mb-8">Profil</h1>

      {error && <p className="text-sm text-red-700 mb-4">{error}</p>}
      {isLoading && <p className="text-ink-muted">Lädt…</p>}

      {!isLoading && preferences && (
        <div className="space-y-8">

          {/* Erscheinungsbild */}
          <section>
            <h2 className="text-sm font-medium text-ink-muted mb-4">Erscheinungsbild</h2>
            <div className="flex items-center justify-between py-3 border-b border-border">
              <div>
                <p className="text-sm font-medium">Dark Mode</p>
                <p className="text-xs text-ink-muted mt-0.5">Dunkles Farbschema verwenden</p>
              </div>
              <button
                onClick={toggle}
                className={`relative w-11 h-6 rounded-full transition-colors duration-200 ${
                  isDark ? "bg-accent" : "bg-border"
                }`}
                aria-label="Dark Mode umschalten"
              >
                <span
                  className={`absolute top-0.5 left-0.5 w-5 h-5 bg-white rounded-full shadow transition-transform duration-200 ${
                    isDark ? "translate-x-5" : "translate-x-0"
                  }`}
                />
              </button>
            </div>
          </section>

          {/* Feature-Toggles */}
          <section>
            <h2 className="text-sm font-medium text-ink-muted mb-1">Features</h2>
            <p className="text-xs text-ink-muted mb-4">
              Deaktivierte Features werden aus der Navigation und der Übersicht ausgeblendet.
            </p>
            <div className="divide-y divide-border border-t border-b border-border">
              {FEATURE_LABELS.map(({ key, label, description }) => (
                <div key={key} className="flex items-center justify-between py-4">
                  <div>
                    <p className={`text-sm font-medium ${!preferences[key] ? "text-ink-muted" : ""}`}>
                      {label}
                    </p>
                    <p className="text-xs text-ink-muted mt-0.5">{description}</p>
                  </div>
                  <button
                    onClick={() => handleToggle(key)}
                    disabled={isSaving}
                    className={`relative w-11 h-6 rounded-full transition-colors duration-200 disabled:opacity-50 ${
                      preferences[key] ? "bg-accent" : "bg-border"
                    }`}
                    aria-label={`${label} ${preferences[key] ? "deaktivieren" : "aktivieren"}`}
                  >
                    <span
                      className={`absolute top-0.5 left-0.5 w-5 h-5 bg-white rounded-full shadow transition-transform duration-200 ${
                        preferences[key] ? "translate-x-5" : "translate-x-0"
                      }`}
                    />
                  </button>
                </div>
              ))}
            </div>
            {saved && (
              <p className="text-xs text-accent mt-3">✓ Gespeichert</p>
            )}
          </section>
        </div>
      )}
    </div>
  );
}