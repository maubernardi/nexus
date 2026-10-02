import {
  keepPreviousData,
  useMutation,
  useQuery,
  useQueryClient,
  type UseMutationResult,
  type UseQueryResult,
} from '@tanstack/react-query';

import { MY_BENEFICIARIES_KEY } from '@/config/hooks/useBeneficiaries';
import {
  fetchAssignedToMe,
  fetchCompatibleJobSlots,
  fetchQueue,
  fetchTicketWork,
  matchTicket,
  submitTicket,
  takeCharge,
  type CompatibleFilters,
  type JobSlotMatch,
  type QueueFilters,
  type QueueItem,
  type Ticket,
  type TicketCreate,
  type TicketDetail,
} from '@/config/api/ticketApi';

export const TICKETS_KEY = ['tickets'] as const;

export const useSubmitTicket = (): UseMutationResult<Ticket, unknown, TicketCreate> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: submitTicket,
    // anche i beneficiari: quello appena segnalato ora ha una segnalazione aperta
    onSuccess: () =>
      Promise.all([
        queryClient.invalidateQueries({ queryKey: TICKETS_KEY }),
        queryClient.invalidateQueries({ queryKey: MY_BENEFICIARIES_KEY }),
      ]),
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

/** Presa in carico: in ogni caso (riuscita o conflitto) coda e lavorazioni vanno riallineate. */
export const useTakeCharge = (): UseMutationResult<QueueItem, unknown, { id: string; version: number }> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: takeCharge,
    onSettled: () => queryClient.invalidateQueries({ queryKey: TICKETS_KEY }),
  });
};

export const useAssignedToMe = (): UseQueryResult<QueueItem[]> =>
  useQuery({
    queryKey: [...TICKETS_KEY, 'assigned-to-me'],
    queryFn: fetchAssignedToMe,
    refetchOnWindowFocus: true,
  });

export const useTicketWork = (id: string): UseQueryResult<TicketDetail> =>
  useQuery({ queryKey: [...TICKETS_KEY, 'work', id], queryFn: () => fetchTicketWork(id) });

export const useCompatibleJobSlots = (
  id: string,
  filters: CompatibleFilters,
  enabled: boolean,
): UseQueryResult<JobSlotMatch[]> =>
  useQuery({
    queryKey: [...TICKETS_KEY, 'compatible', id, filters],
    queryFn: () => fetchCompatibleJobSlots(id, filters),
    enabled,
    placeholderData: keepPreviousData,
  });

/** Abbinamento: in ogni caso (riuscita o conflitto) segnalazione, compatibili e liste vanno riallineate. */
export const useMatchTicket = (): UseMutationResult<
  TicketDetail,
  unknown,
  { id: string; jobSlotId: string; ticketVersion: number; jobSlotVersion: number }
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: matchTicket,
    onSettled: () => queryClient.invalidateQueries({ queryKey: TICKETS_KEY }),
  });
};
