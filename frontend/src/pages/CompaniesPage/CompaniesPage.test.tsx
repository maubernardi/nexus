import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { searchCompanies, type Company } from '@/config/api/companyApi';
import { CompaniesPage } from '@/pages/CompaniesPage/CompaniesPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderRoutes } from '@/tests/renderWithProviders';

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

  it('nessun risultato: lo dice', async () => {
    vi.mocked(searchCompanies).mockResolvedValue([]);
    renderPage('/aziende?q=zzz');
    expect(await screen.findByText('Nessuna azienda corrisponde alla ricerca.')).toBeInTheDocument();
  });
});
