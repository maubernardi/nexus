import { useQueryClient } from '@tanstack/react-query';
import { LogOut } from 'lucide-react';
import { useTranslation } from 'react-i18next';

import { Button } from '@/components/ui/button';
import { logout } from '@/config/auth/authService';
import type { CurrentUser } from '@/config/auth/types';
import { UserIdentity } from '@/molecules/UserIdentity/UserIdentity';

type UserMenuProps = {
  user: CurrentUser;
};

/** Identità (solo da desktop: su mobile sta nel menu a tendina) e pulsante di uscita, sempre visibile. */
export const UserMenu = ({ user }: UserMenuProps) => {
  const { t } = useTranslation();
  const queryClient = useQueryClient();

  const handleLogout = async (): Promise<void> => {
    await logout();
    queryClient.clear();
  };

  return (
    <div className="flex shrink-0 items-center gap-3">
      <UserIdentity user={user} className="hidden md:flex" />
      <Button variant="brand" size="sm" onClick={() => void handleLogout()}>
        <LogOut aria-hidden="true" />
        {t('user.logout')}
      </Button>
    </div>
  );
};
