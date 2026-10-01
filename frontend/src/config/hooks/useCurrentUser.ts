import { useQuery, type UseQueryResult } from '@tanstack/react-query';

import { isUserNotEnabled } from '@/config/api/errors';
import { CURRENT_USER_QUERY_KEY } from '@/config/api/queryClient';
import { fetchCurrentUser } from '@/config/api/userApi';
import type { CurrentUser } from '@/config/auth/types';
import { useSessionStore } from '@/config/stores/sessionStore';

export { CURRENT_USER_QUERY_KEY };

export const useCurrentUser = (): UseQueryResult<CurrentUser> => {
  // la chiave include l'utente mock così il cambio utente invalida la cache
  const mockUserId = useSessionStore((state) => state.mockUserId);
  return useQuery({
    queryKey: [...CURRENT_USER_QUERY_KEY, mockUserId],
    queryFn: fetchCurrentUser,
    staleTime: 5 * 60_000,
    // un utente non abilitato non lo diventa riprovando
    retry: (failureCount, error) => !isUserNotEnabled(error) && failureCount < 1,
  });
};
