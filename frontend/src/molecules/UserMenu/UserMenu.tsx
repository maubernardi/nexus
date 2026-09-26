import { useQueryClient } from '@tanstack/react-query';
import { LogOut, UserRound } from 'lucide-react';
import { useTranslation } from 'react-i18next';

import { Button } from '@/components/ui/button';
import { AUTH_MODE } from '@/config/auth/authConfig';
import { logout } from '@/config/auth/authService';
import type { CurrentUser } from '@/config/auth/types';

type UserMenuProps = {
  user: CurrentUser;
};

export const UserMenu = ({ user }: UserMenuProps) => {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const fullName = [user.firstName, user.lastName].filter(Boolean).join(' ') || user.username;
  const primaryRole = user.roles[0];

  const handleLogout = async (): Promise<void> => {
    await logout();
    queryClient.clear();
  };

  return (
    <div className="flex items-center gap-3">
      <p className="flex items-center gap-2 text-sm">
        <UserRound aria-hidden="true" className="size-5 shrink-0 text-muted-foreground" />
        <span className="sr-only">{t('user.loggedAs')}: </span>
        <span className="flex flex-col leading-tight">
          <span className="font-medium">{fullName}</span>
          {primaryRole && (
            <span className="text-xs text-muted-foreground">
              <span className="sr-only">{t('user.role')}: </span>
              {t(`roles.${primaryRole}`)}
            </span>
          )}
        </span>
      </p>
      <Button variant="outline" size="sm" onClick={() => void handleLogout()}>
        <LogOut aria-hidden="true" />
        {AUTH_MODE === 'mock' ? t('user.switchUser') : t('user.logout')}
      </Button>
    </div>
  );
};
