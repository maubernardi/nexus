import { act, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';

import { OfflineBanner } from '@/molecules/OfflineBanner/OfflineBanner';
import { expectNoAxeViolations } from '@/tests/axe';

const setOnline = (online: boolean): void => {
  vi.spyOn(navigator, 'onLine', 'get').mockReturnValue(online);
  act(() => {
    window.dispatchEvent(new Event(online ? 'online' : 'offline'));
  });
};

describe('<OfflineBanner>', () => {
  afterEach(() => vi.restoreAllMocks());

  it('non mostra messaggi quando online ma mantiene la regione live', () => {
    render(<OfflineBanner />);
    expect(screen.getByRole('status')).toBeEmptyDOMElement();
  });

  it('annuncia lo stato offline con testo, non solo colore', async () => {
    render(<OfflineBanner />);
    setOnline(false);
    expect(screen.getByRole('status')).toHaveTextContent('Sei offline');
    await expectNoAxeViolations();

    setOnline(true);
    expect(screen.getByRole('status')).toBeEmptyDOMElement();
  });
});
