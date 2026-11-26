import { axiosInstance } from '@/config/api/axiosInstance';
import { ENDPOINTS } from '@/config/api/Endpoint';

export type AppNotification = {
  id: string;
  type: string;
  ticketId: string | null;
  ticketNumber: number | null;
  /** Azienda a cui porta l'avviso, invece che alla segnalazione (US-515); assente negli altri. */
  companyId?: string | null;
  createdAt: string;
  read: boolean;
  /** Testo della notifica, quello dell'ADMIN o il predefinito (US-1204); manca solo nelle risposte vecchie. */
  text?: string;
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
