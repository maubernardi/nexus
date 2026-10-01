import { useMemo, useState } from 'react';
import { Plus, Trash2 } from 'lucide-react';
import { Controller, useFieldArray, useForm, type FieldErrors, type FieldPath } from 'react-hook-form';
import { useTranslation } from 'react-i18next';

import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import { Input } from '@/components/ui/input';
import { NativeSelect } from '@/components/ui/native-select';
import { Textarea } from '@/components/ui/textarea';
import type { Beneficiary, BeneficiaryCreate } from '@/config/api/beneficiaryApi';
import { apiError, toFormPath } from '@/config/api/errors';
import { useCreateBeneficiary } from '@/config/hooks/useBeneficiaries';
import { useZones } from '@/config/hooks/useReferenceData';
import { useErrorSummaryFocus } from '@/hooks/useErrorSummaryFocus';
import {
  EDUCATION_LEVELS,
  GENDERS,
  LANGUAGE_LEVELS,
  LICENSE_TYPES,
  TRANSPORT_MODES,
  type EducationLevel,
  type Gender,
  type LanguageLevel,
  type LicenseType,
  type TransportMode,
} from '@/lib/domainValues';
import { countryOptions, languageOptions } from '@/lib/isoNames';
import { CheckboxGroup } from '@/molecules/CheckboxGroup/CheckboxGroup';
import { ErrorSummary, type SummaryError } from '@/molecules/ErrorSummary/ErrorSummary';
import { FormField } from '@/molecules/FormField/FormField';

export type BeneficiaryFormValues = {
  firstName: string;
  lastName: string;
  birthYear: string;
  gender: Gender | '';
  nationality: string;
  citizenship: string;
  residenceZoneId: string;
  licenseTypes: LicenseType[];
  hasVehicle: boolean;
  transportMode: TransportMode | '';
  educationLevel: EducationLevel | '';
  hasLaw68: boolean;
  constraints: string;
  languages: { language: string; level: LanguageLevel | '' }[];
};

const EMPTY: BeneficiaryFormValues = {
  firstName: '',
  lastName: '',
  birthYear: '',
  gender: '',
  nationality: '',
  citizenship: '',
  residenceZoneId: '',
  licenseTypes: [],
  hasVehicle: false,
  transportMode: '',
  educationLevel: '',
  hasLaw68: false,
  constraints: '',
  languages: [],
};

const fieldId = (path: string): string => `beneficiary-${path.replace(/\./g, '-')}`;

const toPayload = (v: BeneficiaryFormValues): BeneficiaryCreate => ({
  firstName: v.firstName.trim(),
  lastName: v.lastName.trim(),
  birthYear: Number(v.birthYear),
  gender: v.gender as Gender,
  nationality: v.nationality || null,
  citizenship: v.citizenship || null,
  residenceZoneId: v.residenceZoneId,
  licenseTypes: v.licenseTypes,
  hasVehicle: v.hasVehicle,
  transportMode: v.transportMode || null,
  hasLaw68: v.hasLaw68,
  educationLevel: v.educationLevel || null,
  constraints: v.constraints.trim() || null,
  languages: v.languages.map((l) => ({ language: l.language, level: l.level as LanguageLevel })),
});

type BeneficiaryFormProps = {
  onSuccess: (beneficiary: Beneficiary) => void;
};

