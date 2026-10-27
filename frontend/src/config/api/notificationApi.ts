import { axiosInstance } from '@/config/api/axiosInstance';
import { ENDPOINTS } from '@/config/api/Endpoint';

/** Tipi di notifica noti all'interfaccia; un tipo nuovo si mostra con un testo generico. */
export const NOTIFICATION_TYPES = [
  'CATEGORY_CHANGED',
  'CATEGORY_APPROVED',
  'PROPOSAL_SENT',
  'PROPOSAL_ACCEPTED',
  'APPOINTMENT_SET',
] as const;

export type AppNotification = {
  id: string;
  type: string;
  ticketId: string | null;
  ticketNumber: number | null;
  createdAt: string;
  read: boolean;
};

export type Notifications = { unread: number; items: AppNotification[] };

export const fetchNotifications = async (): Promise<Notifications> => {
  const { data } = await axiosInstance.get<Notifications>(ENDPOINTS.notifications);
  return data;
};

export const fetchUnreadCount = async (): Promise<number> => {
  const { data } = await axiosInstance.get<number>(`${ENDPOINTS.notifications}/unread-count`);
  return data;
};

export const markNotificationRead = async (id: string): Promise<void> => {
  await axiosInstance.post(`${ENDPOINTS.notifications}/${encodeURIComponent(id)}/read`);
};

export const markAllNotificationsRead = async (): Promise<void> => {
  await axiosInstance.post(`${ENDPOINTS.notifications}/read-all`);
};
