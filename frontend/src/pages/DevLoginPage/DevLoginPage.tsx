import { useTranslation } from 'react-i18next';
import { Navigate, useNavigate } from 'react-router';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { SkipLink } from '@/atoms/SkipLink/SkipLink';
import { Button } from '@/components/ui/button';
import { AUTH_MODE } from '@/config/auth/authConfig';
import { MOCK_USERS } from '@/config/auth/mockUsers';
import { useSessionStore } from '@/config/stores/sessionStore';
import { RouteAnnouncer } from '@/organisms/RouteAnnouncer/RouteAnnouncer';

/** Selezione dell'utente fittizio, disponibile solo con VITE_AUTH_MODE=mock. */
export const DevLoginPage = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const setMockUserId = useSessionStore((state) => state.setMockUserId);

  if (AUTH_MODE !== 'mock') {
    return <Navigate to="/" replace />;
  }

  const handleLogin = (username: string): void => {
    setMockUserId(username);
    navigate('/', { replace: true });
  };

  return (
    <>
      <SkipLink />
      <RouteAnnouncer />
      <main id="main-content" tabIndex={-1} className="mx-auto w-full max-w-md space-y-6 px-4 py-10">
        <PageHeading>{t('pages.devLogin.heading')}</PageHeading>
        <p className="text-muted-foreground">{t('pages.devLogin.description')}</p>
        <section aria-labelledby="dev-users-heading">
          <h2 id="dev-users-heading" className="mb-3 text-lg font-semibold">
            {t('pages.devLogin.usersLabel')}
          </h2>
          <ul className="space-y-2">
            {MOCK_USERS.map((user) => (
              <li key={user.username}>
                <Button
                  size="lg"
                  variant="outline"
                  className="h-auto min-h-11 w-full flex-col items-start py-2 text-left"
                  onClick={() => handleLogin(user.username)}
                >
                  <span>{t('pages.devLogin.loginAs', { name: user.displayName })}</span>
                  <span className="text-sm font-normal text-muted-foreground">{t(`roles.${user.role}`)}</span>
                </Button>
              </li>
            ))}
          </ul>
        </section>
      </main>
    </>
  );
};
