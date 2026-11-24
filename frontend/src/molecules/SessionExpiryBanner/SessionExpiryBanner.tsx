import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';

import { Button } from '@/components/ui/button';
import { getSessionExpiresAt, reloginNow } from '@/config/auth/authService';

/** Ogni quanto si controlla la fine della sessione. */
export const SESSION_CHECK_MS = 15_000;
/** L'avviso compare quando alla fine della sessione mancano 5 minuti o meno. */
export const SESSION_WARNING_MS = 5 * 60_000;

const timeFormat = new Intl.DateTimeFormat('it-IT', {
  hour: '2-digit',
  minute: '2-digit',
  timeZone: 'Europe/Rome',
});

/**
 * US-1310: avvisa prima della durata massima della sessione (WCAG 2.2.1), con l'ora della scadenza e un nuovo accesso
 * subito. Il rinnovo tiene viva la sessione finché la pagina è aperta, quindi l'avviso compare solo vicino al massimo.
 * Senza Keycloak (accesso di sviluppo) non compare mai.
 */
export const SessionExpiryBanner = () => {
  const { t } = useTranslation();
  const [expiresAt, setExpiresAt] = useState<number | null>(() => getSessionExpiresAt());
  const [now, setNow] = useState(() => Date.now());
  const [dismissedFor, setDismissedFor] = useState<number | null>(null);

  useEffect(() => {
    const id = setInterval(() => {
      setExpiresAt(getSessionExpiresAt());
      setNow(Date.now());
    }, SESSION_CHECK_MS);
    return () => clearInterval(id);
  }, []);

  const expiring =
    expiresAt !== null &&
    expiresAt > now &&
    expiresAt - now <= SESSION_WARNING_MS &&
    dismissedFor !== expiresAt;

  return (
    // striscia nel flusso della pagina, come quella dell'aggiornamento: non copre contenuti né il focus (WCAG 2.4.11)
    <div role="alert">
      {expiring && (
        <div className="border-b border-border bg-accent text-accent-foreground">
          <div className="mx-auto flex max-w-6xl flex-wrap items-center gap-x-4 gap-y-2 px-4 py-3">
            <p className="text-sm font-medium">
              {t('session.expiring', { time: timeFormat.format(expiresAt) })}
            </p>
            <div className="flex flex-wrap gap-2">
              <Button onClick={() => void reloginNow()}>{t('session.reloginNow')}</Button>
              <Button variant="outline" onClick={() => setDismissedFor(expiresAt)}>
                {t('session.dismiss')}
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
