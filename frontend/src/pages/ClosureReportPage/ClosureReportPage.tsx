import { useEffect, useRef, useState } from 'react';
import { CircleAlert } from 'lucide-react';
import { useForm, useWatch, type UseFormRegisterReturn } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import { Link, useParams } from 'react-router';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { Button } from '@/components/ui/button';
import { NativeSelect } from '@/components/ui/native-select';
import { Textarea } from '@/components/ui/textarea';
import { apiError } from '@/config/api/errors';
import type { ClosureReportForm, TicketDetail } from '@/config/api/ticketApi';
import { useHiringFormulas, useNoHireReasons } from '@/config/hooks/useReferenceData';
import { useSubmitClosureReport, useTicket } from '@/config/hooks/useTickets';
import { useErrorSummaryFocus } from '@/hooks/useErrorSummaryFocus';
import { ErrorSummary, type SummaryError } from '@/molecules/ErrorSummary/ErrorSummary';
import { FormField } from '@/molecules/FormField/FormField';
import { ClosureReportView } from '@/organisms/ClosureReportView/ClosureReportView';

type YesNo = '' | 'yes' | 'no';

type ReportValues = {
  companySatisfaction: string;
  beneficiarySatisfaction: string;
  objectives: string;
  difficulties: string;
  hired: YesNo;
  hiringFormulaId: string;
  noHireReasonId: string;
  jobSlotStillAvailable: YesNo;
};

const EMPTY: ReportValues = {
  companySatisfaction: '',
  beneficiarySatisfaction: '',
  objectives: '',
  difficulties: '',
  hired: '',
  hiringFormulaId: '',
  noHireReasonId: '',
  jobSlotStillAvailable: '',
};

const FIELDS = [
  'companySatisfaction',
  'beneficiarySatisfaction',
  'hired',
  'hiringFormulaId',
  'noHireReasonId',
  'jobSlotStillAvailable',
  'objectives',
  'difficulties',
] as const satisfies readonly (keyof ReportValues)[];

const SCORES = ['1', '2', '3', '4', '5'] as const;
const MAX_TEXT = 2000;

const fieldId = (name: keyof ReportValues): string => `closure-${name}`;
/** Le scelte a pallini si raggiungono dal riepilogo degli errori tramite la prima opzione. */
const RADIO_FIELDS: readonly (keyof ReportValues)[] = [
  'companySatisfaction',
  'beneficiarySatisfaction',
  'hired',
  'jobSlotStillAvailable',
];
const focusId = (name: keyof ReportValues): string =>
  RADIO_FIELDS.includes(name) ? `${fieldId(name)}-0` : fieldId(name);

const toPayload = (v: ReportValues): ClosureReportForm => ({
  companySatisfaction: Number(v.companySatisfaction),
  beneficiarySatisfaction: Number(v.beneficiarySatisfaction),
  objectives: v.objectives.trim() || null,
  difficulties: v.difficulties.trim() || null,
  hired: v.hired === 'yes',
  // conta solo la voce dell'esito scelto
  hiringFormulaId: v.hired === 'yes' ? v.hiringFormulaId : null,
  noHireReasonId: v.hired === 'no' ? v.noHireReasonId : null,
  jobSlotStillAvailable: v.jobSlotStillAvailable === 'yes',
});

/**
 * Report di restituzione (US-1101), solo per il Tutor della segnalazione durante il tirocinio: gradimenti, esito
 * dell'assunzione e mansione ancora disponibile. Con l'invio, dopo una conferma, il percorso si chiude e il report non
 * si modifica più; inviato, la pagina lo mostra in sola lettura.
 */
