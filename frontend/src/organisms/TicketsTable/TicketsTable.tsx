import { Zap } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router';

import { Button } from '@/components/ui/button';
import type { TicketListItem } from '@/config/api/ticketApi';

const dateTime = new Intl.DateTimeFormat('it-IT', { dateStyle: 'short', timeStyle: 'short' });

/** Colonne facoltative: dipendono dal ruolo e dalla vista. */
export type TicketsTableColumns = {
  priority?: boolean;
  zone?: boolean;
  tutor?: boolean;
  operator?: boolean;
};

type TicketsTableProps = {
  items: TicketListItem[];
  /** Didascalia della tabella (nome della regione scorrevole). */
  caption: string;
  columns?: TicketsTableColumns;
  /** Se presente, il numero è un link (lavorazione o dettaglio della segnalazione). */
  rowLink?: (item: TicketListItem) => string;
  /** Se presente, ogni riga ha il pulsante "Prendi in carico". */
  onTakeCharge?: (item: TicketListItem) => void;
  /** Riga la cui presa in carico è in corso (pulsanti disabilitati). */
  pendingId?: string | null;
};

/**
 * Tabella delle segnalazioni (Home, US-303). Scorre orizzontalmente dentro il proprio contenitore, focalizzabile da
 * tastiera, così la pagina non scorre mai di lato (WCAG 1.4.10 ammette lo scorrimento per le tabelle dati).
 */
export const TicketsTable = ({
  items,
  caption,
  columns = {},
  rowLink,
  onTakeCharge,
  pendingId = null,
}: TicketsTableProps) => {
  const { t } = useTranslation();
  const th = 'px-3 py-2 text-left text-sm font-semibold whitespace-nowrap';
  const td = 'px-3 py-2 align-top';

  return (
    // relative: i testi sr-only (posizionati in assoluto) restano dentro la regione scorrevole, non allargano la pagina
    <div
      role="region"
      aria-labelledby="tickets-caption"
      // eslint-disable-next-line jsx-a11y/no-noninteractive-tabindex -- regione scorrevole: deve essere raggiungibile da tastiera
      tabIndex={0}
      className="relative overflow-x-auto rounded-xl border border-border bg-card focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
    >
      {/* sotto i 1024 px niente a capo: la tabella prende la larghezza naturale e scorre, invece di righe altissime */}
      <table className="w-full border-collapse text-sm max-lg:w-max max-lg:whitespace-nowrap">
        <caption id="tickets-caption" className="sr-only">
          {caption}
        </caption>
        <thead className="border-b border-border bg-muted">
          <tr>
            <th scope="col" className={th}>
              {t('tickets.columns.number')}
            </th>
            {columns.priority && (
              <th scope="col" className={th}>
                {t('tickets.columns.priority')}
              </th>
            )}
            <th scope="col" className={th}>
              {t('tickets.columns.beneficiary')}
            </th>
            {columns.zone && (
              <th scope="col" className={th}>
                {t('tickets.columns.zone')}
              </th>
            )}
            <th scope="col" className={th}>
              {t('tickets.columns.project')}
            </th>
            <th scope="col" className={th}>
              {t('tickets.columns.jobCategory')}
            </th>
            {columns.tutor && (
              <th scope="col" className={th}>
                {t('tickets.columns.tutor')}
              </th>
            )}
            {columns.operator && (
              <th scope="col" className={th}>
                {t('tickets.columns.operator')}
              </th>
            )}
            <th scope="col" className={th}>
              {t('tickets.columns.status')}
            </th>
            <th scope="col" className={th}>
              {t('tickets.columns.createdAt')}
            </th>
            {onTakeCharge && (
              <th scope="col" className={th}>
                {t('tickets.columns.actions')}
              </th>
            )}
          </tr>
        </thead>
        <tbody>
          {items.map((item) => (
            <tr key={item.id} className="border-b border-border last:border-b-0">
              <th scope="row" className={`${td} text-left font-semibold tabular-nums`}>
                {rowLink ? (
                  // aria-label invece di un testo nascosto: un elemento sr-only spostava l'area cliccabile del link;
                  // il nome contiene il numero visibile (WCAG 2.5.3)
                  <Link
                    to={rowLink(item)}
                    aria-label={t('tickets.openNumber', { number: item.number })}
                    className="text-primary underline underline-offset-4"
                  >
                    {item.number}
                  </Link>
                ) : (
                  item.number
                )}
              </th>
              {columns.priority && (
                <td className={td}>
                  {item.fastTrack ? (
                    <span className="inline-flex items-center gap-1 rounded-full bg-gold px-2 py-0.5 text-xs font-semibold whitespace-nowrap text-brand">
                      <Zap aria-hidden="true" className="size-3.5" />
                      {t('tickets.fastTrack')}
                    </span>
                  ) : (
                    <span className="text-muted-foreground">{t('tickets.normal')}</span>
                  )}
                </td>
              )}
              <td className={td}>
                {item.beneficiary.lastName} {item.beneficiary.firstName}
              </td>
              {columns.zone && (
                <td className={`${td} whitespace-nowrap`}>{item.beneficiary.residenceZoneName}</td>
              )}
              <td className={td}>{item.project.name}</td>
              <td className={td}>
                {item.requestedJobCategory?.name ??
                  (item.requestedJobFreeText
                    ? t('tickets.newCategory', { name: item.requestedJobFreeText })
                    : '—')}
              </td>
              {columns.tutor && <td className={`${td} whitespace-nowrap`}>{item.tutorName}</td>}
              {columns.operator && (
                <td className={`${td} whitespace-nowrap`}>
                  {item.assignedOperatorName ?? t('tickets.nobody')}
                </td>
              )}
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
                    {t('tickets.takeCharge')}
                    {/* spazio come nodo a sé: nome accessibile "Prendi in carico la segnalazione n. 12" */}{' '}
                    <span className="sr-only">{t('tickets.takeChargeTarget', { number: item.number })}</span>
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
