import { useInfiniteQuery, type InfiniteData, type UseInfiniteQueryResult } from '@tanstack/react-query';

import {
  fetchFailedLogins,
  fetchMyAccessLog,
  fetchUserAccessLog,
  type AccessLogPage,
} from '@/config/api/accessLogApi';

export const ACCESS_LOG_KEY = ['access-log'] as const;

export type AccessLogQuery = UseInfiniteQueryResult<InfiniteData<AccessLogPage, number>>;

const useAccessLogPages = (
  key: readonly unknown[],
  fetchPage: (page: number) => Promise<AccessLogPage>,
): AccessLogQuery =>
  useInfiniteQuery<AccessLogPage, Error, InfiniteData<AccessLogPage, number>, readonly unknown[], number>({
    queryKey: [...ACCESS_LOG_KEY, ...key],
    queryFn: ({ pageParam }) => fetchPage(pageParam),
    initialPageParam: 0,
    getNextPageParam: (last: AccessLogPage, all: AccessLogPage[]) => (last.hasMore ? all.length : undefined),
  });

/** Accessi di un utente, per l'ADMIN (US-113). */
export const useUserAccessLog = (userId: string): AccessLogQuery =>
  useAccessLogPages(['user', userId], (page) => fetchUserAccessLog(userId, page));

/** I propri accessi (US-113). */
export const useMyAccessLog = (): AccessLogQuery => useAccessLogPages(['me'], fetchMyAccessLog);

/** Tentativi falliti di tutti, per l'ADMIN (US-113). */
export const useFailedLogins = (): AccessLogQuery => useAccessLogPages(['failed'], fetchFailedLogins);
