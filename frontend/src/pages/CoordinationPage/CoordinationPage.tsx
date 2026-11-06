import { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { Button } from '@/components/ui/button';
import type { TransferResult } from '@/config/api/ticketTransferApi';
import { useCoordination } from '@/config/hooks/useTicketTransfer';
import { TransferGroupForm } from '@/organisms/TransferGroupForm/TransferGroupForm';

/**
 * Coordinamento (US-112): il Tutor Coordinatore sposta le segnalazioni aperte dei progetti che coordina tra i Tutor
 * attivi dello stesso progetto, sé compreso; con ogni segnalazione passa anche il beneficiario.
 */
export const CoordinationPage = () => {
  const { t } = useTranslation();
  const groups = useCoordination();
  const [done, setDone] = useState<string | null>(null);
  const doneRef = useRef<HTMLParagraphElement>(null);

  useEffect(() => {
    if (done) doneRef.current?.focus();
  }, [done]);

  const onMoved = (result: TransferResult): void =>
    setDone(t('users.tickets.moved', { count: result.moved, name: result.tutorName }));

  return (
    <div className="space-y-6">
      <PageHeading>{t('coordination.heading')}</PageHeading>
      <p className="text-muted-foreground">{t('coordination.intro')}</p>
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
      {groups.isError && !groups.data ? (
        <div role="alert" className="space-y-3 rounded-md border-2 border-destructive bg-card p-4">
          <p className="font-medium text-destructive">{t('coordination.loadError')}</p>
          <Button variant="outline" onClick={() => void groups.refetch()}>
            {t('common.retry')}
          </Button>
        </div>
      ) : !groups.data ? (
        <p>{t('common.loading')}</p>
      ) : groups.data.length === 0 ? (
        <p>{t('coordination.none')}</p>
      ) : (
        groups.data.map((g) =>
          g.tickets.length === 0 ? (
            <section
              key={g.project.id}
              aria-labelledby={`coordination-${g.project.id}`}
              className="space-y-2 rounded-xl border border-border bg-card p-4"
            >
              <h2 id={`coordination-${g.project.id}`} className="text-lg font-semibold text-primary">
                {g.project.name}
              </h2>
              <p>{t('coordination.empty', { name: g.project.name })}</p>
            </section>
          ) : (
            <TransferGroupForm
              key={`${g.project.id}-${g.tickets.map((x) => `${x.id}:${x.tutorName}`).join()}`}
              group={g}
              onMoved={onMoved}
              showTutor
            />
          ),
        )
      )}
    </div>
  );
};
