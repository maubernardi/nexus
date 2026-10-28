import { useEffect, useRef, useState, type ReactNode } from 'react';
import { CircleCheck } from 'lucide-react';
import { useForm, type FieldPath } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import { Link, useParams } from 'react-router';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { Button } from '@/components/ui/button';
import { NativeSelect } from '@/components/ui/native-select';
import type { TutorBoardPost } from '@/config/api/boardPostApi';
import { apiError } from '@/config/api/errors';
import type { Ticket } from '@/config/api/ticketApi';
import { useMyBeneficiaries } from '@/config/hooks/useBeneficiaries';
import { useTutorBoardPost } from '@/config/hooks/useBoardPosts';
import { useMyProjects } from '@/config/hooks/useReferenceData';
import { useSubmitSpecialTicket } from '@/config/hooks/useTickets';
import { useErrorSummaryFocus } from '@/hooks/useErrorSummaryFocus';
import { ErrorSummary, type SummaryError } from '@/molecules/ErrorSummary/ErrorSummary';
import { FormField } from '@/molecules/FormField/FormField';
import { TutorBoardPostCard } from '@/molecules/TutorBoardPostCard/TutorBoardPostCard';

/**
 * Candidatura di un beneficiario del Tutor a un annuncio della bacheca (US-306): nasce una segnalazione speciale con la
 * corsia veloce. La prima candidatura blocca l'annuncio finché il Call Center non decide (review Sprint 6, §8.1).
 */
