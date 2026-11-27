import { axiosInstance } from '@/config/api/axiosInstance';
import { ENDPOINTS } from '@/config/api/Endpoint';
import type { ReferenceItem } from '@/config/api/referenceApi';

/** Stato di una richiesta di contatto (US-507); la chiusura con l'esito è di US-512. */
export type ContactRequestStatus = 'APERTA' | 'IN_CARICO' | 'CHIUSA';

/** Esito di un tentativo e della richiesta chiusa (US-512): fissi. */
export type ContactOutcome = 'DISPONIBILE' | 'NON_INTERESSATA' | 'NON_RAGGIUNGIBILE';
export const CONTACT_OUTCOMES: readonly ContactOutcome[] = [
  'DISPONIBILE',
  'NON_INTERESSATA',
  'NON_RAGGIUNGIBILE',
];

/**
 * Richiesta di contatto di un'azienda dal Tutor al Call Center (US-507), per un'azienda non ancora in anagrafica: nome,
 * zona e categoria come li ha scritti il Tutor. `companyId` c'è quando l'operatore, alla chiusura, la collega
 * all'anagrafica (US-512). L'API omette i campi vuoti.
 */
export type ContactRequest = {
  id: string;
  number: number;
  createdAt: string;
  requestedByName: string;
  companyId?: string;
  companyName: string;
  zone?: ReferenceItem;
  businessCategory?: ReferenceItem;
  status: ContactRequestStatus;
  assignedOperatorName?: string;
  takenAt?: string;
  /** Esito finale e istante della chiusura (US-512). */
  outcome?: ContactOutcome;
  closedAt?: string;
  version: number;
};

/** Tentativo di contatto (US-512); la nota è assente se l'operatore non l'ha scritta. */
export type ContactAttempt = {
  id: string;
  /** aaaa-mm-gg */
  date: string;
  outcome: ContactOutcome;
  note?: string;
  operatorName?: string;
  createdAt: string;
};

/** Pagina della richiesta (US-512): `assignedToMe` = l'utente la ha in carico, quindi segna i tentativi e la chiude. */
export type ContactRequestDetail = {
  request: ContactRequest;
  closingNote?: string;
  linkedCompanyName?: string;
  attempts?: ContactAttempt[];
  assignedToMe: boolean;
};

export type ContactAttemptForm = { date: string; outcome: ContactOutcome; note: string | null };

export type ContactRequestCloseForm = {
  version: number;
  outcome: ContactOutcome;
  note: string;
  companyId: string | null;
};

export const fetchContactRequest = async (id: string): Promise<ContactRequestDetail> => {
  const { data } = await axiosInstance.get<ContactRequestDetail>(`${ENDPOINTS.contactRequests}/${id}`);
  return data;
};

export const addContactAttempt = async ({
  id,
  form,
}: {
  id: string;
  form: ContactAttemptForm;
}): Promise<ContactRequestDetail> => {
  const { data } = await axiosInstance.post<ContactRequestDetail>(
    `${ENDPOINTS.contactRequests}/${id}/attempts`,
    form,
  );
  return data;
};

export const closeContactRequest = async ({
  id,
  form,
}: {
  id: string;
  form: ContactRequestCloseForm;
}): Promise<ContactRequestDetail> => {
  const { data } = await axiosInstance.post<ContactRequestDetail>(
    `${ENDPOINTS.contactRequests}/${id}/close`,
    form,
  );
  return data;
};

/** Richiesta per un'azienda non ancora in anagrafica (PO, 27/11): nome e zona, categoria facoltativa. */
export type ContactRequestForm = { companyName: string; zoneId: string; businessCategoryId: string | null };

export const createContactRequest = async (form: ContactRequestForm): Promise<ContactRequest> => {
  const { data } = await axiosInstance.post<ContactRequest>(ENDPOINTS.contactRequests, form);
  return data;
};

export const fetchMyContactRequests = async (): Promise<ContactRequest[]> => {
  const { data } = await axiosInstance.get<ContactRequest[]>(`${ENDPOINTS.contactRequests}/mine`);
  return data;
};

export const fetchContactRequestQueue = async (): Promise<ContactRequest[]> => {
  const { data } = await axiosInstance.get<ContactRequest[]>(ENDPOINTS.contactRequests);
  return data;
};

export const fetchOpenContactRequestCount = async (): Promise<number> => {
  const { data } = await axiosInstance.get<number>(`${ENDPOINTS.contactRequests}/open-count`);
  return data;
};

export const takeContactRequest = async ({
  id,
  version,
}: {
  id: string;
  version: number;
}): Promise<ContactRequest> => {
  const { data } = await axiosInstance.post<ContactRequest>(`${ENDPOINTS.contactRequests}/${id}/take`, {
    version,
  });
  return data;
};
