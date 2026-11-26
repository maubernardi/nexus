import { screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';

import { fetchCurrentUser } from '@/config/api/userApi';
import type { Role } from '@/config/auth/types';
import { CompaniesRoute, CompanyRoute } from '@/config/routing/CompaniesRoute';
import { renderRoutes } from '@/tests/renderWithProviders';

vi.mock('@/config/api/userApi', () => ({ fetchCurrentUser: vi.fn() }));
vi.mock('@/pages/CompaniesPage/CompaniesPage', () => ({ CompaniesPage: () => <p>anagrafica completa</p> }));
vi.mock('@/pages/TutorCompaniesPage/TutorCompaniesPage', () => ({
  TutorCompaniesPage: () => <p>elenco del Tutor</p>,
}));
vi.mock('@/pages/CompanyEditPage/CompanyEditPage', () => ({
  CompanyEditPage: () => <p>scheda modificabile</p>,
}));
vi.mock('@/pages/TutorCompanyPage/TutorCompanyPage', () => ({
  TutorCompanyPage: () => <p>scheda del Tutor</p>,
}));

const renderAs = (roles: Role[]) => {
  vi.mocked(fetchCurrentUser).mockResolvedValue({
    id: 'u',
    username: 'u',
    firstName: 'U',
    lastName: 'P',
    roles,
  });
  renderRoutes({ routes: [{ path: '/aziende', element: <CompaniesRoute /> }], initialPath: '/aziende' });
};

describe('<CompaniesRoute>', () => {
  it('Call Center e ADMIN: anagrafica completa', async () => {
    renderAs(['CALL_CENTER']);
    expect(await screen.findByText('anagrafica completa')).toBeInTheDocument();
  });

  it('Tutor: elenco in sola lettura (US-511)', async () => {
    renderAs(['TUTOR']);
    expect(await screen.findByText('elenco del Tutor')).toBeInTheDocument();
  });

  it('scheda: modificabile per il Call Center, in sola lettura per il Tutor (US-1104)', async () => {
    vi.mocked(fetchCurrentUser).mockResolvedValue({
      id: 'u',
      username: 'u',
      firstName: 'U',
      lastName: 'P',
      roles: ['TUTOR'],
    });
    renderRoutes({
      routes: [{ path: '/aziende/:id', element: <CompanyRoute /> }],
      initialPath: '/aziende/C1',
    });
    expect(await screen.findByText('scheda del Tutor')).toBeInTheDocument();
  });
});
