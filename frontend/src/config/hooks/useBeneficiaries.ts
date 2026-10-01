import {
  useMutation,
  useQuery,
  useQueryClient,
  type UseMutationResult,
  type UseQueryResult,
} from '@tanstack/react-query';

import {
  createBeneficiary,
  fetchMyBeneficiaries,
  type Beneficiary,
  type BeneficiaryCreate,
  type BeneficiarySummary,
} from '@/config/api/beneficiaryApi';

export const MY_BENEFICIARIES_KEY = ['beneficiaries', 'mine'] as const;

export const useMyBeneficiaries = (): UseQueryResult<BeneficiarySummary[]> =>
  useQuery({ queryKey: MY_BENEFICIARIES_KEY, queryFn: fetchMyBeneficiaries });

export const useCreateBeneficiary = (): UseMutationResult<Beneficiary, unknown, BeneficiaryCreate> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createBeneficiary,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: MY_BENEFICIARIES_KEY }),
  });
};
