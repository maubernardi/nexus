import { screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';

import { MainNav } from '@/organisms/MainNav/MainNav';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderWithProviders } from '@/tests/renderWithProviders';

describe('<MainNav>', () => {
  it('espone un landmark nav etichettato con la pagina corrente marcata', async () => {
    renderWithProviders(<MainNav roles={['TUTOR']} />);
    expect(screen.getByRole('navigation', { name: 'Navigazione principale' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Segnalazioni' })).toHaveAttribute('aria-current', 'page');
    await expectNoAxeViolations();
  });

  it('Coordinamento solo per il Tutor che coordina un progetto', () => {
    const { unmount } = renderWithProviders(<MainNav roles={['TUTOR']} />);
    expect(screen.queryAllByRole('link', { name: 'Coordinamento' })).toHaveLength(0);
    unmount();
    renderWithProviders(<MainNav roles={['TUTOR']} coordinator />);
    expect(screen.getAllByRole('link', { name: 'Coordinamento' }).length).toBeGreaterThan(0);
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

  it('mostra il contenuto dedicato in cima al menu mobile', async () => {
    renderWithProviders(<MainNav roles={['TUTOR']} mobileHeader={<p>Tutor Uno</p>} />);
    const user = userEvent.setup();

    await user.click(screen.getByRole('button', { name: 'Apri menu' }));

    const panel = document.getElementById(
      screen.getByRole('button', { name: 'Chiudi menu' }).getAttribute('aria-controls') ?? '',
    );
    expect(panel).toHaveTextContent('Tutor Uno');
    await expectNoAxeViolations();
  });

  it('ADMIN: Utenti, Cataloghi, Modelli e Calendario nel sottomenu «Amministrazione», che si chiude con Esc', async () => {
    renderWithProviders(<MainNav roles={['ADMIN']} />);
    const user = userEvent.setup();
    const toggle = screen.getByRole('button', { name: 'Amministrazione' });
    expect(toggle).toHaveAttribute('aria-expanded', 'false');
    // le voci in vista restano quattro, più il sottomenu
    expect(screen.getByRole('link', { name: 'Beneficiari' })).toHaveAttribute('href', '/beneficiari/tutti');

    await user.click(toggle);
    expect(toggle).toHaveAttribute('aria-expanded', 'true');
    const submenu = document.getElementById(toggle.getAttribute('aria-controls') ?? '') as HTMLElement;
    expect(
      within(submenu)
        .getAllByRole('link')
        .map((l) => l.textContent),
    ).toEqual(['Utenti', 'Cataloghi', 'Modelli dei documenti', 'Calendario']);
    await expectNoAxeViolations();

    await user.keyboard('{Escape}');
    expect(toggle).toHaveAttribute('aria-expanded', 'false');
    expect(toggle).toHaveFocus();
  });

  it('il Tutor non ha il sottomenu', () => {
    renderWithProviders(<MainNav roles={['TUTOR']} />);
    expect(screen.queryByRole('button', { name: 'Amministrazione' })).not.toBeInTheDocument();
  });
});