export const ClosureReportPage = () => {
  const { t } = useTranslation();
  const { id = '' } = useParams();
  const ticket = useTicket(id);
  // la risposta dell'invio ha già il report: lo si mostra senza aspettare il nuovo caricamento
  const [sent, setSent] = useState<TicketDetail | null>(null);
  const sentRef = useRef<HTMLParagraphElement>(null);

  useEffect(() => {
    if (sent) sentRef.current?.focus();
  }, [sent]);

  const backLink = (
    <Link
      to={`/segnalazioni/${id}`}
      className="inline-block font-medium text-primary underline underline-offset-4"
    >
      {t('closureReport.backToTicket')}
    </Link>
  );

  if (ticket.isError) {
    return (
      <div className="space-y-4">
        <PageHeading>
          {apiError(ticket.error)?.status === 404 ? t('work.notFound') : t('closureReport.loadError')}
        </PageHeading>
        {backLink}
      </div>
    );
  }
  const d = sent ?? ticket.data;
  if (!d) {
    return <p role="status">{t('common.loading')}</p>;
  }

  if (d.closureReport) {
    return (
      <div className="space-y-6">
        <PageHeading>{t('closureReport.heading', { number: d.number })}</PageHeading>
        {sent && (
          <p
            ref={sentRef}
            role="status"
            tabIndex={-1}
            className="rounded-md border-l-4 border-primary bg-card p-4 font-medium"
          >
            {t('closureReport.sent', { number: d.number })}
          </p>
        )}
        <ClosureReportView report={d.closureReport} />
        {backLink}
      </div>
    );
  }
  if (!d.myTicket || d.status !== 'IN_TIROCINIO') {
    return (
      <div className="space-y-4">
        <PageHeading>{t('closureReport.notAvailable')}</PageHeading>
        {backLink}
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <PageHeading>{t('closureReport.heading', { number: d.number })}</PageHeading>
      <p className="text-muted-foreground">
        {t('closureReport.who', {
          beneficiary: `${d.beneficiary.firstName} ${d.beneficiary.lastName}`,
          company: d.proposal?.companyName ?? '',
        })}
      </p>
      <p>{t('closureReport.intro')}</p>
      <ReportForm ticket={d} onSent={setSent} />
      {backLink}
    </div>
  );
};

type ReportFormProps = {
  ticket: TicketDetail;
  onSent: (ticket: TicketDetail) => void;
};

const ReportForm = ({ ticket: d, onSent }: ReportFormProps) => {
  const { t } = useTranslation();
  const formulas = useHiringFormulas();
  const reasons = useNoHireReasons();
  const submit = useSubmitClosureReport();
  // valori controllati, in attesa di conferma: durante la conferma i campi sono bloccati
  const [pending, setPending] = useState<ReportValues | null>(null);
  const [failure, setFailure] = useState<string | null>(null);
  const confirmRef = useRef<HTMLHeadingElement>(null);
  const failureRef = useRef<HTMLDivElement>(null);
  const {
    register,
    handleSubmit,
    setError,
    control,
    formState: { errors, submitCount },
  } = useForm<ReportValues>({
    defaultValues: EMPTY,
    mode: 'onSubmit',
    reValidateMode: 'onChange',
    // il focus va al riepilogo degli errori (non al primo campo): lo gestisce useErrorSummaryFocus
    shouldFocusError: false,
  });
  const { summaryRef, requestSummaryFocus } = useErrorSummaryFocus(submitCount, errors);
  const hired = useWatch({ control, name: 'hired' });

  useEffect(() => {
    if (pending) confirmRef.current?.focus();
  }, [pending]);
  useEffect(() => {
    if (failure) failureRef.current?.focus();
  }, [failure]);

  const required = { value: true, message: t('form.errors.required') };
  const maxText = { value: MAX_TEXT, message: t('form.errors.maxLength', { max: MAX_TEXT }) };
  const yesNo = [
    { value: 'yes', label: t('beneficiary.yes') },
    { value: 'no', label: t('beneficiary.no') },
  ];
  const scores = SCORES.map((s) => ({ value: s, label: s }));

  const summary: SummaryError[] = FIELDS.flatMap((name) => {
    const message = errors[name]?.message;
    return message
      ? [{ fieldId: focusId(name), message: `${t(`closureReport.fields.${name}`)}: ${message}` }]
      : [];
  });

  const onConfirm = async (): Promise<void> => {
    if (!pending) return;
    try {
      onSent(await submit.mutateAsync({ id: d.id, version: d.version, report: toPayload(pending) }));
    } catch (error) {
      const body = apiError(error);
      const fieldErrors = (body?.fieldErrors ?? []).filter(
        (fe): fe is typeof fe & { field: keyof ReportValues } =>
          (FIELDS as readonly string[]).includes(fe.field),
      );
      setPending(null);
      if (fieldErrors.length > 0) {
        fieldErrors.forEach((fe) => setError(fe.field, { type: 'server', message: fe.message }));
        requestSummaryFocus();
        return;
      }
      setFailure(
        body?.status === 409
          ? t('closureReport.conflict', { reason: body.message })
          : t('closureReport.error'),
      );
    }
  };

  const onValid = (values: ReportValues): void => {
    setFailure(null);
    setPending(values);
  };

  const noFormulas = formulas.data?.length === 0;
  const noReasons = reasons.data?.length === 0;

  return (
    <form
      noValidate
      onSubmit={handleSubmit(onValid, requestSummaryFocus)}
      className="space-y-6 rounded-xl border border-border bg-card p-4"
    >
      {submitCount > 0 && <ErrorSummary ref={summaryRef} errors={summary} />}
      {failure && (
        <div
          ref={failureRef}
          role="alert"
          tabIndex={-1}
          className="rounded-md border-2 border-destructive bg-card p-4 font-medium text-destructive"
        >
          {failure}
        </div>
      )}

      {/* durante la conferma i campi non si cambiano: si invia ciò che è stato controllato */}
      <fieldset disabled={pending !== null} className="space-y-6">
        <RadioField
          name="companySatisfaction"
          legend={t('closureReport.fields.companySatisfaction')}
          hint={t('closureReport.hints.scale')}
          options={scores}
          error={errors.companySatisfaction?.message}
          registration={register('companySatisfaction', { required })}
        />
        <RadioField
          name="beneficiarySatisfaction"
          legend={t('closureReport.fields.beneficiarySatisfaction')}
          hint={t('closureReport.hints.scale')}
          options={scores}
          error={errors.beneficiarySatisfaction?.message}
          registration={register('beneficiarySatisfaction', { required })}
        />
        <RadioField
          name="hired"
          legend={t('closureReport.fields.hired')}
          options={yesNo}
          error={errors.hired?.message}
          registration={register('hired', { required })}
        />
        {hired === 'yes' && (
          <FormField
            id={fieldId('hiringFormulaId')}
            label={t('closureReport.fields.hiringFormulaId')}
            required
            hint={noFormulas ? t('closureReport.noOptions') : undefined}
            error={errors.hiringFormulaId?.message}
          >
            {(p) => (
              <NativeSelect
                {...p}
                {...register('hiringFormulaId', {
                  validate: (v, all) => all.hired !== 'yes' || v !== '' || t('form.errors.required'),
                })}
              >
                <option value="">{t('form.select')}</option>
                {formulas.data?.map((f) => (
                  <option key={f.id} value={f.id}>
                    {f.name}
                  </option>
                ))}
              </NativeSelect>
            )}
          </FormField>
        )}
        {hired === 'no' && (
          <FormField
            id={fieldId('noHireReasonId')}
            label={t('closureReport.fields.noHireReasonId')}
            required
            hint={noReasons ? t('closureReport.noOptions') : undefined}
            error={errors.noHireReasonId?.message}
          >
            {(p) => (
              <NativeSelect
                {...p}
                {...register('noHireReasonId', {
                  validate: (v, all) => all.hired !== 'no' || v !== '' || t('form.errors.required'),
                })}
              >
                <option value="">{t('form.select')}</option>
                {reasons.data?.map((r) => (
                  <option key={r.id} value={r.id}>
                    {r.name}
                  </option>
                ))}
              </NativeSelect>
            )}
          </FormField>
        )}
        <RadioField
          name="jobSlotStillAvailable"
          legend={t('closureReport.fields.jobSlotStillAvailable')}
          hint={t('closureReport.hints.jobSlotStillAvailable')}
          options={yesNo}
          error={errors.jobSlotStillAvailable?.message}
          registration={register('jobSlotStillAvailable', { required })}
        />
        <FormField
          id={fieldId('objectives')}
          label={t('closureReport.fields.objectives')}
          hint={t('closureReport.hints.texts')}
          error={errors.objectives?.message}
        >
          {(p) => <Textarea {...p} rows={4} {...register('objectives', { maxLength: maxText })} />}
        </FormField>
        <FormField
          id={fieldId('difficulties')}
          label={t('closureReport.fields.difficulties')}
          hint={t('closureReport.hints.texts')}
          error={errors.difficulties?.message}
        >
          {(p) => <Textarea {...p} rows={4} {...register('difficulties', { maxLength: maxText })} />}
        </FormField>
      </fieldset>

      {pending ? (
        <div className="space-y-3 rounded-xl border-2 border-primary p-4">
          <h2 ref={confirmRef} tabIndex={-1} className="font-semibold">
            {t('closureReport.confirmHeading')}
          </h2>
          <p>{t('closureReport.confirmText', { number: d.number })}</p>
          <div className="flex flex-wrap gap-3">
            <Button type="button" size="lg" onClick={() => void onConfirm()} disabled={submit.isPending}>
              {t('closureReport.confirm')}
            </Button>
            <Button
              type="button"
              size="lg"
              variant="outline"
              onClick={() => setPending(null)}
              disabled={submit.isPending}
            >
              {t('closureReport.back')}
            </Button>
          </div>
        </div>
      ) : (
        <Button type="submit" size="lg">
          {t('closureReport.submit')}
        </Button>
      )}
    </form>
  );
};

type RadioFieldProps = {
  name: keyof ReportValues;
  legend: string;
  hint?: string;
  options: readonly { value: string; label: string }[];
  error?: string;
  registration: UseFormRegisterReturn;
};

/** Scelta obbligatoria a pallini: legenda, aiuto ed errore collegati al gruppo (WCAG 1.3.1, 3.3.1, 3.3.2). */
const RadioField = ({ name, legend, hint, options, error, registration }: RadioFieldProps) => {
  const { t } = useTranslation();
  const id = fieldId(name);
  const hintId = hint ? `${id}-hint` : undefined;
  const errorId = error ? `${id}-error` : undefined;
  return (
    <fieldset
      className="space-y-2"
      aria-describedby={[hintId, errorId].filter(Boolean).join(' ') || undefined}
    >
      <legend className="text-sm font-medium">
        {legend}
        {/* spazio come nodo di testo a sé, come in FormField */}{' '}
        <span className="font-normal text-muted-foreground">({t('form.required')})</span>
      </legend>
      {hint && (
        <p id={hintId} className="text-sm text-muted-foreground">
          {hint}
        </p>
      )}
      <div className="flex flex-wrap gap-x-6 gap-y-1">
        {options.map((option, index) => (
          <label key={option.value} htmlFor={`${id}-${index}`} className="flex min-h-11 items-center gap-2">
            <input
              id={`${id}-${index}`}
              type="radio"
              value={option.value}
              className="size-5 accent-primary"
              {...registration}
            />
            <span>{option.label}</span>
          </label>
        ))}
      </div>
      {error && (
        <p id={errorId} className="flex items-start gap-1.5 text-sm font-medium text-destructive">
          <CircleAlert aria-hidden="true" className="mt-0.5 size-4 shrink-0" />
          {error}
        </p>
      )}
    </fieldset>
  );
};