/** Registrazione di un beneficiario (US-201): quattro sezioni, errori sui campi e riepilogo focalizzato all'invio. */
export const BeneficiaryForm = ({ onSuccess }: BeneficiaryFormProps) => {
  const { t } = useTranslation();
  const zones = useZones();
  const createBeneficiary = useCreateBeneficiary();
  const [genericError, setGenericError] = useState(false);
  const countries = useMemo(() => countryOptions(), []);
  const languages = useMemo(() => languageOptions(), []);
  const currentYear = new Date().getFullYear();

  const {
    register,
    control,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting, submitCount },
  } = useForm<BeneficiaryFormValues>({
    defaultValues: EMPTY,
    mode: 'onSubmit',
    reValidateMode: 'onChange',
    // il focus va al riepilogo degli errori (non al primo campo): lo gestisce useErrorSummaryFocus
    shouldFocusError: false,
  });
  const languageRows = useFieldArray({ control, name: 'languages' });

  const { summaryRef, requestSummaryFocus } = useErrorSummaryFocus(submitCount, errors);

  const required = { value: true, message: t('form.errors.required') };
  const notBlank = (value: string): true | string => value.trim() !== '' || t('form.errors.required');

  const labels: Record<string, string> = {
    firstName: t('beneficiary.fields.firstName'),
    lastName: t('beneficiary.fields.lastName'),
    birthYear: t('beneficiary.fields.birthYear'),
    gender: t('beneficiary.fields.gender'),
    nationality: t('beneficiary.fields.nationality'),
    citizenship: t('beneficiary.fields.citizenship'),
    residenceZoneId: t('beneficiary.fields.residenceZone'),
    transportMode: t('beneficiary.fields.transportMode'),
    educationLevel: t('beneficiary.fields.educationLevel'),
    languages: t('beneficiary.fields.languages'),
    constraints: t('beneficiary.fields.constraints'),
  };

  const summary: SummaryError[] = [];
  const collect = (errs: FieldErrors<BeneficiaryFormValues>): void => {
    for (const key of Object.keys(labels) as (keyof BeneficiaryFormValues)[]) {
      const error = errs[key];
      if (!error) continue;
      if (key === 'languages' && Array.isArray(error)) {
        error.forEach((row, i) => {
          for (const part of ['language', 'level'] as const) {
            const message = row?.[part]?.message;
            if (message) {
              const label = t(
                part === 'language' ? 'beneficiary.fields.language' : 'beneficiary.fields.level',
                {
                  n: i + 1,
                },
              );
              summary.push({ fieldId: fieldId(`languages.${i}.${part}`), message: `${label}: ${message}` });
            }
          }
        });
      }
      if ('message' in error && typeof error.message === 'string' && error.message) {
        summary.push({ fieldId: fieldId(key), message: `${labels[key]}: ${error.message}` });
      }
    }
  };
  collect(errors);

  const onValid = async (values: BeneficiaryFormValues): Promise<void> => {
    setGenericError(false);
    const seen = new Set<string>();
    const duplicate = values.languages.some((l) =>
      seen.has(l.language) ? true : (seen.add(l.language), false),
    );
    if (duplicate) {
      setError('languages', { type: 'duplicate', message: t('form.errors.duplicateLanguage') });
      requestSummaryFocus();
      return;
    }
    try {
      onSuccess(await createBeneficiary.mutateAsync(toPayload(values)));
    } catch (error) {
      const fieldErrors = apiError(error)?.fieldErrors ?? [];
      if (fieldErrors.length > 0) {
        fieldErrors.forEach((fe) =>
          setError(toFormPath(fe.field) as FieldPath<BeneficiaryFormValues>, {
            type: 'server',
            message: fe.message,
          }),
        );
      } else {
        setGenericError(true);
      }
      requestSummaryFocus();
    }
  };

  const errorOf = (path: string): string | undefined => {
    const parts = path.split('.');
    let node: unknown = errors;
    for (const part of parts) node = (node as Record<string, unknown> | undefined)?.[part];
    return (node as { message?: string } | undefined)?.message;
  };

  return (
    <form noValidate onSubmit={handleSubmit(onValid, requestSummaryFocus)} className="space-y-8">
      {submitCount > 0 && <ErrorSummary ref={summaryRef} errors={summary} />}
      {genericError && (
        <p
          role="alert"
          className="rounded-md border-2 border-destructive bg-card p-4 font-medium text-destructive"
        >
          {t('form.errors.generic')}
        </p>
      )}

      <section aria-labelledby="beneficiary-section-personal" className="space-y-4">
        <h2 id="beneficiary-section-personal" className="text-lg font-semibold text-primary">
          {t('beneficiary.sections.personal')}
        </h2>
        <div className="grid gap-4 md:grid-cols-2">
          <FormField id={fieldId('firstName')} label={labels.firstName} required error={errorOf('firstName')}>
            {(p) => (
              <Input
                {...p}
                autoComplete="off"
                {...register('firstName', {
                  required,
                  maxLength: { value: 100, message: t('form.errors.maxLength', { max: 100 }) },
                  validate: notBlank,
                })}
              />
            )}
          </FormField>
          <FormField id={fieldId('lastName')} label={labels.lastName} required error={errorOf('lastName')}>
            {(p) => (
              <Input
                {...p}
                autoComplete="off"
                {...register('lastName', {
                  required,
                  maxLength: { value: 100, message: t('form.errors.maxLength', { max: 100 }) },
                  validate: notBlank,
                })}
              />
            )}
          </FormField>
          <FormField
            id={fieldId('birthYear')}
            label={labels.birthYear}
            required
            hint={t('beneficiary.fields.birthYearHint')}
            error={errorOf('birthYear')}
          >
            {(p) => (
              <Input
                {...p}
                inputMode="numeric"
                maxLength={4}
                {...register('birthYear', {
                  required,
                  validate: (v) =>
                    (/^\d{4}$/.test(v) && Number(v) >= 1900 && Number(v) <= currentYear) ||
                    t('form.errors.birthYear', { year: currentYear }),
                })}
              />
            )}
          </FormField>
          <FormField id={fieldId('gender')} label={labels.gender} required error={errorOf('gender')}>
            {(p) => (
              <NativeSelect {...p} {...register('gender', { required })}>
                <option value="">{t('form.select')}</option>
                {GENDERS.map((g) => (
                  <option key={g} value={g}>
                    {t(`enums.gender.${g}`)}
                  </option>
                ))}
              </NativeSelect>
            )}
          </FormField>
          <FormField id={fieldId('nationality')} label={labels.nationality} error={errorOf('nationality')}>
            {(p) => (
              <NativeSelect {...p} {...register('nationality')}>
                <option value="">{t('form.select')}</option>
                {countries.map((c) => (
                  <option key={c.value} value={c.value}>
                    {c.label}
                  </option>
                ))}
              </NativeSelect>
            )}
          </FormField>
          <FormField id={fieldId('citizenship')} label={labels.citizenship} error={errorOf('citizenship')}>
            {(p) => (
              <NativeSelect {...p} {...register('citizenship')}>
                <option value="">{t('form.select')}</option>
                {countries.map((c) => (
                  <option key={c.value} value={c.value}>
                    {c.label}
                  </option>
                ))}
              </NativeSelect>
            )}
          </FormField>
          <FormField
            id={fieldId('residenceZoneId')}
            label={labels.residenceZoneId}
            required
            error={errorOf('residenceZoneId')}
          >
            {(p) => (
              <NativeSelect {...p} disabled={zones.isPending} {...register('residenceZoneId', { required })}>
                <option value="">{t('form.select')}</option>
                {(zones.data ?? []).map((z) => (
                  <option key={z.id} value={z.id}>
                    {z.name}
                  </option>
                ))}
              </NativeSelect>
            )}
          </FormField>
        </div>
      </section>

      <section aria-labelledby="beneficiary-section-mobility" className="space-y-4">
        <h2 id="beneficiary-section-mobility" className="text-lg font-semibold text-primary">
          {t('beneficiary.sections.mobility')}
        </h2>
        <Controller
          control={control}
          name="licenseTypes"
          render={({ field }) => (
            <CheckboxGroup
              legend={t('beneficiary.fields.licenseTypes')}
              hint={t('beneficiary.fields.licenseTypesHint')}
              options={LICENSE_TYPES.map((l) => ({ value: l, label: l }))}
              value={field.value}
              onChange={(value) => field.onChange(value as LicenseType[])}
            />
          )}
        />
        <div className="grid gap-4 md:grid-cols-2">
          <div className="flex min-h-11 items-center gap-3">
            <Checkbox id={fieldId('hasVehicle')} {...register('hasVehicle')} />
            <label htmlFor={fieldId('hasVehicle')} className="text-sm font-medium">
              {t('beneficiary.fields.hasVehicle')}
            </label>
          </div>
          <FormField
            id={fieldId('transportMode')}
            label={labels.transportMode}
            error={errorOf('transportMode')}
          >
            {(p) => (
              <NativeSelect {...p} {...register('transportMode')}>
                <option value="">{t('form.select')}</option>
                {TRANSPORT_MODES.map((m) => (
                  <option key={m} value={m}>
                    {t(`enums.transportMode.${m}`)}
                  </option>
                ))}
              </NativeSelect>
            )}
          </FormField>
        </div>
      </section>

      <section aria-labelledby="beneficiary-section-education" className="space-y-4">
        <h2 id="beneficiary-section-education" className="text-lg font-semibold text-primary">
          {t('beneficiary.sections.education')}
        </h2>
        <FormField
          id={fieldId('educationLevel')}
          label={labels.educationLevel}
          error={errorOf('educationLevel')}
          className="md:w-1/2"
        >
          {(p) => (
            <NativeSelect {...p} {...register('educationLevel')}>
              <option value="">{t('form.select')}</option>
              {EDUCATION_LEVELS.map((e) => (
                <option key={e} value={e}>
                  {t(`enums.educationLevel.${e}`)}
                </option>
              ))}
            </NativeSelect>
          )}
        </FormField>
        <fieldset
          id={fieldId('languages')}
          tabIndex={-1}
          className="space-y-3"
          aria-describedby={errorOf('languages') ? `${fieldId('languages')}-error` : undefined}
        >
          <legend className="text-sm font-medium">{labels.languages}</legend>
          {languageRows.fields.length === 0 && (
            <p className="text-sm text-muted-foreground">{t('beneficiary.fields.noLanguages')}</p>
          )}
          {languageRows.fields.map((row, i) => (
            <div key={row.id} className="grid items-end gap-3 sm:grid-cols-[1fr_1fr_auto]">
              <FormField
                id={fieldId(`languages.${i}.language`)}
                label={t('beneficiary.fields.language', { n: i + 1 })}
                required
                error={errorOf(`languages.${i}.language`)}
              >
                {(p) => (
                  <NativeSelect {...p} {...register(`languages.${i}.language` as const, { required })}>
                    <option value="">{t('form.select')}</option>
                    {languages.map((l) => (
                      <option key={l.value} value={l.value}>
                        {l.label}
                      </option>
                    ))}
                  </NativeSelect>
                )}
              </FormField>
              <FormField
                id={fieldId(`languages.${i}.level`)}
                label={t('beneficiary.fields.level', { n: i + 1 })}
                required
                error={errorOf(`languages.${i}.level`)}
              >
                {(p) => (
                  <NativeSelect
                    {...p}
                    {...register(`languages.${i}.level` as const, {
                      required: { value: true, message: t('form.errors.languageLevel') },
                    })}
                  >
                    <option value="">{t('form.select')}</option>
                    {LANGUAGE_LEVELS.map((lv) => (
                      <option key={lv} value={lv}>
                        {t(`enums.languageLevel.${lv}`)}
                      </option>
                    ))}
                  </NativeSelect>
                )}
              </FormField>
              <Button
                type="button"
                variant="outline"
                size="icon-lg"
                aria-label={t('beneficiary.fields.removeLanguage', { n: i + 1 })}
                onClick={() => languageRows.remove(i)}
              >
                <Trash2 aria-hidden="true" />
              </Button>
            </div>
          ))}
          {errorOf('languages') && (
            <p id={`${fieldId('languages')}-error`} className="text-sm font-medium text-destructive">
              {errorOf('languages')}
            </p>
          )}
          <Button
            type="button"
            variant="outline"
            onClick={() => languageRows.append({ language: '', level: '' })}
          >
            <Plus aria-hidden="true" />
            {t('beneficiary.fields.addLanguage')}
          </Button>
        </fieldset>
      </section>

      <section
        aria-labelledby="beneficiary-section-confidential"
        className="space-y-4 rounded-xl border border-border bg-muted p-4"
      >
        <h2 id="beneficiary-section-confidential" className="text-lg font-semibold text-primary">
          {t('beneficiary.sections.confidential')}
        </h2>
        <p className="text-sm text-muted-foreground">{t('beneficiary.confidentialNote')}</p>
        <div className="flex min-h-11 items-center gap-3">
          <Checkbox id={fieldId('hasLaw68')} {...register('hasLaw68')} />
          <label htmlFor={fieldId('hasLaw68')} className="text-sm font-medium">
            {t('beneficiary.fields.hasLaw68')}
          </label>
        </div>
        <FormField
          id={fieldId('constraints')}
          label={labels.constraints}
          hint={t('beneficiary.fields.constraintsHint')}
          error={errorOf('constraints')}
        >
          {(p) => (
            <Textarea
              {...p}
              rows={3}
              {...register('constraints', {
                maxLength: { value: 2000, message: t('form.errors.maxLength', { max: 2000 }) },
              })}
            />
          )}
        </FormField>
      </section>

      <Button type="submit" size="lg" className="w-full sm:w-auto" disabled={isSubmitting}>
        {isSubmitting ? t('beneficiary.submitting') : t('beneficiary.submit')}
      </Button>
    </form>
  );
};
