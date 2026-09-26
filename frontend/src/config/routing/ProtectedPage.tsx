import { useTranslation } from 'react-i18next';
import { Navigate } from 'react-router-dom';

import { Button } from '@/components/ui/button';
import { AUTH_MODE } from '@/config/auth/authConfig';
import { useCurrentUser } from '@/config/hooks/useCurrentUser';
import { useSessionStore } from '@/config/stores/sessionStore';
import { AppLayout } from '@/templates/AppLayout/AppLayout';

export const DEV_LOGIN_PATH = '/accesso-sviluppo';

/** Garantisce un utente autenticato prima di mostrare la shell applicativa. */
export const ProtectedPage = () => {
  const { t } = useTranslation();
  const mockUserId = useSessionStore((state) => state.mockUserId);
  const { data: user, isPending, isError, refetch } = useCurrentUser();

  if (AUTH_MODE === 'mock' && !mockUserId) {
    return <Navigate to={DEV_LOGIN_PATH} replace />;
  }

  if (isPending) {
    return (
      <main className="grid min-h-dvh place-items-center p-4">
        <p role="status">{t('user.loading')}</p>
      </main>
    );
  }

  if (isError) {
    return (
      <main className="grid min-h-dvh place-items-center p-4">
        <div role="alert" className="space-y-3 text-center">
          <p>{t('user.loadError')}</p>
          <Button onClick={() => void refetch()}>{t('user.retry')}</Button>
        </div>
      </main>
    );
  }

  return <AppLayout user={user} />;
};
