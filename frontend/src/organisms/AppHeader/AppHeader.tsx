import { useTranslation } from 'react-i18next';
import { Link } from 'react-router';

import type { CurrentUser } from '@/config/auth/types';
import { UserIdentity } from '@/molecules/UserIdentity/UserIdentity';
import { UserMenu } from '@/molecules/UserMenu/UserMenu';
import { MainNav } from '@/organisms/MainNav/MainNav';

type AppHeaderProps = {
  user: CurrentUser;
};

export const AppHeader = ({ user }: AppHeaderProps) => {
  const { t } = useTranslation();

  return (
    // fondo Deep Emerald con filetto oro; dentro l'intestazione il focus è oro (il verde non si vedrebbe)
    <header className="sticky top-0 z-30 border-b-2 border-gold bg-brand text-brand-foreground [--ring:var(--gold)]">
      {/* una sola riga anche su mobile: logo, menu, Esci (nome e ruolo stanno nel menu a tendina) */}
      <div className="relative mx-auto flex max-w-6xl items-center justify-between gap-2 px-4 py-2 md:gap-3 md:py-3">
        <div className="flex min-w-0 items-center gap-2 md:gap-4">
          <Link
            to="/"
            aria-label={t('a11y.homeLink')}
            className="flex min-h-11 shrink-0 items-center gap-2 rounded-md text-lg font-bold tracking-wide"
          >
            <img src="/favicon.svg" alt="" className="size-8" />
            <span aria-hidden="true">{t('app.name')}</span>
          </Link>
          <MainNav roles={user.roles} mobileHeader={<UserIdentity user={user} />} />
        </div>
        <UserMenu user={user} />
      </div>
    </header>
  );
};
