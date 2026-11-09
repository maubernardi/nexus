import { screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AxiosError, AxiosHeaders } from 'axios';
import type { InitialEntry } from 'react-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import {
  deleteBeneficiaryCv,
  downloadBeneficiaryCv,
  fetchBeneficiary,
  uploadBeneficiaryCv,
  type Beneficiary,
} from '@/config/api/beneficiaryApi';
import { BeneficiaryPage } from '@/pages/BeneficiaryPage/BeneficiaryPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderRoutes } from '@/tests/renderWithProviders';

vi.mock('@/config/api/beneficiaryApi', () => ({
  fetchBeneficiary: vi.fn(),
  uploadBeneficiaryCv: vi.fn(),
  deleteBeneficiaryCv: vi.fn(),
  downloadBeneficiaryCv: vi.fn(),
  CV_MAX_BYTES: 5 * 1024 * 1024,
}));

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

  describe('curriculum (US-205)', () => {
    const pdf = new File(['%PDF-1.7'], 'CV Mario.pdf', { type: 'application/pdf' });

    it('il proprietario carica un PDF; un altro file è rifiutato prima di inviarlo', async () => {
      vi.mocked(fetchBeneficiary).mockResolvedValue(mario);
      vi.mocked(uploadBeneficiaryCv).mockResolvedValue({
        ...mario,
        cvFileName: 'CV Mario.pdf',
        cvSizeBytes: 245_000,
      });
      const user = userEvent.setup({ applyAccept: false });
      renderPage();

      const section = await screen.findByRole('region', { name: 'Curriculum' });
      expect(within(section).getByText('Nessun CV caricato.')).toBeInTheDocument();
      await expectNoAxeViolations();

      await user.upload(
        within(section).getByLabelText('Carica il CV'),
        new File(['x'], 'foto.png', { type: 'image/png' }),
      );
      await user.click(within(section).getByRole('button', { name: 'Carica' }));
      expect(await within(section).findByRole('alert')).toHaveTextContent('Il CV deve essere un file PDF.');
      expect(uploadBeneficiaryCv).not.toHaveBeenCalled();

      await user.upload(within(section).getByLabelText('Carica il CV'), pdf);
      await user.click(within(section).getByRole('button', { name: 'Carica' }));
      expect(uploadBeneficiaryCv).toHaveBeenCalledWith({ id: 'B1', file: pdf });
      expect(await within(section).findByRole('status')).toHaveTextContent('CV caricato.');
      expect(within(section).getByText('CV Mario.pdf (239 KB)')).toBeInTheDocument();
      expect(within(section).getByLabelText('Sostituisci il CV')).toBeInTheDocument();
    });

    it('chi non è proprietario lo scarica, senza caricarlo né toglierlo', async () => {
      vi.mocked(fetchBeneficiary).mockResolvedValue({
        ...mario,
        editable: false,
        cvFileName: 'CV Mario.pdf',
        cvSizeBytes: 2_500_000,
      });
      vi.mocked(downloadBeneficiaryCv).mockResolvedValue(new Blob(['%PDF-1.7'], { type: 'application/pdf' }));
      URL.createObjectURL = vi.fn(() => 'blob:cv');
      URL.revokeObjectURL = vi.fn();
      const user = userEvent.setup();
      renderPage();

      const section = await screen.findByRole('region', { name: 'Curriculum' });
      expect(within(section).getByText('CV Mario.pdf (2,4 MB)')).toBeInTheDocument();
      expect(within(section).queryByLabelText(/Carica il CV|Sostituisci il CV/)).not.toBeInTheDocument();
      expect(within(section).queryByRole('button', { name: 'Togli il CV' })).not.toBeInTheDocument();
      await user.click(within(section).getByRole('button', { name: 'Scarica il CV' }));
      expect(downloadBeneficiaryCv).toHaveBeenCalledWith('B1');
      expect(deleteBeneficiaryCv).not.toHaveBeenCalled();
      await expectNoAxeViolations();
    });
  });
});
