import { AxiosError, type AxiosAdapter } from 'axios';
import { afterEach, describe, expect, it, vi } from 'vitest';

import { axiosInstance } from '@/config/api/axiosInstance';
import { ENDPOINTS } from '@/config/api/Endpoint';
import { CURRENT_USER_QUERY_KEY, queryClient } from '@/config/api/queryClient';

vi.mock('@/config/auth/authService', () => ({
  getAuthHeaders: vi.fn(async () => ({})),
  handleUnauthorized: vi.fn(),
}));

/** Adapter che risponde 403, con o senza codice dedicato. */
const reply403 =
  (code?: string): AxiosAdapter =>
  async (config) => {
    throw new AxiosError('Forbidden', '403', config, undefined, {
      status: 403,
      statusText: 'Forbidden',
      headers: {},
      config,
      data: { status: 403, message: 'Accesso negato', ...(code ? { code } : {}) },
    });
  };

describe('interceptor di risposta', () => {
  afterEach(() => vi.restoreAllMocks());

  it('utente non abilitato su un’API qualunque: ricarica il profilo', async () => {
    const invalidate = vi.spyOn(queryClient, 'invalidateQueries').mockResolvedValue();
    await expect(
      axiosInstance.get(ENDPOINTS.zones, { adapter: reply403('USER_NOT_ENABLED') }),
    ).rejects.toBeTruthy();
    expect(invalidate).toHaveBeenCalledWith({ queryKey: CURRENT_USER_QUERY_KEY });
  });

  it('sul profilo stesso non invalida (niente ciclo di richieste)', async () => {
    const invalidate = vi.spyOn(queryClient, 'invalidateQueries').mockResolvedValue();
    await expect(
      axiosInstance.get(ENDPOINTS.currentUser, { adapter: reply403('USER_NOT_ENABLED') }),
    ).rejects.toBeTruthy();
    expect(invalidate).not.toHaveBeenCalled();
  });

  it('un 403 di ruolo non tocca il profilo', async () => {
    const invalidate = vi.spyOn(queryClient, 'invalidateQueries').mockResolvedValue();
    await expect(axiosInstance.get(ENDPOINTS.ticketQueue, { adapter: reply403() })).rejects.toBeTruthy();
    expect(invalidate).not.toHaveBeenCalled();
  });
});
