import { axiosInstance } from '@/config/api/axiosInstance';
import { ENDPOINTS } from '@/config/api/Endpoint';

export type CalendarClosure = { id: string; startDate: string; endDate: string; description: string };

export type WorkCalendar = {
  patronDay: number;
  patronMonth: number;
  version: number;
  closures: CalendarClosure[];
};

export type ClosureForm = { startDate: string; endDate: string | null; description: string };

export const fetchCalendar = async (): Promise<WorkCalendar> => {
  const { data } = await axiosInstance.get<WorkCalendar>(ENDPOINTS.calendar);
  return data;
};

export const updatePatron = async (payload: {
  day: number;
  month: number;
  version: number;
}): Promise<WorkCalendar> => {
  const { data } = await axiosInstance.put<WorkCalendar>(`${ENDPOINTS.calendar}/patron`, payload);
  return data;
};

export const addClosure = async (payload: ClosureForm): Promise<WorkCalendar> => {
  const { data } = await axiosInstance.post<WorkCalendar>(`${ENDPOINTS.calendar}/closures`, payload);
  return data;
};

export const removeClosure = async (id: string): Promise<WorkCalendar> => {
  const { data } = await axiosInstance.delete<WorkCalendar>(
    `${ENDPOINTS.calendar}/closures/${encodeURIComponent(id)}`,
  );
  return data;
};

export type DeadlinePreview = { from: string; reminder: string; deadline: string };

export const fetchDeadlinePreview = async (from: string): Promise<DeadlinePreview> => {
  const { data } = await axiosInstance.get<DeadlinePreview>(`${ENDPOINTS.calendar}/deadline-preview`, {
    params: { from },
  });
  return data;
};
