import { createContext, useContext, useState, useEffect, type ReactNode } from "react";
import { clearToken, setToken as saveToken } from "../api/client";
import { userApi } from "../api/userApi.ts";
import type { UserPreferences } from "../types/user.ts";

interface AuthContextValue {
  isAuthenticated: boolean;
  preferences: UserPreferences | null;
  login: (token: string) => void;
  logout: () => void;
  refreshPreferences: () => Promise<void>;
}

const DEFAULT_PREFERENCES: UserPreferences = {
  goalsEnabled: true,
  tasksEnabled: true,
  habitsEnabled: true,
  fitnessEnabled: true,
  learningEnabled: true,
  financeEnabled: true,
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(
    () => localStorage.getItem("token") !== null,
  );
  const [preferences, setPreferences] = useState<UserPreferences | null>(null);

  async function loadPreferences() {
    try {
      const prefs = await userApi.getPreferences();
      setPreferences(prefs);
    } catch {
      setPreferences(DEFAULT_PREFERENCES);
    }
  }

  useEffect(() => {
    if (isAuthenticated) {
      loadPreferences();
    }
  }, [isAuthenticated]);

  function login(token: string) {
    saveToken(token);
    setIsAuthenticated(true);
  }

  function logout() {
    clearToken();
    setIsAuthenticated(false);
    setPreferences(null);
  }

  async function refreshPreferences() {
    await loadPreferences();
  }

  return (
    <AuthContext.Provider value={{ isAuthenticated, preferences, login, logout, refreshPreferences }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}