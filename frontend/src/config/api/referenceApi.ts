import { axiosInstance } from '@/config/api/axiosInstance';
import { ENDPOINTS } from '@/config/api/Endpoint';

export type ReferenceItem = { id: string; code: string; name: string };

export const fetchZones = async (): Promise<ReferenceItem[]> => {
  const { data } = await axiosInstance.get<ReferenceItem[]>(ENDPOINTS.zones);
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
