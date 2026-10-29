import { useEffect, useRef, useState } from 'react';
import { useForm, type FieldPath } from 'react-hook-form';
import { useTranslation } from 'react-i18next';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import type {
  LegalRepresentative,
  LegalRepresentativeForm as LegalRepresentativePayload,
} from '@/config/api/companyLegalDataApi';
import { apiError } from '@/config/api/errors';
import { useLegalRepresentative, useSaveLegalRepresentative } from '@/config/hooks/useCompanyLegalData';
import { useErrorSummaryFocus } from '@/hooks/useErrorSummaryFocus';
import { ErrorSummary, type SummaryError } from '@/molecules/ErrorSummary/ErrorSummary';
import { FormField } from '@/molecules/FormField/FormField';

type Values = {
  firstName: string;
  lastName: string;
  fiscalCode: string;
  birthDate: string;
  birthPlace: string;
  residence: string;
};

const FIELDS = ['firstName', 'lastName', 'fiscalCode', 'birthDate', 'birthPlace', 'residence'] as const;
const fieldId = (field: keyof Values): string => `legal-rep-${field}`;
const dateFormat = new Intl.DateTimeFormat('it-IT', { dateStyle: 'long' });

/** Giorno ISO (aaaa-mm-gg) in esteso, senza passare dal fuso orario. */
const formatDay = (iso: string): string => {
  const [y, m, d] = iso.split('-').map(Number);
  return dateFormat.format(new Date(y, m - 1, d));
};

/**
 * Legale rappresentante dell'azienda (US-506): può non coincidere con il referente. Dati personali per i documenti dei
 * tirocini; nell'audit solo che cosa è cambiato.
 */
export const LegalRepresentativeSection = ({ companyId }: { companyId: string }) => {
  const { t } = useTranslation();
  const rep = useLegalRepresentative(companyId);
  const save = useSaveLegalRepresentative();
  const [editing, setEditing] = useState(false);
  const [outcome, setOutcome] = useState<{ text: string; error?: boolean } | null>(null);
  const formHeadingRef = useRef<HTMLHeadingElement>(null);
  const outcomeRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (editing) formHeadingRef.current?.focus();
  }, [editing]);
  useEffect(() => {
    if (outcome) outcomeRef.current?.focus();
  }, [outcome]);

  return (
    <section aria-labelledby="legal-rep-heading" className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h2 id="legal-rep-heading" className="text-lg font-semibold text-primary">
          {t('legalRep.heading')}
        </h2>
        {!editing && rep.data !== undefined && (
          <Button
            size="lg"
            variant={rep.data ? 'outline' : 'default'}
            onClick={() => {
              setOutcome(null);
              setEditing(true);
            }}
          >
            {t(rep.data ? 'legalRep.edit' : 'legalRep.add')}
          </Button>
        )}
      </div>
      <p className="text-muted-foreground">{t('legalRep.intro')}</p>

      {outcome && (
        <div
          ref={outcomeRef}
          tabIndex={-1}
          className={
            outcome.error
              ? 'rounded-md border-2 border-destructive bg-card p-4 font-medium text-destructive'
              : 'rounded-md border-l-4 border-primary bg-card p-4 font-medium'
          }
        >
          {outcome.text}
        </div>
      )}

      {editing && rep.data !== undefined ? (
        <div className="space-y-4 rounded-xl border border-border bg-card p-4">
          <h3 ref={formHeadingRef} tabIndex={-1} className="text-base font-semibold">
            {t(rep.data ? 'legalRep.editHeading' : 'legalRep.addHeading')}
          </h3>
          <LegalRepresentativeForm
            current={rep.data}
            submit={(payload) => save.mutateAsync({ companyId, payload })}
            onSaved={() => {
              setEditing(false);
              setOutcome({ text: t('legalRep.saved') });
            }}
            onConflict={(reason) => {
              setEditing(false);
              setOutcome({ text: t('legalRep.conflict', { reason }), error: true });
            }}
            onCancel={() => setEditing(false)}
          />
        </div>
      ) : rep.isError ? (
        <p role="alert" className="font-medium text-destructive">
          {t('legalRep.loadError')}
        </p>
      ) : rep.data === undefined ? (
        <p>{t('common.loading')}</p>
      ) : rep.data === null ? (
        <p className="rounded-xl border border-border bg-card p-4">{t('legalRep.none')}</p>
      ) : (
        <LegalRepresentativeDetails rep={rep.data} />
      )}
    </section>
  );
};

