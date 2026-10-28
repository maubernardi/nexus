import { useId } from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router';

import type { CompanyHistoryEvent, CompanyHistorySummary } from '@/config/api/companyHistoryApi';
import { useCompanyHistory } from '@/config/hooks/useCompanyHistory';
import { COMPANY_EVENT_TYPES } from '@/lib/domainValues';

const dateTime = new Intl.DateTimeFormat('it-IT', { dateStyle: 'short', timeStyle: 'short' });

const SUMMARY_KEYS: readonly (keyof CompanyHistorySummary)[] = [
  'proposals',
  'accepted',
  'failedAppointments',
  'internships',
  'hirings',
  'negativeOutcomes',
];

/**
 * Storico e pregressi dell'azienda (US-503), in sola lettura: riepilogo sempre completo ed eventi dalla più recente,
 * con la segnalazione collegata e le esclusioni con motivo.
 */
type CompanyHistorySectionProps = {
  companyId: string;
  /** Livello del titolo: 2 nella scheda azienda, 3 dentro il dialogo "Dettagli" (US-604). */
  headingLevel?: 2 | 3;
  /** Se indicato, mostra solo gli eventi più recenti. */
  maxEvents?: number;
};

export const CompanyHistorySection = ({
  companyId,
  headingLevel = 2,
  maxEvents,
}: CompanyHistorySectionProps) => {
  const { t } = useTranslation();
  const history = useCompanyHistory(companyId);
  const headingId = useId();
  const Heading = headingLevel === 2 ? 'h2' : 'h3';
  const events = history.data ? history.data.events.slice(0, maxEvents) : [];

  const typeLabel = (e: CompanyHistoryEvent): string =>
    (COMPANY_EVENT_TYPES as readonly string[]).includes(e.type)
      ? t(`companyHistory.types.${e.type as (typeof COMPANY_EVENT_TYPES)[number]}`)
      : e.type;

  return (
    <section
      aria-labelledby={headingId}
      className="@container space-y-4 rounded-xl border border-border bg-card p-4"
    >
      <Heading id={headingId} className="text-lg font-semibold text-primary">
        {t('companyHistory.heading')}
      </Heading>
      {history.isError ? (
        <p role="alert" className="font-medium text-destructive">
          {t('common.loadError')}
        </p>
      ) : !history.data ? (
        <p>{t('common.loading')}</p>
      ) : (
        <>
          <dl
            aria-label={t('companyHistory.summaryLabel')}
            className="grid grid-cols-2 gap-3 @md:grid-cols-3 @4xl:grid-cols-6"
          >
            {SUMMARY_KEYS.map((key) => (
              <div key={key} className="rounded-md border border-border p-3">
                <dt className="text-sm break-words text-muted-foreground">
                  {t(`companyHistory.summary.${key}`)}
                </dt>
                <dd className="text-2xl font-semibold tabular-nums">{history.data.summary[key]}</dd>
              </div>
            ))}
          </dl>

          {events.length === 0 ? (
            <p>{t('companyHistory.empty')}</p>
          ) : (
            <ol aria-label={t('companyHistory.listLabel')} className="space-y-3">
              {events.map((e, i) => (
                <li key={`${e.at}-${i}`} className="border-l-4 border-primary/40 pl-3">
                  <p className="font-medium">
                    {typeLabel(e)}
                    {e.ticketId && typeof e.ticketNumber === 'number' && (
                      <>
                        {' – '}
                        <Link
                          to={`/segnalazioni/${e.ticketId}`}
                          className="text-primary underline underline-offset-4"
                        >
                          {t('companyHistory.ticket', { number: e.ticketNumber })}
                        </Link>
                      </>
                    )}
                  </p>
                  {e.detail && <p className="break-words">{e.detail}</p>}
                  <p className="text-sm text-muted-foreground">
                    <time dateTime={e.at}>{dateTime.format(new Date(e.at))}</time> · {e.authorName}
                  </p>
                </li>
              ))}
            </ol>
          )}
        </>
      )}
    </section>
  );
};
