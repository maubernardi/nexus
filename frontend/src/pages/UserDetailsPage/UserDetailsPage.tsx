import { useState } from 'react';
import { useForm, type FieldPath } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import { useNavigate, useParams } from 'react-router';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { apiError } from '@/config/api/errors';
import type { UserDetails, UserDetailsForm } from '@/config/api/userAdminApi';
import { useUpdateUserDetails, useUserDetails } from '@/config/hooks/useUsers';
import { useErrorSummaryFocus } from '@/hooks/useErrorSummaryFocus';
import { ErrorSummary, type SummaryError } from '@/molecules/ErrorSummary/ErrorSummary';
import { FormField } from '@/molecules/FormField/FormField';
import type { UsersPageState } from '@/pages/UsersPage/UsersPage';
import { BackLink } from '@/molecules/BackLink/BackLink';
import { PageHeader } from '@/molecules/PageHeader/PageHeader';

type Values = {
  firstName: string;
  lastName: string;
  username: string;
  email: string;
  phone: string;
};

const FIELDS = ['firstName', 'lastName', 'username', 'email', 'phone'] as const;
// come «Nuovo utente» (US-103) e il backend
const USERNAME_PATTERN = /^\s*[A-Za-z0-9]+([._-][A-Za-z0-9]+)*\s*$/;
const EMAIL_PATTERN = /^\s*[^@\s]+@[^@\s]+\.[^@\s]+\s*$/;
const LOCKED_ID = 'user-username-locked';

const fieldId = (field: keyof Values): string => `user-${field}`;

/**
 * Dati di un utente (US-116): l'ADMIN corregge nome, cognome, username, email e telefono, in NEXUS e in Keycloak. Lo
 * username solo finché l'utente non ha impostato la password: lo storico registra con lo username chi ha fatto cosa.
 */
export const UserDetailsPage = () => {
  const { t } = useTranslation();
  const { id = '' } = useParams();
  const details = useUserDetails(id);

  return (
    <div className="space-y-6">
      <PageHeader back={<BackLink to="/utenti">{t('users.backToList')}</BackLink>}>
        {details.data
          ? t('users.details.heading', { name: `${details.data.firstName} ${details.data.lastName}` })
          : t('pages.userDetails.title')}
      </PageHeader>
      {details.isError && !details.data ? (
        <div role="alert" className="space-y-3 rounded-md border-2 border-destructive bg-card p-4">
          <p className="font-medium text-destructive">
            {apiError(details.error)?.status === 503
              ? apiError(details.error)?.message
              : t('users.details.loadError')}
          </p>
          <Button variant="outline" onClick={() => void details.refetch()}>
            {t('common.retry')}
          </Button>
        </div>
      ) : !details.data ? (
        <p>{t('common.loading')}</p>
      ) : (
        <DetailsForm user={details.data} />
      )}
    </div>
  );
};

type DetailsFormProps = { user: UserDetails };