/** Dati del legale rappresentante in sola lettura, "Non indicato" per quelli mancanti. */
export const LegalRepresentativeDetails = ({ rep }: { rep: LegalRepresentative }) => {
  const { t } = useTranslation();
  const value = (v?: string | null): string => v || t('legalRep.missing');
  return (
    <dl className="grid gap-x-6 gap-y-2 rounded-xl border border-border bg-card p-4 sm:grid-cols-[max-content_1fr]">
      <dt className="font-medium">{t('legalRep.fields.name')}</dt>
      <dd>
        {rep.firstName} {rep.lastName}
      </dd>
      <dt className="font-medium">{t('legalRep.fields.fiscalCode')}</dt>
      <dd className="font-mono">{value(rep.fiscalCode)}</dd>
      <dt className="font-medium">{t('legalRep.fields.birth')}</dt>
      <dd>
        {rep.birthDate || rep.birthPlace
          ? [rep.birthDate ? formatDay(rep.birthDate) : null, rep.birthPlace].filter(Boolean).join(', ')
          : t('legalRep.missing')}
      </dd>
      <dt className="font-medium">{t('legalRep.fields.residence')}</dt>
      <dd>{value(rep.residence)}</dd>
    </dl>
  );
};

type LegalRepresentativeFormProps = {
  current: LegalRepresentative | null;
  submit: (payload: LegalRepresentativePayload) => Promise<unknown>;
  onSaved: () => void;
  onConflict: (reason: string) => void;
  onCancel: () => void;
};

/**
 * Modulo del legale rappresentante: nella scheda dell'azienda (Call Center) e, se manca, nella scheda del tirocinio
 * (Tutor, US-1001). Chi lo usa decide dove salvare.
 */
