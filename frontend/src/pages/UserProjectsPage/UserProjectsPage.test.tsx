import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AxiosError, AxiosHeaders } from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { fetchCatalog } from '@/config/api/catalogApi';
import { fetchUser, updateUserProjects, type AdminUser } from '@/config/api/userAdminApi';
import { UserProjectsPage } from '@/pages/UserProjectsPage/UserProjectsPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderRoutes } from '@/tests/renderWithProviders';

vi.mock('@/config/api/userAdminApi', () => ({
  fetchUsers: vi.fn(),
  fetchUser: vi.fn(),
  createUser: vi.fn(),
  resendInvitation: vi.fn(),
  updateUserProjects: vi.fn(),
}));

vi.mock('@/config/api/catalogApi', () => ({
  fetchCatalog: vi.fn(),
  createCatalogItem: vi.fn(),
  renameCatalogItem: vi.fn(),
  setCatalogItemActive: vi.fn(),
}));

const tutor: AdminUser = {
  id: 'U1',
  username: 'mario.rossi',
  firstName: 'Mario',
  lastName: 'Rossi',
  email: 'mario@example.org',
  role: 'TUTOR',
  active: true,
  projects: [
    { id: 'P1', code: 'GOL', name: 'GOL' },
    { id: 'P3', code: 'VECCHIO', name: 'Vecchio' },
  ],
};

const renderPage = () =>
  renderRoutes({
    routes: [
      {
        path: '/utenti/:id/progetti',
        element: (
          <main>
            <UserProjectsPage />
          </main>
        ),
      },
      { path: '/utenti', element: <p>Elenco degli utenti</p> },
    ],
    initialPath: '/utenti/U1/progetti',
  });

describe('<UserProjectsPage>', () => {
  beforeEach(() => {
    vi.mocked(fetchUser).mockResolvedValue(tutor);
    vi.mocked(fetchCatalog).mockResolvedValue([
      { id: 'P1', code: 'GOL', name: 'GOL', active: true },
      { id: 'P2', code: 'POLIS', name: 'POLIS', active: true },
      { id: 'P4', code: 'CHIUSO', name: 'Chiuso', active: false },
      { id: 'P3', code: 'VECCHIO', name: 'Vecchio', active: false },
    ]);
    vi.mocked(updateUserProjects).mockReset();
  });

  it('progetti attivi più i disattivati ancora assegnati; salva e torna all’elenco', async () => {
    const user = userEvent.setup();
    const { router } = renderPage();

    expect(
      await screen.findByRole('heading', { level: 1, name: 'Progetti di Mario Rossi' }),
    ).toBeInTheDocument();
    expect(screen.getByRole('group', { name: 'Progetti assegnati a Mario Rossi' })).toBeInTheDocument();
    expect(screen.getByRole('checkbox', { name: 'GOL' })).toBeChecked();
    expect(screen.getByRole('checkbox', { name: 'POLIS' })).not.toBeChecked();
    expect(screen.getByRole('checkbox', { name: /^Vecchio \(disattivato/ })).toBeChecked();
    // un progetto disattivato non assegnato non si offre
    expect(screen.queryByRole('checkbox', { name: /Chiuso/ })).not.toBeInTheDocument();
    await expectNoAxeViolations();

    await user.click(screen.getByRole('checkbox', { name: 'POLIS' }));
    await user.click(screen.getByRole('checkbox', { name: /^Vecchio/ }));
    vi.mocked(updateUserProjects).mockResolvedValueOnce(tutor);
    await user.click(screen.getByRole('button', { name: 'Salva i progetti' }));

    expect(updateUserProjects).toHaveBeenCalledWith({ id: 'U1', projectIds: ['P1', 'P2'] });
    await waitFor(() => expect(router.state.location.pathname).toBe('/utenti'));
    expect(router.state.location.state).toEqual({ projectsSaved: 'Mario Rossi' });
  });

  it('errore del server sul campo', async () => {
    const user = userEvent.setup();
    renderPage();
    await user.click(await screen.findByRole('checkbox', { name: 'POLIS' }));
    vi.mocked(updateUserProjects).mockRejectedValueOnce(
      new AxiosError('400', '400', undefined, undefined, {
        status: 400,
        statusText: '400',
        headers: {},
        config: { headers: new AxiosHeaders() },
        data: {
          status: 400,
          fieldErrors: [
            { field: 'projectIds', message: 'Il progetto POLIS è disattivato: non si può assegnare' },
          ],
        },
      }),
    );
    await user.click(screen.getByRole('button', { name: 'Salva i progetti' }));
    const alert = await screen.findByRole('alert');
    expect(alert).toHaveTextContent('Il progetto POLIS è disattivato');
    await waitFor(() => expect(alert).toHaveFocus());
  });

  it('per chi non è Tutor spiega che vede già tutti i progetti', async () => {
    vi.mocked(fetchUser).mockResolvedValue({ ...tutor, role: 'CALL_CENTER', projects: [] });
    renderPage();
    expect(await screen.findByText(/Solo i Tutor hanno progetti assegnati/)).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Salva i progetti' })).not.toBeInTheDocument();
    await expectNoAxeViolations();
  });
});
