import { useEffect, useRef } from 'react';
import { Plus } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Link, useLocation } from 'react-router';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { Button } from '@/components/ui/button';
import { useMyContactRequests } from '@/config/hooks/useContactRequests';
import { ContactRequestsTable } from '@/organisms/ContactRequestsTable/ContactRequestsTable';

/** Stato di navigazione dopo un invio: il numero della richiesta appena nata. */
export type ContactRequestSentState = { sent?: number };

/**
 * «Le mie richieste» del Tutor (US-507): le aziende che ha chiesto al Call Center di contattare, dalla più recente, con
 * lo stato e l'operatore che ce l'ha. Dopo un invio mostra l'esito, con il focus.
 */
export const MyContactRequestsPage = () => {
  const { t } = useTranslation();
  const location = useLocation();
  const sent = (location.state as ContactRequestSentState | null)?.sent;
  const requests = useMyContactRequests();
  const sentRef = useRef<HTMLParagraphElement>(null);

  useEffect(() => {
    if (sent) sentRef.current?.focus();
  }, [sent]);

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <PageHeading>{t('pages.myContactRequests.heading')}</PageHeading>
        <Button asChild size="lg">
          <Link to="/richieste/nuova">
            <Plus aria-hidden="true" />
            {t('contactRequest.newRequest')}
          </Link>
        </Button>
      </div>
      <p className="text-muted-foreground">{t('pages.myContactRequests.intro')}</p>

      {sent && (
        <p
          ref={sentRef}
          tabIndex={-1}
          role="status"
          className="rounded-md border-l-4 border-primary bg-card p-4 font-medium"
        >
          {t('contactRequest.sent', { number: sent })}
        </p>
      )}

      {requests.isError ? (
        <div role="alert" className="space-y-3 rounded-md border-2 border-destructive bg-card p-4">
          <p className="font-medium text-destructive">{t('contactRequest.loadError')}</p>
          <Button variant="outline" onClick={() => void requests.refetch()}>
            {t('common.retry')}
          </Button>
        </div>
      ) : !requests.data ? (
        <p>{t('common.loading')}</p>
      ) : requests.data.length === 0 ? (
        <p className="rounded-xl border border-border bg-card p-6">{t('contactRequest.myEmpty')}</p>
      ) : (
        <ContactRequestsTable requests={requests.data} caption={t('contactRequest.myCaption')} mode="tutor" />
      )}
    </div>
  );
};
