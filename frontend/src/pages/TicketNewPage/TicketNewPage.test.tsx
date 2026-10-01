import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { fetchMyCandidates } from '@/config/api/candidateApi';
import { fetchJobCategories, fetchMyProjects } from '@/config/api/referenceApi';
import { submitTicket, type Ticket } from '@/config/api/ticketApi';
import { TicketNewPage } from '@/pages/TicketNewPage/TicketNewPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderRoutes } from '@/tests/renderWithProviders';

vi.mock('@/config/api/candidateApi', () => ({ createCandidate: vi.fn(), fetchMyCandidates: vi.fn() }));
vi.mock('@/config/api/referenceApi', () => ({
  fetchZones: vi.fn(),
  fetchJobCategories: vi.fn(),
  fetchMyProjects: vi.fn(),
}));
vi.mock('@/config/api/ticketApi', () => ({ submitTicket: vi.fn() }));

const MARIO = { id: 'C1', firstName: 'Mario', lastName: 'Rossi', birthYear: 1998, residenceZoneName: 'Nord' };

describe('<TicketNewPage>', () => {
  beforeEach(() => {
    vi.mocked(fetchMyCandidates).mockResolvedValue([MARIO, { ...MARIO, id: 'C2', firstName: 'Luca' }]);
    vi.mocked(fetchMyProjects).mockResolvedValue([{ id: 'P1', code: 'GOL', name: 'GOL' }]);
    vi.mocked(fetchJobCategories).mockResolvedValue([
      { id: 'J1', code: 'MAGAZZINIERE', name: 'Magazziniere' },
    ]);
    vi.mocked(submitTicket).mockResolvedValue({
      id: 'T1',
      number: 42,
      status: 'NUOVA',
      candidate: MARIO,
    } as Ticket);
  });

  it('preseleziona il candidato dall’indirizzo e alla conferma sposta il focus su numero e stato', async () => {
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
      initialPath: '/segnalazioni/nuova?candidato=C1',
    });

    expect(await screen.findByLabelText(/^Candidato/)).toHaveValue('C1');
    await user.selectOptions(screen.getByLabelText(/^Mansione richiesta/), 'J1');
    await user.click(screen.getByRole('button', { name: 'Invia segnalazione' }));

    const heading = await screen.findByRole('heading', { name: 'Segnalazione n. 42 inviata' });
    await waitFor(() => expect(heading).toHaveFocus());
    expect(screen.getByText(/in stato «Nuova» ed è entrata nella coda del Call Center/)).toBeInTheDocument();
    await expectNoAxeViolations();

    // una nuova segnalazione riparte da zero, senza riprendere il candidato dall'indirizzo
    await user.click(screen.getByRole('button', { name: 'Invia un’altra segnalazione' }));
    expect(await screen.findByLabelText(/^Candidato/)).toHaveValue('');
  });
});
