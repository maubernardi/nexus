import {
  useMutation,
  useQuery,
  useQueryClient,
  type UseMutationResult,
  type UseQueryResult,
} from '@tanstack/react-query';

import {
  createCatalogItem,
  fetchCatalog,
  renameCatalogItem,
  setCatalogItemActive,
  type CatalogItem,
  type CatalogKind,
} from '@/config/api/catalogApi';

const catalogKey = (kind: CatalogKind) => ['catalogs', kind] as const;

export const useCatalog = (kind: CatalogKind): UseQueryResult<CatalogItem[]> =>
  useQuery({ queryKey: catalogKey(kind), queryFn: () => fetchCatalog(kind) });

const useCatalogMutation = <V extends { kind: CatalogKind }>(
  mutationFn: (vars: V) => Promise<CatalogItem>,
): UseMutationResult<CatalogItem, unknown, V> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn,
    // anche in caso di conflitto si riallinea; e i menu di zone e tipologie degli altri moduli
    onSettled: (_item, _error, vars) =>
      Promise.all([
        queryClient.invalidateQueries({ queryKey: catalogKey(vars.kind) }),
        queryClient.invalidateQueries({ queryKey: ['reference'] }),
      ]),
  });
};

export const useCreateCatalogItem = (): UseMutationResult<
  CatalogItem,
  unknown,
  { kind: CatalogKind; name: string }
> => useCatalogMutation(createCatalogItem);

export const useRenameCatalogItem = (): UseMutationResult<
  CatalogItem,
  unknown,
  { kind: CatalogKind; id: string; name: string }
> => useCatalogMutation(renameCatalogItem);

export const useSetCatalogItemActive = (): UseMutationResult<
  CatalogItem,
  unknown,
  { kind: CatalogKind; id: string; active: boolean }
> => useCatalogMutation(setCatalogItemActive);
