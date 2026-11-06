import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AxiosError, AxiosHeaders } from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { createUser, fetchUsers } from '@/config/api/userAdminApi';
import { UserNewPage } from '@/pages/UserNewPage/UserNewPage';
import { UsersPage } from '@/pages/UsersPage/UsersPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderRoutes } from '@/tests/renderWithProviders';

vi.mock('@/config/api/userAdminApi', () => ({
  fetchUsers: vi.fn(),
  createUser: vi.fn(),
  resendInvitation: vi.fn(),
}));

const error = (status: number, body: Record<string, unknown>) =>
  new AxiosError(String(status), String(status), undefined, undefined, {
    status,
    statusText: String(status),
    headers: {},
    config: { headers: new AxiosHeaders() },
    data: { status, ...body },
  });

const created = (invitationSent: boolean) => ({
  user: {
    id: 'U9',
    username: 'mario.rossi',
    firstName: 'Mario',
    lastName: 'Rossi',
    email: 'mario@example.org',
    role: 'CALL_CENTER' as const,
    active: true,
  },
  invitationSent,
});

const renderPage = () =>
  renderRoutes({
    routes: [
      {
        path: '/utenti/nuovo',
        element: (
          <main>
            <UserNewPage />
          </main>
        ),
      },
      {
        path: '/utenti',
        element: (
          <main>
            <UsersPage />
          </main>
        ),
      },
    ],
    initialPath: '/utenti/nuovo',
  });

/** Nome e cognome: lo username si propone da sé. */
const fillValid = async (user: ReturnType<typeof userEvent.setup>): Promise<void> => {
  await user.type(screen.getByLabelText(/^Nome/), 'Mario');
  await user.type(screen.getByLabelText(/^Cognome/), 'Rossi');
  await user.tab();
  await user.type(screen.getByLabelText(/^Email/), 'mario@example.org');
  await user.selectOptions(screen.getByLabelText(/^Ruolo/), 'Operatore Call Center');
};

describe('<UserNewPage>', () => {
  beforeEach(() => {
    vi.mocked(createUser).mockReset();
    vi.mocked(fetchUsers).mockResolvedValue([created(true).user]);
  });

  it('username proposto come nome.cognome; creato, torna all’elenco con l’esito', async () => {
    const user = userEvent.setup();
    renderPage();
    expect(screen.getByRole('heading', { level: 1, name: 'Nuovo utente' })).toBeInTheDocument();
    await expectNoAxeViolations();

    await fillValid(user);
    expect(screen.getByLabelText(/^Username/)).toHaveValue('mario.rossi');
    vi.mocked(createUser).mockResolvedValue(created(true));
    await user.click(screen.getByRole('button', { name: 'Crea l’utente' }));

    expect(createUser).toHaveBeenCalledWith({
      firstName: 'Mario',
      lastName: 'Rossi',
      username: 'mario.rossi',
      email: 'mario@example.org',
      phone: null,
      role: 'CALL_CENTER',
    });
    const outcome = await screen.findByRole('status');
    expect(outcome).toHaveTextContent(
      'Utente Mario Rossi creato. Abbiamo inviato a mario@example.org il collegamento per impostare la password',
    );
    await waitFor(() => expect(outcome).toHaveFocus());
    expect(screen.getByRole('heading', { level: 1, name: 'Utenti' })).toBeInTheDocument();
  });

  it('invito non partito: l’elenco lo dice e propone il reinvio', async () => {
    const user = userEvent.setup();
    renderPage();
    await fillValid(user);
    vi.mocked(createUser).mockResolvedValue(created(false));
    await user.click(screen.getByRole('button', { name: 'Crea l’utente' }));

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'l’email per impostare la password non è partita. Usa «Invia di nuovo l’invito»',
    );
  });

  it('campi obbligatori e formati: riepilogo degli errori, nessuna richiesta', async () => {
    const user = userEvent.setup();
    renderPage();
    await user.type(screen.getByLabelText(/^Username/), 'mario rossi');
    await user.type(screen.getByLabelText(/^Email/), 'non-valida');
    await user.click(screen.getByRole('button', { name: 'Crea l’utente' }));

    const summary = await screen.findByRole('alert');
    expect(summary).toHaveTextContent('Nome: Campo obbligatorio');
    expect(summary).toHaveTextContent('Username: Solo lettere, cifre e i separatori');
    expect(summary).toHaveTextContent('Email: Scrivi un indirizzo email valido');
    expect(summary).toHaveTextContent('Ruolo: Campo obbligatorio');
    expect(createUser).not.toHaveBeenCalled();
  });

  it('username già usato: il messaggio del server sul campo', async () => {
    const user = userEvent.setup();
    renderPage();
    await fillValid(user);
    vi.mocked(createUser).mockRejectedValue(
      error(400, { fieldErrors: [{ field: 'username', message: 'Username già usato da un altro utente' }] }),
    );
    await user.click(screen.getByRole('button', { name: 'Crea l’utente' }));

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Username: Username già usato da un altro utente',
    );
    expect(screen.getByLabelText(/^Username/)).toHaveAttribute('aria-invalid', 'true');
  });

  it('Keycloak non raggiungibile: il messaggio del server', async () => {
    const user = userEvent.setup();
    renderPage();
    await fillValid(user);
    vi.mocked(createUser).mockRejectedValue(
      error(503, {
        message: 'Non è stato possibile creare l’account di accesso: l’utente non è stato creato',
      }),
    );
    await user.click(screen.getByRole('button', { name: 'Crea l’utente' }));

    expect(await screen.findByText(/l’utente non è stato creato/)).toBeInTheDocument();
  });
});
