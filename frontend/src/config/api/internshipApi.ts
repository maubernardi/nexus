import { axiosInstance } from '@/config/api/axiosInstance';
import type {
  AtecoCode,
  LegalRepresentative,
  LegalRepresentativeForm,
} from '@/config/api/companyLegalDataApi';
import { ENDPOINTS } from '@/config/api/Endpoint';
import type { ReferenceItem } from '@/config/api/referenceApi';

export type CompanyTutor = { fullName: string; contact?: string | null };

/** Periodo di sospensione, estremi compresi (aaaa-mm-gg). */
export type Suspension = { from: string; to: string };

/**
 * Scheda del tirocinio (US-1001) con i dati dell'azienda per i documenti. L'API omette i campi vuoti; `version`
 * assente = scheda mai salvata.
 */
export type Internship = {
  version?: number | null;
  companyName?: string | null;
  legalRepresentative?: LegalRepresentative | null;
  atecoCodes: AtecoCode[];
  internshipType?: ReferenceItem | null;
  startDate?: string | null;
  expectedEndDate?: string | null;
  weeklyHours?: number | null;
  site?: string | null;
  atecoCodeId?: string | null;
  companyTutors: CompanyTutor[];
  fiscalCode?: string | null;
  birthDate?: string | null;
  birthPlace?: string | null;
  residence?: string | null;
  suspensions: Suspension[];
  notes?: string | null;
};

export type InternshipForm = {
  internshipTypeId: string | null;
  startDate: string | null;
  expectedEndDate: string | null;
  weeklyHours: number | null;
  site: string | null;
  atecoCodeId: string | null;
  companyTutors: { fullName: string; contact: string | null }[];
  fiscalCode: string | null;
  birthDate: string | null;
  birthPlace: string | null;
  residence: string | null;
  suspensions: Suspension[];
  notes: string | null;
  /** null al primo salvataggio. */
  version: number | null;
};

const internshipUrl = (ticketId: string): string =>
  `${ENDPOINTS.tickets}/${encodeURIComponent(ticketId)}/internship`;

export const fetchInternship = async (ticketId: string): Promise<Internship> => {
  const { data } = await axiosInstance.get<Internship>(internshipUrl(ticketId));
  return data;
};

export const saveInternship = async ({
  ticketId,
  payload,
}: {
  ticketId: string;
  payload: InternshipForm;
}): Promise<Internship> => {
  const { data } = await axiosInstance.put<Internship>(internshipUrl(ticketId), payload);
  return data;
};

/** Legale rappresentante dell'azienda inserito dal Tutor: solo se manca (correggerlo spetta al Call Center). */
export const addInternshipLegalRepresentative = async ({
  ticketId,
  payload,
}: {
  ticketId: string;
  payload: LegalRepresentativeForm;
}): Promise<LegalRepresentative> => {
  const { data } = await axiosInstance.put<LegalRepresentative>(
    `${internshipUrl(ticketId)}/legal-representative`,
    payload,
  );
  return data;
};

/** Codice ATECO aggiunto all'azienda della segnalazione, da scegliere nella scheda. */
export const addInternshipAtecoCode = async ({
  ticketId,
  code,
  description,
}: {
  ticketId: string;
  code: string;
  description: string | null;
}): Promise<AtecoCode> => {
  const { data } = await axiosInstance.post<AtecoCode>(`${internshipUrl(ticketId)}/ateco-codes`, {
    code,
    description,
  });
  return data;
};
