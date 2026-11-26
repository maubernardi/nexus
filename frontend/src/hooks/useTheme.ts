import { useSyncExternalStore } from 'react';

/** Tema dell'interfaccia (US-1312): chiaro o scuro. */
export type Theme = 'light' | 'dark';

/** Scelta dell'utente nel browser; senza scelta vale il tema del sistema operativo. Letta anche da theme-init.js. */
export const THEME_STORAGE_KEY = 'nexus-theme';

const systemDark = (): MediaQueryList | undefined => window.matchMedia?.('(prefers-color-scheme: dark)');

/** Tema scelto, cioè l'attributo `data-theme` della pagina; `null` se si segue il sistema. */
const chosen = (): Theme | null => {
  const theme = document.documentElement.dataset.theme;
  return theme === 'light' || theme === 'dark' ? theme : null;
};

/** Tema in uso: quello scelto, altrimenti quello del sistema operativo. */
export const currentTheme = (): Theme => chosen() ?? (systemDark()?.matches ? 'dark' : 'light');

const listeners = new Set<() => void>();

const subscribe = (listener: () => void): (() => void) => {
  listeners.add(listener);
  const media = systemDark();
  media?.addEventListener('change', listener);
  return () => {
    listeners.delete(listener);
    media?.removeEventListener('change', listener);
  };
};

/** Sceglie il tema: vale subito per la pagina e resta nel browser (se lo spazio locale non è disponibile, solo ora). */
export const setTheme = (theme: Theme): void => {
  document.documentElement.dataset.theme = theme;
  try {
    localStorage.setItem(THEME_STORAGE_KEY, theme);
  } catch {
    // navigazione privata o spazio bloccato: il tema vale fino alla chiusura della pagina
  }
  listeners.forEach((listener) => listener());
};

/** Tema in uso e passaggio all'altro (pulsante 🌙/☀️ nella barra in alto). */
export const useTheme = (): { theme: Theme; toggle: () => void } => {
  const theme = useSyncExternalStore(subscribe, currentTheme);
  return { theme, toggle: () => setTheme(theme === 'dark' ? 'light' : 'dark') };
};
