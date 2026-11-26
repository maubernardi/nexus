import { useCurrentUser } from '@/config/hooks/useCurrentUser';
import { CompaniesPage } from '@/pages/CompaniesPage/CompaniesPage';
import { CompanyEditPage } from '@/pages/CompanyEditPage/CompanyEditPage';
import { ForbiddenPage } from '@/pages/ForbiddenPage/ForbiddenPage';
import { TutorCompaniesPage } from '@/pages/TutorCompaniesPage/TutorCompaniesPage';
import { TutorCompanyPage } from '@/pages/TutorCompanyPage/TutorCompanyPage';

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

/**
 * Scheda di un'azienda (US-1104): Call Center e ADMIN la modificano; il Tutor la vede in sola lettura, con le valutazioni
 * dei tirocini.
 */
export const CompanyRoute = () => {
  const { data: user } = useCurrentUser();
  const roles = user?.roles ?? [];
  if (roles.includes('CALL_CENTER') || roles.includes('ADMIN')) return <CompanyEditPage />;
  if (roles.includes('TUTOR')) return <TutorCompanyPage />;
  return <ForbiddenPage />;
};
