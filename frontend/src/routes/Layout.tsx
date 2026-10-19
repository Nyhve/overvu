import { useState } from "react";
import { NavLink, Link, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext.tsx";
import { useDarkMode } from "../hooks/useDarkMode.ts";

const NAV_LINKS = [
  { to: "/goals", label: "Ziele" },
  { to: "/tasks", label: "Aufgaben" },
  { to: "/habits", label: "Habits" },
  { to: "/fitness", label: "Fitness" },
  { to: "/learning", label: "Learning" },
  { to: "/finance", label: "Finanzen" },
];

export function Layout() {
  const { logout } = useAuth();
  const [menuOpen, setMenuOpen] = useState(false);
  const { isDark, toggle } = useDarkMode();

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
            {/* Desktop Navigation */}
            <nav className="hidden md:flex gap-4 text-sm">
              {NAV_LINKS.map((link) => (
                <NavLink key={link.to} to={link.to} className={linkClass}>
                  {link.label}
                </NavLink>
              ))}
            </nav>
          </div>

          <div className="flex items-center gap-4">
            <button
              onClick={toggle}
              className="hidden md:block text-sm text-ink-muted hover:text-ink"
              aria-label="Dark Mode umschalten"
            >
              {isDark ? "☀️" : "🌙"}
            </button>
            {/* Desktop Logout */}
            <button
              onClick={logout}
              className="hidden md:block text-sm text-ink-muted hover:text-ink underline"
            >
              Abmelden
            </button>

            {/* Hamburger Button – nur auf Mobile */}
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
        </div>

        {/* Mobile Dropdown Menu */}
        {menuOpen && (
          <div className="md:hidden border-t border-border bg-surface">
            {NAV_LINKS.map((link) => (
              <NavLink
                key={link.to}
                to={link.to}
                className={mobileLinkClass}
                onClick={() => setMenuOpen(false)}
              >
                {link.label}
              </NavLink>
            ))}
            <button
              onClick={toggle}
              className="block w-full text-left py-3 px-4 text-sm text-ink-muted hover:bg-surface border-b border-border"
            >
              {isDark ? "☀️ Hell" : "🌙 Dunkel"}
            </button>
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