import {
  useMutation,
  useQuery,
  useQueryClient,
  type UseMutationResult,
  type UseQueryResult,
} from '@tanstack/react-query';

import { CURRENT_USER_QUERY_KEY } from '@/config/api/queryClient';
import {
  cancelEmailChange,
  confirmEmailChange,
  fetchMyEmail,
  requestEmailChange,
  type EmailStatus,
} from '@/config/api/userApi';

export const MY_EMAIL_KEY = ['me', 'email'] as const;

/** Indirizzo email dell'utente in NEXUS e cambio in corso (US-110). */
export const useMyEmail = (): UseQueryResult<EmailStatus> =>
  useQuery({ queryKey: MY_EMAIL_KEY, queryFn: fetchMyEmail });

/** Ogni passo risponde con lo stato aggiornato: lo si mette subito in cache. */
const useEmailStep = <V>(
  mutationFn: (variables: V) => Promise<EmailStatus>,
  onConfirmed = false,
): UseMutationResult<EmailStatus, unknown, V> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (variables: V) => mutationFn(variables),
    onSuccess: (status) => {
      queryClient.setQueryData(MY_EMAIL_KEY, status);
      // l'utente corrente porta l'email del token: dopo il cambio si rilegge
      if (onConfirmed) void queryClient.invalidateQueries({ queryKey: CURRENT_USER_QUERY_KEY });
    },
    // un errore (codice sbagliato, scaduto) cambia i tentativi rimasti: si rilegge lo stato
    onError: () => queryClient.invalidateQueries({ queryKey: MY_EMAIL_KEY }),
  });
};

export const useRequestEmailChange = (): UseMutationResult<EmailStatus, unknown, string> =>
  useEmailStep(requestEmailChange);

export const useConfirmEmailChange = (): UseMutationResult<EmailStatus, unknown, string> =>
  useEmailStep(confirmEmailChange, true);

export const useCancelEmailChange = (): UseMutationResult<EmailStatus, unknown, void> =>
  useEmailStep(() => cancelEmailChange());
