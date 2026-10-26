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
  removeClosure,
  updatePatron,
  type ClosureForm,
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
    onSuccess: (calendar) => queryClient.setQueryData(CALENDAR_KEY, calendar),
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
