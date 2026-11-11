import {
  useMutation,
  useQuery,
  useQueryClient,
  type UseMutationResult,
  type UseQueryResult,
} from '@tanstack/react-query';

import {
  downloadDocumentTemplateFile,
  fetchDocumentTemplates,
  fetchTemplatePlaceholders,
  replaceDocumentTemplateFile,
  setDocumentTemplateActive,
  uploadDocumentTemplate,
  type DocumentTemplate,
  type DocumentTemplateUpload,
} from '@/config/api/documentTemplateApi';

const key = ['document-templates'] as const;

export const useDocumentTemplates = (): UseQueryResult<DocumentTemplate[]> =>
  useQuery({ queryKey: key, queryFn: fetchDocumentTemplates });

export const useTemplatePlaceholders = (): UseQueryResult<string[]> =>
  useQuery({ queryKey: [...key, 'placeholders'], queryFn: fetchTemplatePlaceholders, staleTime: Infinity });

/** Dopo ogni modifica si rilegge l'elenco, anche dopo un conflitto. */
const useTemplateMutation = <T, V>(mutationFn: (vars: V) => Promise<T>): UseMutationResult<T, unknown, V> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn,
    onSettled: () => queryClient.invalidateQueries({ queryKey: key, exact: true }),
  });
};

export const useUploadDocumentTemplate = (): UseMutationResult<
  DocumentTemplate,
  unknown,
  DocumentTemplateUpload
> => useTemplateMutation(uploadDocumentTemplate);

export const useReplaceDocumentTemplateFile = (): UseMutationResult<
  DocumentTemplate,
  unknown,
  { id: string; version: number; file: File }
> => useTemplateMutation(replaceDocumentTemplateFile);

export const useSetDocumentTemplateActive = (): UseMutationResult<
  DocumentTemplate,
  unknown,
  { id: string; active: boolean; version: number }
> => useTemplateMutation(setDocumentTemplateActive);

export const useDownloadDocumentTemplateFile = (): UseMutationResult<Blob, unknown, string> =>
  useMutation({ mutationFn: downloadDocumentTemplateFile });
