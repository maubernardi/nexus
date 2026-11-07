import { useEffect, useRef, useState } from 'react';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { apiError } from '@/config/api/errors';
import type { EmailStatus, PendingEmailChange } from '@/config/api/userApi';
import { useMyAccessLog } from '@/config/hooks/useAccessLog';
import { useCurrentUser } from '@/config/hooks/useCurrentUser';
import {
  useCancelEmailChange,
  useConfirmEmailChange,
  useMyEmail,
  useRequestEmailChange,
} from '@/config/hooks/useMyEmail';
import { FormField } from '@/molecules/FormField/FormField';
import { AccessLogTable } from '@/organisms/AccessLogTable/AccessLogTable';

const time = new Intl.DateTimeFormat('it-IT', {
  hour: '2-digit',
  minute: '2-digit',
  timeZone: 'Europe/Rome',
});

type Feedback = { kind: 'status' | 'alert'; text: string };

/** Messaggio del backend per i 400/503 (codice sbagliato, attesa per un nuovo codice…), altrimenti quello generico. */
const useFailureText = (): ((error: unknown) => string) => {
  const { t } = useTranslation();
  return (error) => {
    const body = apiError(error);
    return body && [400, 503].includes(body.status) && body.message ? body.message : t('profile.error');
  };
};

/**
 * Il mio profilo (US-110): dati dell'account e cambio dell'indirizzo email in due passi. Il nuovo indirizzo riceve un
 * codice di 6 cifre; solo con il codice l'indirizzo cambia (in NEXUS e nell'account di accesso). In fondo i miei
 * accessi (US-113), per accorgermi di quelli che non ho fatto io.
 */
export const ProfilePage = () => {
  const { t } = useTranslation();
  const { data: user } = useCurrentUser();
  const email = useMyEmail();
  const accessLog = useMyAccessLog();
  const [feedback, setFeedback] = useState<Feedback | null>(null);
  const feedbackRef = useRef<HTMLParagraphElement>(null);

  useEffect(() => {
    if (feedback) feedbackRef.current?.focus();
  }, [feedback]);

  const fullName = [user?.firstName, user?.lastName].filter(Boolean).join(' ') || user?.username;
  const pending = email.data?.pending;

  return (
    <div className="space-y-6">
      <PageHeading>{t('profile.heading')}</PageHeading>

      <section
        aria-labelledby="profile-account"
        className="space-y-3 rounded-xl border border-border bg-card p-4"
      >
        <h2 id="profile-account" className="text-lg font-semibold">
          {t('profile.account')}
        </h2>
        <dl className="grid gap-x-6 gap-y-2 sm:grid-cols-[max-content_1fr]">
          <dt className="font-medium">{t('profile.name')}</dt>
          <dd>{fullName}</dd>
          <dt className="font-medium">{t('profile.role')}</dt>
          <dd>{user?.roles.map((r) => t(`roles.${r}`)).join(', ')}</dd>
          <dt className="font-medium">{t('profile.email')}</dt>
          <dd className="break-all">
            {email.data?.email ?? (email.isError ? t('profile.loadError') : t('common.loading'))}
          </dd>
        </dl>
      </section>

      <section
        aria-labelledby="profile-email"
        className="space-y-4 rounded-xl border border-border bg-card p-4"
      >
        <h2 id="profile-email" className="text-lg font-semibold">
          {t('profile.changeEmail')}
        </h2>
        {feedback && (
          <p
            ref={feedbackRef}
            role={feedback.kind}
            tabIndex={-1}
            className={
              feedback.kind === 'alert'
                ? 'rounded-md border-2 border-destructive p-4 font-medium text-destructive'
                : 'rounded-md border-l-4 border-primary bg-background p-4 font-medium'
            }
          >
            {feedback.text}
          </p>
        )}
        {email.data &&
          (pending ? (
            <CodeStep key={pending.expiresAt} pending={pending} onFeedback={setFeedback} />
          ) : (
            <NewEmailStep current={email.data} onFeedback={setFeedback} />
          ))}
      </section>

      <section
        aria-labelledby="profile-access-log"
        className="space-y-4 rounded-xl border border-border bg-card p-4"
      >
        <h2 id="profile-access-log" className="text-lg font-semibold">
          {t('accessLog.mineHeading')}
        </h2>
        <p className="text-muted-foreground">{t('accessLog.mineIntro')}</p>
        <AccessLogTable query={accessLog} caption={t('accessLog.mineCaption')} />
      </section>
    </div>
  );
};

type StepProps = {
  onFeedback: (feedback: Feedback | null) => void;
};

type NewEmailValues = { newEmail: string };

const EMAIL_PATTERN = /^[^@\s]+@[^@\s]+\.[^@\s]+$/;

