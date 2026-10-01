import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AxiosError, AxiosHeaders } from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { fetchCompany, setCompanyActive, updateCompany, type Company } from '@/config/api/companyApi';
import { CompanyEditPage } from '@/pages/CompanyEditPage/CompanyEditPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderRoutes } from '@/tests/renderWithProviders';

vi.mock('@/config/api/companyApi', () => ({
  searchCompanies: vi.fn(),
  fetchCompany: vi.fn(),
  createCompany: vi.fn(),
  updateCompany: vi.fn(),
  setCompanyActive: vi.fn(),
}));

const rossi: Company = {
  id: 'C1',
  name: 'Officina Rossi',
  vatCode: '12345678901',
  legalAddress: 'Via Roma 1',
  contactPerson: 'Paola Neri',
  phone: null,
  email: null,
  active: true,
  version: 3,
  updatedAt: '2026-10-02T08:00:00Z',
};

const renderPage = () =>
  renderRoutes({
    routes: [
      {
        path: '/aziende/:id',
        element: (
          <main>
            <CompanyEditPage />
          </main>
        ),
      },
    ],
    initialPath: '/aziende/C1',
  });

describe('<CompanyEditPage>', () => {
  beforeEach(() => {
    vi.mocked(fetchCompany).mockResolvedValue(rossi);
    vi.mocked(updateCompany).mockReset();
    vi.mocked(setCompanyActive).mockReset();
  });

  it('modifica con la versione vista e annuncia il salvataggio', async () => {
    const user = userEvent.setup();
    vi.mocked(updateCompany).mockResolvedValue({ ...rossi, name: 'Officina Rossi S.r.l.', version: 4 });
    renderPage();

    const name = await screen.findByLabelText(/^Ragione sociale/);
    expect(name).toHaveValue('Officina Rossi');
    await user.clear(name);
    await user.type(name, 'Officina Rossi S.r.l.');
    await user.click(screen.getByRole('button', { name: 'Salva modifiche' }));

    const outcome = await screen.findByText('Modifiche salvate.');
    await waitFor(() => expect(outcome).toHaveFocus());
    expect(vi.mocked(updateCompany).mock.calls[0][0]).toMatchObject({
      id: 'C1',
      payload: { name: 'Officina Rossi S.r.l.', version: 3 },
    });
    await expectNoAxeViolations();
  });

  it('disattiva e, se un collega l’ha cambiata, lo spiega', async () => {
    const user = userEvent.setup();
    vi.mocked(setCompanyActive)
      .mockResolvedValueOnce({ ...rossi, active: false, version: 4 })
      .mockRejectedValueOnce(
        new AxiosError('Conflict', '409', undefined, undefined, {
          status: 409,
          statusText: 'Conflict',
          headers: {},
          config: { headers: new AxiosHeaders() },
          data: { status: 409, message: 'conflitto' },
        }),
      );
    renderPage();

    await user.click(await screen.findByRole('button', { name: 'Disattiva azienda' }));
    expect(await screen.findByText('Azienda disattivata.')).toHaveFocus();
    expect(setCompanyActive).toHaveBeenCalledWith({ id: 'C1', version: 3, active: false }, expect.anything());

    await user.click(await screen.findByRole('button', { name: 'Riattiva azienda' }));
    expect(await screen.findByText(/modificata da un collega/)).toHaveFocus();
  });
});
