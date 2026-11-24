import { act, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { getSessionExpiresAt, reloginNow } from '@/config/auth/authService';
import { SESSION_CHECK_MS, SessionExpiryBanner } from '@/molecules/SessionExpiryBanner/SessionExpiryBanner';
import { expectNoAxeViolations } from '@/tests/axe';

vi.mock('@/config/auth/authService', () => ({
  getSessionExpiresAt: vi.fn(),
  reloginNow: vi.fn(),
}));

// 24/11/2026 alle 9:00 a Roma (8:00 UTC)
const NOW = Date.UTC(2026, 10, 24, 8, 0, 0);
const minutes = (m: number): number => NOW + m * 60_000;

describe('<SessionExpiryBanner>', () => {
  beforeEach(() => {
    // solo setInterval e Date: i setTimeout restano veri per axe e userEvent
    vi.useFakeTimers({ now: NOW, toFake: ['setInterval', 'clearInterval', 'Date'] });
    vi.mocked(reloginNow).mockResolvedValue();
  });

  afterEach(() => {
    vi.useRealTimers();
    vi.mocked(getSessionExpiresAt).mockReset();
    vi.mocked(reloginNow).mockReset();
  });

  it('senza Keycloak non mostra nulla', () => {
    vi.mocked(getSessionExpiresAt).mockReturnValue(null);
    render(<SessionExpiryBanner />);
    expect(screen.queryByRole('button')).not.toBeInTheDocument();
  });

  it('con la sessione lontana dalla fine non mostra nulla', () => {
    vi.mocked(getSessionExpiresAt).mockReturnValue(minutes(30));
    render(<SessionExpiryBanner />);
    expect(screen.queryByRole('button')).not.toBeInTheDocument();
  });

  it("cinque minuti prima avvisa con l'ora e propone un nuovo accesso", async () => {
    vi.mocked(getSessionExpiresAt).mockReturnValue(minutes(4));
    render(<SessionExpiryBanner />);

    expect(screen.getByRole('alert')).toHaveTextContent('La sessione scade alle 09:04');
    await expectNoAxeViolations();

    await userEvent.click(screen.getByRole('button', { name: 'Accedi di nuovo adesso' }));
    expect(reloginNow).toHaveBeenCalledTimes(1);
  });

  it('compare quando la fine si avvicina, al controllo successivo', () => {
    vi.mocked(getSessionExpiresAt).mockReturnValue(minutes(6));
    render(<SessionExpiryBanner />);
    expect(screen.queryByRole('button')).not.toBeInTheDocument();

    act(() => vi.advanceTimersByTime(SESSION_CHECK_MS * 5));
    expect(screen.getByRole('button', { name: 'Accedi di nuovo adesso' })).toBeInTheDocument();
  });

  it('«Ho capito» lo chiude fino alla scadenza successiva', async () => {
    vi.mocked(getSessionExpiresAt).mockReturnValue(minutes(4));
    render(<SessionExpiryBanner />);

    await userEvent.click(screen.getByRole('button', { name: 'Ho capito' }));
    expect(screen.queryByRole('button')).not.toBeInTheDocument();

    act(() => vi.advanceTimersByTime(SESSION_CHECK_MS));
    expect(screen.queryByRole('button')).not.toBeInTheDocument();
  });

  it('a sessione già scaduta non mostra nulla', () => {
    vi.mocked(getSessionExpiresAt).mockReturnValue(minutes(-1));
    render(<SessionExpiryBanner />);
    expect(screen.queryByRole('button')).not.toBeInTheDocument();
  });
});
