import { axiosInstance } from '@/config/api/axiosInstance';
import { ENDPOINTS } from '@/config/api/Endpoint';

export type ReferenceItem = { id: string; code: string; name: string };

export const fetchZones = async (): Promise<ReferenceItem[]> => {
  const { data } = await axiosInstance.get<ReferenceItem[]>(ENDPOINTS.zones);
  return data;
};

export const fetchJobCategories = async (): Promise<ReferenceItem[]> => {
  const { data } = await axiosInstance.get<ReferenceItem[]>(ENDPOINTS.jobCategories);
  return data;
};
