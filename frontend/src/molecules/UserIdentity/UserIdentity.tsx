import { UserRound } from 'lucide-react';
import { useTranslation } from 'react-i18next';

import type { CurrentUser } from '@/config/auth/types';
import { cn } from '@/lib/utils';

type UserIdentityProps = {
  user: CurrentUser;
  className?: string;
};

/** Nome e ruolo dell'utente collegato, per fondi brand (intestazione e menu mobile). */
export const UserIdentity = ({ user, className }: UserIdentityProps) => {
  const { t } = useTranslation();
  const fullName = [user.firstName, user.lastName].filter(Boolean).join(' ') || user.username;
  const primaryRole = user.roles[0];

  return (
    <p className={cn('flex items-center gap-2 text-sm', className)}>
      <UserRound aria-hidden="true" className="size-5 shrink-0 text-gold" />
      <span className="sr-only">{t('user.loggedAs')}: </span>
      <span className="flex flex-col leading-tight">
        <span className="font-medium">{fullName}</span>
        {primaryRole && (
          <span className="text-xs text-gold">
            <span className="sr-only">{t('user.role')}: </span>
            {t(`roles.${primaryRole}`)}
          </span>
        )}
      </span>
    </p>
  );
};
