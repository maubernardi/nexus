import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AxiosError, AxiosHeaders } from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { fetchInternship, type Internship } from '@/config/api/internshipApi';
import { fetchHiringFormulas, fetchInterruptionReasons, fetchNoHireReasons } from '@/config/api/referenceApi';
import { fetchTicket, submitClosureReport, type TicketDetail } from '@/config/api/ticketApi';
import { ClosureReportPage } from '@/pages/ClosureReportPage/ClosureReportPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderRoutes } from '@/tests/renderWithProviders';

vi.mock('@/config/api/internshipApi', () => ({ fetchInternship: vi.fn() }));
vi.mock('@/config/api/referenceApi', () => ({
  fetchHiringFormulas: vi.fn(),
  fetchNoHireReasons: vi.fn(),
  fetchInterruptionReasons: vi.fn(),
}));
vi.mock('@/config/api/ticketApi', () => ({ fetchTicket: vi.fn(), submitClosureReport: vi.fn() }));

const error = (status: number, body: Record<string, unknown>) =>
  new AxiosError(String(status), String(status), undefined, undefined, {
    status,
    statusText: String(status),
    headers: {},
    config: { headers: new AxiosHeaders() },
    data: { status, ...body },
  });

const ticket = {
  id: 'T9',
  number: 9,
  status: 'IN_TIROCINIO',
  version: 7,
  beneficiary: {
    id: 'B1',
    firstName: 'Anna',
    lastName: 'Bianchi',
    birthYear: 1999,
    residenceZoneName: 'Novoli',
  },
  proposal: { companyName: 'Demo Ristorazione' },
  myTicket: true,
} as TicketDetail;

/** Scheda del tirocinio: servono solo le date (US-1009). */
const internship = { startDate: '2026-12-01', expectedEndDate: '2027-05-31' } as Internship;

const closed = {
  ...ticket,
  status: 'FORM_RESTITUZIONE',
  version: 8,
  closureReport: {
    companySatisfaction: 4,
    beneficiarySatisfaction: 5,
    hired: true,
    hiringFormula: { id: 'F2', code: 'TEMPO_INDETERMINATO', name: 'Tempo indeterminato' },
    jobSlotStillAvailable: false,
    submittedAt: '2026-10-30T09:30:00Z',
    submittedBy: 'Mario Rossi',
  },
} as TicketDetail;

const renderPage = () =>
  renderRoutes({
    routes: [
      {
        path: '/segnalazioni/:id/restituzione',
        element: (
          <main>
            <ClosureReportPage />
          </main>
        ),
      },
    ],
    initialPath: '/segnalazioni/T9/restituzione',
  });

/** Compila le scelte obbligatorie: assunto a tempo indeterminato, mansione non più disponibile. */
const fillRequired = async (user: ReturnType<typeof userEvent.setup>): Promise<void> => {
  // il modulo compare dopo la lettura della scheda (US-1009)
  const [companyFour] = await screen.findAllByRole('radio', { name: '4' });
  await user.click(companyFour);
  await user.click(screen.getAllByRole('radio', { name: '5' })[1]);
  await user.click(screen.getAllByRole('radio', { name: 'Sì' })[0]);
  await user.selectOptions(
    await screen.findByRole('combobox', { name: /Formula di assunzione/ }),
    'Tempo indeterminato',
  );
  await user.click(screen.getAllByRole('radio', { name: 'No' })[1]);
  await chooseReview(user, ['Buona', 'Ottima', 'Buona']);
};

/** Valutazione dell'azienda (US-1104): una faccina per ognuna delle tre domande, nell'ordine del modulo. */
const chooseReview = async (
  user: ReturnType<typeof userEvent.setup>,
  words: [string, string, string],
): Promise<void> => {
  const groups = [/interazione con l’azienda/, /accogliere il beneficiario/, /Capacità formativa/];
  for (const [i, name] of groups.entries()) {
    await user.click(within(screen.getByRole('group', { name })).getByRole('radio', { name: words[i] }));
  }
};

