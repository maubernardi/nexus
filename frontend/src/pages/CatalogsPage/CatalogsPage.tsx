import { useTranslation } from 'react-i18next';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { CatalogSection } from '@/organisms/CatalogSection/CatalogSection';

/**
 * Cataloghi per l'ADMIN: zone e tipologie di mansione (US-106), tipi di tirocinio (US-108). I valori reali della
 * cooperativa senza rilasci.
 */
export const CatalogsPage = () => {
  const { t } = useTranslation();
  return (
    <div className="space-y-8">
      <PageHeading>{t('pages.catalogs.heading')}</PageHeading>
      <p className="text-muted-foreground">{t('pages.catalogs.intro')}</p>
      <CatalogSection kind="zones" />
      <CatalogSection kind="job-categories" />
      <CatalogSection kind="internship-types" />
    </div>
  );
};
