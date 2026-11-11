import { useState } from 'react';
import { Controller, useForm, type FieldPath } from 'react-hook-form';
import { useTranslation } from 'react-i18next';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import type { Company, CompanyForm as CompanyPayload } from '@/config/api/companyApi';
import { apiError } from '@/config/api/errors';
import { useErrorSummaryFocus } from '@/hooks/useErrorSummaryFocus';
import { ErrorSummary, type SummaryError } from '@/molecules/ErrorSummary/ErrorSummary';
import { FormField } from '@/molecules/FormField/FormField';
import { MunicipalityField } from '@/molecules/MunicipalityField/MunicipalityField';
import {
  isUnchosen,
  toMunicipalityValue,
  type MunicipalityValue,
} from '@/molecules/MunicipalityField/municipalityValue';

type CompanyFormValues = {
  name: string;
  vatCode: string;
  legalAddress: string;
  legalMunicipality: MunicipalityValue;
  contactPerson: string;
  phone: string;
  email: string;
};

const FIELDS = [
  'name',
  'vatCode',
  'contactPerson',
  'legalAddress',
  'legalMunicipality',
  'phone',
  'email',
] as const;

const fieldId = (field: keyof CompanyFormValues): string => `company-${field}`;

const toValues = (company?: Company): CompanyFormValues => ({
  name: company?.name ?? '',
  vatCode: company?.vatCode ?? '',
  legalAddress: company?.legalAddress ?? '',
  legalMunicipality: toMunicipalityValue(company?.legalMunicipality ?? undefined),
  contactPerson: company?.contactPerson ?? '',
  phone: company?.phone ?? '',
  email: company?.email ?? '',
});

const toPayload = (v: CompanyFormValues): CompanyPayload => ({
  name: v.name.trim(),
  vatCode: v.vatCode.trim(),
  legalAddress: v.legalAddress.trim() || null,
  legalMunicipalityId: v.legalMunicipality.municipality?.id ?? null,
  contactPerson: v.contactPerson.trim() || null,
  phone: v.phone.trim() || null,
  email: v.email.trim() || null,
});

type CompanyFormProps = {
  /** Azienda da modificare; assente = nuova azienda. */
  company?: Company;
  submitLabel: string;
  onSubmit: (payload: CompanyPayload) => Promise<Company>;
  onSuccess: (company: Company) => void;
};

