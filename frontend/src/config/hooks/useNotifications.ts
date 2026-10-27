import {
  useMutation,
  useQuery,
  useQueryClient,
  type UseMutationResult,
  type UseQueryResult,
} from '@tanstack/react-query';

import {
  fetchNotifications,
  fetchUnreadCount,
  markAllNotificationsRead,
  markNotificationRead,
  type Notifications,
} from '@/config/api/notificationApi';

export const NOTIFICATIONS_KEY = ['notifications'] as const;

/** Aggiornamento del contatore: le notifiche arrivano dalle azioni degli altri utenti. */
const POLL_INTERVAL = 60_000;

export const useUnreadCount = (enabled = true): UseQueryResult<number> =>
  useQuery({
    queryKey: [...NOTIFICATIONS_KEY, 'unread'],
    queryFn: fetchUnreadCount,
    enabled,
    refetchInterval: POLL_INTERVAL,
  });

export const useNotifications = (): UseQueryResult<Notifications> =>
  useQuery({ queryKey: [...NOTIFICATIONS_KEY, 'list'], queryFn: fetchNotifications });

const useNotificationMutation = <V>(
  mutationFn: (vars: V) => Promise<void>,
): UseMutationResult<void, unknown, V> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn,
    onSettled: () => queryClient.invalidateQueries({ queryKey: NOTIFICATIONS_KEY }),
  });
};

export const useMarkNotificationRead = (): UseMutationResult<void, unknown, string> =>
  useNotificationMutation(markNotificationRead);

export const useMarkAllNotificationsRead = (): UseMutationResult<void, unknown, void> =>
  useNotificationMutation(markAllNotificationsRead);
