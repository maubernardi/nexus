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
