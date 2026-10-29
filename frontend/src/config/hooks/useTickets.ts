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
  fetchTicket,
  fetchTicketHistory,
  matchTicket,
  submitSpecialTicket,
  submitTicket,
  takeCharge,
  takeOverTicket,
  acceptProposal,
  approveFastTrack,
  rejectFastTrack,
  excludeCompany,
  fetchExclusions,
  appointmentFailed,
  appointmentSet,
  approveCategory,
  rejectCategory,
  rejectProposal,
  startInternship,
  type CompanyExclusion,
  type CompatibleFilters,
  type JobSlotMatch,
  type Ticket,
  type TicketCreate,
  type TicketSpecialCreate,
  type TicketDetail,
  type TicketEvent,
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

/**
 * Candidatura dalla bacheca (US-306): si riallineano segnalazioni, beneficiari (ora con una segnalazione aperta) e
 * bacheca (l'annuncio è bloccato).
 */
export const useSubmitSpecialTicket = (): UseMutationResult<Ticket, unknown, TicketSpecialCreate> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: submitSpecialTicket,
    onSettled: () =>
      Promise.all([
        queryClient.invalidateQueries({ queryKey: TICKETS_KEY }),
        queryClient.invalidateQueries({ queryKey: MY_BENEFICIARIES_KEY }),
        queryClient.invalidateQueries({ queryKey: ['board-posts'] }),
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

export const useTicket = (id: string): UseQueryResult<TicketDetail> =>
  useQuery({ queryKey: [...TICKETS_KEY, 'detail', id], queryFn: () => fetchTicket(id) });

/** Cronologia: sotto la stessa chiave dei ticket, così si aggiorna dopo ogni azione. */
export const useTicketHistory = (id: string): UseQueryResult<TicketEvent[]> =>
  useQuery({ queryKey: [...TICKETS_KEY, 'history', id], queryFn: () => fetchTicketHistory(id) });

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

/** Il Tutor accetta la proposta (US-701): la segnalazione e gli elenchi si aggiornano. */
/** Approvazione della corsia veloce (US-403): cambiano segnalazione, mansione e bacheca (annuncio archiviato). */
export const useApproveFastTrack = (): UseMutationResult<
  TicketDetail,
  unknown,
  { id: string; version: number }
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: approveFastTrack,
    onSettled: () =>
      Promise.all([
        queryClient.invalidateQueries({ queryKey: TICKETS_KEY }),
        queryClient.invalidateQueries({ queryKey: ['board-posts'] }),
      ]),
  });
};

/** Rifiuto della corsia veloce (US-404): cambia la segnalazione e l'annuncio torna candidabile. */
export const useRejectFastTrack = (): UseMutationResult<
  TicketDetail,
  unknown,
  { id: string; version: number; reason: string }
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: rejectFastTrack,
    onSettled: () =>
      Promise.all([
        queryClient.invalidateQueries({ queryKey: TICKETS_KEY }),
        queryClient.invalidateQueries({ queryKey: ['board-posts'] }),
      ]),
  });
};

export const useAcceptProposal = (): UseMutationResult<
  TicketDetail,
  unknown,
  { id: string; version: number }
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: acceptProposal,
    onSettled: () => queryClient.invalidateQueries({ queryKey: TICKETS_KEY }),
  });
};

/** Decisione del Call Center sulla nuova tipologia (US-302): la segnalazione e gli elenchi si aggiornano. */
export const useApproveCategory = (): UseMutationResult<
  TicketDetail,
  unknown,
  { id: string; name: string; version: number }
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: approveCategory,
    onSettled: () => queryClient.invalidateQueries({ queryKey: TICKETS_KEY }),
  });
};

export const useRejectCategory = (): UseMutationResult<
  TicketDetail,
  unknown,
  { id: string; jobCategoryId: string; reason: string; version: number }
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: rejectCategory,
    onSettled: () => queryClient.invalidateQueries({ queryKey: TICKETS_KEY }),
  });
};

/** Il Tutor registra l'appuntamento fissato (US-801): il timer si ferma. */
export const useAppointmentSet = (): UseMutationResult<
  TicketDetail,
  unknown,
  { id: string; version: number; note: string | null }
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: appointmentSet,
    onSettled: () => queryClient.invalidateQueries({ queryKey: TICKETS_KEY }),
  });
};

/** Il Tutor avvia il tirocinio (US-1003): segnalazione, scheda e cronologia si aggiornano (stessa chiave). */
export const useStartInternship = (): UseMutationResult<
  TicketDetail,
  unknown,
  { id: string; version: number }
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: startInternship,
    onSettled: () => queryClient.invalidateQueries({ queryKey: TICKETS_KEY }),
  });
};

/** Il Tutor registra l'appuntamento fallito (US-802): la segnalazione torna al Call Center. */
export const useAppointmentFailed = (): UseMutationResult<
  TicketDetail,
  unknown,
  Parameters<typeof appointmentFailed>[0]
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: appointmentFailed,
    onSettled: () => queryClient.invalidateQueries({ queryKey: TICKETS_KEY }),
  });
};

/** Il Tutor rifiuta la proposta (US-702): la segnalazione torna al Call Center. */
export const useRejectProposal = (): UseMutationResult<
  TicketDetail,
  unknown,
  { id: string; version: number; reason: string }
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: rejectProposal,
    onSettled: () => queryClient.invalidateQueries({ queryKey: TICKETS_KEY }),
  });
};

/** Aziende escluse per la segnalazione (US-603). */
export const useExclusions = (id: string, enabled = true): UseQueryResult<CompanyExclusion[]> =>
  useQuery({ queryKey: [...TICKETS_KEY, 'exclusions', id], queryFn: () => fetchExclusions(id), enabled });

/** Esclusione di un'azienda: si aggiornano esclusioni, compatibili e cronologia. */
export const useExcludeCompany = (): UseMutationResult<
  CompanyExclusion[],
  unknown,
  { id: string; companyId: string; reason: string }
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: excludeCompany,
    onSettled: () => queryClient.invalidateQueries({ queryKey: TICKETS_KEY }),
  });
};
