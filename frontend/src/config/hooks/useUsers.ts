import {
  useMutation,
  useQuery,
  useQueryClient,
  type UseMutationResult,
  type UseQueryResult,
} from '@tanstack/react-query';

import {
  createUser,
  fetchUser,
  fetchUsers,
  resendInvitation,
  updateUserProjects,
  type AdminUser,
  type NewUserForm,
  type UserCreated,
} from '@/config/api/userAdminApi';

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

export const useUpdateUserProjects = (): UseMutationResult<
  AdminUser,
  unknown,
  { id: string; projectIds: string[] }
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (vars: { id: string; projectIds: string[] }) => updateUserProjects(vars),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: USERS_KEY }),
  });
};