/** Dati anagrafici dell'azienda ospitante (US-501), con errori sui campi e riepilogo focalizzato. */
export const CompanyForm = ({ company, submitLabel, onSubmit, onSuccess }: CompanyFormProps) => {
  const { t } = useTranslation();
  const [formError, setFormError] = useState<'generic' | 'conflict' | null>(null);
  const {
    register,
    control,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting, submitCount },
  } = useForm<CompanyFormValues>({
    defaultValues: toValues(company),
    mode: 'onSubmit',
    reValidateMode: 'onChange',
    // il focus va al riepilogo degli errori (non al primo campo): lo gestisce useErrorSummaryFocus
    shouldFocusError: false,
  });
  const { summaryRef, requestSummaryFocus } = useErrorSummaryFocus(submitCount, errors);

  const labels: Record<keyof CompanyFormValues, string> = {
    name: t('company.fields.name'),
    vatCode: t('company.fields.vatCode'),
    legalAddress: t('company.fields.legalAddress'),
    legalMunicipality: t('company.fields.legalMunicipality'),
    contactPerson: t('company.fields.contactPerson'),
    phone: t('company.fields.phone'),
    email: t('company.fields.email'),
  };
  const summary: SummaryError[] = FIELDS.flatMap((field) => {
    const message = errors[field]?.message;
    return message ? [{ fieldId: fieldId(field), message: `${labels[field]}: ${message}` }] : [];
  });
  const required = { value: true, message: t('form.errors.required') };
  const max = (n: number) => ({ value: n, message: t('form.errors.maxLength', { max: n }) });

  const onValid = async (values: CompanyFormValues): Promise<void> => {
    setFormError(null);
    try {
      onSuccess(await onSubmit({ ...toPayload(values), version: company?.version }));
    } catch (error) {
      const body = apiError(error);
      // il server parla dell'id del comune, il modulo del campo del comune (US-1007)
      const fieldErrors = (body?.fieldErrors ?? [])
        .map((fe) => (fe.field === 'legalMunicipalityId' ? { ...fe, field: 'legalMunicipality' } : fe))
        .filter((fe) => (FIELDS as readonly string[]).includes(fe.field));
      if (fieldErrors.length > 0) {
        fieldErrors.forEach((fe) =>
          setError(fe.field as FieldPath<CompanyFormValues>, { type: 'server', message: fe.message }),
        );
      } else {
        setFormError(body?.status === 409 ? 'conflict' : 'generic');
      }
      requestSummaryFocus();
    }
  };

  return (
    <form noValidate onSubmit={handleSubmit(onValid, requestSummaryFocus)} className="space-y-6">
      {submitCount > 0 && <ErrorSummary ref={summaryRef} errors={summary} />}
      {formError && (
        <p
          role="alert"
          className="rounded-md border-2 border-destructive bg-card p-4 font-medium text-destructive"
        >
          {t(formError === 'conflict' ? 'form.errors.conflict' : 'form.errors.generic')}
        </p>
      )}

      <div className="grid max-w-3xl gap-4 md:grid-cols-2">
        <FormField
          id={fieldId('name')}
          label={labels.name}
          required
          error={errors.name?.message}
          className="md:col-span-2"
        >
          {(p) => (
            <Input
              {...p}
              autoComplete="organization"
              {...register('name', {
                required,
                maxLength: max(200),
                validate: (v) => v.trim() !== '' || t('form.errors.required'),
              })}
            />
          )}
        </FormField>
        <FormField
          id={fieldId('vatCode')}
          label={labels.vatCode}
          hint={t('company.hints.vatCode')}
          required
          error={errors.vatCode?.message}
        >
          {(p) => (
            <Input
              {...p}
              inputMode="numeric"
              autoComplete="off"
              {...register('vatCode', {
                required,
                pattern: { value: /^\s*\d{11}\s*$/, message: t('company.errors.vatCode') },
              })}
            />
          )}
        </FormField>
        <FormField
          id={fieldId('contactPerson')}
          label={labels.contactPerson}
          error={errors.contactPerson?.message}
        >
          {(p) => <Input {...p} autoComplete="off" {...register('contactPerson', { maxLength: max(200) })} />}
        </FormField>
        <FormField
          id={fieldId('legalAddress')}
          label={labels.legalAddress}
          hint={t('company.hints.legalAddress')}
          error={errors.legalAddress?.message}
        >
          {(p) => <Input {...p} autoComplete="off" {...register('legalAddress', { maxLength: max(300) })} />}
        </FormField>
        <FormField
          id={fieldId('legalMunicipality')}
          label={labels.legalMunicipality}
          hint={t('municipalityField.hint')}
          error={errors.legalMunicipality?.message}
        >
          {(p) => (
            <Controller
              control={control}
              name="legalMunicipality"
              rules={{ validate: (v) => !isUnchosen(v) || t('municipalityField.choose') }}
              render={({ field }) => (
                <MunicipalityField
                  {...p}
                  ref={field.ref}
                  value={field.value}
                  onChange={field.onChange}
                  onBlur={field.onBlur}
                />
              )}
            />
          )}
        </FormField>
        <FormField id={fieldId('phone')} label={labels.phone} error={errors.phone?.message}>
          {(p) => (
            <Input
              {...p}
              type="tel"
              autoComplete="off"
              {...register('phone', {
                maxLength: max(30),
                pattern: { value: /^[0-9 +().\-/]*$/, message: t('company.errors.phone') },
              })}
            />
          )}
        </FormField>
        <FormField id={fieldId('email')} label={labels.email} error={errors.email?.message}>
          {(p) => (
            <Input
              {...p}
              type="email"
              autoComplete="off"
              {...register('email', {
                maxLength: max(254),
                pattern: { value: /^$|^[^\s@]+@[^\s@]+\.[^\s@]+$/, message: t('company.errors.email') },
              })}
            />
          )}
        </FormField>
      </div>

      <Button type="submit" size="lg" className="w-full sm:w-auto" disabled={isSubmitting}>
        {isSubmitting ? t('company.saving') : submitLabel}
      </Button>
    </form>
  );
};
