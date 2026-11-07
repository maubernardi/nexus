import { axiosInstance } from '@/config/api/axiosInstance';
import { ENDPOINTS } from '@/config/api/Endpoint';
import type { ReferenceItem } from '@/config/api/referenceApi';
import type { EducationLevel, Gender, LanguageLevel, LicenseType, TransportMode } from '@/lib/domainValues';

export type BeneficiaryLanguage = { language: string; level: LanguageLevel };

export type BeneficiaryCreate = {
  firstName: string;
  lastName: string;
  birthYear: number;
  gender: Gender;
  nationality: string | null;
  citizenship: string | null;
  residenceZoneId: string;
  licenseTypes: LicenseType[];
  hasVehicle: boolean;
  transportMode: TransportMode | null;
  hasLaw68: boolean;
  educationLevel: EducationLevel | null;
  constraints: string | null;
  languages: BeneficiaryLanguage[];
};

export type Beneficiary = Omit<BeneficiaryCreate, 'residenceZoneId'> & {
  id: string;
  residenceZone: ReferenceItem;
  hasDrivingLicense: boolean;
  createdAt: string;
  ownerTutorName: string;
  /** L'utente corrente può modificarlo: solo il Tutor proprietario (US-203). */
  editable: boolean;
};

export type BeneficiarySummary = {
  id: string;
  firstName: string;
  lastName: string;
  birthYear: number;
  residenceZoneName: string;
  /** Solo nell'elenco del Tutor: numero della segnalazione aperta (una sola per beneficiario). */
  openTicketNumber?: number | null;
  /** Conservazione dei dati (US-1303); assente se normale. */
  retention?: RetentionStatus;
};

/** Conservazione dei dati di un beneficiario (US-1303). */
export type RetentionStatus = 'TO_ANONYMIZE' | 'KEPT' | 'ANONYMIZED';

/** Beneficiario nell'elenco dell'ADMIN, con il suo Tutor e la conservazione dei dati (US-1303). */
export type BeneficiaryRetentionItem = {
  id: string;
  firstName: string;
  lastName: string;
  birthYear: number;
  residenceZoneName: string;
  ownerTutorName: string;
  retention?: RetentionStatus;
  anonymizationDueSince?: string;
  keptReason?: string;
};

export const fetchAllBeneficiaries = async (): Promise<BeneficiaryRetentionItem[]> => {
  const { data } = await axiosInstance.get<BeneficiaryRetentionItem[]>(`${ENDPOINTS.beneficiaries}/all`);
  return data;
};

/** Anonimizza un beneficiario da anonimizzare: restano le iniziali e i dati statistici (US-1303). */
export const anonymizeBeneficiary = async (id: string): Promise<void> => {
  await axiosInstance.post(`${ENDPOINTS.beneficiaries}/${encodeURIComponent(id)}/anonymize`);
};

/** Tiene per sempre i dati di un beneficiario da anonimizzare, con il motivo (US-1303). */
export const keepBeneficiary = async ({ id, reason }: { id: string; reason: string }): Promise<void> => {
  await axiosInstance.post(`${ENDPOINTS.beneficiaries}/${encodeURIComponent(id)}/keep`, { reason });
};

export const createBeneficiary = async (payload: BeneficiaryCreate): Promise<Beneficiary> => {
  const { data } = await axiosInstance.post<Beneficiary>(ENDPOINTS.beneficiaries, payload);
  return data;
};

export const fetchMyBeneficiaries = async (): Promise<BeneficiarySummary[]> => {
  const { data } = await axiosInstance.get<BeneficiarySummary[]>(ENDPOINTS.beneficiaries);
  return data;
};

export const fetchBeneficiary = async (id: string): Promise<Beneficiary> => {
  const { data } = await axiosInstance.get<Beneficiary>(
    `${ENDPOINTS.beneficiaries}/${encodeURIComponent(id)}`,
  );
  return data;
};

export const updateBeneficiary = async ({
  id,
  payload,
}: {
  id: string;
  payload: BeneficiaryCreate;
}): Promise<Beneficiary> => {
  const { data } = await axiosInstance.put<Beneficiary>(
    `${ENDPOINTS.beneficiaries}/${encodeURIComponent(id)}`,
    payload,
  );
  return data;
};
