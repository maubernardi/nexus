import { useQuery, type UseQueryResult } from '@tanstack/react-query';

import {
  fetchCareAgencies,
  fetchCompetenceSites,
  fetchHiringFormulas,
  fetchJobCategories,
  fetchMyProjects,
  fetchNoHireReasons,
  fetchProjects,
  fetchZones,
  type ReferenceItem,
} from '@/config/api/referenceApi';

// i dati di riferimento cambiano di rado: cache lunga
const STALE = 30 * 60_000;

export const useZones = (): UseQueryResult<ReferenceItem[]> =>
  useQuery({ queryKey: ['reference', 'zones'], queryFn: fetchZones, staleTime: STALE });

export const useJobCategories = (): UseQueryResult<ReferenceItem[]> =>
  useQuery({ queryKey: ['reference', 'jobCategories'], queryFn: fetchJobCategories, staleTime: STALE });

export const useHiringFormulas = (): UseQueryResult<ReferenceItem[]> =>
  useQuery({ queryKey: ['reference', 'hiringFormulas'], queryFn: fetchHiringFormulas, staleTime: STALE });

/** Enti di presa in carico attivi (US-206). */
export const useActiveCareAgencies = (): UseQueryResult<ReferenceItem[]> =>
  useQuery({ queryKey: ['reference', 'careAgencies'], queryFn: fetchCareAgencies, staleTime: STALE });

export const useNoHireReasons = (): UseQueryResult<ReferenceItem[]> =>
  useQuery({ queryKey: ['reference', 'noHireReasons'], queryFn: fetchNoHireReasons, staleTime: STALE });

/** Tutti i progetti attivi: solo per Call Center e ADMIN (per il Tutor l'API risponde 403). */
export const useProjects = (enabled = true): UseQueryResult<ReferenceItem[]> =>
  useQuery({ queryKey: ['reference', 'projects'], queryFn: fetchProjects, staleTime: STALE, enabled });

// le assegnazioni ai progetti le cambia l'ADMIN: niente cache lunga
export const useMyProjects = (enabled = true): UseQueryResult<ReferenceItem[]> =>
  useQuery({ queryKey: ['me', 'projects'], queryFn: fetchMyProjects, enabled });

/** Sedi di competenza attive del progetto scelto (US-308); nessuna richiesta senza progetto. */
export const useProjectCompetenceSites = (projectId: string): UseQueryResult<ReferenceItem[]> =>
  useQuery({
    queryKey: ['reference', 'competenceSites', projectId],
    queryFn: () => fetchCompetenceSites(projectId),
    enabled: projectId !== '',
    staleTime: STALE,
  });
