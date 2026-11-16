import { useState } from 'react';
import { useFieldArray, useForm, type FieldPath } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import { Plus, Trash2 } from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { PROMOTER_FIELDS, type ProjectDocuments, type PromoterField } from '@/config/api/documentCatalogApi';
import { apiError } from '@/config/api/errors';
import { useUpdateProjectDocuments } from '@/config/hooks/useDocumentCatalogs';
import { useErrorSummaryFocus } from '@/hooks/useErrorSummaryFocus';
import { useFocusAfterRender } from '@/hooks/useFocusAfterRender';
import { todayIso } from '@/lib/isoDate';
import { ErrorSummary, type SummaryError } from '@/molecules/ErrorSummary/ErrorSummary';
import { FormField } from '@/molecules/FormField/FormField';

type VariableRow = { name: string; value: string; description: string; saved: boolean };

type Values = {
  headerLine: string;
  noticeReference: string;
  promoter: Record<PromoterField, string>;
  variables: VariableRow[];
};

/** Lunghezza massima dei testi del promotore, come nel database (V44). */
const PROMOTER_MAX: Record<PromoterField, number | undefined> = {
  name: 300,
  address: 300,
  fiscalCode: 16,
  representative: 200,
  representativeRole: 200,
  representativeBirthPlace: 200,
  representativeBirthDate: undefined,
  phone: 50,
  email: 254,
  pec: 254,
  inailPosition: 100,
  rcInsurer: 200,
  rcPolicy: 100,
  rcExpiry: undefined,
};

const VARIABLE_NAME = /^[A-Z][A-Z0-9_]{0,49}$/;
const MAX_VARIABLES = 50;

const fieldId = (field: string): string => `project-documents-${field.replaceAll('.', '-')}`;

/** Gli errori del server usano «variables[0].name»; il modulo «variables.0.name». */
const formPath = (serverField: string): string => serverField.replace(/\[(\d+)]/g, '.$1');

type ProjectDocumentsFormProps = {
  row: ProjectDocuments;
  onDone: () => void;
  onCancel: () => void;
};

/**
 * Dati dei documenti di un progetto: intestazione e riferimento all'avviso (US-1011), dati del soggetto promotore e
 * variabili libere (US-1013). Il riferimento parte dal testo in vigore, anche quello predefinito: svuotato, torna il
 * predefinito. Il nome di una variabile salvata non si cambia.
 */
