import { axiosInstance } from '@/config/api/axiosInstance';
import { ENDPOINTS } from '@/config/api/Endpoint';
import type { ReferenceItem } from '@/config/api/referenceApi';
import type { TicketListItem } from '@/config/api/ticketApi';

/** Tutor che può ricevere una segnalazione spostata (US-111). */
export type TutorOption = { id: string; name: string };

/** Segnalazioni aperte di un Tutor in un progetto, con i Tutor attivi dello stesso progetto che possono riceverle. */
export type TransferGroup = {
  project: ReferenceItem;
  tickets: TicketListItem[];
  targets: TutorOption[];
};

export type TransferResult = { moved: number; tutorName: string };

export const fetchOpenTickets = async (userId: string): Promise<TransferGroup[]> => {
  const { data } = await axiosInstance.get<TransferGroup[]>(`${ENDPOINTS.users}/${userId}/open-tickets`);
  return data;
};

/** Segnalazioni aperte dei progetti che l'utente coordina, con i Tutor che possono riceverle (US-112). */
export const fetchCoordination = async (): Promise<TransferGroup[]> => {
  const { data } = await axiosInstance.get<TransferGroup[]>(ENDPOINTS.myCoordination);
  return data;
};

/** Sposta segnalazioni aperte dello stesso progetto sul Tutor indicato, con i loro beneficiari (US-111). */
export const transferTickets = async (body: {
  ticketIds: string[];
  tutorId: string;
}): Promise<TransferResult> => {
  const { data } = await axiosInstance.post<TransferResult>(`${ENDPOINTS.tickets}/transfer`, body);
  return data;
};
