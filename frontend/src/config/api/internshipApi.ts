import { axiosInstance } from '@/config/api/axiosInstance';
import type {
  AtecoCode,
  LegalRepresentative,
  LegalRepresentativeForm,
} from '@/config/api/companyLegalDataApi';
import { ENDPOINTS } from '@/config/api/Endpoint';
import type { Municipality } from '@/config/api/municipalityApi';
import type { ReferenceItem } from '@/config/api/referenceApi';

/** Tutor aziendale: funzione o ruolo in azienda (US-1009) e recapito facoltativi. */
export type CompanyTutor = { fullName: string; role?: string | null; contact?: string | null };

/**
 * Progetto formativo salvato (US-1008): il numero è la posizione nell'ordine delle date; mesi arrotondati e monte ore
 * (minuti, assente senza griglia) sono calcolati sulle sue date.
 */
export type TrainingProject = {
  number: number;
  from: string;
  to: string;
  jobTitle: string;
  jobDetails?: string | null;
  months: number;
  totalMinutes?: number | null;
};

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
  /** Tipi che si possono scegliere: attivi e ammessi dal progetto della segnalazione, più quello scelto (US-115). */
  internshipTypes?: ReferenceItem[];
  startDate?: string | null;
  expectedEndDate?: string | null;
  /** Indirizzo della sede e versione del suo comune (US-1007). */
  site?: string | null;
  siteMunicipality?: Municipality | null;
  atecoCodeId?: string | null;
  companyTutors: CompanyTutor[];
  fiscalCode?: string | null;
  birthDate?: string | null;
  birthPlace?: string | null;
  /** Residenza del beneficiario (US-1007): versione del comune, via e numero civico. */
  residenceMunicipality?: Municipality | null;
  residenceStreet?: string | null;
  residenceStreetNumber?: string | null;
  suspensions: Suspension[];
  notes?: string | null;
  /** Mansione della segnalazione, proposta per il primo progetto formativo (US-1008). */
  jobSlotTitle?: string | null;
  /** Convenzione (US-1008): la durata è quella del tirocinio. */
  agreementNumber?: string | null;
  agreementDate?: string | null;
  agreementSignedDate?: string | null;
  trainingProjects: TrainingProject[];
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
  /**
   * Dati del beneficiario che mancano per il tipo «Di inclusione» (US-206): careAgency, caseManagerName,
   * caseManagerEmail, caseManagerPhone; vuoto o assente = si può scegliere.
   */
  inclusionMissing?: string[];
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
  | 'agreement'
  | 'trainingProjects'
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
  siteMunicipalityId: string | null;
  atecoCodeId: string | null;
  companyTutors: { fullName: string; role: string | null; contact: string | null }[];
  fiscalCode: string | null;
  birthDate: string | null;
  birthPlace: string | null;
  residenceMunicipalityId: string | null;
  residenceStreet: string | null;
  residenceStreetNumber: string | null;
  suspensions: Suspension[];
  notes: string | null;
  agreementNumber: string | null;
  agreementDate: string | null;
  agreementSignedDate: string | null;
  /** Nell'ordine delle date: la posizione è il numero del progetto (US-1008). */
  trainingProjects: { from: string; to: string; jobTitle: string; jobDetails: string | null }[];
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

/** Documenti del tirocinio generati dai modelli Word (US-1004, US-1005). */
export type InternshipDocumentKind = 'convenzione' | 'progetto-formativo';

/** Modello caricato dall'ADMIN che il Tutor può scegliere per un documento (US-1010). */
export type DocumentTemplateChoice = { id: string; name: string };

/** Modelli caricati validi per il documento: vuoto = modello generico, uno = automatico, più = si sceglie. */
export const fetchDocumentTemplateChoices = async (
  ticketId: string,
  kind: InternshipDocumentKind,
): Promise<DocumentTemplateChoice[]> => {
  const { data } = await axiosInstance.get<DocumentTemplateChoice[]>(
    `${internshipUrl(ticketId)}/documents/${kind}/templates`,
  );
  return data;
};

/** Documento compilato dalla scheda salvata, come file .docx; con più modelli, quello scelto. */
export const downloadInternshipDocument = async ({
  ticketId,
  kind,
  template,
}: {
  ticketId: string;
  kind: InternshipDocumentKind;
  template?: string;
}): Promise<Blob> => {
  const { data } = await axiosInstance.get<Blob>(`${internshipUrl(ticketId)}/documents/${kind}`, {
    params: template ? { template } : undefined,
    responseType: 'blob',
  });
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
