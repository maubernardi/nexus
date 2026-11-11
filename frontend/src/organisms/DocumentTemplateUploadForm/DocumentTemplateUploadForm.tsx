import { useEffect, useState } from 'react';
import { Upload } from 'lucide-react';
import { useForm, useWatch } from 'react-hook-form';
import { useTranslation } from 'react-i18next';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { NativeSelect } from '@/components/ui/native-select';
import { TEMPLATE_DOCUMENTS, type TemplateDocument } from '@/config/api/documentTemplateApi';
import { apiError } from '@/config/api/errors';
import type { ReferenceItem } from '@/config/api/referenceApi';
import { useCatalog } from '@/config/hooks/useCatalogs';
import { useProjectInternshipTypes } from '@/config/hooks/useDocumentCatalogs';
import { useUploadDocumentTemplate } from '@/config/hooks/useDocumentTemplates';
import { useActiveCareAgencies } from '@/config/hooks/useReferenceData';
import { useErrorSummaryFocus } from '@/hooks/useErrorSummaryFocus';
import { ErrorSummary, type SummaryError } from '@/molecules/ErrorSummary/ErrorSummary';
import { FormField } from '@/molecules/FormField/FormField';

type UploadValues = {
  document: TemplateDocument | '';
  projectId: string;
  internshipTypeId: string;
  careAgencyId: string;
  name: string;
  file: FileList | null;
};

const EMPTY: UploadValues = {
  document: '',
  projectId: '',
  internshipTypeId: '',
  careAgencyId: '',
  name: '',
  file: null,
};

const FIELDS = [
  'document',
  'projectId',
  'internshipTypeId',
  'careAgencyId',
  'name',
  'file',
] as const satisfies readonly (keyof UploadValues)[];

const MAX_NAME = 200;
const fieldId = (name: keyof UploadValues): string => `template-${name}`;

/**
 * Caricamento di un modello Word (US-1010): documento, progetto, tipo di tirocinio tra quelli ammessi dal progetto,
 * ente facoltativo, nome e file. Il server controlla il file e i segnaposto: i suoi errori vanno sul campo.
 */
