import { axiosInstance } from '@/config/api/axiosInstance';
import { ENDPOINTS } from '@/config/api/Endpoint';
import type { ReferenceItem } from '@/config/api/referenceApi';

/** Documenti che hanno modelli caricati dall'ADMIN (US-1010). */
export const TEMPLATE_DOCUMENTS = ['CONVENZIONE', 'PROGETTO_FORMATIVO', 'RELAZIONE_FINALE'] as const;
export type TemplateDocument = (typeof TEMPLATE_DOCUMENTS)[number];

/** Modello Word caricato dall'ADMIN. L'API omette i campi vuoti: senza `careAgency` vale per tutti gli enti. */
export type DocumentTemplate = {
  id: string;
  document: TemplateDocument;
  project: ReferenceItem;
  internshipType: ReferenceItem;
  careAgency?: ReferenceItem;
  name: string;
  fileName: string;
  sizeBytes: number;
  active: boolean;
  uploadedAt: string;
  updatedAt: string;
  version: number;
};

export type DocumentTemplateUpload = {
  file: File;
  document: TemplateDocument;
  projectId: string;
  internshipTypeId: string;
  /** Vuoto = tutti gli enti. */
  careAgencyId: string;
  name: string;
};

const base = ENDPOINTS.documentTemplates;

export const fetchDocumentTemplates = async (): Promise<DocumentTemplate[]> => {
  const { data } = await axiosInstance.get<DocumentTemplate[]>(base);
  return data;
};

/** Segnaposto che i modelli possono usare. */
export const fetchTemplatePlaceholders = async (): Promise<string[]> => {
  const { data } = await axiosInstance.get<string[]>(`${base}/placeholders`);
  return data;
};

export const uploadDocumentTemplate = async (upload: DocumentTemplateUpload): Promise<DocumentTemplate> => {
  const body = new FormData();
  body.append('file', upload.file);
  body.append('document', upload.document);
  body.append('projectId', upload.projectId);
  body.append('internshipTypeId', upload.internshipTypeId);
  if (upload.careAgencyId) body.append('careAgencyId', upload.careAgencyId);
  body.append('name', upload.name);
  const { data } = await axiosInstance.post<DocumentTemplate>(base, body);
  return data;
};

export const replaceDocumentTemplateFile = async ({
  id,
  version,
  file,
}: {
  id: string;
  version: number;
  file: File;
}): Promise<DocumentTemplate> => {
  const body = new FormData();
  body.append('file', file);
  body.append('version', String(version));
  const { data } = await axiosInstance.put<DocumentTemplate>(`${base}/${encodeURIComponent(id)}/file`, body);
  return data;
};

export const setDocumentTemplateActive = async ({
  id,
  active,
  version,
}: {
  id: string;
  active: boolean;
  version: number;
}): Promise<DocumentTemplate> => {
  const { data } = await axiosInstance.put<DocumentTemplate>(`${base}/${encodeURIComponent(id)}/active`, {
    active,
    version,
  });
  return data;
};

export const downloadDocumentTemplateFile = async (id: string): Promise<Blob> => {
  const { data } = await axiosInstance.get<Blob>(`${base}/${encodeURIComponent(id)}/file`, {
    responseType: 'blob',
  });
  return data;
};
