import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AxiosError, AxiosHeaders } from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { fetchMyProjects, fetchProjects } from '@/config/api/referenceApi';
import { fetchTickets, takeCharge, type TicketListItem } from '@/config/api/ticketApi';
import { fetchCurrentUser } from '@/config/api/userApi';
import type { Role } from '@/config/auth/types';
import { TicketsPage } from '@/pages/TicketsPage/TicketsPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderRoutes } from '@/tests/renderWithProviders';

vi.mock('@/config/api/ticketApi', () => ({
  fetchTickets: vi.fn(),
  submitTicket: vi.fn(),
  takeCharge: vi.fn(),
  fetchTicketWork: vi.fn(),
  fetchCompatibleJobSlots: vi.fn(),
  matchTicket: vi.fn(),
  takeOverTicket: vi.fn(),
}));
vi.mock('@/config/api/referenceApi', () => ({
  fetchZones: vi.fn(),
  fetchJobCategories: vi.fn(),
  fetchMyProjects: vi.fn(),
  fetchProjects: vi.fn(),
}));
vi.mock('@/config/api/userApi', () => ({ fetchCurrentUser: vi.fn() }));

const item = (number: number, overrides: Partial<TicketListItem> = {}): TicketListItem => ({
  id: `T${number}`,
  number,
  type: 'NORMAL',
  status: 'NUOVA',
  fastTrack: false,
  version: 0,
  beneficiary: {
    id: `B${number}`,
    firstName: 'Mario',
    lastName: `Rossi${number}`,
    birthYear: 1998,
    residenceZoneName: 'Firenze Novoli',
  },
  project: { id: 'P1', code: 'GOL', name: 'GOL' },
  requestedJobCategory: { id: 'J1', code: 'MAG', name: 'Magazziniere' },
  tutorName: 'Tutor Uno',
  assignedOperatorName: null,
  createdAt: '2026-10-24T08:15:00Z',
  ...overrides,
});

const asUser = (role: Role) =>
  vi.mocked(fetchCurrentUser).mockResolvedValue({
    id: 'u',
    username: 'u',
    firstName: 'Utente',
    lastName: 'Prova',
    roles: [role],
  });

const renderPage = (path = '/') =>
  renderRoutes({
    routes: [
      {
        path: '/',
        element: (
          <main>
            <TicketsPage />
          </main>
        ),
      },
    ],
    initialPath: path,
  });

