import { axiosInstance } from '@/config/api/axiosInstance';
import { ENDPOINTS } from '@/config/api/Endpoint';
import type { Municipality } from '@/config/api/municipalityApi';

export type Company = {
  id: string;
  name: string;
  vatCode: string;
  legalAddress: string | null;
  /** Comune della sede legale (US-1007), con la provincia della versione scelta. */
  legalMunicipality?: Municipality | null;
  contactPerson: string | null;
  phone: string | null;
  email: string | null;
  active: boolean;
  version: number;
  updatedAt: string;
};

export type CompanyForm = {
  name: string;
  vatCode: string;
  legalAddress: string | null;
  legalMunicipalityId: string | null;
  contactPerson: string | null;
  phone: string | null;
  email: string | null;
  /** Solo in modifica: versione vista (blocco ottimistico). */
  version?: number;
};

export type CompanySearch = { search?: string; includeInactive?: boolean };

export const searchCompanies = async (params: CompanySearch): Promise<Company[]> => {
  const { data } = await axiosInstance.get<Company[]>(ENDPOINTS.companies, { params });
  return data;
};

export const fetchCompany = async (id: string): Promise<Company> => {
  const { data } = await axiosInstance.get<Company>(`${ENDPOINTS.companies}/${id}`);
  return data;
};

export const createCompany = async (payload: CompanyForm): Promise<Company> => {
  const { data } = await axiosInstance.post<Company>(ENDPOINTS.companies, payload);
  return data;
};

export const updateCompany = async ({
  id,
  payload,
}: {
  id: string;
  payload: CompanyForm;
}): Promise<Company> => {
  const { data } = await axiosInstance.put<Company>(`${ENDPOINTS.companies}/${id}`, payload);
  return data;
};

export const setCompanyActive = async ({
  id,
  version,
  active,
}: {
  id: string;
  version: number;
  active: boolean;
}): Promise<Company> => {
  const { data } = await axiosInstance.post<Company>(
    `${ENDPOINTS.companies}/${id}/${active ? 'activate' : 'deactivate'}`,
    { version },
  );
  return data;
};
