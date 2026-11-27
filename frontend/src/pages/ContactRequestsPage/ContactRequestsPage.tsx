import { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { Button } from '@/components/ui/button';
import type { ContactRequest } from '@/config/api/contactRequestApi';
import { apiError } from '@/config/api/errors';
import { useContactRequestQueue, useTakeContactRequest } from '@/config/hooks/useContactRequests';
import { ContactRequestsTable } from '@/organisms/ContactRequestsTable/ContactRequestsTable';

type Outcome = { kind: 'taken' | 'error'; message: string };

/**
 * «Richieste di contatto» del Call Center (US-507): le richieste aperte e in carico, dalla più vecchia. Un operatore
 * qualsiasi prende in carico una richiesta aperta; se un altro l'ha già presa, l'avviso dice chi ce l'ha.
 */
export const ContactRequestsPage = () => {
  const { t } = useTranslation();
  const requests = useContactRequestQueue();
  const take = useTakeContactRequest();
  const [outcome, setOutcome] = useState<Outcome | null>(null);
  const outcomeRef = useRef<HTMLParagraphElement>(null);

  useEffect(() => {
    if (outcome) outcomeRef.current?.focus();
  }, [outcome]);

  const onTake = (request: ContactRequest): void => {
    setOutcome(null);
    take.mutate(
      { id: request.id, version: request.version },
      {
        onSuccess: () =>
          setOutcome({ kind: 'taken', message: t('contactRequest.taken', { number: request.number }) }),
        onError: (error) => {
          const body = apiError(error);
          // il server dice chi ce l'ha già: «… è già in carico a …»
          setOutcome({
            kind: 'error',
            message:
              body?.status === 409 && body.message
                ? body.message
                : t('contactRequest.takeError', { number: request.number }),
          });
        },
      },
    );
  };

  const open = requests.data?.filter((r) => r.status === 'APERTA').length ?? 0;

  return (
    <div className="space-y-6">
      <PageHeading>{t('pages.contactRequests.heading')}</PageHeading>
      <p className="text-muted-foreground">{t('pages.contactRequests.intro')}</p>

      {outcome && (
        <p
          ref={outcomeRef}
          tabIndex={-1}
          role={outcome.kind === 'taken' ? 'status' : 'alert'}
          className={
            outcome.kind === 'taken'
              ? 'rounded-md border-l-4 border-primary bg-card p-4 font-medium'
              : 'rounded-md border-2 border-destructive bg-card p-4 font-medium text-destructive'
          }
        >
          {outcome.message}
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
        <p className="rounded-xl border border-border bg-card p-6">{t('contactRequest.queueEmpty')}</p>
      ) : (
        <>
          <p className="font-medium">
            {t('contactRequest.count', { count: requests.data.length })}:{' '}
            {t('contactRequest.openCount', { count: open })}
          </p>
          <ContactRequestsTable
            requests={requests.data}
            caption={t('contactRequest.queueCaption')}
            mode="callCenter"
            onTake={onTake}
            pendingId={take.isPending ? (take.variables?.id ?? null) : null}
          />
        </>
      )}
    </div>
  );
};
