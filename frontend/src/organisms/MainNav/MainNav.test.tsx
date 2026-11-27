import { screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';

import { fetchOpenContactRequestCount } from '@/config/api/contactRequestApi';

import { MainNav } from '@/organisms/MainNav/MainNav';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderWithProviders } from '@/tests/renderWithProviders';

vi.mock('@/config/api/contactRequestApi', () => ({
  fetchOpenContactRequestCount: vi.fn().mockResolvedValue(2),
}));

describe('<MainNav>', () => {
  it('espone un landmark nav etichettato con la pagina corrente marcata', async () => {
    renderWithProviders(<MainNav roles={['TUTOR']} />);
    expect(screen.getByRole('navigation', { name: 'Navigazione principale' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Segnalazioni' })).toHaveAttribute('aria-current', 'page');
    // il Tutor vede le aziende, in sola lettura (US-511)
    expect(screen.getByRole('link', { name: 'Aziende' })).toHaveAttribute('href', '/aziende');
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

  it('ADMIN: richieste di contatto, Utenti, Cataloghi, Modelli e Calendario nel sottomenu «Amministrazione», che si chiude con Esc', async () => {
    renderWithProviders(<MainNav roles={['ADMIN']} />);
    const user = userEvent.setup();
    const toggle = screen.getByRole('button', { name: 'Amministrazione' });
    expect(toggle).toHaveAttribute('aria-expanded', 'false');
    // le voci in vista restano quattro, più il sottomenu
    expect(screen.getByRole('link', { name: 'Beneficiari' })).toHaveAttribute('href', '/beneficiari/tutti');

    await user.click(toggle);
    expect(toggle).toHaveAttribute('aria-expanded', 'true');
    const submenu = document.getElementById(toggle.getAttribute('aria-controls') ?? '') as HTMLElement;
    // le richieste di contatto in testa (US-507): nella riga del menu non c'è più spazio
    expect(
      within(submenu)
        .getAllByRole('link')
        .map((l) => l.getAttribute('href')),
    ).toEqual(['/richieste-contatto', '/utenti', '/cataloghi', '/modelli', '/testi', '/calendario']);
    await expectNoAxeViolations();

    await user.keyboard('{Escape}');
    expect(toggle).toHaveAttribute('aria-expanded', 'false');
    expect(toggle).toHaveFocus();
  });

  it('il Tutor non ha il sottomenu', () => {
    renderWithProviders(<MainNav roles={['TUTOR']} />);
    expect(screen.queryByRole('button', { name: 'Amministrazione' })).not.toBeInTheDocument();
  });

  it('richieste di contatto: «Richieste» per il Tutor, per il Call Center la voce con il numero (US-507)', async () => {
    const { unmount } = renderWithProviders(<MainNav roles={['TUTOR']} />);
    expect(screen.getByRole('link', { name: 'Richieste' })).toHaveAttribute('href', '/richieste');
    expect(fetchOpenContactRequestCount).not.toHaveBeenCalled();
    unmount();

    renderWithProviders(<MainNav roles={['CALL_CENTER']} />);
    const link = await screen.findByRole('link', { name: 'Richieste di contatto (2 da prendere in carico)' });
    expect(link).toHaveAttribute('href', '/richieste-contatto');
    expect(screen.queryByRole('link', { name: 'Richieste' })).not.toBeInTheDocument();
    await expectNoAxeViolations();
  });

  it('per l’ADMIN le richieste di contatto stanno nel sottomenu «Amministrazione», con il numero', async () => {
    renderWithProviders(<MainNav roles={['ADMIN']} />);
    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: 'Amministrazione' }));
    const submenu = document.getElementById(
      screen.getByRole('button', { name: 'Amministrazione' }).getAttribute('aria-controls') ?? '',
    );
    expect(
      await within(submenu as HTMLElement).findByRole('link', {
        name: 'Richieste di contatto (2 da prendere in carico)',
      }),
    ).toHaveAttribute('href', '/richieste-contatto');
  });
});
