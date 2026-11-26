import { COMPANY_PAGE_SIZES, DEFAULT_COMPANY_PAGE_SIZE } from '@/config/api/companyApi';

/** Pagina dell'indirizzo, da 1: un valore non valido vale la prima (US-513). */
export const parsePage = (value: string | null): number => {
  const page = Number(value);
  return Number.isInteger(page) && page >= 1 ? page : 1;
};

/** Aziende per pagina dell'indirizzo: solo 10, 25, 50 o 100, altrimenti 25 (US-513). */
export const parsePageSize = (value: string | null): number => {
  const size = Number(value);
  return COMPANY_PAGE_SIZES.some((s) => s === size) ? size : DEFAULT_COMPANY_PAGE_SIZE;
};
