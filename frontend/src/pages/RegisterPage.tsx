import { useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import { authApi } from "../api/authApi";
import { ApiError } from "../api/client";

export default function RegisterPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const navigate = useNavigate();

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setIsSubmitting(true);

    try {
      await authApi.register({ email, password });
      navigate("/login");
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.message);
      } else {
        setError("Registrierung fehlgeschlagen. Bitte versuch es erneut.");
      }
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center px-4">
      <div className="w-full max-w-sm">
        <h1 className="text-2xl font-semibold mb-1">Konto erstellen</h1>
        <p className="text-ink-muted mb-8">Leg deinen OvervU-Account an.</p>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label htmlFor="email" className="block text-sm mb-1">
              Email
            </label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              className="w-full border border-border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-accent"
            />
          </div>

          <div>
            <label htmlFor="password" className="block text-sm mb-1">
              Passwort
            </label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              minLength={8}
              className="w-full border border-border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-accent"
            />
            <p className="text-xs text-ink-muted mt-1">Mindestens 8 Zeichen.</p>
          </div>

          {error && <p className="text-sm text-red-700">{error}</p>}

          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full bg-accent text-white rounded px-3 py-2 disabled:opacity-50"
          >
            {isSubmitting ? "Erstellt Konto…" : "Registrieren"}
          </button>
        </form>

        <p className="text-sm text-ink-muted mt-6">
          Schon einen Account?{" "}
          <Link to="/login" className="text-accent underline">
            Anmelden
          </Link>
        </p>
      </div>
    </div>
  );
}