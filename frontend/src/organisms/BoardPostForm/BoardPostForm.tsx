import { useState } from 'react';
import { useForm, type FieldPath } from 'react-hook-form';
import { useTranslation } from 'react-i18next';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { NativeSelect } from '@/components/ui/native-select';
import { Textarea } from '@/components/ui/textarea';
import type { BoardPost, BoardPostForm as BoardPostPayload } from '@/config/api/boardPostApi';
import { apiError } from '@/config/api/errors';
import type { ReferenceItem } from '@/config/api/referenceApi';
import { useProjects } from '@/config/hooks/useReferenceData';
import { useErrorSummaryFocus } from '@/hooks/useErrorSummaryFocus';
import { ErrorSummary, type SummaryError } from '@/molecules/ErrorSummary/ErrorSummary';
import { FormField } from '@/molecules/FormField/FormField';

type Values = {
  title: string;
  projectId: string;
  ageMin: string;
  ageMax: string;
  weeklyHours: string;
  durationMonths: string;
  notes: string;
};

const FIELDS = ['title', 'projectId', 'ageMin', 'ageMax', 'weeklyHours', 'durationMonths', 'notes'] as const;
type NumberField = 'ageMin' | 'ageMax' | 'weeklyHours' | 'durationMonths';
// stessi limiti dei vincoli del database
const RANGES: Record<NumberField, { min: number; max: number }> = {
  ageMin: { min: 14, max: 99 },
  ageMax: { min: 14, max: 99 },
  weeklyHours: { min: 1, max: 60 },
  durationMonths: { min: 1, max: 36 },
};
const fieldId = (field: keyof Values): string => `board-post-${field}`;
// l'API omette i campi vuoti: null e assente valgono uguale
const toText = (n: number | null | undefined): string => (typeof n === 'number' ? String(n) : '');
const toNumber = (v: string): number | null => (v.trim() === '' ? null : Number(v.trim()));

/** Progetti attivi più quello già assegnato, anche se nel frattempo disattivato. */
const withCurrent = (items: ReferenceItem[] | undefined, current: ReferenceItem | null): ReferenceItem[] => {
  const list = items ?? [];
  return current && !list.some((i) => i.id === current.id) ? [current, ...list] : list;
};

type BoardPostFormProps = {
  post: BoardPost;
  onSubmit: (payload: BoardPostPayload) => Promise<BoardPost>;
  onSuccess: (post: BoardPost) => void;
  onConflict: (reason: string) => void;
};

/**
 * Dati modificabili di un annuncio (US-902). Il progetto si sceglie solo nella bozza: dopo la pubblicazione un
 * annuncio riservato diventa visibile a tutti solo con lo sblocco dopo 7 giorni (US-903).
 */
