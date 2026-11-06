import { axiosInstance } from '@/config/api/axiosInstance';
import { ENDPOINTS } from '@/config/api/Endpoint';
import type { ReferenceItem } from '@/config/api/referenceApi';
import type { Role } from '@/config/auth/types';

/**
 * Utente di NEXUS nell'elenco dell'ADMIN (US-103), con i progetti assegnati per nome (US-104): contano solo per i Tutor.
 */
export type AdminUser = {
  id: string;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  role: Role;
  active: boolean;
  projects: ReferenceItem[];
  /** Progetti che il Tutor coordina (US-112). */
  coordinatedProjects?: ReferenceItem[];
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

export const fetchUser = async (id: string): Promise<AdminUser> => {
  const { data } = await axiosInstance.get<AdminUser>(`${ENDPOINTS.users}/${id}`);
  return data;
};

/** Progetti del Tutor scelti dall'ADMIN: tra questi, quelli che coordina (US-112). */
export type UserProjectsVars = { id: string; projectIds: string[]; coordinatedProjectIds: string[] };

/** Sostituisce i progetti del Tutor (US-104) e quelli che coordina (US-112); un elenco vuoto li toglie tutti. */
export const updateUserProjects = async ({
  id,
  projectIds,
  coordinatedProjectIds,
}: UserProjectsVars): Promise<AdminUser> => {
  const { data } = await axiosInstance.put<AdminUser>(`${ENDPOINTS.users}/${id}/projects`, {
    projectIds,
    coordinatedProjectIds,
  });
  return data;
};

/** Che cosa tocca disattivare l'utente o cambiargli il ruolo (US-105). */
export type UserImpact = {
  /** Segnalazioni aperte di cui è il Tutor: bloccano, prima vanno spostate. */
  openTickets: number;
  /** Ticket in carico come operatore: tornano in coda. */
  ticketsInCharge: number;
  /** È l'ADMIN collegato: non può toccare se stesso. */
  self: boolean;
};

export const fetchUserImpact = async (id: string): Promise<UserImpact> => {
  const { data } = await axiosInstance.get<UserImpact>(`${ENDPOINTS.users}/${id}/impact`);
  return data;
};

export const setUserActive = async ({ id, active }: { id: string; active: boolean }): Promise<AdminUser> => {
  const { data } = await axiosInstance.put<AdminUser>(`${ENDPOINTS.users}/${id}/active`, { active });
  return data;
};

export const changeUserRole = async ({ id, role }: { id: string; role: Role }): Promise<AdminUser> => {
  const { data } = await axiosInstance.put<AdminUser>(`${ENDPOINTS.users}/${id}/role`, { role });
  return data;
};
