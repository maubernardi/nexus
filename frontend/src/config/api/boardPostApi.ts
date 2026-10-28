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