export const LegalRepresentativeForm = ({
  current,
  submit,
  onSaved,
  onConflict,
  onCancel,
}: LegalRepresentativeFormProps) => {
  const { t } = useTranslation();
  const [genericError, setGenericError] = useState(false);
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting, submitCount },
  } = useForm<Values>({
    defaultValues: {
      firstName: current?.firstName ?? '',
      lastName: current?.lastName ?? '',
      fiscalCode: current?.fiscalCode ?? '',
      birthDate: current?.birthDate ?? '',
      birthPlace: current?.birthPlace ?? '',
      residence: current?.residence ?? '',
    },
    mode: 'onSubmit',
    reValidateMode: 'onChange',
    shouldFocusError: false,
  });
  const { summaryRef, requestSummaryFocus } = useErrorSummaryFocus(submitCount, errors);

  const labels: Record<keyof Values, string> = {
    firstName: t('legalRep.fields.firstName'),
    lastName: t('legalRep.fields.lastName'),
    fiscalCode: t('legalRep.fields.fiscalCode'),
    birthDate: t('legalRep.fields.birthDate'),
    birthPlace: t('legalRep.fields.birthPlace'),
    residence: t('legalRep.fields.residence'),
  };
  const summary: SummaryError[] = FIELDS.flatMap((field) => {
    const message = errors[field]?.message;
    return message ? [{ fieldId: fieldId(field), message: `${labels[field]}: ${message}` }] : [];
  });
  const required = (v: string) => v.trim() !== '' || t('form.errors.required');
  const max = (n: number) => ({ value: n, message: t('form.errors.maxLength', { max: n }) });
  const today = new Date().toISOString().slice(0, 10);

  const onValid = async (v: Values): Promise<void> => {
    setGenericError(false);
    try {
      await submit({
        firstName: v.firstName.trim(),
        lastName: v.lastName.trim(),
        fiscalCode: v.fiscalCode.trim().toUpperCase() || null,
        birthDate: v.birthDate || null,
        birthPlace: v.birthPlace.trim() || null,
        residence: v.residence.trim() || null,
        version: current?.version ?? null,
      });
      onSaved();
    } catch (error) {
      const body = apiError(error);
      if (body?.status === 409) {
        onConflict(body.message);
        return;
      }
      const fieldErrors = (body?.fieldErrors ?? []).filter((fe) =>
        (FIELDS as readonly string[]).includes(fe.field),
      );
      if (fieldErrors.length > 0) {
        fieldErrors.forEach((fe) =>
          setError(fe.field as FieldPath<Values>, { type: 'server', message: fe.message }),
        );
      } else {
        setGenericError(true);
      }
      requestSummaryFocus();
    }
  };

  return (
    <form noValidate onSubmit={handleSubmit(onValid, requestSummaryFocus)} className="space-y-4">
      {submitCount > 0 && <ErrorSummary ref={summaryRef} errors={summary} />}
      {genericError && (
        <p
          role="alert"
          className="rounded-md border-2 border-destructive bg-card p-4 font-medium text-destructive"
        >
          {t('form.errors.generic')}
        </p>
      )}
      <div className="grid gap-4 md:grid-cols-2">
        <FormField
          id={fieldId('firstName')}
          label={labels.firstName}
          required
          error={errors.firstName?.message}
        >
          {(p) => (
            <Input
              {...p}
              autoComplete="off"
              {...register('firstName', { validate: required, maxLength: max(100) })}
            />
          )}
        </FormField>
        <FormField id={fieldId('lastName')} label={labels.lastName} required error={errors.lastName?.message}>
          {(p) => (
            <Input
              {...p}
              autoComplete="off"
              {...register('lastName', { validate: required, maxLength: max(100) })}
            />
          )}
        </FormField>
        <FormField
          id={fieldId('fiscalCode')}
          label={labels.fiscalCode}
          hint={t('legalRep.hints.fiscalCode')}
          error={errors.fiscalCode?.message}
        >
          {(p) => (
            <Input
              {...p}
              autoComplete="off"
              className="max-w-64 uppercase"
              {...register('fiscalCode', {
                pattern: { value: /^\s*[A-Za-z0-9]{16}\s*$/, message: t('legalRep.errors.fiscalCode') },
              })}
            />
          )}
        </FormField>
        <FormField id={fieldId('birthDate')} label={labels.birthDate} error={errors.birthDate?.message}>
          {(p) => (
            <Input
              {...p}
              type="date"
              max={today}
              className="max-w-48"
              {...register('birthDate', {
                validate: (v) => !v || v < today || t('legalRep.errors.birthDate'),
              })}
            />
          )}
        </FormField>
        <FormField id={fieldId('birthPlace')} label={labels.birthPlace} error={errors.birthPlace?.message}>
          {(p) => <Input {...p} autoComplete="off" {...register('birthPlace', { maxLength: max(200) })} />}
        </FormField>
        <FormField
          id={fieldId('residence')}
          label={labels.residence}
          hint={t('legalRep.hints.residence')}
          error={errors.residence?.message}
        >
          {(p) => <Input {...p} autoComplete="off" {...register('residence', { maxLength: max(300) })} />}
        </FormField>
      </div>
      <div className="flex flex-wrap gap-3">
        <Button type="submit" size="lg" disabled={isSubmitting}>
          {t('legalRep.save')}
        </Button>
        <Button type="button" size="lg" variant="outline" onClick={onCancel}>
          {t('legalRep.cancel')}
        </Button>
      </div>
    </form>
  );
};
