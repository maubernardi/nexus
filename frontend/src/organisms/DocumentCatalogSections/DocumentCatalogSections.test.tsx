import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AxiosError, AxiosHeaders } from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { fetchCatalog, type CatalogItem, type CatalogKind } from '@/config/api/catalogApi';
import {
  fetchCareAgencies,
  fetchCompetenceSites,
  fetchProjectDocuments,
  fetchProjectInternshipTypes,
  setCompetenceSiteProjects,
  setProjectInternshipTypes,
  updateCareAgency,
  updateProjectDocuments,
  PROMOTER_FIELDS,
  type CareAgency,
} from '@/config/api/documentCatalogApi';
import {
  CareAgenciesSection,
  CompetenceSitesSection,
  ProjectDocumentsSection,
  ProjectInternshipTypesSection,
} from '@/organisms/DocumentCatalogSections/DocumentCatalogSections';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderWithProviders } from '@/tests/renderWithProviders';

vi.mock('@/config/api/catalogApi', () => ({
  fetchCatalog: vi.fn(),
  createCatalogItem: vi.fn(),
  renameCatalogItem: vi.fn(),
  setCatalogItemActive: vi.fn(),
}));
vi.mock('@/config/api/documentCatalogApi', async (original) => ({
  ...(await original<Record<string, unknown>>()),
  fetchCompetenceSites: vi.fn(),
  fetchProjectInternshipTypes: vi.fn(),
  fetchProjectDocuments: vi.fn(),
  updateProjectDocuments: vi.fn(),
  fetchCareAgencies: vi.fn(),
  setCompetenceSiteProjects: vi.fn(),
  setProjectInternshipTypes: vi.fn(),
  updateCareAgency: vi.fn(),
}));

const item = (id: string, name: string, code: string, active = true): CatalogItem => ({
  id,
  name,
  code,
  active,
});
const gol = { id: 'P1', code: 'GOL', name: 'GOL' };
const polis = { id: 'P2', code: 'POLIS', name: 'POLIS' };
const extra = { id: 'T2', code: 'EXTRACURRICULARE', name: 'Extracurriculare' };
const inclusione = { id: 'T3', code: 'INCLUSIONE', name: 'Di inclusione' };

const catalogs: Partial<Record<CatalogKind, CatalogItem[]>> = {
  projects: [item('P1', 'GOL', 'GOL'), item('P2', 'POLIS', 'POLIS'), item('P3', 'Vecchio', 'VECCHIO', false)],
  'internship-types': [
    item('T1', 'Curriculare', 'CURRICULARE'),
    item('T2', 'Extracurriculare', 'EXTRACURRICULARE'),
    item('T3', 'Di inclusione', 'INCLUSIONE'),
  ],
  'competence-sites': [item('S1', 'Firenze', 'FIRENZE'), item('S2', 'Empoli', 'EMPOLI')],
  'care-agencies': [item('E1', 'Comune di Firenze', 'COMUNE_DI_FIRENZE')],
};

const AVVISO = 'Avviso pubblico n. 10 per l’attuazione del Programma Garanzia Occupabilità dei Lavoratori';

const comune: CareAgency = {
  id: 'E1',
  code: 'COMUNE_DI_FIRENZE',
  name: 'Comune di Firenze',
  active: true,
  kind: 'COMUNE',
  legalAddress: 'Piazza della Signoria 1, Firenze',
  version: 3,
};

const badRequest = (field: string, message: string) =>
  new AxiosError('400', '400', undefined, undefined, {
    status: 400,
    statusText: '400',
    headers: {},
    config: { headers: new AxiosHeaders() },
    data: { status: 400, fieldErrors: [{ field, message }] },
  });

const render = (node: React.ReactNode) =>
  renderWithProviders(
    <main>
      <h1>Cataloghi</h1>
      {node}
    </main>,
  );

