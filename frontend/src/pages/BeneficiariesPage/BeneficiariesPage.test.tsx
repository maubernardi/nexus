import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { fetchMyBeneficiaries } from '@/config/api/beneficiaryApi';
import { BeneficiariesPage } from '@/pages/BeneficiariesPage/BeneficiariesPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderRoutes } from '@/tests/renderWithProviders';

vi.mock('@/config/api/beneficiaryApi', () => ({ fetchMyBeneficiaries: vi.fn() }));

const renderPage = (initialPath = '/beneficiari') =>
  renderRoutes({
    routes: [
      {
        path: '/beneficiari',
        element: (
          <main>
            <BeneficiariesPage />
          </main>
        ),
      },
    ],
    initialPath,
  });

describe('<BeneficiariesPage>', () => {
  beforeEach(() => {
    vi.mocked(fetchMyBeneficiaries).mockResolvedValue([
      {
        id: 'B1',
        firstName: 'Mario',
        lastName: 'Rossi',
        birthYear: 1998,
        residenceZoneName: 'Novoli',
        openTicketNumber: 12,
      },
      {
        id: 'B2',
        firstName: 'Lucía',
        lastName: 'Verdi',
        birthYear: 2001,
        residenceZoneName: 'Fiesole',
        openTicketNumber: null,
      },
    ]);
  });

  it('elenca i beneficiari con link alla scheda e segnalazione aperta', async () => {
    renderPage();

    expect(await screen.findByRole('link', { name: 'Rossi Mario' })).toHaveAttribute(
      'href',
      '/beneficiari/B1',
    );
    expect(screen.getByRole('status')).toHaveTextContent('2 beneficiari');
    expect(screen.getByRole('cell', { name: 'n. 12' })).toBeInTheDocument();
    expect(screen.getByRole('cell', { name: 'Nessuna' })).toBeInTheDocument();
    await expectNoAxeViolations();
  });

  it('cerca per nome e cognome in qualunque ordine, senza accenti', async () => {
    const user = userEvent.setup();
    renderPage();
    await screen.findByRole('link', { name: 'Rossi Mario' });

    await user.type(screen.getByLabelText('Cerca per nome o cognome'), 'verdi lucia');
    await user.click(screen.getByRole('button', { name: 'Cerca' }));

    expect(await screen.findByRole('status')).toHaveTextContent('1 beneficiario');
    expect(screen.getByRole('link', { name: 'Verdi Lucía' })).toBeInTheDocument();
    expect(screen.queryByRole('link', { name: 'Rossi Mario' })).not.toBeInTheDocument();
  });

  it('distingue l’elenco vuoto dalla ricerca senza risultati', async () => {
    renderPage('/beneficiari?q=bianchi');
    expect(await screen.findByText('Nessun beneficiario corrisponde alla ricerca.')).toBeInTheDocument();
  });
});
