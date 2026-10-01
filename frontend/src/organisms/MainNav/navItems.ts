import {
  Building2,
  ClipboardList,
  FilePlus2,
  House,
  ListOrdered,
  UserPlus,
  type LucideIcon,
} from 'lucide-react';

import type { Role } from '@/config/auth/types';

export type NavItem = {
  to: string;
  labelKey:
    'nav.home' | 'nav.newBeneficiary' | 'nav.newTicket' | 'nav.queue' | 'nav.myWork' | 'nav.companies';
  icon: LucideIcon;
  /** Ruoli abilitati; vuoto = tutti. */
  roles: readonly Role[];
};

export const NAV_ITEMS: readonly NavItem[] = [
  { to: '/', labelKey: 'nav.home', icon: House, roles: [] },
  { to: '/beneficiari/nuovo', labelKey: 'nav.newBeneficiary', icon: UserPlus, roles: ['TUTOR'] },
  { to: '/segnalazioni/nuova', labelKey: 'nav.newTicket', icon: FilePlus2, roles: ['TUTOR'] },
  { to: '/coda', labelKey: 'nav.queue', icon: ListOrdered, roles: ['CALL_CENTER', 'ADMIN'] },
  { to: '/lavorazioni', labelKey: 'nav.myWork', icon: ClipboardList, roles: ['CALL_CENTER', 'ADMIN'] },
  { to: '/aziende', labelKey: 'nav.companies', icon: Building2, roles: ['CALL_CENTER', 'ADMIN'] },
];
