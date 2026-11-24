import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AxiosError, AxiosHeaders } from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { fetchUserDetails, updateUserDetails, type UserDetails } from '@/config/api/userAdminApi';
import { UserDetailsPage } from '@/pages/UserDetailsPage/UserDetailsPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderRoutes } from '@/tests/renderWithProviders';

vi.mock('@/config/api/userAdminApi', () => ({
  fetchUsers: vi.fn(),
  fetchUser: vi.fn(),
  fetchUserDetails: vi.fn(),
  updateUserDetails: vi.fn(),
}));

const neverEntered: UserDetails = {
  id: 'U2',
  username: 'mario.rosi',
  firstName: 'Mario',
  lastName: 'Rossi',
  email: 'mario.rossi@example.org',
  active: true,
  usernameEditable: true,
};

const error = (status: number, data: object): AxiosError =>
  new AxiosError(String(status), String(status), undefined, undefined, {
    status,
    statusText: String(status),
    headers: {},
    config: { headers: new AxiosHeaders() },
    data: { status, ...data },
  });

const renderPage = () =>
  renderRoutes({
    routes: [
      {
        path: '/utenti/:id/dati',
        element: (
          <main>
            <UserDetailsPage />
          </main>
        ),
      },
      { path: '/utenti', element: <p>Elenco degli utenti</p> },
    ],
    initialPath: '/utenti/U2/dati',
  });

describe('<UserDetailsPage>', () => {
  beforeEach(() => {
    vi.mocked(fetchUserDetails).mockReset().mockResolvedValue(neverEntered);
    vi.mocked(updateUserDetails).mockReset();
  });

  it('utente mai entrato: corregge lo username e torna all’elenco con l’esito', async () => {
    const user = userEvent.setup();
    const { router } = renderPage();

    expect(await screen.findByRole('heading', { level: 1, name: 'Dati di Mario Rossi' })).toBeInTheDocument();
    expect(screen.getByText(/Non è ancora entrato in NEXUS/)).toBeInTheDocument();
    const username = screen.getByLabelText(/^Username/);
    expect(username).not.toHaveAttribute('readonly');
    await expectNoAxeViolations();

    vi.mocked(updateUserDetails).mockResolvedValueOnce({
      user: { ...neverEntered, username: 'mario.rossi' },
    });
    await user.clear(username);
    await user.type(username, ' mario.rossi ');
    await user.click(screen.getByRole('button', { name: 'Salva i dati' }));

    expect(updateUserDetails).toHaveBeenCalledWith({
      id: 'U2',
      form: {
        firstName: 'Mario',
        lastName: 'Rossi',
        username: 'mario.rossi',
        email: 'mario.rossi@example.org',
        phone: null,
      },
    });
    await waitFor(() => expect(router.state.location.pathname).toBe('/utenti'));
    expect(router.state.location.state).toEqual({ notice: 'Dati di Mario Rossi salvati.' });
  });

  it('utente già entrato: username in sola lettura con il motivo collegato', async () => {
    vi.mocked(fetchUserDetails).mockResolvedValue({
      ...neverEntered,
      usernameEditable: false,
      phone: '055 1234',
    });
    renderPage();

    const username = await screen.findByLabelText(/^Username/);
    expect(username).toHaveAttribute('readonly');
    expect(username).toHaveValue('mario.rosi');
    expect(username).toHaveAccessibleDescription(/lo username non si cambia più/);
    expect(screen.getByLabelText(/^Telefono/)).toHaveValue('055 1234');
    expect(screen.getByText(/il vecchio indirizzo riceve un avviso del cambio/)).toBeInTheDocument();
    await expectNoAxeViolations();
  });

  it('email corretta: esito con il nuovo invito, o avviso se non è partito', async () => {
    const user = userEvent.setup();
    const { router } = renderPage();
    const email = await screen.findByLabelText(/^Email/);
    await user.clear(email);
    await user.type(email, 'mario.giusto@example.org');

    vi.mocked(updateUserDetails).mockResolvedValueOnce({
      user: { ...neverEntered, email: 'mario.giusto@example.org' },
      invitationSent: false,
    });
    await user.click(screen.getByRole('button', { name: 'Salva i dati' }));
    await waitFor(() => expect(router.state.location.pathname).toBe('/utenti'));
    expect(router.state.location.state).toEqual({
      notice:
        'Dati di Mario Rossi salvati, ma il nuovo invito per impostare la password non è partito. Usa «Invia di nuovo l’invito» quando la posta di Keycloak funziona.',
      alert: true,
    });
  });

  it('errori del server sul campo e Keycloak non raggiungibile', async () => {
    const user = userEvent.setup();
    renderPage();
    const button = await screen.findByRole('button', { name: 'Salva i dati' });

    vi.mocked(updateUserDetails).mockRejectedValueOnce(
      error(400, { fieldErrors: [{ field: 'username', message: 'Username già usato da un altro utente' }] }),
    );
    await user.click(button);
    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Username: Username già usato da un altro utente',
    );
    expect(screen.getByLabelText(/^Username/)).toHaveAttribute('aria-invalid', 'true');

    vi.mocked(updateUserDetails).mockRejectedValueOnce(
      error(503, { message: 'Non è stato possibile aggiornare l’account di accesso' }),
    );
    await user.click(button);
    expect(
      await screen.findByText('Non è stato possibile aggiornare l’account di accesso'),
    ).toBeInTheDocument();
  });

  it('dati non validi: riepilogo degli errori, nessuna chiamata', async () => {
    const user = userEvent.setup();
    renderPage();
    const lastName = await screen.findByLabelText(/^Cognome/);
    await user.clear(lastName);
    await user.type(screen.getByLabelText(/^Telefono/), 'abc');
    await user.click(screen.getByRole('button', { name: 'Salva i dati' }));

    const summary = await screen.findByRole('alert');
    expect(summary).toHaveTextContent('Cognome');
    expect(summary).toHaveTextContent('Telefono');
    expect(updateUserDetails).not.toHaveBeenCalled();
    await expectNoAxeViolations();
  });

  it('caricamento non riuscito: messaggio del server e «Riprova»', async () => {
    vi.mocked(fetchUserDetails).mockRejectedValue(
      error(503, { message: 'Non è stato possibile leggere l’account di accesso: riprova più tardi' }),
    );
    renderPage();
    expect(
      await screen.findByText(
        'Non è stato possibile leggere l’account di accesso: riprova più tardi',
        undefined,
        {
          timeout: 5000,
        },
      ),
    ).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Riprova' })).toBeInTheDocument();
  });
});
