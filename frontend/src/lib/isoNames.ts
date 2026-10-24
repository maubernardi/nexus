import { COUNTRY_CODES, LANGUAGE_CODES } from '@/lib/isoCodes';

export type Option = { value: string; label: string };

const byLabel = (a: Option, b: Option): number => a.label.localeCompare(b.label, 'it');

/** Opzioni con nome in italiano (Intl.DisplayNames), le più frequenti in cima; esclude i codici senza nome. */
const buildOptions = (
  codes: readonly string[],
  type: 'region' | 'language',
  first: readonly string[],
): Option[] => {
  const names = new Intl.DisplayNames(['it'], { type, fallback: 'none' });
  const options = codes
    .map((code) => ({ value: code, label: names.of(code) ?? '' }))
    .filter((o) => o.label !== '' && o.label.toUpperCase() !== o.value.toUpperCase());
  const top = first
    .map((code) => options.find((o) => o.value === code))
    .filter((o): o is Option => o !== undefined);
  const rest = options.filter((o) => !first.includes(o.value)).sort(byLabel);
  return [...top, ...rest];
};

export const countryOptions = (): Option[] => buildOptions(COUNTRY_CODES, 'region', ['IT']);

export const languageOptions = (): Option[] =>
  buildOptions(LANGUAGE_CODES, 'language', ['it', 'en', 'fr', 'es', 'ar']);

/** Nome in italiano di un paese (ISO 3166-1) o di una lingua (ISO 639-1); il codice se sconosciuto. */
export const isoName = (code: string, type: 'region' | 'language'): string =>
  new Intl.DisplayNames(['it'], { type, fallback: 'code' }).of(code) ?? code;
