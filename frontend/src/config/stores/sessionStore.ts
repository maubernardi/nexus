import { create } from 'zustand';
import { createJSONStorage, persist } from 'zustand/middleware';

type SessionState = {
  /** Utente selezionato in modalità mock (sviluppo locale). */
  mockUserId: string | null;
  setMockUserId: (userId: string) => void;
  clearMockUserId: () => void;
};

export const useSessionStore = create<SessionState>()(
  persist(
    (set) => ({
      mockUserId: null,
      setMockUserId: (mockUserId) => set({ mockUserId }),
      clearMockUserId: () => set({ mockUserId: null }),
    }),
    { name: 'nexus-session', storage: createJSONStorage(() => localStorage) },
  ),
);
