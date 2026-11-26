import { useState, type ReactNode } from 'react';
import { Controller, useForm, type Control, type FieldPath } from 'react-hook-form';
import { useTranslation } from 'react-i18next';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { NativeSelect } from '@/components/ui/native-select';
import type { Company, CompanyForm as CompanyPayload } from '@/config/api/companyApi';
import { apiError, toFormPath } from '@/config/api/errors';
import type { ReferenceItem } from '@/config/api/referenceApi';
import { useBusinessCategories, useContactRoles } from '@/config/hooks/useReferenceData';
import { isMissing, isNamePlaceholder, isVatCodePlaceholder } from '@/lib/companyCompletion';
import { useErrorSummaryFocus } from '@/hooks/useErrorSummaryFocus';
import { ErrorSummary, type SummaryError } from '@/molecules/ErrorSummary/ErrorSummary';
import { FormField } from '@/molecules/FormField/FormField';
import { MunicipalityField } from '@/molecules/MunicipalityField/MunicipalityField';
import {
  isUnchosen,
  toMunicipalityValue,
  type MunicipalityValue,
} from '@/molecules/MunicipalityField/municipalityValue';
import { RatingInput } from '@/molecules/RatingInput/RatingInput';
import {
  CONTACT_PARTS,
  EMAIL_PATTERN,
  LEGACY_MARKS_PATTERN,
  PHONE_PATTERN,
  type CompanyFormValues,
  type ContactValues,
} from '@/organisms/CompanyForm/companyFormValues';
import { ContactsFields } from '@/organisms/CompanyForm/ContactsFields';

type Field = Exclude<keyof CompanyFormValues, 'contacts'>;

/** Campi dell'azienda nell'ordine del modulo: l'ordine del riepilogo degli errori. */
const FIELDS = [
  'name',
  'vatCode',
  'businessCategoryId',
  'activity',
  'legalAddress',
  'legalMunicipality',
  'operatingAddress',
  'operatingMunicipality',
  'phone',
  'mobile',
  'fax',
  'email',
  'secondaryEmail',
  'reliability',
  'projectInterest',
] as const satisfies readonly Field[];

/** Il server parla degli id dei comuni, il modulo dei campi dei comuni (US-1007). */
const SERVER_FIELDS: Record<string, string> = {
  legalMunicipalityId: 'legalMunicipality',
  operatingMunicipalityId: 'operatingMunicipality',
};

const fieldId = (path: string): string => `company-${path.replaceAll('.', '-')}`;

/**
 * Voci attive più quelle che l'azienda ha già, anche se nel frattempo disattivate (US-509, US-510). Le voci dell'azienda
 * ci sono anche mentre il catalogo si carica: il menu parte già con il valore salvato.
 */
const withCurrent = (
  items: ReferenceItem[] | undefined,
  current: (ReferenceItem | null | undefined)[],
): ReferenceItem[] => {
  const list = items ?? [];
  const missing = current.filter(
    (c, i, all): c is ReferenceItem =>
      !!c && !list.some((item) => item.id === c.id) && all.findIndex((o) => o?.id === c.id) === i,
  );
  return [...missing, ...list];
};

// i segnaposto del database attuale non si mostrano: il campo vuoto chiede il dato vero (US-508)
const toValues = (company?: Company): CompanyFormValues => ({
  name: company && !isMissing(company, 'NAME') ? company.name : '',
  vatCode: company && !isMissing(company, 'VAT_CODE') ? company.vatCode : '',
  businessCategoryId: company?.businessCategory?.id ?? '',
  activity: company?.activity ?? '',
  legalAddress: company?.legalAddress ?? '',
  legalMunicipality: toMunicipalityValue(company?.legalMunicipality ?? undefined),
  operatingAddress: company?.operatingAddress ?? '',
  operatingMunicipality: toMunicipalityValue(company?.operatingMunicipality ?? undefined),
  phone: company?.phone ?? '',
  mobile: company?.mobile ?? '',
  fax: company?.fax ?? '',
  email: company?.email ?? '',
  secondaryEmail: company?.secondaryEmail ?? '',
  contacts: (company?.contacts ?? []).map((c): ContactValues => ({
    name: c.name ?? '',
    roleId: c.role?.id ?? '',
    phone: c.phone ?? '',
    mobile: c.mobile ?? '',
    email: c.email ?? '',
  })),
  reliability: company?.reliability ?? null,
  projectInterest: company?.projectInterest ?? null,
});

