import { axiosInstance } from '@/config/api/axiosInstance';
import type { BeneficiarySummary } from '@/config/api/beneficiaryApi';
import type { BoardPostStatus } from '@/config/api/boardPostApi';
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

/** Candidatura di un beneficiario del Tutor a un annuncio della bacheca (US-306): nasce una segnalazione speciale. */
export type TicketSpecialCreate = {
  boardPostId: string;
  beneficiaryId: string;
  /** Solo per un annuncio aperto a tutti: per uno riservato vale il progetto dell'annuncio. */
  projectId: string | null;
};

export const submitSpecialTicket = async (payload: TicketSpecialCreate): Promise<Ticket> => {
  const { data } = await axiosInstance.post<Ticket>(`${ENDPOINTS.tickets}/special`, payload);
  return data;
};

export const submitTicket = async (payload: TicketCreate): Promise<Ticket> => {
  const { data } = await axiosInstance.post<Ticket>(ENDPOINTS.tickets, payload);
  return data;
};

/** Riga dell'elenco delle segnalazioni (Home, US-303). */
export type TicketListItem = Ticket &
  NewCategoryInfo & {
    tutorName: string;
    assignedOperatorName: string | null;
    /** Ultimo giorno utile del timer attivo (aaaa-mm-gg), null senza scadenza (US-307). */
    timerDeadline: string | null;
    /** Promemoria del timer già inviato: scadenza vicina (US-307). */
    timerDueSoon: boolean;
  };

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
  /** Ultimo annuncio nato dal rilancio in bacheca di questa segnalazione (US-803); l'API omette i campi vuoti. */
  relaunchPostId?: string | null;
  relaunchPostNumber?: number | null;
  relaunchPostStatus?: BoardPostStatus | null;
  /** Annuncio della bacheca a cui è candidata la segnalazione speciale (US-306). */
  boardPostId?: string | null;
  boardPostNumber?: number | null;
  /** Report di restituzione inviato (US-1101), solo in FORM_RESTITUZIONE. */
  closureReport?: ClosureReport;
};

/**
 * Report di restituzione inviato (US-1101), non più modificabile. L'API omette i campi vuoti: testi non scritti e la
 * voce dell'esito non scelto (formula se non assunto, motivo se assunto).
 */
export type ClosureReport = {
  companySatisfaction: number;
  beneficiarySatisfaction: number;
  objectives?: string;
  difficulties?: string;
  hired: boolean;
  hiringFormula?: ReferenceItem;
  noHireReason?: ReferenceItem;
  jobSlotStillAvailable: boolean;
  submittedAt: string;
  submittedBy: string;
};

/** Report di restituzione dal modulo (US-1101): conta la formula se assunto, il motivo se non assunto. */
export type ClosureReportForm = {
  companySatisfaction: number;
  beneficiarySatisfaction: number;
  objectives: string | null;
  difficulties: string | null;
  hired: boolean;
  hiringFormulaId: string | null;
  noHireReasonId: string | null;
  jobSlotStillAvailable: boolean;
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

/** Il Call Center approva la corsia veloce (US-403): proposta con la mansione dell'annuncio, annuncio archiviato. */
export const approveFastTrack = async ({
  id,
  version,
}: {
  id: string;
  version: number;
}): Promise<TicketDetail> => {
  const { data } = await axiosInstance.post<TicketDetail>(`${ENDPOINTS.tickets}/${id}/fast-track/approve`, {
    version,
  });
  return data;
};

/** Il Call Center rifiuta la corsia veloce con il motivo (US-404): resta in lavorazione normale. */
export const rejectFastTrack = async ({
  id,
  version,
  reason,
}: {
  id: string;
  version: number;
  reason: string;
}): Promise<TicketDetail> => {
  const { data } = await axiosInstance.post<TicketDetail>(`${ENDPOINTS.tickets}/${id}/fast-track/reject`, {
    version,
    reason,
  });
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

/** Il Tutor della segnalazione avvia il tirocinio (US-1003): solo con la scheda completa. */
export const startInternship = async ({
  id,
  version,
}: {
  id: string;
  version: number;
}): Promise<TicketDetail> => {
  const { data } = await axiosInstance.post<TicketDetail>(`${ENDPOINTS.tickets}/${id}/start`, { version });
  return data;
};

/** Il Tutor della segnalazione invia il report di restituzione (US-1101): il percorso si chiude. */
export const submitClosureReport = async ({
  id,
  version,
  report,
}: {
  id: string;
  version: number;
  report: ClosureReportForm;
}): Promise<TicketDetail> => {
  const { data } = await axiosInstance.post<TicketDetail>(`${ENDPOINTS.tickets}/${id}/closure-report`, {
    version,
    ...report,
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
  relaunch,
}: {
  id: string;
  version: number;
  cause: AppointmentFailureCause;
  note: string | null;
  /** Chiede il rilancio della mansione in bacheca (US-803). */
  relaunch: boolean;
}): Promise<TicketDetail> => {
  const { data } = await axiosInstance.post<TicketDetail>(`${ENDPOINTS.tickets}/${id}/appointment-failed`, {
    version,
    cause,
    note,
    relaunch,
  });
  return data;
};

export const rejectProposal = async ({
  id,
  version,
  reason,
}: {
  id: string;
  version: number;
  reason: string;
}): Promise<TicketDetail> => {
  const { data } = await axiosInstance.post<TicketDetail>(`${ENDPOINTS.tickets}/${id}/reject`, {
    version,
    reason,
  });
  return data;
};

export type CompanyExclusion = {
  companyId: string;
  companyName: string;
  reason: string | null;
  createdAt: string;
  authorName: string;
};

export const fetchExclusions = async (id: string): Promise<CompanyExclusion[]> => {
  const { data } = await axiosInstance.get<CompanyExclusion[]>(`${ENDPOINTS.tickets}/${id}/exclusions`);
  return data;
};

export const excludeCompany = async ({
  id,
  companyId,
  reason,
}: {
  id: string;
  companyId: string;
  reason: string;
}): Promise<CompanyExclusion[]> => {
  const { data } = await axiosInstance.post<CompanyExclusion[]>(`${ENDPOINTS.tickets}/${id}/exclusions`, {
    companyId,
    reason,
  });
  return data;
};
