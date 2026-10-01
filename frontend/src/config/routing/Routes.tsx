import { type RouteObject } from 'react-router';

import { DEV_LOGIN_PATH, ProtectedPage } from '@/config/routing/ProtectedPage';
import { RoleRoute } from '@/config/routing/RoleRoute';
import type { RouteHandle } from '@/config/routing/types';
import { BeneficiaryNewPage } from '@/pages/BeneficiaryNewPage/BeneficiaryNewPage';
import { DevLoginPage } from '@/pages/DevLoginPage/DevLoginPage';
import { HomePage } from '@/pages/HomePage/HomePage';
import { MyWorkPage } from '@/pages/MyWorkPage/MyWorkPage';
import { NotFoundPage } from '@/pages/NotFoundPage/NotFoundPage';
import { QueuePage } from '@/pages/QueuePage/QueuePage';
import { TicketNewPage } from '@/pages/TicketNewPage/TicketNewPage';

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
      { index: true, element: <HomePage />, handle: { titleKey: 'pages.home.title' } satisfies RouteHandle },
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
      {
        path: 'coda',
        element: (
          <RoleRoute roles={['CALL_CENTER', 'ADMIN']}>
            <QueuePage />
          </RoleRoute>
        ),
        handle: { titleKey: 'pages.queue.title' } satisfies RouteHandle,
      },
      {
        path: 'lavorazioni',
        element: (
          <RoleRoute roles={['CALL_CENTER', 'ADMIN']}>
            <MyWorkPage />
          </RoleRoute>
        ),
        handle: { titleKey: 'pages.myWork.title' } satisfies RouteHandle,
      },
      {
        path: '*',
        element: <NotFoundPage />,
        handle: { titleKey: 'pages.notFound.title' } satisfies RouteHandle,
      },
    ],
  },
];