const text = (v: string): string | null => v.trim() || null;

const toPayload = (v: CompanyFormValues): CompanyPayload => ({
  name: v.name.trim(),
  vatCode: v.vatCode.trim(),
  legalAddress: text(v.legalAddress),
  legalMunicipalityId: v.legalMunicipality.municipality?.id ?? null,
  businessCategoryId: v.businessCategoryId || null,
  activity: text(v.activity),
  operatingAddress: text(v.operatingAddress),
  operatingMunicipalityId: v.operatingMunicipality.municipality?.id ?? null,
  phone: text(v.phone),
  mobile: text(v.mobile),
  fax: text(v.fax),
  email: text(v.email),
  secondaryEmail: text(v.secondaryEmail),
  contacts: v.contacts.map((c) => ({
    name: text(c.name),
    roleId: c.roleId || null,
    phone: text(c.phone),
    mobile: text(c.mobile),
    email: text(c.email),
  })),
  reliability: v.reliability,
  projectInterest: v.projectInterest,
});

type CompanyFormProps = {
  /** Azienda da modificare; assente = nuova azienda. */
  company?: Company;
  submitLabel: string;
  onSubmit: (payload: CompanyPayload) => Promise<Company>;
  onSuccess: (company: Company) => void;
};

/**
 * Dati anagrafici dell'azienda ospitante (US-501), con errori sui campi e riepilogo focalizzato. I dati da completare
 * (US-508) partono vuoti e non accettano i segnaposto. Sedi, recapiti e referenti sono in gruppi (US-510).
 */
