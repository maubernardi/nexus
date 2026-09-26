import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';

import { MainNav } from '@/organisms/MainNav/MainNav';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderWithProviders } from '@/tests/renderWithProviders';

describe('<MainNav>', () => {
  it('espone un landmark nav etichettato con la pagina corrente marcata', async () => {
    renderWithProviders(<MainNav roles={['TUTOR']} />);
    expect(screen.getByRole('navigation', { name: 'Navigazione principale' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Home' })).toHaveAttribute('aria-current', 'page');
    await expectNoAxeViolations();
  });

  it('apre e chiude il menu mobile da tastiera, restituendo il focus al pulsante', async () => {
    renderWithProviders(<MainNav roles={['TUTOR']} />);
    const user = userEvent.setup();
    const toggle = screen.getByRole('button', { name: 'Apri menu' });
    expect(toggle).toHaveAttribute('aria-expanded', 'false');

    await user.click(toggle);
    expect(screen.getByRole('button', { name: 'Chiudi menu' })).toHaveAttribute('aria-expanded', 'true');

    await user.tab();
    await user.keyboard('{Escape}');
    expect(screen.getByRole('button', { name: 'Apri menu' })).toHaveFocus();
    expect(screen.getByRole('button', { name: 'Apri menu' })).toHaveAttribute('aria-expanded', 'false');
  });
});
