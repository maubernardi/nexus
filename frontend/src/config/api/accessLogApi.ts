import { axiosInstance } from '@/config/api/axiosInstance';
import { ENDPOINTS } from '@/config/api/Endpoint';

/** Un accesso a NEXUS dal registro di Keycloak (US-113); i campi vuoti l'API li omette. */
export type AccessLogEntry = {
  at: string;
  success: boolean;
  ipAddress?: string;
  /** Username o email scritti nel modulo di accesso. */
  username?: string;
  /** Nome dell'utente NEXUS, se lo username esiste. */
  userName?: string;
  /** Codice di Keycloak del tentativo fallito (es. `invalid_user_credentials`). */
  error?: string;
};

/** Una pagina, dal più recente; `hasMore` se ce ne sono di più vecchi. */
export type AccessLogPage = { entries: AccessLogEntry[]; hasMore: boolean };

const page = async (url: string, pageNumber: number): Promise<AccessLogPage> => {
  const { data } = await axiosInstance.get<AccessLogPage>(url, { params: { page: pageNumber } });
  return data;
};

export const fetchUserAccessLog = (userId: string, pageNumber: number): Promise<AccessLogPage> =>
  page(`${ENDPOINTS.users}/${userId}/access-log`, pageNumber);

export const fetchMyAccessLog = (pageNumber: number): Promise<AccessLogPage> =>
  page(ENDPOINTS.myAccessLog, pageNumber);

export const fetchFailedLogins = (pageNumber: number): Promise<AccessLogPage> =>
  page(ENDPOINTS.failedLogins, pageNumber);
