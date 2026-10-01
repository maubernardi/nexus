import axios from 'axios';

import type { ApiError } from '@/config/api/types';

/** Corpo di errore uniforme del backend, se la risposta ne contiene uno. */
export const apiError = (error: unknown): ApiError | null =>
  axios.isAxiosError<ApiError>(error) && error.response?.data && typeof error.response.data === 'object'
    ? error.response.data
    : null;

/** Converte i nomi di campo del backend (es. `languages[0].level`) nei percorsi di react-hook-form (`languages.0.level`). */
export const toFormPath = (field: string): string => field.replace(/\[(\d+)\]/g, '.$1');

export const USER_NOT_ENABLED = 'USER_NOT_ENABLED';

/** Utente autenticato ma non censito in NEXUS o disattivato (403 con codice dedicato). */
export const isUserNotEnabled = (error: unknown): boolean => apiError(error)?.code === USER_NOT_ENABLED;
