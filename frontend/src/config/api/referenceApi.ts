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
export const fetchInternshipTypes = async (): Promise<ReferenceItem[]> => {
  const { data } = await axiosInstance.get<ReferenceItem[]>(ENDPOINTS.internshipTypes);
  return data;
};

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
