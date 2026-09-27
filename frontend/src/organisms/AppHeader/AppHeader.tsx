import { useTranslation } from 'react-i18next';
import { Link } from 'react-router';

import type { CurrentUser } from '@/config/auth/types';
import { UserMenu } from '@/molecules/UserMenu/UserMenu';
import { MainNav } from '@/organisms/MainNav/MainNav';

type AppHeaderProps = {
  user: CurrentUser;
};

export const AppHeader = ({ user }: AppHeaderProps) => {
  const { t } = useTranslation();

  return (
    <header className="sticky top-0 z-30 border-b bg-background/95 backdrop-blur">
      <div className="relative mx-auto flex max-w-6xl flex-wrap items-center justify-between gap-3 px-4 py-3">
        <div className="flex items-center gap-4">
          <Link
            to="/"
            aria-label={t('a11y.homeLink')}
            className="flex min-h-11 items-center gap-2 rounded-md text-lg font-bold"
          >
            <img src="/favicon.svg" alt="" className="size-8" />
            <span aria-hidden="true">{t('app.name')}</span>
          </Link>
          <MainNav roles={user.roles} />
        </div>
        <UserMenu user={user} />
      </div>
    </header>
  );
};
