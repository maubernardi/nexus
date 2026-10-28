import { useTranslation } from 'react-i18next';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { CatalogSection } from '@/organisms/CatalogSection/CatalogSection';

/** Cataloghi di zone e tipologie di mansione per l'ADMIN (US-106): i valori reali della cooperativa senza rilasci. */
export const CatalogsPage = () => {
  const { t } = useTranslation();
  return (
    <div className="space-y-8">
      <PageHeading>{t('pages.catalogs.heading')}</PageHeading>
      <p className="text-muted-foreground">{t('pages.catalogs.intro')}</p>
      <CatalogSection kind="zones" />
      <CatalogSection kind="job-categories" />
    </div>
  );
};
