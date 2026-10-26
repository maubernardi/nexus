import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AxiosError, AxiosHeaders } from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { fetchJobCategories, fetchZones } from '@/config/api/referenceApi';
import {
  fetchCompatibleJobSlots,
  fetchTicket,
  fetchTicketHistory,
  matchTicket,
  takeOverTicket,
  type JobSlotMatch,
  type TicketDetail,
} from '@/config/api/ticketApi';
import { fetchCurrentUser } from '@/config/api/userApi';
import { TicketPage } from '@/pages/TicketPage/TicketPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderRoutes } from '@/tests/renderWithProviders';

vi.mock('@/config/api/ticketApi', () => ({
  submitTicket: vi.fn(),
  fetchTickets: vi.fn(),
  takeCharge: vi.fn(),
  fetchTicket: vi.fn(),
  fetchTicketHistory: vi.fn(),
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
vi.mock('@/config/api/companyApi', () => ({
  fetchCompany: vi.fn().mockResolvedValue({
    id: 'C1',
    name: 'Alfa Logistica',
    vatCode: '12345678901',
    legalAddress: 'Via Roma 1, Firenze',
    contactPerson: 'Paola Neri',
    phone: null,
    email: 'info@alfa.test',
    active: true,
    version: 1,
    updatedAt: '2026-10-01T08:00:00Z',
  }),
  searchCompanies: vi.fn(),
  createCompany: vi.fn(),
  updateCompany: vi.fn(),
  setCompanyActive: vi.fn(),
}));
vi.mock('@/config/api/companyNoteApi', () => ({
  fetchCompanyNotes: vi.fn().mockResolvedValue([
    {
      id: 'N1',
      text: 'Contattare al mattino',
      createdAt: '2026-10-20T09:00:00Z',
      authorName: 'Paolo Verdi',
    },
  ]),
  addCompanyNote: vi.fn(),
}));
vi.mock('@/config/api/companyHistoryApi', () => ({
  fetchCompanyHistory: vi.fn().mockResolvedValue({
    summary: {
      proposals: 2,
      accepted: 1,
      failedAppointments: 0,
      internships: 1,
      hirings: 1,
      negativeOutcomes: 0,
    },
    events: [],
  }),
}));

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
        path: '/segnalazioni/:id',
        element: (
          <main>
            <TicketPage />
          </main>
        ),
      },
    ],
    initialPath: '/segnalazioni/T9',
  });

