import { axiosInstance } from '@/config/api/axiosInstance';
import { ENDPOINTS } from '@/config/api/Endpoint';
import type { ReferenceItem } from '@/config/api/referenceApi';
import type { EducationLevel, Gender, LanguageLevel, LicenseType, TransportMode } from '@/lib/domainValues';

export type CandidateLanguage = { language: string; level: LanguageLevel };

export type CandidateCreate = {
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
  languages: CandidateLanguage[];
};

export type Candidate = Omit<CandidateCreate, 'residenceZoneId'> & {
  id: string;
  residenceZone: ReferenceItem;
  hasDrivingLicense: boolean;
  createdAt: string;
};

export type CandidateSummary = {
  id: string;
  firstName: string;
  lastName: string;
  birthYear: number;
  residenceZoneName: string;
};

export const createCandidate = async (payload: CandidateCreate): Promise<Candidate> => {
  const { data } = await axiosInstance.post<Candidate>(ENDPOINTS.candidates, payload);
  return data;
};

export const fetchMyCandidates = async (): Promise<CandidateSummary[]> => {
  const { data } = await axiosInstance.get<CandidateSummary[]>(ENDPOINTS.candidates);
  return data;
};