describe('cataloghi dei documenti (US-115)', () => {
  beforeEach(() => {
    vi.mocked(fetchCatalog).mockImplementation(async (kind) => catalogs[kind] ?? []);
    vi.mocked(fetchCompetenceSites).mockResolvedValue([
      { id: 'S1', code: 'FIRENZE', name: 'Firenze', active: true, projects: [gol] },
      { id: 'S2', code: 'EMPOLI', name: 'Empoli', active: true, projects: [] },
    ]);
    vi.mocked(fetchProjectInternshipTypes).mockResolvedValue([
      { project: gol, active: true, internshipTypes: [inclusione, extra] },
      { project: polis, active: true, internshipTypes: [] },
    ]);
    vi.mocked(fetchCareAgencies).mockResolvedValue([comune]);
    vi.mocked(fetchProjectDocuments).mockResolvedValue([
      {
        project: gol,
        active: true,
        noticeReference: AVVISO,
        defaultNotice: true,
        headerLine: 'Firenze in Rete 2 – Codice 9014358 – CUP B81D24000440006',
        promoter: { name: 'Cooperativa di prova', pec: 'prova@pec.example', rcExpiry: '2027-07-22' },
        variables: [{ name: 'SEDE_OPERATIVA', value: 'Via Roma 1, Firenze' }],
      },
      // come lo manda l'API: senza intestazione il campo è omesso
      { project: polis, active: true, noticeReference: 'Avviso POLIS', defaultNotice: false },
    ]);
    vi.mocked(updateProjectDocuments).mockReset();
    vi.mocked(setCompetenceSiteProjects).mockReset();
    vi.mocked(setProjectInternshipTypes).mockReset();
    vi.mocked(updateCareAgency).mockReset();
  });

  it('sedi di competenza: progetti in tabella e modifica', async () => {
    vi.mocked(setCompetenceSiteProjects).mockResolvedValue({
      id: 'S1',
      code: 'FIRENZE',
      name: 'Firenze',
      active: true,
      projects: [gol, polis],
    });
    const user = userEvent.setup();
    render(<CompetenceSitesSection />);

    const table = await screen.findByRole('table', { name: 'Sedi di competenza: 2 voci' });
    expect(await within(table).findByText('GOL')).toBeInTheDocument();
    expect(within(table).getByText('Nessun progetto')).toBeInTheDocument();
    await expectNoAxeViolations();

    await user.click(within(table).getByRole('button', { name: 'Progetti Firenze' }));
    expect(await screen.findByRole('heading', { name: 'Progetti di «Firenze»' })).toHaveFocus();
    const group = screen.getByRole('group', { name: 'Progetti della sede' });
    expect(within(group).getByRole('checkbox', { name: 'GOL' })).toBeChecked();
    // un progetto disattivato non si propone
    expect(within(group).queryByRole('checkbox', { name: 'Vecchio' })).not.toBeInTheDocument();
    await user.click(within(group).getByRole('checkbox', { name: 'POLIS' }));
    await user.click(screen.getByRole('button', { name: 'Salva' }));

    expect(vi.mocked(setCompetenceSiteProjects).mock.calls[0][0]).toEqual({ id: 'S1', ids: ['P1', 'P2'] });
    expect(await screen.findByText('Progetti di «Firenze» salvati.')).toBeInTheDocument();
  });

  it('tipi per progetto: «Tutti i tipi» se nessuno, e modifica', async () => {
    vi.mocked(setProjectInternshipTypes).mockResolvedValue({
      project: polis,
      active: true,
      internshipTypes: [extra],
    });
    const user = userEvent.setup();
    render(<ProjectInternshipTypesSection />);

    const table = await screen.findByRole('table', { name: 'Tipi di tirocinio ammessi per progetto' });
    expect(within(table).getByText('Di inclusione, Extracurriculare')).toBeInTheDocument();
    expect(within(table).getByText('Tutti i tipi')).toBeInTheDocument();
    await expectNoAxeViolations();

    await user.click(within(table).getByRole('button', { name: 'Modifica i tipi POLIS' }));
    const group = await screen.findByRole('group', { name: 'Tipi di tirocinio ammessi' });
    await user.click(within(group).getByRole('checkbox', { name: 'Extracurriculare' }));
    await user.click(screen.getByRole('button', { name: 'Salva' }));
    expect(vi.mocked(setProjectInternshipTypes).mock.calls[0][0]).toEqual({ id: 'P2', ids: ['T2'] });
    expect(await screen.findByText('Tipi ammessi da «POLIS» salvati.')).toBeInTheDocument();
  });

  it('intestazione e avviso per progetto (US-1011): tabella, modifica ed errori del server', async () => {
    vi.mocked(updateProjectDocuments)
      .mockRejectedValueOnce(badRequest('headerLine', 'Massimo 300 caratteri'))
      .mockResolvedValueOnce({ project: polis, active: true, noticeReference: AVVISO, defaultNotice: true });
    const user = userEvent.setup();
    render(<ProjectDocumentsSection />);

    const table = await screen.findByRole('table', { name: 'Dati dei documenti per progetto' });
    expect(
      within(table).getByText('Firenze in Rete 2 – Codice 9014358 – CUP B81D24000440006'),
    ).toBeInTheDocument();
    expect(within(table).getByText('Testo predefinito')).toBeInTheDocument();
    expect(within(table).getByText('Nessuna intestazione')).toBeInTheDocument();
    expect(within(table).getByText('Testo del progetto')).toBeInTheDocument();
    await expectNoAxeViolations();

    await user.click(within(table).getByRole('button', { name: 'Modifica POLIS' }));
    expect(await screen.findByRole('heading', { name: 'Dati dei documenti di «POLIS»' })).toHaveFocus();
    const notice = screen.getByLabelText(/^Riferimento all’avviso/);
    expect(notice).toHaveValue('Avviso POLIS');
    await user.type(screen.getByLabelText(/^Intestazione dei documenti/), ' POLIS – CUP X ');
    await user.clear(notice);
    await expectNoAxeViolations();
    await user.click(screen.getByRole('button', { name: 'Salva' }));

    expect(
      await screen.findByRole('link', { name: 'Intestazione dei documenti: Massimo 300 caratteri' }),
    ).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'Salva' }));
    await waitFor(() => expect(updateProjectDocuments).toHaveBeenCalledTimes(2));
    // svuotato, l'avviso torna al testo predefinito
    expect(vi.mocked(updateProjectDocuments).mock.calls[1][0]).toEqual({
      id: 'P2',
      headerLine: 'POLIS – CUP X',
      noticeReference: null,
      promoter: Object.fromEntries(PROMOTER_FIELDS.map((f) => [f, null])),
      variables: [],
    });
    expect(await screen.findByText('Dati dei documenti di «POLIS» salvati.')).toHaveFocus();
  });

  it('promotore e variabili per progetto (US-1013): tabella, modifica ed errori', async () => {
    vi.mocked(updateProjectDocuments)
      .mockRejectedValueOnce(
        badRequest('variables', 'La variabile SEDE_OPERATIVA è usata dal modello «Convenzione GOL»'),
      )
      .mockResolvedValueOnce({ project: gol, active: true, noticeReference: AVVISO, defaultNotice: true });
    const user = userEvent.setup();
    render(<ProjectDocumentsSection />);

    const table = await screen.findByRole('table', { name: 'Dati dei documenti per progetto' });
    expect(within(table).getByText('Cooperativa di prova')).toBeInTheDocument();
    expect(within(table).getByText('SEDE_OPERATIVA')).toBeInTheDocument();
    expect(within(table).getByText('Non indicato')).toBeInTheDocument();
    expect(within(table).getByText('Nessuna')).toBeInTheDocument();

    await user.click(within(table).getByRole('button', { name: 'Modifica GOL' }));
    await screen.findByRole('heading', { name: 'Dati dei documenti di «GOL»' });
    expect(screen.getByLabelText(/^Denominazione/)).toHaveValue('Cooperativa di prova');
    expect(screen.getByLabelText(/^Scadenza della polizza RC/)).toHaveValue('2027-07-22');
    // il nome di una variabile salvata non si cambia
    expect(screen.getByLabelText(/^Nome della variabile 1/)).toHaveAttribute('readonly');
    await user.type(screen.getByLabelText(/^Numero della polizza RC/), 'RC-1');

    // variabile nuova: nome ripetuto, poi non valido, poi giusto
    await user.click(screen.getByRole('button', { name: 'Aggiungi una variabile' }));
    const name = screen.getByLabelText(/^Nome della variabile 2/);
    expect(name).toHaveFocus();
    await user.type(name, 'sede_operativa');
    await user.type(screen.getByLabelText(/^Valore della variabile 2/), 'Via Verdi 2');
    await expectNoAxeViolations();
    await user.click(screen.getByRole('button', { name: 'Salva' }));
    expect(
      await screen.findByRole('link', {
        name: 'Nome della variabile 2: C’è già una variabile con questo nome',
      }),
    ).toBeInTheDocument();
    await user.clear(name);
    await user.type(name, '1codice');
    expect(
      await screen.findByText('Usa lettere maiuscole, cifre e trattino basso, cominciando con una lettera'),
    ).toBeInTheDocument();
    await user.clear(name);
    await user.type(name, 'codice_ats');
    expect(updateProjectDocuments).not.toHaveBeenCalled();

    // la variabile usata da un modello: l'errore del server sulle variabili
    await user.click(screen.getByRole('button', { name: 'Salva' }));
    expect(
      await screen.findByRole('link', {
        name: 'Variabili del progetto: La variabile SEDE_OPERATIVA è usata dal modello «Convenzione GOL»',
      }),
    ).toBeInTheDocument();
    expect(vi.mocked(updateProjectDocuments).mock.calls[0][0]).toMatchObject({
      id: 'P1',
      promoter: { name: 'Cooperativa di prova', rcPolicy: 'RC-1', rcExpiry: '2027-07-22', fiscalCode: null },
      variables: [
        { name: 'SEDE_OPERATIVA', value: 'Via Roma 1, Firenze', description: null },
        { name: 'CODICE_ATS', value: 'Via Verdi 2', description: null },
      ],
    });

    // togliere una variabile riporta il focus su «Aggiungi una variabile»
    await user.click(screen.getByRole('button', { name: 'Togli la variabile 1' }));
    expect(screen.getByRole('button', { name: 'Aggiungi una variabile' })).toHaveFocus();
    await user.click(screen.getByRole('button', { name: 'Salva' }));
    await waitFor(() => expect(updateProjectDocuments).toHaveBeenCalledTimes(2));
    expect(vi.mocked(updateProjectDocuments).mock.calls[1][0].variables).toEqual([
      { name: 'CODICE_ATS', value: 'Via Verdi 2', description: null },
    ]);
    expect(await screen.findByText('Dati dei documenti di «GOL» salvati.')).toHaveFocus();
  });

  it('enti: tipologia in tabella, dati con gli errori del server', async () => {
    vi.mocked(updateCareAgency)
      .mockRejectedValueOnce(badRequest('referentEmail', 'Inserisci un indirizzo email valido'))
      .mockResolvedValueOnce({ ...comune, version: 4 });
    const user = userEvent.setup();
    render(<CareAgenciesSection />);

    const table = await screen.findByRole('table', { name: 'Enti di presa in carico: 1 voce' });
    expect(await within(table).findByText('Comune')).toBeInTheDocument();
    await user.click(within(table).getByRole('button', { name: 'Dati Comune di Firenze' }));

    expect(await screen.findByRole('heading', { name: 'Dati di «Comune di Firenze»' })).toHaveFocus();
    expect(screen.getByLabelText('Sede legale')).toHaveValue('Piazza della Signoria 1, Firenze');
    await user.type(screen.getByLabelText('Nome e cognome del referente'), 'Maria Prova');
    await user.type(screen.getByLabelText('Mail del referente'), 'non-una-mail');
    await user.click(screen.getByRole('button', { name: 'Salva' }));
    expect(
      await screen.findByText('Inserisci un indirizzo email valido', { selector: 'p' }),
    ).toBeInTheDocument();
    await expectNoAxeViolations();

    await user.clear(screen.getByLabelText('Mail del referente'));
    await user.click(screen.getByRole('button', { name: 'Salva' }));
    await waitFor(() => expect(updateCareAgency).toHaveBeenCalledTimes(2));
    expect(vi.mocked(updateCareAgency).mock.calls[1][0]).toEqual({
      id: 'E1',
      version: 3,
      data: {
        kind: 'COMUNE',
        legalAddress: 'Piazza della Signoria 1, Firenze',
        referentName: 'Maria Prova',
      },
    });
    expect(await screen.findByText('Dati di «Comune di Firenze» salvati.')).toBeInTheDocument();
  });
});
