import { useEffect, useRef, useState } from 'react';
import { useForm, useWatch, type FieldPath, type UseFormRegisterReturn } from 'react-hook-form';
import { useTranslation } from 'react-i18next';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { NativeSelect } from '@/components/ui/native-select';
import { apiError } from '@/config/api/errors';
import type { ReferenceItem } from '@/config/api/referenceApi';
import type { TicketDetail } from '@/config/api/ticketApi';
import {
  useJobCategories,
  useMyProjects,
  useProjectCompetenceSites,
  useProjectInternshipTypes,
} from '@/config/hooks/useReferenceData';
import { useCorrectTicketRequest } from '@/config/hooks/useTickets';
import { useErrorSummaryFocus } from '@/hooks/useErrorSummaryFocus';
import { useFocusAfterRender } from '@/hooks/useFocusAfterRender';
import { ErrorSummary, type SummaryError } from '@/molecules/ErrorSummary/ErrorSummary';
import { FormField } from '@/molecules/FormField/FormField';

type Feedback = { kind: 'status' | 'alert'; text: string };

type CorrectionValues = {
  projectId: string;
  competenceSiteId: string;
  internshipTypeId: string;
  jobCategoryId: string;
  jobFreeText: string;
};

const FIELDS = ['projectId', 'competenceSiteId', 'internshipTypeId', 'jobCategoryId', 'jobFreeText'] as const;

const fieldId = (field: keyof CorrectionValues): string => `request-correction-${field}`;

/** L'elenco con la voce già scelta, anche se non c'è più (progetto lasciato, tipologia disattivata). */
const withCurrent = (items: ReferenceItem[], current: ReferenceItem | null | undefined): ReferenceItem[] =>
  current && !items.some((item) => item.id === current.id) ? [current, ...items] : items;

/**
 * Correzione di progetto e mansione richiesta (US-309): solo il Tutor della segnalazione, fino alla proposta
 * all'azienda (PO, 28/11). Cambiando progetto si scelgono di nuovo sede di competenza e tipo di tirocinio; se la nuova
 * tipologia aspetta l'approvazione si corregge il testo. Nella segnalazione riaperta si corregge solo la mansione, per le
 * nuove proposte (PO, 28/11). La correzione finisce nella cronologia.
 */
export const TicketRequestCorrection = ({ ticket }: { ticket: TicketDetail }) => {
  const { t } = useTranslation();
  const onlyJob = !ticket.canCorrectProject;
  const [open, setOpen] = useState(false);
  const [feedback, setFeedback] = useState<Feedback | null>(null);
  const feedbackRef = useRef<HTMLParagraphElement>(null);
  // il pulsante torna con la chiusura del modulo: il focus ci va dopo il render
  const { ref: toggleRef, requestFocus: focusToggle } = useFocusAfterRender<HTMLButtonElement>();

  useEffect(() => {
    if (feedback) feedbackRef.current?.focus();
  }, [feedback]);

  return (
    <div className="space-y-3">
      {feedback && (
        <p
          ref={feedbackRef}
          role={feedback.kind}
          tabIndex={-1}
          className={
            feedback.kind === 'alert'
              ? 'rounded-md border-2 border-destructive p-3 font-medium text-destructive'
              : 'rounded-md border-l-4 border-primary bg-background p-3 font-medium'
          }
        >
          {feedback.text}
        </p>
      )}
      {open ? (
        <CorrectionForm
          ticket={ticket}
          onSaved={(saved) => {
            setOpen(false);
            const job = saved.requestedJobCategory?.name ?? saved.requestedJobFreeText ?? '';
            setFeedback({
              kind: 'status',
              text: onlyJob
                ? t('work.requestCorrection.savedJob', { job })
                : t('work.requestCorrection.saved', { project: saved.project.name, job }),
            });
          }}
          onRefused={(text) => {
            setOpen(false);
            setFeedback({ kind: 'alert', text });
          }}
          onCancel={() => {
            setOpen(false);
            focusToggle();
          }}
        />
      ) : (
        <Button
          ref={toggleRef}
          type="button"
          variant="outline"
          size="sm"
          onClick={() => {
            setFeedback(null);
            setOpen(true);
          }}
        >
          {t(onlyJob ? 'work.requestCorrection.changeJob' : 'work.requestCorrection.change')}
        </Button>
      )}
    </div>
  );
};

type CorrectionFormProps = {
  ticket: TicketDetail;
  onSaved: (saved: TicketDetail) => void;
  /** Correzione non più possibile (proposta arrivata nel frattempo, versione superata): il motivo del server. */
  onRefused: (message: string) => void;
  onCancel: () => void;
};

