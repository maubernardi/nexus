import { useEffect, useRef, useState } from 'react';
import { Zap } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import { Link, Navigate, useParams } from 'react-router';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { NativeSelect } from '@/components/ui/native-select';
import { Textarea } from '@/components/ui/textarea';
import { apiError } from '@/config/api/errors';
import type { JobSlotMatch, TicketDetail, TicketEvent } from '@/config/api/ticketApi';
import { useCurrentUser } from '@/config/hooks/useCurrentUser';
import { useJobCategories, useZones } from '@/config/hooks/useReferenceData';
import {
  useAcceptProposal,
  useApproveCategory,
  useAppointmentFailed,
  useAppointmentSet,
  useExcludeCompany,
  useExclusions,
  useCompatibleJobSlots,
  useMatchTicket,
  useRejectCategory,
  useRejectProposal,
  useTakeOver,
  useTicket,
  useTicketHistory,
} from '@/config/hooks/useTickets';
import {
  APPOINTMENT_FAILURE_CAUSES,
  TICKET_STATUSES,
  type AppointmentFailureCause,
} from '@/lib/domainValues';
import { FormField } from '@/molecules/FormField/FormField';
import { MatchDetailsDialog } from '@/organisms/MatchDetailsDialog/MatchDetailsDialog';
import type { BeneficiaryLinkState } from '@/pages/BeneficiaryPage/BeneficiaryPage';

const dateTime = new Intl.DateTimeFormat('it-IT', { dateStyle: 'short', timeStyle: 'short' });

// valore del select per "nessun filtro"
const ALL = '';

type Outcome =
  | { kind: 'proposed'; title: string; company: string }
  | { kind: 'takenOver' }
  | { kind: 'accepted'; deadline: string }
  | { kind: 'appointmentSet' }
  | { kind: 'appointmentFailed' }
  | { kind: 'proposalRejected' }
  | { kind: 'excluded'; company: string }
  | { kind: 'categoryApproved'; name: string }
  | { kind: 'categoryRejected'; name: string }
  | { kind: 'conflict'; reason: string }
  | { kind: 'error' };

/** Vecchio indirizzo della lavorazione (`/lavorazioni/:id`): porta al dettaglio unico. */
export const LegacyWorkRedirect = () => {
  const { id = '' } = useParams();
  return <Navigate to={`/segnalazioni/${id}`} replace />;
};

/**
 * Dettaglio della segnalazione per tutti i ruoli (US-305): dati, proposta e cronologia; per Call Center e ADMIN anche
 * la lavorazione (presa in carico da un collega, mansioni compatibili e abbinamento, US-405/601/602).
 */
