import { axiosInstance } from '@/config/api/axiosInstance';
import { ENDPOINTS } from '@/config/api/Endpoint';
import type { CurrentUser } from '@/config/auth/types';

export const fetchCurrentUser = async (): Promise<CurrentUser> => {
  const { data } = await axiosInstance.get<CurrentUser>(ENDPOINTS.currentUser);
  return data;
};

/** Cambio dell'indirizzo email in attesa del codice (US-110). */
export type PendingEmailChange = {
  newEmail: string;
  expiresAt: string;
  resendAvailableAt: string;
  remainingAttempts: number;
};

/** Indirizzo registrato in NEXUS; `pending` assente se non c'è un cambio in corso. */
export type EmailStatus = {
  email: string;
  pending?: PendingEmailChange;
};

export const fetchMyEmail = async (): Promise<EmailStatus> => {
  const { data } = await axiosInstance.get<EmailStatus>(ENDPOINTS.myEmail);
  return data;
};

/** Chiede il cambio: il codice di 6 cifre parte verso il nuovo indirizzo (anche per «invia un nuovo codice»). */
export const requestEmailChange = async (newEmail: string): Promise<EmailStatus> => {
  const { data } = await axiosInstance.post<EmailStatus>(ENDPOINTS.emailChange, { newEmail });
  return data;
};

export const confirmEmailChange = async (code: string): Promise<EmailStatus> => {
  const { data } = await axiosInstance.post<EmailStatus>(`${ENDPOINTS.emailChange}/confirm`, { code });
  return data;
};

export const cancelEmailChange = async (): Promise<EmailStatus> => {
  const { data } = await axiosInstance.delete<EmailStatus>(ENDPOINTS.emailChange);
  return data;
};
