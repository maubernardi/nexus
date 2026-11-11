import {
  Building2,
  CalendarDays,
  FileText,
  ArrowLeftRight,
  ListOrdered,
  ListTree,
  Megaphone,
  UserCog,
  Users,
  type LucideIcon,
} from 'lucide-react';

import type { Role } from '@/config/auth/types';

export type NavItem = {
  to: string;
  labelKey:
    | 'nav.home'
    | 'nav.beneficiaries'
    | 'nav.companies'
    | 'nav.board'
    | 'nav.users'
    | 'nav.catalogs'
    | 'nav.documentTemplates'
    | 'nav.calendar'
    | 'nav.coordination';
  icon: LucideIcon;
  /** Ruoli abilitati; vuoto = tutti. */
  roles: readonly Role[];
  /** Solo per chi coordina almeno un progetto (US-112). */
  coordinatorOnly?: boolean;
  /** Voce del sottomenu «Amministrazione» (decisione del PO, 08/11: il menu dell'ADMIN non ci stava). */
  admin?: boolean;
};

export const NAV_ITEMS: readonly NavItem[] = [
  { to: '/', labelKey: 'nav.home', icon: ListOrdered, roles: [] },
  { to: '/beneficiari', labelKey: 'nav.beneficiaries', icon: Users, roles: ['TUTOR'] },
  // US-1303: l'ADMIN vede i beneficiari di tutti, con la conservazione dei dati
  { to: '/beneficiari/tutti', labelKey: 'nav.beneficiaries', icon: Users, roles: ['ADMIN'] },
  { to: '/aziende', labelKey: 'nav.companies', icon: Building2, roles: ['CALL_CENTER', 'ADMIN'] },
  {
    to: '/coordinamento',
    labelKey: 'nav.coordination',
    icon: ArrowLeftRight,
    roles: ['TUTOR'],
    coordinatorOnly: true,
  },
  { to: '/bacheca', labelKey: 'nav.board', icon: Megaphone, roles: [] },
  { to: '/utenti', labelKey: 'nav.users', icon: UserCog, roles: ['ADMIN'], admin: true },
  { to: '/cataloghi', labelKey: 'nav.catalogs', icon: ListTree, roles: ['ADMIN'], admin: true },
  { to: '/modelli', labelKey: 'nav.documentTemplates', icon: FileText, roles: ['ADMIN'], admin: true },
  { to: '/calendario', labelKey: 'nav.calendar', icon: CalendarDays, roles: ['ADMIN'], admin: true },
];
