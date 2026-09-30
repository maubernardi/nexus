import { House, UserPlus, type LucideIcon } from 'lucide-react';

import type { Role } from '@/config/auth/types';

export type NavItem = {
  to: string;
  labelKey: 'nav.home' | 'nav.newCandidate';
  icon: LucideIcon;
  /** Ruoli abilitati; vuoto = tutti. */
  roles: readonly Role[];
};

export const NAV_ITEMS: readonly NavItem[] = [
  { to: '/', labelKey: 'nav.home', icon: House, roles: [] },
  { to: '/candidati/nuovo', labelKey: 'nav.newCandidate', icon: UserPlus, roles: ['TUTOR'] },
];
