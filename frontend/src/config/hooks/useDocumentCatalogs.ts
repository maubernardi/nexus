import {
  useMutation,
  useQuery,
  useQueryClient,
  type UseMutationResult,
  type UseQueryResult,
} from '@tanstack/react-query';

import {
  fetchCareAgencies,
  fetchCompetenceSites,
  fetchProjectDocuments,
  fetchProjectInternshipTypes,
  setCompetenceSiteProjects,
  setProjectInternshipTypes,
  updateCareAgency,
  updateProjectDocuments,
  type CareAgency,
  type CareAgencyData,
  type CompetenceSite,
  type ProjectDocuments,
  type ProjectDocumentsUpdate,
  type ProjectInternshipTypes,
} from '@/config/api/documentCatalogApi';

const key = ['document-catalogs'] as const;

export const useCompetenceSites = (): UseQueryResult<CompetenceSite[]> =>
  useQuery({ queryKey: [...key, 'competence-sites'], queryFn: fetchCompetenceSites });

export const useProjectInternshipTypes = (): UseQueryResult<ProjectInternshipTypes[]> =>
  useQuery({ queryKey: [...key, 'project-internship-types'], queryFn: fetchProjectInternshipTypes });

export const useProjectDocuments = (): UseQueryResult<ProjectDocuments[]> =>
  useQuery({ queryKey: [...key, 'project-documents'], queryFn: fetchProjectDocuments });

export const useCareAgencies = (): UseQueryResult<CareAgency[]> =>
  useQuery({ queryKey: [...key, 'care-agencies'], queryFn: fetchCareAgencies });

/** Dopo ogni modifica si rileggono i dettagli e gli elenchi degli altri moduli. */
const useDocumentCatalogMutation = <T, V>(
  mutationFn: (vars: V) => Promise<T>,
): UseMutationResult<T, unknown, V> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn,
    onSettled: () =>
      Promise.all([
        queryClient.invalidateQueries({ queryKey: key }),
        queryClient.invalidateQueries({ queryKey: ['reference'] }),
      ]),
  });
};

export const useSetCompetenceSiteProjects = (): UseMutationResult<
  CompetenceSite,
  unknown,
  { id: string; ids: string[] }
> => useDocumentCatalogMutation(setCompetenceSiteProjects);

export const useSetProjectInternshipTypes = (): UseMutationResult<
  ProjectInternshipTypes,
  unknown,
  { id: string; ids: string[] }
> => useDocumentCatalogMutation(setProjectInternshipTypes);

export const useUpdateProjectDocuments = (): UseMutationResult<
  ProjectDocuments,
  unknown,
  ProjectDocumentsUpdate
> => useDocumentCatalogMutation(updateProjectDocuments);

export const useUpdateCareAgency = (): UseMutationResult<
  CareAgency,
  unknown,
  { id: string; version: number; data: CareAgencyData }
> => useDocumentCatalogMutation(updateCareAgency);
