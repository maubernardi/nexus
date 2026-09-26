import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { fetchCurrentUser } from '@/config/api/userApi';
import { routes } from '@/config/routing/Routes';
import { useSessionStore } from '@/config/stores/sessionStore';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderRoutes } from '@/tests/renderWithProviders';

vi.mock('@/config/api/userApi', () => ({ fetchCurrentUser: vi.fn() }));

describe('App shell', () => {
  beforeEach(() => {
    useSessionStore.setState({ mockUserId: null });
    vi.mocked(fetchCurrentUser).mockResolvedValue({
      id: 'operatore.cc',
      username: 'operatore.cc',
      firstName: 'Operatore',
      lastName: 'Call Center',
      roles: ['CALL_CENTER'],
    });
  });

  it("senza utente mock reindirizza all'accesso di sviluppo", async () => {
    renderRoutes({ routes });
    expect(await screen.findByRole('heading', { level: 1, name: 'Accesso di sviluppo' })).toBeInTheDocument();
    await waitFor(() => expect(document.title).toBe('Accesso di sviluppo · NEXUS'));
    await expectNoAxeViolations();
  });

  it('mostra la shell con landmark, utente e ruolo, senza violazioni axe', async () => {
    useSessionStore.setState({ mockUserId: 'operatore.cc' });
    renderRoutes({ routes });

    expect(await screen.findByRole('heading', { level: 1, name: 'Benvenuto in NEXUS' })).toBeInTheDocument();
    expect(screen.getByRole('banner')).toHaveTextContent('Operatore Call Center');
    expect(screen.getByRole('main')).toBeInTheDocument();
    expect(screen.getByRole('contentinfo')).toBeInTheDocument();
    expect(screen.getAllByRole('heading', { level: 1 })).toHaveLength(1);
    expect(document.title).toBe('Home · NEXUS');
    await expectNoAxeViolations();
  });

  it('il primo Tab raggiunge lo skip link', async () => {
    useSessionStore.setState({ mockUserId: 'operatore.cc' });
    renderRoutes({ routes });
    await screen.findByRole('heading', { level: 1 });

    await userEvent.tab();
    expect(screen.getByRole('link', { name: 'Salta al contenuto principale' })).toHaveFocus();
  });

  it("al cambio pagina aggiorna il titolo e porta il focus sull'h1", async () => {
    useSessionStore.setState({ mockUserId: 'operatore.cc' });
    const { router } = renderRoutes({ routes });
    await screen.findByRole('heading', { level: 1, name: 'Benvenuto in NEXUS' });

    await router.navigate('/pagina-inesistente');

    const heading = await screen.findByRole('heading', { level: 1, name: 'Pagina non trovata' });
    await waitFor(() => expect(heading).toHaveFocus());
    expect(document.title).toBe('Pagina non trovata · NEXUS');
  });

  it('dalla pagina di accesso entra con l’utente scelto', async () => {
    renderRoutes({ routes });
    await userEvent.click(await screen.findByRole('button', { name: /Entra come Operatore Call Center/ }));
    expect(await screen.findByRole('heading', { level: 1, name: 'Benvenuto in NEXUS' })).toBeInTheDocument();
    expect(useSessionStore.getState().mockUserId).toBe('operatore.cc');
  });
});
