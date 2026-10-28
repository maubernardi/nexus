import { axiosInstance } from '@/config/api/axiosInstance';
import { ENDPOINTS } from '@/config/api/Endpoint';
import type { ReferenceItem } from '@/config/api/referenceApi';
import type { JobSlotMatch } from '@/config/api/ticketApi';

export type BoardPostStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';

/** Annuncio della bacheca per Call Center e ADMIN (US-902): contiene l'azienda, mai mostrata al Tutor. */
export type BoardPost = {
  id: string;
  number: number;
  title: string;
  status: BoardPostStatus;
  jobSlot: JobSlotMatch;
  /** null = visibile a tutti i progetti. */
  project: ReferenceItem | null;
  ageMin: number | null;
  ageMax: number | null;
  weeklyHours: number | null;
  durationMonths: number | null;
  notes: string | null;
  publishedAt: string | null;
  madePublicAt: string | null;
  /** Segnalazione del rilancio da cui è nato l'annuncio. */
  createdFromTicketId: string | null;
  createdFromTicketNumber: number | null;
  createdAt: string;
  version: number;
  /** Da quando l'annuncio riservato si può rendere visibile a tutti (7 giorni dalla pubblicazione, US-903). */
  makePublicFrom?: string | null;
};

export type BoardPostForm = {
  title: string;
  projectId: string | null;
  ageMin: number | null;
  ageMax: number | null;
  weeklyHours: number | null;
  durationMonths: number | null;
  notes: string | null;
  version: number;
};

/**
 * Annuncio come lo vede il Tutor (US-901): mai azienda, titolo né note. {@code project} è il suo progetto a cui è
 * riservato; l'API omette i campi vuoti.
 */
export type TutorBoardPost = {
  id: string;
  number: number;
  jobCategory: ReferenceItem;
  zone: ReferenceItem;
  ageMin?: number | null;
  ageMax?: number | null;
  weeklyHours?: number | null;
  durationMonths?: number | null;
  project?: ReferenceItem | null;
  publishedAt: string;
};

export type TutorBoardFilters = { zoneId?: string; jobCategoryId?: string };

export const fetchTutorBoard = async (filters: TutorBoardFilters): Promise<TutorBoardPost[]> => {
  const { data } = await axiosInstance.get<TutorBoardPost[]>(ENDPOINTS.board, { params: filters });
  return data;
};

export const fetchBoardPosts = async (status: BoardPostStatus): Promise<BoardPost[]> => {
  const { data } = await axiosInstance.get<BoardPost[]>(ENDPOINTS.boardPosts, { params: { status } });
  return data;
};

export const fetchBoardPost = async (id: string): Promise<BoardPost> => {
  const { data } = await axiosInstance.get<BoardPost>(`${ENDPOINTS.boardPosts}/${id}`);
  return data;
};

/** Bozza da una mansione libera (US-904): titolo della mansione, visibile a tutti i progetti. */
export const createBoardPost = async (jobSlotId: string): Promise<BoardPost> => {
  const { data } = await axiosInstance.post<BoardPost>(ENDPOINTS.boardPosts, { jobSlotId });
  return data;
};

export const updateBoardPost = async ({
  id,
  payload,
}: {
  id: string;
  payload: BoardPostForm;
}): Promise<BoardPost> => {
  const { data } = await axiosInstance.put<BoardPost>(`${ENDPOINTS.boardPosts}/${id}`, payload);
  return data;
};

export const publishBoardPost = async ({
  id,
  version,
}: {
  id: string;
  version: number;
}): Promise<BoardPost> => {
  const { data } = await axiosInstance.post<BoardPost>(`${ENDPOINTS.boardPosts}/${id}/publish`, { version });
  return data;
};

export const makePublicBoardPost = async ({
  id,
  version,
}: {
  id: string;
  version: number;
}): Promise<BoardPost> => {
  const { data } = await axiosInstance.post<BoardPost>(`${ENDPOINTS.boardPosts}/${id}/make-public`, {
    version,
  });
  return data;
};

export const archiveBoardPost = async ({
  id,
  version,
}: {
  id: string;
  version: number;
}): Promise<BoardPost> => {
  const { data } = await axiosInstance.post<BoardPost>(`${ENDPOINTS.boardPosts}/${id}/archive`, { version });
  return data;
};
