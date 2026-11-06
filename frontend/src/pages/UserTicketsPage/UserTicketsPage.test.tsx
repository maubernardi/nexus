import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AxiosError, AxiosHeaders } from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import type { TicketListItem } from '@/config/api/ticketApi';
import { fetchOpenTickets, transferTickets } from '@/config/api/ticketTransferApi';
import { fetchUser, type AdminUser } from '@/config/api/userAdminApi';
import { UserTicketsPage } from '@/pages/UserTicketsPage/UserTicketsPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderRoutes } from '@/tests/renderWithProviders';

vi.mock('@/config/api/userAdminApi', () => ({
  fetchUsers: vi.fn(),
  fetchUser: vi.fn(),
  createUser: vi.fn(),
  resendInvitation: vi.fn(),
  updateUserProjects: vi.fn(),
}));

vi.mock('@/config/api/ticketTransferApi', () => ({
  fetchOpenTickets: vi.fn(),
  transferTickets: vi.fn(),
}));

const tutor: AdminUser = {
  id: 'U1',
  username: 'mario.rossi',
  firstName: 'Mario',
  lastName: 'Rossi',
  email: 'mario@example.org',
  role: 'TUTOR',
  active: true,
  projects: [],
};

const ticket = (id: string, number: number, lastName: string): TicketListItem => ({
  id,
  number,
  type: 'NORMAL',
  status: 'IN_LAVORAZIONE',
  fastTrack: false,
  version: 0,
  beneficiary: { id: `B${id}`, firstName: 'Anna', lastName, birthYear: 1990, residenceZoneName: 'Novoli' },
  project: { id: 'P1', code: 'GOL', name: 'GOL' },
  requestedJobCategory: null,
  requestedJobFreeText: null,
  createdAt: '2026-11-01T09:00:00Z',
  tutorName: 'Mario Rossi',
  assignedOperatorName: null,
  timerDeadline: null,
  timerDueSoon: false,
});

const renderPage = () =>
  renderRoutes({
    routes: [
      {
        path: '/utenti/:id/segnalazioni',
        element: (
          <main>
            <UserTicketsPage />
          </main>
        ),
      },
    ],
    initialPath: '/utenti/U1/segnalazioni',
  });

describe('<UserTicketsPage>', () => {
  beforeEach(() => {
    vi.mocked(fetchUser).mockResolvedValue(tutor);
    vi.mocked(fetchOpenTickets).mockResolvedValue([
      {
        project: { id: 'P1', code: 'GOL', name: 'GOL' },
        tickets: [ticket('T1', 12, 'Bianchi'), ticket('T2', 15, 'Neri')],
        targets: [{ id: 'U2', name: 'Luca Verdi' }],
      },
      {
        project: { id: 'P2', code: 'POLIS', name: 'POLIS' },
        tickets: [{ ...ticket('T3', 20, 'Gialli'), project: { id: 'P2', code: 'POLIS', name: 'POLIS' } }],
        targets: [],
      },
    ]);
    vi.mocked(transferTickets).mockReset();
  });

  it('per progetto: segnalazioni già selezionate, Tutor da scegliere, poi la conferma', async () => {
    const user = userEvent.setup();
    renderPage();

    expect(
      await screen.findByRole('heading', { level: 1, name: 'Segnalazioni di Mario Rossi' }),
    ).toBeInTheDocument();
    const gol = screen.getByRole('region', { name: 'GOL: 2 segnalazioni aperte' });
    expect(
      within(gol).getByRole('checkbox', { name: 'n. 12 · Bianchi Anna · In lavorazione' }),
    ).toBeChecked();
    // nessun collega su POLIS: si spiega come fare
    const polis = screen.getByRole('region', { name: 'POLIS: 1 segnalazione aperta' });
    expect(within(polis).getByText(/Nessun altro Tutor attivo è assegnato a POLIS/)).toBeInTheDocument();
    expect(within(polis).queryByRole('button')).not.toBeInTheDocument();
    await expectNoAxeViolations();

    // senza Tutor scelto non parte
    await user.click(within(gol).getByRole('button', { name: 'Sposta 2 segnalazioni' }));
    expect(await within(gol).findByRole('alert')).toHaveTextContent('Scegli il Tutor');
    expect(transferTickets).not.toHaveBeenCalled();

    await user.click(within(gol).getByRole('checkbox', { name: /n\. 15/ }));
    await user.selectOptions(within(gol).getByRole('combobox', { name: 'Tutor che le riceve' }), 'U2');
    vi.mocked(transferTickets).mockResolvedValueOnce({ moved: 1, tutorName: 'Luca Verdi' });
    await user.click(within(gol).getByRole('button', { name: 'Sposta 1 segnalazione' }));

    expect(transferTickets).toHaveBeenCalledWith({ ticketIds: ['T1'], tutorId: 'U2' });
    const done = await screen.findByRole('status');
    expect(done).toHaveTextContent('1 segnalazione spostata a Luca Verdi, con il beneficiario.');
    await waitFor(() => expect(done).toHaveFocus());
  });

  it('errore del server', async () => {
    const user = userEvent.setup();
    renderPage();
    const gol = await screen.findByRole('region', { name: 'GOL: 2 segnalazioni aperte' });
    await user.selectOptions(within(gol).getByRole('combobox', { name: 'Tutor che le riceve' }), 'U2');
    vi.mocked(transferTickets).mockRejectedValueOnce(
      new AxiosError('400', '400', undefined, undefined, {
        status: 400,
        statusText: '400',
        headers: {},
        config: { headers: new AxiosHeaders() },
        data: { status: 400, message: 'La segnalazione n. 12 è chiusa: non si sposta' },
      }),
    );
    await user.click(within(gol).getByRole('button', { name: 'Sposta 2 segnalazioni' }));
    expect(await within(gol).findByRole('alert')).toHaveTextContent('La segnalazione n. 12 è chiusa');
  });

  it('nessuna segnalazione aperta', async () => {
    vi.mocked(fetchOpenTickets).mockResolvedValue([]);
    renderPage();
    expect(await screen.findByText('Nessuna segnalazione aperta.')).toBeInTheDocument();
  });
});
