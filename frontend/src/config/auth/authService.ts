import type Keycloak from 'keycloak-js';

import { AUTH_MODE, KEYCLOAK_CONFIG, MOCK_USER_HEADER } from '@/config/auth/authConfig';
import { useSessionStore } from '@/config/stores/sessionStore';

let keycloak: Keycloak | null = null;
let keepAliveTimer: ReturnType<typeof setInterval> | null = null;

/** US-1310: ogni quanto si controlla il token, anche con la pagina nascosta (Chrome rallenta i timer a 1 al minuto). */
export const SESSION_KEEP_ALIVE_MS = 60_000;
/** Il token si rinnova se scade entro questi secondi: con token da 5 minuti, circa un rinnovo ogni 4 minuti. */
const KEEP_ALIVE_MIN_VALIDITY_S = 70;

/** Da chiamare prima del primo render: in modalità keycloak forza il login (PKCE). */
export const initAuth = async (): Promise<void> => {
  if (AUTH_MODE !== 'keycloak') {
    return;
  }
  const { default: KeycloakClient } = await import('keycloak-js');
  keycloak = new KeycloakClient(KEYCLOAK_CONFIG);
  await keycloak.init({ onLoad: 'login-required', pkceMethod: 'S256', checkLoginIframe: false });
  startSessionKeepAlive();
};

/**
 * US-1310: finché la pagina è aperta, anche in secondo piano o con lo schermo bloccato, la sessione di Keycloak non
 * scade per inattività. Ogni rinnovo del token azzera l'inattività; resta la durata massima della sessione (10 ore).
 */
const startSessionKeepAlive = (): void => {
  if (keepAliveTimer !== null) {
    return;
  }
  keepAliveTimer = setInterval(() => void keepSessionAlive(), SESSION_KEEP_ALIVE_MS);
};

/**
 * Un giro del rinnovo. Se la sessione è finita (browser chiuso o PC sospeso oltre l'inattività), Keycloak risponde 400:
 * keycloak-js cancella i token e, con `login-required`, apre la pagina di accesso, senza lasciare dati sullo schermo.
 * Se manca la rete non succede niente e si riprova al giro successivo.
 */
export const keepSessionAlive = async (): Promise<void> => {
  if (!keycloak?.refreshToken) {
    return;
  }
  try {
    await keycloak.updateToken(KEEP_ALIVE_MIN_VALIDITY_S);
  } catch {
    // rete assente o sessione finita: nel secondo caso keycloak-js ha già avviato l'accesso
  }
};

/**
 * Fine della sessione di Keycloak, in millisecondi dell'orologio locale: la scadenza del refresh token, corretta per lo
 * scarto tra gli orologi. Con il rinnovo attivo resta circa 30 minuti avanti; vicino alla durata massima si ferma lì.
 * `null` senza Keycloak (accesso di sviluppo).
 */
export const getSessionExpiresAt = (): number | null => {
  const exp = keycloak?.refreshTokenParsed?.exp;
  if (!keycloak || !exp) {
    return null;
  }
  return (exp + (keycloak.timeSkew ?? 0)) * 1000;
};

/** Accesso di nuovo con la password: una sessione nuova, con di nuovo la durata massima (US-1310). */
export const reloginNow = async (): Promise<void> => {
  await keycloak?.login({ prompt: 'login' });
};

export const getAuthHeaders = async (): Promise<Record<string, string>> => {
  if (AUTH_MODE === 'keycloak') {
    if (!keycloak) {
      return {};
    }
    await keycloak.updateToken(30);
    return keycloak.token ? { Authorization: `Bearer ${keycloak.token}` } : {};
  }
  const mockUserId = useSessionStore.getState().mockUserId;
  return mockUserId ? { [MOCK_USER_HEADER]: mockUserId } : {};
};

export const logout = async (): Promise<void> => {
  if (AUTH_MODE === 'keycloak') {
    await keycloak?.logout({ redirectUri: window.location.origin });
    return;
  }
  useSessionStore.getState().clearMockUserId();
};

/** Sessione scaduta o non valida. */
export const handleUnauthorized = (): void => {
  if (AUTH_MODE === 'keycloak') {
    void keycloak?.login();
    return;
  }
  useSessionStore.getState().clearMockUserId();
};