/** Il modulo compare con progetti e tipologie già caricati: le scelte attuali sono subito selezionate. */
const CorrectionForm = (props: CorrectionFormProps) => {
  const { t } = useTranslation();
  const projects = useMyProjects();
  const categories = useJobCategories();
  if (projects.isError || categories.isError) {
    return (
      <div role="alert" className="space-y-3 rounded-md border-2 border-destructive p-3">
        <p className="font-medium text-destructive">{t('ticket.loadError')}</p>
        <div className="flex flex-wrap gap-3">
          <Button
            type="button"
            variant="outline"
            onClick={() => {
              void projects.refetch();
              void categories.refetch();
            }}
          >
            {t('ticket.retry')}
          </Button>
          <Button type="button" variant="outline" onClick={props.onCancel}>
            {t('work.requestCorrection.cancel')}
          </Button>
        </div>
      </div>
    );
  }
  if (!projects.data || !categories.data) {
    return <p role="status">{t('ticket.loading')}</p>;
  }
  return <CorrectionFields {...props} projects={projects.data} categories={categories.data} />;
};

type CorrectionFieldsProps = CorrectionFormProps & { projects: ReferenceItem[]; categories: ReferenceItem[] };

const CorrectionFields = ({
  ticket,
  projects,
  categories,
  onSaved,
  onRefused,
  onCancel,
}: CorrectionFieldsProps) => {
  const { t } = useTranslation();
  const correct = useCorrectTicketRequest();
  const [genericError, setGenericError] = useState(false);
  // la versione dell'apertura del modulo: se intanto la segnalazione cambia, il server lo dice (niente sovrascritture)
  const [version] = useState(ticket.version);
  // in attesa dell'approvazione della nuova tipologia (US-302) si corregge il testo, non si sceglie dal catalogo
  const freeText = !ticket.requestedJobCategory;
  // riaperta: il progetto resta, si corregge la mansione (PO, 28/11)
  const onlyJob = !ticket.canCorrectProject;
  const currentTypeId = ticket.internshipType?.id ?? '';
  const {
    register,
    handleSubmit,
    setError,
    setValue,
    control,
    formState: { errors, isSubmitting, submitCount },
  } = useForm<CorrectionValues>({
    defaultValues: {
      projectId: ticket.project.id,
      competenceSiteId: ticket.competenceSite?.id ?? '',
      internshipTypeId: currentTypeId,
      jobCategoryId: ticket.requestedJobCategory?.id ?? '',
      jobFreeText: ticket.requestedJobFreeText ?? '',
    },
    mode: 'onSubmit',
    reValidateMode: 'onChange',
    shouldFocusError: false,
  });
  const { summaryRef, requestSummaryFocus } = useErrorSummaryFocus(submitCount, errors);
  // il focus va al primo campo: il progetto, oppure la mansione se il progetto resta
  const firstFieldRef = useRef<HTMLElement | null>(null);
  // la registrazione del campo, con il riferimento anche per il focus iniziale
  const withFocus = <R extends UseFormRegisterReturn>(registration: R, first: boolean): R => ({
    ...registration,
    ref: (element: HTMLElement | null) => {
      registration.ref(element);
      if (first) firstFieldRef.current = element;
    },
  });

  useEffect(() => {
    firstFieldRef.current?.focus();
  }, []);

  const projectId = useWatch({ control, name: 'projectId' });
  // sede e tipo si scelgono di nuovo solo se cambia il progetto
  const projectChanged = projectId !== ticket.project.id;
  const sites = useProjectCompetenceSites(projectChanged ? projectId : '');
  const siteOptions = projectChanged ? (sites.data ?? []) : [];
  const onlySiteId = siteOptions.length === 1 ? siteOptions[0].id : '';
  const types = useProjectInternshipTypes(projectChanged ? projectId : '');
  const typeOptions = projectChanged ? (types.data ?? []) : [];
  // il tipo della segnalazione resta, se il progetto nuovo lo ammette
  const keptTypeId = typeOptions.some((type) => type.id === currentTypeId)
    ? currentTypeId
    : typeOptions.length === 1
      ? typeOptions[0].id
      : '';
  useEffect(() => {
    if (projectChanged) setValue('competenceSiteId', onlySiteId);
  }, [projectId, projectChanged, onlySiteId, setValue]);
  useEffect(() => {
    if (projectChanged) setValue('internshipTypeId', keptTypeId);
  }, [projectId, projectChanged, keptTypeId, setValue]);

  const required = { value: true, message: t('form.errors.required') };
  const labels: Record<(typeof FIELDS)[number], string> = {
    projectId: t('ticket.fields.project'),
    competenceSiteId: t('ticket.fields.competenceSite'),
    internshipTypeId: t('ticket.fields.internshipType'),
    jobCategoryId: t('ticket.fields.jobCategory'),
    jobFreeText: t('ticket.fields.jobFreeText'),
  };
  const summary: SummaryError[] = FIELDS.flatMap((field) => {
    const message = errors[field]?.message;
    return message ? [{ fieldId: fieldId(field), message: `${labels[field]}: ${message}` }] : [];
  });

  const onValid = async (values: CorrectionValues): Promise<void> => {
    setGenericError(false);
    try {
      onSaved(
        await correct.mutateAsync({
          id: ticket.id,
          form: {
            version,
            projectId: onlyJob ? ticket.project.id : values.projectId,
            jobCategoryId: freeText ? null : values.jobCategoryId,
            jobFreeText: freeText ? values.jobFreeText.trim() : null,
            competenceSiteId: siteOptions.length > 0 ? values.competenceSiteId : null,
            internshipTypeId: projectChanged ? values.internshipTypeId : null,
          },
        }),
      );
    } catch (error) {
      const body = apiError(error);
      if (body?.status === 409 && body.message) {
        onRefused(body.message);
        return;
      }
      const fieldErrors = (body?.fieldErrors ?? []).filter((fe) =>
        (FIELDS as readonly string[]).includes(fe.field),
      );
      if (fieldErrors.length > 0) {
        fieldErrors.forEach((fe) =>
          setError(fe.field as FieldPath<CorrectionValues>, { type: 'server', message: fe.message }),
        );
      } else {
        setGenericError(true);
      }
      requestSummaryFocus();
    }
  };

  return (
    <form
      noValidate
      aria-label={t(onlyJob ? 'work.requestCorrection.formJob' : 'work.requestCorrection.form')}
      onSubmit={handleSubmit(onValid, requestSummaryFocus)}
      className="space-y-4 rounded-md border border-border p-4"
    >
      <p className="text-sm text-muted-foreground">
        {onlyJob
          ? t('work.requestCorrection.hintReopened', { project: ticket.project.name })
          : t('work.requestCorrection.hint')}
      </p>
      {submitCount > 0 && <ErrorSummary ref={summaryRef} errors={summary} />}
      {genericError && (
        <p role="alert" className="rounded-md border-2 border-destructive p-3 font-medium text-destructive">
          {t('form.errors.generic')}
        </p>
      )}
      <div className="grid max-w-md gap-4">
        {!onlyJob && (
          <FormField
            id={fieldId('projectId')}
            label={labels.projectId}
            hint={t('work.requestCorrection.projectHint')}
            required
            error={errors.projectId?.message}
          >
            {(p) => (
              <NativeSelect {...p} {...withFocus(register('projectId', { required }), true)}>
                {withCurrent(projects, ticket.project).map((project) => (
                  <option key={project.id} value={project.id}>
                    {project.name}
                  </option>
                ))}
              </NativeSelect>
            )}
          </FormField>
        )}
        {siteOptions.length > 0 && (
          <FormField
            id={fieldId('competenceSiteId')}
            label={labels.competenceSiteId}
            required
            error={errors.competenceSiteId?.message}
          >
            {(p) => (
              <NativeSelect
                {...p}
                {...register('competenceSiteId', {
                  validate: (v) => siteOptions.length === 0 || v !== '' || t('form.errors.required'),
                })}
              >
                <option value="">{t('form.select')}</option>
                {siteOptions.map((site) => (
                  <option key={site.id} value={site.id}>
                    {site.name}
                  </option>
                ))}
              </NativeSelect>
            )}
          </FormField>
        )}
        {projectChanged && (
          <FormField
            id={fieldId('internshipTypeId')}
            label={labels.internshipTypeId}
            hint={t('ticket.hints.internshipType')}
            required
            error={errors.internshipTypeId?.message}
          >
            {(p) => (
              <NativeSelect
                {...p}
                disabled={!types.data}
                {...register('internshipTypeId', {
                  validate: (v) => !projectChanged || v !== '' || t('form.errors.required'),
                })}
              >
                <option value="">{t('form.select')}</option>
                {typeOptions.map((type) => (
                  <option key={type.id} value={type.id}>
                    {type.name}
                  </option>
                ))}
              </NativeSelect>
            )}
          </FormField>
        )}
        {freeText ? (
          <FormField
            id={fieldId('jobFreeText')}
            label={labels.jobFreeText}
            hint={t('work.requestCorrection.jobFreeTextHint')}
            required
            error={errors.jobFreeText?.message}
          >
            {(p) => (
              <Input
                {...p}
                autoComplete="off"
                {...withFocus(
                  register('jobFreeText', {
                    validate: (v) => v.trim() !== '' || t('form.errors.required'),
                    maxLength: { value: 200, message: t('form.errors.maxLength', { max: 200 }) },
                  }),
                  onlyJob,
                )}
              />
            )}
          </FormField>
        ) : (
          <FormField
            id={fieldId('jobCategoryId')}
            label={labels.jobCategoryId}
            hint={t('ticket.hints.jobCategory')}
            required
            error={errors.jobCategoryId?.message}
          >
            {(p) => (
              <NativeSelect {...p} {...withFocus(register('jobCategoryId', { required }), onlyJob)}>
                <option value="">{t('form.select')}</option>
                {withCurrent(categories, ticket.requestedJobCategory).map((category) => (
                  <option key={category.id} value={category.id}>
                    {category.name}
                  </option>
                ))}
              </NativeSelect>
            )}
          </FormField>
        )}
      </div>
      <div className="flex flex-wrap gap-3">
        <Button type="submit" disabled={isSubmitting}>
          {t('work.requestCorrection.save')}
        </Button>
        <Button type="button" variant="outline" onClick={onCancel}>
          {t('work.requestCorrection.cancel')}
        </Button>
      </div>
    </form>
  );
};
