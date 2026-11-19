import { axiosInstance } from '@/config/api/axiosInstance';
import { ENDPOINTS } from '@/config/api/Endpoint';
import type { Municipality } from '@/config/api/municipalityApi';
import type { ReferenceItem } from '@/config/api/referenceApi';

/** Dato arrivato vuoto dal database attuale, con un segnaposto ancora da sostituire (US-508). */
export type CompanyMissingData = 'NAME' | 'VAT_CODE';

/** Referente dell'azienda (US-510); i campi vuoti sono assenti. */
export type CompanyContact = {
  name?: string | null;
  /** Ruolo dal catalogo, anche se nel frattempo disattivato. */
  role?: ReferenceItem | null;
  phone?: string | null;
  mobile?: string | null;
  email?: string | null;
};

export type CompanyContactForm = {
  name: string | null;
  roleId: string | null;
  phone: string | null;
  mobile: string | null;
  email: string | null;
};

export type Company = {
  id: string;
  name: string;
  vatCode: string;
  legalAddress: string | null;
  /** Comune della sede legale (US-1007), con la provincia della versione scelta. */
  legalMunicipality?: Municipality | null;
  /** Categoria merceologica (US-509); assente se non indicata. */
  businessCategory?: ReferenceItem | null;
  /** Descrizione dell'attività (US-509). */
  activity?: string | null;
  /** Sede operativa (US-510). */
  operatingAddress?: string | null;
  operatingMunicipality?: Municipality | null;
  phone: string | null;
  /** Cellulare, fax e seconda email dell'azienda (US-510). */
  mobile?: string | null;
  fax?: string | null;
  email: string | null;
  secondaryEmail?: string | null;
  /** Referenti in ordine (US-510). */
  contacts?: CompanyContact[];
  active: boolean;
  version: number;
  updatedAt: string;
  /** Dati da completare (US-508); vuoto se l'azienda è completa. */
  missingData?: CompanyMissingData[];
};

export type CompanyForm = {
  name: string;
  vatCode: string;
  legalAddress: string | null;
  legalMunicipalityId: string | null;
  businessCategoryId: string | null;
  activity: string | null;
  operatingAddress: string | null;
  operatingMunicipalityId: string | null;
  phone: string | null;
  mobile: string | null;
  fax: string | null;
  email: string | null;
  secondaryEmail: string | null;
  contacts: CompanyContactForm[];
  /** Solo in modifica: versione vista (blocco ottimistico). */
  version?: number;
};

export type CompanySearch = {
  search?: string;
  includeInactive?: boolean;
  /** Solo le aziende con un dato da completare (US-508). */
  toComplete?: boolean;
  /** Solo le aziende della categoria merceologica (US-509). */
  businessCategoryId?: string;
};

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
