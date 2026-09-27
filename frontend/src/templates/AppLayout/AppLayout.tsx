import { useTranslation } from 'react-i18next';
import { Outlet } from 'react-router';

import { SkipLink } from '@/atoms/SkipLink/SkipLink';
import type { CurrentUser } from '@/config/auth/types';
import { OfflineBanner } from '@/molecules/OfflineBanner/OfflineBanner';
import { UpdatePrompt } from '@/molecules/UpdatePrompt/UpdatePrompt';
import { AppHeader } from '@/organisms/AppHeader/AppHeader';
import { RouteAnnouncer } from '@/organisms/RouteAnnouncer/RouteAnnouncer';

type AppLayoutProps = {
  user: CurrentUser;
};

export const AppLayout = ({ user }: AppLayoutProps) => {
  const { t } = useTranslation();

  return (
    <div className="flex min-h-dvh flex-col">
      <SkipLink />
      <RouteAnnouncer />
      <AppHeader user={user} />
      <OfflineBanner />
      <main id="main-content" tabIndex={-1} className="mx-auto w-full max-w-6xl flex-1 px-4 py-6 sm:py-8">
        <Outlet />
      </main>
      <footer className="border-t px-4 py-4 text-center text-sm text-muted-foreground">
        {t('app.footer')}
      </footer>
      <UpdatePrompt />
    </div>
  );
};
