import { useId, useState } from 'react';
import { useTranslation } from 'react-i18next';

import { ReviewFace } from '@/atoms/ReviewFace/ReviewFace';
import { useCompanyReviews } from '@/config/hooks/useCompanies';
import { formatIsoDate } from '@/lib/isoDate';
import { Pagination } from '@/molecules/Pagination/Pagination';

type CompanyReviewsSectionProps = {
  companyId: string;
};

/**
 * Valutazioni dei tirocini dell'azienda (US-1104): la media delle medie come faccina, con quanti tirocini sono stati
 * valutati, e l'elenco dalla più recente, 10 per pagina, con data di fine, faccina, nota e nome del Tutor (PO, 26/11).
 * Nessun dato del beneficiario. La vedono Call Center, ADMIN e Tutor.
 */
export const CompanyReviewsSection = ({ companyId }: CompanyReviewsSectionProps) => {
  const { t } = useTranslation();
  const headingId = useId();
  const [page, setPage] = useState(1);
  const reviews = useCompanyReviews(companyId, page - 1);
  const data = reviews.data;
  const pageCount = data ? Math.max(1, Math.ceil(data.total / data.size)) : 1;

  return (
    <section aria-labelledby={headingId} className="space-y-4 rounded-xl border border-border bg-card p-4">
      <h2 id={headingId} className="text-lg font-semibold text-primary">
        {t('companyReviews.heading')}
      </h2>
      <p className="text-sm text-muted-foreground">{t('companyReviews.intro')}</p>
      {reviews.isError && !data ? (
        <p role="alert" className="font-medium text-destructive">
          {t('companyReviews.loadError')}
        </p>
      ) : !data ? (
        <p>{t('common.loading')}</p>
      ) : data.total === 0 || data.average === undefined || data.average === null ? (
        <p>{t('companyReviews.empty')}</p>
      ) : (
        <>
          <p className="flex flex-wrap items-center gap-2 text-base font-semibold">
            <ReviewFace average={data.average} withWord className="text-lg" />
            <span>{t('companyReviews.summary', { count: data.total })}</span>
          </p>
          <ol aria-label={t('companyReviews.listLabel')} className="space-y-3">
            {data.reviews.map((r, i) => (
              <li key={`${r.endDate}-${i}`} className="space-y-1 border-l-4 border-primary/40 pl-3">
                <p className="flex flex-wrap items-center gap-x-3 gap-y-1">
                  <ReviewFace average={r.average} withWord className="font-medium" />
                  <span className="text-sm text-muted-foreground">
                    {t('companyReviews.endDate', { date: formatIsoDate(r.endDate) })}
                  </span>
                </p>
                {r.note && <p className="whitespace-pre-line">{r.note}</p>}
                <p className="text-sm text-muted-foreground">
                  {t('companyReviews.by', { name: r.tutorName })}
                </p>
              </li>
            ))}
          </ol>
          {data.total > data.size && (
            <Pagination
              id={`${headingId}-size`}
              page={page}
              pageCount={pageCount}
              pageSize={data.size}
              pageSizes={[data.size]}
              label={t('companyReviews.pages')}
              pageSizeLabel={t('companyReviews.pageSize')}
              onPageChange={setPage}
              onPageSizeChange={() => undefined}
            />
          )}
        </>
      )}
    </section>
  );
};
