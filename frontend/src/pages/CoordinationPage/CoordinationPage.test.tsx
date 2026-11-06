import { screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import type { TicketListItem } from '@/config/api/ticketApi';
import { fetchCoordination, transferTickets } from '@/config/api/ticketTransferApi';
import { CoordinationPage } from '@/pages/CoordinationPage/CoordinationPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderRoutes } from '@/tests/renderWithProviders';

vi.mock('@/config/api/ticketTransferApi', () => ({
  fetchOpenTickets: vi.fn(),
  fetchCoordination: vi.fn(),
  transferTickets: vi.fn(),
}));

const ticket = (id: string, number: number, lastName: string, tutorName: string): TicketListItem => ({
  id,
  number,
  type: 'NORMAL',
  status: 'IN_LAVORAZIONE',
  fastTrack: false,
  version: 0,
  beneficiary: { id: `B${id}`, firstName: 'Anna', lastName, birthYear: 1990, residenceZoneName: 'Novoli' },
  project: { id: 'P1', code: 'GOL', name: 'GOL' },
  requestedJobCategory: null,
  requestedJobFreeText: null,
  createdAt: '2026-11-01T09:00:00Z',
  tutorName,
  assignedOperatorName: null,
  timerDeadline: null,
  timerDueSoon: false,
});

const renderPage = () =>
  renderRoutes({
    routes: [
      {
        path: '/coordinamento',
        element: (
          <main>
            <CoordinationPage />
          </main>
        ),
      },
    ],
    initialPath: '/coordinamento',
  });

describe('<CoordinationPage>', () => {
  beforeEach(() => {
    vi.mocked(transferTickets).mockReset();
    vi.mocked(fetchCoordination).mockResolvedValue([
      {
        project: { id: 'P1', code: 'GOL', name: 'GOL' },
        tickets: [ticket('T1', 12, 'Bianchi', 'Mario Rossi'), ticket('T2', 15, 'Neri', 'Luca Verdi')],
        targets: [
          { id: 'U1', name: 'Mario Rossi' },
          { id: 'U2', name: 'Luca Verdi' },
        ],
      },
      { project: { id: 'P2', code: 'POLIS', name: 'POLIS' }, tickets: [], targets: [] },
    ]);
  });

  it('segnalazioni dei progetti coordinati con il loro Tutor; ne sposta una scelta', async () => {
    const user = userEvent.setup();
    renderPage();

    expect(await screen.findByRole('heading', { level: 1, name: 'Coordinamento' })).toBeInTheDocument();
    const gol = await screen.findByRole('region', { name: 'GOL: 2 segnalazioni aperte' });
    // nessuna parte già scelta: le segnalazioni sono di Tutor diversi
    const first = within(gol).getByRole('checkbox', { name: /n\. 12 · Bianchi Anna .* Tutor: Mario Rossi/ });
    expect(first).not.toBeChecked();
    expect(screen.getByText('Nessuna segnalazione aperta in POLIS.')).toBeInTheDocument();
    await expectNoAxeViolations();

    await user.click(first);
    await user.selectOptions(within(gol).getByLabelText('Tutor che le riceve'), 'U2');
    vi.mocked(transferTickets).mockResolvedValueOnce({ moved: 1, tutorName: 'Luca Verdi' });
    await user.click(within(gol).getByRole('button', { name: 'Sposta 1 segnalazione' }));

    expect(transferTickets).toHaveBeenCalledWith({ ticketIds: ['T1'], tutorId: 'U2' });
    expect(await screen.findByRole('status')).toHaveTextContent('1 segnalazione spostata a Luca Verdi');
  });

  it('chi non coordina nessun progetto', async () => {
    vi.mocked(fetchCoordination).mockResolvedValue([]);
    renderPage();
    expect(await screen.findByText(/Non coordini nessun progetto/)).toBeInTheDocument();
    await expectNoAxeViolations();
  });
});
