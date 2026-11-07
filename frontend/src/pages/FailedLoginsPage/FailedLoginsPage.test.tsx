import { screen } from '@testing-library/react';
import { AxiosError, AxiosHeaders } from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { fetchFailedLogins } from '@/config/api/accessLogApi';
import { FailedLoginsPage } from '@/pages/FailedLoginsPage/FailedLoginsPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderRoutes } from '@/tests/renderWithProviders';

vi.mock('@/config/api/accessLogApi', () => ({
  fetchMyAccessLog: vi.fn(),
  fetchUserAccessLog: vi.fn(),
  fetchFailedLogins: vi.fn(),
}));

const renderPage = () =>
  renderRoutes({
    routes: [
      {
        path: '/utenti/accessi-falliti',
        element: (
          <main>
            <FailedLoginsPage />
          </main>
        ),
      },
    ],
    initialPath: '/utenti/accessi-falliti',
  });

describe('<FailedLoginsPage>', () => {
  beforeEach(() => {
    vi.mocked(fetchFailedLogins).mockReset();
  });

  it('tentativi falliti di tutti, anche con uno username inesistente', async () => {
    vi.mocked(fetchFailedLogins).mockResolvedValue({
      entries: [
        {
          at: '2026-11-07T11:46:00Z',
          success: false,
          username: 'nessuno.cosi',
          ipAddress: '203.0.113.8',
          error: 'user_not_found',
        },
        {
          at: '2026-11-07T11:45:00Z',
          success: false,
          username: 'mario.rossi',
          userName: 'Mario Rossi',
          ipAddress: '203.0.113.7',
          error: 'user_temporarily_disabled',
        },
      ],
      hasMore: false,
    });
    renderPage();

    expect(
      await screen.findByRole('heading', { level: 1, name: 'Accessi non riusciti' }),
    ).toBeInTheDocument();
    const table = await screen.findByRole('table', { name: 'Accessi non riusciti, dal più recente' });
    const rows = screen.getAllByRole('row');
    expect(rows[1]).toHaveTextContent('Utente inesistente');
    expect(rows[1]).toHaveTextContent('nessuno.cosi');
    expect(rows[1]).toHaveTextContent('Non riuscito: utente inesistente');
    expect(rows[2]).toHaveTextContent('Mario Rossi');
    expect(rows[2]).toHaveTextContent('Non riuscito: bloccato per troppi tentativi');
    expect(table).toHaveTextContent('203.0.113.8');
    expect(screen.queryByRole('button', { name: 'Mostra accessi più vecchi' })).not.toBeInTheDocument();
    await expectNoAxeViolations();
  });

  it('registro non disponibile: il messaggio del server e «Riprova»', async () => {
    vi.mocked(fetchFailedLogins).mockRejectedValue(
      new AxiosError('503', '503', undefined, undefined, {
        status: 503,
        statusText: '503',
        headers: {},
        config: { headers: new AxiosHeaders() },
        data: { status: 503, message: 'Il registro degli accessi non è disponibile: riprova più tardi' },
      }),
    );
    renderPage();

    expect(await screen.findByRole('alert', {}, { timeout: 3000 })).toHaveTextContent(
      'Il registro degli accessi non è disponibile',
    );
    expect(screen.getByRole('button', { name: 'Riprova' })).toBeInTheDocument();
  });
});
