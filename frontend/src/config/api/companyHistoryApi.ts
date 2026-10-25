import { axiosInstance } from '@/config/api/axiosInstance';
import { ENDPOINTS } from '@/config/api/Endpoint';

export type CompanyHistorySummary = {
  proposals: number;
  accepted: number;
  failedAppointments: number;
  internships: number;
  hirings: number;
  negativeOutcomes: number;
};

export type CompanyHistoryEvent = {
  at: string;
  type: string;
  ticketId: string | null;
  ticketNumber: number | null;
  detail: string | null;
  authorName: string;
};

export type CompanyHistory = { summary: CompanyHistorySummary; events: CompanyHistoryEvent[] };

export const fetchCompanyHistory = async (companyId: string): Promise<CompanyHistory> => {
  const { data } = await axiosInstance.get<CompanyHistory>(
    `${ENDPOINTS.companies}/${encodeURIComponent(companyId)}/history`,
  );
  return data;
};
