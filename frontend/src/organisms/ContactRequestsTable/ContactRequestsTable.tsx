import { useTranslation } from 'react-i18next';
import { Link } from 'react-router';

import { Button } from '@/components/ui/button';
import type { ContactRequest } from '@/config/api/contactRequestApi';
import { COMPANY_NAME_COLUMN } from '@/lib/companyColumns';
import { cn } from '@/lib/utils';

const dateTime = new Intl.DateTimeFormat('it-IT', { dateStyle: 'short', timeStyle: 'short' });

type ContactRequestsTableProps = {
  requests: ContactRequest[];
  caption: string;
  /** `tutor`: le proprie richieste; `callCenter`: anche il Tutor e la presa in carico. */
  mode: 'tutor' | 'callCenter';
  /** Solo per il Call Center: prende in carico una richiesta aperta. */
  onTake?: (request: ContactRequest) => void;
  /** Richiesta in corso di presa in carico: i pulsanti sono disattivati. */
  pendingId?: string | null;
};

/**
 * Richieste di contatto di un'azienda (US-507), per il Tutor («Le mie richieste») e per il Call Center: l'azienda già
 * presente apre la sua scheda; quella nuova ha il nome scritto dal Tutor e l'etichetta «nuova». Lo stato è in testo.
 */
export const ContactRequestsTable = ({
  requests,
  caption,
  mode,
  onTake,
  pendingId = null,
}: ContactRequestsTableProps) => {
  const { t } = useTranslation();
  const th = 'px-3 py-2 text-left text-sm font-semibold whitespace-nowrap';
  const td = 'px-3 py-2 align-top';
  const callCenter = mode === 'callCenter';

  return (
    <div
      role="region"
      aria-labelledby="contact-requests-caption"
      // eslint-disable-next-line jsx-a11y/no-noninteractive-tabindex -- regione scorrevole: deve essere raggiungibile da tastiera
      tabIndex={0}
      className="overflow-x-auto rounded-xl border border-border bg-card focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
    >
      <table className="w-full border-collapse text-sm max-lg:w-max max-lg:whitespace-nowrap">
        <caption id="contact-requests-caption" className="sr-only">
          {caption}
        </caption>
        <thead className="border-b border-border bg-muted">
          <tr>
            <th scope="col" className={th}>
              {t('contactRequest.columns.number')}
            </th>
            <th scope="col" className={cn(th, COMPANY_NAME_COLUMN)}>
              {t('contactRequest.columns.company')}
            </th>
            <th scope="col" className={th}>
              {t('contactRequest.columns.zone')}
            </th>
            <th scope="col" className={th}>
              {t('contactRequest.columns.category')}
            </th>
            {callCenter && (
              <th scope="col" className={th}>
                {t('contactRequest.columns.tutor')}
              </th>
            )}
            <th scope="col" className={th}>
              {t('contactRequest.columns.date')}
            </th>
            <th scope="col" className={th}>
              {t('contactRequest.columns.status')}
            </th>
            <th scope="col" className={th}>
              {t('contactRequest.columns.operator')}
            </th>
          </tr>
        </thead>
        <tbody>
          {requests.map((r) => (
            <tr key={r.id} className="border-b border-border last:border-b-0">
              <td className={`${td} tabular-nums`}>{r.number}</td>
              <th scope="row" className={`${td} text-left font-semibold`}>
                {r.companyId ? (
                  <Link to={`/aziende/${r.companyId}`} className="text-primary underline underline-offset-4">
                    {r.companyName}
                  </Link>
                ) : (
                  <>
                    {r.companyName}{' '}
                    <span className="inline-block rounded-full border border-input px-2 py-0.5 text-xs font-semibold whitespace-nowrap">
                      <span aria-hidden="true">{t('contactRequest.newCompany')}</span>
                      <span className="sr-only">{t('contactRequest.newCompanyLabel')}</span>
                    </span>
                  </>
                )}
              </th>
              <td className={td}>{r.zone?.name ?? t('contactRequest.none')}</td>
              <td className={td}>{r.businessCategory?.name ?? t('contactRequest.none')}</td>
              {callCenter && <td className={td}>{r.requestedByName}</td>}
              <td className={`${td} whitespace-nowrap tabular-nums`}>
                <time dateTime={r.createdAt}>{dateTime.format(new Date(r.createdAt))}</time>
              </td>
              <td className={`${td} whitespace-nowrap`}>{t(`contactRequest.status.${r.status}`)}</td>
              <td className={td}>
                {r.assignedOperatorName ??
                  (callCenter && onTake && r.status === 'APERTA' ? (
                    <Button
                      size="sm"
                      disabled={pendingId !== null}
                      aria-busy={pendingId === r.id}
                      onClick={() => onTake(r)}
                    >
                      {t('contactRequest.take')}
                      {/* spazio come nodo a sé: «Prendi in carico la richiesta di contatto n. 3» */}{' '}
                      <span className="sr-only">{t('contactRequest.takeTarget', { number: r.number })}</span>
                    </Button>
                  ) : (
                    t('contactRequest.none')
                  ))}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};
