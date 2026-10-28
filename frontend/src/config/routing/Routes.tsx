import { Navigate, type RouteObject } from 'react-router';

import { DEV_LOGIN_PATH, ProtectedPage } from '@/config/routing/ProtectedPage';
import { RoleRoute } from '@/config/routing/RoleRoute';
import type { RouteHandle } from '@/config/routing/types';
import { BeneficiariesPage } from '@/pages/BeneficiariesPage/BeneficiariesPage';
import { BoardRoute } from '@/pages/BoardPage/BoardRoute';
import { CandidaturePage } from '@/pages/CandidaturePage/CandidaturePage';
import { CatalogsPage } from '@/pages/CatalogsPage/CatalogsPage';
import { BoardPostPage } from '@/pages/BoardPostPage/BoardPostPage';
import { BeneficiaryEditPage } from '@/pages/BeneficiaryEditPage/BeneficiaryEditPage';
import { BeneficiaryNewPage } from '@/pages/BeneficiaryNewPage/BeneficiaryNewPage';
import { BeneficiaryPage } from '@/pages/BeneficiaryPage/BeneficiaryPage';
import { DevLoginPage } from '@/pages/DevLoginPage/DevLoginPage';
import { CalendarPage } from '@/pages/CalendarPage/CalendarPage';
import { CompaniesPage } from '@/pages/CompaniesPage/CompaniesPage';
import { CompanyEditPage } from '@/pages/CompanyEditPage/CompanyEditPage';
import { CompanyNewPage } from '@/pages/CompanyNewPage/CompanyNewPage';
import { NotificationsPage } from '@/pages/NotificationsPage/NotificationsPage';
import { NotFoundPage } from '@/pages/NotFoundPage/NotFoundPage';
import { LegacyWorkRedirect, TicketPage } from '@/pages/TicketPage/TicketPage';
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
        path: 'beneficiari',
        element: (
          <RoleRoute roles={['TUTOR']}>
            <BeneficiariesPage />
          </RoleRoute>
        ),
        handle: { titleKey: 'pages.beneficiaries.title' } satisfies RouteHandle,
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
        // US-204: la visibilità del singolo beneficiario la decide l'API
        path: 'beneficiari/:id',
        element: <BeneficiaryPage />,
        handle: { titleKey: 'pages.beneficiary.title' } satisfies RouteHandle,
      },
      {
        path: 'beneficiari/:id/modifica',
        element: (
          <RoleRoute roles={['TUTOR']}>
            <BeneficiaryEditPage />
          </RoleRoute>
        ),
        handle: { titleKey: 'pages.beneficiaryEdit.title' } satisfies RouteHandle,
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
        path: 'segnalazioni/:id',
        element: <TicketPage />,
        handle: { titleKey: 'pages.ticket.title' } satisfies RouteHandle,
      },
      // indirizzo della vecchia pagina di lavorazione: porta al dettaglio unico
      { path: 'lavorazioni/:id', element: <LegacyWorkRedirect /> },
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
        path: 'bacheca',
        element: (
          <RoleRoute roles={['TUTOR', 'CALL_CENTER', 'ADMIN']}>
            <BoardRoute />
          </RoleRoute>
        ),
        handle: { titleKey: 'pages.board.title' } satisfies RouteHandle,
      },
      {
        path: 'bacheca/:id/candidatura',
        element: (
          <RoleRoute roles={['TUTOR']}>
            <CandidaturePage />
          </RoleRoute>
        ),
        handle: { titleKey: 'pages.candidature.title' } satisfies RouteHandle,
      },
      {
        path: 'bacheca/:id',
        element: (
          <RoleRoute roles={['CALL_CENTER', 'ADMIN']}>
            <BoardPostPage />
          </RoleRoute>
        ),
        handle: { titleKey: 'pages.boardPost.title' } satisfies RouteHandle,
      },
      {
        path: 'notifiche',
        element: <NotificationsPage />,
        handle: { titleKey: 'pages.notifications.title' } satisfies RouteHandle,
      },
      {
        path: 'cataloghi',
        element: (
          <RoleRoute roles={['ADMIN']}>
            <CatalogsPage />
          </RoleRoute>
        ),
        handle: { titleKey: 'pages.catalogs.title' } satisfies RouteHandle,
      },
      {
        path: 'calendario',
        element: (
          <RoleRoute roles={['ADMIN']}>
            <CalendarPage />
          </RoleRoute>
        ),
        handle: { titleKey: 'pages.calendar.title' } satisfies RouteHandle,
      },
      {
        path: '*',
        element: <NotFoundPage />,
        handle: { titleKey: 'pages.notFound.title' } satisfies RouteHandle,
      },
    ],
  },
];
