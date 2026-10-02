import { axiosInstance } from '@/config/api/axiosInstance';
import { ENDPOINTS } from '@/config/api/Endpoint';
import type { ReferenceItem } from '@/config/api/referenceApi';

export type JobSlotStatus = 'LIBERA' | 'BLOCCATA';

export type JobSlot = {
  id: string;
  companyId: string;
  title: string;
  description: string | null;
  jobCategory: ReferenceItem;
  zone: ReferenceItem;
  status: JobSlotStatus;
  /** Segnalazione che tiene bloccata la mansione. */
  blockedByTicketNumber: number | null;
  active: boolean;
  version: number;
};

export type JobSlotForm = {
  title: string;
  jobCategoryId: string;
  zoneId: string;
  description: string | null;
  version?: number;
};

export const fetchJobSlots = async (companyId: string): Promise<JobSlot[]> => {
  const { data } = await axiosInstance.get<JobSlot[]>(`${ENDPOINTS.companies}/${companyId}/job-slots`);
  return data;
};

export const createJobSlot = async ({
  companyId,
  payload,
}: {
  companyId: string;
  payload: JobSlotForm;
}): Promise<JobSlot> => {
  const { data } = await axiosInstance.post<JobSlot>(
    `${ENDPOINTS.companies}/${companyId}/job-slots`,
    payload,
  );
  return data;
};

export const updateJobSlot = async ({
  id,
  payload,
}: {
  id: string;
  payload: JobSlotForm;
}): Promise<JobSlot> => {
  const { data } = await axiosInstance.put<JobSlot>(`${ENDPOINTS.jobSlots}/${id}`, payload);
  return data;
};

export const setJobSlotActive = async ({
  id,
  version,
  active,
}: {
  id: string;
  version: number;
  active: boolean;
}): Promise<JobSlot> => {
  const { data } = await axiosInstance.post<JobSlot>(
    `${ENDPOINTS.jobSlots}/${id}/${active ? 'activate' : 'deactivate'}`,
    {
      version,
    },
  );
  return data;
};
