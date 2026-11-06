import {
  useMutation,
  useQuery,
  useQueryClient,
  type UseMutationResult,
  type UseQueryResult,
} from '@tanstack/react-query';

import {
  changeUserRole,
  createUser,
  fetchUser,
  fetchUsers,
  fetchUserImpact,
  resendInvitation,
  setUserActive,
  updateUserProjects,
  type AdminUser,
  type NewUserForm,
  type UserCreated,
  type UserImpact,
  type UserProjectsVars,
} from '@/config/api/userAdminApi';
import type { Role } from '@/config/auth/types';
import { TICKETS_KEY } from '@/config/hooks/useTickets';

export const USERS_KEY = ['users'] as const;

/** Utenti di NEXUS per l'ADMIN (US-103). */
export const useUsers = (): UseQueryResult<AdminUser[]> =>
  useQuery({ queryKey: USERS_KEY, queryFn: fetchUsers });

export const useCreateUser = (): UseMutationResult<UserCreated, unknown, NewUserForm> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (form: NewUserForm) => createUser(form),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: USERS_KEY }),
  });
};

export const useResendInvitation = (): UseMutationResult<void, unknown, string> =>
  useMutation({ mutationFn: (id: string) => resendInvitation(id) });

/** Un utente con i progetti assegnati (US-104). */
export const useUser = (id: string): UseQueryResult<AdminUser> =>
  useQuery({ queryKey: [...USERS_KEY, id], queryFn: () => fetchUser(id) });

export const useUpdateUserProjects = (): UseMutationResult<AdminUser, unknown, UserProjectsVars> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (vars: UserProjectsVars) => updateUserProjects(vars),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: USERS_KEY }),
  });
};

/** Che cosa tocca disattivare l'utente o cambiargli il ruolo (US-105). */
export const useUserImpact = (id: string): UseQueryResult<UserImpact> =>
  useQuery({ queryKey: [...USERS_KEY, id, 'impact'], queryFn: () => fetchUserImpact(id) });

/** Dopo il cambio si riallineano utenti e segnalazioni: i ticket in carico possono essere tornati in coda. */
const useUserMutation = <V>(
  mutationFn: (vars: V) => Promise<AdminUser>,
): UseMutationResult<AdminUser, unknown, V> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn,
    onSettled: () =>
      Promise.all([
        queryClient.invalidateQueries({ queryKey: USERS_KEY }),
        queryClient.invalidateQueries({ queryKey: TICKETS_KEY }),
      ]),
  });
};

export const useSetUserActive = (): UseMutationResult<AdminUser, unknown, { id: string; active: boolean }> =>
  useUserMutation((vars: { id: string; active: boolean }) => setUserActive(vars));

export const useChangeUserRole = (): UseMutationResult<AdminUser, unknown, { id: string; role: Role }> =>
  useUserMutation((vars: { id: string; role: Role }) => changeUserRole(vars));
