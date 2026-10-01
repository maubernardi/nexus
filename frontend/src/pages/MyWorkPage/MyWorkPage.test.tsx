import { screen, within } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { fetchAssignedToMe, type QueueItem } from '@/config/api/ticketApi';
import { MyWorkPage } from '@/pages/MyWorkPage/MyWorkPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderWithProviders } from '@/tests/renderWithProviders';

vi.mock('@/config/api/ticketApi', () => ({
  submitTicket: vi.fn(),
  fetchQueue: vi.fn(),
  takeCharge: vi.fn(),
  fetchAssignedToMe: vi.fn(),
}));

const mine: QueueItem = {
  id: 'T5',
  number: 5,
  type: 'NORMAL',
  status: 'IN_LAVORAZIONE',
  fastTrack: false,
  version: 1,
  beneficiary: {
    id: 'B5',
    firstName: 'Anna',
    lastName: 'Bianchi',
    birthYear: 1999,
    residenceZoneName: 'Novoli',
  },
  project: { id: 'P1', code: 'GOL', name: 'GOL' },
  requestedJobCategory: { id: 'J1', code: 'MAG', name: 'Magazziniere' },
  tutorName: 'Tutor Uno',
  createdAt: '2026-10-01T08:15:00Z',
};

const renderPage = () =>
  renderWithProviders(
    <main>
      <MyWorkPage />
    </main>,
  );

describe('<MyWorkPage>', () => {
  beforeEach(() => vi.mocked(fetchAssignedToMe).mockReset());

  it('elenca le segnalazioni in carico, senza azioni di presa in carico', async () => {
    vi.mocked(fetchAssignedToMe).mockResolvedValue([mine]);
    renderPage();

    const table = await screen.findByRole('table', { name: /assegnate a te/ });
    expect(within(table).getByText('Bianchi Anna')).toBeInTheDocument();
    expect(within(table).getByText('In lavorazione')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /Prendi in carico/ })).not.toBeInTheDocument();
    expect(screen.getByRole('status')).toHaveTextContent('1 segnalazione in carico');
    await expectNoAxeViolations();
  });

  it('senza segnalazioni rimanda alla coda', async () => {
    vi.mocked(fetchAssignedToMe).mockResolvedValue([]);
    renderPage();

    expect(
      await screen.findByText('Non hai segnalazioni in carico. Prendine una dalla coda.'),
    ).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Vai alla coda' })).toHaveAttribute('href', '/coda');
  });
});
