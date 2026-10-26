import {
  useMutation,
  useQuery,
  useQueryClient,
  type UseMutationResult,
  type UseQueryResult,
} from '@tanstack/react-query';

import {
  addClosure,
  fetchCalendar,
  fetchDeadlinePreview,
  removeClosure,
  updatePatron,
  type ClosureForm,
  type DeadlinePreview,
  type WorkCalendar,
} from '@/config/api/calendarApi';

export const CALENDAR_KEY = ['calendar'] as const;

export const useCalendar = (): UseQueryResult<WorkCalendar> =>
  useQuery({ queryKey: CALENDAR_KEY, queryFn: fetchCalendar });

/** Ogni modifica restituisce il calendario aggiornato, che sostituisce quello in cache. */
const useCalendarMutation = <V>(
  mutationFn: (vars: V) => Promise<WorkCalendar>,
): UseMutationResult<WorkCalendar, unknown, V> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn,
    onSuccess: (calendar) => {
      queryClient.setQueryData(CALENDAR_KEY, calendar);
      // patrono e chiusure cambiano le scadenze: la prova del calcolo va rifatta
      return queryClient.invalidateQueries({ queryKey: [...CALENDAR_KEY, 'preview'] });
    },
  });
};

export const useUpdatePatron = (): UseMutationResult<
  WorkCalendar,
  unknown,
  { day: number; month: number; version: number }
> => useCalendarMutation(updatePatron);

export const useAddClosure = (): UseMutationResult<WorkCalendar, unknown, ClosureForm> =>
  useCalendarMutation(addClosure);

export const useRemoveClosure = (): UseMutationResult<WorkCalendar, unknown, string> =>
  useCalendarMutation(removeClosure);

/** Prova del calcolo (US-703) per la data indicata; nessuna richiesta finché la data è vuota. */
export const useDeadlinePreview = (from: string): UseQueryResult<DeadlinePreview> =>
  useQuery({
    queryKey: [...CALENDAR_KEY, 'preview', from],
    queryFn: () => fetchDeadlinePreview(from),
    enabled: from !== '',
  });
