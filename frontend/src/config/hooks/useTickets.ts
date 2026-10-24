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
  fetchCompatibleJobSlots,
  fetchTickets,
  fetchTicketWork,
  matchTicket,
  submitTicket,
  takeCharge,
  takeOverTicket,
  type CompatibleFilters,
  type JobSlotMatch,
  type Ticket,
  type TicketCreate,
  type TicketDetail,
  type TicketListItem,
  type TicketListQuery,
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

/** Elenco delle segnalazioni della Home (US-303): si aggiorna da solo mentre si lavora. */
export const useTicketList = (query: TicketListQuery, enabled = true): UseQueryResult<TicketListItem[]> =>
  useQuery({
    enabled,
    queryKey: [...TICKETS_KEY, 'list', query],
    queryFn: () => fetchTickets(query),
    refetchInterval: 60_000,
    refetchOnWindowFocus: true,
    // cambiando vista o filtro la tabella resta visibile finché arrivano i nuovi dati
    placeholderData: keepPreviousData,
  });

/** Presa in carico: in ogni caso (riuscita o conflitto) coda e lavorazioni vanno riallineate. */
export const useTakeCharge = (): UseMutationResult<
  TicketListItem,
  unknown,
  { id: string; version: number }
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: takeCharge,
    onSettled: () => queryClient.invalidateQueries({ queryKey: TICKETS_KEY }),
  });
};

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

/** "Prendila in carico tu" (US-405): la segnalazione di un collega passa all'operatore corrente. */
export const useTakeOver = (): UseMutationResult<TicketDetail, unknown, { id: string; version: number }> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: takeOverTicket,
    onSettled: () => queryClient.invalidateQueries({ queryKey: TICKETS_KEY }),
  });
};
