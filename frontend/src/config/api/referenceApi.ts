import { axiosInstance } from '@/config/api/axiosInstance';
import { ENDPOINTS } from '@/config/api/Endpoint';

export type ReferenceItem = { id: string; code: string; name: string };

export const fetchZones = async (): Promise<ReferenceItem[]> => {
  const { data } = await axiosInstance.get<ReferenceItem[]>(ENDPOINTS.zones);
  return data;
};

/** Tutti i progetti attivi (filtri del Call Center e dell'ADMIN). */
export const fetchProjects = async (): Promise<ReferenceItem[]> => {
  const { data } = await axiosInstance.get<ReferenceItem[]>(ENDPOINTS.projects);
  return data;
};

/** Progetti attivi a cui è assegnato l'utente corrente. */
export const fetchMyProjects = async (): Promise<ReferenceItem[]> => {
  const { data } = await axiosInstance.get<ReferenceItem[]>(ENDPOINTS.myProjects);
  return data;
};

export const fetchJobCategories = async (): Promise<ReferenceItem[]> => {
  const { data } = await axiosInstance.get<ReferenceItem[]>(ENDPOINTS.jobCategories);
  return data;
};

/** Tipi di tirocinio attivi (US-108), per la scheda del tirocinio. */

/** Formule di assunzione attive (US-109), per il report di restituzione. */
export const fetchHiringFormulas = async (): Promise<ReferenceItem[]> => {
  const { data } = await axiosInstance.get<ReferenceItem[]>(ENDPOINTS.hiringFormulas);
  return data;
};

/** Motivi attivi della mancata assunzione (US-109), per il report di restituzione. */
export const fetchNoHireReasons = async (): Promise<ReferenceItem[]> => {
  const { data } = await axiosInstance.get<ReferenceItem[]>(ENDPOINTS.noHireReasons);
  return data;
};

/** Motivi di interruzione anticipata attivi (US-1103): il catalogo dei motivi di chiusura senza la chiusura regolare. */
export const fetchInterruptionReasons = async (): Promise<ReferenceItem[]> => {
  const { data } = await axiosInstance.get<ReferenceItem[]>(ENDPOINTS.interruptionReasons);
  return data;
};

/** Categorie merceologiche attive (US-509): solo Call Center e ADMIN. */
export const fetchBusinessCategories = async (): Promise<ReferenceItem[]> => {
  const { data } = await axiosInstance.get<ReferenceItem[]>(ENDPOINTS.businessCategories);
  return data;
};

/** Ruoli dei referenti attivi (US-510): solo Call Center e ADMIN. */
export const fetchContactRoles = async (): Promise<ReferenceItem[]> => {
  const { data } = await axiosInstance.get<ReferenceItem[]>(ENDPOINTS.contactRoles);
  return data;
};

/** Enti di presa in carico attivi (US-206). */
export const fetchCareAgencies = async (): Promise<ReferenceItem[]> => {
  const { data } = await axiosInstance.get<ReferenceItem[]>(ENDPOINTS.careAgencies);
  return data;
};

/** Sedi di competenza attive di un progetto (US-308). */
/** Tipi di tirocinio attivi ammessi dal progetto (US-605), per i moduli di segnalazione. */
export const fetchProjectInternshipTypes = async (projectId: string): Promise<ReferenceItem[]> => {
  const { data } = await axiosInstance.get<ReferenceItem[]>(ENDPOINTS.internshipTypes, {
    params: { projectId },
  });
  return data;
};

export const fetchCompetenceSites = async (projectId: string): Promise<ReferenceItem[]> => {
  const { data } = await axiosInstance.get<ReferenceItem[]>(ENDPOINTS.competenceSites, {
    params: { projectId },
  });
  return data;
};
