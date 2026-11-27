import {
  useMutation,
  useQuery,
  useQueryClient,
  type UseMutationResult,
  type UseQueryResult,
} from '@tanstack/react-query';

import {
  createContactRequest,
  fetchContactRequestQueue,
  fetchMyContactRequests,
  fetchOpenContactRequestCount,
  takeContactRequest,
  type ContactRequest,
  type ContactRequestForm,
} from '@/config/api/contactRequestApi';

export const CONTACT_REQUESTS_KEY = ['contact-requests'] as const;

/** Il numero nel menu si aggiorna da solo: le richieste le mandano i Tutor (US-507). */
const POLL_INTERVAL = 60_000;

export const useMyContactRequests = (): UseQueryResult<ContactRequest[]> =>
  useQuery({ queryKey: [...CONTACT_REQUESTS_KEY, 'mine'], queryFn: fetchMyContactRequests });

export const useContactRequestQueue = (): UseQueryResult<ContactRequest[]> =>
  useQuery({ queryKey: [...CONTACT_REQUESTS_KEY, 'queue'], queryFn: fetchContactRequestQueue });

export const useOpenContactRequestCount = (enabled = true): UseQueryResult<number> =>
  useQuery({
    queryKey: [...CONTACT_REQUESTS_KEY, 'open-count'],
    queryFn: fetchOpenContactRequestCount,
    enabled,
    refetchInterval: POLL_INTERVAL,
  });

export const useCreateContactRequest = (): UseMutationResult<ContactRequest, unknown, ContactRequestForm> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createContactRequest,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: CONTACT_REQUESTS_KEY }),
  });
};

export const useTakeContactRequest = (): UseMutationResult<
  ContactRequest,
  unknown,
  { id: string; version: number }
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: takeContactRequest,
    // anche se fallisce: un altro operatore potrebbe averla presa nel frattempo
    onSettled: () => queryClient.invalidateQueries({ queryKey: CONTACT_REQUESTS_KEY }),
  });
};
