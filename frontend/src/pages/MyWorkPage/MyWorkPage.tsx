import { useTranslation } from 'react-i18next';
import { Link } from 'react-router';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { Button } from '@/components/ui/button';
import { useAssignedToMe } from '@/config/hooks/useTickets';
import { QueueTable } from '@/organisms/QueueTable/QueueTable';

/** Segnalazioni aperte prese in carico dall'operatore corrente (US-402). */
export const MyWorkPage = () => {
  const { t } = useTranslation();
  const mine = useAssignedToMe();

  return (
    <div className="space-y-6">
      <PageHeading>{t('pages.myWork.heading')}</PageHeading>
      <p className="text-muted-foreground">{t('pages.myWork.intro')}</p>

      <p role="status" className="font-medium">
        {mine.data ? t('queue.myWorkCount', { count: mine.data.length }) : ''}
      </p>

      {mine.isError && !mine.data ? (
        <div role="alert" className="space-y-3 rounded-md border-2 border-destructive bg-card p-4">
          <p className="font-medium text-destructive">{t('queue.loadError')}</p>
          <Button variant="outline" onClick={() => void mine.refetch()}>
            {t('queue.retry')}
          </Button>
        </div>
      ) : !mine.data ? (
        <p>{t('queue.loading')}</p>
      ) : mine.data.length === 0 ? (
        <div className="space-y-3 rounded-xl border border-border bg-card p-6">
          <p>{t('queue.myWorkEmpty')}</p>
          <Button asChild size="lg">
            <Link to="/coda">{t('queue.goToQueue')}</Link>
          </Button>
        </div>
      ) : (
        <QueueTable items={mine.data} caption={t('queue.myWorkCaption')} />
      )}
    </div>
  );
};
