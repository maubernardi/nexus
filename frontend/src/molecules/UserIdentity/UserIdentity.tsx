import { UserRound } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router';

import type { CurrentUser } from '@/config/auth/types';
import { cn } from '@/lib/utils';

type UserIdentityProps = {
  user: CurrentUser;
  /** Collegamento a «Il mio profilo» (US-110); senza, solo testo. */
  linkToProfile?: boolean;
  className?: string;
};

/** Nome e ruolo dell'utente collegato, per fondi brand (intestazione e menu mobile). */
export const UserIdentity = ({ user, linkToProfile = false, className }: UserIdentityProps) => {
  const { t } = useTranslation();
  const fullName = [user.firstName, user.lastName].filter(Boolean).join(' ') || user.username;
  const primaryRole = user.roles[0];

  const content = (
    <>
      <UserRound aria-hidden="true" className="size-5 shrink-0 text-gold" />
      <span className="sr-only">{linkToProfile ? t('user.profile') : t('user.loggedAs')}: </span>
      <span className="flex flex-col leading-tight">
        <span className={cn('font-medium', linkToProfile && 'underline underline-offset-4')}>{fullName}</span>
        {primaryRole && (
          <span className="text-xs text-gold">
            <span className="sr-only">{t('user.role')}: </span>
            {t(`roles.${primaryRole}`)}
          </span>
        )}
      </span>
    </>
  );

  return linkToProfile ? (
    <Link to="/profilo" className={cn('flex min-h-11 items-center gap-2 rounded-md text-sm', className)}>
      {content}
    </Link>
  ) : (
    <p className={cn('flex items-center gap-2 text-sm', className)}>{content}</p>
  );
};
