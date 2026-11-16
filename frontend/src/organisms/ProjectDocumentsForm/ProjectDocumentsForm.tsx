import { useState } from 'react';
import { useForm, type FieldPath } from 'react-hook-form';
import { useTranslation } from 'react-i18next';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import type { ProjectDocuments } from '@/config/api/documentCatalogApi';
import { apiError } from '@/config/api/errors';
import { useUpdateProjectDocuments } from '@/config/hooks/useDocumentCatalogs';
import { useErrorSummaryFocus } from '@/hooks/useErrorSummaryFocus';
import { ErrorSummary, type SummaryError } from '@/molecules/ErrorSummary/ErrorSummary';
import { FormField } from '@/molecules/FormField/FormField';

type Values = { headerLine: string; noticeReference: string };

const FIELDS = ['headerLine', 'noticeReference'] as const;

const fieldId = (field: keyof Values): string => `project-documents-${field}`;

type ProjectDocumentsFormProps = {
  row: ProjectDocuments;
  onDone: () => void;
  onCancel: () => void;
};

/**
 * Intestazione e riferimento all'avviso dei documenti di un progetto (US-1011). Il riferimento parte dal testo in
 * vigore, anche quello predefinito: svuotato, torna il predefinito.
 */
export const ProjectDocumentsForm = ({ row, onDone, onCancel }: ProjectDocumentsFormProps) => {
  const { t } = useTranslation();
  const update = useUpdateProjectDocuments();
  const [formError, setFormError] = useState(false);
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting, submitCount },
  } = useForm<Values>({
    defaultValues: { headerLine: row.headerLine ?? '', noticeReference: row.noticeReference },
    mode: 'onSubmit',
    reValidateMode: 'onChange',
    shouldFocusError: false,
  });
  const { summaryRef, requestSummaryFocus } = useErrorSummaryFocus(submitCount, errors);
  const label = (field: keyof Values): string => t(`documentCatalogs.projectDocuments.fields.${field}`);
  const summary: SummaryError[] = FIELDS.flatMap((field) => {
    const message = errors[field]?.message;
    return message ? [{ fieldId: fieldId(field), message: `${label(field)}: ${message}` }] : [];
  });
  const max = (n: number) => ({ value: n, message: t('form.errors.maxLength', { max: n }) });

  const onValid = async (values: Values): Promise<void> => {
    setFormError(false);
    const text = (v: string): string | null => (v.trim() === '' ? null : v.trim());
    try {
      await update.mutateAsync({
        id: row.project.id,
        headerLine: text(values.headerLine),
        noticeReference: text(values.noticeReference),
      });
      onDone();
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
    <form noValidate onSubmit={handleSubmit(onValid, requestSummaryFocus)} className="space-y-6">
      {submitCount > 0 && <ErrorSummary ref={summaryRef} errors={summary} />}
      {formError && (
        <p
          role="alert"
          className="rounded-md border-2 border-destructive bg-card p-4 font-medium text-destructive"
        >
          {t('form.errors.generic')}
        </p>
      )}
      <div className="grid max-w-3xl gap-4">
        <FormField
          id={fieldId('headerLine')}
          label={label('headerLine')}
          hint={t('documentCatalogs.projectDocuments.hints.headerLine')}
          error={errors.headerLine?.message}
        >
          {(p) => <Input {...p} autoComplete="off" {...register('headerLine', { maxLength: max(300) })} />}
        </FormField>
        <FormField
          id={fieldId('noticeReference')}
          label={label('noticeReference')}
          hint={t('documentCatalogs.projectDocuments.hints.noticeReference')}
          error={errors.noticeReference?.message}
        >
          {(p) => <Textarea {...p} rows={5} {...register('noticeReference', { maxLength: max(1000) })} />}
        </FormField>
      </div>
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
