import { type RouteObject } from 'react-router-dom';

import { DEV_LOGIN_PATH, ProtectedPage } from '@/config/routing/ProtectedPage';
import type { RouteHandle } from '@/config/routing/types';
import { DevLoginPage } from '@/pages/DevLoginPage/DevLoginPage';
import { HomePage } from '@/pages/HomePage/HomePage';
import { NotFoundPage } from '@/pages/NotFoundPage/NotFoundPage';

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
        path: '*',
        element: <NotFoundPage />,
        handle: { titleKey: 'pages.notFound.title' } satisfies RouteHandle,
      },
    ],
  },
];
