import type { AuthMode } from '@/config/auth/types';

export const AUTH_MODE: AuthMode = import.meta.env.VITE_AUTH_MODE === 'keycloak' ? 'keycloak' : 'mock';

export const KEYCLOAK_CONFIG = {
  url: import.meta.env.VITE_KEYCLOAK_URL ?? 'http://localhost:8180',
  realm: import.meta.env.VITE_KEYCLOAK_REALM ?? 'nexus',
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID ?? 'nexus-frontend',
} as const;

export const MOCK_USER_HEADER = 'X-USER-ID';
