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
      // il testo lo manda il backend: quello dell'ADMIN o il predefinito (US-1204)
      text: 'C’è una proposta di tirocinio per la segnalazione n. 9: valutala e accettala.',
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

  it('elenca le notifiche con il testo del backend, stato in testo, link alla segnalazione e testo generico se manca', async () => {
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

  it('proposta di nave scuola: il link porta alla scheda dell’azienda (US-515)', async () => {
    vi.mocked(fetchNotifications).mockResolvedValue({
      unread: 1,
      items: [
        {
          id: 'N5',
          type: 'TRAINING_SHIP_PROPOSED',
          ticketId: 'T9',
          ticketNumber: 9,
          companyId: 'C7',
          createdAt: '2026-11-26T09:00:00Z',
          read: false,
          text: 'Nel report di restituzione della segnalazione n. 9 il Tutor propone l’azienda del tirocinio come nave scuola: decidi tu dalla scheda dell’azienda.',
        },
      ],
    });
    renderPage();

    const [item] = await screen.findAllByRole('listitem');
    expect(
      within(item).getByRole('link', { name: 'Apri la scheda dell’azienda della segnalazione n. 9' }),
    ).toHaveAttribute('href', '/aziende/C7');
    expect(within(item).queryByRole('link', { name: /Apri la segnalazione/ })).not.toBeInTheDocument();
    await expectNoAxeViolations();
  });

  it('chiusura della richiesta di contatto: il link porta alla richiesta (US-512)', async () => {
    vi.mocked(fetchNotifications).mockResolvedValue({
      unread: 1,
      items: [
        {
          id: 'N6',
          type: 'CONTACT_REQUEST_CLOSED',
          ticketId: null,
          ticketNumber: null,
          contactRequestId: 'R3',
          contactRequestNumber: 3,
          createdAt: '2026-11-27T09:00:00Z',
          read: false,
          text: 'Il Call Center ha chiuso la tua richiesta di contatto n. 3: Non interessata. Nota: Hanno già tirocinanti.',
        },
      ],
    });
    renderPage();
    const [item] = await screen.findAllByRole('listitem');
    expect(within(item).getByRole('link', { name: 'Apri la richiesta di contatto n. 3' })).toHaveAttribute(
      'href',
      '/richieste/R3',
    );
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
