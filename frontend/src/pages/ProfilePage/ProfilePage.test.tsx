import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AxiosError, AxiosHeaders } from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import {
  cancelEmailChange,
  confirmEmailChange,
  fetchCurrentUser,
  fetchMyEmail,
  requestEmailChange,
  type EmailStatus,
} from '@/config/api/userApi';
import { ProfilePage } from '@/pages/ProfilePage/ProfilePage';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderWithProviders } from '@/tests/renderWithProviders';

vi.mock('@/config/api/userApi', () => ({
  fetchCurrentUser: vi.fn(),
  fetchMyEmail: vi.fn(),
  requestEmailChange: vi.fn(),
  confirmEmailChange: vi.fn(),
  cancelEmailChange: vi.fn(),
}));

const error = (status: number, body: Record<string, unknown>) =>
  new AxiosError(String(status), String(status), undefined, undefined, {
    status,
    statusText: String(status),
    headers: {},
    config: { headers: new AxiosHeaders() },
    data: { status, ...body },
  });

const current: EmailStatus = { email: 'tutor1@nexus.local' };
// 10:30 UTC = 11:30 a Roma
const pendingChange = {
  newEmail: 'mario@example.org',
  expiresAt: '2026-11-05T10:30:00Z',
  resendAvailableAt: '2026-11-05T10:01:00Z',
  remainingAttempts: 5,
};
const pending: EmailStatus = { email: 'tutor1@nexus.local', pending: pendingChange };

const renderPage = () =>
  renderWithProviders(
    <main>
      <ProfilePage />
    </main>,
  );

