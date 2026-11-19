import type { CompanyContact } from '@/config/api/companyApi';

/** Chi è il referente (US-510): il nome con il ruolo tra parentesi, oppure il solo nome o il solo ruolo. */
export const contactWho = (c: CompanyContact): string => {
  const role = c.role?.name;
  if (c.name && role) return `${c.name} (${role})`;
  return c.name ?? role ?? '';
};

/** Nome del referente nell'elenco delle aziende: il nome, oppure il ruolo se il nome manca. */
export const contactName = (c: CompanyContact): string => c.name ?? c.role?.name ?? '';
