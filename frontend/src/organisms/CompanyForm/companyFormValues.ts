import type { MunicipalityValue } from '@/molecules/MunicipalityField/municipalityValue';

/** Referente nel modulo (US-510): testi, con il ruolo come id ('' = nessuno). */
export type ContactValues = { name: string; roleId: string; phone: string; mobile: string; email: string };

export type CompanyFormValues = {
  name: string;
  vatCode: string;
  businessCategoryId: string;
  activity: string;
  legalAddress: string;
  legalMunicipality: MunicipalityValue;
  operatingAddress: string;
  operatingMunicipality: MunicipalityValue;
  phone: string;
  mobile: string;
  fax: string;
  email: string;
  secondaryEmail: string;
  contacts: ContactValues[];
};

/** Massimo dei referenti, come nel server (US-510). */
export const MAX_CONTACTS = 10;

/** Campi di un referente, nell'ordine del modulo: l'ordine del riepilogo degli errori. */
export const CONTACT_PARTS = [
  'name',
  'roleId',
  'phone',
  'mobile',
  'email',
] as const satisfies readonly (keyof ContactValues)[];
export type ContactPart = (typeof CONTACT_PARTS)[number];

/** Telefono, cellulare e fax: come nel server. */
export const PHONE_PATTERN = /^[0-9 +().\-/]*$/;
export const EMAIL_PATTERN = /^$|^[^\s@]+@[^\s@]+\.[^\s@]+$/;
