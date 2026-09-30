import { useEffect, useRef } from 'react';
import { LogOut } from 'lucide-react';
import { useTranslation } from 'react-i18next';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { Button } from '@/components/ui/button';
import { logout } from '@/config/auth/authService';

/**
 * Utente autenticato ma non censito o disattivato (US-101). Fuori dalla shell: senza profilo non ci sono menu né
 * azioni; nessun "Riprova", perché solo l'amministratore può abilitare l'account.
 */
export const NotEnabledPage = () => {
  const { t } = useTranslation();
  const headingRef = useRef<HTMLHeadingElement>(null);

  useEffect(() => {
    document.title = `${t('pages.notEnabled.title')} · ${t('app.name')}`;
    headingRef.current?.focus();
  }, [t]);

  return (
    <main className="grid min-h-dvh place-items-center bg-background p-4">
      <div className="w-full max-w-lg space-y-4 rounded-xl border border-l-4 border-border border-l-primary bg-card p-6">
        <p className="font-semibold text-muted-foreground">{t('app.fullName')}</p>
        <PageHeading ref={headingRef}>{t('pages.notEnabled.heading')}</PageHeading>
        <p>{t('pages.notEnabled.description')}</p>
        <p>{t('pages.notEnabled.action')}</p>
        <Button size="lg" variant="outline" onClick={() => void logout()}>
          <LogOut aria-hidden="true" />
          {t('user.logout')}
        </Button>
      </div>
    </main>
  );
};
