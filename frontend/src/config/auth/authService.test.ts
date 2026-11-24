import { afterAll, beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';

import {
  getSessionExpiresAt,
  initAuth,
  keepSessionAlive,
  reloginNow,
  SESSION_KEEP_ALIVE_MS,
} from '@/config/auth/authService';

const kc = vi.hoisted(() => ({
  init: vi.fn(),
  updateToken: vi.fn(),
  login: vi.fn(),
  logout: vi.fn(),
  token: 'token',
  refreshToken: 'refresh-token' as string | undefined,
  refreshTokenParsed: { exp: 1_000 } as { exp?: number } | undefined,
  timeSkew: 5 as number | null,
}));

vi.mock('@/config/auth/authConfig', () => ({
  AUTH_MODE: 'keycloak',
  KEYCLOAK_CONFIG: { url: 'http://localhost:8180', realm: 'nexus', clientId: 'nexus-frontend' },
  MOCK_USER_HEADER: 'X-USER-ID',
}));

vi.mock('keycloak-js', () => ({
  default: vi.fn(function KeycloakFinto() {
    return kc;
  }),
}));

describe('authService con Keycloak (US-1310)', () => {
  beforeAll(async () => {
    vi.useFakeTimers({ toFake: ['setInterval', 'clearInterval'] });
    kc.init.mockResolvedValue(true);
    await initAuth();
  });

  afterAll(() => {
    vi.useRealTimers();
  });

  beforeEach(() => {
    kc.updateToken.mockReset().mockResolvedValue(true);
    kc.login.mockReset().mockResolvedValue(undefined);
    kc.refreshToken = 'refresh-token';
    kc.refreshTokenParsed = { exp: 1_000 };
    kc.timeSkew = 5;
  });

  it("dopo l'accesso rinnova il token ogni minuto, anche senza chiamate all'API", () => {
    vi.advanceTimersByTime(SESSION_KEEP_ALIVE_MS);
    expect(kc.updateToken).toHaveBeenCalledTimes(1);
    expect(kc.updateToken).toHaveBeenCalledWith(70);

    vi.advanceTimersByTime(SESSION_KEEP_ALIVE_MS * 3);
    expect(kc.updateToken).toHaveBeenCalledTimes(4);
  });

  it('con la rete assente non cambia pagina e riprova al giro dopo', async () => {
    kc.updateToken.mockRejectedValue(new Error('rete assente'));
    await expect(keepSessionAlive()).resolves.toBeUndefined();
    expect(kc.login).not.toHaveBeenCalled();
  });

  it('senza refresh token (sessione chiusa da keycloak-js) non prova a rinnovare', async () => {
    kc.refreshToken = undefined;
    await keepSessionAlive();
    expect(kc.updateToken).not.toHaveBeenCalled();
  });

  it('la fine della sessione è la scadenza del refresh token, corretta per lo scarto degli orologi', () => {
    expect(getSessionExpiresAt()).toBe(1_005_000);
    kc.timeSkew = null;
    expect(getSessionExpiresAt()).toBe(1_000_000);
    kc.refreshTokenParsed = undefined;
    expect(getSessionExpiresAt()).toBeNull();
  });

  it('il nuovo accesso chiede la password', async () => {
    await reloginNow();
    expect(kc.login).toHaveBeenCalledWith({ prompt: 'login' });
  });
});
