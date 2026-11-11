import type { Municipality } from '@/config/api/municipalityApi';

/** Il valore nel modulo: il testo della casella e il comune scelto, assente finché non lo si sceglie dall'elenco. */
export type MunicipalityValue = { text: string; municipality?: Municipality };

/** «Sesto Fiorentino (FI)». */
export const municipalityLabel = (m: Pick<Municipality, 'name' | 'provinceAbbreviation'>): string =>
  `${m.name} (${m.provinceAbbreviation})`;

export const toMunicipalityValue = (m?: Municipality): MunicipalityValue => ({
  text: m ? municipalityLabel(m) : '',
  municipality: m,
});

/** Indirizzo su una riga, come nei documenti: «Via Roma 1, Firenze (FI)»; assente senza nessuna parte. */
export const addressLine = (address?: string | null, m?: Municipality | null): string | undefined =>
  [address?.trim(), m ? municipalityLabel(m) : undefined].filter(Boolean).join(', ') || undefined;

/** Testo scritto senza scegliere un comune dall'elenco: il modulo lo segnala come errore. */
export const isUnchosen = (v: MunicipalityValue): boolean => v.text.trim() !== '' && !v.municipality;
