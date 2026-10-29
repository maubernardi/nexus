import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { fetchBeneficiary, updateBeneficiary, type Beneficiary } from '@/config/api/beneficiaryApi';
import { fetchZones } from '@/config/api/referenceApi';
import { BeneficiaryEditPage } from '@/pages/BeneficiaryEditPage/BeneficiaryEditPage';
import { BeneficiaryPage } from '@/pages/BeneficiaryPage/BeneficiaryPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderRoutes } from '@/tests/renderWithProviders';

vi.mock('@/config/api/referenceApi', () => ({ fetchZones: vi.fn(), fetchJobCategories: vi.fn() }));
vi.mock('@/config/api/beneficiaryApi', () => ({
  fetchBeneficiary: vi.fn(),
  updateBeneficiary: vi.fn(),
  createBeneficiary: vi.fn(),
  fetchMyBeneficiaries: vi.fn(),
}));

const mario: Beneficiary = {
  id: 'B1',
  firstName: 'Mario',
  lastName: 'Rossi',
  birthYear: 1998,
  gender: 'M',
  nationality: 'IT',
  citizenship: 'IT',
  // zona nel frattempo disattivata: non è tra quelle attive ma resta selezionata
  residenceZone: { id: 'Z9', code: 'VECCHIA', name: 'Zona dismessa' },
  hasDrivingLicense: true,
  licenseTypes: ['B'],
  hasVehicle: true,
  transportMode: 'AUTO_PROPRIA',
  hasLaw68: false,
  educationLevel: 'DIPLOMA',
  constraints: null,
  languages: [{ language: 'en', level: 'B1' }],
  createdAt: '2026-10-01T08:00:00Z',
  ownerTutorName: 'Tutor Uno',
  editable: true,
};

const renderPage = () =>
  renderRoutes({
    routes: [
      {
        path: '/beneficiari/:id/modifica',
        element: (
          <main>
            <BeneficiaryEditPage />
          </main>
        ),
      },
      {
        path: '/beneficiari/:id',
        element: (
          <main>
            <BeneficiaryPage />
          </main>
        ),
      },
    ],
    initialPath: '/beneficiari/B1/modifica',
  });

describe('<BeneficiaryEditPage>', () => {
  beforeEach(() => {
    vi.mocked(fetchZones).mockResolvedValue([{ id: 'Z1', code: 'NOVOLI', name: 'Firenze Novoli' }]);
    vi.mocked(fetchBeneficiary).mockResolvedValue(mario);
    vi.mocked(updateBeneficiary).mockReset();
  });

  it('parte dai dati attuali, salva e torna alla scheda annunciando l’esito', async () => {
    const user = userEvent.setup();
    vi.mocked(updateBeneficiary).mockResolvedValue({ ...mario, firstName: 'Luigi' });
    renderPage();

    const firstName = await screen.findByLabelText(/^Nome/);
    expect(firstName).toHaveValue('Mario');
    expect(screen.getByLabelText(/^Zona di residenza/)).toHaveValue('Z9');
    expect(screen.getByLabelText(/^Livello lingua 1/)).toHaveValue('B1');
    await expectNoAxeViolations();

    await user.clear(firstName);
    await user.type(firstName, 'Luigi');
    await user.click(screen.getByRole('button', { name: 'Salva modifiche' }));

    const outcome = await screen.findByText('Modifiche salvate.');
    await waitFor(() => expect(outcome).toHaveFocus());
    expect(vi.mocked(updateBeneficiary).mock.calls[0][0]).toMatchObject({
      id: 'B1',
      payload: { firstName: 'Luigi', residenceZoneId: 'Z9', languages: [{ language: 'en', level: 'B1' }] },
    });
    // axe su un modulo con centinaia di elementi: con la suite in parallelo supera i 15 s predefiniti (azione A13)
  }, 30_000);

  it('chi non è proprietario non vede il modulo', async () => {
    vi.mocked(fetchBeneficiary).mockResolvedValue({ ...mario, editable: false });
    renderPage();

    expect(
      await screen.findByText('Solo il Tutor che ha inserito il beneficiario può modificarlo.'),
    ).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Salva modifiche' })).not.toBeInTheDocument();
  });
});
