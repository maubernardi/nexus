import { axiosInstance } from '@/config/api/axiosInstance';
import { ENDPOINTS } from '@/config/api/Endpoint';
import type { Role } from '@/config/auth/types';

/** Utente di NEXUS nell'elenco dell'ADMIN (US-103). */
export type AdminUser = {
  id: string;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  role: Role;
  active: boolean;
};

export type NewUserForm = {
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string | null;
  role: Role;
};

/** `invitationSent` falso: l'email per la password non è partita, si reinvia dall'elenco. */
export type UserCreated = {
  user: AdminUser;
  invitationSent: boolean;
};

export const fetchUsers = async (): Promise<AdminUser[]> => {
  const { data } = await axiosInstance.get<AdminUser[]>(ENDPOINTS.users);
  return data;
};

export const createUser = async (form: NewUserForm): Promise<UserCreated> => {
  const { data } = await axiosInstance.post<UserCreated>(ENDPOINTS.users, form);
  return data;
};

export const resendInvitation = async (id: string): Promise<void> => {
  await axiosInstance.post(`${ENDPOINTS.users}/${id}/invitation`);
};
