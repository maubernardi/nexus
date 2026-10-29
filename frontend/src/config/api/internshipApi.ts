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

/** Giorno della griglia oraria (US-1002): 1 = lunedì … 7 = domenica; minuti assenti = fasce intere. */
export type ScheduleDay = {
  dayOfWeek: number;
  firstSlot: boolean;
  secondSlot: boolean;
  minutes?: number | null;
};

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
  site?: string | null;
  atecoCodeId?: string | null;
  companyTutors: CompanyTutor[];
  fiscalCode?: string | null;
  birthDate?: string | null;
  birthPlace?: string | null;
  residence?: string | null;
  suspensions: Suspension[];
  notes?: string | null;
  /** Fasce orarie, "hh:mm:ss" (US-1002). */
  firstSlotStart: string;
  firstSlotEnd: string;
  secondSlotStart: string;
  secondSlotEnd: string;
  excludeHolidays: boolean;
  schedule: ScheduleDay[];
  /** Totale settimanale e monte ore in minuti, calcolati sui dati salvati; assenti se non calcolabili. */
  weeklyMinutes?: number | null;
  totalMinutes?: number | null;
  /** Dati che mancano per avviare il tirocinio (US-1003), in base alla scheda salvata; vuoto = completa. */
  missingForStart: StartRequirement[];
};

/** Codici dei dati richiesti per l'avvio (review dello Sprint 7, §8.2). */
export type StartRequirement =
  | 'internshipType'
  | 'startDate'
  | 'expectedEndDate'
  | 'site'
  | 'atecoCode'
  | 'companyTutors'
  | 'schedule'
  | 'fiscalCode'
  | 'birthDate'
  | 'birthPlace'
  | 'residence'
  | 'legalRepresentative'
  | 'legalRepresentativeData';

export type InternshipForm = {
  internshipTypeId: string | null;
  startDate: string | null;
  expectedEndDate: string | null;
  site: string | null;
  atecoCodeId: string | null;
  companyTutors: { fullName: string; contact: string | null }[];
  fiscalCode: string | null;
  birthDate: string | null;
  birthPlace: string | null;
  residence: string | null;
  suspensions: Suspension[];
  notes: string | null;
  /** "hh:mm" */
  firstSlotStart: string;
  firstSlotEnd: string;
  secondSlotStart: string;
  secondSlotEnd: string;
  excludeHolidays: boolean;
  /** Solo i giorni con almeno una fascia. */
  schedule: { dayOfWeek: number; firstSlot: boolean; secondSlot: boolean; minutes: number | null }[];
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
