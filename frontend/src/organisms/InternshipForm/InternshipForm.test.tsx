import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AxiosError, AxiosHeaders } from 'axios';
import { describe, expect, it, vi } from 'vitest';

import type { Internship } from '@/config/api/internshipApi';
import { searchMunicipalities, type Municipality } from '@/config/api/municipalityApi';
import { InternshipForm } from '@/organisms/InternshipForm/InternshipForm';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderWithProviders } from '@/tests/renderWithProviders';

vi.mock('@/config/api/municipalityApi', () => ({ searchMunicipalities: vi.fn() }));

const municipality = (id: string, name: string, provinceAbbreviation: string): Municipality => ({
  id,
  name,
  istatCode: '048017',
  cadastralCode: 'D612',
  provinceName: name,
  provinceAbbreviation,
  validFrom: '2026-01-01',
});

const error = (status: number, body: Record<string, unknown>) =>
  new AxiosError(String(status), String(status), undefined, undefined, {
    status,
    statusText: String(status),
    headers: {},
    config: { headers: new AxiosHeaders() },
    data: { status, ...body },
  });

// come lo manda l'API per una scheda mai salvata: i campi vuoti sono omessi
const empty: Internship = {
  companyName: 'Demo Ristorazione',
  atecoCodes: [{ id: 'A1', code: '56.10.11', description: 'Ristorazione con somministrazione' }],
  // i tipi ammessi dal progetto della segnalazione (US-115)
  internshipTypes: [
    { id: 'T1', code: 'CURRICULARE', name: 'Curriculare' },
    { id: 'T3', code: 'INCLUSIONE', name: 'Di inclusione' },
  ],
  companyTutors: [],
  suspensions: [],
  firstSlotStart: '09:00:00',
  firstSlotEnd: '13:00:00',
  secondSlotStart: '14:00:00',
  secondSlotEnd: '18:00:00',
  excludeHolidays: true,
  schedule: [],
  missingForStart: [],
};

const renderForm = (internship: Internship = empty) => {
  const onSubmit = vi.fn();
  const onSuccess = vi.fn();
  const onConflict = vi.fn();
  renderWithProviders(
    <main>
      <h1>Scheda del tirocinio</h1>
      <InternshipForm
        internship={internship}
        onSubmit={onSubmit}
        onSuccess={onSuccess}
        onConflict={onConflict}
      />
    </main>,
  );
  return { onSubmit, onSuccess, onConflict };
};

