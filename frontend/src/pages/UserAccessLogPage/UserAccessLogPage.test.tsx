import { screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';

import { fetchUserAccessLog } from '@/config/api/accessLogApi';
import { fetchUser, type AdminUser } from '@/config/api/userAdminApi';
import { UserAccessLogPage } from '@/pages/UserAccessLogPage/UserAccessLogPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderRoutes } from '@/tests/renderWithProviders';

vi.mock('@/config/api/accessLogApi', () => ({
  fetchMyAccessLog: vi.fn(),
  fetchUserAccessLog: vi.fn(),
  fetchFailedLogins: vi.fn(),
}));

vi.mock('@/config/api/userAdminApi', () => ({
  fetchUsers: vi.fn(),
  fetchUser: vi.fn(),
  createUser: vi.fn(),
  resendInvitation: vi.fn(),
  updateUserProjects: vi.fn(),
}));

const operator: AdminUser = {
  id: 'U3',
  username: 'anna.verdi',
  firstName: 'Anna',
  lastName: 'Verdi',
  email: 'anna@example.org',
  role: 'CALL_CENTER',
  active: true,
  projects: [],
};

describe('<UserAccessLogPage>', () => {
  it('accessi di un utente, senza accessi registrati', async () => {
    vi.mocked(fetchUser).mockResolvedValue(operator);
    vi.mocked(fetchUserAccessLog).mockResolvedValue({ entries: [], hasMore: false });
    renderRoutes({
      routes: [
        {
          path: '/utenti/:id/accessi',
          element: (
            <main>
              <UserAccessLogPage />
            </main>
          ),
        },
      ],
      initialPath: '/utenti/U3/accessi',
    });

    expect(
      await screen.findByRole('heading', { level: 1, name: 'Accessi di Anna Verdi' }),
    ).toBeInTheDocument();
    expect(await screen.findByText('Nessun accesso registrato.')).toBeInTheDocument();
    expect(fetchUserAccessLog).toHaveBeenCalledWith('U3', 0);
    // in cima e in fondo alla scheda (US-1312)
    const back = screen.getAllByRole('link', { name: 'Torna agli utenti' });
    expect(back).toHaveLength(2);
    for (const link of back) expect(link).toHaveAttribute('href', '/utenti');
    await expectNoAxeViolations();
  });
});
