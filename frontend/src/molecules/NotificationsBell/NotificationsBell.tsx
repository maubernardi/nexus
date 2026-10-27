import { useEffect, useRef, useState } from 'react';
import { Bell } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { NavLink } from 'react-router';

import { useUnreadCount } from '@/config/hooks/useNotifications';
import { cn } from '@/lib/utils';

/**
 * Campanella delle notifiche (US-1201): porta alla pagina "Notifiche" e mostra il numero di non lette, che è anche nel
 * nome accessibile. Quando ne arrivano di nuove, una regione nascosta lo annuncia agli screen reader.
 */
export const NotificationsBell = () => {
  const { t } = useTranslation();
  const unread = useUnreadCount();
  const count = unread.data ?? 0;
  const previous = useRef<number | null>(null);
  const [announcement, setAnnouncement] = useState('');

  // si annunciano solo gli aumenti, non il valore iniziale
  useEffect(() => {
    if (unread.data === undefined) return;
    if (previous.current !== null && unread.data > previous.current) {
      setAnnouncement(t('notifications.arrived', { count: unread.data }));
    }
    previous.current = unread.data;
  }, [unread.data, t]);

  return (
    <>
      <NavLink
        to="/notifiche"
        aria-label={t('notifications.bell', { count })}
        className={({ isActive }) =>
          cn(
            'relative flex size-11 shrink-0 items-center justify-center rounded-md text-brand-foreground hover:bg-brand-foreground/10',
            isActive && 'text-gold',
          )
        }
      >
        <Bell aria-hidden="true" className="size-5" />
        {count > 0 && (
          <span
            aria-hidden="true"
            className="absolute top-1 right-0.5 min-w-5 rounded-full bg-gold px-1 text-center text-xs leading-5 font-bold text-brand"
          >
            {count > 99 ? '99+' : count}
          </span>
        )}
      </NavLink>
      <span role="status" className="sr-only">
        {announcement}
      </span>
    </>
  );
};
