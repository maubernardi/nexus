import { axiosInstance } from '@/config/api/axiosInstance';
import { ENDPOINTS } from '@/config/api/Endpoint';
import type { CurrentUser } from '@/config/auth/types';

export const fetchCurrentUser = async (): Promise<CurrentUser> => {
  const { data } = await axiosInstance.get<CurrentUser>(ENDPOINTS.currentUser);
  return data;
};
