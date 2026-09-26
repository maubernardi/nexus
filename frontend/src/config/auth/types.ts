export type Role = 'TUTOR' | 'CALL_CENTER' | 'ADMIN';

export type AuthMode = 'mock' | 'keycloak';

export type CurrentUser = {
  id: string;
  username: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  roles: Role[];
};
