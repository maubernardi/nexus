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
