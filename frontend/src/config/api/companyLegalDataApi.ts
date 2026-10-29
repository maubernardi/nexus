import { axiosInstance } from '@/config/api/axiosInstance';
import { ENDPOINTS } from '@/config/api/Endpoint';

/** Legale rappresentante dell'azienda (US-506); l'API omette i campi vuoti. */
export type LegalRepresentative = {
  firstName: string;
  lastName: string;
  fiscalCode?: string | null;
  /** aaaa-mm-gg */
  birthDate?: string | null;
  birthPlace?: string | null;
  residence?: string | null;
  version: number;
};

export type LegalRepresentativeForm = {
  firstName: string;
  lastName: string;
  fiscalCode: string | null;
  birthDate: string | null;
  birthPlace: string | null;
  residence: string | null;
  /** null alla prima registrazione. */
  version: number | null;
};

export type AtecoCode = { id: string; code: string; description?: string | null };

const companyUrl = (companyId: string): string => `${ENDPOINTS.companies}/${encodeURIComponent(companyId)}`;

/** null se il legale rappresentante non è ancora indicato (204). */
export const fetchLegalRepresentative = async (companyId: string): Promise<LegalRepresentative | null> => {
  const { data } = await axiosInstance.get<LegalRepresentative | ''>(
    `${companyUrl(companyId)}/legal-representative`,
  );
  return data || null;
};

export const saveLegalRepresentative = async ({
  companyId,
  payload,
}: {
  companyId: string;
  payload: LegalRepresentativeForm;
}): Promise<LegalRepresentative> => {
  const { data } = await axiosInstance.put<LegalRepresentative>(
    `${companyUrl(companyId)}/legal-representative`,
    payload,
  );
  return data;
};

export const fetchAtecoCodes = async (companyId: string): Promise<AtecoCode[]> => {
  const { data } = await axiosInstance.get<AtecoCode[]>(`${companyUrl(companyId)}/ateco-codes`);
  return data;
};

export const addAtecoCode = async ({
  companyId,
  code,
  description,
}: {
  companyId: string;
  code: string;
  description: string | null;
}): Promise<AtecoCode> => {
  const { data } = await axiosInstance.post<AtecoCode>(`${companyUrl(companyId)}/ateco-codes`, {
    code,
    description,
  });
  return data;
};

export const removeAtecoCode = async ({
  companyId,
  id,
}: {
  companyId: string;
  id: string;
}): Promise<void> => {
  await axiosInstance.delete(`${companyUrl(companyId)}/ateco-codes/${id}`);
};
