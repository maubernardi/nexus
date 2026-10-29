import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AxiosError, AxiosHeaders } from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { fetchHiringFormulas, fetchNoHireReasons } from '@/config/api/referenceApi';
import { fetchTicket, submitClosureReport, type TicketDetail } from '@/config/api/ticketApi';
import { ClosureReportPage } from '@/pages/ClosureReportPage/ClosureReportPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderRoutes } from '@/tests/renderWithProviders';

vi.mock('@/config/api/referenceApi', () => ({ fetchHiringFormulas: vi.fn(), fetchNoHireReasons: vi.fn() }));
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
  const [companyFour] = screen.getAllByRole('radio', { name: '4' });
  await user.click(companyFour);
  await user.click(screen.getAllByRole('radio', { name: '5' })[1]);
  await user.click(screen.getAllByRole('radio', { name: 'Sì' })[0]);
  await user.selectOptions(
    await screen.findByRole('combobox', { name: /Formula di assunzione/ }),
    'Tempo indeterminato',
  );
  await user.click(screen.getAllByRole('radio', { name: 'No' })[1]);
};

describe('<ClosureReportPage>', () => {
  beforeEach(() => {
    vi.mocked(fetchTicket).mockResolvedValue(ticket);
    vi.mocked(fetchHiringFormulas).mockResolvedValue([
      { id: 'F1', code: 'TEMPO_DETERMINATO_3_MESI', name: 'Tempo determinato 3 mesi' },
      { id: 'F2', code: 'TEMPO_INDETERMINATO', name: 'Tempo indeterminato' },
    ]);
    vi.mocked(fetchNoHireReasons).mockResolvedValue([
      { id: 'R1', code: 'AZIENDA_SENZA_RISORSE', name: 'L’azienda non ha risorse' },
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
    expect(screen.getByRole('group', { name: /Soddisfazione dell’azienda/ })).toHaveAccessibleDescription(
      'Da 1 (per niente soddisfatto) a 5 (molto soddisfatto).',
    );
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
        companySatisfaction: 4,
        beneficiarySatisfaction: 5,
        objectives: 'Autonomia in cucina',
        difficulties: null,
        hired: true,
        hiringFormulaId: 'F2',
        noHireReasonId: null,
        jobSlotStillAvailable: false,
      },
    });
    const sent = await screen.findByRole('status');
    expect(sent).toHaveTextContent('Report inviato: il percorso della segnalazione n. 9 è concluso.');
    await waitFor(() => expect(sent).toHaveFocus());
    expect(screen.getByRole('region', { name: 'Report di restituzione' })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Invia il report' })).not.toBeInTheDocument();
    await expectNoAxeViolations();
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