export const DocumentTemplateUploadForm = () => {
  const { t } = useTranslation();
  const projects = useProjectInternshipTypes();
  const types = useCatalog('internship-types');
  const agencies = useActiveCareAgencies();
  const upload = useUploadDocumentTemplate();
  const [done, setDone] = useState<string | null>(null);
  const [failure, setFailure] = useState<string | null>(null);
  const {
    register,
    handleSubmit,
    control,
    setError,
    setValue,
    getValues,
    reset,
    formState: { errors, submitCount, isSubmitting },
  } = useForm<UploadValues>({
    defaultValues: EMPTY,
    mode: 'onSubmit',
    reValidateMode: 'onChange',
    shouldFocusError: false,
  });
  const { summaryRef, requestSummaryFocus } = useErrorSummaryFocus(submitCount, errors);
  const projectId = useWatch({ control, name: 'projectId' });

  const activeProjects = (projects.data ?? []).filter((p) => p.active);
  const allowed = activeProjects.find((p) => p.project.id === projectId)?.internshipTypes ?? [];
  // nessun tipo indicato per il progetto = tutti i tipi attivi (US-115)
  const typeOptions: ReferenceItem[] =
    projectId === '' ? [] : allowed.length > 0 ? allowed : (types.data ?? []).filter((type) => type.active);

  // cambiato il progetto, il tipo scelto può non essere più ammesso
  const typeIds = typeOptions.map((type) => type.id).join(' ');
  useEffect(() => {
    const current = getValues('internshipTypeId');
    if (current && !typeIds.split(' ').includes(current)) {
      setValue('internshipTypeId', '');
    }
  }, [getValues, setValue, typeIds]);

  const required = { value: true, message: t('form.errors.required') };
  const summary: SummaryError[] = FIELDS.flatMap((name) => {
    const message = errors[name]?.message;
    return message
      ? [{ fieldId: fieldId(name), message: `${t(`documentTemplates.fields.${name}`)}: ${message}` }]
      : [];
  });

  const onValid = async (v: UploadValues): Promise<void> => {
    setDone(null);
    setFailure(null);
    const file = v.file?.[0];
    if (!file || v.document === '') return;
    try {
      const saved = await upload.mutateAsync({
        file,
        document: v.document,
        projectId: v.projectId,
        internshipTypeId: v.internshipTypeId,
        careAgencyId: v.careAgencyId,
        name: v.name.trim(),
      });
      reset(EMPTY);
      setDone(t('documentTemplates.upload.done', { name: saved.name }));
    } catch (error) {
      const body = apiError(error);
      const fieldErrors = (body?.fieldErrors ?? []).filter(
        (fe): fe is typeof fe & { field: keyof UploadValues } =>
          (FIELDS as readonly string[]).includes(fe.field),
      );
      if (fieldErrors.length > 0) {
        fieldErrors.forEach((fe) => setError(fe.field, { type: 'server', message: fe.message }));
        requestSummaryFocus();
        return;
      }
      setFailure(body?.message ?? t('documentTemplates.upload.error'));
    }
  };

  return (
    <form noValidate onSubmit={handleSubmit(onValid, requestSummaryFocus)} className="space-y-4">
      {submitCount > 0 && <ErrorSummary ref={summaryRef} errors={summary} />}
      <div className="grid gap-4 sm:grid-cols-2">
        <FormField
          id={fieldId('document')}
          label={t('documentTemplates.fields.document')}
          required
          error={errors.document?.message}
        >
          {(p) => (
            <NativeSelect {...p} {...register('document', { required })}>
              <option value="">{t('form.select')}</option>
              {TEMPLATE_DOCUMENTS.map((d) => (
                <option key={d} value={d}>
                  {t(`documentTemplates.documents.${d}`)}
                </option>
              ))}
            </NativeSelect>
          )}
        </FormField>
        <FormField
          id={fieldId('projectId')}
          label={t('documentTemplates.fields.projectId')}
          required
          error={errors.projectId?.message}
        >
          {(p) => (
            <NativeSelect {...p} {...register('projectId', { required })}>
              <option value="">{t('form.select')}</option>
              {activeProjects.map((item) => (
                <option key={item.project.id} value={item.project.id}>
                  {item.project.name}
                </option>
              ))}
            </NativeSelect>
          )}
        </FormField>
        <FormField
          id={fieldId('internshipTypeId')}
          label={t('documentTemplates.fields.internshipTypeId')}
          required
          hint={t('documentTemplates.hints.internshipTypeId')}
          error={errors.internshipTypeId?.message}
        >
          {(p) => (
            <NativeSelect {...p} {...register('internshipTypeId', { required })}>
              <option value="">
                {projectId === '' ? t('documentTemplates.chooseProjectFirst') : t('form.select')}
              </option>
              {typeOptions.map((type) => (
                <option key={type.id} value={type.id}>
                  {type.name}
                </option>
              ))}
            </NativeSelect>
          )}
        </FormField>
        <FormField
          id={fieldId('careAgencyId')}
          label={t('documentTemplates.fields.careAgencyId')}
          hint={t('documentTemplates.hints.careAgencyId')}
          error={errors.careAgencyId?.message}
        >
          {(p) => (
            <NativeSelect {...p} {...register('careAgencyId')}>
              <option value="">{t('documentTemplates.allAgencies')}</option>
              {agencies.data?.map((agency) => (
                <option key={agency.id} value={agency.id}>
                  {agency.name}
                </option>
              ))}
            </NativeSelect>
          )}
        </FormField>
      </div>
      <FormField
        id={fieldId('name')}
        label={t('documentTemplates.fields.name')}
        required
        hint={t('documentTemplates.hints.name')}
        error={errors.name?.message}
      >
        {(p) => (
          <Input
            {...p}
            autoComplete="off"
            {...register('name', {
              validate: (v) => v.trim() !== '' || t('form.errors.required'),
              maxLength: { value: MAX_NAME, message: t('form.errors.maxLength', { max: MAX_NAME }) },
            })}
          />
        )}
      </FormField>
      <FormField
        id={fieldId('file')}
        label={t('documentTemplates.fields.file')}
        required
        hint={t('documentTemplates.hints.file')}
        error={errors.file?.message}
      >
        {(p) => (
          <input
            {...p}
            type="file"
            accept=".docx,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            className="min-h-11 max-w-full text-sm file:mr-3 file:min-h-11 file:rounded-md file:border file:border-border file:bg-background file:px-3 file:font-medium"
            {...register('file', {
              validate: (files) => (files !== null && files.length > 0) || t('form.errors.required'),
            })}
          />
        )}
      </FormField>
      <Button type="submit" size="lg" disabled={isSubmitting}>
        <Upload aria-hidden="true" />
        {isSubmitting ? t('documentTemplates.upload.uploading') : t('documentTemplates.upload.submit')}
      </Button>
      <p
        role="status"
        className={failure ? 'font-medium text-destructive' : 'font-medium text-muted-foreground'}
      >
        {failure ?? done}
      </p>
    </form>
  );
};
