import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AxiosError, AxiosHeaders } from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import {
  changeUserRole,
  fetchUser,
  fetchUserImpact,
  setUserActive,
  type AdminUser,
} from '@/config/api/userAdminApi';
import { UserAccountPage } from '@/pages/UserAccountPage/UserAccountPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderRoutes } from '@/tests/renderWithProviders';

vi.mock('@/config/api/userAdminApi', () => ({
  fetchUsers: vi.fn(),
  fetchUser: vi.fn(),
  fetchUserImpact: vi.fn(),
  createUser: vi.fn(),
  resendInvitation: vi.fn(),
  updateUserProjects: vi.fn(),
  setUserActive: vi.fn(),
  changeUserRole: vi.fn(),
}));

const operator: AdminUser = {
  id: 'U2',
  username: 'anna.verdi',
  firstName: 'Anna',
  lastName: 'Verdi',
  email: 'anna@example.org',
  role: 'CALL_CENTER',
  active: true,
  projects: [],
};

const renderPage = () =>
  renderRoutes({
    routes: [
      {
        path: '/utenti/:id/accesso',
        element: (
          <main>
            <UserAccountPage />
          </main>
        ),
      },
      { path: '/utenti', element: <p>Elenco degli utenti</p> },
    ],
    initialPath: '/utenti/U2/accesso',
  });

describe('<UserAccountPage>', () => {
  beforeEach(() => {
    vi.mocked(fetchUser).mockResolvedValue(operator);
    vi.mocked(fetchUserImpact).mockResolvedValue({ openTickets: 0, ticketsInCharge: 2, self: false });
    vi.mocked(setUserActive).mockReset();
    vi.mocked(changeUserRole).mockReset();
  });

  it('disattivazione con l’avviso dei ticket in carico, poi torna all’elenco', async () => {
    const user = userEvent.setup();
    const { router } = renderPage();

    expect(
      await screen.findByRole('heading', { level: 1, name: 'Accesso e ruolo di Anna Verdi' }),
    ).toBeInTheDocument();
    expect(screen.getByText(/Ha 2 ticket in carico come operatore: torneranno in coda/)).toBeInTheDocument();
    await expectNoAxeViolations();

    vi.mocked(setUserActive).mockResolvedValueOnce({ ...operator, active: false });
    await user.click(screen.getByRole('button', { name: 'Disattiva l’utente' }));
    expect(setUserActive).toHaveBeenCalledWith({ id: 'U2', active: false });
    await waitFor(() => expect(router.state.location.pathname).toBe('/utenti'));
    expect(router.state.location.state).toEqual({
      notice: 'Anna Verdi è disattivato: non può più accedere.',
    });
  });

  it('cambio di ruolo; errore di Keycloak mostrato', async () => {
    const user = userEvent.setup();
    renderPage();
    const button = await screen.findByRole('button', { name: 'Cambia ruolo' });
    expect(button).toBeDisabled();
    await user.selectOptions(screen.getByRole('combobox', { name: 'Ruolo' }), 'TUTOR');
    expect(screen.getByText(/Da Tutor vedrà solo i progetti assegnati/)).toBeInTheDocument();

    vi.mocked(changeUserRole).mockRejectedValueOnce(
      new AxiosError('503', '503', undefined, undefined, {
        status: 503,
        statusText: '503',
        headers: {},
        config: { headers: new AxiosHeaders() },
        data: { status: 503, message: 'Non è stato possibile aggiornare l’account di accesso' },
      }),
    );
    await user.click(button);
    expect(changeUserRole).toHaveBeenCalledWith({ id: 'U2', role: 'TUTOR' });
    const alert = await screen.findByRole('alert');
    expect(alert).toHaveTextContent('Non è stato possibile aggiornare l’account di accesso');
    await waitFor(() => expect(alert).toHaveFocus());
  });

  it('Tutor con segnalazioni aperte: niente pulsanti, rimanda allo spostamento', async () => {
    vi.mocked(fetchUser).mockResolvedValue({ ...operator, role: 'TUTOR' });
    vi.mocked(fetchUserImpact).mockResolvedValue({ openTickets: 3, ticketsInCharge: 0, self: false });
    renderPage();
    expect(await screen.findByText(/Ha 3 segnalazioni aperte come Tutor/)).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Sposta le segnalazioni' })).toHaveAttribute(
      'href',
      '/utenti/U2/segnalazioni',
    );
    expect(screen.queryByRole('button', { name: 'Disattiva l’utente' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Cambia ruolo' })).not.toBeInTheDocument();
    await expectNoAxeViolations();
  });

  it('il proprio utente non si tocca', async () => {
    vi.mocked(fetchUserImpact).mockResolvedValue({ openTickets: 0, ticketsInCharge: 0, self: true });
    renderPage();
    expect(await screen.findByText(/Questo è il tuo utente/)).toBeInTheDocument();
    expect(screen.queryByRole('button')).not.toBeInTheDocument();
  });

  it('utente disattivato: si riattiva', async () => {
    const user = userEvent.setup();
    vi.mocked(fetchUser).mockResolvedValue({ ...operator, active: false });
    vi.mocked(fetchUserImpact).mockResolvedValue({ openTickets: 0, ticketsInCharge: 0, self: false });
    renderPage();
    vi.mocked(setUserActive).mockResolvedValueOnce(operator);
    await user.click(await screen.findByRole('button', { name: 'Riattiva l’utente' }));
    expect(setUserActive).toHaveBeenCalledWith({ id: 'U2', active: true });
  });
});