export const TicketPage = () => {
  const { t } = useTranslation();
  const { id = '' } = useParams();
  const ticket = useTicket(id);
  const { data: user } = useCurrentUser();
  const isCallCenter = user?.roles.some((r) => r === 'CALL_CENTER' || r === 'ADMIN') ?? false;
  const outcomeRef = useRef<HTMLDivElement>(null);
  const [outcome, setOutcome] = useState<Outcome | null>(null);
  const takeOver = useTakeOver();

  useEffect(() => {
    if (outcome) outcomeRef.current?.focus();
  }, [outcome]);

  if (ticket.isError) {
    return (
      <div className="space-y-4">
        <PageHeading>
          {t(apiError(ticket.error)?.status === 404 ? 'work.notFound' : 'common.loadError')}
        </PageHeading>
        <Link to="/" className="font-medium text-primary underline underline-offset-4">
          {t('work.backToList')}
        </Link>
      </div>
    );
  }
  if (!ticket.data) {
    return <p role="status">{t('common.loading')}</p>;
  }
  const d = ticket.data;
  // dopo la presa in carico chiunque del Call Center (o l'ADMIN) può lavorarla, anche se è di un collega (US-405)
  // anche una segnalazione riaperta (appuntamento fallito) si abbina di nuovo (US-802)
  const workable = d.status === 'IN_LAVORAZIONE' || d.status === 'RIAPERTO';
  // l'API omette i campi vuoti: senza operatore il campo manca del tutto
  const assigned = Boolean(d.assignedOperatorName);
  const canMatch = isCallCenter && workable && assigned;
  const ofColleague = isCallCenter && assigned && !d.assignedToMe;
  const onTakeOver = (): void => {
    takeOver.mutate(
      { id: d.id, version: d.version },
      {
        onSuccess: () => setOutcome({ kind: 'takenOver' }),
        onError: (error) => {
          const body = apiError(error);
          setOutcome(body?.status === 409 ? { kind: 'conflict', reason: body.message } : { kind: 'error' });
        },
      },
    );
  };
  const assignment = d.assignedToMe
    ? t('work.assignedToYou')
    : d.assignedOperatorName
      ? t('work.assignedTo', { name: d.assignedOperatorName })
      : t('work.unassigned');

  return (
    <div className="space-y-6">
      <PageHeading>{t('pages.ticket.heading', { number: d.number })}</PageHeading>
      <p className="font-medium">
        {t('work.status', { status: t(`enums.ticketStatus.${d.status}`) })} · {assignment}
      </p>
      {ofColleague && (
        <div className="flex flex-wrap items-center gap-3">
          <p className="text-muted-foreground">{t('work.sharedHint')}</p>
          <Button variant="outline" onClick={onTakeOver} disabled={takeOver.isPending}>
            {t('work.takeOver')}
          </Button>
        </div>
      )}

      {outcome && (
        <div
          ref={outcomeRef}
          tabIndex={-1}
          className={
            outcome.kind !== 'conflict' && outcome.kind !== 'error'
              ? 'rounded-md border-l-4 border-primary bg-card p-4 font-medium'
              : 'rounded-md border-2 border-destructive bg-card p-4 font-medium text-destructive'
          }
        >
          {outcome.kind === 'takenOver'
            ? t('work.takenOver')
            : outcome.kind === 'categoryApproved'
              ? t('work.category.approved', { name: outcome.name })
              : outcome.kind === 'categoryRejected'
                ? t('work.category.rejected', { name: outcome.name })
                : outcome.kind === 'excluded'
                  ? t('exclusion.done', { company: outcome.company })
                  : outcome.kind === 'proposalRejected'
                    ? t('work.reject.saved')
                    : outcome.kind === 'appointmentFailed'
                      ? t('work.appointment.failedSaved')
                      : outcome.kind === 'appointmentSet'
                        ? t('work.appointment.saved')
                        : outcome.kind === 'accepted'
                          ? t('work.accepted', { deadline: formatDay(outcome.deadline) })
                          : outcome.kind === 'proposed'
                            ? t('work.proposed', { title: outcome.title, company: outcome.company })
                            : outcome.kind === 'conflict'
                              ? t('work.conflict', { reason: outcome.reason })
                              : t('work.error')}
        </div>
      )}

      <TicketData ticket={d} />

      {isCallCenter && d.status === 'IN_ATTESA_APPROVAZIONE' && d.requestedJobFreeText && (
        <CategoryDecisionSection ticket={d} onOutcome={setOutcome} />
      )}

      {d.proposal && <ProposalSection ticket={d} onOutcome={setOutcome} />}
      {isCallCenter && workable && !canMatch && (
        <p className="rounded-xl border border-border bg-card p-4">{t('work.notTaken')}</p>
      )}
      {canMatch && <CompatibleSection ticket={d} onOutcome={setOutcome} />}

      <TicketHistory ticketId={d.id} />

      <Link to="/" className="inline-block font-medium text-primary underline underline-offset-4">
        {t('work.backToList')}
      </Link>
    </div>
  );
};

const KNOWN_ACTIONS = [
  'SUBMIT',
  'CATEGORY_APPROVE',
  'CATEGORY_REJECT',
  'TAKE_CHARGE',
  'TAKE_OVER',
  'MATCH',
  'ACCEPT',
  'APPOINTMENT_SET',
  'APPOINTMENT_FAILED',
  'PROPOSAL_REJECT',
  'TIMER_REMINDER',
  'TIMER_EXPIRE',
  'COMPANY_EXCLUDE',
  'STATUS',
] as const;

type ApproveValues = { name: string };
type RejectValues = { jobCategoryId: string; reason: string };

/**
 * Decisione del Call Center sulla nuova tipologia proposta dal Tutor (US-302): approvarla, anche con il nome corretto,
 * oppure usare una tipologia del catalogo spiegando il motivo.
 */
