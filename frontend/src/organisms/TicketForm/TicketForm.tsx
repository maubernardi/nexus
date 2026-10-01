import { useState, type ReactNode } from 'react';
import { useForm, type FieldPath } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router';

import { Button } from '@/components/ui/button';
import { NativeSelect } from '@/components/ui/native-select';
import type { BeneficiarySummary } from '@/config/api/beneficiaryApi';
import { apiError } from '@/config/api/errors';
import type { ReferenceItem } from '@/config/api/referenceApi';
import type { Ticket } from '@/config/api/ticketApi';
import { useMyBeneficiaries } from '@/config/hooks/useBeneficiaries';
import { useJobCategories, useMyProjects } from '@/config/hooks/useReferenceData';
import { useSubmitTicket } from '@/config/hooks/useTickets';
import { useErrorSummaryFocus } from '@/hooks/useErrorSummaryFocus';
import { ErrorSummary, type SummaryError } from '@/molecules/ErrorSummary/ErrorSummary';
import { FormField } from '@/molecules/FormField/FormField';

type TicketFormValues = {
  beneficiaryId: string;
  projectId: string;
  jobCategoryId: string;
};

const FIELDS = ['beneficiaryId', 'projectId', 'jobCategoryId'] as const;

const fieldId = (field: keyof TicketFormValues): string => `ticket-${field}`;

type TicketFormProps = {
  onSuccess: (ticket: Ticket) => void;
  /** Beneficiario da preselezionare (es. appena registrato); ignorato se non è tra quelli del Tutor. */
  initialBeneficiaryId?: string | null;
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

/** Segnalazione normale di un beneficiario del Tutor (US-301). Carica i dati di scelta prima di mostrare il modulo. */
export const TicketForm = ({ onSuccess, initialBeneficiaryId = null }: TicketFormProps) => {
  const { t } = useTranslation();
  const beneficiaries = useMyBeneficiaries();
  const projects = useMyProjects();
  const categories = useJobCategories();

  if (beneficiaries.isError || projects.isError || categories.isError) {
    return (
      <div role="alert" className="space-y-3 rounded-md border-2 border-destructive bg-card p-4">
        <p className="font-medium text-destructive">{t('ticket.loadError')}</p>
        <Button
          variant="outline"
          onClick={() => {
            void beneficiaries.refetch();
            void projects.refetch();
            void categories.refetch();
          }}
        >
          {t('ticket.retry')}
        </Button>
      </div>
    );
  }
  if (!beneficiaries.data || !projects.data || !categories.data) {
    return <p role="status">{t('ticket.loading')}</p>;
  }
  if (beneficiaries.data.length === 0) {
    return (
      <Notice id="ticket-no-beneficiaries" heading={t('ticket.empty.beneficiariesHeading')}>
        <p>{t('ticket.empty.beneficiariesText')}</p>
        <Button asChild size="lg">
          <Link to="/beneficiari/nuovo">{t('ticket.empty.beneficiariesAction')}</Link>
        </Button>
      </Notice>
    );
  }
  if (beneficiaries.data.every((b) => b.openTicketNumber)) {
    return (
      <Notice id="ticket-all-open" heading={t('ticket.empty.allOpenHeading')}>
        <p>{t('ticket.empty.allOpenText')}</p>
        <Button asChild size="lg">
          <Link to="/beneficiari/nuovo">{t('ticket.empty.beneficiariesAction')}</Link>
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
      beneficiaries={beneficiaries.data}
      projects={projects.data}
      categories={categories.data}
      initialBeneficiaryId={initialBeneficiaryId}
      onSuccess={onSuccess}
    />
  );
};

type TicketFormFieldsProps = {
  beneficiaries: BeneficiarySummary[];
  projects: ReferenceItem[];
  categories: ReferenceItem[];
  initialBeneficiaryId: string | null;
  onSuccess: (ticket: Ticket) => void;
};

const TicketFormFields = ({
  beneficiaries,
  projects,
  categories,
  initialBeneficiaryId,
  onSuccess,
}: TicketFormFieldsProps) => {
  const { t } = useTranslation();
  const submitTicket = useSubmitTicket();
  const [genericError, setGenericError] = useState(false);

  // chi ha già una segnalazione aperta resta nell'elenco (si capisce perché manca) ma non è selezionabile
  const available = beneficiaries.filter((b) => !b.openTicketNumber);
  // scelta obbligata già fatta: un solo progetto, oppure il beneficiario appena registrato
  const onlyOption = (items: { id: string }[]): string => (items.length === 1 ? items[0].id : '');
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting, submitCount },
  } = useForm<TicketFormValues>({
    defaultValues: {
      beneficiaryId: available.some((b) => b.id === initialBeneficiaryId)
        ? (initialBeneficiaryId ?? '')
        : onlyOption(available),
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
    beneficiaryId: t('ticket.fields.beneficiary'),
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
          id={fieldId('beneficiaryId')}
          label={labels.beneficiaryId}
          hint={t('ticket.hints.beneficiary')}
          required
          error={errors.beneficiaryId?.message}
        >
          {(p) => (
            <NativeSelect {...p} {...register('beneficiaryId', { required })}>
              <option value="">{t('form.select')}</option>
              {beneficiaries.map((b) => (
                <option key={b.id} value={b.id} disabled={Boolean(b.openTicketNumber)}>
                  {t(b.openTicketNumber ? 'ticket.beneficiaryOptionOpen' : 'ticket.beneficiaryOption', {
                    lastName: b.lastName,
                    firstName: b.firstName,
                    birthYear: b.birthYear,
                    number: b.openTicketNumber,
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