const DetailsForm = ({ user }: DetailsFormProps) => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const update = useUpdateUserDetails();
  const [formError, setFormError] = useState<string | null>(null);
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting, submitCount },
  } = useForm<Values>({
    defaultValues: {
      firstName: user.firstName,
      lastName: user.lastName,
      username: user.username,
      email: user.email,
      phone: user.phone ?? '',
    },
    mode: 'onSubmit',
    reValidateMode: 'onChange',
    shouldFocusError: false,
  });
  const { summaryRef, requestSummaryFocus } = useErrorSummaryFocus(submitCount, errors);

  const summary: SummaryError[] = FIELDS.flatMap((field) => {
    const message = errors[field]?.message;
    return message ? [{ fieldId: fieldId(field), message: `${t(`users.fields.${field}`)}: ${message}` }] : [];
  });
  const required = { value: true, message: t('form.errors.required') };
  const max = (n: number) => ({ value: n, message: t('form.errors.maxLength', { max: n }) });
  const notBlank = (v: string): true | string => v.trim() !== '' || t('form.errors.required');

  const onValid = async (v: Values): Promise<void> => {
    setFormError(null);
    const form: UserDetailsForm = {
      firstName: v.firstName.trim(),
      lastName: v.lastName.trim(),
      username: v.username.trim(),
      email: v.email.trim(),
      phone: v.phone.trim() || null,
    };
    try {
      const result = await update.mutateAsync({ id: user.id, form });
      const name = `${result.user.firstName} ${result.user.lastName}`;
      const state: UsersPageState =
        result.invitationSent === undefined
          ? { notice: t('users.details.saved', { name }) }
          : result.invitationSent
            ? { notice: t('users.details.savedInvitation', { name, email: result.user.email }) }
            : { notice: t('users.details.savedNoInvitation', { name }), alert: true };
      void navigate('/utenti', { state });
    } catch (error) {
      const body = apiError(error);
      const fieldErrors = (body?.fieldErrors ?? []).filter((fe) =>
        (FIELDS as readonly string[]).includes(fe.field),
      );
      if (fieldErrors.length > 0) {
        fieldErrors.forEach((fe) =>
          setError(fe.field as FieldPath<Values>, { type: 'server', message: fe.message }),
        );
      } else {
        setFormError(
          body && [400, 503].includes(body.status) && body.message ? body.message : t('users.error'),
        );
      }
      requestSummaryFocus();
    }
  };

  return (
    <>
      <p className="text-muted-foreground">
        {t('users.details.intro')}{' '}
        {t(user.usernameEditable ? 'users.details.notEntered' : 'users.details.entered')}
      </p>
      <form noValidate onSubmit={handleSubmit(onValid, requestSummaryFocus)} className="space-y-6">
        {submitCount > 0 && <ErrorSummary ref={summaryRef} errors={summary} />}
        {formError && (
          <p
            role="alert"
            className="rounded-md border-2 border-destructive bg-card p-4 font-medium text-destructive"
          >
            {formError}
          </p>
        )}
        <div className="grid max-w-3xl gap-4 md:grid-cols-2">
          <FormField
            id={fieldId('firstName')}
            label={t('users.fields.firstName')}
            required
            error={errors.firstName?.message}
          >
            {(p) => (
              <Input
                {...p}
                autoComplete="off"
                {...register('firstName', { required, maxLength: max(100), validate: notBlank })}
              />
            )}
          </FormField>
          <FormField
            id={fieldId('lastName')}
            label={t('users.fields.lastName')}
            required
            error={errors.lastName?.message}
          >
            {(p) => (
              <Input
                {...p}
                autoComplete="off"
                {...register('lastName', { required, maxLength: max(100), validate: notBlank })}
              />
            )}
          </FormField>
          <div className="space-y-1.5">
            <FormField
              id={fieldId('username')}
              label={t('users.fields.username')}
              hint={user.usernameEditable ? t('users.hints.username') : undefined}
              required={user.usernameEditable}
              error={errors.username?.message}
            >
              {(p) =>
                user.usernameEditable ? (
                  <Input
                    {...p}
                    autoComplete="off"
                    autoCapitalize="none"
                    spellCheck={false}
                    {...register('username', {
                      required,
                      minLength: { value: 3, message: t('users.errors.usernameLength') },
                      maxLength: max(50),
                      pattern: { value: USERNAME_PATTERN, message: t('users.errors.username') },
                    })}
                  />
                ) : (
                  // in sola lettura e non disabilitato: resta leggibile e raggiungibile, con il motivo collegato
                  <Input
                    {...p}
                    readOnly
                    aria-describedby={[p['aria-describedby'], LOCKED_ID].filter(Boolean).join(' ')}
                    className="bg-muted"
                    {...register('username')}
                  />
                )
              }
            </FormField>
            {!user.usernameEditable && (
              <p id={LOCKED_ID} className="text-sm text-muted-foreground">
                {t('users.details.usernameLocked')}
              </p>
            )}
          </div>
          <FormField
            id={fieldId('email')}
            label={t('users.fields.email')}
            required
            error={errors.email?.message}
          >
            {(p) => (
              <Input
                {...p}
                type="email"
                autoComplete="off"
                spellCheck={false}
                {...register('email', {
                  required,
                  maxLength: max(254),
                  pattern: { value: EMAIL_PATTERN, message: t('users.errors.email') },
                })}
              />
            )}
          </FormField>
          <FormField id={fieldId('phone')} label={t('users.fields.phone')} error={errors.phone?.message}>
            {(p) => (
              <Input
                {...p}
                type="tel"
                autoComplete="off"
                {...register('phone', {
                  maxLength: max(30),
                  pattern: { value: /^[0-9 +().\-/]*$/, message: t('users.errors.phone') },
                })}
              />
            )}
          </FormField>
        </div>
        <div className="flex flex-wrap items-center gap-4">
          <Button type="submit" size="lg" disabled={isSubmitting}>
            {isSubmitting ? t('users.details.saving') : t('users.details.save')}
          </Button>
        </div>
      </form>
    </>
  );
};
