import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AxiosError, AxiosHeaders } from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { fetchJobCategories, fetchZones } from '@/config/api/referenceApi';
import {
  fetchCompatibleJobSlots,
  fetchTicketWork,
  matchTicket,
  type JobSlotMatch,
  type TicketDetail,
} from '@/config/api/ticketApi';
import { fetchCurrentUser } from '@/config/api/userApi';
import { TicketWorkPage } from '@/pages/TicketWorkPage/TicketWorkPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderRoutes } from '@/tests/renderWithProviders';

vi.mock('@/config/api/ticketApi', () => ({
  submitTicket: vi.fn(),
  fetchQueue: vi.fn(),
  takeCharge: vi.fn(),
  fetchAssignedToMe: vi.fn(),
  fetchTicketWork: vi.fn(),
  fetchCompatibleJobSlots: vi.fn(),
  matchTicket: vi.fn(),
}));
vi.mock('@/config/api/referenceApi', () => ({
  fetchZones: vi.fn(),
  fetchJobCategories: vi.fn(),
  fetchMyProjects: vi.fn(),
  fetchProjects: vi.fn(),
}));
vi.mock('@/config/api/userApi', () => ({ fetchCurrentUser: vi.fn() }));

const NOVOLI = { id: 'Z1', code: 'NOVOLI', name: 'Novoli' };
const MAG = { id: 'J1', code: 'MAG', name: 'Magazziniere' };

const detail = (overrides: Partial<TicketDetail> = {}): TicketDetail => ({
  id: 'T9',
  number: 9,
  type: 'NORMAL',
  status: 'IN_LAVORAZIONE',
  fastTrack: false,
  version: 2,
  beneficiary: {
    id: 'B1',
    firstName: 'Anna',
    lastName: 'Bianchi',
    birthYear: 1999,
    residenceZoneName: 'Novoli',
  },
  beneficiaryZone: NOVOLI,
  project: { id: 'P1', code: 'GOL', name: 'GOL' },
  requestedJobCategory: MAG,
  requestedJobFreeText: null,
  tutorName: 'Tutor Uno',
  assignedOperatorName: 'Operatore Call Center',
  assignedToMe: true,
  proposal: null,
  createdAt: '2026-10-01T08:00:00Z',
  ...overrides,
});

const alfa: JobSlotMatch = {
  id: 'S1',
  title: 'Magazziniere',
  description: null,
  companyId: 'C1',
  companyName: 'Alfa Logistica',
  jobCategory: MAG,
  zone: NOVOLI,
  version: 4,
};

const renderPage = () =>
  renderRoutes({
    routes: [
      {
        path: '/lavorazioni/:id',
        element: (
          <main>
            <TicketWorkPage />
          </main>
        ),
      },
    ],
    initialPath: '/lavorazioni/T9',
  });

