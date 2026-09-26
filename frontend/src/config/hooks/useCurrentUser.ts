import { useQuery, type UseQueryResult } from '@tanstack/react-query';

import { fetchCurrentUser } from '@/config/api/userApi';
import type { CurrentUser } from '@/config/auth/types';
import { useSessionStore } from '@/config/stores/sessionStore';

export const CURRENT_USER_QUERY_KEY = ['currentUser'] as const;

export const useCurrentUser = (): UseQueryResult<CurrentUser> => {
  // la chiave include l'utente mock così il cambio utente invalida la cache
  const mockUserId = useSessionStore((state) => state.mockUserId);
  return useQuery({
    queryKey: [...CURRENT_USER_QUERY_KEY, mockUserId],
    queryFn: fetchCurrentUser,
    staleTime: 5 * 60_000,
  });
};
