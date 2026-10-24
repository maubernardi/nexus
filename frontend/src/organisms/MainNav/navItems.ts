import { Building2, FilePlus2, ListOrdered, Users, type LucideIcon } from 'lucide-react';

import type { Role } from '@/config/auth/types';

export type NavItem = {
  to: string;
  labelKey: 'nav.home' | 'nav.beneficiaries' | 'nav.newTicket' | 'nav.companies';
  icon: LucideIcon;
  /** Ruoli abilitati; vuoto = tutti. */
  roles: readonly Role[];
};

export const NAV_ITEMS: readonly NavItem[] = [
  { to: '/', labelKey: 'nav.home', icon: ListOrdered, roles: [] },
  { to: '/beneficiari', labelKey: 'nav.beneficiaries', icon: Users, roles: ['TUTOR'] },
  { to: '/segnalazioni/nuova', labelKey: 'nav.newTicket', icon: FilePlus2, roles: ['TUTOR'] },
  { to: '/aziende', labelKey: 'nav.companies', icon: Building2, roles: ['CALL_CENTER', 'ADMIN'] },
];
