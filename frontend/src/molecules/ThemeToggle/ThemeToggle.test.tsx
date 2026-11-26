import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, describe, expect, it } from 'vitest';

import { THEME_STORAGE_KEY } from '@/hooks/useTheme';
import { ThemeToggle } from '@/molecules/ThemeToggle/ThemeToggle';
import { expectNoAxeViolations } from '@/tests/axe';

describe('<ThemeToggle>', () => {
  afterEach(() => {
    delete document.documentElement.dataset.theme;
    localStorage.removeItem(THEME_STORAGE_KEY);
  });

  it('dal tema chiaro passa allo scuro e ritorno; la scelta resta nel browser', async () => {
    const user = userEvent.setup();
    render(
      <main>
        <ThemeToggle />
      </main>,
    );
    // senza scelta e senza preferenza del sistema (jsdom): tema chiaro, il pulsante porta allo scuro
    await user.click(screen.getByRole('button', { name: 'Passa al tema scuro' }));
    expect(document.documentElement.dataset.theme).toBe('dark');
    expect(localStorage.getItem(THEME_STORAGE_KEY)).toBe('dark');
    await expectNoAxeViolations();

    await user.click(screen.getByRole('button', { name: 'Passa al tema chiaro' }));
    expect(document.documentElement.dataset.theme).toBe('light');
    expect(localStorage.getItem(THEME_STORAGE_KEY)).toBe('light');
  });

  it('parte dal tema già scelto', () => {
    document.documentElement.dataset.theme = 'dark';
    render(<ThemeToggle />);
    expect(screen.getByRole('button', { name: 'Passa al tema chiaro' })).toHaveAttribute(
      'title',
      'Passa al tema chiaro',
    );
  });
});
