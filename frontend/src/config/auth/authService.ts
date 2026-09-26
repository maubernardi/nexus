import type Keycloak from 'keycloak-js';

import { AUTH_MODE, KEYCLOAK_CONFIG, MOCK_USER_HEADER } from '@/config/auth/authConfig';
import { useSessionStore } from '@/config/stores/sessionStore';

let keycloak: Keycloak | null = null;

/** Da chiamare prima del primo render: in modalità keycloak forza il login (PKCE). */
export const initAuth = async (): Promise<void> => {
  if (AUTH_MODE !== 'keycloak') {
    return;
  }
  const { default: KeycloakClient } = await import('keycloak-js');
  keycloak = new KeycloakClient(KEYCLOAK_CONFIG);
  await keycloak.init({ onLoad: 'login-required', pkceMethod: 'S256', checkLoginIframe: false });
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
