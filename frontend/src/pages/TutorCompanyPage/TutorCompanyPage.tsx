import { useTranslation } from 'react-i18next';
import { Link, useParams } from 'react-router';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { RatingDisplay } from '@/atoms/RatingDisplay/RatingDisplay';
import { TrainingShipBadge } from '@/atoms/TrainingShipBadge/TrainingShipBadge';
import { Button } from '@/components/ui/button';
import { apiError } from '@/config/api/errors';
import { useCompanyDirectoryItem } from '@/config/hooks/useCompanies';
import { BackLink } from '@/molecules/BackLink/BackLink';
import { PageHeader } from '@/molecules/PageHeader/PageHeader';
import { CompanyReviewsSection } from '@/organisms/CompanyReviewsSection/CompanyReviewsSection';

/**
 * Scheda di un'azienda per il Tutor (US-1104), in sola lettura: i dati della sua lista (US-511) e le valutazioni dei
 * tirocini, con il nome del Tutor che le ha date. Niente partita IVA, recapiti, referenti, note o mansioni.
 */
export const TutorCompanyPage = () => {
  const { t } = useTranslation();
  const { id = '' } = useParams();
  const company = useCompanyDirectoryItem(id);

  if (company.isError) {
    const notFound = apiError(company.error)?.status === 404;
    return (
      <div className="space-y-4">
        <PageHeading>{t(notFound ? 'company.notFound' : 'company.loadError')}</PageHeading>
        <Button asChild size="lg" variant="outline">
          <Link to="/aziende">{t('company.backToList')}</Link>
        </Button>
      </div>
    );
  }
  if (!company.data) {
    return <p role="status">{t('common.loading')}</p>;
  }
  const c = company.data;
  const address = [c.address, c.municipality].filter(Boolean).join(', ');
  const rows = [
    { label: t('company.category'), value: c.businessCategory?.name ?? '—' },
    { label: t('company.fields.zone'), value: c.zone?.name ?? '—' },
    { label: t('companyDirectory.address'), value: address || '—' },
    { label: t('company.fields.reliability'), value: <RatingDisplay kind="stars" value={c.reliability} /> },
    {
      label: t('company.fields.projectInterest'),
      value: <RatingDisplay kind="thumbs" value={c.projectInterest} />,
    },
  ];

  return (
    <div className="space-y-6">
      <PageHeader back={<BackLink to="/aziende">{t('company.backToList')}</BackLink>}>
        {c.name}
        {c.trainingShip && (
          <>
            {' '}
            <TrainingShipBadge size="lg" />
          </>
        )}
      </PageHeader>
      <section
        aria-labelledby="tutor-company-data"
        className="space-y-3 rounded-xl border border-border bg-card p-4"
      >
        <h2 id="tutor-company-data" className="text-lg font-semibold text-primary">
          {t('company.sections.data')}
        </h2>
        <dl className="grid gap-x-6 gap-y-3 sm:grid-cols-[max-content_1fr]">
          {rows.map((row) => (
            <div key={row.label} className="contents">
              <dt className="font-medium">{row.label}</dt>
              <dd>{row.value}</dd>
            </div>
          ))}
        </dl>
      </section>
      <CompanyReviewsSection companyId={c.id} />
    </div>
  );
};
