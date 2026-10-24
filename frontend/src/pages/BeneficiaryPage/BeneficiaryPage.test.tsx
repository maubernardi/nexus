import { screen } from '@testing-library/react';
import { AxiosError, AxiosHeaders } from 'axios';
import type { InitialEntry } from 'react-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { fetchBeneficiary, type Beneficiary } from '@/config/api/beneficiaryApi';
import { BeneficiaryPage } from '@/pages/BeneficiaryPage/BeneficiaryPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderRoutes } from '@/tests/renderWithProviders';

vi.mock('@/config/api/beneficiaryApi', () => ({ fetchBeneficiary: vi.fn() }));

const mario: Beneficiary = {
  id: 'B1',
  firstName: 'Mario',
  lastName: 'Rossi',
  birthYear: 1998,
  gender: 'M',
  nationality: 'IT',
  citizenship: null,
  residenceZone: { id: 'Z1', code: 'NOVOLI', name: 'Firenze Novoli' },
  hasDrivingLicense: true,
  licenseTypes: ['B'],
  hasVehicle: false,
  transportMode: null,
  hasLaw68: true,
  educationLevel: null,
  constraints: null,
  languages: [{ language: 'en', level: 'B1' }],
  createdAt: '2026-10-01T08:00:00Z',
  ownerTutorName: 'Tutor Uno',
  editable: true,
};

const renderPage = (initialPath: InitialEntry = '/beneficiari/B1') =>
  renderRoutes({
    routes: [
      {
        path: '/beneficiari/:id',
        element: (
          <main>
            <BeneficiaryPage />
          </main>
        ),
      },
    ],
    initialPath,
  });

describe('<BeneficiaryPage>', () => {
  beforeEach(() => {
    vi.mocked(fetchBeneficiary).mockClear();
  });

  it('mostra i dati in testo e, al proprietario, il link di modifica', async () => {
    vi.mocked(fetchBeneficiary).mockResolvedValue(mario);
    renderPage();

    expect(await screen.findByRole('heading', { level: 1, name: 'Mario Rossi' })).toBeInTheDocument();
    expect(screen.getByText('Inserito da Tutor Uno')).toBeInTheDocument();
    expect(screen.getByText('Italia')).toBeInTheDocument();
    expect(screen.getByText('inglese (B1 – Intermedio)', { exact: false })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Modifica' })).toHaveAttribute(
      'href',
      '/beneficiari/B1/modifica',
    );
    await expectNoAxeViolations();
  });

  it('a chi non è proprietario mostra i dati senza modifica', async () => {
    vi.mocked(fetchBeneficiary).mockResolvedValue({ ...mario, editable: false });
    renderPage();

    await screen.findByRole('heading', { level: 1, name: 'Mario Rossi' });
    expect(screen.queryByRole('link', { name: 'Modifica' })).not.toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Torna alla Home' })).toBeInTheDocument();
  });

  it('arrivando da una segnalazione, il ritorno riporta alla segnalazione', async () => {
    vi.mocked(fetchBeneficiary).mockResolvedValue({ ...mario, editable: false });
    renderPage({ pathname: '/beneficiari/B1', state: { fromTicket: { id: 'T9', number: 24 } } });

    expect(await screen.findByRole('link', { name: 'Torna alla segnalazione n. 24' })).toHaveAttribute(
      'href',
      '/segnalazioni/T9',
    );
  });

  it('beneficiario non visibile: stesso messaggio di inesistente', async () => {
    const notFound = new AxiosError('Not Found', '404', undefined, undefined, {
      status: 404,
      statusText: 'Not Found',
      headers: {},
      config: { headers: new AxiosHeaders() },
      data: { status: 404, message: 'Beneficiario non trovato' },
    });
    vi.mocked(fetchBeneficiary).mockImplementation(() => Promise.reject(notFound));
    renderPage();

    expect(
      await screen.findByRole('heading', { level: 1, name: 'Beneficiario non trovato' }),
    ).toBeInTheDocument();
  });
});
