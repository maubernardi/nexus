import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { NativeSelect } from '@/components/ui/native-select';
import { Textarea } from '@/components/ui/textarea';
import { CONTACT_OUTCOMES, type ContactOutcome } from '@/config/api/contactRequestApi';
import { apiError } from '@/config/api/errors';
import { useAddContactAttempt } from '@/config/hooks/useContactRequests';
import { useErrorSummaryFocus } from '@/hooks/useErrorSummaryFocus';
import { ErrorSummary, type SummaryError } from '@/molecules/ErrorSummary/ErrorSummary';
import { FormField } from '@/molecules/FormField/FormField';

type Values = { date: string; outcome: ContactOutcome | ''; note: string };

const FIELDS = ['date', 'outcome', 'note'] as const satisfies readonly (keyof Values)[];
const fieldId = (name: keyof Values): string => `contact-attempt-${name}`;
const MAX_NOTE = 500;

/** Oggi nel fuso del browser, aaaa-mm-gg: la data predefinita e il massimo del campo. */
const today = (): string => {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
};

/**
 * Nuovo tentativo di contatto (US-512), per l'operatore che ha la richiesta in carico: data (non futura), esito fisso
 * e nota facoltativa per il Tutor. Dopo il salvataggio il modulo si svuota e l'esito si annuncia.
 */
export const ContactAttemptForm = ({ requestId }: { requestId: string }) => {
  const { t } = useTranslation();
  const add = useAddContactAttempt();
  /** Esito dell'ultimo invio: salvato, oppure il messaggio d'errore. */
  const [notice, setNotice] = useState<{ kind: 'saved' } | { kind: 'error'; message: string } | null>(null);
  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors, submitCount },
  } = useForm<Values>({
    defaultValues: { date: today(), outcome: '', note: '' },
    mode: 'onSubmit',
    reValidateMode: 'onChange',
    shouldFocusError: false,
  });
  const { summaryRef, requestSummaryFocus } = useErrorSummaryFocus(submitCount, errors);

  const labels: Record<keyof Values, string> = {
    date: t('contactRequest.attemptForm.date'),
    outcome: t('contactRequest.attemptForm.outcome'),
    note: t('contactRequest.attemptForm.note'),
  };
  const summary: SummaryError[] = FIELDS.flatMap((field) => {
    const message = errors[field]?.message;
    return message ? [{ fieldId: fieldId(field), message: `${labels[field]}: ${message}` }] : [];
  });

  const onValid = (v: Values): void => {
    setNotice(null);
    add.mutate(
      {
        id: requestId,
        form: { date: v.date, outcome: v.outcome as ContactOutcome, note: v.note.trim() || null },
      },
      {
        onSuccess: () => {
          reset({ date: today(), outcome: '', note: '' });
          setNotice({ kind: 'saved' });
        },
        onError: (error) => {
          const body = apiError(error);
          const fieldErrors = (body?.fieldErrors ?? []).filter(
            (fe): fe is { field: keyof Values; message: string } =>
              (FIELDS as readonly string[]).includes(fe.field),
          );
          if (fieldErrors.length > 0) {
            fieldErrors.forEach((fe) => setError(fe.field, { type: 'server', message: fe.message }));
            requestSummaryFocus();
          } else {
            setNotice({
              kind: 'error',
              message:
                body?.status === 409 && body.message ? body.message : t('contactRequest.attemptForm.error'),
            });
          }
        },
      },
    );
  };

  return (
    <section
      aria-labelledby="contact-attempt-heading"
      className="space-y-4 rounded-xl border border-border bg-card p-4"
    >
      <h2 id="contact-attempt-heading" className="text-lg font-semibold text-primary">
        {t('contactRequest.attemptForm.heading')}
      </h2>
      <form noValidate onSubmit={handleSubmit(onValid, requestSummaryFocus)} className="max-w-xl space-y-4">
        {submitCount > 0 && <ErrorSummary ref={summaryRef} errors={summary} />}
        <div className="grid gap-4 sm:grid-cols-2">
          <FormField id={fieldId('date')} label={labels.date} required error={errors.date?.message}>
            {(p) => (
              <Input
                {...p}
                type="date"
                max={today()}
                {...register('date', {
                  required: t('contactRequest.attemptForm.errors.date'),
                  validate: (v) => v <= today() || t('contactRequest.attemptForm.errors.future'),
                })}
              />
            )}
          </FormField>
          <FormField id={fieldId('outcome')} label={labels.outcome} required error={errors.outcome?.message}>
            {(p) => (
              <NativeSelect
                {...p}
                {...register('outcome', { required: t('contactRequest.attemptForm.errors.outcome') })}
              >
                <option value="">{t('contactRequest.attemptForm.chooseOutcome')}</option>
                {CONTACT_OUTCOMES.map((o) => (
                  <option key={o} value={o}>
                    {t(`contactRequest.outcome.${o}`)}
                  </option>
                ))}
              </NativeSelect>
            )}
          </FormField>
        </div>
        <FormField
          id={fieldId('note')}
          label={labels.note}
          hint={t('contactRequest.attemptForm.noteHint')}
          error={errors.note?.message}
        >
          {(p) => (
            <Textarea
              {...p}
              rows={3}
              {...register('note', {
                maxLength: { value: MAX_NOTE, message: t('form.errors.maxLength', { max: MAX_NOTE }) },
              })}
            />
          )}
        </FormField>
        {notice && (
          <p
            role={notice.kind === 'saved' ? 'status' : 'alert'}
            className={notice.kind === 'saved' ? 'font-medium' : 'font-medium text-destructive'}
          >
            {notice.kind === 'saved' ? t('contactRequest.attemptForm.saved') : notice.message}
          </p>
        )}
        <Button type="submit" disabled={add.isPending} aria-busy={add.isPending}>
          {add.isPending ? t('contactRequest.attemptForm.saving') : t('contactRequest.attemptForm.submit')}
        </Button>
      </form>
    </section>
  );
};
