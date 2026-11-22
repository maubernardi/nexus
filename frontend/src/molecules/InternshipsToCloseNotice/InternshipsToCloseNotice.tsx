import { CalendarX } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router';

import type { InternshipToClose } from '@/config/api/ticketApi';
import { formatIsoDate } from '@/lib/isoDate';

type InternshipsToCloseNoticeProps = {
  items: InternshipToClose[];
};

/**
 * Tirocini finiti alla data prevista e non chiusi (US-1203): finché ce ne sono, il Tutor non apre nuove segnalazioni.
 * Ogni voce porta alla segnalazione, dove si chiude il tirocinio o si registra la proroga.
 */
export const InternshipsToCloseNotice = ({ items }: InternshipsToCloseNoticeProps) => {
  const { t } = useTranslation();

  return (
    <section
      aria-labelledby="internships-to-close"
      className="space-y-3 rounded-xl border border-l-4 border-border border-l-destructive bg-card p-6"
    >
      <h2 id="internships-to-close" className="flex items-center gap-2 text-xl font-semibold">
        <CalendarX aria-hidden="true" className="size-6 shrink-0 text-destructive" />
        {t('internshipsToClose.heading')}
      </h2>
      <p>{t('internshipsToClose.intro')}</p>
      <ul className="list-disc space-y-1 pl-6">
        {items.map((item) => (
          <li key={item.ticketId}>
            <Link
              to={`/segnalazioni/${item.ticketId}`}
              className="font-medium text-primary underline underline-offset-4"
            >
              {t('internshipsToClose.item', { number: item.ticketNumber, date: formatIsoDate(item.endDate) })}
            </Link>
          </li>
        ))}
      </ul>
    </section>
  );
};