export const CandidaturePage = () => {
  const { t } = useTranslation();
  const { id = '' } = useParams();
  const post = useTutorBoardPost(id);
  const [created, setCreated] = useState<Ticket | null>(null);
  const successRef = useRef<HTMLHeadingElement>(null);

  // candidatura inviata: il focus va sul titolo del riquadro di esito
  useEffect(() => {
    if (created) successRef.current?.focus();
  }, [created]);

  if (post.isError) {
    const notFound = apiError(post.error)?.status === 404;
    return (
      <div className="space-y-4">
        <PageHeading>{t(notFound ? 'candidature.notFound' : 'board.loadError')}</PageHeading>
        {notFound && <p>{t('candidature.notFoundText')}</p>}
        <Button asChild size="lg" variant="outline">
          <Link to="/bacheca">{t('candidature.back')}</Link>
        </Button>
      </div>
    );
  }
  if (!post.data) {
    return <p role="status">{t('common.loading')}</p>;
  }
  const p = post.data;

  return (
    <div className="space-y-6">
      <PageHeading>{t('candidature.heading', { number: p.number })}</PageHeading>
      {created ? (
        <section
          aria-labelledby="candidature-success"
          className="space-y-4 rounded-xl border border-l-4 border-border border-l-primary bg-card p-6"
        >
          <h2
            id="candidature-success"
            ref={successRef}
            tabIndex={-1}
            className="flex items-center gap-2 text-xl font-semibold text-primary"
          >
            <CircleCheck aria-hidden="true" className="size-6" />
            {t('candidature.success.heading', { number: created.number })}
          </h2>
          <p>
            {t('candidature.success.text', {
              name: `${created.beneficiary.firstName} ${created.beneficiary.lastName}`,
              post: p.number,
            })}
          </p>
          <div className="flex flex-wrap gap-3">
            <Button asChild size="lg">
              <Link to={`/segnalazioni/${created.id}`}>{t('candidature.success.open')}</Link>
            </Button>
            <Button asChild size="lg" variant="outline">
              <Link to="/bacheca">{t('candidature.back')}</Link>
            </Button>
          </div>
        </section>
      ) : (
        <>
          <p className="text-muted-foreground">{t('candidature.intro')}</p>
          <div className="max-w-xl">
            <TutorBoardPostCard post={p} />
          </div>
          {p.candidatureInProgress ? (
            <Notice id="candidature-in-progress" heading={t('candidature.inProgressHeading')}>
              <p>{t('candidature.inProgressText')}</p>
              <Button asChild size="lg" variant="outline">
                <Link to="/bacheca">{t('candidature.back')}</Link>
              </Button>
            </Notice>
          ) : (
            <CandidatureForm post={p} onSuccess={setCreated} />
          )}
        </>
      )}
    </div>
  );
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

type Values = { beneficiaryId: string; projectId: string };

const FIELDS = ['beneficiaryId', 'projectId'] as const;
const fieldId = (field: keyof Values): string => `candidature-${field}`;

/** Scelta del beneficiario e, per un annuncio aperto a tutti, del progetto della segnalazione. */
const CandidatureForm = ({
  post: p,
  onSuccess,
}: {
  post: TutorBoardPost;
  onSuccess: (t: Ticket) => void;
}) => {
  const { t } = useTranslation();
  const beneficiaries = useMyBeneficiaries();
  // annuncio riservato: il progetto è il suo, non si sceglie
  const projects = useMyProjects(!p.project);

  if (beneficiaries.isError || projects.isError) {
    return (
      <div role="alert" className="space-y-3 rounded-md border-2 border-destructive bg-card p-4">
        <p className="font-medium text-destructive">{t('ticket.loadError')}</p>
        <Button
          variant="outline"
          onClick={() => {
            void beneficiaries.refetch();
            void projects.refetch();
          }}
        >
          {t('ticket.retry')}
        </Button>
      </div>
    );
  }
  if (!beneficiaries.data || (!p.project && !projects.data)) {
    return <p role="status">{t('ticket.loading')}</p>;
  }
  if (beneficiaries.data.length === 0) {
    return (
      <Notice id="candidature-no-beneficiaries" heading={t('ticket.empty.beneficiariesHeading')}>
        <p>{t('ticket.empty.beneficiariesText')}</p>
        <Button asChild size="lg">
          <Link to="/beneficiari/nuovo">{t('ticket.empty.beneficiariesAction')}</Link>
        </Button>
      </Notice>
    );
  }
  if (beneficiaries.data.every((b) => b.openTicketNumber)) {
    return (
      <Notice id="candidature-all-open" heading={t('ticket.empty.allOpenHeading')}>
        <p>{t('ticket.empty.allOpenText')}</p>
        <Button asChild size="lg">
          <Link to="/beneficiari/nuovo">{t('ticket.empty.beneficiariesAction')}</Link>
        </Button>
      </Notice>
    );
  }
  if (!p.project && projects.data?.length === 0) {
    return (
      <Notice id="candidature-no-projects" heading={t('ticket.empty.projectsHeading')}>
        <p>{t('ticket.empty.projectsText')}</p>
      </Notice>
    );
  }
  return (
    <CandidatureFields
      post={p}
      beneficiaries={beneficiaries.data}
      projects={projects.data ?? []}
      onSuccess={onSuccess}
    />
  );
};

type CandidatureFieldsProps = {
  post: TutorBoardPost;
  beneficiaries: NonNullable<ReturnType<typeof useMyBeneficiaries>['data']>;
  projects: NonNullable<ReturnType<typeof useMyProjects>['data']>;
  onSuccess: (t: Ticket) => void;
};

const CandidatureFields = ({ post: p, beneficiaries, projects, onSuccess }: CandidatureFieldsProps) => {
  const { t } = useTranslation();
  const submit = useSubmitSpecialTicket();
  const [refused, setRefused] = useState<string | null>(null);
  const refusedRef = useRef<HTMLParagraphElement>(null);
  const available = beneficiaries.filter((b) => !b.openTicketNumber);
  const onlyOption = (items: { id: string }[]): string => (items.length === 1 ? items[0].id : '');
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting, submitCount },
  } = useForm<Values>({
    defaultValues: { beneficiaryId: onlyOption(available), projectId: onlyOption(projects) },
    mode: 'onSubmit',
    reValidateMode: 'onChange',
    shouldFocusError: false,
  });
  const { summaryRef, requestSummaryFocus } = useErrorSummaryFocus(submitCount, errors);

  useEffect(() => {
    if (refused) refusedRef.current?.focus();
  }, [refused]);

  const required = { value: true, message: t('form.errors.required') };
  const labels: Record<keyof Values, string> = {
    beneficiaryId: t('ticket.fields.beneficiary'),
    projectId: t('ticket.fields.project'),
  };
  const summary: SummaryError[] = FIELDS.flatMap((field) => {
    const message = errors[field]?.message;
    return message ? [{ fieldId: fieldId(field), message: `${labels[field]}: ${message}` }] : [];
  });

  const onValid = async (v: Values): Promise<void> => {
    setRefused(null);
    try {
      onSuccess(
        await submit.mutateAsync({
          boardPostId: p.id,
          beneficiaryId: v.beneficiaryId,
          projectId: p.project ? null : v.projectId,
        }),
      );
    } catch (error) {
      const body = apiError(error);
      const fieldErrors = (body?.fieldErrors ?? []).filter((fe) =>
        (FIELDS as readonly string[]).includes(fe.field),
      );
      if (fieldErrors.length > 0) {
        fieldErrors.forEach((fe) =>
          setError(fe.field as FieldPath<Values>, { type: 'server', message: fe.message }),
        );
        requestSummaryFocus();
      } else {
        // annuncio appena bloccato da un'altra candidatura o uscito dalla bacheca: il motivo lo dà il server
        setRefused(body?.status === 409 || body?.status === 404 ? body.message : t('form.errors.generic'));
      }
    }
  };

  return (
    <section aria-labelledby="candidature-form" className="space-y-4">
      <h2 id="candidature-form" className="text-lg font-semibold text-primary">
        {t('candidature.formHeading')}
      </h2>
      <form noValidate onSubmit={handleSubmit(onValid, requestSummaryFocus)} className="space-y-6">
        {submitCount > 0 && <ErrorSummary ref={summaryRef} errors={summary} />}
        {refused && (
          <p
            ref={refusedRef}
            tabIndex={-1}
            className="rounded-md border-2 border-destructive bg-card p-4 font-medium text-destructive"
          >
            {t('candidature.refused', { reason: refused })}
          </p>
        )}
        <div className="grid max-w-2xl gap-4">
          <FormField
            id={fieldId('beneficiaryId')}
            label={labels.beneficiaryId}
            hint={t('candidature.beneficiaryHint')}
            required
            error={errors.beneficiaryId?.message}
          >
            {(f) => (
              <NativeSelect {...f} {...register('beneficiaryId', { required })}>
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
          {p.project ? (
            <p>{t('candidature.projectFixed', { project: p.project.name })}</p>
          ) : (
            <FormField
              id={fieldId('projectId')}
              label={labels.projectId}
              hint={t('candidature.projectHint')}
              required
              error={errors.projectId?.message}
            >
              {(f) => (
                <NativeSelect {...f} {...register('projectId', { required })}>
                  <option value="">{t('form.select')}</option>
                  {projects.map((project) => (
                    <option key={project.id} value={project.id}>
                      {project.name}
                    </option>
                  ))}
                </NativeSelect>
              )}
            </FormField>
          )}
        </div>
        <div className="flex flex-wrap gap-3">
          <Button type="submit" size="lg" disabled={isSubmitting}>
            {isSubmitting ? t('candidature.submitting') : t('candidature.submit')}
          </Button>
          <Button asChild size="lg" variant="outline">
            <Link to="/bacheca">{t('candidature.back')}</Link>
          </Button>
        </div>
      </form>
    </section>
  );
};
