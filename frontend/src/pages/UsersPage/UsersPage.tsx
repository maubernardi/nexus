import { useEffect, useRef, useState } from 'react';
import { Plus } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Link, useLocation } from 'react-router';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { Button } from '@/components/ui/button';
import { apiError } from '@/config/api/errors';
import type { AdminUser } from '@/config/api/userAdminApi';
import { useResendInvitation, useUsers } from '@/config/hooks/useUsers';

/** Esito della creazione, passato da «Nuovo utente» con la navigazione. */
export type UserCreatedState = { created?: { name: string; email: string; invitationSent: boolean } };

type Feedback = { kind: 'status' | 'alert'; text: string };

/**
 * Utenti di NEXUS per l'ADMIN (US-103): elenco, nuovo utente e nuovo invito per impostare la password, senza la
 * console di Keycloak.
 */
export const UsersPage = () => {
  const { t } = useTranslation();
  const users = useUsers();
  const resend = useResendInvitation();
  const location = useLocation();
  const created = (location.state as UserCreatedState | null)?.created;
  const [feedback, setFeedback] = useState<Feedback | null>(() =>
    created
      ? {
          kind: created.invitationSent ? 'status' : 'alert',
          text: t(created.invitationSent ? 'users.created' : 'users.createdNoInvitation', {
            name: created.name,
            email: created.email,
          }),
        }
      : null,
  );
  const feedbackRef = useRef<HTMLParagraphElement>(null);

  useEffect(() => {
    if (feedback) feedbackRef.current?.focus();
  }, [feedback]);

  const onResend = async (user: AdminUser): Promise<void> => {
    setFeedback(null);
    try {
      await resend.mutateAsync(user.id);
      setFeedback({ kind: 'status', text: t('users.invitationSent', { email: user.email }) });
    } catch (error) {
      const body = apiError(error);
      setFeedback({
        kind: 'alert',
        text: body && [400, 503].includes(body.status) && body.message ? body.message : t('users.error'),
      });
    }
  };

  const th = 'px-3 py-2 text-left text-sm font-semibold whitespace-nowrap';
  const td = 'px-3 py-2 align-top';

  return (
    <div className="space-y-6">
      <PageHeading>{t('users.heading')}</PageHeading>
      <div className="flex flex-wrap items-center justify-between gap-3">
        <p className="text-muted-foreground">{t('users.intro')}</p>
        <Button asChild size="lg">
          <Link to="/utenti/nuovo">
            <Plus aria-hidden="true" />
            {t('users.new')}
          </Link>
        </Button>
      </div>

      {feedback && (
        <p
          ref={feedbackRef}
          role={feedback.kind}
          tabIndex={-1}
          className={
            feedback.kind === 'alert'
              ? 'rounded-md border-2 border-destructive bg-card p-4 font-medium text-destructive'
              : 'rounded-md border-l-4 border-primary bg-card p-4 font-medium'
          }
        >
          {feedback.text}
        </p>
      )}

      {users.isError && !users.data ? (
        <div role="alert" className="space-y-3 rounded-md border-2 border-destructive bg-card p-4">
          <p className="font-medium text-destructive">{t('users.loadError')}</p>
          <Button variant="outline" onClick={() => void users.refetch()}>
            {t('common.retry')}
          </Button>
        </div>
      ) : !users.data ? (
        <p>{t('common.loading')}</p>
      ) : (
        <div
          role="region"
          aria-labelledby="users-caption"
          // eslint-disable-next-line jsx-a11y/no-noninteractive-tabindex -- regione scorrevole: deve essere raggiungibile da tastiera
          tabIndex={0}
          className="relative overflow-x-auto rounded-xl border border-border bg-card focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
        >
          <table className="w-full border-collapse text-sm max-lg:w-max max-lg:whitespace-nowrap">
            <caption id="users-caption" className="sr-only">
              {t('users.caption', { count: users.data.length })}
            </caption>
            <thead className="border-b border-border bg-muted">
              <tr>
                <th scope="col" className={th}>
                  {t('users.fields.name')}
                </th>
                <th scope="col" className={th}>
                  {t('users.fields.username')}
                </th>
                <th scope="col" className={th}>
                  {t('users.fields.email')}
                </th>
                <th scope="col" className={th}>
                  {t('users.fields.role')}
                </th>
                <th scope="col" className={th}>
                  {t('users.fields.status')}
                </th>
                <th scope="col" className={th}>
                  {t('users.fields.actions')}
                </th>
              </tr>
            </thead>
            <tbody>
              {users.data.map((u) => {
                const name = `${u.lastName} ${u.firstName}`;
                return (
                  <tr key={u.id} className="border-b border-border last:border-b-0">
                    <th scope="row" className={`${td} text-left font-semibold`}>
                      {name}
                    </th>
                    <td className={td}>{u.username}</td>
                    <td className={`${td} break-all`}>{u.email}</td>
                    <td className={td}>{t(`roles.${u.role}`)}</td>
                    <td className={td}>{t(u.active ? 'users.active' : 'users.inactive')}</td>
                    <td className={td}>
                      {u.active && (
                        <Button
                          type="button"
                          variant="outline"
                          size="sm"
                          disabled={resend.isPending}
                          aria-label={t('users.resendFor', { name })}
                          onClick={() => void onResend(u)}
                        >
                          {t('users.resend')}
                        </Button>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};
