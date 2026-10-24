import { axiosInstance } from '@/config/api/axiosInstance';
import { ENDPOINTS } from '@/config/api/Endpoint';

export type CompanyNote = {
  id: string;
  text: string;
  createdAt: string;
  authorName: string;
};

const notesUrl = (companyId: string): string =>
  `${ENDPOINTS.companies}/${encodeURIComponent(companyId)}/notes`;

export const fetchCompanyNotes = async (companyId: string): Promise<CompanyNote[]> => {
  const { data } = await axiosInstance.get<CompanyNote[]>(notesUrl(companyId));
  return data;
};

export const addCompanyNote = async ({
  companyId,
  text,
}: {
  companyId: string;
  text: string;
}): Promise<CompanyNote> => {
  const { data } = await axiosInstance.post<CompanyNote>(notesUrl(companyId), { text });
  return data;
};