describe('<TicketPage>', () => {
  beforeEach(() => {
    vi.mocked(fetchCurrentUser).mockResolvedValue({
      id: 'operatore.cc',
      username: 'operatore.cc',
      firstName: 'Operatore',
      lastName: 'Call Center',
      roles: ['CALL_CENTER'],
    });
    vi.mocked(fetchTicket).mockResolvedValue(detail());
    vi.mocked(fetchTicketHistory).mockResolvedValue([
      {
        at: '2026-10-01T08:00:00Z',
        authorName: 'Tutor Uno',
        action: 'SUBMIT',
        fromStatus: null,
        toStatus: 'NUOVA',
        detail: null,
        note: null,
      },
      {
        at: '2026-10-01T09:00:00Z',
        authorName: 'Operatore Call Center',
        action: 'TAKE_CHARGE',
        fromStatus: 'NUOVA',
        toStatus: 'IN_LAVORAZIONE',
        detail: 'Operatore Call Center',
        note: 'Chiamato il tutor',
      },
    ]);
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

  it('Dettagli: dialogo con azienda, mansione, note e pregressi; Esc riporta il focus', async () => {
    const user = userEvent.setup();
    renderPage();

    const details = await screen.findByRole('button', {
      name: 'Dettagli Magazziniere presso Alfa Logistica',
    });
    await user.click(details);

    const dialog = await screen.findByRole('dialog', { name: '«Magazziniere» presso Alfa Logistica' });
    expect(await within(dialog).findByText('Paola Neri')).toBeInTheDocument();
    expect(within(dialog).getByRole('heading', { level: 3, name: 'Mansione' })).toBeInTheDocument();
    expect(await within(dialog).findByText('Contattare al mattino')).toBeInTheDocument();
    expect(await within(dialog).findByText('Proposte')).toBeInTheDocument();
    expect(
      within(dialog).getByRole('link', { name: 'Apri la scheda completa dell’azienda' }),
    ).toHaveAttribute('href', '/aziende/C1');
    await expectNoAxeViolations();

    await user.keyboard('{Escape}');
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument());
    await waitFor(() => expect(details).toHaveFocus());
  });

  it('Dettagli: "Proponi" dal dialogo apre la stessa conferma', async () => {
    const user = userEvent.setup();
    renderPage();

    await user.click(
      await screen.findByRole('button', { name: 'Dettagli Magazziniere presso Alfa Logistica' }),
    );
    const dialog = await screen.findByRole('dialog');
    await user.click(within(dialog).getByRole('button', { name: 'Proponi' }));

    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument());
    await waitFor(() => expect(screen.getByRole('heading', { name: 'Conferma la proposta' })).toHaveFocus());
    expect(matchTicket).not.toHaveBeenCalled();
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

  it('in carico a un collega: si può lavorare e prendere in carico, l’assegnazione resta', async () => {
    const user = userEvent.setup();
    vi.mocked(fetchTicket).mockResolvedValue(
      detail({ assignedToMe: false, assignedOperatorName: 'Collega Due' }),
    );
    vi.mocked(takeOverTicket).mockResolvedValue(detail({ assignedToMe: true, version: 3 }));
    renderPage();

    expect(await screen.findByText(/in carico a Collega Due/)).toBeInTheDocument();
    expect(screen.getByText('Puoi lavorarla anche tu: l’assegnazione resta al collega.')).toBeInTheDocument();
    expect(await screen.findByRole('heading', { name: 'Mansioni compatibili' })).toBeInTheDocument();
    await screen.findByRole('button', { name: /^Proponi Magazziniere/ });
    await expectNoAxeViolations();

    await user.click(screen.getByRole('button', { name: 'Prendila in carico tu' }));
    expect(await screen.findByText(/Ora la segnalazione è in carico a te/)).toHaveFocus();
    expect(takeOverTicket).toHaveBeenCalledWith({ id: 'T9', version: 2 }, expect.anything());
  });

  it('non ancora presa in carico: niente ricerca, rimanda alla coda', async () => {
    vi.mocked(fetchTicket).mockResolvedValue(detail({ assignedToMe: false, assignedOperatorName: null }));
    renderPage();

    expect(
      await screen.findByText('Prendi in carico la segnalazione dalla coda per poterla abbinare.'),
    ).toBeInTheDocument();
    expect(screen.queryByRole('heading', { name: 'Mansioni compatibili' })).not.toBeInTheDocument();
    expect(fetchCompatibleJobSlots).not.toHaveBeenCalled();
  });

  it('dopo l’abbinamento mostra la proposta e non la ricerca', async () => {
    vi.mocked(fetchTicket).mockResolvedValue(detail({ status: 'PROPOSTA_AZIENDA', proposal: alfa }));
    renderPage();

    const proposal = await screen.findByRole('region', { name: 'Proposta all’azienda' });
    expect(within(proposal).getByText('Alfa Logistica')).toBeInTheDocument();
    expect(screen.queryByRole('heading', { name: 'Mansioni compatibili' })).not.toBeInTheDocument();
    await expectNoAxeViolations();
  });

  it('cronologia: azioni, cambi di stato, autori, dettagli e note in una lista ordinata', async () => {
    renderPage();

    const history = await screen.findByRole('region', { name: 'Cronologia' });
    const items = await within(history).findAllByRole('listitem');
    expect(items).toHaveLength(2);
    expect(items[0]).toHaveTextContent('Segnalazione inviata – stato «Nuova»');
    expect(items[0]).toHaveTextContent('Tutor Uno');
    expect(items[1]).toHaveTextContent('Presa in carico – da «Nuova» a «In lavorazione»');
    expect(items[1]).toHaveTextContent('Nota: Chiamato il tutor');
    await expectNoAxeViolations();
  });

  it('Tutor: dettaglio e cronologia in sola lettura, senza ricerca né presa in carico', async () => {
    vi.mocked(fetchCurrentUser).mockResolvedValue({
      id: 'tutor1',
      username: 'tutor1',
      firstName: 'Tutor',
      lastName: 'Uno',
      roles: ['TUTOR'],
    });
    vi.mocked(fetchTicket).mockResolvedValue(
      detail({ assignedToMe: false, assignedOperatorName: 'Operatore Call Center' }),
    );
    renderPage();

    expect(await screen.findByRole('region', { name: 'Cronologia' })).toBeInTheDocument();
    expect(screen.getByRole('region', { name: 'Dati della segnalazione' })).toBeInTheDocument();
    expect(screen.queryByRole('heading', { name: 'Mansioni compatibili' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Prendila in carico tu' })).not.toBeInTheDocument();
    expect(fetchCompatibleJobSlots).not.toHaveBeenCalled();
    expect(screen.getByRole('link', { name: 'Torna alle segnalazioni' })).toHaveAttribute('href', '/');
    // il nome del beneficiario apre la sua scheda; il nome accessibile comincia con il testo visibile
    expect(
      screen.getByRole('link', { name: /^Bianchi Anna \(\d{4}\), apri la scheda del beneficiario$/ }),
    ).toHaveAttribute('href', '/beneficiari/B1');
  });
});