export const ProjectDocumentsForm = ({ row, onDone, onCancel }: ProjectDocumentsFormProps) => {
  const { t } = useTranslation();
  const update = useUpdateProjectDocuments();
  const [formError, setFormError] = useState(false);
  // errore del server sull'elenco delle variabili (una variabile usata da un modello): non è di un campo, e come errore
  // del modulo bloccherebbe i salvataggi successivi
  const [variablesError, setVariablesError] = useState<string | null>(null);
  const { ref: addRef, requestFocus: focusAdd } = useFocusAfterRender<HTMLButtonElement>();
  const {
    register,
    control,
    handleSubmit,
    setError,
    getValues,
    formState: { errors, isSubmitting, submitCount },
  } = useForm<Values>({
    defaultValues: {
      headerLine: row.headerLine ?? '',
      noticeReference: row.noticeReference,
      promoter: Object.fromEntries(PROMOTER_FIELDS.map((f) => [f, row.promoter?.[f] ?? ''])) as Record<
        PromoterField,
        string
      >,
      variables: (row.variables ?? []).map((v) => ({
        name: v.name,
        value: v.value,
        description: v.description ?? '',
        saved: true,
      })),
    },
    mode: 'onSubmit',
    reValidateMode: 'onChange',
    shouldFocusError: false,
  });
  const variableRows = useFieldArray({ control, name: 'variables' });
  const { summaryRef, requestSummaryFocus } = useErrorSummaryFocus(submitCount, errors);
  const max = (n: number) => ({ value: n, message: t('form.errors.maxLength', { max: n }) });

  const summary: SummaryError[] = [
    ...(['headerLine', 'noticeReference'] as const).flatMap((f) => {
      const message = errors[f]?.message;
      return message
        ? [
            {
              fieldId: fieldId(f),
              message: `${t(`documentCatalogs.projectDocuments.fields.${f}`)}: ${message}`,
            },
          ]
        : [];
    }),
    ...PROMOTER_FIELDS.flatMap((f) => {
      const message = errors.promoter?.[f]?.message;
      return message
        ? [
            {
              fieldId: fieldId(`promoter.${f}`),
              message: `${t(`documentCatalogs.projectDocuments.promoterFields.${f}`)}: ${message}`,
            },
          ]
        : [];
    }),
    ...variableRows.fields.flatMap((_, i) =>
      (['name', 'value', 'description'] as const).flatMap((f) => {
        const message = errors.variables?.[i]?.[f]?.message;
        return message
          ? [
              {
                fieldId: fieldId(`variables.${i}.${f}`),
                message: `${t(`documentCatalogs.projectDocuments.variableFields.${f}`, { n: i + 1 })}: ${message}`,
              },
            ]
          : [];
      }),
    ),
    ...(variablesError
      ? [
          {
            fieldId: fieldId('variables'),
            message: `${t('documentCatalogs.projectDocuments.sections.variables')}: ${variablesError}`,
          },
        ]
      : []),
  ];

  const onValid = async (values: Values): Promise<void> => {
    setFormError(false);
    setVariablesError(null);
    const text = (v: string): string | null => (v.trim() === '' ? null : v.trim());
    try {
      await update.mutateAsync({
        id: row.project.id,
        headerLine: text(values.headerLine),
        noticeReference: text(values.noticeReference),
        promoter: Object.fromEntries(PROMOTER_FIELDS.map((f) => [f, text(values.promoter[f])])) as Record<
          PromoterField,
          string | null
        >,
        variables: values.variables.map((v) => ({
          name: v.name,
          value: v.value.trim(),
          description: text(v.description),
        })),
      });
      onDone();
    } catch (error) {
      const fieldErrors = apiError(error)?.fieldErrors ?? [];
      const known = fieldErrors.filter(
        (fe) =>
          ['headerLine', 'noticeReference', 'variables'].includes(fe.field) ||
          fe.field.startsWith('promoter.') ||
          fe.field.startsWith('variables['),
      );
      if (known.length > 0) {
        known.forEach((fe) =>
          fe.field === 'variables'
            ? setVariablesError(fe.message)
            : setError(formPath(fe.field) as FieldPath<Values>, { type: 'server', message: fe.message }),
        );
      } else {
        setFormError(true);
      }
      requestSummaryFocus();
    }
  };

  const promoterField = (
    field: PromoterField,
    options: { type?: string; hint?: string; wide?: boolean } = {},
  ) => {
    const limit = PROMOTER_MAX[field];
    return (
      <div className={options.wide ? 'md:col-span-2' : undefined}>
        <FormField
          id={fieldId(`promoter.${field}`)}
          label={t(`documentCatalogs.projectDocuments.promoterFields.${field}`)}
          hint={options.hint}
          error={errors.promoter?.[field]?.message}
        >
          {(p) => (
            <Input
              {...p}
              type={options.type ?? 'text'}
              max={field === 'representativeBirthDate' ? todayIso() : undefined}
              autoComplete="off"
              {...register(`promoter.${field}`, { maxLength: limit === undefined ? undefined : max(limit) })}
            />
          )}
        </FormField>
      </div>
    );
  };

  // come il server: il nome ripetuto è un errore della variabile che viene dopo
  const uniqueName = (i: number) => (name: string) =>
    getValues('variables')
      .slice(0, i)
      .every((v) => v.name !== name) || t('documentCatalogs.projectDocuments.errors.variableDuplicate');

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
      <fieldset className="space-y-4">
        <legend className="font-semibold">{t('documentCatalogs.projectDocuments.sections.documents')}</legend>
        <div className="grid max-w-3xl gap-4">
          <FormField
            id={fieldId('headerLine')}
            label={t('documentCatalogs.projectDocuments.fields.headerLine')}
            hint={t('documentCatalogs.projectDocuments.hints.headerLine')}
            error={errors.headerLine?.message}
          >
            {(p) => <Input {...p} autoComplete="off" {...register('headerLine', { maxLength: max(300) })} />}
          </FormField>
          <FormField
            id={fieldId('noticeReference')}
            label={t('documentCatalogs.projectDocuments.fields.noticeReference')}
            hint={t('documentCatalogs.projectDocuments.hints.noticeReference')}
            error={errors.noticeReference?.message}
          >
            {(p) => <Textarea {...p} rows={5} {...register('noticeReference', { maxLength: max(1000) })} />}
          </FormField>
        </div>
      </fieldset>

      <fieldset className="space-y-4">
        <legend className="font-semibold">{t('documentCatalogs.projectDocuments.sections.promoter')}</legend>
        <p className="max-w-3xl text-sm text-muted-foreground">
          {t('documentCatalogs.projectDocuments.hints.promoter')}
        </p>
        <div className="grid max-w-3xl gap-4 md:grid-cols-2">
          {promoterField('name', { wide: true })}
          {promoterField('address', {
            wide: true,
            hint: t('documentCatalogs.projectDocuments.hints.address'),
          })}
          {promoterField('fiscalCode', { hint: t('documentCatalogs.projectDocuments.hints.fiscalCode') })}
          {promoterField('phone', { type: 'tel' })}
          {promoterField('email', { type: 'email' })}
          {promoterField('pec', { type: 'email' })}
        </div>
      </fieldset>

      <fieldset className="space-y-4">
        <legend className="font-semibold">
          {t('documentCatalogs.projectDocuments.sections.representative')}
        </legend>
        <p className="max-w-3xl text-sm text-muted-foreground">
          {t('documentCatalogs.projectDocuments.hints.privacy')}
        </p>
        <div className="grid max-w-3xl gap-4 md:grid-cols-2">
          {promoterField('representative')}
          {promoterField('representativeRole')}
          {promoterField('representativeBirthPlace')}
          {promoterField('representativeBirthDate', { type: 'date' })}
        </div>
      </fieldset>

      <fieldset className="space-y-4">
        <legend className="font-semibold">{t('documentCatalogs.projectDocuments.sections.insurance')}</legend>
        <div className="grid max-w-3xl gap-4 md:grid-cols-2">
          {promoterField('inailPosition')}
          {promoterField('rcInsurer')}
          {promoterField('rcPolicy')}
          {promoterField('rcExpiry', { type: 'date' })}
        </div>
      </fieldset>

      <fieldset id={fieldId('variables')} tabIndex={-1} className="space-y-4">
        <legend className="font-semibold">{t('documentCatalogs.projectDocuments.sections.variables')}</legend>
        <p className="max-w-3xl text-sm text-muted-foreground">
          {t('documentCatalogs.projectDocuments.hints.variables')}
        </p>
        {variablesError && <p className="max-w-3xl font-medium text-destructive">{variablesError}</p>}
        {variableRows.fields.length === 0 && <p>{t('documentCatalogs.projectDocuments.noVariablesYet')}</p>}
        {variableRows.fields.map((field, i) => (
          <fieldset key={field.id} className="max-w-3xl space-y-3 rounded-lg border border-border p-4">
            <legend className="px-1 text-sm font-semibold">
              {t('documentCatalogs.projectDocuments.variableLegend', { n: i + 1 })}
            </legend>
            <div className="grid gap-4 md:grid-cols-2">
              <FormField
                id={fieldId(`variables.${i}.name`)}
                label={t('documentCatalogs.projectDocuments.variableFields.name', { n: i + 1 })}
                hint={t('documentCatalogs.projectDocuments.hints.variableName')}
                required
                error={errors.variables?.[i]?.name?.message}
              >
                {(p) => (
                  <Input
                    {...p}
                    autoComplete="off"
                    spellCheck={false}
                    readOnly={field.saved}
                    className="font-mono uppercase read-only:bg-muted"
                    {...register(`variables.${i}.name`, {
                      setValueAs: (v: string) => v.trim().toUpperCase(),
                      required: t('form.errors.required'),
                      pattern: {
                        value: VARIABLE_NAME,
                        message: t('documentCatalogs.projectDocuments.errors.variableName'),
                      },
                      validate: uniqueName(i),
                    })}
                  />
                )}
              </FormField>
              <FormField
                id={fieldId(`variables.${i}.value`)}
                label={t('documentCatalogs.projectDocuments.variableFields.value', { n: i + 1 })}
                required
                error={errors.variables?.[i]?.value?.message}
              >
                {(p) => (
                  <Input
                    {...p}
                    autoComplete="off"
                    {...register(`variables.${i}.value`, {
                      validate: (v: string) => v.trim() !== '' || t('form.errors.required'),
                      maxLength: max(500),
                    })}
                  />
                )}
              </FormField>
              <div className="md:col-span-2">
                <FormField
                  id={fieldId(`variables.${i}.description`)}
                  label={t('documentCatalogs.projectDocuments.variableFields.description', { n: i + 1 })}
                  error={errors.variables?.[i]?.description?.message}
                >
                  {(p) => (
                    <Input
                      {...p}
                      autoComplete="off"
                      {...register(`variables.${i}.description`, { maxLength: max(300) })}
                    />
                  )}
                </FormField>
              </div>
            </div>
            <Button
              type="button"
              variant="outline"
              onClick={() => {
                variableRows.remove(i);
                // il pulsante sparisce con la riga: il focus va su «Aggiungi una variabile»
                focusAdd();
              }}
            >
              <Trash2 aria-hidden="true" />
              {t('documentCatalogs.projectDocuments.removeVariable', { n: i + 1 })}
            </Button>
          </fieldset>
        ))}
        <Button
          ref={addRef}
          type="button"
          variant="outline"
          disabled={variableRows.fields.length >= MAX_VARIABLES}
          onClick={() => variableRows.append({ name: '', value: '', description: '', saved: false })}
        >
          <Plus aria-hidden="true" />
          {t('documentCatalogs.projectDocuments.addVariable')}
        </Button>
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
