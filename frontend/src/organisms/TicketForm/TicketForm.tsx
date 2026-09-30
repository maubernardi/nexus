import { useState, type ReactNode } from 'react';
import { useForm, type FieldPath } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router';

import { Button } from '@/components/ui/button';
import { NativeSelect } from '@/components/ui/native-select';
import type { CandidateSummary } from '@/config/api/candidateApi';
import { apiError } from '@/config/api/errors';
import type { ReferenceItem } from '@/config/api/referenceApi';
import type { Ticket } from '@/config/api/ticketApi';
import { useMyCandidates } from '@/config/hooks/useCandidates';
import { useJobCategories, useMyProjects } from '@/config/hooks/useReferenceData';
import { useSubmitTicket } from '@/config/hooks/useTickets';
import { useErrorSummaryFocus } from '@/hooks/useErrorSummaryFocus';
import { ErrorSummary, type SummaryError } from '@/molecules/ErrorSummary/ErrorSummary';
import { FormField } from '@/molecules/FormField/FormField';

type TicketFormValues = {
  candidateId: string;
  projectId: string;
  jobCategoryId: string;
};

const FIELDS = ['candidateId', 'projectId', 'jobCategoryId'] as const;

const fieldId = (field: keyof TicketFormValues): string => `ticket-${field}`;

type TicketFormProps = {
  onSuccess: (ticket: Ticket) => void;
  /** Candidato da preselezionare (es. appena registrato); ignorato se non è tra quelli del Tutor. */
  initialCandidateId?: string | null;
};

/** Riquadro informativo al posto del modulo quando mancano i presupposti per compilarlo. */
const Notice = ({ id, heading, children }: { id: string; heading: string; children: ReactNode }) => (
  <section aria-labelledby={id} className="space-y-3 rounded-xl border border-border bg-card p-6">
    <h2 id={id} className="text-lg font-semibold text-primary">
      {heading}
    </h2>
    {children}
  </section>
);

/** Segnalazione normale di un candidato del Tutor (US-301). Carica i dati di scelta prima di mostrare il modulo. */
export const TicketForm = ({ onSuccess, initialCandidateId = null }: TicketFormProps) => {
  const { t } = useTranslation();
  const candidates = useMyCandidates();
  const projects = useMyProjects();
  const categories = useJobCategories();

  if (candidates.isError || projects.isError || categories.isError) {
    return (
      <div role="alert" className="space-y-3 rounded-md border-2 border-destructive bg-card p-4">
        <p className="font-medium text-destructive">{t('ticket.loadError')}</p>
        <Button
          variant="outline"
          onClick={() => {
            void candidates.refetch();
            void projects.refetch();
            void categories.refetch();
          }}
        >
          {t('ticket.retry')}
        </Button>
      </div>
    );
  }
  if (!candidates.data || !projects.data || !categories.data) {
    return <p role="status">{t('ticket.loading')}</p>;
  }
  if (candidates.data.length === 0) {
    return (
      <Notice id="ticket-no-candidates" heading={t('ticket.empty.candidatesHeading')}>
        <p>{t('ticket.empty.candidatesText')}</p>
        <Button asChild size="lg">
          <Link to="/candidati/nuovo">{t('ticket.empty.candidatesAction')}</Link>
        </Button>
      </Notice>
    );
  }
  if (projects.data.length === 0) {
    return (
      <Notice id="ticket-no-projects" heading={t('ticket.empty.projectsHeading')}>
        <p>{t('ticket.empty.projectsText')}</p>
      </Notice>
    );
  }

  return (
    <TicketFormFields
      candidates={candidates.data}
      projects={projects.data}
      categories={categories.data}
      initialCandidateId={initialCandidateId}
      onSuccess={onSuccess}
    />
  );
};

type TicketFormFieldsProps = {
  candidates: CandidateSummary[];
  projects: ReferenceItem[];
  categories: ReferenceItem[];
  initialCandidateId: string | null;
  onSuccess: (ticket: Ticket) => void;
};

