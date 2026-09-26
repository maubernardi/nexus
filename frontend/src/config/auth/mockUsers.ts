import type { Role } from '@/config/auth/types';

export type MockUserOption = {
  username: string;
  displayName: string;
  role: Role;
};

/** Utenti fittizi allineati a backend/src/main/resources/application-security-mock.yml. */
export const MOCK_USERS: readonly MockUserOption[] = [
  { username: 'tutor1', displayName: 'Tutor Uno', role: 'TUTOR' },
  { username: 'tutor2', displayName: 'Tutor Due', role: 'TUTOR' },
  { username: 'operatore.cc', displayName: 'Operatore Call Center', role: 'CALL_CENTER' },
  { username: 'admin', displayName: 'Amministratore Nexus', role: 'ADMIN' },
];
