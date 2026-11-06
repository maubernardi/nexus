import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AxiosError, AxiosHeaders } from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { fetchUsers, resendInvitation, type AdminUser } from '@/config/api/userAdminApi';
import { UsersPage } from '@/pages/UsersPage/UsersPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderRoutes } from '@/tests/renderWithProviders';

vi.mock('@/config/api/userAdminApi', () => ({
  fetchUsers: vi.fn(),
  createUser: vi.fn(),
  resendInvitation: vi.fn(),
}));

const users: AdminUser[] = [
  {
    id: 'U1',
    username: 'mario.rossi',
    firstName: 'Mario',
    lastName: 'Rossi',
    email: 'mario@example.org',
    role: 'TUTOR',
    active: true,
  },
  {
    id: 'U2',
    username: 'anna.verdi',
    firstName: 'Anna',
    lastName: 'Verdi',
    email: 'anna@example.org',
    role: 'CALL_CENTER',
    active: false,
  },
];

const renderPage = () =>
  renderRoutes({
    routes: [
      {
        path: '/utenti',
        element: (
          <main>
            <UsersPage />
          </main>
        ),
      },
    ],
    initialPath: '/utenti',
  });

describe('<UsersPage>', () => {
  beforeEach(() => {
    vi.mocked(fetchUsers).mockResolvedValue(users);
    vi.mocked(resendInvitation).mockReset();
  });

  it('elenco con ruolo e stato, collegamento al nuovo utente', async () => {
    renderPage();

    expect(screen.getByRole('heading', { level: 1, name: 'Utenti' })).toBeInTheDocument();
    const table = await screen.findByRole('table', { name: 'Utenti: 2 persone' });
    const rows = within(table).getAllByRole('row');
    expect(rows[1]).toHaveTextContent('Rossi Mario');
    expect(rows[1]).toHaveTextContent('Tutor territoriale');
    expect(rows[1]).toHaveTextContent('Attivo');
    expect(rows[2]).toHaveTextContent('Disattivato');
    // l'invito si reinvia solo agli utenti attivi
    expect(within(rows[2]).queryByRole('button')).not.toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Nuovo utente' })).toHaveAttribute('href', '/utenti/nuovo');
    await expectNoAxeViolations();
  });

  it('nuovo invito: conferma o messaggio del server', async () => {
    const user = userEvent.setup();
    renderPage();
    const button = await screen.findByRole('button', { name: 'Invia di nuovo l’invito a Rossi Mario' });

    vi.mocked(resendInvitation).mockResolvedValueOnce();
    await user.click(button);
    expect(resendInvitation).toHaveBeenCalledWith('U1');
    const sent = await screen.findByRole('status');
    expect(sent).toHaveTextContent('Invito inviato a mario@example.org');
    await waitFor(() => expect(sent).toHaveFocus());

    vi.mocked(resendInvitation).mockRejectedValueOnce(
      new AxiosError('503', '503', undefined, undefined, {
        status: 503,
        statusText: '503',
        headers: {},
        config: { headers: new AxiosHeaders() },
        data: { status: 503, message: 'L’email per impostare la password non è partita' },
      }),
    );
    await user.click(button);
    expect(await screen.findByRole('alert')).toHaveTextContent(
      'L’email per impostare la password non è partita',
    );
  });
});
