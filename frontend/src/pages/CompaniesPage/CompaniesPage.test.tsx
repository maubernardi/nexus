import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { searchCompanies, type Company, type CompanyPage } from '@/config/api/companyApi';
import { CompaniesPage } from '@/pages/CompaniesPage/CompaniesPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderRoutes } from '@/tests/renderWithProviders';

vi.mock('@/config/api/referenceApi', () => ({
  fetchBusinessCategories: vi.fn().mockResolvedValue([
    { id: 'K14', code: 'BAR_PUB', name: 'Bar / Pub' },
    { id: 'K11', code: 'RISTORANTE_TRATTORIA_BISTR', name: 'Ristorante / Trattoria / Bistrot' },
  ]),
}));
vi.mock('@/config/api/companyApi', async (importOriginal) => ({
  // costanti vere (aziende per pagina), funzioni finte
  ...(await importOriginal<object>()),
  searchCompanies: vi.fn(),
  fetchCompany: vi.fn(),
  createCompany: vi.fn(),
  updateCompany: vi.fn(),
  setCompanyActive: vi.fn(),
}));

const company = (id: string, name: string, active = true): Company => ({
  id,
  name,
  vatCode: '12345678901',
  legalAddress: null,
  contacts: [{ name: 'Paola Neri' }],
  phone: null,
  email: null,
  active,
  version: 0,
  updatedAt: '2026-10-02T08:00:00Z',
});

/** Una pagina della risposta (US-513): di norma tutte le aziende in una pagina da 25. */
const pageOf = (companies: Company[], total = companies.length, page = 0, size = 25): CompanyPage => ({
  companies,
  total,
  page,
  size,
});

/** `count` aziende numerate da `from`: «Azienda 001», … */
const numbered = (from: number, count: number): Company[] =>
  Array.from({ length: count }, (_, i) =>
    company(`N${from + i}`, `Azienda ${String(from + i).padStart(3, '0')}`),
  );

const renderPage = (path = '/aziende') =>
  renderRoutes({
    routes: [
      {
        path: '/aziende',
        element: (
          <main>
            <CompaniesPage />
          </main>
        ),
      },
    ],
    initialPath: path,
  });

