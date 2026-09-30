// Valori codificati del dominio: devono restare allineati agli enum del backend (it.nexus.domain.enumeration).
export const GENDERS = ['M', 'F', 'ALTRO', 'NON_DICHIARATO'] as const;
export type Gender = (typeof GENDERS)[number];

export const EDUCATION_LEVELS = [
  'NESSUN_TITOLO',
  'LICENZA_ELEMENTARE',
  'LICENZA_MEDIA',
  'QUALIFICA_PROFESSIONALE',
  'DIPLOMA',
  'ITS',
  'LAUREA_TRIENNALE',
  'LAUREA_MAGISTRALE',
  'DOTTORATO',
] as const;
export type EducationLevel = (typeof EDUCATION_LEVELS)[number];

export const TRANSPORT_MODES = ['AUTO_PROPRIA', 'MEZZI_PUBBLICI', 'BICICLETTA', 'A_PIEDI', 'ALTRO'] as const;
export type TransportMode = (typeof TRANSPORT_MODES)[number];

export const LICENSE_TYPES = [
  'AM',
  'A1',
  'A2',
  'A',
  'B',
  'BE',
  'C1',
  'C1E',
  'C',
  'CE',
  'D1',
  'D1E',
  'D',
  'DE',
  'CQC',
  'KB',
] as const;
export type LicenseType = (typeof LICENSE_TYPES)[number];

export const LANGUAGE_LEVELS = ['A1', 'A2', 'B1', 'B2', 'C1', 'C2', 'MADRELINGUA'] as const;
export type LanguageLevel = (typeof LANGUAGE_LEVELS)[number];
