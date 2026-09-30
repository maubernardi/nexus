import axios, { AxiosHeaders } from 'axios';

import { API_BASE_URL, ENDPOINTS } from '@/config/api/Endpoint';
import { isUserNotEnabled } from '@/config/api/errors';
import { CURRENT_USER_QUERY_KEY, queryClient } from '@/config/api/queryClient';
import { getAuthHeaders, handleUnauthorized } from '@/config/auth/authService';

export const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 20_000,
  headers: { Accept: 'application/json' },
});

axiosInstance.interceptors.request.use(async (config) => {
  const headers = AxiosHeaders.from(config.headers);
  Object.entries(await getAuthHeaders()).forEach(([key, value]) => headers.set(key, value));
  config.headers = headers;
  return config;
});

axiosInstance.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      handleUnauthorized();
    }
    // utente disattivato durante la sessione: ricaricando il profilo l'app mostra la pagina dedicata. Mai per la
    // richiesta del profilo stessa: il suo 403 lo gestisce già la query, e invalidarla la ripeterebbe all'infinito
    if (isUserNotEnabled(error) && axios.isAxiosError(error) && error.config?.url !== ENDPOINTS.currentUser) {
      void queryClient.invalidateQueries({ queryKey: CURRENT_USER_QUERY_KEY });
    }
    return Promise.reject(error);
  },
);
