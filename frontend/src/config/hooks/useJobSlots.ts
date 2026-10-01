import {
  useMutation,
  useQuery,
  useQueryClient,
  type UseMutationResult,
  type UseQueryResult,
} from '@tanstack/react-query';

import {
  createJobSlot,
  fetchJobSlots,
  setJobSlotActive,
  updateJobSlot,
  type JobSlot,
  type JobSlotForm,
} from '@/config/api/jobSlotApi';

export const jobSlotsKey = (companyId: string) => ['companies', companyId, 'job-slots'] as const;

export const useJobSlots = (companyId: string): UseQueryResult<JobSlot[]> =>
  useQuery({ queryKey: jobSlotsKey(companyId), queryFn: () => fetchJobSlots(companyId) });

const useJobSlotMutation = <V>(
  mutationFn: (vars: V) => Promise<JobSlot>,
): UseMutationResult<JobSlot, unknown, V> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn,
    // anche in caso di conflitto l'elenco va riallineato
    onSettled: (slot, _error, vars) => {
      const companyId = slot?.companyId ?? (vars as { companyId?: string }).companyId;
      return queryClient.invalidateQueries({ queryKey: companyId ? jobSlotsKey(companyId) : ['companies'] });
    },
  });
};

export const useCreateJobSlot = (): UseMutationResult<
  JobSlot,
  unknown,
  { companyId: string; payload: JobSlotForm }
> => useJobSlotMutation(createJobSlot);

export const useUpdateJobSlot = (): UseMutationResult<
  JobSlot,
  unknown,
  { id: string; payload: JobSlotForm }
> => useJobSlotMutation(updateJobSlot);

export const useSetJobSlotActive = (): UseMutationResult<
  JobSlot,
  unknown,
  { id: string; version: number; active: boolean }
> => useJobSlotMutation(setJobSlotActive);
