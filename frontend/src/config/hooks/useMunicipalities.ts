import {
  keepPreviousData,
  useMutation,
  useQuery,
  useQueryClient,
  type UseMutationResult,
  type UseQueryResult,
} from '@tanstack/react-query';

import {
  fetchMunicipalityStatus,
  importMunicipalities,
  previewMunicipalityImport,
  searchMunicipalities,
  type Municipality,
  type MunicipalityImportResult,
  type MunicipalityStatus,
} from '@/config/api/municipalityApi';

export const useMunicipalityStatus = (): UseQueryResult<MunicipalityStatus> =>
  useQuery({ queryKey: ['municipalities', 'status'], queryFn: fetchMunicipalityStatus });

/** Ricerca dalla seconda lettera; con `history` anche le versioni passate (solo ADMIN). */
export const useMunicipalitySearch = (q: string, history = false): UseQueryResult<Municipality[]> =>
  useQuery({
    queryKey: ['municipalities', 'search', history, q],
    queryFn: () => searchMunicipalities({ q, history }),
    enabled: q.trim().length >= 2,
    placeholderData: keepPreviousData,
  });

export const usePreviewMunicipalityImport = (): UseMutationResult<MunicipalityImportResult, unknown, File> =>
  useMutation({ mutationFn: previewMunicipalityImport });

export const useImportMunicipalities = (): UseMutationResult<
  MunicipalityImportResult,
  unknown,
  { file: File; validFrom: string }
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: importMunicipalities,
    onSettled: () => queryClient.invalidateQueries({ queryKey: ['municipalities'] }),
  });
};
