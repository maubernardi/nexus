import { useEffect, useMemo, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useSearchParams } from 'react-router';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { RetentionBadge } from '@/atoms/RetentionBadge/RetentionBadge';
import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import { useAllBeneficiaries } from '@/config/hooks/useBeneficiaries';
import { cn } from '@/lib/utils';
import { RetentionDecision } from '@/organisms/RetentionDecision/RetentionDecision';

const TO_ANONYMIZE_PARAM = 'anonimizzare';
const date = new Intl.DateTimeFormat('it-IT', { dateStyle: 'short', timeZone: 'Europe/Rome' });

/**
 * Beneficiari di tutti, per l'ADMIN (US-1303): quelli da anonimizzare sono evidenziati e si filtrano; l'ADMIN li
 * anonimizza o ne tiene i dati con un motivo, come il loro Tutor.
 */
export const BeneficiariesAdminPage = () => {
  const { t } = useTranslation();
  const [searchParams, setSearchParams] = useSearchParams();
  const onlyToAnonymize = searchParams.get(TO_ANONYMIZE_PARAM) === '1';
  const beneficiaries = useAllBeneficiaries();
  const [done, setDone] = useState<string | null>(null);
  const doneRef = useRef<HTMLParagraphElement>(null);

  useEffect(() => {
    if (done) doneRef.current?.focus();
  }, [done]);

  const all = beneficiaries.data ?? [];
  const toAnonymize = all.filter((b) => b.retention === 'TO_ANONYMIZE').length;
  const shown = useMemo(
    () => (beneficiaries.data ?? []).filter((b) => !onlyToAnonymize || b.retention === 'TO_ANONYMIZE'),
    [beneficiaries.data, onlyToAnonymize],
  );

  const th = 'px-3 py-2 text-left text-sm font-semibold whitespace-nowrap';
  const td = 'px-3 py-2 align-top';

  return (
    <div className="space-y-6">
      <PageHeading>{t('retention.adminHeading')}</PageHeading>
      <p className="text-muted-foreground">{t('retention.adminIntro')}</p>

      <label
        htmlFor="all-beneficiaries-to-anonymize"
        className="flex min-h-11 items-center gap-3 rounded-xl border border-border bg-card px-4"
      >
        <Checkbox
          id="all-beneficiaries-to-anonymize"
          checked={onlyToAnonymize}
          onChange={(e) => setSearchParams(e.target.checked ? { [TO_ANONYMIZE_PARAM]: '1' } : {})}
        />
        <span>{t('retention.filter', { count: toAnonymize })}</span>
      </label>

      {done && (
        <p
          ref={doneRef}
          role="status"
          tabIndex={-1}
          className="rounded-md border-l-4 border-primary bg-card p-4 font-medium"
        >
          {done}
        </p>
      )}

      {beneficiaries.isError && !beneficiaries.data ? (
        <div role="alert" className="space-y-3 rounded-md border-2 border-destructive bg-card p-4">
          <p className="font-medium text-destructive">{t('common.loadError')}</p>
          <Button variant="outline" onClick={() => void beneficiaries.refetch()}>
            {t('common.retry')}
          </Button>
        </div>
      ) : !beneficiaries.data ? (
        <p>{t('common.loading')}</p>
      ) : shown.length === 0 ? (
        <p className="rounded-xl border border-border bg-card p-6">
          {t(onlyToAnonymize ? 'retention.noneToAnonymize' : 'beneficiary.empty')}
        </p>
      ) : (
        <div
          role="region"
          aria-labelledby="all-beneficiaries-caption"
          // eslint-disable-next-line jsx-a11y/no-noninteractive-tabindex -- regione scorrevole: deve essere raggiungibile da tastiera
          tabIndex={0}
          className="relative overflow-x-auto rounded-xl border border-border bg-card focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
        >
          <table className="w-full border-collapse text-sm max-lg:w-max max-lg:whitespace-nowrap">
            <caption id="all-beneficiaries-caption" className="sr-only">
              {t('retention.adminCaption', { count: shown.length })}
            </caption>
            <thead className="border-b border-border bg-muted">
              <tr>
                <th scope="col" className={th}>
                  {t('beneficiary.name')}
                </th>
                <th scope="col" className={th}>
                  {t('beneficiary.fields.birthYear')}
                </th>
                <th scope="col" className={th}>
                  {t('beneficiary.fields.residenceZone')}
                </th>
                <th scope="col" className={th}>
                  {t('retention.tutor')}
                </th>
                <th scope="col" className={th}>
                  {t('retention.column')}
                </th>
              </tr>
            </thead>
            <tbody>
              {shown.map((b) => (
                <tr
                  key={b.id}
                  className={cn(
                    'border-b border-border last:border-b-0',
                    b.retention === 'TO_ANONYMIZE' && 'bg-warning text-warning-foreground',
                  )}
                >
                  <th scope="row" className={`${td} text-left font-semibold`}>
                    <Link to={`/beneficiari/${b.id}`} className="text-primary underline underline-offset-4">
                      {b.lastName} {b.firstName}
                    </Link>
                  </th>
                  <td className={`${td} tabular-nums`}>{b.birthYear}</td>
                  <td className={td}>{b.residenceZoneName}</td>
                  <td className={td}>{b.ownerTutorName}</td>
                  <td className={`${td} space-y-2`}>
                    <RetentionBadge status={b.retention} />
                    {b.retention === 'TO_ANONYMIZE' && b.anonymizationDueSince && (
                      <p className="text-xs">
                        {t('retention.dueSince', { date: date.format(new Date(b.anonymizationDueSince)) })}
                      </p>
                    )}
                    {b.retention === 'KEPT' && b.keptReason && (
                      <p className="text-xs">{t('retention.keptReason', { reason: b.keptReason })}</p>
                    )}
                    {b.retention === 'TO_ANONYMIZE' && (
                      <RetentionDecision id={b.id} name={`${b.lastName} ${b.firstName}`} onDone={setDone} />
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};
