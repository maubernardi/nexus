import type { BoardPost } from '@/config/api/boardPostApi';

/** Annuncio della bacheca per i test: bozza riservata a GOL, mansione di Officina Rossi. */
export const boardPost = (number: number, overrides: Partial<BoardPost> = {}): BoardPost => ({
  id: `P${number}`,
  number,
  title: 'Tirocinio magazziniere',
  status: 'DRAFT',
  jobSlot: {
    id: 'S1',
    title: 'Magazziniere',
    description: null,
    companyId: 'C1',
    companyName: 'Officina Rossi',
    jobCategory: { id: 'J1', code: 'MAG', name: 'Magazziniere' },
    zone: { id: 'Z1', code: 'NORD', name: 'Firenze Nord' },
    version: 0,
  },
  project: { id: 'PR1', code: 'GOL', name: 'GOL' },
  ageMin: null,
  ageMax: null,
  weeklyHours: 20,
  durationMonths: 6,
  notes: null,
  publishedAt: null,
  madePublicAt: null,
  createdFromTicketId: null,
  createdFromTicketNumber: null,
  createdAt: '2026-10-27T08:00:00Z',
  version: 0,
  ...overrides,
});
