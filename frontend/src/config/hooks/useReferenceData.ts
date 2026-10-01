import { useQuery, type UseQueryResult } from '@tanstack/react-query';

import { fetchJobCategories, fetchZones, type ReferenceItem } from '@/config/api/referenceApi';

// i dati di riferimento cambiano di rado: cache lunga
const STALE = 30 * 60_000;

export const useZones = (): UseQueryResult<ReferenceItem[]> =>
  useQuery({ queryKey: ['reference', 'zones'], queryFn: fetchZones, staleTime: STALE });

export const useJobCategories = (): UseQueryResult<ReferenceItem[]> =>
  useQuery({ queryKey: ['reference', 'jobCategories'], queryFn: fetchJobCategories, staleTime: STALE });
