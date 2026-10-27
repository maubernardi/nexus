import { act, screen, waitFor } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';

import { fetchUnreadCount } from '@/config/api/notificationApi';
import { NotificationsBell } from '@/molecules/NotificationsBell/NotificationsBell';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderWithProviders } from '@/tests/renderWithProviders';

vi.mock('@/config/api/notificationApi', () => ({ fetchUnreadCount: vi.fn() }));

describe('<NotificationsBell>', () => {
  it('mostra il numero di non lette nel nome accessibile e porta alla pagina', async () => {
    vi.mocked(fetchUnreadCount).mockResolvedValue(3);
    renderWithProviders(
      <nav aria-label="Prova">
        <NotificationsBell />
      </nav>,
    );

    const link = await screen.findByRole('link', { name: 'Notifiche, 3 da leggere' });
    expect(link).toHaveAttribute('href', '/notifiche');
    // il primo valore non si annuncia: solo gli aumenti
    expect(screen.getByRole('status')).toHaveTextContent('');
    await expectNoAxeViolations();
  });

  it('annuncia le notifiche nuove quando il numero cresce', async () => {
    vi.useFakeTimers({ shouldAdvanceTime: true });
    vi.mocked(fetchUnreadCount).mockResolvedValueOnce(0).mockResolvedValue(2);
    renderWithProviders(<NotificationsBell />);
    await screen.findByRole('link', { name: 'Notifiche, nessuna da leggere' });

    await act(async () => {
      await vi.advanceTimersByTimeAsync(61_000);
    });
    await waitFor(() => expect(screen.getByRole('status')).toHaveTextContent('Hai 2 notifiche da leggere'));
    expect(screen.getByRole('link', { name: 'Notifiche, 2 da leggere' })).toBeInTheDocument();
    vi.useRealTimers();
  });
});
