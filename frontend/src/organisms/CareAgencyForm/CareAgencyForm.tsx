import { useState } from 'react';
import { useForm, type FieldPath } from 'react-hook-form';
import { useTranslation } from 'react-i18next';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { NativeSelect } from '@/components/ui/native-select';
import { CARE_AGENCY_KINDS, type CareAgency, type CareAgencyKind } from '@/config/api/documentCatalogApi';
import { apiError } from '@/config/api/errors';
import { useUpdateCareAgency } from '@/config/hooks/useDocumentCatalogs';
import { useErrorSummaryFocus } from '@/hooks/useErrorSummaryFocus';
import { todayIso } from '@/lib/isoDate';
import { ErrorSummary, type SummaryError } from '@/molecules/ErrorSummary/ErrorSummary';
import { FormField } from '@/molecules/FormField/FormField';

type Values = {
  kind: CareAgencyKind;
  fiscalCode: string;
  legalAddress: string;
  representativeName: string;
  representativeRole: string;
  representativeBirthPlace: string;
  representativeBirthDate: string;
  referentName: string;
  referentPhone: string;
  referentEmail: string;
};

const TEXT_FIELDS = [
  'fiscalCode',
  'legalAddress',
  'representativeName',
  'representativeRole',
  'representativeBirthPlace',
  'representativeBirthDate',
  'referentName',
  'referentPhone',
  'referentEmail',
] as const;
const FIELDS = ['kind', ...TEXT_FIELDS] as const;

const fieldId = (field: keyof Values): string => `care-agency-${field}`;

type CareAgencyFormProps = {
  agency: CareAgency;
  onDone: (text: string) => void;
  onCancel: () => void;
};

/**
 * Dati di un ente di presa in carico (US-115): tutti facoltativi tranne la tipologia; finiscono nella convenzione e nel
 * progetto personalizzato dei tirocini di inclusione.
 */
export const CareAgencyForm = ({ agency, onDone, onCancel }: CareAgencyFormProps) => {
  const { t } = useTranslation();
  const update = useUpdateCareAgency();
  const [formError, setFormError] = useState<'generic' | 'conflict' | null>(null);
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting, submitCount },
  } = useForm<Values>({
    defaultValues: {
      kind: agency.kind,
      ...Object.fromEntries(TEXT_FIELDS.map((f) => [f, agency[f] ?? ''])),
    },
    mode: 'onSubmit',
    reValidateMode: 'onChange',
    shouldFocusError: false,
  });
  const { summaryRef, requestSummaryFocus } = useErrorSummaryFocus(submitCount, errors);
  const label = (field: keyof Values): string => t(`documentCatalogs.agencies.fields.${field}`);
  const summary: SummaryError[] = FIELDS.flatMap((field) => {
    const message = errors[field]?.message;
    return message ? [{ fieldId: fieldId(field), message: `${label(field)}: ${message}` }] : [];
  });
  const max = (n: number) => ({ value: n, message: t('form.errors.maxLength', { max: n }) });

  const onValid = async (values: Values): Promise<void> => {
    setFormError(null);
    const data = Object.fromEntries(
      TEXT_FIELDS.map((f) => [f, values[f].trim() === '' ? undefined : values[f].trim()]),
    );
    try {
      const saved = await update.mutateAsync({
        id: agency.id,
        version: agency.version,
        data: { kind: values.kind, ...data },
      });
      onDone(t('documentCatalogs.agencies.saved', { name: saved.name }));
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
        setFormError(body?.status === 409 ? 'conflict' : 'generic');
      }
      requestSummaryFocus();
    }
  };

  const text = (
    field: (typeof TEXT_FIELDS)[number],
    n: number,
    options: { type?: string; hint?: string } = {},
  ) => (
    <FormField id={fieldId(field)} label={label(field)} hint={options.hint} error={errors[field]?.message}>
      {(p) => (
        <Input
          {...p}
          type={options.type ?? 'text'}
          max={options.type === 'date' ? todayIso() : undefined}
          autoComplete="off"
          {...register(field, { maxLength: options.type === 'date' ? undefined : max(n) })}
        />
      )}
    </FormField>
  );

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
      <p className="text-sm text-muted-foreground">{t('documentCatalogs.agencies.privacy')}</p>
      <fieldset className="space-y-4">
        <legend className="font-semibold">{t('documentCatalogs.agencies.sections.agency')}</legend>
        <div className="grid max-w-3xl gap-4 md:grid-cols-2">
          <FormField id={fieldId('kind')} label={label('kind')} required error={errors.kind?.message}>
            {(p) => (
              <NativeSelect {...p} {...register('kind')}>
                {CARE_AGENCY_KINDS.map((k) => (
                  <option key={k} value={k}>
                    {t(`documentCatalogs.agencies.kinds.${k}`)}
                  </option>
                ))}
              </NativeSelect>
            )}
          </FormField>
          {text('fiscalCode', 16, { hint: t('documentCatalogs.agencies.hints.fiscalCode') })}
          <div className="md:col-span-2">{text('legalAddress', 300)}</div>
        </div>
      </fieldset>
      <fieldset className="space-y-4">
        <legend className="font-semibold">{t('documentCatalogs.agencies.sections.representative')}</legend>
        <div className="grid max-w-3xl gap-4 md:grid-cols-2">
          {text('representativeName', 200)}
          {text('representativeRole', 200)}
          {text('representativeBirthPlace', 200, { hint: t('documentCatalogs.agencies.hints.birth') })}
          {text('representativeBirthDate', 10, {
            type: 'date',
            hint: t('documentCatalogs.agencies.hints.birth'),
          })}
        </div>
      </fieldset>
      <fieldset className="space-y-4">
        <legend className="font-semibold">{t('documentCatalogs.agencies.sections.referent')}</legend>
        <div className="grid max-w-3xl gap-4 md:grid-cols-2">
          {text('referentName', 200)}
          {text('referentPhone', 30, { type: 'tel' })}
          {text('referentEmail', 254, { type: 'email' })}
        </div>
      </fieldset>
      <div className="flex flex-wrap gap-3">
        <Button type="submit" size="lg" disabled={isSubmitting}>
          {t('catalog.save')}
        </Button>
        <Button type="button" size="lg" variant="outline" onClick={onCancel}>
          {t('catalog.cancel')}
        </Button>
      </div>
    </form>
  );
};
