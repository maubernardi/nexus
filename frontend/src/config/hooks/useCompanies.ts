import {
  keepPreviousData,
  useMutation,
  useQuery,
  useQueryClient,
  type UseMutationResult,
  type UseQueryResult,
} from '@tanstack/react-query';

import {
  createCompany,
  fetchCompany,
  searchCompanies,
  setCompanyActive,
  updateCompany,
  type Company,
  type CompanyForm,
  type CompanySearch,
} from '@/config/api/companyApi';

export const COMPANIES_KEY = ['companies'] as const;

export const useCompanySearch = (params: CompanySearch): UseQueryResult<Company[]> =>
  useQuery({
    queryKey: [...COMPANIES_KEY, 'search', params],
    queryFn: () => searchCompanies(params),
    placeholderData: keepPreviousData,
  });

export const useCompany = (id: string): UseQueryResult<Company> =>
  useQuery({ queryKey: [...COMPANIES_KEY, id], queryFn: () => fetchCompany(id) });

const useCompanyMutation = <V>(
  mutationFn: (vars: V) => Promise<Company>,
): UseMutationResult<Company, unknown, V> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn,
    onSuccess: (company) => {
      queryClient.setQueryData([...COMPANIES_KEY, company.id], company);
      return queryClient.invalidateQueries({ queryKey: [...COMPANIES_KEY, 'search'] });
    },
  });
};

export const useCreateCompany = (): UseMutationResult<Company, unknown, CompanyForm> =>
  useCompanyMutation(createCompany);

export const useUpdateCompany = (): UseMutationResult<
  Company,
  unknown,
  { id: string; payload: CompanyForm }
> => useCompanyMutation(updateCompany);

export const useSetCompanyActive = (): UseMutationResult<
  Company,
  unknown,
  { id: string; version: number; active: boolean }
> => useCompanyMutation(setCompanyActive);
