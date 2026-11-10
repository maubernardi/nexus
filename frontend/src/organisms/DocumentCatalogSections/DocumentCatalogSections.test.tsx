import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AxiosError, AxiosHeaders } from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { fetchCatalog, type CatalogItem, type CatalogKind } from '@/config/api/catalogApi';
import {
  fetchCareAgencies,
  fetchCompetenceSites,
  fetchProjectInternshipTypes,
  setCompetenceSiteProjects,
  setProjectInternshipTypes,
  updateCareAgency,
  type CareAgency,
} from '@/config/api/documentCatalogApi';
import {
  CareAgenciesSection,
  CompetenceSitesSection,
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