describe('<TicketsPage>', () => {
  beforeEach(() => {
    vi.mocked(fetchTickets).mockReset();
    vi.mocked(fetchTickets).mockResolvedValue([
      item(7, { type: 'SPECIAL', fastTrack: true, status: 'IN_LAVORAZIONE' }),
      item(3),
    ]);
    vi.mocked(fetchProjects).mockResolvedValue([
      { id: 'P1', code: 'GOL', name: 'GOL' },
      { id: 'P2', code: 'POLIS', name: 'POLIS' },
    ]);
    vi.mocked(fetchMyProjects).mockResolvedValue([{ id: 'P1', code: 'GOL', name: 'GOL' }]);
    vi.mocked(takeCharge).mockReset();
  });

  it('Call Center: predefinita "Da prendere in carico", con fast-track, link alla lavorazione e presa in carico', async () => {
    asUser('CALL_CENTER');
    renderPage();

    const table = await screen.findByRole('table', { name: /Segnalazioni da prendere in carico/ });
    expect(fetchTickets).toHaveBeenCalledWith({ view: 'TO_TAKE', status: undefined, projectId: undefined });
    expect(screen.getByRole('link', { name: 'Da prendere in carico' })).toHaveAttribute(
      'aria-current',
      'page',
    );
    const [first] = within(table).getAllByRole('row').slice(1);
    expect(within(first).getByText('Fast-track')).toBeInTheDocument();
    expect(within(first).getByRole('link', { name: 'Apri la segnalazione n. 7' })).toHaveAttribute(
      'href',
      '/lavorazioni/T7',
    );
    expect(
      within(first).getByRole('button', { name: 'Prendi in carico la segnalazione n. 7' }),
    ).toBeInTheDocument();
    expect(screen.getByRole('status')).toHaveTextContent('2 segnalazioni');
    // nessun filtro per zona (nota 4 del PO)
    expect(screen.queryByRole('combobox', { name: /Zona/ })).not.toBeInTheDocument();
    await expectNoAxeViolations();
  });

  it('Call Center: la vista "Tutte" mostra chi ha in carico, senza pulsanti di presa in carico', async () => {
    const user = userEvent.setup();
    asUser('CALL_CENTER');
    vi.mocked(fetchTickets).mockResolvedValue([
      item(9, { status: 'PROPOSTA_AZIENDA', assignedOperatorName: 'Operatore Due' }),
    ]);
    const { router } = renderPage();

    await user.click(await screen.findByRole('link', { name: 'Tutte' }));

    await waitFor(() => expect(router.state.location.search).toBe('?vista=tutte'));
    expect(fetchTickets).toHaveBeenLastCalledWith({ view: 'ALL', status: undefined, projectId: undefined });
    const table = await screen.findByRole('table', { name: /Tutte le segnalazioni/ });
    expect(within(table).getByText('Operatore Due')).toBeInTheDocument();
    expect(within(table).getByText('Proposta all’azienda')).toBeInTheDocument();
    expect(within(table).queryByRole('button', { name: /Prendi in carico/ })).not.toBeInTheDocument();
  });

  it('filtri per stato e progetto nell’indirizzo e nella richiesta', async () => {
    const user = userEvent.setup();
    asUser('CALL_CENTER');
    const { router } = renderPage('/?vista=in-carico');
    await screen.findByRole('table');

    await user.selectOptions(screen.getByRole('combobox', { name: 'Stato' }), 'IN_LAVORAZIONE');
    await user.selectOptions(await screen.findByRole('combobox', { name: 'Progetto' }), 'P2');

    await waitFor(() =>
      expect(fetchTickets).toHaveBeenLastCalledWith({
        view: 'ASSIGNED_TO_ME',
        status: 'IN_LAVORAZIONE',
        projectId: 'P2',
      }),
    );
    expect(router.state.location.search).toBe('?vista=in-carico&stato=IN_LAVORAZIONE&progetto=P2');
    await user.click(screen.getByRole('button', { name: 'Azzera filtri' }));
    await waitFor(() => expect(router.state.location.search).toBe('?vista=in-carico'));
  });

  it('presa in carico: esito con focus e link alla lavorazione; conflitto spiegato', async () => {
    const user = userEvent.setup();
    asUser('CALL_CENTER');
    vi.mocked(takeCharge)
      .mockResolvedValueOnce(item(3, { status: 'IN_LAVORAZIONE', version: 1 }))
      .mockRejectedValueOnce(
        new AxiosError('Conflict', '409', undefined, undefined, {
          status: 409,
          statusText: 'Conflict',
          headers: {},
          config: { headers: new AxiosHeaders() },
          data: { status: 409, message: 'già presa' },
        }),
      );
    renderPage();

    await user.click(await screen.findByRole('button', { name: 'Prendi in carico la segnalazione n. 3' }));
    const ok = await screen.findByText('Segnalazione n. 3 presa in carico.');
    await waitFor(() => expect(ok.parentElement).toHaveFocus());
    expect(screen.getByRole('link', { name: 'Lavora la segnalazione n. 3' })).toHaveAttribute(
      'href',
      '/lavorazioni/T3',
    );

    await user.click(screen.getByRole('button', { name: 'Prendi in carico la segnalazione n. 7' }));
    const ko = await screen.findByText(/un collega l’ha appena presa/);
    await waitFor(() => expect(ko.parentElement).toHaveFocus());
  });

  it('Tutor: "Le mie segnalazioni" predefinita, "Del mio progetto" con il tutor, nessuna vista del Call Center', async () => {
    const user = userEvent.setup();
    asUser('TUTOR');
    renderPage();

    await screen.findByRole('table', { name: /Le mie segnalazioni/ });
    expect(fetchTickets).toHaveBeenCalledWith({ view: 'MINE', status: undefined, projectId: undefined });
    expect(screen.getByRole('link', { name: 'Nuova segnalazione' })).toHaveAttribute(
      'href',
      '/segnalazioni/nuova',
    );
    expect(screen.queryByRole('link', { name: 'Da prendere in carico' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /Prendi in carico/ })).not.toBeInTheDocument();
    expect(fetchProjects).not.toHaveBeenCalled();
    await expectNoAxeViolations();

    await user.click(screen.getByRole('link', { name: 'Del mio progetto' }));
    const table = await screen.findByRole('table', { name: /Segnalazioni dei miei progetti/ });
    expect(within(table).getByRole('columnheader', { name: 'Tutor' })).toBeInTheDocument();
    expect(fetchTickets).toHaveBeenLastCalledWith({
      view: 'PROJECT',
      status: undefined,
      projectId: undefined,
    });
  });

  it('vista vuota: messaggio al posto della tabella', async () => {
    asUser('CALL_CENTER');
    vi.mocked(fetchTickets).mockResolvedValue([]);
    renderPage('/?vista=in-carico');
    expect(await screen.findByText(/Non hai segnalazioni in carico/)).toBeInTheDocument();
    expect(screen.queryByRole('table')).not.toBeInTheDocument();
  });
});
