import { Zap } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router';

import { Button } from '@/components/ui/button';
import type { QueueItem } from '@/config/api/ticketApi';

const dateTime = new Intl.DateTimeFormat('it-IT', { dateStyle: 'short', timeStyle: 'short' });

type QueueTableProps = {
  items: QueueItem[];
  /** Didascalia della tabella (nome della regione scorrevole). */
  caption: string;
  /** Se presente, ogni riga ha il pulsante "Prendi in carico". */
  onTakeCharge?: (item: QueueItem) => void;
  /** Riga la cui presa in carico è in corso (pulsante disabilitato). */
  pendingId?: string | null;
  /** Se presente, il numero della segnalazione è un link alla sua lavorazione. */
  rowLink?: (item: QueueItem) => string;
};

/**
 * Tabella dati della coda (US-401). Scorre orizzontalmente dentro il proprio contenitore, focalizzabile da tastiera,
 * così la pagina non scorre mai di lato (WCAG 1.4.10 ammette lo scorrimento bidimensionale per le tabelle dati).
 */
export const QueueTable = ({ items, caption, onTakeCharge, pendingId = null, rowLink }: QueueTableProps) => {
  const { t } = useTranslation();
  const th = 'px-3 py-2 text-left text-sm font-semibold whitespace-nowrap';
  const td = 'px-3 py-2 align-top';

  return (
    // relative: i testi sr-only (posizionati in assoluto) restano dentro la regione scorrevole, non allargano la pagina
    <div
      role="region"
      aria-labelledby="queue-caption"
      // eslint-disable-next-line jsx-a11y/no-noninteractive-tabindex -- regione scorrevole: deve essere raggiungibile da tastiera
      tabIndex={0}
      className="relative overflow-x-auto rounded-xl border border-border bg-card focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
    >
      {/* sotto i 1024 px niente a capo: la tabella prende la larghezza naturale e scorre, invece di righe altissime */}
      <table className="w-full border-collapse text-sm max-lg:w-max max-lg:whitespace-nowrap">
        {/* didascalia per le tecnologie assistive: il contesto è spiegato anche nell'introduzione della pagina */}
        <caption id="queue-caption" className="sr-only">
          {caption}
        </caption>
        <thead className="border-b border-border bg-muted">
          <tr>
            <th scope="col" className={th}>
              {t('queue.columns.number')}
            </th>
            <th scope="col" className={th}>
              {t('queue.columns.priority')}
            </th>
            <th scope="col" className={th}>
              {t('queue.columns.beneficiary')}
            </th>
            <th scope="col" className={th}>
              {t('queue.columns.zone')}
            </th>
            <th scope="col" className={th}>
              {t('queue.columns.project')}
            </th>
            <th scope="col" className={th}>
              {t('queue.columns.jobCategory')}
            </th>
            <th scope="col" className={th}>
              {t('queue.columns.tutor')}
            </th>
            <th scope="col" className={th}>
              {t('queue.columns.status')}
            </th>
            <th scope="col" className={th}>
              {t('queue.columns.arrivedAt')}
            </th>
            {onTakeCharge && (
              <th scope="col" className={th}>
                {t('queue.columns.actions')}
              </th>
            )}
          </tr>
        </thead>
        <tbody>
          {items.map((item) => (
            <tr key={item.id} className="border-b border-border last:border-b-0">
              <th scope="row" className={`${td} text-left font-semibold tabular-nums`}>
                {rowLink ? (
                  <Link to={rowLink(item)} className="text-primary underline underline-offset-4">
                    {/* nome accessibile "Apri la segnalazione n. 12" */}
                    <span className="sr-only">{t('queue.openTicket')}</span> {item.number}
                  </Link>
                ) : (
                  item.number
                )}
              </th>
              <td className={td}>
                {item.fastTrack ? (
                  <span className="inline-flex items-center gap-1 rounded-full bg-gold px-2 py-0.5 text-xs font-semibold whitespace-nowrap text-brand">
                    <Zap aria-hidden="true" className="size-3.5" />
                    {t('queue.fastTrack')}
                  </span>
                ) : (
                  <span className="text-muted-foreground">{t('queue.normal')}</span>
                )}
              </td>
              <td className={td}>
                {item.beneficiary.lastName} {item.beneficiary.firstName}
              </td>
              <td className={`${td} whitespace-nowrap`}>{item.beneficiary.residenceZoneName}</td>
              <td className={td}>{item.project.name}</td>
              <td className={td}>{item.requestedJobCategory?.name ?? '—'}</td>
              <td className={`${td} whitespace-nowrap`}>{item.tutorName}</td>
              <td className={`${td} whitespace-nowrap`}>{t(`enums.ticketStatus.${item.status}`)}</td>
              <td className={`${td} whitespace-nowrap tabular-nums`}>
                <time dateTime={item.createdAt}>{dateTime.format(new Date(item.createdAt))}</time>
              </td>
              {onTakeCharge && (
                <td className={td}>
                  <Button
                    size="sm"
                    disabled={pendingId !== null}
                    aria-busy={pendingId === item.id}
                    onClick={() => onTakeCharge(item)}
                  >
                    {t('queue.takeCharge')}
                    {/* nome accessibile univoco per riga ("Prendi in carico la segnalazione n. 12"); lo spazio è un
                        nodo a sé perché dentro lo span verrebbe perso */}{' '}
                    <span className="sr-only">{t('queue.takeChargeTarget', { number: item.number })}</span>
                  </Button>
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};
