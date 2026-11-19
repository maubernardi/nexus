import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { searchCompanies, type Company } from '@/config/api/companyApi';
import { CompaniesPage } from '@/pages/CompaniesPage/CompaniesPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderRoutes } from '@/tests/renderWithProviders';

vi.mock('@/config/api/referenceApi', () => ({
  fetchBusinessCategories: vi.fn().mockResolvedValue([
    { id: 'K14', code: 'BAR_PUB', name: 'Bar / Pub' },
    { id: 'K11', code: 'RISTORANTE_TRATTORIA_BISTR', name: 'Ristorante / Trattoria / Bistrot' },
  ]),
}));
vi.mock('@/config/api/companyApi', () => ({
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
  contactPerson: 'Paola Neri',
  phone: null,
  email: null,
  active,
  version: 0,
  updatedAt: '2026-10-02T08:00:00Z',
});

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
    vi.mocked(searchCompanies).mockResolvedValue([
      company('C1', 'Officina Rossi'),
      company('C2', 'Panificio', false),
    ]);
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
    await expectNoAxeViolations();
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
    expect(searchCompanies).toHaveBeenLastCalledWith({ search: 'rossi', includeInactive: true });
  });

  it('aziende da completare: etichetta nella colonna del dato che manca e filtro nell’indirizzo', async () => {
    vi.mocked(searchCompanies).mockResolvedValue([
      {
        ...company('C3', 'Azienda senza nome n. 1234'),
        vatCode: '00000000000',
        missingData: ['NAME', 'VAT_CODE'],
      },
      { ...company('C4', 'Bar Centrale'), vatCode: '00000000000', missingData: ['VAT_CODE'] },
    ]);
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
    expect(searchCompanies).toHaveBeenLastCalledWith({ includeInactive: false, toComplete: true });
  });

  it('categoria nella colonna e filtro per categoria nell’indirizzo (US-509)', async () => {
    vi.mocked(searchCompanies).mockResolvedValue([
      {
        ...company('C5', 'Pizzeria Napoli'),
        businessCategory: {
          id: 'K11',
          code: 'RISTORANTE_TRATTORIA_BISTR',
          name: 'Ristorante / Trattoria / Bistrot',
        },
      },
      company('C6', 'Officina Verdi'),
    ]);
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
    });
    expect(filter).toHaveDisplayValue('Bar / Pub');
    await expectNoAxeViolations();
  });

  it('nessun risultato: lo dice', async () => {
    vi.mocked(searchCompanies).mockResolvedValue([]);
    renderPage('/aziende?q=zzz');
    expect(await screen.findByText('Nessuna azienda corrisponde alla ricerca.')).toBeInTheDocument();
  });
});
