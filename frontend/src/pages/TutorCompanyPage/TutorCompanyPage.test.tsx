import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AxiosError, AxiosHeaders } from 'axios';

import { fetchCompanyDirectoryItem, fetchCompanyReviews } from '@/config/api/companyApi';
import { createContactRequest } from '@/config/api/contactRequestApi';
import { TutorCompanyPage } from '@/pages/TutorCompanyPage/TutorCompanyPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { contactRequest } from '@/tests/fixtures/contactRequest';
import { renderRoutes } from '@/tests/renderWithProviders';

vi.mock('@/config/api/contactRequestApi', () => ({ createContactRequest: vi.fn() }));
vi.mock('@/config/api/companyApi', async (importOriginal) => ({
  ...(await importOriginal<object>()),
  fetchCompanyDirectoryItem: vi.fn(),
  fetchCompanyReviews: vi.fn(),
}));

const renderPage = () =>
  renderRoutes({
    routes: [
      { path: '/richieste', element: <p>Le mie richieste</p> },
      {
        path: '/aziende/:id',
        element: (
          <main>
            <TutorCompanyPage />
          </main>
        ),
      },
    ],
    initialPath: '/aziende/C1',
  });

describe('<TutorCompanyPage>', () => {
  beforeEach(() => {
    vi.mocked(fetchCompanyReviews).mockResolvedValue({ reviews: [], total: 0, page: 0, size: 10 });
  });

  it('scheda in sola lettura con i dati della lista e le valutazioni (US-1104)', async () => {
    vi.mocked(fetchCompanyDirectoryItem).mockResolvedValue({
      id: 'C1',
      name: 'Officina Rossi',
      zone: { id: 'Z1', code: 'RIFREDI', name: 'Rifredi' },
      address: 'Via Pratese 9',
      municipality: 'Prato (PO)',
      reliability: 2,
    });
    renderPage();
    expect(await screen.findByRole('heading', { level: 1, name: 'Officina Rossi' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Torna all’elenco delle aziende' })).toHaveAttribute(
      'href',
      '/aziende',
    );
    expect(screen.getByText('Via Pratese 9, Prato (PO)')).toBeInTheDocument();
    expect(screen.getByText('2 stelle su 4')).toBeInTheDocument();
    expect(await screen.findByText('Nessun tirocinio valutato.')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /Salva/ })).not.toBeInTheDocument();
    expect(screen.queryByRole('textbox')).not.toBeInTheDocument();
    await expectNoAxeViolations();
  });

  it('azienda disattivata o inesistente: «Azienda non trovata»', async () => {
    vi.mocked(fetchCompanyDirectoryItem).mockRejectedValue(
      new AxiosError('Not Found', '404', undefined, undefined, {
        status: 404,
        statusText: 'Not Found',
        headers: {},
        config: { headers: new AxiosHeaders() },
        data: { status: 404 },
      }),
    );
    renderPage();
    expect(await screen.findByRole('heading', { name: 'Azienda non trovata' })).toBeInTheDocument();
  });

  it('«Chiedi di contattarla»: la richiesta nasce collegata all’azienda e si torna a «Le mie richieste» (US-507)', async () => {
    vi.mocked(fetchCompanyDirectoryItem).mockResolvedValue({ id: 'C1', name: 'Officina Rossi' });
    vi.mocked(createContactRequest).mockResolvedValue(
      contactRequest(6, { companyId: 'C1', companyName: 'Officina Rossi' }),
    );
    const user = userEvent.setup();
    const { router } = renderPage();
    await user.click(await screen.findByRole('button', { name: 'Chiedi di contattarla' }));
    await waitFor(() => expect(router.state.location.pathname).toBe('/richieste'));
    expect(router.state.location.state).toEqual({ sent: 6 });
    expect(vi.mocked(createContactRequest).mock.calls[0][0]).toEqual({ companyId: 'C1' });
  });
});
