import { axiosInstance } from '@/config/api/axiosInstance';
import { ENDPOINTS } from '@/config/api/Endpoint';
import type { ReferenceItem } from '@/config/api/referenceApi';

/** Dettagli dei cataloghi per i documenti reali (US-115, US-1011), solo per l'ADMIN. */

export type CompetenceSite = {
  id: string;
  code: string;
  name: string;
  active: boolean;
  projects: ReferenceItem[];
};

/** Tipi ammessi da un progetto; vuoto = tutti. */
export type ProjectInternshipTypes = {
  project: ReferenceItem;
  active: boolean;
  internshipTypes: ReferenceItem[];
};

/**
 * Dati dei documenti di un progetto (US-1011): il riferimento all'avviso in vigore (`defaultNotice` = è il testo
 * predefinito) e la riga di intestazione con codice e CUP, omessa se manca.
 */
export type ProjectDocuments = {
  project: ReferenceItem;
  active: boolean;
  noticeReference: string;
  defaultNotice: boolean;
  headerLine?: string;
};

export const CARE_AGENCY_KINDS = [
  'COMUNE',
  'UNIONE_COMUNI',
  'COMUNITA_MONTANA',
  'SOCIETA_DELLA_SALUTE',
  'AZIENDA_SANITARIA',
  'UIEPE',
  'ALTRO',
] as const;
export type CareAgencyKind = (typeof CARE_AGENCY_KINDS)[number];

export type CareAgencyData = {
  kind: CareAgencyKind;
  fiscalCode?: string;
  legalAddress?: string;
  representativeName?: string;
  representativeRole?: string;
  representativeBirthPlace?: string;
  /** Data ISO. */
  representativeBirthDate?: string;
  referentName?: string;
  referentPhone?: string;
  referentEmail?: string;
};

export type CareAgency = CareAgencyData & {
  id: string;
  code: string;
  name: string;
  active: boolean;
  version: number;
};

const base = ENDPOINTS.documentCatalogs;

export const fetchCompetenceSites = async (): Promise<CompetenceSite[]> => {
  const { data } = await axiosInstance.get<CompetenceSite[]>(`${base}/competence-sites`);
  return data;
};

export const setCompetenceSiteProjects = async ({
  id,
  ids,
}: {
  id: string;
  ids: string[];
}): Promise<CompetenceSite> => {
  const { data } = await axiosInstance.put<CompetenceSite>(
    `${base}/competence-sites/${encodeURIComponent(id)}/projects`,
    { ids },
  );
  return data;
};

export const fetchProjectInternshipTypes = async (): Promise<ProjectInternshipTypes[]> => {
  const { data } = await axiosInstance.get<ProjectInternshipTypes[]>(`${base}/project-internship-types`);
  return data;
};

export const setProjectInternshipTypes = async ({
  id,
  ids,
}: {
  id: string;
  ids: string[];
}): Promise<ProjectInternshipTypes> => {
  const { data } = await axiosInstance.put<ProjectInternshipTypes>(
    `${base}/projects/${encodeURIComponent(id)}/internship-types`,
    { ids },
  );
  return data;
};

export const fetchProjectDocuments = async (): Promise<ProjectDocuments[]> => {
  const { data } = await axiosInstance.get<ProjectDocuments[]>(`${base}/project-documents`);
  return data;
};

/** Testi vuoti = avviso predefinito, nessuna intestazione. */
export const updateProjectDocuments = async ({
  id,
  noticeReference,
  headerLine,
}: {
  id: string;
  noticeReference: string | null;
  headerLine: string | null;
}): Promise<ProjectDocuments> => {
  const { data } = await axiosInstance.put<ProjectDocuments>(
    `${base}/projects/${encodeURIComponent(id)}/documents`,
    {
      noticeReference,
      headerLine,
    },
  );
  return data;
};

export const fetchCareAgencies = async (): Promise<CareAgency[]> => {
  const { data } = await axiosInstance.get<CareAgency[]>(`${base}/care-agencies`);
  return data;
};

export const updateCareAgency = async ({
  id,
  version,
  data: body,
}: {
  id: string;
  version: number;
  data: CareAgencyData;
}): Promise<CareAgency> => {
  const { data } = await axiosInstance.put<CareAgency>(`${base}/care-agencies/${encodeURIComponent(id)}`, {
    ...body,
    version,
  });
  return data;
};
