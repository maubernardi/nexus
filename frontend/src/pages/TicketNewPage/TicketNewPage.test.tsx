import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { fetchMyBeneficiaries } from '@/config/api/beneficiaryApi';
import { fetchJobCategories, fetchMyProjects } from '@/config/api/referenceApi';
import { fetchInternshipsToClose, submitTicket, type Ticket } from '@/config/api/ticketApi';
import { TicketNewPage } from '@/pages/TicketNewPage/TicketNewPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderRoutes } from '@/tests/renderWithProviders';

vi.mock('@/config/api/beneficiaryApi', () => ({ createBeneficiary: vi.fn(), fetchMyBeneficiaries: vi.fn() }));
vi.mock('@/config/api/referenceApi', () => ({
  fetchZones: vi.fn(),
  fetchJobCategories: vi.fn(),
  fetchMyProjects: vi.fn(),
  fetchCompetenceSites: vi.fn(async () => []),
  // tipi di tirocinio del progetto (US-605): uno solo, già scelto
  fetchProjectInternshipTypes: vi.fn(async () => [
    { id: 'IT2', code: 'EXTRACURRICULARE', name: 'Extracurriculare' },
  ]),
}));
vi.mock('@/config/api/ticketApi', () => ({
  submitTicket: vi.fn(),
  // tirocini finiti da chiudere (US-1203): di solito nessuno
  fetchInternshipsToClose: vi.fn(async () => []),
}));

const MARIO = { id: 'C1', firstName: 'Mario', lastName: 'Rossi', birthYear: 1998, residenceZoneName: 'Nord' };

describe('<TicketNewPage>', () => {
  beforeEach(() => {
    vi.mocked(fetchInternshipsToClose).mockResolvedValue([]);
    vi.mocked(fetchMyBeneficiaries).mockResolvedValue([MARIO, { ...MARIO, id: 'C2', firstName: 'Luca' }]);
    vi.mocked(fetchMyProjects).mockResolvedValue([{ id: 'P1', code: 'GOL', name: 'GOL' }]);
    vi.mocked(fetchJobCategories).mockResolvedValue([
      { id: 'J1', code: 'MAGAZZINIERE', name: 'Magazziniere' },
    ]);
    vi.mocked(submitTicket).mockResolvedValue({
      id: 'T1',
      number: 42,
      status: 'NUOVA',
      beneficiary: MARIO,
    } as Ticket);
  });

  it('preseleziona il beneficiario dall’indirizzo e alla conferma sposta il focus su numero e stato', async () => {
    const user = userEvent.setup();
    renderRoutes({
      routes: [
        {
          path: '/segnalazioni/nuova',
          element: (
            <main>
              <TicketNewPage />
            </main>
          ),
        },
      ],
      initialPath: '/segnalazioni/nuova?beneficiario=C1',
    });

    expect(await screen.findByLabelText(/^Beneficiario/)).toHaveValue('C1');
    await user.selectOptions(screen.getByLabelText(/^Mansione richiesta/), 'J1');
    await user.click(screen.getByRole('button', { name: 'Invia segnalazione' }));

    const heading = await screen.findByRole('heading', { name: 'Segnalazione n. 42 inviata' });
    await waitFor(() => expect(heading).toHaveFocus());
    expect(screen.getByText(/in stato «Nuova» ed è entrata nella coda del Call Center/)).toBeInTheDocument();
    await expectNoAxeViolations();

    // una nuova segnalazione riparte da zero, senza riprendere il beneficiario dall'indirizzo
    await user.click(screen.getByRole('button', { name: 'Invia un’altra segnalazione' }));
    expect(await screen.findByLabelText(/^Beneficiario/)).toHaveValue('');
  });

  it('con tirocini finiti da chiudere non mostra il modulo ma l’elenco, con i collegamenti', async () => {
    vi.mocked(fetchInternshipsToClose).mockResolvedValue([
      { ticketId: 'T12', ticketNumber: 12, endDate: '2026-11-20' },
      { ticketId: 'T15', ticketNumber: 15, endDate: '2026-11-21' },
    ]);
    renderRoutes({
      routes: [
        {
          path: '/segnalazioni/nuova',
          element: (
            <main>
              <TicketNewPage />
            </main>
          ),
        },
      ],
      initialPath: '/segnalazioni/nuova',
    });

    expect(
      await screen.findByRole('heading', { name: 'Prima chiudi i tirocini finiti' }),
    ).toBeInTheDocument();
    expect(screen.getAllByRole('link', { name: /^Segnalazione n\. / })).toHaveLength(2);
    expect(
      screen.getByRole('link', { name: 'Segnalazione n. 15, fine prevista il 21/11/2026' }),
    ).toHaveAttribute('href', '/segnalazioni/T15');
    expect(screen.queryByRole('button', { name: 'Invia segnalazione' })).not.toBeInTheDocument();
    await expectNoAxeViolations();
  });
});