export const BoardPostForm = ({ post, onSubmit, onSuccess, onConflict }: BoardPostFormProps) => {
  const { t } = useTranslation();
  const isDraft = post.status === 'DRAFT';
  const projects = useProjects(isDraft);
  const [genericError, setGenericError] = useState(false);
  const {
    register,
    handleSubmit,
    setError,
    getValues,
    formState: { errors, isSubmitting, submitCount },
  } = useForm<Values>({
    defaultValues: {
      title: post.title,
      projectId: post.project?.id ?? '',
      ageMin: toText(post.ageMin),
      ageMax: toText(post.ageMax),
      weeklyHours: toText(post.weeklyHours),
      durationMonths: toText(post.durationMonths),
      notes: post.notes ?? '',
    },
    mode: 'onSubmit',
    reValidateMode: 'onChange',
    shouldFocusError: false,
  });
  const { summaryRef, requestSummaryFocus } = useErrorSummaryFocus(submitCount, errors);

  const labels: Record<keyof Values, string> = {
    title: t('board.form.title'),
    projectId: t('board.form.project'),
    ageMin: t('board.form.ageMin'),
    ageMax: t('board.form.ageMax'),
    weeklyHours: t('board.form.weeklyHours'),
    durationMonths: t('board.form.durationMonths'),
    notes: t('board.form.notes'),
  };
  const summary: SummaryError[] = FIELDS.flatMap((field) => {
    const message = errors[field]?.message;
    return message ? [{ fieldId: fieldId(field), message: `${labels[field]}: ${message}` }] : [];
  });

  const numberRules = (field: NumberField) => {
    const { min, max } = RANGES[field];
    return {
      validate: (v: string) => {
        if (v.trim() === '') return true;
        const n = Number(v.trim());
        return (/^\d+$/.test(v.trim()) && n >= min && n <= max) || t('board.form.range', { min, max });
      },
    };
  };

  const onValid = async (v: Values): Promise<void> => {
    setGenericError(false);
    try {
      onSuccess(
        await onSubmit({
          title: v.title.trim(),
          projectId: isDraft ? v.projectId || null : (post.project?.id ?? null),
          ageMin: toNumber(v.ageMin),
          ageMax: toNumber(v.ageMax),
          weeklyHours: toNumber(v.weeklyHours),
          durationMonths: toNumber(v.durationMonths),
          notes: v.notes.trim() || null,
          version: post.version,
        }),
      );
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

  const numberField = (field: NumberField, hint?: string) => (
    <FormField id={fieldId(field)} label={labels[field]} hint={hint} error={errors[field]?.message}>
      {(p) => (
        <Input
          {...p}
          inputMode="numeric"
          autoComplete="off"
          className="max-w-32"
          {...register(field, numberRules(field))}
        />
      )}
    </FormField>
  );

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
          hint={t('board.form.titleHint')}
          required
          error={errors.title?.message}
          className="md:col-span-2"
        >
          {(p) => (
            <Input
              {...p}
              autoComplete="off"
              {...register('title', {
                maxLength: { value: 200, message: t('form.errors.maxLength', { max: 200 }) },
                validate: (v) => v.trim() !== '' || t('form.errors.required'),
              })}
            />
          )}
        </FormField>
        {isDraft ? (
          <FormField
            id={fieldId('projectId')}
            label={labels.projectId}
            hint={t('board.form.projectHint')}
            error={errors.projectId?.message}
            className="md:col-span-2"
          >
            {(p) => (
              <NativeSelect {...p} disabled={!projects.data} {...register('projectId')}>
                <option value="">{t('board.allProjects')}</option>
                {withCurrent(projects.data, post.project).map((pr) => (
                  <option key={pr.id} value={pr.id}>
                    {pr.name}
                  </option>
                ))}
              </NativeSelect>
            )}
          </FormField>
        ) : (
          <div className="space-y-1 md:col-span-2">
            <p className="text-sm font-medium">{labels.projectId}</p>
            <p>{post.project?.name ?? t('board.allProjects')}</p>
            <p className="text-sm text-muted-foreground">{t('board.form.projectLocked')}</p>
          </div>
        )}
        {numberField('ageMin', t('board.form.ageHint'))}
        <FormField
          id={fieldId('ageMax')}
          label={labels.ageMax}
          hint={t('board.form.ageHint')}
          error={errors.ageMax?.message}
        >
          {(p) => (
            <Input
              {...p}
              inputMode="numeric"
              autoComplete="off"
              className="max-w-32"
              {...register('ageMax', {
                validate: (v) => {
                  const range = numberRules('ageMax').validate(v);
                  if (range !== true) return range;
                  const min = toNumber(getValues('ageMin'));
                  const max = toNumber(v);
                  return min === null || max === null || min <= max || t('board.form.ageOrder');
                },
              })}
            />
          )}
        </FormField>
        {numberField('weeklyHours')}
        {numberField('durationMonths')}
        <FormField
          id={fieldId('notes')}
          label={labels.notes}
          hint={t('board.form.notesHint')}
          error={errors.notes?.message}
          className="md:col-span-2"
        >
          {(p) => (
            <Textarea
              {...p}
              rows={3}
              {...register('notes', {
                maxLength: { value: 4000, message: t('form.errors.maxLength', { max: 4000 }) },
              })}
            />
          )}
        </FormField>
      </div>
      <Button type="submit" size="lg" disabled={isSubmitting}>
        {isSubmitting ? t('board.form.saving') : t('board.form.save')}
      </Button>
    </form>
  );
};
