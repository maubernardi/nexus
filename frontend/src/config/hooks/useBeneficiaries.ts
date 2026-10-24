import {
  useMutation,
  useQuery,
  useQueryClient,
  type UseMutationResult,
  type UseQueryResult,
} from '@tanstack/react-query';

import {
  createBeneficiary,
  fetchBeneficiary,
  fetchMyBeneficiaries,
  updateBeneficiary,
  type Beneficiary,
  type BeneficiaryCreate,
  type BeneficiarySummary,
} from '@/config/api/beneficiaryApi';

export const BENEFICIARIES_KEY = ['beneficiaries'] as const;
export const MY_BENEFICIARIES_KEY = [...BENEFICIARIES_KEY, 'mine'] as const;

export const useMyBeneficiaries = (): UseQueryResult<BeneficiarySummary[]> =>
  useQuery({ queryKey: MY_BENEFICIARIES_KEY, queryFn: fetchMyBeneficiaries });

export const useCreateBeneficiary = (): UseMutationResult<Beneficiary, unknown, BeneficiaryCreate> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createBeneficiary,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: MY_BENEFICIARIES_KEY }),
  });
};

export const useBeneficiary = (id: string): UseQueryResult<Beneficiary> =>
  useQuery({ queryKey: [...BENEFICIARIES_KEY, id], queryFn: () => fetchBeneficiary(id) });

export const useUpdateBeneficiary = (): UseMutationResult<
  Beneficiary,
  unknown,
  { id: string; payload: BeneficiaryCreate }
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: updateBeneficiary,
    onSuccess: (beneficiary) => {
      queryClient.setQueryData([...BENEFICIARIES_KEY, beneficiary.id], beneficiary);
      return queryClient.invalidateQueries({ queryKey: MY_BENEFICIARIES_KEY });
    },
  });
};
