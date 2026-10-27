import { useEffect, useRef, useState } from 'react';
import { CheckCheck } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { Button } from '@/components/ui/button';
import { NOTIFICATION_TYPES, type AppNotification } from '@/config/api/notificationApi';
import {
  useMarkAllNotificationsRead,
  useMarkNotificationRead,
  useNotifications,
} from '@/config/hooks/useNotifications';
import { cn } from '@/lib/utils';

const dateTime = new Intl.DateTimeFormat('it-IT', { dateStyle: 'short', timeStyle: 'short' });

/** Notifiche in app (US-1201): dalla più recente, con il link alla segnalazione; segna come letta una o tutte. */
export const NotificationsPage = () => {
  const { t } = useTranslation();
  const notifications = useNotifications();
  const markRead = useMarkNotificationRead();
  const markAll = useMarkAllNotificationsRead();
  const [allRead, setAllRead] = useState(false);
  const outcomeRef = useRef<HTMLParagraphElement>(null);

  useEffect(() => {
    if (allRead) outcomeRef.current?.focus();
  }, [allRead]);

  const text = (n: AppNotification): string =>
    (NOTIFICATION_TYPES as readonly string[]).includes(n.type)
      ? t(`notifications.types.${n.type as (typeof NOTIFICATION_TYPES)[number]}`, { number: n.ticketNumber })
      : t('notifications.generic', { number: n.ticketNumber });

  if (notifications.isError && !notifications.data) {
    return (
      <div className="space-y-4">
        <PageHeading>{t('pages.notifications.heading')}</PageHeading>
        <div role="alert" className="space-y-3 rounded-md border-2 border-destructive bg-card p-4">
          <p className="font-medium text-destructive">{t('common.loadError')}</p>
          <Button variant="outline" onClick={() => void notifications.refetch()}>
            {t('common.retry')}
          </Button>
        </div>
      </div>
    );
  }
  if (!notifications.data) {
    return <p role="status">{t('common.loading')}</p>;
  }
  const { unread, items } = notifications.data;

  return (
    <div className="space-y-6">
      <PageHeading>{t('pages.notifications.heading')}</PageHeading>
      <div className="flex flex-wrap items-center justify-between gap-3">
        <p className="font-medium">{t('notifications.unread', { count: unread })}</p>
        {unread > 0 && (
          <Button
            variant="outline"
            size="lg"
            disabled={markAll.isPending}
            onClick={() => markAll.mutate(undefined, { onSuccess: () => setAllRead(true) })}
          >
            <CheckCheck aria-hidden="true" />
            {t('notifications.markAll')}
          </Button>
        )}
      </div>

      {allRead && (
        <p
          ref={outcomeRef}
          tabIndex={-1}
          className="rounded-md border-l-4 border-primary bg-card p-4 font-medium"
        >
          {t('notifications.allRead')}
        </p>
      )}

      {items.length === 0 ? (
        <p className="rounded-xl border border-border bg-card p-6">{t('notifications.empty')}</p>
      ) : (
        <ol aria-label={t('notifications.listLabel')} className="space-y-3">
          {items.map((n) => (
            <li
              key={n.id}
              className={cn(
                'flex flex-wrap items-start justify-between gap-3 rounded-xl border bg-card p-4',
                n.read ? 'border-border' : 'border-l-4 border-border border-l-gold',
              )}
            >
              <div className="min-w-0 space-y-1">
                <p className={cn('break-words', !n.read && 'font-semibold')}>
                  {/* "Nuova" in testo: lo stato non è affidato al solo stile */}
                  {!n.read && (
                    <>
                      <span className="text-primary">{t('notifications.new')}</span>{' '}
                    </>
                  )}
                  {text(n)}
                </p>
                <p className="text-sm text-muted-foreground">
                  <time dateTime={n.createdAt}>{dateTime.format(new Date(n.createdAt))}</time>
                </p>
                {n.ticketId && (
                  <Link
                    to={`/segnalazioni/${n.ticketId}`}
                    onClick={() => !n.read && markRead.mutate(n.id)}
                    className="inline-block font-medium text-primary underline underline-offset-4"
                  >
                    {t('notifications.open', { number: n.ticketNumber })}
                  </Link>
                )}
              </div>
              {!n.read && (
                <Button
                  size="sm"
                  variant="outline"
                  disabled={markRead.isPending}
                  onClick={() => markRead.mutate(n.id)}
                >
                  {t('notifications.markRead')}
                  {/* spazio come nodo a sé: nome accessibile completo */}{' '}
                  <span className="sr-only">{text(n)}</span>
                </Button>
              )}
            </li>
          ))}
        </ol>
      )}
    </div>
  );
};