describe('<ProfilePage>', () => {
  beforeEach(() => {
    vi.mocked(fetchCurrentUser).mockResolvedValue({
      id: 'U1',
      username: 'tutor1',
      firstName: 'Tutor',
      lastName: 'Uno',
      roles: ['TUTOR'],
    });
    vi.mocked(fetchMyEmail).mockResolvedValue(current);
    vi.mocked(requestEmailChange).mockReset();
    vi.mocked(confirmEmailChange).mockReset();
    vi.mocked(cancelEmailChange).mockReset();
  });

  it('dati dell’account e richiesta del codice al nuovo indirizzo', async () => {
    const user = userEvent.setup();
    renderPage();

    expect(screen.getByRole('heading', { level: 1, name: 'Il mio profilo' })).toBeInTheDocument();
    expect(await screen.findByText('tutor1@nexus.local')).toBeInTheDocument();
    expect(screen.getByText('Tutor Uno')).toBeInTheDocument();
    expect(screen.getByText('Tutor territoriale')).toBeInTheDocument();
    await expectNoAxeViolations();

    vi.mocked(requestEmailChange).mockResolvedValue(pending);
    vi.mocked(fetchMyEmail).mockResolvedValue(pending);
    await user.type(screen.getByLabelText(/Nuovo indirizzo email/), ' mario@example.org ');
    await user.click(screen.getByRole('button', { name: 'Invia il codice' }));

    expect(requestEmailChange).toHaveBeenCalledWith('mario@example.org');
    const sent = await screen.findByRole('status');
    expect(sent).toHaveTextContent('Codice inviato a mario@example.org');
    await waitFor(() => expect(sent).toHaveFocus());
    expect(
      screen.getByText(
        /Abbiamo inviato un codice di 6 cifre a mario@example.org. Il codice vale fino alle 11:30/,
      ),
    ).toBeInTheDocument();
    const code = screen.getByLabelText(/Codice ricevuto/);
    expect(code).toHaveAttribute('autocomplete', 'one-time-code');
    expect(code).toHaveAttribute('inputmode', 'numeric');
    await expectNoAxeViolations();
  });

  it('indirizzo non valido o uguale all’attuale: errore sul campo, nessuna richiesta', async () => {
    const user = userEvent.setup();
    renderPage();
    const field = await screen.findByLabelText(/Nuovo indirizzo email/);

    await user.type(field, 'non-valido');
    await user.click(screen.getByRole('button', { name: 'Invia il codice' }));
    expect(await screen.findByText(/Scrivi un indirizzo email valido/)).toBeInTheDocument();
    expect(field).toHaveAttribute('aria-invalid', 'true');

    await user.clear(field);
    await user.type(field, 'TUTOR1@nexus.local');
    await user.click(screen.getByRole('button', { name: 'Invia il codice' }));
    expect(await screen.findByText('È già il tuo indirizzo attuale')).toBeInTheDocument();
    expect(requestEmailChange).not.toHaveBeenCalled();
  });

  it('indirizzo già usato: il messaggio del server sul campo', async () => {
    const user = userEvent.setup();
    vi.mocked(requestEmailChange).mockRejectedValue(
      error(400, { fieldErrors: [{ field: 'newEmail', message: 'Indirizzo già usato da un altro utente' }] }),
    );
    renderPage();

    await user.type(await screen.findByLabelText(/Nuovo indirizzo email/), 'operatore@example.org');
    await user.click(screen.getByRole('button', { name: 'Invia il codice' }));

    expect(await screen.findByText('Indirizzo già usato da un altro utente')).toBeInTheDocument();
    await waitFor(() => expect(screen.getByLabelText(/Nuovo indirizzo email/)).toHaveFocus());
  });

  it('conferma col codice: indirizzo aggiornato', async () => {
    const user = userEvent.setup();
    vi.mocked(fetchMyEmail).mockResolvedValue(pending);
    renderPage();

    const changed: EmailStatus = { email: 'mario@example.org' };
    vi.mocked(confirmEmailChange).mockResolvedValue(changed);
    await user.type(await screen.findByLabelText(/Codice ricevuto/), '123456');
    vi.mocked(fetchMyEmail).mockResolvedValue(changed);
    await user.click(screen.getByRole('button', { name: 'Conferma il nuovo indirizzo' }));

    expect(confirmEmailChange).toHaveBeenCalledWith('123456');
    expect(await screen.findByRole('status')).toHaveTextContent(
      'Indirizzo email aggiornato: ora è mario@example.org.',
    );
    expect(screen.getByText('mario@example.org')).toBeInTheDocument();
    expect(screen.getByLabelText(/Nuovo indirizzo email/)).toBeInTheDocument();
  });

  it('codice nel formato sbagliato: errore sul campo; codice sbagliato: il messaggio del server', async () => {
    const user = userEvent.setup();
    vi.mocked(fetchMyEmail).mockResolvedValue(pending);
    renderPage();
    const code = await screen.findByLabelText(/Codice ricevuto/);

    await user.type(code, '12a');
    await user.click(screen.getByRole('button', { name: 'Conferma il nuovo indirizzo' }));
    expect(await screen.findByText('Il codice è di 6 cifre')).toBeInTheDocument();
    expect(confirmEmailChange).not.toHaveBeenCalled();

    vi.mocked(confirmEmailChange).mockRejectedValue(
      error(400, { message: 'Codice non valido: 4 tentativi rimasti' }),
    );
    await user.clear(code);
    await user.type(code, '000000');
    await user.click(screen.getByRole('button', { name: 'Conferma il nuovo indirizzo' }));

    const alert = await screen.findByRole('alert');
    expect(alert).toHaveTextContent('Codice non valido: 4 tentativi rimasti');
    await waitFor(() => expect(alert).toHaveFocus());
  });

  it('tentativi finiti: niente campo del codice, si chiede un nuovo codice', async () => {
    const user = userEvent.setup();
    vi.mocked(fetchMyEmail).mockResolvedValue({
      ...pending,
      pending: { ...pendingChange, remainingAttempts: 0 },
    });
    renderPage();

    expect(await screen.findByText(/Troppi tentativi sbagliati/)).toBeInTheDocument();
    expect(screen.queryByLabelText(/Codice ricevuto/)).not.toBeInTheDocument();

    vi.mocked(requestEmailChange).mockResolvedValue(pending);
    await user.click(screen.getByRole('button', { name: 'Invia un nuovo codice' }));
    expect(requestEmailChange).toHaveBeenCalledWith('mario@example.org');
    expect(await screen.findByRole('status')).toHaveTextContent('Codice inviato a mario@example.org');
  });

  it('nuovo codice troppo presto: il messaggio del server', async () => {
    const user = userEvent.setup();
    vi.mocked(fetchMyEmail).mockResolvedValue(pending);
    vi.mocked(requestEmailChange).mockRejectedValue(
      error(400, { message: 'Hai appena chiesto un codice: puoi chiederne un altro tra 42 secondi' }),
    );
    renderPage();

    await user.click(await screen.findByRole('button', { name: 'Invia un nuovo codice' }));
    expect(await screen.findByRole('alert')).toHaveTextContent('puoi chiederne un altro tra 42 secondi');
  });

  it('annullamento: si torna al nuovo indirizzo da scrivere', async () => {
    const user = userEvent.setup();
    vi.mocked(fetchMyEmail).mockResolvedValue(pending);
    vi.mocked(cancelEmailChange).mockResolvedValue(current);
    renderPage();

    await user.click(await screen.findByRole('button', { name: 'Annulla il cambio' }));

    expect(await screen.findByRole('status')).toHaveTextContent('Cambio annullato');
    expect(await screen.findByLabelText(/Nuovo indirizzo email/)).toBeInTheDocument();
  });

  it('errore di rete: messaggio generico', async () => {
    const user = userEvent.setup();
    vi.mocked(requestEmailChange).mockRejectedValue(new Error('Network Error'));
    renderPage();

    await user.type(await screen.findByLabelText(/Nuovo indirizzo email/), 'mario@example.org');
    await user.click(screen.getByRole('button', { name: 'Invia il codice' }));
    expect(await screen.findByRole('alert')).toHaveTextContent('Operazione non riuscita');
  });
});
