import { FilePlus2, House, ListOrdered, UserPlus, type LucideIcon } from 'lucide-react';

import type { Role } from '@/config/auth/types';

export type NavItem = {
  to: string;
  labelKey: 'nav.home' | 'nav.newCandidate' | 'nav.newTicket' | 'nav.queue';
  icon: LucideIcon;
  /** Ruoli abilitati; vuoto = tutti. */
  roles: readonly Role[];
};

export const NAV_ITEMS: readonly NavItem[] = [
  { to: '/', labelKey: 'nav.home', icon: House, roles: [] },
  { to: '/candidati/nuovo', labelKey: 'nav.newCandidate', icon: UserPlus, roles: ['TUTOR'] },
  { to: '/segnalazioni/nuova', labelKey: 'nav.newTicket', icon: FilePlus2, roles: ['TUTOR'] },
  { to: '/coda', labelKey: 'nav.queue', icon: ListOrdered, roles: ['CALL_CENTER', 'ADMIN'] },
];
