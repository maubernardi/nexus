import {
  useMutation,
  useQuery,
  useQueryClient,
  type UseMutationResult,
  type UseQueryResult,
} from '@tanstack/react-query';

import { addCompanyNote, fetchCompanyNotes, type CompanyNote } from '@/config/api/companyNoteApi';

export const companyNotesKey = (companyId: string) => ['companies', companyId, 'notes'] as const;

export const useCompanyNotes = (companyId: string): UseQueryResult<CompanyNote[]> =>
  useQuery({ queryKey: companyNotesKey(companyId), queryFn: () => fetchCompanyNotes(companyId) });

export const useAddCompanyNote = (): UseMutationResult<
  CompanyNote,
  unknown,
  { companyId: string; text: string }
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: addCompanyNote,
    onSuccess: (_note, { companyId }) =>
      queryClient.invalidateQueries({ queryKey: companyNotesKey(companyId) }),
  });
};
