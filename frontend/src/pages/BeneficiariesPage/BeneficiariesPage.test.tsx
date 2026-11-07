import { screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { anonymizeBeneficiary, fetchMyBeneficiaries, keepBeneficiary } from '@/config/api/beneficiaryApi';
import { BeneficiariesPage } from '@/pages/BeneficiariesPage/BeneficiariesPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderRoutes } from '@/tests/renderWithProviders';

vi.mock('@/config/api/beneficiaryApi', () => ({
  fetchMyBeneficiaries: vi.fn(),
  anonymizeBeneficiary: vi.fn(),
  keepBeneficiary: vi.fn(),
}));

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

  describe('conservazione dei dati (US-1303)', () => {
    beforeEach(() => {
      vi.mocked(anonymizeBeneficiary).mockReset();
      vi.mocked(keepBeneficiary).mockReset();
      vi.mocked(fetchMyBeneficiaries).mockResolvedValue([
        {
          id: 'B1',
          firstName: 'Mario',
          lastName: 'Rossi',
          birthYear: 1998,
          residenceZoneName: 'Novoli',
          retention: 'TO_ANONYMIZE',
        },
        {
          id: 'B2',
          firstName: 'L',
          lastName: 'V',
          birthYear: 2001,
          residenceZoneName: 'Fiesole',
          retention: 'ANONYMIZED',
        },
        { id: 'B3', firstName: 'Anna', lastName: 'Bianchi', birthYear: 1990, residenceZoneName: 'Rifredi' },
      ]);
    });

    it('da anonimizzare evidenziati, con il filtro e le due decisioni', async () => {
      const user = userEvent.setup();
      renderPage();

      const row = (await screen.findByRole('link', { name: 'Rossi Mario' })).closest('tr') as HTMLElement;
      expect(within(row).getByText('Da anonimizzare')).toBeInTheDocument();
      expect(screen.getByText('Anonimizzato')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: 'Anonimizza Rossi Mario' })).toBeInTheDocument();
      expect(screen.queryByRole('button', { name: /Anonimizza Bianchi/ })).not.toBeInTheDocument();
      await expectNoAxeViolations();

      await user.click(screen.getByRole('checkbox', { name: 'Solo da anonimizzare (1)' }));
      expect(screen.getByRole('status')).toHaveTextContent('1 beneficiario');
      expect(screen.queryByRole('link', { name: 'Bianchi Anna' })).not.toBeInTheDocument();

      // anonimizzare chiede conferma
      await user.click(screen.getByRole('button', { name: 'Anonimizza Rossi Mario' }));
      const confirm = await screen.findByRole('dialog', { name: 'Anonimizzare Rossi Mario?' });
      expect(confirm).toHaveTextContent('restano le iniziali');
      await expectNoAxeViolations();
      vi.mocked(anonymizeBeneficiary).mockResolvedValueOnce();
      await user.click(within(confirm).getByRole('button', { name: 'Anonimizza definitivamente' }));
      expect(anonymizeBeneficiary).toHaveBeenCalledWith('B1');
      expect(await screen.findByText('Rossi Mario è stato anonimizzato.')).toBeInTheDocument();
    });

    it('tenere i dati vuole il motivo', async () => {
      const user = userEvent.setup();
      renderPage();

      await user.click(await screen.findByRole('button', { name: 'Tieni i dati di Rossi Mario' }));
      const dialog = await screen.findByRole('dialog', { name: 'Tenere i dati di Rossi Mario?' });
      await user.click(within(dialog).getByRole('button', { name: 'Tieni i dati' }));
      expect(within(dialog).getByRole('alert')).toHaveTextContent('Scrivi il motivo');
      expect(keepBeneficiary).not.toHaveBeenCalled();
      await expectNoAxeViolations();

      vi.mocked(keepBeneficiary).mockResolvedValueOnce();
      await user.type(within(dialog).getByLabelText('Motivo'), ' Contenzioso in corso ');
      await user.click(within(dialog).getByRole('button', { name: 'Tieni i dati' }));
      expect(keepBeneficiary).toHaveBeenCalledWith({ id: 'B1', reason: 'Contenzioso in corso' });
      expect(await screen.findByText('I dati di Rossi Mario si tengono per sempre.')).toBeInTheDocument();
    });
  });
});
