import { useTranslation } from 'react-i18next';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { CatalogSection } from '@/organisms/CatalogSection/CatalogSection';
import {
  CareAgenciesSection,
  CompetenceSitesSection,
  ProjectInternshipTypesSection,
} from '@/organisms/DocumentCatalogSections/DocumentCatalogSections';
import { MunicipalitiesSection } from '@/organisms/MunicipalitiesSection/MunicipalitiesSection';

/**
 * Cataloghi per l'ADMIN: progetti (US-102), zone e tipologie di mansione (US-106), tipi di tirocinio (US-108), formule di assunzione e
 * motivi della mancata assunzione del report di restituzione (US-109), comuni e province dell'ISTAT (US-114), sedi di competenza, tipi per progetto ed enti di presa in carico (US-115). I valori reali della cooperativa senza rilasci.
 */
export const CatalogsPage = () => {
  const { t } = useTranslation();
  return (
    <div className="space-y-8">
      <PageHeading>{t('pages.catalogs.heading')}</PageHeading>
      <p className="text-muted-foreground">{t('pages.catalogs.intro')}</p>
      <CatalogSection kind="projects" />
      <CatalogSection kind="zones" />
      <CatalogSection kind="job-categories" />
      <CatalogSection kind="internship-types" />
      <ProjectInternshipTypesSection />
      <CompetenceSitesSection />
      <CareAgenciesSection />
      <CatalogSection kind="hiring-formulas" />
      <CatalogSection kind="no-hire-reasons" />
      <MunicipalitiesSection />
    </div>
  );
};
