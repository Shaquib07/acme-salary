import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import { api, type Me } from "../api";
import type { Role } from "./roles";
import { can, type Action } from "./roles";

type AuthState = {
  user: Me | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
  allowed: (action: Action) => boolean;
};

const AuthContext = createContext<AuthState | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<Me | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!api.token()) {
      setLoading(false);
      return;
    }
    api.me()
      .then(setUser)
      .catch(() => {
        api.setToken(null);
        setUser(null);
      })
      .finally(() => setLoading(false));
  }, []);

  const value = useMemo<AuthState>(
    () => ({
      user,
      loading,
      login: async (email, password) => {
        const result = await api.login(email, password);
        api.setToken(result.token);
        setUser({ email: result.email, displayName: result.displayName, role: result.role });
      },
      logout: () => {
        api.setToken(null);
        setUser(null);
      },
      allowed: (action) => can(user?.role as Role | undefined, action),
    }),
    [user, loading],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth outside provider");
  }
  return ctx;
}
