import { useState } from 'react';
import { useForm, type FieldPath } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import { Link, useNavigate } from 'react-router';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { NativeSelect } from '@/components/ui/native-select';
import { apiError } from '@/config/api/errors';
import type { NewUserForm } from '@/config/api/userAdminApi';
import type { Role } from '@/config/auth/types';
import { useCreateUser } from '@/config/hooks/useUsers';
import { useErrorSummaryFocus } from '@/hooks/useErrorSummaryFocus';
import { suggestUsername } from '@/lib/username';
import { ErrorSummary, type SummaryError } from '@/molecules/ErrorSummary/ErrorSummary';
import { FormField } from '@/molecules/FormField/FormField';
import type { UserCreatedState } from '@/pages/UsersPage/UsersPage';

type Values = {
  firstName: string;
  lastName: string;
  username: string;
  email: string;
  phone: string;
  role: '' | Role;
};

const FIELDS = ['firstName', 'lastName', 'username', 'email', 'phone', 'role'] as const;
const ROLES: readonly Role[] = ['TUTOR', 'CALL_CENTER', 'ADMIN'];
const USERNAME_PATTERN = /^\s*[A-Za-z0-9]+([._-][A-Za-z0-9]+)*\s*$/;
const EMAIL_PATTERN = /^\s*[^@\s]+@[^@\s]+\.[^@\s]+\s*$/;

const fieldId = (field: keyof Values): string => `user-${field}`;

/**
 * Nuovo utente (US-103): l'ADMIN lo crea in NEXUS e in Keycloak in un solo passo; l'utente riceve l'email per impostare
 * la password, con un collegamento valido 3 giorni.
 */
export const UserNewPage = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const create = useCreateUser();
  const [formError, setFormError] = useState(false);
  const {
    register,
    handleSubmit,
    setError,
    getValues,
    setValue,
    formState: { errors, isSubmitting, submitCount },
  } = useForm<Values>({
    defaultValues: { firstName: '', lastName: '', username: '', email: '', phone: '', role: '' },
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

  /** Se lo username è ancora vuoto, propone nome.cognome. */
  const proposeUsername = (): void => {
    const { firstName, lastName, username } = getValues();
    if (username.trim() === '' && firstName.trim() !== '' && lastName.trim() !== '') {
      setValue('username', suggestUsername(firstName, lastName));
    }
  };

  const onValid = async (v: Values): Promise<void> => {
    setFormError(false);
    const payload: NewUserForm = {
      firstName: v.firstName.trim(),
      lastName: v.lastName.trim(),
      username: v.username.trim(),
      email: v.email.trim(),
      phone: v.phone.trim() || null,
      role: v.role as Role,
    };
    try {
      const result = await create.mutateAsync(payload);
      const state: UserCreatedState = {
        created: {
          name: `${result.user.firstName} ${result.user.lastName}`,
          email: result.user.email,
          invitationSent: result.invitationSent,
        },
      };
      void navigate('/utenti', { state });
    } catch (error) {
      const fieldErrors = (apiError(error)?.fieldErrors ?? []).filter((fe) =>
        (FIELDS as readonly string[]).includes(fe.field),
      );
      if (fieldErrors.length > 0) {
        fieldErrors.forEach((fe) =>
          setError(fe.field as FieldPath<Values>, { type: 'server', message: fe.message }),
        );
      } else {
        setFormError(true);
      }
      requestSummaryFocus();
    }
  };

  return (
    <div className="space-y-6">
      <PageHeading>{t('users.newHeading')}</PageHeading>
      <p className="text-muted-foreground">{t('users.newIntro')}</p>
      <form noValidate onSubmit={handleSubmit(onValid, requestSummaryFocus)} className="space-y-6">
        {submitCount > 0 && <ErrorSummary ref={summaryRef} errors={summary} />}
        {formError && (
          <p
            role="alert"
            className="rounded-md border-2 border-destructive bg-card p-4 font-medium text-destructive"
          >
            {apiError(create.error)?.status === 503 ? apiError(create.error)?.message : t('users.error')}
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
                {...register('firstName', {
                  required,
                  maxLength: max(100),
                  validate: notBlank,
                  onBlur: proposeUsername,
                })}
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
                {...register('lastName', {
                  required,
                  maxLength: max(100),
                  validate: notBlank,
                  onBlur: proposeUsername,
                })}
              />
            )}
          </FormField>
          <FormField
            id={fieldId('username')}
            label={t('users.fields.username')}
            hint={t('users.hints.username')}
            required
            error={errors.username?.message}
          >
            {(p) => (
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
            )}
          </FormField>
          <FormField
            id={fieldId('email')}
            label={t('users.fields.email')}
            hint={t('users.hints.email')}
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
          <FormField
            id={fieldId('role')}
            label={t('users.fields.role')}
            required
            error={errors.role?.message}
          >
            {(p) => (
              <NativeSelect {...p} {...register('role', { required })}>
                <option value="">{t('form.select')}</option>
                {ROLES.map((r) => (
                  <option key={r} value={r}>
                    {t(`roles.${r}`)}
                  </option>
                ))}
              </NativeSelect>
            )}
          </FormField>
        </div>
        <div className="flex flex-wrap items-center gap-4">
          <Button type="submit" size="lg" disabled={isSubmitting}>
            {isSubmitting ? t('users.creating') : t('users.create')}
          </Button>
          <Link to="/utenti" className="font-medium text-primary underline underline-offset-4">
            {t('users.backToList')}
          </Link>
        </div>
      </form>
    </div>
  );
};
