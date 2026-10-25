import { useQuery, type UseQueryResult } from '@tanstack/react-query';

import { fetchCompanyHistory, type CompanyHistory } from '@/config/api/companyHistoryApi';

export const companyHistoryKey = (companyId: string) => ['companies', companyId, 'history'] as const;

export const useCompanyHistory = (companyId: string): UseQueryResult<CompanyHistory> =>
  useQuery({ queryKey: companyHistoryKey(companyId), queryFn: () => fetchCompanyHistory(companyId) });
