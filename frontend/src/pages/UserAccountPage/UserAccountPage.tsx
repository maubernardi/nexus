import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useNavigate, useParams } from 'react-router';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { Button } from '@/components/ui/button';
import { NativeSelect } from '@/components/ui/native-select';
import { apiError } from '@/config/api/errors';
import type { AdminUser, UserImpact } from '@/config/api/userAdminApi';
import type { Role } from '@/config/auth/types';
import { useChangeUserRole, useSetUserActive, useUser, useUserImpact } from '@/config/hooks/useUsers';
import { useFocusAfterRender } from '@/hooks/useFocusAfterRender';
import type { UsersPageState } from '@/pages/UsersPage/UsersPage';

const ROLES: readonly Role[] = ['TUTOR', 'CALL_CENTER', 'ADMIN'];
const link = 'font-medium text-primary underline underline-offset-4';
const section = 'space-y-3 rounded-xl border border-border bg-card p-4';

/**
 * Accesso e ruolo di un utente (US-105): l'ADMIN lo disattiva o lo riattiva e gli cambia il ruolo, in NEXUS e in
 * Keycloak. Un Tutor con segnalazioni aperte va prima liberato spostandole (US-111); l'ADMIN non tocca se stesso.
 */
export const UserAccountPage = () => {
  const { t } = useTranslation();
  const { id = '' } = useParams();
  const user = useUser(id);
  const impact = useUserImpact(id);
  const failed = (user.isError && !user.data) || (impact.isError && !impact.data);

  return (
    <div className="space-y-6">
      <PageHeading>
        {user.data
          ? t('users.account.heading', { name: `${user.data.firstName} ${user.data.lastName}` })
          : t('pages.userAccount.title')}
      </PageHeading>
      {failed ? (
        <div role="alert" className="space-y-3 rounded-md border-2 border-destructive bg-card p-4">
          <p className="font-medium text-destructive">{t('users.account.loadError')}</p>
          <Button
            variant="outline"
            onClick={() => {
              void user.refetch();
              void impact.refetch();
            }}
          >
            {t('common.retry')}
          </Button>
        </div>
      ) : !user.data || !impact.data ? (
        <p>{t('common.loading')}</p>
      ) : impact.data.self ? (
        <p>{t('users.account.self')}</p>
      ) : (
        <AccountForms user={user.data} impact={impact.data} />
      )}
      <Link to="/utenti" className={link}>
        {t('users.backToList')}
      </Link>
    </div>
  );
};

type AccountFormsProps = { user: AdminUser; impact: UserImpact };

const AccountForms = ({ user, impact }: AccountFormsProps) => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const setActive = useSetUserActive();
  const changeRole = useChangeUserRole();
  const [role, setRole] = useState<Role>(user.role);
  const [error, setError] = useState<string | null>(null);
  const { ref: errorRef, requestFocus: focusError } = useFocusAfterRender<HTMLParagraphElement>();
  const name = `${user.firstName} ${user.lastName}`;
  const busy = setActive.isPending || changeRole.isPending;
  // un Tutor con segnalazioni aperte non si disattiva né cambia ruolo (decisione del PO, 06/11)
  const blocked = impact.openTickets > 0;

  const done = (notice: string): void => {
    const state: UsersPageState = { notice };
    void navigate('/utenti', { state });
  };

  const fail = (e: unknown): void => {
    const body = apiError(e);
    setError(body && [400, 503].includes(body.status) && body.message ? body.message : t('users.error'));
    focusError();
  };

  const onToggle = async (): Promise<void> => {
    setError(null);
    try {
      await setActive.mutateAsync({ id: user.id, active: !user.active });
      done(t(user.active ? 'users.account.deactivated' : 'users.account.reactivated', { name }));
    } catch (e) {
      fail(e);
    }
  };

  const onRole = async (): Promise<void> => {
    setError(null);
    try {
      await changeRole.mutateAsync({ id: user.id, role });
      done(t('users.account.roleChanged', { name, role: t(`roles.${role}`) }));
    } catch (e) {
      fail(e);
    }
  };

  return (
    <>
      {error && (
        <p
          ref={errorRef}
          role="alert"
          tabIndex={-1}
          className="rounded-md border-2 border-destructive bg-card p-4 font-medium text-destructive"
        >
          {error}
        </p>
      )}
      {blocked && (
        <div className="space-y-2 rounded-md border-l-4 border-destructive bg-card p-4">
          <p className="font-medium">{t('users.account.blocked', { count: impact.openTickets })}</p>
          <Link to={`/utenti/${user.id}/segnalazioni`} className={link}>
            {t('users.account.toTickets')}
          </Link>
        </div>
      )}

      <section aria-labelledby="account-status" className={section}>
        <h2 id="account-status" className="text-lg font-semibold text-primary">
          {t('users.account.statusHeading')}
        </h2>
        <p>
          {t(user.active ? 'users.account.isActive' : 'users.account.isInactive')}{' '}
          {t(user.active ? 'users.account.deactivateHint' : 'users.account.reactivateHint')}
        </p>
        {user.active && impact.ticketsInCharge > 0 && (
          <p className="font-medium">{t('users.account.inCharge', { count: impact.ticketsInCharge })}</p>
        )}
        {!(user.active && blocked) && (
          <Button
            type="button"
            variant={user.active ? 'destructive' : 'default'}
            disabled={busy}
            onClick={() => void onToggle()}
          >
            {t(user.active ? 'users.account.deactivate' : 'users.account.reactivate')}
          </Button>
        )}
      </section>

      <section aria-labelledby="account-role" className={section}>
        <h2 id="account-role" className="text-lg font-semibold text-primary">
          {t('users.account.roleHeading')}
        </h2>
        <p>{t('users.account.roleHint')}</p>
        {!blocked && (
          <div className="flex flex-wrap items-end gap-4">
            <div className="space-y-1">
              <label htmlFor="account-role-select" className="block text-sm font-medium">
                {t('users.fields.role')}
              </label>
              <NativeSelect
                id="account-role-select"
                value={role}
                onChange={(e) => setRole(e.target.value as Role)}
              >
                {ROLES.map((r) => (
                  <option key={r} value={r}>
                    {t(`roles.${r}`)}
                  </option>
                ))}
              </NativeSelect>
            </div>
            <Button type="button" disabled={busy || role === user.role} onClick={() => void onRole()}>
              {t('users.account.changeRole')}
            </Button>
          </div>
        )}
        {!blocked && role === 'TUTOR' && user.role !== 'TUTOR' && (
          <p className="font-medium">
            {impact.ticketsInCharge > 0 &&
              `${t('users.account.inCharge', { count: impact.ticketsInCharge })} `}
            {t('users.account.becomesTutor')}
          </p>
        )}
      </section>
    </>
  );
};
