import type { Role } from '@/config/auth/types';
import { useCurrentUser } from '@/config/hooks/useCurrentUser';
import { ForbiddenPage } from '@/pages/ForbiddenPage/ForbiddenPage';

type RoleRouteProps = {
  roles: readonly Role[];
  children: React.ReactNode;
};

/** Mostra la pagina solo ai ruoli indicati (la sicurezza vera resta nelle API: qui si evita solo un'interfaccia inutilizzabile). */
export const RoleRoute = ({ roles, children }: RoleRouteProps) => {
  const { data: user } = useCurrentUser();
  const allowed = user?.roles.some((r) => roles.includes(r)) ?? false;
  return allowed ? children : <ForbiddenPage />;
};
