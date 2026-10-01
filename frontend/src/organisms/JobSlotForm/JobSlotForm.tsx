import { useState } from 'react';
import { useForm, type FieldPath } from 'react-hook-form';
import { useTranslation } from 'react-i18next';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { NativeSelect } from '@/components/ui/native-select';
import { Textarea } from '@/components/ui/textarea';
import { apiError } from '@/config/api/errors';
import type { JobSlot, JobSlotForm as JobSlotPayload } from '@/config/api/jobSlotApi';
import type { ReferenceItem } from '@/config/api/referenceApi';
import { useJobCategories, useZones } from '@/config/hooks/useReferenceData';
import { useErrorSummaryFocus } from '@/hooks/useErrorSummaryFocus';
import { ErrorSummary, type SummaryError } from '@/molecules/ErrorSummary/ErrorSummary';
import { FormField } from '@/molecules/FormField/FormField';

type Values = { title: string; jobCategoryId: string; zoneId: string; description: string };

const FIELDS = ['title', 'jobCategoryId', 'zoneId', 'description'] as const;
const fieldId = (field: keyof Values): string => `jobslot-${field}`;

/** Voci attive più quella già assegnata, anche se nel frattempo disattivata (resta selezionabile così com'è). */
const withCurrent = (items: ReferenceItem[] | undefined, current?: ReferenceItem): ReferenceItem[] => {
  const list = items ?? [];
  return current && !list.some((i) => i.id === current.id) ? [current, ...list] : list;
};

type JobSlotFormProps = {
  slot?: JobSlot;
  onSubmit: (payload: JobSlotPayload) => Promise<JobSlot>;
  onSuccess: (slot: JobSlot) => void;
  onCancel: () => void;
  onConflict: () => void;
};

/** Titolo, tipologia, zona e descrizione di una mansione (US-502). Lo stato non si modifica a mano. */
export const JobSlotForm = ({ slot, onSubmit, onSuccess, onCancel, onConflict }: JobSlotFormProps) => {
  const { t } = useTranslation();
  const categories = useJobCategories();
  const zones = useZones();
  const [genericError, setGenericError] = useState(false);
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting, submitCount },
  } = useForm<Values>({
    defaultValues: {
      title: slot?.title ?? '',
      jobCategoryId: slot?.jobCategory.id ?? '',
      zoneId: slot?.zone.id ?? '',
      description: slot?.description ?? '',
    },
    mode: 'onSubmit',
    reValidateMode: 'onChange',
    // il focus va al riepilogo degli errori (non al primo campo): lo gestisce useErrorSummaryFocus
    shouldFocusError: false,
  });
  const { summaryRef, requestSummaryFocus } = useErrorSummaryFocus(submitCount, errors);

  const labels: Record<keyof Values, string> = {
    title: t('jobSlot.fields.title'),
    jobCategoryId: t('jobSlot.fields.jobCategory'),
    zoneId: t('jobSlot.fields.zone'),
    description: t('jobSlot.fields.description'),
  };
  const summary: SummaryError[] = FIELDS.flatMap((field) => {
    const message = errors[field]?.message;
    return message ? [{ fieldId: fieldId(field), message: `${labels[field]}: ${message}` }] : [];
  });
  const required = { value: true, message: t('form.errors.required') };

  const onValid = async (v: Values): Promise<void> => {
    setGenericError(false);
    try {
      onSuccess(
        await onSubmit({
          title: v.title.trim(),
          jobCategoryId: v.jobCategoryId,
          zoneId: v.zoneId,
          description: v.description.trim() || null,
          version: slot?.version,
        }),
      );
    } catch (error) {
      const body = apiError(error);
      if (body?.status === 409) {
        onConflict();
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
          id={fieldId('title')}
          label={labels.title}
          required
          error={errors.title?.message}
          className="md:col-span-2"
        >
          {(p) => (
            <Input
              {...p}
              autoComplete="off"
              {...register('title', {
                required,
                maxLength: { value: 200, message: t('form.errors.maxLength', { max: 200 }) },
                validate: (v) => v.trim() !== '' || t('form.errors.required'),
              })}
            />
          )}
        </FormField>
        <FormField
          id={fieldId('jobCategoryId')}
          label={labels.jobCategoryId}
          required
          error={errors.jobCategoryId?.message}
        >
          {(p) => (
            <NativeSelect {...p} disabled={!categories.data} {...register('jobCategoryId', { required })}>
              <option value="">{t('form.select')}</option>
              {withCurrent(categories.data, slot?.jobCategory).map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </NativeSelect>
          )}
        </FormField>
        <FormField id={fieldId('zoneId')} label={labels.zoneId} required error={errors.zoneId?.message}>
          {(p) => (
            <NativeSelect {...p} disabled={!zones.data} {...register('zoneId', { required })}>
              <option value="">{t('form.select')}</option>
              {withCurrent(zones.data, slot?.zone).map((z) => (
                <option key={z.id} value={z.id}>
                  {z.name}
                </option>
              ))}
            </NativeSelect>
          )}
        </FormField>
        <FormField
          id={fieldId('description')}
          label={labels.description}
          error={errors.description?.message}
          className="md:col-span-2"
        >
          {(p) => (
            <Textarea
              {...p}
              rows={3}
              {...register('description', {
                maxLength: { value: 4000, message: t('form.errors.maxLength', { max: 4000 }) },
              })}
            />
          )}
        </FormField>
      </div>
      <div className="flex flex-wrap gap-3">
        <Button type="submit" size="lg" disabled={isSubmitting}>
          {isSubmitting ? t('company.saving') : t(slot ? 'jobSlot.save' : 'jobSlot.create')}
        </Button>
        <Button type="button" size="lg" variant="outline" onClick={onCancel}>
          {t('jobSlot.cancel')}
        </Button>
      </div>
    </form>
  );
};