const CategoryDecisionSection = ({
  ticket: d,
  onOutcome,
}: {
  ticket: TicketDetail;
  onOutcome: (o: Outcome) => void;
}) => {
  const { t } = useTranslation();
  const categories = useJobCategories();
  const approve = useApproveCategory();
  const reject = useRejectCategory();
  const proposed = d.requestedJobFreeText ?? '';
  const required = { value: true, message: t('form.errors.required') };
  const approveForm = useForm<ApproveValues>({ defaultValues: { name: proposed } });
  const rejectForm = useForm<RejectValues>({ defaultValues: { jobCategoryId: '', reason: '' } });

  const onError = (error: unknown, setField: (field: string, message: string) => void): void => {
    const body = apiError(error);
    const fieldError = body?.fieldErrors?.[0];
    if (fieldError) {
      setField(fieldError.field, fieldError.message);
    } else {
      onOutcome(body?.status === 409 ? { kind: 'conflict', reason: body.message } : { kind: 'error' });
    }
  };

  const onApprove = async ({ name }: ApproveValues): Promise<void> => {
    try {
      const updated = await approve.mutateAsync({ id: d.id, name: name.trim(), version: d.version });
      onOutcome({ kind: 'categoryApproved', name: updated.requestedJobCategory?.name ?? name.trim() });
    } catch (error) {
      onError(error, (_field, message) => approveForm.setError('name', { type: 'server', message }));
    }
  };

  const onReject = async ({ jobCategoryId, reason }: RejectValues): Promise<void> => {
    try {
      const updated = await reject.mutateAsync({
        id: d.id,
        jobCategoryId,
        reason: reason.trim(),
        version: d.version,
      });
      onOutcome({ kind: 'categoryRejected', name: updated.requestedJobCategory?.name ?? '' });
    } catch (error) {
      onError(error, (field, message) =>
        rejectForm.setError(field === 'reason' ? 'reason' : 'jobCategoryId', { type: 'server', message }),
      );
    }
  };

  return (
    <section
      aria-labelledby="work-category"
      className="space-y-4 rounded-xl border border-l-4 border-border border-l-gold bg-card p-4"
    >
      <h2 id="work-category" className="text-lg font-semibold text-primary">
        {t('work.category.heading')}
      </h2>
      <p>{t('work.category.proposed', { name: proposed })}</p>

      <form noValidate onSubmit={approveForm.handleSubmit(onApprove)} className="space-y-3">
        <FormField
          id="work-category-name"
          label={t('work.category.name')}
          hint={t('work.category.nameHint')}
          required
          error={approveForm.formState.errors.name?.message}
        >
          {(p) => (
            <Input
              {...p}
              autoComplete="off"
              {...approveForm.register('name', {
                validate: (v) => v.trim() !== '' || t('form.errors.required'),
                maxLength: { value: 200, message: t('form.errors.maxLength', { max: 200 }) },
              })}
            />
          )}
        </FormField>
        {/* testo lungo: a 320 px va a capo invece di sbordare (WCAG 1.4.10) */}
        <Button
          type="submit"
          size="lg"
          className="h-auto min-h-11 py-2 whitespace-normal"
          disabled={approve.isPending || reject.isPending}
        >
          {t('work.category.approve')}
        </Button>
      </form>

      <form
        noValidate
        onSubmit={rejectForm.handleSubmit(onReject)}
        aria-labelledby="work-category-or"
        className="space-y-3 border-t border-border pt-4"
      >
        <h3 id="work-category-or" className="font-semibold">
          {t('work.category.orHeading')}
        </h3>
        <FormField
          id="work-category-catalog"
          label={t('work.category.catalog')}
          required
          error={rejectForm.formState.errors.jobCategoryId?.message}
        >
          {(p) => (
            <NativeSelect {...p} {...rejectForm.register('jobCategoryId', { required })}>
              <option value="">{t('form.select')}</option>
              {(categories.data ?? []).map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </NativeSelect>
          )}
        </FormField>
        <FormField
          id="work-category-reason"
          label={t('work.category.reason')}
          hint={t('work.category.reasonHint')}
          required
          error={rejectForm.formState.errors.reason?.message}
        >
          {(p) => (
            <Textarea
              {...p}
              rows={2}
              {...rejectForm.register('reason', {
                validate: (v) => v.trim() !== '' || t('form.errors.required'),
                maxLength: { value: 500, message: t('form.errors.maxLength', { max: 500 }) },
              })}
            />
          )}
        </FormField>
        <Button type="submit" size="lg" variant="outline" disabled={approve.isPending || reject.isPending}>
          {t('work.category.reject')}
        </Button>
      </form>
    </section>
  );
};

const dayFormat = new Intl.DateTimeFormat('it-IT', {
  weekday: 'long',
  day: '2-digit',
  month: '2-digit',
  year: 'numeric',
});

/** Giorno ISO (aaaa-mm-gg) con il giorno della settimana, senza passare dal fuso orario. */
const formatDay = (iso: string): string => {
  const [y, m, d] = iso.split('-').map(Number);
  return dayFormat.format(new Date(y, m - 1, d));
};

/**
 * Proposta all'azienda: mansione abbinata, scadenza del timer se accettata e, per il Tutor della segnalazione,
 * l'accettazione con conferma (US-701).
 */
const ProposalSection = ({
  ticket: d,
  onOutcome,
}: {
  ticket: TicketDetail;
  onOutcome: (o: Outcome) => void;
}) => {
  const { t } = useTranslation();
  const accept = useAcceptProposal();
  const [confirming, setConfirming] = useState(false);
  const confirmRef = useRef<HTMLHeadingElement>(null);
  const proposal = d.proposal;

  useEffect(() => {
    if (confirming) confirmRef.current?.focus();
  }, [confirming]);

  if (!proposal) return null;
  const canAccept = d.myTicket && d.status === 'PROPOSTA_AZIENDA';

  const onConfirm = (): void => {
    accept.mutate(
      { id: d.id, version: d.version },
      {
        onSuccess: (updated) => onOutcome({ kind: 'accepted', deadline: updated.timerDeadline ?? '' }),
        onError: (error) => {
          const body = apiError(error);
          onOutcome(body?.status === 409 ? { kind: 'conflict', reason: body.message } : { kind: 'error' });
        },
        onSettled: () => setConfirming(false),
      },
    );
  };

  return (
    <section
      aria-labelledby="work-proposal"
      className="space-y-3 rounded-xl border border-l-4 border-border border-l-primary bg-card p-4"
    >
      <h2 id="work-proposal" className="text-lg font-semibold text-primary">
        {t('work.sections.proposal')}
      </h2>
      <dl className="grid gap-x-6 gap-y-2 sm:grid-cols-[max-content_1fr]">
        <dt className="font-medium">{t('work.fields.company')}</dt>
        <dd>{proposal.companyName}</dd>
        <dt className="font-medium">{t('work.fields.jobSlot')}</dt>
        <dd>{proposal.title}</dd>
        <dt className="font-medium">{t('work.fields.jobCategory')}</dt>
        <dd>{proposal.jobCategory.name}</dd>
        <dt className="font-medium">{t('work.fields.zone')}</dt>
        <dd>{proposal.zone.name}</dd>
        {d.timerDeadline && (
          <>
            <dt className="font-medium">{t('work.timerDeadline')}</dt>
            <dd>{t('work.timerDeadlineValue', { date: formatDay(d.timerDeadline) })}</dd>
          </>
        )}
      </dl>
      {canAccept && !confirming && (
        <Button size="lg" onClick={() => setConfirming(true)}>
          {t('work.accept')}
        </Button>
      )}
      {canAccept && confirming && (
        <div className="space-y-3 rounded-xl border-2 border-primary bg-card p-4">
          <h3 ref={confirmRef} tabIndex={-1} className="font-semibold">
            {t('work.acceptHeading')}
          </h3>
          <p>{t('work.acceptText', { title: proposal.title, company: proposal.companyName })}</p>
          <div className="flex flex-wrap gap-3">
            <Button size="lg" onClick={onConfirm} disabled={accept.isPending}>
              {t('work.acceptConfirm')}
            </Button>
            <Button
              size="lg"
              variant="outline"
              onClick={() => setConfirming(false)}
              disabled={accept.isPending}
            >
              {t('work.cancel')}
            </Button>
          </div>
        </div>
      )}
      {canAccept && !confirming && (
        <ProposalRejectAction ticket={d} company={proposal.companyName} onOutcome={onOutcome} />
      )}
      {d.myTicket && d.status === 'PROPOSTA_ACCOLTA' && (
        <AppointmentSetAction ticket={d} company={proposal.companyName} onOutcome={onOutcome} />
      )}
      {d.myTicket && d.status === 'APPUNTAMENTO' && (
        <AppointmentFailedAction ticket={d} company={proposal.companyName} onOutcome={onOutcome} />
      )}
    </section>
  );
};

/** Il Tutor della segnalazione registra l'appuntamento fissato, con conferma e nota facoltativa (US-801). */
const AppointmentSetAction = ({
  ticket: d,
  company,
  onOutcome,
}: {
  ticket: TicketDetail;
  company: string;
  onOutcome: (o: Outcome) => void;
}) => {
  const { t } = useTranslation();
  const appointment = useAppointmentSet();
  const [confirming, setConfirming] = useState(false);
  const [note, setNote] = useState('');
  const confirmRef = useRef<HTMLHeadingElement>(null);

  useEffect(() => {
    if (confirming) confirmRef.current?.focus();
  }, [confirming]);

  // mutateAsync: l'esito arriva anche se, ricaricata la segnalazione in "Appuntamento", questo pulsante sparisce
  const onConfirm = async (): Promise<void> => {
    try {
      await appointment.mutateAsync({ id: d.id, version: d.version, note: note.trim() || null });
      onOutcome({ kind: 'appointmentSet' });
    } catch (error) {
      const body = apiError(error);
      onOutcome(body?.status === 409 ? { kind: 'conflict', reason: body.message } : { kind: 'error' });
      setConfirming(false);
    }
  };

  if (!confirming) {
    return (
      <Button size="lg" onClick={() => setConfirming(true)}>
        {t('work.appointment.set')}
      </Button>
    );
  }
  return (
    <div className="space-y-3 rounded-xl border-2 border-primary bg-card p-4">
      <h3 ref={confirmRef} tabIndex={-1} className="font-semibold">
        {t('work.appointment.setHeading')}
      </h3>
      <p>{t('work.appointment.setText', { company })}</p>
      <FormField
        id="work-appointment-note"
        label={t('work.appointment.note')}
        hint={t('work.appointment.noteHint')}
      >
        {(p) => (
          <Textarea {...p} rows={2} maxLength={1000} value={note} onChange={(e) => setNote(e.target.value)} />
        )}
      </FormField>
      <div className="flex flex-wrap gap-3">
        <Button size="lg" onClick={() => void onConfirm()} disabled={appointment.isPending}>
          {t('work.appointment.setConfirm')}
        </Button>
        <Button
          size="lg"
          variant="outline"
          onClick={() => setConfirming(false)}
          disabled={appointment.isPending}
        >
          {t('work.cancel')}
        </Button>
      </div>
    </div>
  );
};

type ProposalRejectValues = { reason: string };

/** Il Tutor della segnalazione rifiuta la proposta con motivo: l'azienda è esclusa (US-702, Q3). */
const ProposalRejectAction = ({
  ticket: d,
  company,
  onOutcome,
}: {
  ticket: TicketDetail;
  company: string;
  onOutcome: (o: Outcome) => void;
}) => {
  const { t } = useTranslation();
  const reject = useRejectProposal();
  const [confirming, setConfirming] = useState(false);
  const confirmRef = useRef<HTMLHeadingElement>(null);
  const form = useForm<ProposalRejectValues>({ defaultValues: { reason: '' } });

  useEffect(() => {
    if (confirming) confirmRef.current?.focus();
  }, [confirming]);

  // mutateAsync: l'esito arriva anche se, riaperta la segnalazione, la sezione della proposta sparisce
  const onConfirm = async ({ reason }: ProposalRejectValues): Promise<void> => {
    try {
      await reject.mutateAsync({ id: d.id, version: d.version, reason: reason.trim() });
      onOutcome({ kind: 'proposalRejected' });
    } catch (error) {
      const body = apiError(error);
      onOutcome(body?.status === 409 ? { kind: 'conflict', reason: body.message } : { kind: 'error' });
      setConfirming(false);
    }
  };

  if (!confirming) {
    return (
      <Button size="lg" variant="outline" onClick={() => setConfirming(true)}>
        {t('work.reject.action')}
      </Button>
    );
  }
  return (
    <form
      noValidate
      onSubmit={form.handleSubmit(onConfirm)}
      className="space-y-3 rounded-xl border-2 border-primary bg-card p-4"
    >
      <h3 ref={confirmRef} tabIndex={-1} className="font-semibold">
        {t('work.reject.heading')}
      </h3>
      <p>{t('work.reject.text', { company })}</p>
      <FormField
        id="work-reject-reason"
        label={t('work.reject.reason')}
        hint={t('work.reject.reasonHint')}
        required
        error={form.formState.errors.reason?.message}
      >
        {(p) => (
          <Textarea
            {...p}
            rows={2}
            {...form.register('reason', {
              validate: (v) => v.trim() !== '' || t('form.errors.required'),
              maxLength: { value: 500, message: t('form.errors.maxLength', { max: 500 }) },
            })}
          />
        )}
      </FormField>
      <div className="flex flex-wrap gap-3">
        <Button type="submit" size="lg" disabled={reject.isPending}>
          {t('work.reject.confirm')}
        </Button>
        <Button
          type="button"
          size="lg"
          variant="outline"
          onClick={() => setConfirming(false)}
          disabled={reject.isPending}
        >
          {t('work.cancel')}
        </Button>
      </div>
    </form>
  );
};

type FailedValues = { cause: AppointmentFailureCause | ''; note: string };

/** Il Tutor della segnalazione registra l'appuntamento fallito, con causa e nota (US-802). */
const AppointmentFailedAction = ({
  ticket: d,
  company,
  onOutcome,
}: {
  ticket: TicketDetail;
  company: string;
  onOutcome: (o: Outcome) => void;
}) => {
  const { t } = useTranslation();
  const failed = useAppointmentFailed();
  const [confirming, setConfirming] = useState(false);
  const confirmRef = useRef<HTMLHeadingElement>(null);
  const form = useForm<FailedValues>({ defaultValues: { cause: '', note: '' } });
  const { errors } = form.formState;

  useEffect(() => {
    if (confirming) confirmRef.current?.focus();
  }, [confirming]);

  // mutateAsync: l'esito arriva anche se, riaperta la segnalazione, questa sezione sparisce
  const onConfirm = async ({ cause, note }: FailedValues): Promise<void> => {
    try {
      await failed.mutateAsync({
        id: d.id,
        version: d.version,
        cause: cause as AppointmentFailureCause,
        note: note.trim() || null,
      });
      onOutcome({ kind: 'appointmentFailed' });
    } catch (error) {
      const body = apiError(error);
      const noteError = body?.fieldErrors?.find((fe) => fe.field === 'note');
      if (noteError) {
        form.setError('note', { type: 'server', message: noteError.message });
        return;
      }
      onOutcome(body?.status === 409 ? { kind: 'conflict', reason: body.message } : { kind: 'error' });
      setConfirming(false);
    }
  };

  if (!confirming) {
    return (
      <Button size="lg" variant="outline" onClick={() => setConfirming(true)}>
        {t('work.appointment.failed')}
      </Button>
    );
  }
  return (
    <form
      noValidate
      onSubmit={form.handleSubmit(onConfirm)}
      className="space-y-3 rounded-xl border-2 border-primary bg-card p-4"
    >
      <h3 ref={confirmRef} tabIndex={-1} className="font-semibold">
        {t('work.appointment.failedHeading')}
      </h3>
      <p>{t('work.appointment.failedText', { company })}</p>
      <FormField
        id="work-appointment-cause"
        label={t('work.appointment.cause')}
        required
        error={errors.cause?.message}
      >
        {(p) => (
          <NativeSelect
            {...p}
            {...form.register('cause', { required: { value: true, message: t('form.errors.required') } })}
          >
            <option value="">{t('form.select')}</option>
            {APPOINTMENT_FAILURE_CAUSES.map((c) => (
              <option key={c} value={c}>
                {t(`enums.appointmentFailureCause.${c}`)}
              </option>
            ))}
          </NativeSelect>
        )}
      </FormField>
      <FormField
        id="work-appointment-failed-note"
        label={t('work.appointment.failedNote')}
        hint={t('work.appointment.failedNoteHint')}
        error={errors.note?.message}
      >
        {(p) => (
          <Textarea
            {...p}
            rows={2}
            {...form.register('note', {
              validate: (v, all) => all.cause !== 'ALTRO' || v.trim() !== '' || t('form.errors.required'),
              maxLength: { value: 1000, message: t('form.errors.maxLength', { max: 1000 }) },
            })}
          />
        )}
      </FormField>
      <div className="flex flex-wrap gap-3">
        <Button type="submit" size="lg" disabled={failed.isPending}>
          {t('work.appointment.failedConfirm')}
        </Button>
        <Button
          type="button"
          size="lg"
          variant="outline"
          onClick={() => setConfirming(false)}
          disabled={failed.isPending}
        >
          {t('work.cancel')}
        </Button>
      </div>
    </form>
  );
};

/** Cronologia della segnalazione (US-305): lista ordinata dalla più vecchia, con data, autore, azione, stato e nota. */
const TicketHistory = ({ ticketId }: { ticketId: string }) => {
  const { t } = useTranslation();
  const history = useTicketHistory(ticketId);
  // dallo storico degli stati, un evento senza stato di partenza è la creazione: "Segnalazione inviata"
  const actionLabel = (e: TicketEvent): string => {
    const action = e.action === 'STATUS' && !e.fromStatus ? 'SUBMIT' : e.action;
    return (KNOWN_ACTIONS as readonly string[]).includes(action)
      ? t(`work.actions.${action as (typeof KNOWN_ACTIONS)[number]}`)
      : action;
  };
  const statusLabel = (s: TicketEvent['toStatus']): string =>
    s && (TICKET_STATUSES as readonly string[]).includes(s) ? t(`enums.ticketStatus.${s}`) : '';

  return (
    <section aria-labelledby="work-history" className="space-y-3 rounded-xl border border-border bg-card p-4">
      <h2 id="work-history" className="text-lg font-semibold text-primary">
        {t('work.sections.history')}
      </h2>
      {history.isError ? (
        <p role="alert" className="font-medium text-destructive">
          {t('common.loadError')}
        </p>
      ) : !history.data ? (
        <p>{t('common.loading')}</p>
      ) : history.data.length === 0 ? (
        <p>{t('work.historyEmpty')}</p>
      ) : (
        <ol className="space-y-3">
          {history.data.map((e, i) => (
            <li key={`${e.at}-${i}`} className="border-l-4 border-primary/40 pl-3">
              <p className="font-medium">
                {actionLabel(e)}
                {e.toStatus && (
                  <>
                    {' – '}
                    {e.fromStatus
                      ? t('work.statusChange', {
                          from: statusLabel(e.fromStatus),
                          to: statusLabel(e.toStatus),
                        })
                      : t('work.statusSet', { to: statusLabel(e.toStatus) })}
                  </>
                )}
              </p>
              {e.detail && <p>{e.detail}</p>}
              {e.note && <p className="italic">{t('work.note', { note: e.note })}</p>}
              <p className="text-sm text-muted-foreground">
                <time dateTime={e.at}>{dateTime.format(new Date(e.at))}</time> · {e.authorName}
              </p>
            </li>
          ))}
        </ol>
      )}
    </section>
  );
};

const TicketData = ({ ticket: d }: { ticket: TicketDetail }) => {
  const { t } = useTranslation();
  const beneficiaryLine = t('work.beneficiaryLine', {
    lastName: d.beneficiary.lastName,
    firstName: d.beneficiary.firstName,
    birthYear: d.beneficiary.birthYear,
  });
  return (
    <section aria-labelledby="work-data" className="space-y-3 rounded-xl border border-border bg-card p-4">
      <h2 id="work-data" className="text-lg font-semibold text-primary">
        {t('work.sections.data')}
      </h2>
      <dl className="grid gap-x-6 gap-y-2 sm:grid-cols-[max-content_1fr]">
        <dt className="font-medium">{t('work.fields.beneficiary')}</dt>
        <dd>
          {/* la scheda si apre con le stesse regole di visibilità della segnalazione (US-204) */}
          <Link
            to={`/beneficiari/${d.beneficiary.id}`}
            state={{ fromTicket: { id: d.id, number: d.number } } satisfies BeneficiaryLinkState}
            aria-label={t('work.openBeneficiary', { line: beneficiaryLine })}
            className="font-medium text-primary underline underline-offset-4"
          >
            {beneficiaryLine}
          </Link>
        </dd>
        <dt className="font-medium">{t('work.fields.zone')}</dt>
        <dd>{d.beneficiaryZone.name}</dd>
        <dt className="font-medium">{t('work.fields.project')}</dt>
        <dd>{d.project.name}</dd>
        <dt className="font-medium">{t('work.fields.requestedJob')}</dt>
        <dd>{d.requestedJobCategory?.name ?? d.requestedJobFreeText ?? '—'}</dd>
        <dt className="font-medium">{t('work.fields.tutor')}</dt>
        <dd>{d.tutorName}</dd>
        <dt className="font-medium">{t('work.fields.arrivedAt')}</dt>
        <dd>
          <time dateTime={d.createdAt}>{dateTime.format(new Date(d.createdAt))}</time>
        </dd>
        <dt className="font-medium">{t('work.fields.type')}</dt>
        <dd>
          {d.fastTrack ? (
            <span className="inline-flex items-center gap-1 rounded-full bg-gold px-2 py-0.5 text-xs font-semibold text-brand">
              <Zap aria-hidden="true" className="size-3.5" />
              {t('tickets.fastTrack')}
            </span>
          ) : (
            t('work.normal')
          )}
        </dd>
      </dl>
    </section>
  );
};

type CompatibleSectionProps = {
  ticket: TicketDetail;
  onOutcome: (outcome: Outcome) => void;
};

const CompatibleSection = ({ ticket: d, onOutcome }: CompatibleSectionProps) => {
  const { t } = useTranslation();
  const zones = useZones();
  const categories = useJobCategories();
  const [zoneId, setZoneId] = useState(d.beneficiaryZone.id);
  const [jobCategoryId, setJobCategoryId] = useState(d.requestedJobCategory?.id ?? ALL);
  const compatible = useCompatibleJobSlots(
    d.id,
    { zoneId: zoneId || undefined, jobCategoryId: jobCategoryId || undefined },
    true,
  );
  const match = useMatchTicket();
  const exclude = useExcludeCompany();
  const [candidate, setCandidate] = useState<JobSlotMatch | null>(null);
  const [details, setDetails] = useState<JobSlotMatch | null>(null);
  const detailsTrigger = useRef<HTMLButtonElement | null>(null);
  const confirmRef = useRef<HTMLHeadingElement>(null);

  useEffect(() => {
    if (candidate) confirmRef.current?.focus();
  }, [candidate]);

  const confirm = (slot: JobSlotMatch): void => {
    match.mutate(
      { id: d.id, jobSlotId: slot.id, ticketVersion: d.version, jobSlotVersion: slot.version },
      {
        onSuccess: () => onOutcome({ kind: 'proposed', title: slot.title, company: slot.companyName }),
        onError: (error) => {
          const body = apiError(error);
          onOutcome(body?.status === 409 ? { kind: 'conflict', reason: body.message } : { kind: 'error' });
        },
        onSettled: () => setCandidate(null),
      },
    );
  };

  const th = 'px-3 py-2 text-left text-sm font-semibold whitespace-nowrap';
  const td = 'px-3 py-2 align-top';
  const beneficiary = `${d.beneficiary.firstName} ${d.beneficiary.lastName}`;

  return (
    <section aria-labelledby="work-compatible" className="space-y-4">
      <h2 id="work-compatible" className="text-lg font-semibold text-primary">
        {t('work.sections.compatible')}
      </h2>

      <fieldset className="space-y-3 rounded-xl border border-border bg-card p-4">
        <legend className="px-1 font-semibold">{t('work.filters')}</legend>
        <p className="text-sm text-muted-foreground">{t('work.defaultHint')}</p>
        <div className="grid gap-4 md:grid-cols-2">
          <div className="space-y-1.5">
            <label htmlFor="work-zone" className="block text-sm font-medium">
              {t('work.zone')}
            </label>
            <NativeSelect id="work-zone" value={zoneId} onChange={(e) => setZoneId(e.target.value)}>
              <option value={ALL}>{t('work.allZones')}</option>
              {(zones.data ?? [d.beneficiaryZone]).map((z) => (
                <option key={z.id} value={z.id}>
                  {z.name}
                </option>
              ))}
            </NativeSelect>
          </div>
          <div className="space-y-1.5">
            <label htmlFor="work-category" className="block text-sm font-medium">
              {t('work.jobCategory')}
            </label>
            <NativeSelect
              id="work-category"
              value={jobCategoryId}
              onChange={(e) => setJobCategoryId(e.target.value)}
            >
              <option value={ALL}>{t('work.allCategories')}</option>
              {(categories.data ?? (d.requestedJobCategory ? [d.requestedJobCategory] : [])).map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </NativeSelect>
          </div>
        </div>
      </fieldset>

      <p role="status" className="font-medium">
        {compatible.data ? t('work.count', { count: compatible.data.length }) : ''}
      </p>

      {candidate && (
        <div className="space-y-3 rounded-xl border-2 border-primary bg-card p-4">
          <h3 ref={confirmRef} tabIndex={-1} className="font-semibold">
            {t('work.confirmHeading')}
          </h3>
          <p>
            {t('work.confirmText', { beneficiary, title: candidate.title, company: candidate.companyName })}
          </p>
          <div className="flex flex-wrap gap-3">
            <Button size="lg" onClick={() => confirm(candidate)} disabled={match.isPending}>
              {t('work.confirm')}
            </Button>
            <Button size="lg" variant="outline" onClick={() => setCandidate(null)} disabled={match.isPending}>
              {t('work.cancel')}
            </Button>
          </div>
        </div>
      )}

      {compatible.isError && !compatible.data ? (
        <p
          role="alert"
          className="rounded-md border-2 border-destructive bg-card p-4 font-medium text-destructive"
        >
          {t('common.loadError')}
        </p>
      ) : !compatible.data ? (
        <p>{t('common.loading')}</p>
      ) : compatible.data.length === 0 ? (
        <p className="rounded-xl border border-border bg-card p-6">{t('work.empty')}</p>
      ) : (
        <div
          role="region"
          aria-labelledby="work-compatible-caption"
          // eslint-disable-next-line jsx-a11y/no-noninteractive-tabindex -- regione scorrevole: deve essere raggiungibile da tastiera
          tabIndex={0}
          className="relative overflow-x-auto rounded-xl border border-border bg-card focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
        >
          <table className="w-full border-collapse text-sm max-lg:w-max max-lg:whitespace-nowrap">
            <caption id="work-compatible-caption" className="sr-only">
              {t('work.caption')}
            </caption>
            <thead className="border-b border-border bg-muted">
              <tr>
                <th scope="col" className={th}>
                  {t('work.fields.company')}
                </th>
                <th scope="col" className={th}>
                  {t('work.fields.jobSlot')}
                </th>
                <th scope="col" className={th}>
                  {t('work.fields.jobCategory')}
                </th>
                <th scope="col" className={th}>
                  {t('work.fields.zone')}
                </th>
                <th scope="col" className={th}>
                  {t('tickets.columns.actions')}
                </th>
              </tr>
            </thead>
            <tbody>
              {compatible.data.map((s) => (
                <tr key={s.id} className="border-b border-border last:border-b-0">
                  <td className={td}>{s.companyName}</td>
                  <th scope="row" className={`${td} text-left font-semibold`}>
                    {s.title}
                  </th>
                  <td className={td}>{s.jobCategory.name}</td>
                  <td className={td}>{s.zone.name}</td>
                  <td className={td}>
                    <div className="flex gap-2">
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={(e) => {
                          detailsTrigger.current = e.currentTarget;
                          setDetails(s);
                        }}
                      >
                        {t('work.details')}
                        {/* spazio come nodo a sé: nome accessibile "Dettagli Magazziniere presso Alfa" */}{' '}
                        <span className="sr-only">
                          {t('work.proposeTarget', { title: s.title, company: s.companyName })}
                        </span>
                      </Button>
                      <Button
                        size="sm"
                        onClick={() => setCandidate(s)}
                        disabled={candidate !== null || match.isPending}
                      >
                        {t('work.propose')}
                        {/* spazio come nodo a sé: nome accessibile "Proponi Magazziniere presso Alfa" */}{' '}
                        <span className="sr-only">
                          {t('work.proposeTarget', { title: s.title, company: s.companyName })}
                        </span>
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <MatchDetailsDialog
        slot={details}
        onClose={() => setDetails(null)}
        onPropose={(slot) => {
          setDetails(null);
          setCandidate(slot);
        }}
        proposeDisabled={candidate !== null || match.isPending}
        onExclude={async (slot, reason) => {
          await exclude.mutateAsync({ id: d.id, companyId: slot.companyId, reason });
          setDetails(null);
          onOutcome({ kind: 'excluded', company: slot.companyName });
        }}
        returnFocusRef={detailsTrigger}
      />

      <ExclusionsList ticketId={d.id} />
    </section>
  );
};

/** Aziende escluse per la segnalazione, con motivo, autore e data (US-603). */
const ExclusionsList = ({ ticketId }: { ticketId: string }) => {
  const { t } = useTranslation();
  const exclusions = useExclusions(ticketId);
  if (!exclusions.data) return null;
  return (
    <section
      aria-labelledby="work-exclusions"
      className="space-y-3 rounded-xl border border-border bg-card p-4"
    >
      <h3 id="work-exclusions" className="font-semibold text-primary">
        {t('exclusion.heading')}
      </h3>
      {exclusions.data.length === 0 ? (
        <p>{t('exclusion.empty')}</p>
      ) : (
        <ul className="space-y-2">
          {exclusions.data.map((x) => (
            <li key={x.companyId} className="border-l-4 border-primary/40 pl-3">
              <p className="font-medium">{x.companyName}</p>
              {x.reason && <p className="break-words">{x.reason}</p>}
              <p className="text-sm text-muted-foreground">
                {t('exclusion.meta', { date: dateTime.format(new Date(x.createdAt)), author: x.authorName })}
              </p>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
};
