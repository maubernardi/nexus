import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AxiosError, AxiosHeaders } from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { fetchJobCategories, fetchZones } from '@/config/api/referenceApi';
import {
  acceptProposal,
  excludeCompany,
  appointmentFailed,
  appointmentSet,
  approveCategory,
  rejectCategory,
  rejectProposal,
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
  acceptProposal: vi.fn(),
  excludeCompany: vi.fn(),
  fetchExclusions: vi.fn().mockResolvedValue([]),
  appointmentFailed: vi.fn(),
  appointmentSet: vi.fn(),
  approveCategory: vi.fn(),
  rejectCategory: vi.fn(),
  rejectProposal: vi.fn(),
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
  myTicket: false,
  timerDeadline: null,
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

  it('Dettagli: esclusione dell’azienda con motivo, esito ed elenco delle escluse', async () => {
    const user = userEvent.setup();
    vi.mocked(excludeCompany).mockResolvedValue([
      {
        companyId: 'C1',
        companyName: 'Alfa Logistica',
        reason: 'Non assume d’estate',
        createdAt: '2026-10-27T10:00:00Z',
        authorName: 'Operatore Call Center',
      },
    ]);
    renderPage();

    await user.click(
      await screen.findByRole('button', { name: 'Dettagli Magazziniere presso Alfa Logistica' }),
    );
    const dialog = await screen.findByRole('dialog');
    await user.click(within(dialog).getByRole('button', { name: 'Escludi questa azienda' }));
    await user.click(within(dialog).getByRole('button', { name: 'Conferma esclusione' }));
    expect(await within(dialog).findByText('Campo obbligatorio')).toBeInTheDocument();
    expect(excludeCompany).not.toHaveBeenCalled();
    await expectNoAxeViolations();

    await user.type(within(dialog).getByLabelText(/^Motivo dell’esclusione/), 'Non assume d’estate');
    await user.click(within(dialog).getByRole('button', { name: 'Conferma esclusione' }));

    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument());
    const outcome = await screen.findByText(/Alfa Logistica esclusa per questa segnalazione/);
    await waitFor(() => expect(outcome).toHaveFocus());
    expect(excludeCompany).toHaveBeenCalledWith(
      { id: 'T9', companyId: 'C1', reason: 'Non assume d’estate' },
      expect.anything(),
    );
    expect(screen.getByRole('heading', { name: 'Aziende escluse' })).toBeInTheDocument();
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
    // come lo manda l'API: i campi vuoti sono omessi, non null
    const unassigned: Partial<TicketDetail> = detail({ assignedToMe: false });
    delete unassigned.assignedOperatorName;
    vi.mocked(fetchTicket).mockResolvedValue(unassigned as TicketDetail);
    renderPage();

    expect(
      await screen.findByText('Prendi in carico la segnalazione dalla coda per poterla abbinare.'),
    ).toBeInTheDocument();
    expect(screen.queryByRole('heading', { name: 'Mansioni compatibili' })).not.toBeInTheDocument();
    expect(fetchCompatibleJobSlots).not.toHaveBeenCalled();
    expect(screen.queryByRole('button', { name: /Prendila in carico tu/ })).not.toBeInTheDocument();
  });

  it('dopo l’abbinamento mostra la proposta e non la ricerca', async () => {
    vi.mocked(fetchTicket).mockResolvedValue(detail({ status: 'PROPOSTA_AZIENDA', proposal: alfa }));
    renderPage();

    const proposal = await screen.findByRole('region', { name: 'Proposta all’azienda' });
    expect(within(proposal).getByText('Alfa Logistica')).toBeInTheDocument();
    expect(screen.queryByRole('heading', { name: 'Mansioni compatibili' })).not.toBeInTheDocument();
    await expectNoAxeViolations();
  });

  it('Tutor della segnalazione: accetta la proposta con conferma e vede la scadenza', async () => {
    const user = userEvent.setup();
    vi.mocked(fetchCurrentUser).mockResolvedValue({
      id: 'tutor1',
      username: 'tutor1',
      firstName: 'Tutor',
      lastName: 'Uno',
      roles: ['TUTOR'],
    });
    vi.mocked(fetchTicket).mockResolvedValue(
      detail({ status: 'PROPOSTA_AZIENDA', proposal: alfa, myTicket: true, assignedToMe: false }),
    );
    vi.mocked(acceptProposal).mockResolvedValue(
      detail({
        status: 'PROPOSTA_ACCOLTA',
        proposal: alfa,
        myTicket: true,
        version: 3,
        timerDeadline: '2026-11-03',
      }),
    );
    renderPage();

    await user.click(await screen.findByRole('button', { name: 'Accetta la proposta' }));
    expect(screen.getByRole('heading', { name: 'Conferma l’accettazione' })).toHaveFocus();
    expect(screen.getByText(/parte il timer di 7 giorni lavorativi/)).toBeInTheDocument();
    await expectNoAxeViolations();
    await user.click(screen.getByRole('button', { name: 'Conferma accettazione' }));

    const outcome = await screen.findByText('Proposta accettata. Scadenza del timer: martedì 03/11/2026.');
    await waitFor(() => expect(outcome).toHaveFocus());
    expect(acceptProposal).toHaveBeenCalledWith({ id: 'T9', version: 2 }, expect.anything());
  });

  it('Tutor della segnalazione: appuntamento fissato con nota, il timer si ferma', async () => {
    const user = userEvent.setup();
    vi.mocked(fetchCurrentUser).mockResolvedValue({
      id: 'tutor1',
      username: 'tutor1',
      firstName: 'Tutor',
      lastName: 'Uno',
      roles: ['TUTOR'],
    });
    vi.mocked(fetchTicket).mockResolvedValue(
      detail({ status: 'PROPOSTA_ACCOLTA', proposal: alfa, myTicket: true, timerDeadline: '2026-11-03' }),
    );
    vi.mocked(appointmentSet).mockResolvedValue(
      detail({ status: 'APPUNTAMENTO', proposal: alfa, myTicket: true, version: 4 }),
    );
    renderPage();

    expect(screen.queryByRole('button', { name: 'Accetta la proposta' })).not.toBeInTheDocument();
    await user.click(await screen.findByRole('button', { name: 'Appuntamento fissato' }));
    expect(screen.getByRole('heading', { name: 'Conferma l’appuntamento' })).toHaveFocus();
    expect(screen.getByText(/con Alfa Logistica è fissato\? Il timer si ferma/)).toBeInTheDocument();
    await expectNoAxeViolations();
    await user.type(screen.getByLabelText(/^Note \(facoltative\)/), 'Giovedì alle 10');
    // come nella realtà: ricaricata, la segnalazione è già in "Appuntamento" e il pulsante sparisce
    vi.mocked(fetchTicket).mockResolvedValue(
      detail({ status: 'APPUNTAMENTO', proposal: alfa, myTicket: true, version: 4 }),
    );
    await user.click(screen.getByRole('button', { name: 'Conferma appuntamento' }));

    const outcome = await screen.findByText('Appuntamento registrato: il timer è fermo.');
    await waitFor(() => expect(outcome).toHaveFocus());
    expect(appointmentSet).toHaveBeenCalledWith(
      { id: 'T9', version: 2, note: 'Giovedì alle 10' },
      expect.anything(),
    );
  });

  it('Tutor della segnalazione: appuntamento fallito con causa; con "Altro" la nota è obbligatoria', async () => {
    const user = userEvent.setup();
    vi.mocked(fetchCurrentUser).mockResolvedValue({
      id: 'tutor1',
      username: 'tutor1',
      firstName: 'Tutor',
      lastName: 'Uno',
      roles: ['TUTOR'],
    });
    vi.mocked(fetchTicket).mockResolvedValue(
      detail({ status: 'APPUNTAMENTO', proposal: alfa, myTicket: true }),
    );
    vi.mocked(appointmentFailed).mockResolvedValue(
      detail({ status: 'RIAPERTO', myTicket: true, version: 5 }),
    );
    renderPage();

    await user.click(await screen.findByRole('button', { name: 'L’appuntamento non è andato a buon fine' }));
    expect(screen.getByRole('heading', { name: 'Registra l’esito dell’appuntamento' })).toHaveFocus();
    await expectNoAxeViolations();
    await user.selectOptions(screen.getByLabelText(/^Causa/), 'ALTRO');
    await user.click(screen.getByRole('button', { name: 'Conferma' }));
    expect(await screen.findByText('Campo obbligatorio')).toBeInTheDocument();
    expect(appointmentFailed).not.toHaveBeenCalled();

    await user.selectOptions(screen.getByLabelText(/^Causa/), 'BENEFICIARIO_RINUNCIA');
    // come nella realtà: ricaricata, la segnalazione è già riaperta e la sezione della proposta sparisce
    vi.mocked(fetchTicket).mockResolvedValue(detail({ status: 'RIAPERTO', myTicket: true, version: 5 }));
    await user.click(screen.getByRole('button', { name: 'Conferma' }));

    const outcome = await screen.findByText(/Esito registrato: la segnalazione è tornata all’operatore/);
    await waitFor(() => expect(outcome).toHaveFocus());
    expect(appointmentFailed).toHaveBeenCalledWith(
      { id: 'T9', version: 2, cause: 'BENEFICIARIO_RINUNCIA', note: null, relaunch: false },
      expect.anything(),
    );
  });

  it('Tutor della segnalazione: appuntamento fallito con il rilancio in bacheca', async () => {
    const user = userEvent.setup();
    vi.mocked(fetchCurrentUser).mockResolvedValue({
      id: 'tutor1',
      username: 'tutor1',
      firstName: 'Tutor',
      lastName: 'Uno',
      roles: ['TUTOR'],
    });
    vi.mocked(fetchTicket).mockResolvedValue(
      detail({ status: 'APPUNTAMENTO', proposal: alfa, myTicket: true }),
    );
    const reopened = detail({
      status: 'RIAPERTO',
      myTicket: true,
      version: 5,
      relaunchPostId: 'P7',
      relaunchPostNumber: 7,
      relaunchPostStatus: 'DRAFT',
    });
    vi.mocked(appointmentFailed).mockResolvedValue(reopened);
    renderPage();

    await user.click(await screen.findByRole('button', { name: 'L’appuntamento non è andato a buon fine' }));
    const relaunch = screen.getByRole('checkbox', { name: 'Chiedi il rilancio della mansione in bacheca' });
    expect(relaunch).toHaveAccessibleDescription(/riservata al progetto GOL/);
    await user.selectOptions(screen.getByLabelText(/^Causa/), 'AZIENDA_NON_CONFERMA');
    await user.click(relaunch);
    vi.mocked(fetchTicket).mockResolvedValue(reopened);
    await user.click(screen.getByRole('button', { name: 'Conferma' }));

    const outcome = await screen.findByText(/il rilancio in bacheca è stato chiesto/);
    await waitFor(() => expect(outcome).toHaveFocus());
    expect(vi.mocked(appointmentFailed).mock.calls[0][0]).toMatchObject({ relaunch: true });
    // il Tutor vede numero e stato dell'annuncio, senza link alla gestione della bacheca
    expect(
      await screen.findByText('Hai chiesto il rilancio della mansione in bacheca: annuncio #7 (bozza).'),
    ).toBeInTheDocument();
    expect(screen.queryByRole('link', { name: 'Annuncio #7' })).not.toBeInTheDocument();
  });

  it('Call Center: link alla bozza del rilancio', async () => {
    const reopened: Partial<TicketDetail> = detail({
      status: 'RIAPERTO',
      relaunchPostId: 'P7',
      relaunchPostNumber: 7,
      relaunchPostStatus: 'DRAFT',
    });
    vi.mocked(fetchTicket).mockResolvedValue(reopened as TicketDetail);
    renderPage();

    expect(await screen.findByRole('link', { name: 'Annuncio #7' })).toHaveAttribute('href', '/bacheca/P7');
    expect(screen.getByText(/Stato: Bozza/)).toBeInTheDocument();
    await expectNoAxeViolations();
  });

  it('segnalazione speciale: annuncio della candidatura, link solo per il Call Center', async () => {
    vi.mocked(fetchTicket).mockResolvedValue(
      detail({ type: 'SPECIAL', fastTrack: true, boardPostId: 'P4', boardPostNumber: 4 }),
    );
    const { unmount } = renderPage();

    expect(await screen.findByRole('link', { name: 'Annuncio #4' })).toHaveAttribute('href', '/bacheca/P4');
    expect(screen.getByText('Candidatura dalla bacheca')).toBeInTheDocument();
    unmount();

    vi.mocked(fetchCurrentUser).mockResolvedValue({
      id: 'tutor1',
      username: 'tutor1',
      firstName: 'Tutor',
      lastName: 'Uno',
      roles: ['TUTOR'],
    });
    renderPage();
    expect(await screen.findByText('Annuncio #4')).toBeInTheDocument();
    expect(screen.queryByRole('link', { name: 'Annuncio #4' })).not.toBeInTheDocument();
  });

  it('Call Center: una segnalazione riaperta si abbina di nuovo', async () => {
    vi.mocked(fetchTicket).mockResolvedValue(detail({ status: 'RIAPERTO' }));
    renderPage();

    expect(await screen.findByRole('heading', { name: 'Mansioni compatibili' })).toBeInTheDocument();
    expect(
      await screen.findByRole('button', { name: 'Proponi Magazziniere presso Alfa Logistica' }),
    ).toBeInTheDocument();
  });

  it('Tutor della segnalazione: rifiuta la proposta con motivo obbligatorio', async () => {
    const user = userEvent.setup();
    vi.mocked(fetchCurrentUser).mockResolvedValue({
      id: 'tutor1',
      username: 'tutor1',
      firstName: 'Tutor',
      lastName: 'Uno',
      roles: ['TUTOR'],
    });
    vi.mocked(fetchTicket).mockResolvedValue(
      detail({ status: 'PROPOSTA_AZIENDA', proposal: alfa, myTicket: true, assignedToMe: false }),
    );
    vi.mocked(rejectProposal).mockResolvedValue(detail({ status: 'RIAPERTO', myTicket: true, version: 3 }));
    renderPage();

    await user.click(await screen.findByRole('button', { name: 'Rifiuta la proposta' }));
    expect(screen.getByRole('heading', { name: 'Rifiuta la proposta' })).toHaveFocus();
    expect(
      screen.getByText(/Alfa Logistica non sarà più proposta per questa segnalazione/),
    ).toBeInTheDocument();
    await expectNoAxeViolations();
    await user.click(screen.getByRole('button', { name: 'Conferma rifiuto' }));
    expect(await screen.findByText('Campo obbligatorio')).toBeInTheDocument();
    expect(rejectProposal).not.toHaveBeenCalled();

    await user.type(screen.getByLabelText(/^Motivo/), 'Orari incompatibili');
    vi.mocked(fetchTicket).mockResolvedValue(detail({ status: 'RIAPERTO', myTicket: true, version: 3 }));
    await user.click(screen.getByRole('button', { name: 'Conferma rifiuto' }));

    const outcome = await screen.findByText(/Proposta rifiutata: la segnalazione è tornata all’operatore/);
    await waitFor(() => expect(outcome).toHaveFocus());
    expect(rejectProposal).toHaveBeenCalledWith(
      { id: 'T9', version: 2, reason: 'Orari incompatibili' },
      expect.anything(),
    );
  });

  it('collega o Call Center: nessuna accettazione; scadenza visibile dopo l’accettazione', async () => {
    vi.mocked(fetchTicket).mockResolvedValue(
      detail({ status: 'PROPOSTA_ACCOLTA', proposal: alfa, timerDeadline: '2026-11-03' }),
    );
    renderPage();

    const proposal = await screen.findByRole('region', { name: 'Proposta all’azienda' });
    expect(within(proposal).getByText('martedì 03/11/2026 (7 giorni lavorativi)')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Accetta la proposta' })).not.toBeInTheDocument();
  });

  it('Call Center: approva la nuova tipologia con il nome corretto', async () => {
    const user = userEvent.setup();
    vi.mocked(fetchTicket).mockResolvedValue(
      detail({
        status: 'IN_ATTESA_APPROVAZIONE',
        assignedOperatorName: null,
        assignedToMe: false,
        requestedJobCategory: null,
        requestedJobFreeText: 'Tecnico del suono',
      }),
    );
    vi.mocked(approveCategory).mockResolvedValue(
      detail({
        status: 'NUOVA',
        requestedJobCategory: { id: 'J9', code: 'TECNICO_AUDIO', name: 'Tecnico audio' },
      }),
    );
    renderPage();

    const section = await screen.findByRole('region', { name: 'Nuova tipologia da approvare' });
    expect(within(section).getByText(/proposto la tipologia «Tecnico del suono»/)).toBeInTheDocument();
    await expectNoAxeViolations();
    const name = within(section).getByLabelText(/^Nome della tipologia/);
    expect(name).toHaveValue('Tecnico del suono');
    await user.clear(name);
    await user.type(name, 'Tecnico audio');
    await user.click(within(section).getByRole('button', { name: 'Approva e aggiungi al catalogo' }));

    const outcome = await screen.findByText(/Tipologia «Tecnico audio» approvata/);
    await waitFor(() => expect(outcome).toHaveFocus());
    expect(approveCategory).toHaveBeenCalledWith(
      { id: 'T9', name: 'Tecnico audio', version: 2 },
      expect.anything(),
    );
  });

  it('Call Center: usa una tipologia del catalogo con il motivo (obbligatorio)', async () => {
    const user = userEvent.setup();
    vi.mocked(fetchTicket).mockResolvedValue(
      detail({
        status: 'IN_ATTESA_APPROVAZIONE',
        assignedOperatorName: null,
        assignedToMe: false,
        requestedJobCategory: null,
        requestedJobFreeText: 'Magazzino',
      }),
    );
    vi.mocked(rejectCategory).mockResolvedValue(detail({ status: 'NUOVA', requestedJobCategory: MAG }));
    renderPage();

    const section = await screen.findByRole('region', { name: 'Nuova tipologia da approvare' });
    await within(section).findByRole('option', { name: 'Magazziniere' });
    await user.selectOptions(within(section).getByLabelText(/^Tipologia del catalogo/), 'J1');
    await user.click(within(section).getByRole('button', { name: 'Usa questa tipologia' }));
    expect(await within(section).findByText('Campo obbligatorio')).toBeInTheDocument();
    expect(rejectCategory).not.toHaveBeenCalled();

    await user.type(within(section).getByLabelText(/^Motivo per il Tutor/), 'È già nel catalogo');
    await user.click(within(section).getByRole('button', { name: 'Usa questa tipologia' }));
    expect(await screen.findByText(/Tipologia cambiata in «Magazziniere»/)).toBeInTheDocument();
    expect(rejectCategory).toHaveBeenCalledWith(
      { id: 'T9', jobCategoryId: 'J1', reason: 'È già nel catalogo', version: 2 },
      expect.anything(),
    );
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