export const CompanyForm = ({ company, submitLabel, onSubmit, onSuccess }: CompanyFormProps) => {
  const { t } = useTranslation();
  const [formError, setFormError] = useState<'generic' | 'conflict' | null>(null);
  const categories = useBusinessCategories();
  const roles = useContactRoles();
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

  const labels: Record<Field, string> = {
    name: t('company.fields.name'),
    vatCode: t('company.fields.vatCode'),
    businessCategoryId: t('company.fields.businessCategory'),
    activity: t('company.fields.activity'),
    legalAddress: t('company.fields.legalAddress'),
    legalMunicipality: t('company.fields.legalMunicipality'),
    operatingAddress: t('company.fields.operatingAddress'),
    operatingMunicipality: t('company.fields.operatingMunicipality'),
    phone: t('company.fields.phone'),
    mobile: t('company.fields.mobile'),
    fax: t('company.fields.fax'),
    email: t('company.fields.email'),
    secondaryEmail: t('company.fields.secondaryEmail'),
    reliability: t('company.fields.reliability'),
    projectInterest: t('company.fields.projectInterest'),
  };
  const contactErrors = errors.contacts;
  const summary: SummaryError[] = [
    ...FIELDS.flatMap((field) => {
      const message = errors[field]?.message;
      return message ? [{ fieldId: fieldId(field), message: `${labels[field]}: ${message}` }] : [];
    }),
    ...(contactErrors?.message
      ? [
          {
            fieldId: fieldId('contacts'),
            message: `${t('company.contacts.heading')}: ${contactErrors.message}`,
          },
        ]
      : []),
    ...Array.from({ length: Array.isArray(contactErrors) ? contactErrors.length : 0 }, (_, k) =>
      CONTACT_PARTS.flatMap((part) => {
        const message = contactErrors?.[k]?.[part]?.message;
        const label = t(`company.contacts.${part}`, { n: k + 1 });
        return message
          ? [{ fieldId: fieldId(`contacts.${k}.${part}`), message: `${label}: ${message}` }]
          : [];
      }),
    ).flat(),
  ];
  const required = { value: true, message: t('form.errors.required') };
  const missingName = company !== undefined && isMissing(company, 'NAME');
  const missingVatCode = company !== undefined && isMissing(company, 'VAT_CODE');
  const max = (n: number) => ({ value: n, message: t('form.errors.maxLength', { max: n }) });
  const phoneRules = (n: number) => ({
    maxLength: max(n),
    pattern: { value: PHONE_PATTERN, message: t('company.errors.phone') },
  });
  const emailRules = {
    maxLength: max(254),
    pattern: { value: EMAIL_PATTERN, message: t('company.errors.email') },
  };

  const onValid = async (values: CompanyFormValues): Promise<void> => {
    setFormError(null);
    try {
      onSuccess(await onSubmit({ ...toPayload(values), version: company?.version }));
    } catch (error) {
      const body = apiError(error);
      const fieldErrors = (body?.fieldErrors ?? [])
        .map((fe) => ({ ...fe, field: SERVER_FIELDS[fe.field] ?? toFormPath(fe.field) }))
        .filter(
          (fe) =>
            (FIELDS as readonly string[]).includes(fe.field) ||
            fe.field === 'contacts' ||
            fe.field.startsWith('contacts.'),
        );
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
    <form noValidate onSubmit={handleSubmit(onValid, requestSummaryFocus)} className="max-w-3xl space-y-8">
      {submitCount > 0 && <ErrorSummary ref={summaryRef} errors={summary} />}
      {formError && (
        <p
          role="alert"
          className="rounded-md border-2 border-destructive bg-card p-4 font-medium text-destructive"
        >
          {t(formError === 'conflict' ? 'form.errors.conflict' : 'form.errors.generic')}
        </p>
      )}

      <div className="grid gap-4 md:grid-cols-2">
        <FormField
          id={fieldId('name')}
          label={labels.name}
          hint={missingName ? t('company.hints.nameMissing') : undefined}
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
                validate: (v) =>
                  v.trim() === ''
                    ? t('form.errors.required')
                    : isNamePlaceholder(v)
                      ? t('company.errors.namePlaceholder')
                      : !LEGACY_MARKS_PATTERN.test(v) || t('company.errors.nameLegacyMarks'),
              })}
            />
          )}
        </FormField>
        <FormField
          id={fieldId('vatCode')}
          label={labels.vatCode}
          hint={t(missingVatCode ? 'company.hints.vatCodeMissing' : 'company.hints.vatCode')}
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
                validate: (v) => !isVatCodePlaceholder(v) || t('company.errors.vatCodePlaceholder'),
              })}
            />
          )}
        </FormField>
        <FormField
          id={fieldId('businessCategoryId')}
          label={labels.businessCategoryId}
          error={errors.businessCategoryId?.message}
        >
          {(p) => (
            <NativeSelect {...p} disabled={!categories.data} {...register('businessCategoryId')}>
              <option value="">{t('company.noCategory')}</option>
              {withCurrent(categories.data, [company?.businessCategory]).map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </NativeSelect>
          )}
        </FormField>
        <FormField
          id={fieldId('activity')}
          label={labels.activity}
          hint={t('company.hints.activity')}
          error={errors.activity?.message}
          className="md:col-span-2"
        >
          {(p) => <Input {...p} autoComplete="off" {...register('activity', { maxLength: max(200) })} />}
        </FormField>
      </div>

      <Group legend={t('company.rating.heading')} intro={t('company.rating.intro')}>
        <Controller
          control={control}
          name="reliability"
          render={({ field }) => (
            <RatingInput
              ref={field.ref}
              id={fieldId('reliability')}
              legend={labels.reliability}
              hint={t('company.hints.reliability')}
              kind="stars"
              value={field.value}
              onChange={field.onChange}
              error={errors.reliability?.message}
            />
          )}
        />
        <Controller
          control={control}
          name="projectInterest"
          render={({ field }) => (
            <RatingInput
              ref={field.ref}
              id={fieldId('projectInterest')}
              legend={labels.projectInterest}
              hint={t('company.hints.projectInterest')}
              kind="thumbs"
              value={field.value}
              onChange={field.onChange}
              error={errors.projectInterest?.message}
            />
          )}
        />
      </Group>

      <Group legend={t('company.legalSeat')}>
        <FormField
          id={fieldId('legalAddress')}
          label={labels.legalAddress}
          hint={t('company.hints.legalAddress')}
          error={errors.legalAddress?.message}
        >
          {(p) => <Input {...p} autoComplete="off" {...register('legalAddress', { maxLength: max(300) })} />}
        </FormField>
        <MunicipalityFormField
          name="legalMunicipality"
          label={labels.legalMunicipality}
          error={errors.legalMunicipality?.message}
          control={control}
        />
      </Group>

      <Group legend={t('company.operatingSeat')} intro={t('company.hints.operatingSeat')}>
        <FormField
          id={fieldId('operatingAddress')}
          label={labels.operatingAddress}
          hint={t('company.hints.legalAddress')}
          error={errors.operatingAddress?.message}
        >
          {(p) => (
            <Input {...p} autoComplete="off" {...register('operatingAddress', { maxLength: max(300) })} />
          )}
        </FormField>
        <MunicipalityFormField
          name="operatingMunicipality"
          label={labels.operatingMunicipality}
          error={errors.operatingMunicipality?.message}
          control={control}
        />
      </Group>

      <Group legend={t('company.contactDetails')}>
        <FormField id={fieldId('phone')} label={labels.phone} error={errors.phone?.message}>
          {(p) => <Input {...p} type="tel" autoComplete="off" {...register('phone', phoneRules(30))} />}
        </FormField>
        <FormField id={fieldId('mobile')} label={labels.mobile} error={errors.mobile?.message}>
          {(p) => <Input {...p} type="tel" autoComplete="off" {...register('mobile', phoneRules(30))} />}
        </FormField>
        <FormField id={fieldId('fax')} label={labels.fax} error={errors.fax?.message}>
          {(p) => <Input {...p} type="tel" autoComplete="off" {...register('fax', phoneRules(30))} />}
        </FormField>
        <FormField id={fieldId('email')} label={labels.email} error={errors.email?.message}>
          {(p) => <Input {...p} type="email" autoComplete="off" {...register('email', emailRules)} />}
        </FormField>
        <FormField
          id={fieldId('secondaryEmail')}
          label={labels.secondaryEmail}
          error={errors.secondaryEmail?.message}
        >
          {(p) => (
            <Input {...p} type="email" autoComplete="off" {...register('secondaryEmail', emailRules)} />
          )}
        </FormField>
      </Group>

      <ContactsFields
        control={control}
        register={register}
        errors={errors}
        roles={withCurrent(
          roles.data,
          (company?.contacts ?? []).map((c) => c.role),
        )}
        rolesLoaded={roles.data !== undefined}
        fieldId={fieldId}
      />

      <Button type="submit" size="lg" className="w-full sm:w-auto" disabled={isSubmitting}>
        {isSubmitting ? t('company.saving') : submitLabel}
      </Button>
    </form>
  );
};

/** Gruppo di campi con la sua legenda: sedi e recapiti (US-510). */
const Group = ({ legend, intro, children }: { legend: string; intro?: string; children: ReactNode }) => (
  <fieldset className="space-y-4">
    <legend className="text-base font-semibold">{legend}</legend>
    {intro && <p className="text-sm text-muted-foreground">{intro}</p>}
    <div className="grid gap-4 md:grid-cols-2">{children}</div>
  </fieldset>
);

type MunicipalityFormFieldProps = {
  name: 'legalMunicipality' | 'operatingMunicipality';
  label: string;
  error?: string;
  control: Control<CompanyFormValues>;
};

/** Comune scelto dall'elenco ufficiale (US-1007): il testo scritto senza sceglierlo è un errore. */
const MunicipalityFormField = ({ name, label, error, control }: MunicipalityFormFieldProps) => {
  const { t } = useTranslation();
  return (
    <FormField id={fieldId(name)} label={label} hint={t('municipalityField.hint')} error={error}>
      {(p) => (
        <Controller
          control={control}
          name={name}
          rules={{ validate: (v: MunicipalityValue) => !isUnchosen(v) || t('municipalityField.choose') }}
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
  );
};
