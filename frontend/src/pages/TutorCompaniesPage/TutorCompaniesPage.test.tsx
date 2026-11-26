import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { searchCompanyDirectory, type CompanyDirectoryItem } from '@/config/api/companyApi';
import { TutorCompaniesPage } from '@/pages/TutorCompaniesPage/TutorCompaniesPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderRoutes } from '@/tests/renderWithProviders';

vi.mock('@/config/api/referenceApi', () => ({
  fetchBusinessCategories: vi.fn().mockResolvedValue([{ id: 'K14', code: 'BAR_PUB', name: 'Bar / Pub' }]),
  fetchZones: vi.fn().mockResolvedValue([
    { id: 'Z1', code: 'RIFREDI', name: 'Rifredi' },
    { id: 'Z2', code: 'NOVOLI', name: 'Novoli' },
  ]),
}));
vi.mock('@/config/api/companyApi', async (importOriginal) => ({
  // costanti vere (aziende per pagina), funzioni finte
  ...(await importOriginal<object>()),
  searchCompanyDirectory: vi.fn(),
}));

const rossi: CompanyDirectoryItem = {
  id: 'C1',
  name: 'Officina Rossi',
  businessCategory: { id: 'K9', code: 'OFFICINE', name: 'Officine Auto-Moto' },
  zone: { id: 'Z1', code: 'RIFREDI', name: 'Rifredi' },
  address: 'Via Pratese 9',
  municipality: 'Prato (PO)',
  reliability: 3,
  projectInterest: 1,
};

const renderPage = (path = '/aziende') =>
  renderRoutes({
    routes: [
      {
        path: '/aziende',
        element: (
          <main>
            <TutorCompaniesPage />
          </main>
        ),
      },
    ],
    initialPath: path,
  });

describe('<TutorCompaniesPage>', () => {
  beforeEach(() => {
    vi.mocked(searchCompanyDirectory).mockReset();
    vi.mocked(searchCompanyDirectory).mockResolvedValue({
      companies: [rossi, { id: 'C2', name: 'Bar Sport' }],
      total: 2,
      page: 0,
      size: 25,
    });
  });

  it('elenco in sola lettura con i dati essenziali: nessun collegamento alla scheda (US-511)', async () => {
    renderPage();
    const table = await screen.findByRole('table', { name: 'Aziende attive, in ordine di ragione sociale' });
    const headers = within(table)
      .getAllByRole('columnheader')
      .map((h) => h.textContent);
    expect(headers).toEqual([
      'Ragione sociale',
      'Categoria',
      'Zona',
      'Indirizzo',
      'Affidabilità',
      'Interesse al progetto',
    ]);
    const [, first, second] = within(table).getAllByRole('row');
    expect(within(first).getByRole('rowheader')).toHaveTextContent('Officina Rossi');
    const cells = within(first).getAllByRole('cell');
    expect(cells[0]).toHaveTextContent('Officine Auto-Moto');
    expect(cells[1]).toHaveTextContent('Rifredi');
    expect(cells[2]).toHaveTextContent('Via Pratese 9, Prato (PO)');
    expect(cells[3]).toHaveTextContent('3 stelle su 4');
    expect(cells[4]).toHaveTextContent('1 pollice alzato su 2');
    // senza dati: trattini
    expect(within(second).getAllByRole('cell')[2]).toHaveTextContent('—');
    expect(within(table).queryByRole('link')).not.toBeInTheDocument();
    expect(screen.queryByRole('link', { name: 'Nuova azienda' })).not.toBeInTheDocument();
    expect(screen.getByRole('status')).toHaveTextContent('2 aziende');
    await expectNoAxeViolations();
  });

  it('ricerca sul nome e filtri per categoria e zona nell’indirizzo', async () => {
    const user = userEvent.setup();
    const { router } = renderPage();
    await screen.findByRole('table');

    await user.type(screen.getByRole('searchbox', { name: 'Cerca per ragione sociale' }), 'officina');
    await user.click(screen.getByRole('button', { name: 'Cerca' }));
    const zone = screen.getByRole('combobox', { name: 'Zona' });
    await waitFor(() => expect(zone).toBeEnabled());
    await user.selectOptions(zone, 'Rifredi');

    await waitFor(() => expect(router.state.location.search).toBe('?q=officina&zona=Z1'));
    expect(searchCompanyDirectory).toHaveBeenLastCalledWith({
      search: 'officina',
      zoneId: 'Z1',
      page: 0,
      size: 25,
    });
  });

  it('«Azzera i filtri» toglie ricerca e filtri e riporta il focus sulla ricerca', async () => {
    const user = userEvent.setup();
    const { router } = renderPage('/aziende?q=officina&categoria=K14&zona=Z1');
    await screen.findByRole('table');
    await user.click(screen.getByRole('button', { name: 'Azzera i filtri' }));

    await waitFor(() => expect(router.state.location.search).toBe(''));
    expect(searchCompanyDirectory).toHaveBeenLastCalledWith({ page: 0, size: 25 });
    await waitFor(() =>
      expect(screen.getByRole('searchbox', { name: 'Cerca per ragione sociale' })).toHaveFocus(),
    );
    expect(screen.queryByRole('button', { name: 'Azzera i filtri' })).not.toBeInTheDocument();
  });

  it('una zona dell’indirizzo non più attiva resta scelta, con il suo nome', async () => {
    renderPage('/aziende?zona=VECCHIA');
    const zone = screen.getByRole('combobox', { name: 'Zona' });
    await waitFor(() => expect(zone).toHaveDisplayValue('Zona non più attiva'));
  });

  it('senza filtri non c’è «Azzera i filtri»', async () => {
    renderPage();
    await screen.findByRole('table');
    expect(screen.queryByRole('button', { name: 'Azzera i filtri' })).not.toBeInTheDocument();
  });

  it('nessun risultato: lo dice', async () => {
    vi.mocked(searchCompanyDirectory).mockResolvedValue({ companies: [], total: 0, page: 0, size: 25 });
    renderPage('/aziende?q=nulla');
    expect(await screen.findByText('Nessuna azienda corrisponde alla ricerca.')).toBeInTheDocument();
  });
});
