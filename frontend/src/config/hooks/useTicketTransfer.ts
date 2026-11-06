import {
  useMutation,
  useQuery,
  useQueryClient,
  type UseMutationResult,
  type UseQueryResult,
} from '@tanstack/react-query';

import {
  fetchCoordination,
  fetchOpenTickets,
  transferTickets,
  type TransferGroup,
  type TransferResult,
} from '@/config/api/ticketTransferApi';
import { TICKETS_KEY } from '@/config/hooks/useTickets';
import { USERS_KEY } from '@/config/hooks/useUsers';

const openTicketsKey = (userId: string) => [...USERS_KEY, userId, 'open-tickets'] as const;

/** Segnalazioni aperte di un Tutor, per progetto (US-111). */
export const useOpenTickets = (userId: string): UseQueryResult<TransferGroup[]> =>
  useQuery({ queryKey: openTicketsKey(userId), queryFn: () => fetchOpenTickets(userId) });

/** Progetti coordinati dall'utente (US-112); sotto TICKETS_KEY, così uno spostamento li ricarica. */
export const useCoordination = (): UseQueryResult<TransferGroup[]> =>
  useQuery({ queryKey: [...TICKETS_KEY, 'coordination'], queryFn: fetchCoordination });

type TransferVars = { ticketIds: string[]; tutorId: string };

export const useTransferTickets = (): UseMutationResult<TransferResult, unknown, TransferVars> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (vars: TransferVars) => transferTickets(vars),
    // anche dopo un errore si riallinea: qualcuno può aver chiuso o spostato una segnalazione nel frattempo
    onSettled: () =>
      Promise.all([
        queryClient.invalidateQueries({ queryKey: USERS_KEY }),
        queryClient.invalidateQueries({ queryKey: TICKETS_KEY }),
      ]),
  });
};
