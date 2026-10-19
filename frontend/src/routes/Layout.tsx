import { NavLink, Link, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext.tsx";

export function Layout() {
  const { logout } = useAuth();

  const linkClass = ({ isActive }: { isActive: boolean }) =>
    isActive ? "text-accent" : "text-ink-muted hover:text-ink";

  return (
    <div className="min-h-screen">
      <header className="border-b border-border">
        <div className="max-w-3xl mx-auto px-8 py-4 flex items-center justify-between">
          <div className="flex items-center gap-6">
            <Link to="/" className="font-semibold hover:text-accent transition-colors">
              OvervU
            </Link>
            <nav className="flex gap-4 text-sm">
              <NavLink to="/goals" className={linkClass}>
                Ziele
              </NavLink>
              <NavLink to="/tasks" className={linkClass}>
                Aufgaben
              </NavLink>
              <NavLink to="/habits" className={linkClass}>
                Habits
              </NavLink>
              <NavLink to="/fitness" className={linkClass}>
                Fitness
              </NavLink>
              <NavLink to="/learning" className={linkClass}>
                Learning
              </NavLink>
              <NavLink to="/finance" className={linkClass}>
                Finanzen
              </NavLink>
              <NavLink to="/analytics" className={linkClass}>
                Analytics
              </NavLink>
            </nav>
          </div>
          <button onClick={logout} className="text-sm text-ink-muted hover:text-ink underline">
            Abmelden
          </button>
        </div>
      </header>
      <main className="max-w-3xl mx-auto px-8 py-8">
        <Outlet />
      </main>
    </div>
  );
}