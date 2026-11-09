import {
  useMutation,
  useQuery,
  useQueryClient,
  type UseMutationResult,
  type UseQueryResult,
} from '@tanstack/react-query';

import {
  anonymizeBeneficiary,
  createBeneficiary,
  deleteBeneficiaryCv,
  downloadBeneficiaryCv,
  fetchAllBeneficiaries,
  fetchBeneficiary,
  fetchMyBeneficiaries,
  keepBeneficiary,
  updateBeneficiary,
  uploadBeneficiaryCv,
  type Beneficiary,
  type BeneficiaryCreate,
  type BeneficiaryRetentionItem,
  type BeneficiarySummary,
} from '@/config/api/beneficiaryApi';

export const BENEFICIARIES_KEY = ['beneficiaries'] as const;
export const MY_BENEFICIARIES_KEY = [...BENEFICIARIES_KEY, 'mine'] as const;

export const useMyBeneficiaries = (): UseQueryResult<BeneficiarySummary[]> =>
  useQuery({ queryKey: MY_BENEFICIARIES_KEY, queryFn: fetchMyBeneficiaries });

/** I miei beneficiari che si possono ancora segnalare: un anonimizzato no (US-1303). */
export const useReportableBeneficiaries = (): UseQueryResult<BeneficiarySummary[]> =>
  useQuery({
    queryKey: MY_BENEFICIARIES_KEY,
    queryFn: fetchMyBeneficiaries,
    select: (list) => list.filter((b) => b.retention !== 'ANONYMIZED'),
  });

/** Tutti i beneficiari, per l'ADMIN (US-1303). */
export const useAllBeneficiaries = (): UseQueryResult<BeneficiaryRetentionItem[]> =>
  useQuery({ queryKey: [...BENEFICIARIES_KEY, 'all'], queryFn: fetchAllBeneficiaries });

export const useAnonymizeBeneficiary = (): UseMutationResult<void, unknown, string> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => anonymizeBeneficiary(id),
    onSettled: () => queryClient.invalidateQueries({ queryKey: BENEFICIARIES_KEY }),
  });
};

export const useKeepBeneficiary = (): UseMutationResult<void, unknown, { id: string; reason: string }> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (vars: { id: string; reason: string }) => keepBeneficiary(vars),
    onSettled: () => queryClient.invalidateQueries({ queryKey: BENEFICIARIES_KEY }),
  });
};

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

/** CV del beneficiario (US-205): la scheda si aggiorna con la risposta. */
const useCvMutation = <V>(
  fn: (vars: V) => Promise<Beneficiary>,
): UseMutationResult<Beneficiary, unknown, V> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (vars: V) => fn(vars),
    onSuccess: (beneficiary) => {
      queryClient.setQueryData([...BENEFICIARIES_KEY, beneficiary.id], beneficiary);
    },
  });
};

export const useUploadBeneficiaryCv = (): UseMutationResult<
  Beneficiary,
  unknown,
  { id: string; file: File }
> => useCvMutation(uploadBeneficiaryCv);

export const useDeleteBeneficiaryCv = (): UseMutationResult<Beneficiary, unknown, string> =>
  useCvMutation(deleteBeneficiaryCv);

export const useDownloadBeneficiaryCv = (): UseMutationResult<Blob, unknown, string> =>
  useMutation({ mutationFn: (id: string) => downloadBeneficiaryCv(id) });
