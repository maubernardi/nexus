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
import { ClosureReportPage } from '@/pages/ClosureReportPage/ClosureReportPage';
import { DevLoginPage } from '@/pages/DevLoginPage/DevLoginPage';
import { InternshipPage } from '@/pages/InternshipPage/InternshipPage';
import { CoordinationPage } from '@/pages/CoordinationPage/CoordinationPage';
import { FailedLoginsPage } from '@/pages/FailedLoginsPage/FailedLoginsPage';
import { UserAccessLogPage } from '@/pages/UserAccessLogPage/UserAccessLogPage';
import { CalendarPage } from '@/pages/CalendarPage/CalendarPage';
import { CompaniesPage } from '@/pages/CompaniesPage/CompaniesPage';
import { CompanyEditPage } from '@/pages/CompanyEditPage/CompanyEditPage';
import { CompanyNewPage } from '@/pages/CompanyNewPage/CompanyNewPage';
import { NotificationsPage } from '@/pages/NotificationsPage/NotificationsPage';
import { ProfilePage } from '@/pages/ProfilePage/ProfilePage';
import { UserAccountPage } from '@/pages/UserAccountPage/UserAccountPage';
import { UserNewPage } from '@/pages/UserNewPage/UserNewPage';
import { UserProjectsPage } from '@/pages/UserProjectsPage/UserProjectsPage';
import { UserTicketsPage } from '@/pages/UserTicketsPage/UserTicketsPage';
import { UsersPage } from '@/pages/UsersPage/UsersPage';
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
      {
        // US-1001: la scheda è solo del Tutor della segnalazione (lo verifica l'API)
        path: 'segnalazioni/:id/tirocinio',
        element: (
          <RoleRoute roles={['TUTOR']}>
            <InternshipPage />
          </RoleRoute>
        ),
        handle: { titleKey: 'pages.internship.title' } satisfies RouteHandle,
      },
      {
        // US-1101: il report lo invia solo il Tutor della segnalazione (lo verifica l'API); gli altri lo leggono nel
        // dettaglio della segnalazione
        path: 'segnalazioni/:id/restituzione',
        element: (
          <RoleRoute roles={['TUTOR']}>
            <ClosureReportPage />
          </RoleRoute>
        ),
        handle: { titleKey: 'pages.closureReport.title' } satisfies RouteHandle,
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
        path: 'profilo',
        element: <ProfilePage />,
        handle: { titleKey: 'pages.profile.title' } satisfies RouteHandle,
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
        path: 'utenti',
        element: (
          <RoleRoute roles={['ADMIN']}>
            <UsersPage />
          </RoleRoute>
        ),
        handle: { titleKey: 'pages.users.title' } satisfies RouteHandle,
      },
      {
        path: 'utenti/nuovo',
        element: (
          <RoleRoute roles={['ADMIN']}>
            <UserNewPage />
          </RoleRoute>
        ),
        handle: { titleKey: 'pages.userNew.title' } satisfies RouteHandle,
      },
      {
        path: 'utenti/:id/progetti',
        element: (
          <RoleRoute roles={['ADMIN']}>
            <UserProjectsPage />
          </RoleRoute>
        ),
        handle: { titleKey: 'pages.userProjects.title' } satisfies RouteHandle,
      },
      {
        path: 'utenti/:id/segnalazioni',
        element: (
          <RoleRoute roles={['ADMIN']}>
            <UserTicketsPage />
          </RoleRoute>
        ),
        handle: { titleKey: 'pages.userTickets.title' } satisfies RouteHandle,
      },
      {
        path: 'utenti/accessi-falliti',
        element: (
          <RoleRoute roles={['ADMIN']}>
            <FailedLoginsPage />
          </RoleRoute>
        ),
        handle: { titleKey: 'pages.failedLogins.title' } satisfies RouteHandle,
      },
      {
        path: 'utenti/:id/accessi',
        element: (
          <RoleRoute roles={['ADMIN']}>
            <UserAccessLogPage />
          </RoleRoute>
        ),
        handle: { titleKey: 'pages.userAccessLog.title' } satisfies RouteHandle,
      },
      {
        path: 'utenti/:id/accesso',
        element: (
          <RoleRoute roles={['ADMIN']}>
            <UserAccountPage />
          </RoleRoute>
        ),
        handle: { titleKey: 'pages.userAccount.title' } satisfies RouteHandle,
      },
      {
        path: 'coordinamento',
        element: (
          <RoleRoute roles={['TUTOR']}>
            <CoordinationPage />
          </RoleRoute>
        ),
        handle: { titleKey: 'pages.coordination.title' } satisfies RouteHandle,
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
