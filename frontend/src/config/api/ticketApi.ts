import { axiosInstance } from '@/config/api/axiosInstance';
import type { CandidateSummary } from '@/config/api/candidateApi';
import { ENDPOINTS } from '@/config/api/Endpoint';
import type { ReferenceItem } from '@/config/api/referenceApi';
import type { TicketStatus, TicketType } from '@/lib/domainValues';

export type TicketCreate = {
  candidateId: string;
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
  candidate: CandidateSummary;
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
