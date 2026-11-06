import { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useParams } from 'react-router';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { Button } from '@/components/ui/button';
import type { TransferResult } from '@/config/api/ticketTransferApi';
import { useOpenTickets } from '@/config/hooks/useTicketTransfer';
import { useUser } from '@/config/hooks/useUsers';
import { TransferGroupForm } from '@/organisms/TransferGroupForm/TransferGroupForm';

const link = 'font-medium text-primary underline underline-offset-4';

/**
 * Segnalazioni aperte di un Tutor (US-111): l'ADMIN le sposta, progetto per progetto, su un altro Tutor attivo dello
 * stesso progetto; con ogni segnalazione passa anche il beneficiario.
 */
export const UserTicketsPage = () => {
  const { t } = useTranslation();
  const { id = '' } = useParams();
  const user = useUser(id);
  const groups = useOpenTickets(id);
  const [done, setDone] = useState<string | null>(null);
  const doneRef = useRef<HTMLParagraphElement>(null);

  useEffect(() => {
    if (done) doneRef.current?.focus();
  }, [done]);

  const onMoved = (result: TransferResult): void =>
    setDone(t('users.tickets.moved', { count: result.moved, name: result.tutorName }));

  const failed = (user.isError && !user.data) || (groups.isError && !groups.data);

  return (
    <div className="space-y-6">
      <PageHeading>
        {user.data
          ? t('users.tickets.heading', { name: `${user.data.firstName} ${user.data.lastName}` })
          : t('pages.userTickets.title')}
      </PageHeading>
      <p className="text-muted-foreground">{t('users.tickets.intro')}</p>
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
      {failed ? (
        <div role="alert" className="space-y-3 rounded-md border-2 border-destructive bg-card p-4">
          <p className="font-medium text-destructive">{t('users.tickets.loadError')}</p>
          <Button
            variant="outline"
            onClick={() => {
              void user.refetch();
              void groups.refetch();
            }}
          >
            {t('common.retry')}
          </Button>
        </div>
      ) : !user.data || !groups.data ? (
        <p>{t('common.loading')}</p>
      ) : groups.data.length === 0 ? (
        <p>{t('users.tickets.none')}</p>
      ) : (
        groups.data.map((g) => (
          <TransferGroupForm
            key={`${g.project.id}-${g.tickets.map((x) => x.id).join()}`}
            group={g}
            onMoved={onMoved}
          />
        ))
      )}
      <Link to="/utenti" className={link}>
        {t('users.backToList')}
      </Link>
    </div>
  );
};
