import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AxiosError, AxiosHeaders } from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import {
  addInternshipAtecoCode,
  addInternshipLegalRepresentative,
  fetchInternship,
  saveInternship,
  type Internship,
} from '@/config/api/internshipApi';
import { fetchInternshipTypes } from '@/config/api/referenceApi';
import { fetchTicket, type TicketDetail } from '@/config/api/ticketApi';
import { InternshipPage } from '@/pages/InternshipPage/InternshipPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderRoutes } from '@/tests/renderWithProviders';

vi.mock('@/config/api/internshipApi', () => ({
  fetchInternship: vi.fn(),
  saveInternship: vi.fn(),
  addInternshipLegalRepresentative: vi.fn(),
  addInternshipAtecoCode: vi.fn(),
}));
vi.mock('@/config/api/referenceApi', () => ({ fetchInternshipTypes: vi.fn() }));
vi.mock('@/config/api/ticketApi', () => ({ fetchTicket: vi.fn() }));

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
  status: 'APPUNTAMENTO',
  version: 5,
  beneficiary: {
    id: 'B1',
    firstName: 'Anna',
    lastName: 'Bianchi',
    birthYear: 1999,
    residenceZoneName: 'Novoli',
  },
  myTicket: true,
} as TicketDetail;

// come lo manda l'API: i campi vuoti sono omessi
const internship = (overrides: Partial<Internship> = {}): Internship => ({
  companyName: 'Demo Ristorazione',
  atecoCodes: [],
  companyTutors: [],
  suspensions: [],
  firstSlotStart: '09:00:00',
  firstSlotEnd: '13:00:00',
  secondSlotStart: '14:00:00',
  secondSlotEnd: '18:00:00',
  excludeHolidays: true,
  schedule: [],
  ...overrides,
});

const renderPage = () =>
  renderRoutes({
    routes: [
      {
        path: '/segnalazioni/:id/tirocinio',
        element: (
          <main>
            <InternshipPage />
          </main>
        ),
      },
    ],
    initialPath: '/segnalazioni/T9/tirocinio',
  });

