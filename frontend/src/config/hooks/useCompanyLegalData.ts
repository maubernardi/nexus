import {
  useMutation,
  useQuery,
  useQueryClient,
  type UseMutationResult,
  type UseQueryResult,
} from '@tanstack/react-query';

import {
  addAtecoCode,
  fetchAtecoCodes,
  fetchLegalRepresentative,
  removeAtecoCode,
  saveLegalRepresentative,
  type AtecoCode,
  type LegalRepresentative,
  type LegalRepresentativeForm,
} from '@/config/api/companyLegalDataApi';

const legalRepKey = (companyId: string) => ['companies', companyId, 'legal-representative'] as const;
const atecoKey = (companyId: string) => ['companies', companyId, 'ateco-codes'] as const;

export const useLegalRepresentative = (companyId: string): UseQueryResult<LegalRepresentative | null> =>
  useQuery({ queryKey: legalRepKey(companyId), queryFn: () => fetchLegalRepresentative(companyId) });

export const useSaveLegalRepresentative = (): UseMutationResult<
  LegalRepresentative,
  unknown,
  { companyId: string; payload: LegalRepresentativeForm }
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: saveLegalRepresentative,
    // anche in caso di conflitto si riallinea alla versione salvata
    onSettled: (_r, _e, vars) => queryClient.invalidateQueries({ queryKey: legalRepKey(vars.companyId) }),
  });
};

export const useAtecoCodes = (companyId: string): UseQueryResult<AtecoCode[]> =>
  useQuery({ queryKey: atecoKey(companyId), queryFn: () => fetchAtecoCodes(companyId) });

export const useAddAtecoCode = (): UseMutationResult<
  AtecoCode,
  unknown,
  { companyId: string; code: string; description: string | null }
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: addAtecoCode,
    onSettled: (_r, _e, vars) => queryClient.invalidateQueries({ queryKey: atecoKey(vars.companyId) }),
  });
};

export const useRemoveAtecoCode = (): UseMutationResult<void, unknown, { companyId: string; id: string }> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: removeAtecoCode,
    onSettled: (_r, _e, vars) => queryClient.invalidateQueries({ queryKey: atecoKey(vars.companyId) }),
  });
};