const NewEmailStep = ({ current, onFeedback }: StepProps & { current: EmailStatus }) => {
  const { t } = useTranslation();
  const failureText = useFailureText();
  const request = useRequestEmailChange();
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<NewEmailValues>({ defaultValues: { newEmail: '' } });

  const onSubmit = async ({ newEmail }: NewEmailValues): Promise<void> => {
    onFeedback(null);
    try {
      const status = await request.mutateAsync(newEmail.trim());
      onFeedback({
        kind: 'status',
        text: t('profile.codeSent', { email: status.pending?.newEmail ?? newEmail }),
      });
    } catch (error) {
      const fieldError = apiError(error)?.fieldErrors?.find((fe) => fe.field === 'newEmail');
      if (fieldError) {
        setError('newEmail', { type: 'server', message: fieldError.message }, { shouldFocus: true });
        return;
      }
      onFeedback({ kind: 'alert', text: failureText(error) });
    }
  };

  return (
    <form noValidate onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <p>{t('profile.changeIntro')}</p>
      <FormField
        id="profile-new-email"
        label={t('profile.newEmail')}
        required
        error={errors.newEmail?.message}
      >
        {(p) => (
          <Input
            {...p}
            type="email"
            autoComplete="email"
            spellCheck={false}
            className="max-w-md"
            {...register('newEmail', {
              required: t('form.errors.required'),
              validate: (v) => {
                const value = v.trim();
                if (!EMAIL_PATTERN.test(value)) return t('profile.invalidEmail');
                if (value.toLowerCase() === current.email.toLowerCase()) return t('profile.sameEmail');
                return true;
              },
            })}
          />
        )}
      </FormField>
      <Button type="submit" size="lg" disabled={request.isPending}>
        {t('profile.sendCode')}
      </Button>
    </form>
  );
};

type CodeValues = { code: string };

const CodeStep = ({ pending, onFeedback }: StepProps & { pending: PendingEmailChange }) => {
  const { t } = useTranslation();
  const failureText = useFailureText();
  const confirm = useConfirmEmailChange();
  const resend = useRequestEmailChange();
  const cancel = useCancelEmailChange();
  const busy = confirm.isPending || resend.isPending || cancel.isPending;
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<CodeValues>({ defaultValues: { code: '' } });

  const run = async (action: () => Promise<string>): Promise<void> => {
    onFeedback(null);
    try {
      onFeedback({ kind: 'status', text: await action() });
    } catch (error) {
      onFeedback({ kind: 'alert', text: failureText(error) });
    }
  };

  const onConfirm = async ({ code }: CodeValues): Promise<void> => {
    onFeedback(null);
    try {
      const status = await confirm.mutateAsync(code.trim());
      onFeedback({ kind: 'status', text: t('profile.changed', { email: status.email }) });
    } catch (error) {
      const fieldError = apiError(error)?.fieldErrors?.find((fe) => fe.field === 'code');
      if (fieldError) {
        setError('code', { type: 'server', message: fieldError.message }, { shouldFocus: true });
        return;
      }
      onFeedback({ kind: 'alert', text: failureText(error) });
    }
  };

  const exhausted = pending.remainingAttempts <= 0;

  return (
    <div className="space-y-4">
      <p>
        {t('profile.pending', {
          email: pending.newEmail,
          time: time.format(new Date(pending.expiresAt)),
        })}
      </p>
      {exhausted ? (
        <p className="font-medium">{t('profile.exhausted')}</p>
      ) : (
        <form noValidate onSubmit={handleSubmit(onConfirm)} className="space-y-4">
          <FormField id="profile-code" label={t('profile.code')} required error={errors.code?.message}>
            {(p) => (
              <Input
                {...p}
                inputMode="numeric"
                autoComplete="one-time-code"
                maxLength={6}
                className="max-w-40 font-mono tracking-widest"
                {...register('code', {
                  required: t('form.errors.required'),
                  pattern: { value: /^\s*\d{6}\s*$/, message: t('profile.codeFormat') },
                })}
              />
            )}
          </FormField>
          <Button type="submit" size="lg" disabled={busy}>
            {t('profile.confirm')}
          </Button>
        </form>
      )}
      <div className="flex flex-wrap gap-3">
        <Button
          type="button"
          variant="outline"
          disabled={busy}
          onClick={() =>
            void run(async () => {
              const status = await resend.mutateAsync(pending.newEmail);
              return t('profile.codeSent', { email: status.pending?.newEmail ?? pending.newEmail });
            })
          }
        >
          {t('profile.resend')}
        </Button>
        <Button
          type="button"
          variant="outline"
          disabled={busy}
          onClick={() =>
            void run(async () => {
              await cancel.mutateAsync();
              return t('profile.cancelled');
            })
          }
        >
          {t('profile.cancel')}
        </Button>
      </div>
    </div>
  );
};