describe('<ClosureReportPage>', () => {
  beforeEach(() => {
    vi.mocked(fetchTicket).mockResolvedValue(ticket);
    vi.mocked(fetchInternship).mockReset();
    vi.mocked(fetchInternship).mockResolvedValue(internship);
    vi.mocked(fetchHiringFormulas).mockResolvedValue([
      { id: 'F1', code: 'TEMPO_DETERMINATO_3_MESI', name: 'Tempo determinato 3 mesi' },
      { id: 'F2', code: 'TEMPO_INDETERMINATO', name: 'Tempo indeterminato' },
    ]);
    vi.mocked(fetchNoHireReasons).mockResolvedValue([
      { id: 'R1', code: 'AZIENDA_SENZA_RISORSE', name: 'L’azienda non ha risorse' },
    ]);
    vi.mocked(fetchInterruptionReasons).mockResolvedValue([
      { id: 'C2', code: 'INTERRUZIONE_AZIENDA', name: 'Interruzione anticipata per volontà dell’azienda' },
      {
        id: 'C3',
        code: 'INTERRUZIONE_DESTINATARIO',
        name: 'Interruzione anticipata per volontà del destinatario',
      },
    ]);
    vi.mocked(submitClosureReport).mockReset();
  });

  it('modulo con le scelte obbligatorie e i testi facoltativi; senza scelte, il riepilogo degli errori', async () => {
    const user = userEvent.setup();
    renderPage();

    expect(
      await screen.findByRole('heading', {
        level: 1,
        name: 'Report di restituzione della segnalazione n. 9',
      }),
    ).toBeInTheDocument();
    expect(screen.getByText('Tirocinio di Anna Bianchi presso Demo Ristorazione.')).toBeInTheDocument();
    expect(
      await screen.findByRole('group', { name: /Soddisfazione dell’azienda/ }),
    ).toHaveAccessibleDescription('Da 1 (per niente soddisfatto) a 5 (molto soddisfatto).');
    // formula e motivo compaiono solo con la risposta sull'assunzione
    expect(screen.queryByRole('combobox')).not.toBeInTheDocument();
    await expectNoAxeViolations();

    await user.click(screen.getByRole('button', { name: 'Invia il report' }));

    const summary = await screen.findByRole('alert');
    expect(summary).toHaveTextContent('Soddisfazione dell’azienda: Campo obbligatorio');
    expect(summary).toHaveTextContent('La mansione è ancora disponibile?: Campo obbligatorio');
    await waitFor(() => expect(summary).toHaveFocus());
    expect(submitClosureReport).not.toHaveBeenCalled();
    await expectNoAxeViolations();
  });

  it('invio dopo la conferma: il report inviato si legge in sola lettura', async () => {
    const user = userEvent.setup();
    vi.mocked(submitClosureReport).mockResolvedValue(closed);
    renderPage();
    await screen.findByRole('heading', { level: 1 });

    await fillRequired(user);
    await user.type(screen.getByRole('textbox', { name: /Obiettivi raggiunti/ }), '  Autonomia in cucina ');
    await user.click(screen.getByRole('button', { name: 'Invia il report' }));

    // prima la conferma, con i campi bloccati
    const confirm = await screen.findByRole('heading', { name: 'Confermi l’invio del report?' });
    await waitFor(() => expect(confirm).toHaveFocus());
    expect(screen.getByRole('textbox', { name: /Obiettivi raggiunti/ })).toBeDisabled();
    expect(submitClosureReport).not.toHaveBeenCalled();
    await expectNoAxeViolations();

    await user.click(screen.getByRole('button', { name: 'Sì, invia il report' }));

    expect(vi.mocked(submitClosureReport).mock.calls[0][0]).toEqual({
      id: 'T9',
      version: 7,
      report: {
        actualEndDate: '2027-05-31',
        closureReasonId: null,
        interruptionDetails: null,
        companySatisfaction: 4,
        beneficiarySatisfaction: 5,
        objectives: 'Autonomia in cucina',
        difficulties: null,
        hired: true,
        hiringFormulaId: 'F2',
        noHireReasonId: null,
        jobSlotStillAvailable: false,
        companyInteraction: 4,
        companyWelcome: 5,
        companyTraining: 4,
        companyReviewNote: null,
      },
    });
    const sent = await screen.findByRole('status');
    expect(sent).toHaveTextContent('Report inviato: il percorso della segnalazione n. 9 è concluso.');
    await waitFor(() => expect(sent).toHaveFocus());
    expect(screen.getByRole('region', { name: 'Report di restituzione' })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Invia il report' })).not.toBeInTheDocument();
    await expectNoAxeViolations();
  });

  it('valutazione dell’azienda: media come faccina, nota sempre presente e facoltativa (US-1104)', async () => {
    const user = userEvent.setup();
    vi.mocked(submitClosureReport).mockResolvedValue(closed);
    renderPage();
    await fillRequired(user);
    // 4, 5, 4: media 4,3, «Buona», con la nota
    expect(screen.getByText('Media:').parentElement).toHaveTextContent('Buona · 4,3');
    const note = screen.getByRole('textbox', { name: /Nota sulla valutazione/ });
    expect(note).toHaveAccessibleDescription(/Niente dati personali del beneficiario/);
    await expectNoAxeViolations();

    // 5, 5, 4: media 4,7, «Ottima»; la nota resta (PO, 26/11, dal collaudo)
    await chooseReview(user, ['Ottima', 'Ottima', 'Buona']);
    expect(screen.getByText('Media:').parentElement).toHaveTextContent('Ottima · 4,7');
    await user.type(screen.getByRole('textbox', { name: /Nota sulla valutazione/ }), ' Poca formazione ');
    await user.click(screen.getByRole('button', { name: 'Invia il report' }));
    await user.click(await screen.findByRole('button', { name: 'Sì, invia il report' }));
    expect(vi.mocked(submitClosureReport).mock.calls[0][0].report).toEqual(
      expect.objectContaining({
        companyInteraction: 5,
        companyWelcome: 5,
        companyTraining: 4,
        companyReviewNote: 'Poca formazione',
      }),
    );
  });

  it('valutazione mancante: il riepilogo indica le domande senza faccina (US-1104)', async () => {
    const user = userEvent.setup();
    renderPage();
    const [companyFour] = await screen.findAllByRole('radio', { name: '4' });
    await user.click(companyFour);
    await user.click(screen.getByRole('button', { name: 'Invia il report' }));
    const summary = await screen.findByRole('alert');
    expect(summary).toHaveTextContent('Capacità formativa dell’azienda e del tutor aziendale');
    expect(submitClosureReport).not.toHaveBeenCalled();
  });

  it('fine effettiva: precompilata con la fine prevista, obbligatoria e non prima dell’inizio', async () => {
    const user = userEvent.setup();
    vi.mocked(submitClosureReport).mockResolvedValue(closed);
    renderPage();

    const date = await screen.findByLabelText(/Fine effettiva del tirocinio/, { selector: 'input' });
    expect(date).toHaveValue('2027-05-31');
    expect(date).toHaveAccessibleDescription(/^Precompilata con la fine prevista nella scheda/);
    await fillRequired(user);

    await user.clear(date);
    await user.click(screen.getByRole('button', { name: 'Invia il report' }));
    const summary = await screen.findByRole('alert');
    expect(summary).toHaveTextContent('Fine effettiva del tirocinio: Campo obbligatorio');

    await user.type(date, '2026-11-30');
    await user.click(screen.getByRole('button', { name: 'Invia il report' }));
    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Fine effettiva del tirocinio: La fine effettiva non può precedere l’inizio del tirocinio (01/12/2026)',
    );
    expect(date).toHaveAttribute('aria-invalid', 'true');
    await expectNoAxeViolations();

    // fine anticipata, lo stesso giorno dell'inizio: serve il motivo dell'interruzione (US-1103)
    await user.clear(date);
    await user.type(date, '2026-12-01');
    await user.selectOptions(
      screen.getByRole('combobox', { name: /Motivo dell’interruzione anticipata/ }),
      'Interruzione anticipata per volontà del destinatario',
    );
    // con il motivo serve la descrizione (PO, 16/11)
    await user.type(
      screen.getByRole('textbox', { name: /Descrizione dell’interruzione/ }),
      '  Ha trovato un altro lavoro ',
    );
    await user.click(screen.getByRole('button', { name: 'Invia il report' }));
    await user.click(await screen.findByRole('button', { name: 'Sì, invia il report' }));
    expect(vi.mocked(submitClosureReport).mock.calls[0][0].report).toMatchObject({
      actualEndDate: '2026-12-01',
      closureReasonId: 'C3',
      interruptionDetails: 'Ha trovato un altro lavoro',
    });
  });

  it('motivo dell’interruzione: compare con la fine prima di quella prevista ed è obbligatorio (US-1103)', async () => {
    const user = userEvent.setup();
    renderPage();
    const date = await screen.findByLabelText(/Fine effettiva del tirocinio/, { selector: 'input' });
    await fillRequired(user);
    // fine come prevista: nessun motivo da scegliere
    expect(screen.queryByRole('combobox', { name: /Motivo dell’interruzione/ })).not.toBeInTheDocument();

    await user.clear(date);
    await user.type(date, '2027-02-15');
    const reason = screen.getByRole('combobox', { name: /Motivo dell’interruzione anticipata/ });
    expect(reason).toHaveAccessibleDescription(
      'Il tirocinio finisce prima della fine prevista (31/05/2027): indica perché. Se finisce alla data prevista il motivo è «Chiusura regolare».',
    );
    await expectNoAxeViolations();
    await user.click(screen.getByRole('button', { name: 'Invia il report' }));
    const summary = await screen.findByRole('alert');
    expect(summary).toHaveTextContent('Motivo dell’interruzione anticipata: Campo obbligatorio');
    expect(summary).toHaveTextContent('Descrizione dell’interruzione: Campo obbligatorio');
    expect(
      screen.getByRole('textbox', { name: /Descrizione dell’interruzione/ }),
    ).toHaveAccessibleDescription(/^Che cosa è successo e quando: serve alla relazione finale\./);
    expect(submitClosureReport).not.toHaveBeenCalled();

    // tornando alla fine prevista il campo sparisce
    await user.clear(date);
    await user.type(date, '2027-05-31');
    expect(screen.queryByRole('combobox', { name: /Motivo dell’interruzione/ })).not.toBeInTheDocument();
    expect(screen.queryByRole('textbox', { name: /Descrizione dell’interruzione/ })).not.toBeInTheDocument();
  });

  it('con le proroghe la fine prevista è quella dell’ultima: la fine della convenzione iniziale è anticipata (US-1012)', async () => {
    const user = userEvent.setup();
    vi.mocked(fetchInternship).mockResolvedValue({ ...internship, currentEndDate: '2027-09-30' });
    renderPage();

    const date = await screen.findByLabelText(/Fine effettiva del tirocinio/, { selector: 'input' });
    expect(date).toHaveValue('2027-09-30');
    expect(screen.queryByRole('combobox', { name: /Motivo dell’interruzione/ })).not.toBeInTheDocument();
    await user.clear(date);
    await user.type(date, '2027-05-31');
    expect(
      screen.getByRole('combobox', { name: /Motivo dell’interruzione anticipata/ }),
    ).toHaveAccessibleDescription(/^Il tirocinio finisce prima della fine prevista \(30\/09\/2027\)/);
  });

  it('senza la scheda la fine effettiva parte vuota; l’errore del server va sul campo', async () => {
    const user = userEvent.setup();
    vi.mocked(fetchInternship).mockRejectedValue(error(403, {}));
    vi.mocked(submitClosureReport).mockRejectedValue(
      error(400, {
        fieldErrors: [
          {
            field: 'actualEndDate',
            message: 'La fine effettiva non può precedere l’inizio del tirocinio (01/12/2026)',
          },
        ],
      }),
    );
    renderPage();

    const date = await screen.findByLabelText(/Fine effettiva del tirocinio/, { selector: 'input' });
    expect(date).toHaveValue('');
    expect(date).toHaveAccessibleDescription(/^L’ultimo giorno di tirocinio/);
    await fillRequired(user);
    await user.type(date, '2026-11-30');
    await user.click(screen.getByRole('button', { name: 'Invia il report' }));
    await user.click(await screen.findByRole('button', { name: 'Sì, invia il report' }));

    const summary = await screen.findByRole('alert');
    expect(summary).toHaveTextContent(
      'Fine effettiva del tirocinio: La fine effettiva non può precedere l’inizio del tirocinio (01/12/2026)',
    );
    expect(date).toHaveAttribute('aria-invalid', 'true');
  });

  it('torna al modulo dalla conferma; l’errore di un campo dal server riporta al riepilogo', async () => {
    const user = userEvent.setup();
    vi.mocked(submitClosureReport).mockRejectedValue(
      error(400, {
        fieldErrors: [{ field: 'hiringFormulaId', message: 'Formula di assunzione non valida' }],
      }),
    );
    renderPage();
    await screen.findByRole('heading', { level: 1 });
    await fillRequired(user);

    await user.click(screen.getByRole('button', { name: 'Invia il report' }));
    await user.click(await screen.findByRole('button', { name: 'Torna al modulo' }));
    expect(screen.getByRole('textbox', { name: /Obiettivi raggiunti/ })).toBeEnabled();

    await user.click(screen.getByRole('button', { name: 'Invia il report' }));
    await user.click(await screen.findByRole('button', { name: 'Sì, invia il report' }));

    const summary = await screen.findByRole('alert');
    expect(summary).toHaveTextContent('Formula di assunzione: Formula di assunzione non valida');
    await waitFor(() => expect(summary).toHaveFocus());
    expect(screen.getByRole('combobox', { name: /Formula di assunzione/ })).toHaveAttribute(
      'aria-invalid',
      'true',
    );
  });

  it('conflitto: il motivo, e il modulo resta', async () => {
    const user = userEvent.setup();
    vi.mocked(submitClosureReport).mockRejectedValue(
      error(409, { message: 'Operazione non ammessa per un ticket in stato FORM_RESTITUZIONE' }),
    );
    renderPage();
    await screen.findByRole('heading', { level: 1 });
    await fillRequired(user);
    await user.click(screen.getByRole('button', { name: 'Invia il report' }));
    await user.click(await screen.findByRole('button', { name: 'Sì, invia il report' }));

    const alert = await screen.findByRole('alert');
    expect(alert).toHaveTextContent(
      'Il report non è stato inviato: Operazione non ammessa per un ticket in stato FORM_RESTITUZIONE',
    );
    await waitFor(() => expect(alert).toHaveFocus());
    expect(screen.getByRole('button', { name: 'Invia il report' })).toBeInTheDocument();
  });

  it('non assunto: il motivo al posto della formula', async () => {
    const user = userEvent.setup();
    vi.mocked(submitClosureReport).mockResolvedValue(closed);
    renderPage();
    await screen.findByRole('heading', { level: 1 });
    await fillRequired(user);
    // ripensamento: la formula scelta prima non conta più
    await user.click(screen.getAllByRole('radio', { name: 'No' })[0]);
    expect(screen.queryByRole('combobox', { name: /Formula di assunzione/ })).not.toBeInTheDocument();
    await user.selectOptions(
      screen.getByRole('combobox', { name: /Motivo della mancata assunzione/ }),
      'L’azienda non ha risorse',
    );
    await user.click(screen.getByRole('button', { name: 'Invia il report' }));
    await user.click(await screen.findByRole('button', { name: 'Sì, invia il report' }));

    expect(vi.mocked(submitClosureReport).mock.calls[0][0].report).toMatchObject({
      hired: false,
      hiringFormulaId: null,
      noHireReasonId: 'R1',
    });
  });

  it('report già inviato: si legge e non si compila; fuori dal tirocinio la pagina lo dice', async () => {
    vi.mocked(fetchTicket).mockResolvedValue(closed);
    const { unmount } = renderPage();
    expect(await screen.findByRole('region', { name: 'Report di restituzione' })).toBeInTheDocument();
    expect(screen.queryByRole('status')).not.toBeInTheDocument();
    expect(screen.queryByRole('button')).not.toBeInTheDocument();
    unmount();

    vi.mocked(fetchTicket).mockResolvedValue({ ...ticket, status: 'APPUNTAMENTO' });
    renderPage();
    expect(
      await screen.findByRole('heading', { level: 1, name: 'Il report si compila durante il tirocinio' }),
    ).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Torna alla segnalazione' })).toHaveAttribute(
      'href',
      '/segnalazioni/T9',
    );
  });
});
