import { axiosInstance } from '@/config/api/axiosInstance';
import type { BeneficiarySummary } from '@/config/api/beneficiaryApi';
import { ENDPOINTS } from '@/config/api/Endpoint';
import type { ReferenceItem } from '@/config/api/referenceApi';
import type { TicketStatus, TicketType } from '@/lib/domainValues';

export type TicketCreate = {
  beneficiaryId: string;
  projectId: string;
  jobCategoryId: string;
};

export type Ticket = {
  id: string;
  number: number;
  type: TicketType;
  status: TicketStatus;
  fastTrack: boolean;
  /** Da rimandare nelle richieste di cambio di stato (blocco ottimistico). */
  version: number;
  beneficiary: BeneficiarySummary;
  project: ReferenceItem;
  requestedJobCategory: ReferenceItem | null;
  createdAt: string;
};

export const submitTicket = async (payload: TicketCreate): Promise<Ticket> => {
  const { data } = await axiosInstance.post<Ticket>(ENDPOINTS.tickets, payload);
  return data;
};

export type QueueItem = Ticket & { tutorName: string };

export type QueueFilters = { projectId?: string; zoneId?: string };

export const fetchQueue = async (filters: QueueFilters): Promise<QueueItem[]> => {
  const { data } = await axiosInstance.get<QueueItem[]>(ENDPOINTS.ticketQueue, { params: filters });
  return data;
};

export const takeCharge = async ({ id, version }: { id: string; version: number }): Promise<QueueItem> => {
  const { data } = await axiosInstance.post<QueueItem>(`${ENDPOINTS.tickets}/${id}/take-charge`, { version });
  return data;
};

export const fetchAssignedToMe = async (): Promise<QueueItem[]> => {
  const { data } = await axiosInstance.get<QueueItem[]>(ENDPOINTS.ticketsAssignedToMe);
  return data;
};

export type JobSlotMatch = {
  id: string;
  title: string;
  description: string | null;
  companyId: string;
  companyName: string;
  jobCategory: ReferenceItem;
  zone: ReferenceItem;
  version: number;
};

export type TicketDetail = {
  id: string;
  number: number;
  type: TicketType;
  status: TicketStatus;
  fastTrack: boolean;
  version: number;
  beneficiary: BeneficiarySummary;
  beneficiaryZone: ReferenceItem;
  project: ReferenceItem;
  requestedJobCategory: ReferenceItem | null;
  requestedJobFreeText: string | null;
  tutorName: string;
  assignedOperatorName: string | null;
  assignedToMe: boolean;
  proposal: JobSlotMatch | null;
  createdAt: string;
};

export type CompatibleFilters = { zoneId?: string; jobCategoryId?: string };

export const fetchTicketWork = async (id: string): Promise<TicketDetail> => {
  const { data } = await axiosInstance.get<TicketDetail>(`${ENDPOINTS.tickets}/${id}/work`);
  return data;
};

export const fetchCompatibleJobSlots = async (
  id: string,
  filters: CompatibleFilters,
): Promise<JobSlotMatch[]> => {
  const { data } = await axiosInstance.get<JobSlotMatch[]>(
    `${ENDPOINTS.tickets}/${id}/compatible-job-slots`,
    {
      params: filters,
    },
  );
  return data;
};

export const matchTicket = async ({
  id,
  jobSlotId,
  ticketVersion,
  jobSlotVersion,
}: {
  id: string;
  jobSlotId: string;
  ticketVersion: number;
  jobSlotVersion: number;
}): Promise<TicketDetail> => {
  const { data } = await axiosInstance.post<TicketDetail>(`${ENDPOINTS.tickets}/${id}/match`, {
    jobSlotId,
    ticketVersion,
    jobSlotVersion,
  });
  return data;
};
