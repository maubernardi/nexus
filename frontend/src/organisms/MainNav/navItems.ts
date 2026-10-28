import {
  Building2,
  CalendarDays,
  ListOrdered,
  ListTree,
  Megaphone,
  Users,
  type LucideIcon,
} from 'lucide-react';

import type { Role } from '@/config/auth/types';

export type NavItem = {
  to: string;
  labelKey:
    'nav.home' | 'nav.beneficiaries' | 'nav.companies' | 'nav.board' | 'nav.catalogs' | 'nav.calendar';
  icon: LucideIcon;
  /** Ruoli abilitati; vuoto = tutti. */
  roles: readonly Role[];
};

export const NAV_ITEMS: readonly NavItem[] = [
  { to: '/', labelKey: 'nav.home', icon: ListOrdered, roles: [] },
  { to: '/beneficiari', labelKey: 'nav.beneficiaries', icon: Users, roles: ['TUTOR'] },
  { to: '/aziende', labelKey: 'nav.companies', icon: Building2, roles: ['CALL_CENTER', 'ADMIN'] },
  { to: '/bacheca', labelKey: 'nav.board', icon: Megaphone, roles: [] },
  { to: '/cataloghi', labelKey: 'nav.catalogs', icon: ListTree, roles: ['ADMIN'] },
  { to: '/calendario', labelKey: 'nav.calendar', icon: CalendarDays, roles: ['ADMIN'] },
];
