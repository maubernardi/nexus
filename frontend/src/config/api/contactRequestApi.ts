import { axiosInstance } from '@/config/api/axiosInstance';
import { ENDPOINTS } from '@/config/api/Endpoint';
import type { ReferenceItem } from '@/config/api/referenceApi';

/** Stato di una richiesta di contatto (US-507): la chiusura arriva con US-512. */
export type ContactRequestStatus = 'APERTA' | 'IN_CARICO' | 'CHIUSA';

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
  version: number;
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
