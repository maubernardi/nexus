import {
  keepPreviousData,
  useMutation,
  useQuery,
  useQueryClient,
  type UseMutationResult,
  type UseQueryResult,
} from '@tanstack/react-query';

import {
  fetchQueue,
  submitTicket,
  type QueueFilters,
  type QueueItem,
  type Ticket,
  type TicketCreate,
} from '@/config/api/ticketApi';

export const TICKETS_KEY = ['tickets'] as const;

export const useSubmitTicket = (): UseMutationResult<Ticket, unknown, TicketCreate> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: submitTicket,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: TICKETS_KEY }),
  });
};

/** Coda del Call Center: si aggiorna da sola mentre l'operatore lavora. */
export const useQueue = (filters: QueueFilters): UseQueryResult<QueueItem[]> =>
  useQuery({
    queryKey: [...TICKETS_KEY, 'queue', filters],
    queryFn: () => fetchQueue(filters),
    refetchInterval: 60_000,
    refetchOnWindowFocus: true,
    // cambiando filtro la tabella resta visibile finché arrivano i nuovi dati
    placeholderData: keepPreviousData,
  });
