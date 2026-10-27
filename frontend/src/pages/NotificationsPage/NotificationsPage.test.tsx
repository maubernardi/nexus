import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import {
  fetchNotifications,
  markAllNotificationsRead,
  markNotificationRead,
  type Notifications,
} from '@/config/api/notificationApi';
import { NotificationsPage } from '@/pages/NotificationsPage/NotificationsPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderWithProviders } from '@/tests/renderWithProviders';

vi.mock('@/config/api/notificationApi', async () => ({
  NOTIFICATION_TYPES: ['CATEGORY_CHANGED', 'CATEGORY_APPROVED', 'PROPOSAL_SENT', 'PROPOSAL_ACCEPTED'],
  fetchNotifications: vi.fn(),
  markNotificationRead: vi.fn().mockResolvedValue(undefined),
  markAllNotificationsRead: vi.fn().mockResolvedValue(undefined),
}));

const data: Notifications = {
  unread: 1,
  items: [
    {
      id: 'N2',
      type: 'PROPOSAL_SENT',
      ticketId: 'T9',
      ticketNumber: 9,
      createdAt: '2026-10-27T09:00:00Z',
      read: false,
    },
    {
      id: 'N1',
      type: 'FUTURO',
      ticketId: 'T3',
      ticketNumber: 3,
      createdAt: '2026-10-26T09:00:00Z',
      read: true,
    },
  ],
};

const renderPage = () =>
  renderWithProviders(
    <main>
      <NotificationsPage />
    </main>,
  );

describe('<NotificationsPage>', () => {
  beforeEach(() => {
    vi.mocked(fetchNotifications).mockResolvedValue(data);
    vi.mocked(markNotificationRead).mockClear();
    vi.mocked(markAllNotificationsRead).mockClear();
  });

  it('elenca le notifiche con stato in testo, link alla segnalazione e testo generico per i tipi nuovi', async () => {
    renderPage();

    const items = await screen.findAllByRole('listitem');
    expect(items[0]).toHaveTextContent('Nuova: C’è una proposta di tirocinio per la segnalazione n. 9');
    expect(within(items[0]).getByRole('link', { name: 'Apri la segnalazione n. 9' })).toHaveAttribute(
      'href',
      '/segnalazioni/T9',
    );
    expect(items[1]).toHaveTextContent('Aggiornamento sulla segnalazione n. 3.');
    expect(within(items[1]).queryByRole('button')).not.toBeInTheDocument();
    expect(screen.getByText('1 notifica da leggere.')).toBeInTheDocument();
    await expectNoAxeViolations();
  });

  it('segna come letta una notifica o tutte', async () => {
    const user = userEvent.setup();
    renderPage();

    await user.click(await screen.findByRole('button', { name: /^Segna come letta C’è una proposta/ }));
    expect(markNotificationRead).toHaveBeenCalledWith('N2', expect.anything());

    await user.click(screen.getByRole('button', { name: 'Segna tutte come lette' }));
    const outcome = await screen.findByText('Tutte le notifiche sono state segnate come lette.');
    await waitFor(() => expect(outcome).toHaveFocus());
    expect(markAllNotificationsRead).toHaveBeenCalled();
  });
});
