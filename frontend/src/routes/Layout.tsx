import { useState } from "react";
import { NavLink, Link, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext.tsx";
import type { UserPreferences } from "../types/user.ts";

const NAV_LINKS: { to: string; label: string; preferenceKey: keyof UserPreferences }[] = [
  { to: "/goals", label: "Ziele", preferenceKey: "goalsEnabled" },
  { to: "/tasks", label: "Aufgaben", preferenceKey: "tasksEnabled" },
  { to: "/habits", label: "Habits", preferenceKey: "habitsEnabled" },
  { to: "/fitness", label: "Fitness", preferenceKey: "fitnessEnabled" },
  { to: "/learning", label: "Learning", preferenceKey: "learningEnabled" },
  { to: "/finance", label: "Finanzen", preferenceKey: "financeEnabled" },
];

export function Layout() {
  const { logout, preferences } = useAuth();
  const [menuOpen, setMenuOpen] = useState(false);

  const visibleLinks = NAV_LINKS.filter(
    (link) => !preferences || preferences[link.preferenceKey]
  );

  const linkClass = ({ isActive }: { isActive: boolean }) =>
    isActive
      ? "text-accent font-medium"
      : "text-ink-muted hover:text-ink";

  const mobileLinkClass = ({ isActive }: { isActive: boolean }) =>
    `block py-3 px-4 text-sm border-b border-border ${
      isActive ? "text-accent font-medium bg-surface" : "text-ink hover:bg-surface"
    }`;

  return (
    <div className="min-h-screen bg-background">
      <header className="border-b border-border bg-surface sticky top-0 z-50">
        <div className="max-w-3xl mx-auto px-4 md:px-8 py-4 flex items-center justify-between">
          <div className="flex items-center gap-6">
            <Link
              to="/"
              className="font-semibold hover:text-accent transition-colors"
              onClick={() => setMenuOpen(false)}
            >
              OvervU
            </Link>
            <nav className="hidden md:flex gap-4 text-sm">
              {visibleLinks.map((link) => (
                <NavLink key={link.to} to={link.to} className={linkClass}>
                  {link.label}
                </NavLink>
              ))}
            </nav>
          </div>

          <div className="hidden md:flex items-center gap-4">
            <NavLink
              to="/profile"
              className={({ isActive }) =>
                `text-sm ${isActive ? "text-accent font-medium" : "text-ink-muted hover:text-ink"}`
              }
            >
              Profil
            </NavLink>
            <button
              onClick={logout}
              className="text-sm text-ink-muted hover:text-ink underline"
            >
              Abmelden
            </button>
          </div>

          <button
            onClick={() => setMenuOpen((prev) => !prev)}
            className="md:hidden flex flex-col gap-1.5 p-2"
            aria-label="Menü öffnen"
          >
            <span className={`block w-5 h-0.5 bg-ink transition-transform duration-200 ${menuOpen ? "rotate-45 translate-y-2" : ""}`} />
            <span className={`block w-5 h-0.5 bg-ink transition-opacity duration-200 ${menuOpen ? "opacity-0" : ""}`} />
            <span className={`block w-5 h-0.5 bg-ink transition-transform duration-200 ${menuOpen ? "-rotate-45 -translate-y-2" : ""}`} />
          </button>
        </div>

        {menuOpen && (
          <div className="md:hidden border-t border-border bg-surface">
            {visibleLinks.map((link) => (
              <NavLink
                key={link.to}
                to={link.to}
                className={mobileLinkClass}
                onClick={() => setMenuOpen(false)}
              >
                {link.label}
              </NavLink>
            ))}
            <NavLink
              to="/profile"
              className={mobileLinkClass}
              onClick={() => setMenuOpen(false)}
            >
              Profil
            </NavLink>
            <button
              onClick={() => { logout(); setMenuOpen(false); }}
              className="block w-full text-left py-3 px-4 text-sm text-ink-muted hover:bg-surface"
            >
              Abmelden
            </button>
          </div>
        )}
      </header>

      <main className="max-w-3xl mx-auto px-4 md:px-8 py-6 md:py-8">
        <Outlet />
      </main>
    </div>
  );
}