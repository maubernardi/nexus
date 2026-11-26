import { useCurrentUser } from '@/config/hooks/useCurrentUser';
import { CompaniesPage } from '@/pages/CompaniesPage/CompaniesPage';
import { ForbiddenPage } from '@/pages/ForbiddenPage/ForbiddenPage';
import { TutorCompaniesPage } from '@/pages/TutorCompaniesPage/TutorCompaniesPage';

/**
 * «Aziende» (US-511): Call Center e ADMIN hanno l'anagrafica completa; il Tutor l'elenco in sola lettura con i soli dati
 * essenziali. Stesso indirizzo, pagina scelta in base al ruolo.
 */
export const CompaniesRoute = () => {
  const { data: user } = useCurrentUser();
  const roles = user?.roles ?? [];
  if (roles.includes('CALL_CENTER') || roles.includes('ADMIN')) return <CompaniesPage />;
  if (roles.includes('TUTOR')) return <TutorCompaniesPage />;
  return <ForbiddenPage />;
};