describe('<CompaniesPage>', () => {
  beforeEach(() => {
    vi.mocked(searchCompanies).mockReset();
    vi.mocked(searchCompanies).mockResolvedValue(
      pageOf([company('C1', 'Officina Rossi'), company('C2', 'Panificio', false)]),
    );
  });

  it('elenca le aziende con link alla scheda e stato in testo', async () => {
    renderPage();

    const table = await screen.findByRole('table', { name: /Aziende ospitanti/ });
    expect(within(table).getByRole('link', { name: 'Officina Rossi' })).toHaveAttribute(
      'href',
      '/aziende/C1',
    );
    expect(within(table).getByText('Disattivata')).toBeInTheDocument();
    expect(screen.getByRole('status')).toHaveTextContent('2 aziende');
    // fino a 10 aziende basta una pagina: niente pulsanti né menu (US-513)
    expect(screen.queryByRole('navigation', { name: /Pagine/ })).not.toBeInTheDocument();
    expect(screen.queryByRole('combobox', { name: 'Aziende per pagina' })).not.toBeInTheDocument();
    await expectNoAxeViolations();
  });

  it('più pagine: quante in tutto e quali, pagina successiva, aziende per pagina (US-513)', async () => {
    vi.mocked(searchCompanies).mockImplementation(({ page, size }) =>
      Promise.resolve(
        pageOf(numbered(page * size + 1, Math.min(size, 13799 - page * size)), 13799, page, size),
      ),
    );
    const user = userEvent.setup();
    const { router } = renderPage();

    const table = await screen.findByRole('table', { name: /Aziende ospitanti/ });
    expect(within(table).getAllByRole('row')).toHaveLength(26);
    expect(screen.getByRole('status')).toHaveTextContent('13.799 aziende: dalla 1 alla 25');
    const pages = screen.getByRole('navigation', { name: 'Pagine dell’elenco delle aziende' });
    expect(pages).toHaveTextContent('Pagina 1 di 552');
    expect(within(pages).getByRole('button', { name: 'Prima' })).toBeDisabled();
    expect(within(pages).getByRole('button', { name: 'Precedente' })).toBeDisabled();
    await expectNoAxeViolations();

    await user.click(within(pages).getByRole('button', { name: 'Successiva' }));
    await waitFor(() => expect(router.state.location.search).toBe('?pagina=2'));
    expect(searchCompanies).toHaveBeenLastCalledWith({ includeInactive: false, page: 1, size: 25 });
    await waitFor(() =>
      expect(screen.getByRole('status')).toHaveTextContent('13.799 aziende: dalla 26 alla 50'),
    );
    // si riparte a leggere dall'inizio della tabella
    expect(screen.getByRole('region', { name: /Aziende ospitanti/ })).toHaveFocus();

    await user.click(within(pages).getByRole('button', { name: 'Ultima' }));
    await waitFor(() => expect(router.state.location.search).toBe('?pagina=552'));
    await waitFor(() =>
      expect(screen.getByRole('status')).toHaveTextContent('13.799 aziende: dalla 13.776 alla 13.799'),
    );
    expect(within(pages).getByRole('button', { name: 'Successiva' })).toBeDisabled();
    expect(within(pages).getByRole('button', { name: 'Ultima' })).toBeDisabled();

    // cambiare le aziende per pagina riparte dalla prima
    const pageSize = screen.getByRole('combobox', { name: 'Aziende per pagina' });
    expect(pageSize).toHaveDisplayValue('25');
    expect(
      within(pageSize)
        .getAllByRole('option')
        .map((o) => o.textContent),
    ).toEqual(['10', '25', '50', '100']);
    await user.selectOptions(pageSize, '100');
    await waitFor(() => expect(router.state.location.search).toBe('?perPagina=100'));
    expect(searchCompanies).toHaveBeenLastCalledWith({ includeInactive: false, page: 0, size: 100 });
    await waitFor(() =>
      expect(screen.getByRole('status')).toHaveTextContent('13.799 aziende: dalla 1 alla 100'),
    );
    expect(screen.getByRole('navigation', { name: /Pagine/ })).toHaveTextContent('Pagina 1 di 138');
  });

  it('un filtro nuovo riparte dalla prima pagina e tiene le aziende per pagina (US-513)', async () => {
    vi.mocked(searchCompanies).mockImplementation(({ page, size }) =>
      Promise.resolve(pageOf(numbered(page * size + 1, size), 500, page, size)),
    );
    const user = userEvent.setup();
    const { router } = renderPage('/aziende?pagina=3&perPagina=50');

    await waitFor(() =>
      expect(screen.getByRole('status')).toHaveTextContent('500 aziende: dalla 101 alla 150'),
    );
    expect(searchCompanies).toHaveBeenLastCalledWith({ includeInactive: false, page: 2, size: 50 });
    await user.click(screen.getByRole('checkbox', { name: 'Mostra anche le aziende disattivate' }));

    await waitFor(() => expect(router.state.location.search).toBe('?disattivate=1&perPagina=50'));
    expect(searchCompanies).toHaveBeenLastCalledWith({ includeInactive: true, page: 0, size: 50 });
  });

  it('pagina oltre l’ultima o valori non validi nell’indirizzo (US-513)', async () => {
    vi.mocked(searchCompanies).mockImplementation(({ page, size }) =>
      Promise.resolve(pageOf(page < 2 ? numbered(page * size + 1, page === 0 ? 25 : 5) : [], 30, page, size)),
    );
    const { router } = renderPage('/aziende?pagina=9');

    // l'indirizzo vecchio passa all'ultima pagina, senza una voce in più nella cronologia
    await waitFor(() => expect(router.state.location.search).toBe('?pagina=2'));
    await waitFor(() => expect(screen.getByRole('status')).toHaveTextContent('30 aziende: dalla 26 alla 30'));
    expect(router.state.historyAction).toBe('REPLACE');
  });

  it('valori non validi nell’indirizzo: prima pagina da 25 (US-513)', async () => {
    renderPage('/aziende?pagina=abc&perPagina=1000');
    await screen.findByRole('table');
    expect(searchCompanies).toHaveBeenLastCalledWith({ includeInactive: false, page: 0, size: 25 });
  });

  it('la ricerca e le disattivate finiscono nell’indirizzo e nella richiesta', async () => {
    const user = userEvent.setup();
    const { router } = renderPage();
    await screen.findByRole('table');

    await user.type(screen.getByRole('searchbox', { name: /Cerca per ragione sociale/ }), 'rossi');
    await user.click(screen.getByRole('button', { name: 'Cerca' }));
    await waitFor(() => expect(router.state.location.search).toBe('?q=rossi'));
    await user.click(screen.getByRole('checkbox', { name: 'Mostra anche le aziende disattivate' }));

    await waitFor(() => expect(router.state.location.search).toBe('?q=rossi&disattivate=1'));
    expect(searchCompanies).toHaveBeenLastCalledWith({
      search: 'rossi',
      includeInactive: true,
      page: 0,
      size: 25,
    });
  });

  it('aziende da completare: etichetta nella colonna del dato che manca e filtro nell’indirizzo', async () => {
    vi.mocked(searchCompanies).mockResolvedValue(
      pageOf([
        {
          ...company('C3', 'Azienda senza nome n. 1234'),
          vatCode: '00000000000',
          missingData: ['NAME', 'VAT_CODE'],
        },
        { ...company('C4', 'Bar Centrale'), vatCode: '00000000000', missingData: ['VAT_CODE'] },
      ]),
    );
    const user = userEvent.setup();
    const { router } = renderPage();

    const table = await screen.findByRole('table', { name: /Aziende ospitanti/ });
    const [, first, second] = within(table).getAllByRole('row');
    expect(within(first).getByRole('rowheader')).toHaveTextContent(
      'Azienda senza nome n. 1234 Da completare',
    );
    expect(within(first).getAllByRole('cell')[0]).toHaveTextContent(/^Da completare$/);
    expect(within(second).getByRole('rowheader')).toHaveTextContent(/^Bar Centrale$/);
    expect(within(second).getAllByRole('cell')[0]).toHaveTextContent(/^Da completare$/);
    expect(table).not.toHaveTextContent('00000000000');
    await expectNoAxeViolations();

    await user.click(screen.getByRole('checkbox', { name: 'Solo le aziende da completare' }));
    await waitFor(() => expect(router.state.location.search).toBe('?daCompletare=1'));
    expect(searchCompanies).toHaveBeenLastCalledWith({
      includeInactive: false,
      toComplete: true,
      page: 0,
      size: 25,
    });
  });

  it('categoria nella colonna e filtro per categoria nell’indirizzo (US-509)', async () => {
    vi.mocked(searchCompanies).mockResolvedValue(
      pageOf([
        {
          ...company('C5', 'Pizzeria Napoli'),
          businessCategory: {
            id: 'K11',
            code: 'RISTORANTE_TRATTORIA_BISTR',
            name: 'Ristorante / Trattoria / Bistrot',
          },
        },
        company('C6', 'Officina Verdi'),
      ]),
    );
    const user = userEvent.setup();
    const { router } = renderPage('/aziende?q=a');

    const table = await screen.findByRole('table', { name: /Aziende ospitanti/ });
    expect(within(table).getByRole('columnheader', { name: 'Categoria' })).toBeInTheDocument();
    const [, first, second] = within(table).getAllByRole('row');
    expect(within(first).getAllByRole('cell')[1]).toHaveTextContent('Ristorante / Trattoria / Bistrot');
    expect(within(second).getAllByRole('cell')[1]).toHaveTextContent('—');

    const filter = screen.getByRole('combobox', { name: 'Categoria merceologica' });
    await waitFor(() => expect(filter).toBeEnabled());
    expect(filter).toHaveDisplayValue('Tutte le categorie');
    await user.selectOptions(filter, 'Bar / Pub');

    await waitFor(() => expect(router.state.location.search).toBe('?q=a&categoria=K14'));
    expect(searchCompanies).toHaveBeenLastCalledWith({
      search: 'a',
      includeInactive: false,
      businessCategoryId: 'K14',
      page: 0,
      size: 25,
    });
    expect(filter).toHaveDisplayValue('Bar / Pub');
    await expectNoAxeViolations();
  });

  it('stelle e pollici nelle colonne e nei filtri dell’indirizzo (US-514)', async () => {
    vi.mocked(searchCompanies).mockResolvedValue(
      pageOf([
        { ...company('C7', 'Officina Rossi'), reliability: 3, projectInterest: 2 },
        company('C8', 'Officina Verdi'),
      ]),
    );
    const user = userEvent.setup();
    const { router } = renderPage('/aziende?stelle=9&interesse=x');

    const table = await screen.findByRole('table', { name: /Aziende ospitanti/ });
    expect(within(table).getByRole('columnheader', { name: 'Affidabilità' })).toBeInTheDocument();
    expect(within(table).getByRole('columnheader', { name: 'Interesse al progetto' })).toBeInTheDocument();
    const [, first, second] = within(table).getAllByRole('row');
    expect(within(first).getAllByRole('cell')[2]).toHaveTextContent('3 stelle su 4');
    expect(within(first).getAllByRole('cell')[3]).toHaveTextContent('2 pollici alzati su 2');
    expect(within(second).getAllByRole('cell')[2]).toHaveTextContent('—Nessuna valutazione');
    // valori non validi nell'indirizzo: nessun filtro
    expect(searchCompanies).toHaveBeenLastCalledWith({ includeInactive: false, page: 0, size: 25 });

    const stars = screen.getByRole('combobox', { name: 'Affidabilità' });
    expect(stars).toHaveDisplayValue('Qualunque');
    await user.selectOptions(stars, 'Almeno 3 stelle');
    await user.selectOptions(screen.getByRole('combobox', { name: 'Interesse al progetto' }), 'Due pollici');

    await waitFor(() => expect(router.state.location.search).toBe('?stelle=3&interesse=2'));
    expect(searchCompanies).toHaveBeenLastCalledWith({
      includeInactive: false,
      minReliability: 3,
      minProjectInterest: 2,
      page: 0,
      size: 25,
    });
    await expectNoAxeViolations();
  });

  it('nessun risultato: lo dice', async () => {
    vi.mocked(searchCompanies).mockResolvedValue(pageOf([]));
    renderPage('/aziende?q=zzz');
    expect(await screen.findByText('Nessuna azienda corrisponde alla ricerca.')).toBeInTheDocument();
  });

  it('referente: il primo, con quanti sono gli altri; il ruolo se manca il nome (US-510)', async () => {
    const titolare = { id: 'R13', code: 'TITOLARE', name: 'Titolare' };
    const personale = { id: 'R3', code: 'RESPONSABILE_DEL_PERSONALE', name: 'Responsabile del personale' };
    vi.mocked(searchCompanies).mockResolvedValue(
      pageOf([
        {
          ...company('C6', 'Hotel Bellavista'),
          contacts: [{ name: 'Paola Neri', role: titolare }, { role: personale }, { name: 'Ugo' }],
        },
        { ...company('C7', 'Hotel Miramare'), contacts: [{ role: personale }, { name: 'Ada' }] },
        { ...company('C8', 'Hotel Stella'), contacts: [] },
      ]),
    );
    renderPage();

    const table = await screen.findByRole('table', { name: /Aziende ospitanti/ });
    expect(within(table).getByRole('columnheader', { name: 'Referente' })).toBeInTheDocument();
    const [, bellavista, miramare, stella] = within(table).getAllByRole('row');
    expect(within(bellavista).getAllByRole('cell')[4]).toHaveTextContent(/^Paola Neri e altri 2$/);
    expect(within(miramare).getAllByRole('cell')[4]).toHaveTextContent(
      /^Responsabile del personale e un altro$/,
    );
    expect(within(stella).getAllByRole('cell')[4]).toHaveTextContent(/^—$/);
  });
});
