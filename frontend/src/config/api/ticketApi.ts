import { axiosInstance } from '@/config/api/axiosInstance';
import type { BeneficiarySummary } from '@/config/api/beneficiaryApi';
import { ENDPOINTS } from '@/config/api/Endpoint';
import type { ReferenceItem } from '@/config/api/referenceApi';
import type { AppointmentFailureCause, TicketStatus, TicketType } from '@/lib/domainValues';

/** Tipologia del catalogo (`jobCategoryId`) oppure nuova tipologia da approvare (`jobFreeText`, US-302). */
export type TicketCreate = {
  beneficiaryId: string;
  projectId: string;
  jobCategoryId: string | null;
  jobFreeText: string | null;
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

/** Campi dell'elenco per la nuova tipologia (US-302). */
type NewCategoryInfo = {
  /** Nuova tipologia proposta dal Tutor, in attesa di approvazione. */
  requestedJobFreeText: string | null;
};

export const submitTicket = async (payload: TicketCreate): Promise<Ticket> => {
  const { data } = await axiosInstance.post<Ticket>(ENDPOINTS.tickets, payload);
  return data;
};

/** Riga dell'elenco delle segnalazioni (Home, US-303). */
export type TicketListItem = Ticket &
  NewCategoryInfo & { tutorName: string; assignedOperatorName: string | null };

/** Viste dell'elenco: MINE e PROJECT per il Tutor, le altre per Call Center e ADMIN. */
export type TicketListView = 'MINE' | 'PROJECT' | 'TO_TAKE' | 'TO_APPROVE' | 'ASSIGNED_TO_ME' | 'ALL';

export type TicketListQuery = { view: TicketListView; status?: TicketStatus; projectId?: string };

export const fetchTickets = async (query: TicketListQuery): Promise<TicketListItem[]> => {
  const { data } = await axiosInstance.get<TicketListItem[]>(ENDPOINTS.tickets, { params: query });
  return data;
};

export const takeCharge = async ({
  id,
  version,
}: {
  id: string;
  version: number;
}): Promise<TicketListItem> => {
  const { data } = await axiosInstance.post<TicketListItem>(`${ENDPOINTS.tickets}/${id}/take-charge`, {
    version,
  });
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
  /** L'utente corrente è il Tutor della segnalazione: può accettare la proposta (US-701). */
  myTicket: boolean;
  /** Ultimo giorno lavorativo del timer della proposta accettata (aaaa-mm-gg), se attivo. */
  timerDeadline: string | null;
};

export type CompatibleFilters = { zoneId?: string; jobCategoryId?: string };

export const fetchTicket = async (id: string): Promise<TicketDetail> => {
  const { data } = await axiosInstance.get<TicketDetail>(`${ENDPOINTS.tickets}/${id}`);
  return data;
};

/** Voce della cronologia (US-305); `action` è il nome della transizione o dell'azione registrata nell'audit. */
export type TicketEvent = {
  at: string;
  authorName: string;
  action: string;
  fromStatus: TicketStatus | null;
  toStatus: TicketStatus | null;
  detail: string | null;
  note: string | null;
};

export const fetchTicketHistory = async (id: string): Promise<TicketEvent[]> => {
  const { data } = await axiosInstance.get<TicketEvent[]>(`${ENDPOINTS.tickets}/${id}/history`);
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

export const takeOverTicket = async ({
  id,
  version,
}: {
  id: string;
  version: number;
}): Promise<TicketDetail> => {
  const { data } = await axiosInstance.post<TicketDetail>(`${ENDPOINTS.tickets}/${id}/take-over`, {
    version,
  });
  return data;
};

export const acceptProposal = async ({
  id,
  version,
}: {
  id: string;
  version: number;
}): Promise<TicketDetail> => {
  const { data } = await axiosInstance.post<TicketDetail>(`${ENDPOINTS.tickets}/${id}/accept`, { version });
  return data;
};

export const approveCategory = async ({
  id,
  name,
  version,
}: {
  id: string;
  name: string;
  version: number;
}): Promise<TicketDetail> => {
  const { data } = await axiosInstance.post<TicketDetail>(`${ENDPOINTS.tickets}/${id}/category/approve`, {
    name,
    version,
  });
  return data;
};

export const rejectCategory = async ({
  id,
  jobCategoryId,
  reason,
  version,
}: {
  id: string;
  jobCategoryId: string;
  reason: string;
  version: number;
}): Promise<TicketDetail> => {
  const { data } = await axiosInstance.post<TicketDetail>(`${ENDPOINTS.tickets}/${id}/category/reject`, {
    jobCategoryId,
    reason,
    version,
  });
  return data;
};

export const appointmentSet = async ({
  id,
  version,
  note,
}: {
  id: string;
  version: number;
  note: string | null;
}): Promise<TicketDetail> => {
  const { data } = await axiosInstance.post<TicketDetail>(`${ENDPOINTS.tickets}/${id}/appointment-set`, {
    version,
    note,
  });
  return data;
};

export const appointmentFailed = async ({
  id,
  version,
  cause,
  note,
}: {
  id: string;
  version: number;
  cause: AppointmentFailureCause;
  note: string | null;
}): Promise<TicketDetail> => {
  const { data } = await axiosInstance.post<TicketDetail>(`${ENDPOINTS.tickets}/${id}/appointment-failed`, {
    version,
    cause,
    note,
  });
  return data;
};
