import type { ContactRequest } from '@/config/api/contactRequestApi';

/** Richiesta di contatto per i test (US-507): aperta, per un'azienda nuova, di Tina Tutor. */
export const contactRequest = (number: number, overrides: Partial<ContactRequest> = {}): ContactRequest => ({
  id: `R${number}`,
  number,
  createdAt: '2026-11-27T09:00:00Z',
  requestedByName: 'Tina Tutor',
  companyName: 'Officina del Ponte',
  zone: { id: 'Z1', code: 'RIFREDI', name: 'Rifredi' },
  status: 'APERTA',
  version: 0,
  ...overrides,
});