describe('<InternshipPage>', () => {
  beforeEach(() => {
    vi.mocked(fetchTicket).mockResolvedValue(ticket);
    vi.mocked(fetchInternshipTypes).mockResolvedValue([
      { id: 'T1', code: 'CURRICULARE', name: 'Curriculare' },
    ]);
    vi.mocked(saveInternship).mockReset();
    vi.mocked(addInternshipLegalRepresentative).mockReset();
    vi.mocked(addInternshipAtecoCode).mockReset();
  });

  it('dati dell’azienda mancanti: il Tutor li aggiunge da qui, poi salva la scheda', async () => {
    const user = userEvent.setup();
    vi.mocked(fetchInternship)
      .mockResolvedValueOnce(internship())
      .mockResolvedValueOnce(
        internship({ legalRepresentative: { firstName: 'Laura', lastName: 'Bianchi', version: 0 } }),
      )
      .mockResolvedValue(
        internship({
          legalRepresentative: { firstName: 'Laura', lastName: 'Bianchi', version: 0 },
          atecoCodes: [{ id: 'A1', code: '56.10.11' }],
        }),
      );
    vi.mocked(addInternshipLegalRepresentative).mockResolvedValue({
      firstName: 'Laura',
      lastName: 'Bianchi',
      version: 0,
    });
    vi.mocked(addInternshipAtecoCode).mockResolvedValue({ id: 'A1', code: '56.10.11' });
    vi.mocked(saveInternship).mockResolvedValue(internship({ version: 0 }));
    renderPage();

    expect(
      await screen.findByRole('heading', { level: 1, name: 'Scheda del tirocinio · segnalazione n. 9' }),
    ).toBeInTheDocument();
    expect(screen.getByText('Beneficiario: Anna Bianchi · Azienda: Demo Ristorazione')).toBeInTheDocument();
    expect(screen.getByText('Il legale rappresentante non è ancora indicato.')).toBeInTheDocument();
    await expectNoAxeViolations();

    await user.click(screen.getByRole('button', { name: 'Inserisci il legale rappresentante' }));
    expect(
      screen.getByRole('heading', { level: 4, name: 'Inserisci il legale rappresentante' }),
    ).toHaveFocus();
    await user.type(screen.getByLabelText(/^Nome/), 'Laura');
    await user.type(screen.getByLabelText(/^Cognome/), 'Bianchi');
    await user.click(screen.getByRole('button', { name: 'Salva' }));
    const added = await screen.findByText('Legale rappresentante inserito nella scheda dell’azienda.');
    await waitFor(() => expect(added).toHaveFocus());
    expect(vi.mocked(addInternshipLegalRepresentative).mock.calls[0][0]).toMatchObject({
      ticketId: 'T9',
      payload: { firstName: 'Laura', lastName: 'Bianchi', version: null },
    });
    expect(await screen.findByText('Laura Bianchi')).toBeInTheDocument();
    // c'è: da qui non si corregge
    expect(
      screen.queryByRole('button', { name: 'Inserisci il legale rappresentante' }),
    ).not.toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Aggiungi un codice ATECO' }));
    await user.type(screen.getByRole('textbox', { name: 'Codice (obbligatorio)' }), '56.10.11');
    await user.click(screen.getByRole('button', { name: 'Aggiungi' }));
    expect(
      await screen.findByText('Codice 56.10.11 aggiunto all’azienda: ora puoi sceglierlo nella scheda.'),
    ).toBeInTheDocument();
    expect(vi.mocked(addInternshipAtecoCode).mock.calls[0][0]).toEqual({
      ticketId: 'T9',
      code: '56.10.11',
      description: null,
    });
    await user.click(await screen.findByRole('radio', { name: /56\.10\.11/ }));
    await user.click(screen.getByRole('button', { name: 'Salva la scheda' }));

    const saved = await screen.findByText('Scheda del tirocinio salvata.');
    await waitFor(() => expect(saved).toHaveFocus());
    expect(vi.mocked(saveInternship).mock.calls[0][0]).toMatchObject({
      ticketId: 'T9',
      payload: { atecoCodeId: 'A1', version: null },
    });
  });

  it('conflitto al salvataggio: il motivo con il focus', async () => {
    const user = userEvent.setup();
    vi.mocked(fetchInternship).mockResolvedValue(internship({ version: 2 }));
    vi.mocked(saveInternship).mockRejectedValue(
      error(409, { message: 'La scheda del tirocinio è stata modificata nel frattempo' }),
    );
    renderPage();

    await user.click(await screen.findByRole('button', { name: 'Salva la scheda' }));

    const outcome = await screen.findByText(
      'Non salvata: La scheda del tirocinio è stata modificata nel frattempo',
    );
    await waitFor(() => expect(outcome).toHaveFocus());
  });

  it('scheda già salvata: il modulo riparte dai dati salvati e si corregge in qualunque momento', async () => {
    const user = userEvent.setup();
    vi.mocked(fetchInternship).mockResolvedValue(
      internship({
        version: 4,
        legalRepresentative: { firstName: 'Laura', lastName: 'Bianchi', version: 0 },
        internshipType: { id: 'T1', code: 'CURRICULARE', name: 'Curriculare' },
        startDate: '2026-12-01',
        atecoCodes: [{ id: 'A1', code: '56.10.11' }],
        atecoCodeId: 'A1',
        companyTutors: [{ fullName: 'Anna Verdi', contact: '055 123456' }],
        suspensions: [{ from: '2026-12-22', to: '2027-01-06' }],
        schedule: [{ dayOfWeek: 1, firstSlot: true, secondSlot: false }],
        weeklyMinutes: 240,
        totalMinutes: 4800,
      }),
    );
    vi.mocked(saveInternship).mockResolvedValue(internship({ version: 5 }));
    renderPage();

    expect(await screen.findByLabelText('Data di inizio')).toHaveValue('2026-12-01');
    expect(screen.getByRole('radio', { name: /56\.10\.11/ })).toBeChecked();
    expect(screen.getByLabelText(/^Nome e cognome del tutor aziendale 1/)).toHaveValue('Anna Verdi');
    expect(screen.getByLabelText(/^Periodo 1: al/)).toHaveValue('2027-01-06');
    expect(screen.getByRole('checkbox', { name: 'Lunedì, Prima fascia' })).toBeChecked();
    expect(screen.getByText('Monte ore complessivo: 80 ore')).toBeInTheDocument();
    // il legale rappresentante c'è: da qui non si corregge
    expect(
      screen.queryByRole('button', { name: 'Inserisci il legale rappresentante' }),
    ).not.toBeInTheDocument();
    await expectNoAxeViolations();

    await user.click(screen.getByRole('button', { name: 'Salva la scheda' }));
    await screen.findByText('Scheda del tirocinio salvata.');
    expect(vi.mocked(saveInternship).mock.calls[0][0].payload).toMatchObject({
      internshipTypeId: 'T1',
      atecoCodeId: 'A1',
      version: 4,
    });
  });

  it('prima dell’appuntamento fissato: la scheda non c’è ancora', async () => {
    vi.mocked(fetchInternship).mockRejectedValue(
      error(409, { message: 'La scheda del tirocinio si compila dopo l’appuntamento fissato' }),
    );
    renderPage();

    expect(
      await screen.findByRole('heading', {
        level: 1,
        name: 'La scheda del tirocinio si compila dopo l’appuntamento fissato.',
      }),
    ).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Torna alla segnalazione' })).toHaveAttribute(
      'href',
      '/segnalazioni/T9',
    );
  });
});
