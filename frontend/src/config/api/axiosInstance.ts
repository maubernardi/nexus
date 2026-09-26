import axios, { AxiosHeaders } from 'axios';

import { API_BASE_URL } from '@/config/api/Endpoint';
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
    return Promise.reject(error);
  },
);