describe('<InternshipForm>', () => {
  it('«Di inclusione» non disponibile senza ente e case manager (US-206)', async () => {
    renderForm({ ...empty, inclusionMissing: ['careAgency', 'caseManagerEmail'] });

    const option = await screen.findByRole('option', { name: 'Di inclusione (non disponibile)' });
    expect(option).toBeDisabled();
    expect(screen.getByRole('option', { name: 'Curriculare' })).toBeEnabled();
    const note =
      'Per scegliere «Di inclusione» completa nella scheda del beneficiario: l’ente che lo ha in carico, la mail del case manager.';
    expect(screen.getByText(note)).toBeInTheDocument();
    // la spiegazione è collegata al campo: lo screen reader la legge insieme al tipo
    expect(screen.getByRole('combobox', { name: 'Tipo di tirocinio' })).toHaveAccessibleDescription(note);
  });

  it('«Di inclusione» già scelta resta disponibile', async () => {
    renderForm({
      ...empty,
      internshipType: { id: 'T3', code: 'INCLUSIONE', name: 'Di inclusione' },
      inclusionMissing: ['careAgency'],
    });
    expect(await screen.findByRole('option', { name: 'Di inclusione' })).toBeEnabled();
    expect(screen.getByRole('combobox', { name: 'Tipo di tirocinio' })).not.toHaveAccessibleDescription();
  });

  it('scheda vuota: si salva anche una parte, con righe ripetibili', async () => {
    const user = userEvent.setup();
    const { onSubmit, onSuccess } = renderForm();
    onSubmit.mockResolvedValue({ ...empty, version: 0 });

    expect(await screen.findByRole('option', { name: 'Di inclusione' })).toBeInTheDocument();
    expect(screen.getByText('Nessun tutor aziendale indicato.')).toBeInTheDocument();
    await expectNoAxeViolations();

    await user.selectOptions(screen.getByLabelText('Tipo di tirocinio'), 'T3');
    await user.type(screen.getByLabelText('Data di inizio'), '2026-12-01');
    await user.click(screen.getByRole('radio', { name: /56\.10\.11/ }));
    await user.click(screen.getByRole('button', { name: 'Aggiungi tutor aziendale' }));
    await user.type(screen.getByLabelText(/^Nome e cognome del tutor aziendale 1/), ' Anna Verdi ');
    // funzione o ruolo (US-1009): facoltativo, con un esempio
    const role = screen.getByLabelText(/^Funzione o ruolo del tutor aziendale 1/);
    expect(role).toHaveAccessibleDescription('Facoltativo, per esempio «Responsabile di produzione».');
    await user.type(role, ' Responsabile di sala ');
    await user.click(screen.getByRole('button', { name: 'Aggiungi periodo' }));
    await user.type(screen.getByLabelText(/^Periodo 1: dal/), '2026-12-22');
    await user.type(screen.getByLabelText(/^Periodo 1: al/), '2027-01-06');
    await user.type(screen.getByLabelText(/^Codice fiscale/), 'rssmra85t10a562s');
    await expectNoAxeViolations();
    await user.click(screen.getByRole('button', { name: 'Salva la scheda' }));

    await waitFor(() => expect(onSuccess).toHaveBeenCalled());
    expect(onSubmit.mock.calls[0][0]).toEqual({
      internshipTypeId: 'T3',
      startDate: '2026-12-01',
      expectedEndDate: null,
      site: null,
      siteMunicipalityId: null,
      atecoCodeId: 'A1',
      companyTutors: [{ fullName: 'Anna Verdi', role: 'Responsabile di sala', contact: null }],
      fiscalCode: 'RSSMRA85T10A562S',
      birthDate: null,
      birthPlace: null,
      residenceMunicipalityId: null,
      residenceStreet: null,
      residenceStreetNumber: null,
      suspensions: [{ from: '2026-12-22', to: '2027-01-06' }],
      notes: null,
      firstSlotStart: '09:00',
      firstSlotEnd: '13:00',
      secondSlotStart: '14:00',
      secondSlotEnd: '18:00',
      excludeHolidays: true,
      schedule: [],
      version: null,
    });
  });

  it('griglia oraria: fasce modificabili, ore del giorno, totale mentre si compila', async () => {
    const user = userEvent.setup();
    const { onSubmit, onSuccess } = renderForm();
    onSubmit
      .mockRejectedValueOnce(
        error(400, {
          fieldErrors: [{ field: 'schedule[1].minutes', message: 'Al massimo 7,5 ore nelle fasce scelte' }],
        }),
      )
      .mockResolvedValueOnce({ ...empty, version: 0 });

    expect(await screen.findByText('Totale settimanale: nessun giorno scelto')).toBeInTheDocument();
    expect(
      screen.getByText(
        'Il monte ore si calcola quando la scheda salvata ha data di inizio, data di fine prevista e almeno un giorno nella griglia.',
      ),
    ).toBeInTheDocument();
    await user.click(screen.getByRole('checkbox', { name: 'Lunedì, Prima fascia' }));
    await user.click(screen.getByRole('checkbox', { name: 'Martedì, Prima fascia' }));
    await user.click(screen.getByRole('checkbox', { name: 'Martedì, Seconda fascia' }));
    expect(screen.getByText('Totale settimanale: 12 ore')).toBeInTheDocument();
    // 9–13 e 14–18, ma 3 ore (esempio del PO)
    await user.type(screen.getByRole('textbox', { name: 'Ore di martedì' }), '3');
    expect(screen.getByText('Totale settimanale: 7 ore')).toBeInTheDocument();
    // la prima fascia finisce prima: il lunedì vale 3 ore e mezza
    const firstEnd = screen.getByLabelText('Prima fascia: alle');
    await user.clear(firstEnd);
    await user.type(firstEnd, '12:30');
    expect(screen.getByRole('columnheader', { name: 'Prima fascia (09:00–12:30)' })).toBeInTheDocument();
    expect(screen.getByText('Totale settimanale: 6,5 ore')).toBeInTheDocument();
    await user.click(screen.getByRole('checkbox', { name: 'Escludi dal monte ore le festività nazionali' }));
    await expectNoAxeViolations();

    // controlli prima dell'invio: ore senza fascia, oltre le fasce, non a quarti d'ora
    await user.type(screen.getByRole('textbox', { name: 'Ore di mercoledì' }), '5');
    await user.type(screen.getByRole('textbox', { name: 'Ore di lunedì' }), '4');
    await user.click(screen.getByRole('button', { name: 'Salva la scheda' }));
    expect(
      await screen.findByRole('link', { name: 'Ore di mercoledì: Scegli prima almeno una fascia' }),
    ).toBeInTheDocument();
    expect(
      screen.getByRole('link', { name: 'Ore di lunedì: Al massimo 3,5 ore nelle fasce scelte' }),
    ).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();

    await user.clear(screen.getByRole('textbox', { name: 'Ore di mercoledì' }));
    await user.clear(screen.getByRole('textbox', { name: 'Ore di lunedì' }));
    // l'errore del server sul secondo giorno inviato torna sul martedì
    await user.click(screen.getByRole('button', { name: 'Salva la scheda' }));
    expect(
      await screen.findByRole('link', { name: 'Ore di martedì: Al massimo 7,5 ore nelle fasce scelte' }),
    ).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Salva la scheda' }));
    await waitFor(() => expect(onSuccess).toHaveBeenCalled());
    expect(onSubmit.mock.calls[1][0]).toMatchObject({
      firstSlotEnd: '12:30',
      excludeHolidays: false,
      schedule: [
        { dayOfWeek: 1, firstSlot: true, secondSlot: false, minutes: null },
        { dayOfWeek: 2, firstSlot: true, secondSlot: true, minutes: 180 },
      ],
    });
  });

  it('griglia salvata e monte ore; oltre 40 ore settimanali non si salva', async () => {
    const user = userEvent.setup();
    const full = (dayOfWeek: number) => ({ dayOfWeek, firstSlot: true, secondSlot: true });
    const { onSubmit } = renderForm({
      ...empty,
      version: 2,
      schedule: [full(1), full(2), full(3), full(4), { ...full(5), minutes: 270 }],
      weeklyMinutes: 2190,
      totalMinutes: 30750,
    });

    expect(await screen.findByText('Monte ore complessivo: 512,5 ore')).toBeInTheDocument();
    expect(screen.getByRole('textbox', { name: 'Ore di venerdì' })).toHaveValue('4,5');
    expect(screen.getByText('Totale settimanale: 36,5 ore')).toBeInTheDocument();

    await user.click(screen.getByRole('checkbox', { name: 'Sabato, Prima fascia' }));
    await user.click(screen.getByRole('button', { name: 'Salva la scheda' }));
    expect(
      await screen.findByRole('link', {
        name: 'Griglia oraria e monte ore: Il totale settimanale (40,5 ore) supera le 40 ore',
      }),
    ).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('controlli prima dell’invio: date nell’ordine giusto e periodi senza sovrapposizioni', async () => {
    const user = userEvent.setup();
    const { onSubmit } = renderForm({
      ...empty,
      version: 3,
      startDate: '2026-12-01',
      expectedEndDate: '2026-11-30',
      companyTutors: [{ fullName: ' ' }],
      suspensions: [
        { from: '2026-12-22', to: '2027-01-06' },
        { from: '2027-01-06', to: '2027-01-10' },
      ],
    });

    await user.click(await screen.findByRole('button', { name: 'Salva la scheda' }));

    expect(
      await screen.findByRole('link', {
        name: 'Data di fine prevista: La fine prevista non può precedere l’inizio',
      }),
    ).toBeInTheDocument();
    expect(
      screen.getByRole('link', { name: 'Nome e cognome del tutor aziendale 1: Campo obbligatorio' }),
    ).toBeInTheDocument();
    expect(
      screen.getByRole('link', { name: 'Periodo 2: dal: Si sovrappone al periodo 1' }),
    ).toBeInTheDocument();
    expect(
      screen.getByRole('link', {
        name: 'Periodo 1: al: Il periodo finisce dopo la fine prevista del tirocinio',
      }),
    ).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('errori del server sui campi, anche delle righe; il conflitto passa a chi usa il modulo', async () => {
    const user = userEvent.setup();
    const { onSubmit, onConflict } = renderForm({
      ...empty,
      version: 1,
      fiscalCode: 'RSSMRA85T10A562S',
      birthDate: '1985-12-11',
      suspensions: [{ from: '2026-12-22', to: '2027-01-06' }],
    });
    onSubmit
      .mockRejectedValueOnce(
        error(400, {
          fieldErrors: [{ field: 'suspensions[0].from', message: 'Si sovrappone al periodo dal 1/12/2026' }],
        }),
      )
      .mockRejectedValueOnce(
        error(400, {
          fieldErrors: [
            { field: 'fiscalCode', message: 'Il codice fiscale non corrisponde alla data di nascita' },
          ],
        }),
      )
      .mockRejectedValueOnce(error(409, { message: 'La scheda è stata modificata nel frattempo' }));

    await user.click(await screen.findByRole('button', { name: 'Salva la scheda' }));
    expect(
      await screen.findByRole('link', { name: 'Periodo 1: dal: Si sovrappone al periodo dal 1/12/2026' }),
    ).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Salva la scheda' }));
    expect(
      await screen.findByRole('link', {
        name: 'Codice fiscale: Il codice fiscale non corrisponde alla data di nascita',
      }),
    ).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Salva la scheda' }));
    await waitFor(() =>
      expect(onConflict).toHaveBeenCalledWith('La scheda è stata modificata nel frattempo'),
    );
    expect(onSubmit.mock.calls[2][0]).toMatchObject({ version: 1 });
  });

  it('ruolo del tutor aziendale salvato; l’errore del server va sul suo campo (US-1009)', async () => {
    const user = userEvent.setup();
    const { onSubmit } = renderForm({
      ...empty,
      version: 2,
      companyTutors: [{ fullName: 'Anna Verdi', role: 'Capo sala' }],
    });
    onSubmit.mockRejectedValueOnce(
      error(400, { fieldErrors: [{ field: 'companyTutors[0].role', message: 'Massimo 200 caratteri' }] }),
    );

    expect(await screen.findByLabelText(/^Funzione o ruolo del tutor aziendale 1/)).toHaveValue('Capo sala');
    await user.click(screen.getByRole('button', { name: 'Salva la scheda' }));

    expect(
      await screen.findByRole('link', {
        name: 'Funzione o ruolo del tutor aziendale 1: Massimo 200 caratteri',
      }),
    ).toBeInTheDocument();
    expect(screen.getByLabelText(/^Funzione o ruolo del tutor aziendale 1/)).toHaveAttribute(
      'aria-invalid',
      'true',
    );
    expect(onSubmit.mock.calls[0][0].companyTutors).toEqual([
      { fullName: 'Anna Verdi', role: 'Capo sala', contact: null },
    ]);
  });

  it('azienda senza codici ATECO: lo dice al posto della scelta', async () => {
    renderForm({ ...empty, atecoCodes: [] });

    expect(await screen.findByText('L’azienda non ha ancora codici ATECO.')).toBeInTheDocument();
    expect(screen.queryByRole('radio')).not.toBeInTheDocument();
  });

  it('sede e residenza con il comune dall’elenco (US-1007)', async () => {
    const firenze = municipality('M1', 'Firenze', 'FI');
    vi.mocked(searchMunicipalities).mockResolvedValue([municipality('M2', 'Sesto Fiorentino', 'FI')]);
    const { onSubmit } = renderForm({
      ...empty,
      version: 3,
      site: 'Via Roma 1',
      siteMunicipality: firenze,
      residenceStreet: 'Via Dante, Firenze',
    });
    const user = userEvent.setup();
    expect(screen.getByRole('combobox', { name: /^Comune della sede del tirocinio/ })).toHaveValue(
      'Firenze (FI)',
    );
    // la residenza scritta prima come testo libero resta nella via, il comune è da scegliere
    expect(screen.getByLabelText(/^Via di residenza/)).toHaveValue('Via Dante, Firenze');
    const residence = screen.getByRole('combobox', { name: /^Comune di residenza/ });
    expect(residence).toHaveValue('');

    await user.type(residence, 'sesto');
    await screen.findByRole('option', { name: 'Sesto Fiorentino (FI)' });
    await user.keyboard('{ArrowDown}{Enter}');
    expect(residence).toHaveValue('Sesto Fiorentino (FI)');
    await user.clear(screen.getByLabelText(/^Via di residenza/));
    await user.type(screen.getByLabelText(/^Via di residenza/), 'Via Gramsci');
    await user.type(screen.getByLabelText(/^Numero civico/), '12');
    await expectNoAxeViolations();
    await user.click(screen.getByRole('button', { name: 'Salva la scheda' }));

    await waitFor(() => expect(onSubmit).toHaveBeenCalled());
    expect(onSubmit.mock.calls[0][0]).toMatchObject({
      site: 'Via Roma 1',
      siteMunicipalityId: 'M1',
      residenceMunicipalityId: 'M2',
      residenceStreet: 'Via Gramsci',
      residenceStreetNumber: '12',
      version: 3,
    });
  });

  it('l’errore del server sul comune va sul suo campo', async () => {
    const { onSubmit } = renderForm({ ...empty, siteMunicipality: municipality('M1', 'Firenze', 'FI') });
    onSubmit.mockRejectedValue(
      error(400, {
        fieldErrors: [
          { field: 'siteMunicipalityId', message: 'Il comune Firenze non è più valido: sceglilo di nuovo' },
        ],
      }),
    );
    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: 'Salva la scheda' }));

    expect(
      await screen.findByRole('link', {
        name: 'Comune della sede del tirocinio: Il comune Firenze non è più valido: sceglilo di nuovo',
      }),
    ).toBeInTheDocument();
    expect(screen.getByRole('combobox', { name: /^Comune della sede del tirocinio/ })).toHaveAttribute(
      'aria-invalid',
      'true',
    );
  });
});
