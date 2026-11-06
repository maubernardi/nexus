import {
  useMutation,
  useQuery,
  useQueryClient,
  type UseMutationResult,
  type UseQueryResult,
} from '@tanstack/react-query';

import {
  createUser,
  fetchUsers,
  resendInvitation,
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
