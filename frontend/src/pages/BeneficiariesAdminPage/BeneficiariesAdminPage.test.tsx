import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';

import { fetchAllBeneficiaries } from '@/config/api/beneficiaryApi';
import { BeneficiariesAdminPage } from '@/pages/BeneficiariesAdminPage/BeneficiariesAdminPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderRoutes } from '@/tests/renderWithProviders';

vi.mock('@/config/api/beneficiaryApi', () => ({
  fetchAllBeneficiaries: vi.fn(),
  anonymizeBeneficiary: vi.fn(),
  keepBeneficiary: vi.fn(),
}));

describe('<BeneficiariesAdminPage>', () => {
  it('beneficiari di tutti, con il Tutor, gli stati e il filtro', async () => {
    vi.mocked(fetchAllBeneficiaries).mockResolvedValue([
      {
        id: 'B1',
        firstName: 'Mario',
        lastName: 'Rossi',
        birthYear: 1998,
        residenceZoneName: 'Novoli',
        ownerTutorName: 'Tutor Uno',
        retention: 'TO_ANONYMIZE',
        anonymizationDueSince: '2026-11-07T01:30:00Z',
      },
      {
        id: 'B2',
        firstName: 'Anna',
        lastName: 'Bianchi',
        birthYear: 1990,
        residenceZoneName: 'Rifredi',
        ownerTutorName: 'Tutor Due',
        retention: 'KEPT',
        keptReason: 'Contenzioso in corso',
      },
    ]);
    const user = userEvent.setup();
    renderRoutes({
      routes: [
        {
          path: '/beneficiari/tutti',
          element: (
            <main>
              <BeneficiariesAdminPage />
            </main>
          ),
        },
      ],
      initialPath: '/beneficiari/tutti',
    });

    expect(await screen.findByRole('heading', { level: 1, name: 'Beneficiari' })).toBeInTheDocument();
    expect(await screen.findByRole('cell', { name: 'Tutor Uno' })).toBeInTheDocument();
    expect(screen.getByText('Dal 07/11/26')).toBeInTheDocument();
    expect(screen.getByText('Motivo: Contenzioso in corso')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Anonimizza Rossi Mario' })).toBeInTheDocument();
    await expectNoAxeViolations();

    await user.click(screen.getByRole('checkbox', { name: 'Solo da anonimizzare (1)' }));
    expect(screen.queryByRole('link', { name: 'Bianchi Anna' })).not.toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Rossi Mario' })).toHaveAttribute('href', '/beneficiari/B1');
  });
});
