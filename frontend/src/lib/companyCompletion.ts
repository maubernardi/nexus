import type { Company, CompanyMissingData } from '@/config/api/companyApi';

// Segnaposto dei dati che il database attuale non ha (US-508): li scrive solo l'importazione. Come nel server
// (CompanyPlaceholders), il modulo li rifiuta anche con spazi e maiuscole diverse.
const VAT_CODE_PLACEHOLDER = '00000000000';
const NAME_PLACEHOLDER = /^\s*azienda senza nome n\./i;

/** Il dato ha ancora il segnaposto: lo dice il server, così la regola resta una sola. */
export const isMissing = (company: Pick<Company, 'missingData'>, data: CompanyMissingData): boolean =>
  company.missingData?.includes(data) ?? false;

export const isVatCodePlaceholder = (value: string): boolean => value.trim() === VAT_CODE_PLACEHOLDER;

export const isNamePlaceholder = (value: string): boolean => NAME_PLACEHOLDER.test(value);
