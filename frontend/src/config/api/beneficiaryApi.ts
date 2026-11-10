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
  /** Contatti e presa in carico (US-206), tutti facoltativi. */
  email: string | null;
  phone: string | null;
  careAgencyId: string | null;
  caseManagerName: string | null;
  caseManagerOffice: string | null;
  caseManagerPhone: string | null;
  caseManagerEmail: string | null;
};

export type Beneficiary = Omit<
  BeneficiaryCreate,
  | 'residenceZoneId'
  | 'careAgencyId'
  | 'email'
  | 'phone'
  | 'caseManagerName'
  | 'caseManagerOffice'
  | 'caseManagerPhone'
  | 'caseManagerEmail'
> & {
  id: string;
  residenceZone: ReferenceItem;
  /** L'API omette i campi vuoti. */
  email?: string;
  phone?: string;
  careAgency?: ReferenceItem;
  caseManagerName?: string;
  caseManagerOffice?: string;
  caseManagerPhone?: string;
  caseManagerEmail?: string;
  /** Ha un tirocinio aperto: l'ente non si cambia (US-206). */
  careAgencyLocked: boolean;
  /** Ha un tirocinio di inclusione aperto: nome, mail e telefono del case manager obbligatori. */
  caseManagerRequired: boolean;
  hasDrivingLicense: boolean;
  createdAt: string;
  ownerTutorName: string;
  /** L'utente corrente può modificarlo: solo il Tutor proprietario (US-203). */
  editable: boolean;
  /** CV caricato (US-205); assenti se non c'è. */
  cvFileName?: string;
  cvSizeBytes?: number;
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

/** CV del beneficiario (US-205): solo PDF fino a 5 MB, lo carica il Tutor proprietario. */
export const CV_MAX_BYTES = 5 * 1024 * 1024;

export const uploadBeneficiaryCv = async ({ id, file }: { id: string; file: File }): Promise<Beneficiary> => {
  const body = new FormData();
  body.append('file', file);
  const { data } = await axiosInstance.put<Beneficiary>(
    `${ENDPOINTS.beneficiaries}/${encodeURIComponent(id)}/cv`,
    body,
  );
  return data;
};

export const deleteBeneficiaryCv = async (id: string): Promise<Beneficiary> => {
  const { data } = await axiosInstance.delete<Beneficiary>(
    `${ENDPOINTS.beneficiaries}/${encodeURIComponent(id)}/cv`,
  );
  return data;
};

export const downloadBeneficiaryCv = async (id: string): Promise<Blob> => {
  const { data } = await axiosInstance.get<Blob>(`${ENDPOINTS.beneficiaries}/${encodeURIComponent(id)}/cv`, {
    responseType: 'blob',
  });
  return data;
};
