import { useMutation, useQueryClient, type UseMutationResult } from '@tanstack/react-query';

import { submitTicket, type Ticket, type TicketCreate } from '@/config/api/ticketApi';

export const TICKETS_KEY = ['tickets'] as const;

export const useSubmitTicket = (): UseMutationResult<Ticket, unknown, TicketCreate> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: submitTicket,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: TICKETS_KEY }),
  });
};
