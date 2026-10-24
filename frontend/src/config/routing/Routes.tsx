import { Navigate, type RouteObject } from 'react-router';

import { DEV_LOGIN_PATH, ProtectedPage } from '@/config/routing/ProtectedPage';
import { RoleRoute } from '@/config/routing/RoleRoute';
import type { RouteHandle } from '@/config/routing/types';
import { BeneficiaryNewPage } from '@/pages/BeneficiaryNewPage/BeneficiaryNewPage';
import { DevLoginPage } from '@/pages/DevLoginPage/DevLoginPage';
import { CompaniesPage } from '@/pages/CompaniesPage/CompaniesPage';
import { CompanyEditPage } from '@/pages/CompanyEditPage/CompanyEditPage';
import { CompanyNewPage } from '@/pages/CompanyNewPage/CompanyNewPage';
import { NotFoundPage } from '@/pages/NotFoundPage/NotFoundPage';
import { TicketWorkPage } from '@/pages/TicketWorkPage/TicketWorkPage';
import { TicketNewPage } from '@/pages/TicketNewPage/TicketNewPage';
import { TicketsPage } from '@/pages/TicketsPage/TicketsPage';

export const routes: RouteObject[] = [
  {
    path: DEV_LOGIN_PATH,
    element: <DevLoginPage />,
    handle: { titleKey: 'pages.devLogin.title' } satisfies RouteHandle,
  },
  {
    path: '/',
    element: <ProtectedPage />,
    children: [
      {
        index: true,
        element: <TicketsPage />,
        handle: { titleKey: 'pages.tickets.title' } satisfies RouteHandle,
      },
      {
        path: 'beneficiari/nuovo',
        element: (
          <RoleRoute roles={['TUTOR']}>
            <BeneficiaryNewPage />
          </RoleRoute>
        ),
        handle: { titleKey: 'pages.beneficiaryNew.title' } satisfies RouteHandle,
      },
      {
        path: 'segnalazioni/nuova',
        element: (
          <RoleRoute roles={['TUTOR']}>
            <TicketNewPage />
          </RoleRoute>
        ),
        handle: { titleKey: 'pages.ticketNew.title' } satisfies RouteHandle,
      },
      // indirizzi delle pagine sostituite dalla Home (US-303): restano validi segnalibri e link già condivisi
      { path: 'coda', element: <Navigate to="/?vista=da-prendere" replace /> },
      { path: 'lavorazioni', element: <Navigate to="/?vista=in-carico" replace /> },
      {
        path: 'lavorazioni/:id',
        element: (
          <RoleRoute roles={['CALL_CENTER', 'ADMIN']}>
            <TicketWorkPage />
          </RoleRoute>
        ),
        handle: { titleKey: 'pages.ticketWork.title' } satisfies RouteHandle,
      },
      {
        path: 'aziende',
        element: (
          <RoleRoute roles={['CALL_CENTER', 'ADMIN']}>
            <CompaniesPage />
          </RoleRoute>
        ),
        handle: { titleKey: 'pages.companies.title' } satisfies RouteHandle,
      },
      {
        path: 'aziende/nuova',
        element: (
          <RoleRoute roles={['CALL_CENTER', 'ADMIN']}>
            <CompanyNewPage />
          </RoleRoute>
        ),
        handle: { titleKey: 'pages.companyNew.title' } satisfies RouteHandle,
      },
      {
        path: 'aziende/:id',
        element: (
          <RoleRoute roles={['CALL_CENTER', 'ADMIN']}>
            <CompanyEditPage />
          </RoleRoute>
        ),
        handle: { titleKey: 'pages.companyEdit.title' } satisfies RouteHandle,
      },
      {
        path: '*',
        element: <NotFoundPage />,
        handle: { titleKey: 'pages.notFound.title' } satisfies RouteHandle,
      },
    ],
  },
];
