import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import type { Session } from '../types';

interface AuthState {
  session: Session | null;
  login: (s: Session) => void;
  logout: () => void;
  updateSession: (partial: Partial<Session>) => void;
  isAdmin: boolean;
}

const AuthContext = createContext<AuthState | undefined>(undefined);

const STORAGE_KEY = 'sa_session';

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [session, setSession] = useState<Session | null>(() => {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      return raw ? (JSON.parse(raw) as Session) : null;
    } catch { return null; }
  });

  useEffect(() => {
    if (session) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
      localStorage.setItem('sa_userId', session.userId);
      localStorage.setItem('sa_role', session.role);
      localStorage.setItem('sa_fullName', session.fullName);
    } else {
      localStorage.removeItem(STORAGE_KEY);
      localStorage.removeItem('sa_userId');
      localStorage.removeItem('sa_role');
      localStorage.removeItem('sa_fullName');
    }
  }, [session]);

  // If the session is cleared in another tab (or wiped from DevTools), reflect
  // the logout in this tab too. The `storage` event fires in *other* documents
  // sharing the same origin.
  useEffect(() => {
    const onStorage = (e: StorageEvent) => {
      if (e.key === null || (e.key === STORAGE_KEY && e.newValue === null)) {
        setSession(null);
      }
    };
    window.addEventListener('storage', onStorage);
    return () => window.removeEventListener('storage', onStorage);
  }, []);

  const value = useMemo<AuthState>(() => ({
    session,
    login: (s) => setSession(s),
    logout: () => setSession(null),
    updateSession: (partial) => setSession((prev) => (prev ? { ...prev, ...partial } : prev)),
    isAdmin: session?.role === 'ADMIN'
  }), [session]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = (): AuthState => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
};