const TicketFormFields = ({
  candidates,
  projects,
  categories,
  initialCandidateId,
  onSuccess,
}: TicketFormFieldsProps) => {
  const { t } = useTranslation();
  const submitTicket = useSubmitTicket();
  const [genericError, setGenericError] = useState(false);

  // scelta obbligata già fatta: un solo progetto, oppure il candidato appena registrato
  const onlyOption = (items: { id: string }[]): string => (items.length === 1 ? items[0].id : '');
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting, submitCount },
  } = useForm<TicketFormValues>({
    defaultValues: {
      candidateId: candidates.some((c) => c.id === initialCandidateId)
        ? (initialCandidateId ?? '')
        : onlyOption(candidates),
      projectId: onlyOption(projects),
      jobCategoryId: '',
    },
    mode: 'onSubmit',
    reValidateMode: 'onChange',
    // il focus va al riepilogo degli errori (non al primo campo): lo gestisce useErrorSummaryFocus
    shouldFocusError: false,
  });
  const { summaryRef, requestSummaryFocus } = useErrorSummaryFocus(submitCount, errors);

  const required = { value: true, message: t('form.errors.required') };
  const labels: Record<keyof TicketFormValues, string> = {
    candidateId: t('ticket.fields.candidate'),
    projectId: t('ticket.fields.project'),
    jobCategoryId: t('ticket.fields.jobCategory'),
  };
  const summary: SummaryError[] = FIELDS.flatMap((field) => {
    const message = errors[field]?.message;
    return message ? [{ fieldId: fieldId(field), message: `${labels[field]}: ${message}` }] : [];
  });

  const onValid = async (values: TicketFormValues): Promise<void> => {
    setGenericError(false);
    try {
      onSuccess(await submitTicket.mutateAsync(values));
    } catch (error) {
      const fieldErrors = (apiError(error)?.fieldErrors ?? []).filter((fe) =>
        (FIELDS as readonly string[]).includes(fe.field),
      );
      if (fieldErrors.length > 0) {
        fieldErrors.forEach((fe) =>
          setError(fe.field as FieldPath<TicketFormValues>, { type: 'server', message: fe.message }),
        );
      } else {
        setGenericError(true);
      }
      requestSummaryFocus();
    }
  };

  return (
    <form noValidate onSubmit={handleSubmit(onValid, requestSummaryFocus)} className="space-y-6">
      {submitCount > 0 && <ErrorSummary ref={summaryRef} errors={summary} />}
      {genericError && (
        <p
          role="alert"
          className="rounded-md border-2 border-destructive bg-card p-4 font-medium text-destructive"
        >
          {t('form.errors.generic')}
        </p>
      )}

      <div className="grid max-w-2xl gap-4">
        <FormField
          id={fieldId('candidateId')}
          label={labels.candidateId}
          hint={t('ticket.hints.candidate')}
          required
          error={errors.candidateId?.message}
        >
          {(p) => (
            <NativeSelect {...p} {...register('candidateId', { required })}>
              <option value="">{t('form.select')}</option>
              {candidates.map((c) => (
                <option key={c.id} value={c.id}>
                  {t('ticket.candidateOption', {
                    lastName: c.lastName,
                    firstName: c.firstName,
                    birthYear: c.birthYear,
                  })}
                </option>
              ))}
            </NativeSelect>
          )}
        </FormField>
        <FormField
          id={fieldId('projectId')}
          label={labels.projectId}
          hint={t('ticket.hints.project')}
          required
          error={errors.projectId?.message}
        >
          {(p) => (
            <NativeSelect {...p} {...register('projectId', { required })}>
              <option value="">{t('form.select')}</option>
              {projects.map((project) => (
                <option key={project.id} value={project.id}>
                  {project.name}
                </option>
              ))}
            </NativeSelect>
          )}
        </FormField>
        <FormField
          id={fieldId('jobCategoryId')}
          label={labels.jobCategoryId}
          hint={t('ticket.hints.jobCategory')}
          required
          error={errors.jobCategoryId?.message}
        >
          {(p) => (
            <NativeSelect {...p} {...register('jobCategoryId', { required })}>
              <option value="">{t('form.select')}</option>
              {categories.map((category) => (
                <option key={category.id} value={category.id}>
                  {category.name}
                </option>
              ))}
            </NativeSelect>
          )}
        </FormField>
      </div>

      <Button type="submit" size="lg" className="w-full sm:w-auto" disabled={isSubmitting}>
        {isSubmitting ? t('ticket.submitting') : t('ticket.submit')}
      </Button>
    </form>
  );
};