describe('<TicketWorkPage>', () => {
  beforeEach(() => {
    vi.mocked(fetchCurrentUser).mockResolvedValue({
      id: 'operatore.cc',
      username: 'operatore.cc',
      firstName: 'Operatore',
      lastName: 'Call Center',
      roles: ['CALL_CENTER'],
    });
    vi.mocked(fetchTicketWork).mockResolvedValue(detail());
    vi.mocked(fetchZones).mockResolvedValue([NOVOLI, { id: 'Z2', code: 'FIESOLE', name: 'Fiesole' }]);
    vi.mocked(fetchJobCategories).mockResolvedValue([MAG]);
    vi.mocked(fetchCompatibleJobSlots).mockReset();
    vi.mocked(fetchCompatibleJobSlots).mockResolvedValue([alfa]);
    vi.mocked(matchTicket).mockReset();
  });

  it('cerca di default per zona del beneficiario e mansione richiesta; i filtri si allargano', async () => {
    const user = userEvent.setup();
    renderPage();

    expect(await screen.findByRole('heading', { level: 1, name: 'Segnalazione n. 9' })).toBeInTheDocument();
    await screen.findByRole('table', { name: /proponibili/ });
    expect(fetchCompatibleJobSlots).toHaveBeenCalledWith('T9', { zoneId: 'Z1', jobCategoryId: 'J1' });
    expect(screen.getByRole('status')).toHaveTextContent('1 mansione compatibile');
    await expectNoAxeViolations();

    await user.selectOptions(screen.getByRole('combobox', { name: 'Zona' }), 'Tutte le zone');
    await waitFor(() =>
      expect(fetchCompatibleJobSlots).toHaveBeenLastCalledWith('T9', {
        zoneId: undefined,
        jobCategoryId: 'J1',
      }),
    );
  });

  it('proposta: conferma con focus, poi esito con focus e versioni inviate', async () => {
    const user = userEvent.setup();
    vi.mocked(matchTicket).mockResolvedValue(
      detail({ status: 'PROPOSTA_AZIENDA', version: 3, proposal: alfa }),
    );
    renderPage();

    await user.click(
      await screen.findByRole('button', { name: 'Proponi Magazziniere presso Alfa Logistica' }),
    );
    expect(screen.getByRole('heading', { name: 'Conferma la proposta' })).toHaveFocus();
    expect(
      screen.getByText(/Proporre Anna Bianchi per la mansione «Magazziniere» presso Alfa Logistica/),
    ).toBeInTheDocument();
    await expectNoAxeViolations();
    await user.click(screen.getByRole('button', { name: 'Conferma proposta' }));

    const outcome = await screen.findByText(/Proposta inviata: «Magazziniere» presso Alfa Logistica/);
    await waitFor(() => expect(outcome).toHaveFocus());
    expect(matchTicket).toHaveBeenCalledWith(
      { id: 'T9', jobSlotId: 'S1', ticketVersion: 2, jobSlotVersion: 4 },
      expect.anything(),
    );
  });

  it('mansione appena bloccata: spiega il motivo', async () => {
    const user = userEvent.setup();
    vi.mocked(matchTicket).mockRejectedValue(
      new AxiosError('Conflict', '409', undefined, undefined, {
        status: 409,
        statusText: 'Conflict',
        headers: {},
        config: { headers: new AxiosHeaders() },
        data: { status: 409, message: 'La mansione «Magazziniere» non è più disponibile.' },
      }),
    );
    renderPage();
    await user.click(await screen.findByRole('button', { name: /^Proponi Magazziniere/ }));
    await user.click(screen.getByRole('button', { name: 'Conferma proposta' }));

    expect(
      await screen.findByText(/Proposta non riuscita: La mansione «Magazziniere» non è più disponibile/),
    ).toHaveFocus();
  });

  it('in carico a un collega: niente ricerca, spiegazione', async () => {
    vi.mocked(fetchTicketWork).mockResolvedValue(
      detail({ assignedToMe: false, assignedOperatorName: 'Collega Due' }),
    );
    renderPage();

    expect(
      await screen.findByText(/in carico a Collega Due: solo chi l’ha in carico può abbinarla/),
    ).toBeInTheDocument();
    expect(screen.queryByRole('heading', { name: 'Mansioni compatibili' })).not.toBeInTheDocument();
    expect(fetchCompatibleJobSlots).not.toHaveBeenCalled();
  });

  it('dopo l’abbinamento mostra la proposta e non la ricerca', async () => {
    vi.mocked(fetchTicketWork).mockResolvedValue(detail({ status: 'PROPOSTA_AZIENDA', proposal: alfa }));
    renderPage();

    const proposal = await screen.findByRole('region', { name: 'Proposta all’azienda' });
    expect(within(proposal).getByText('Alfa Logistica')).toBeInTheDocument();
    expect(screen.queryByRole('heading', { name: 'Mansioni compatibili' })).not.toBeInTheDocument();
    await expectNoAxeViolations();
  });
});
