import { useEffect, useRef, useState } from 'react';
import { Plus } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';

import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
import { apiError } from '@/config/api/errors';
import { useAddCompanyNote, useCompanyNotes } from '@/config/hooks/useCompanyNotes';
import { FormField } from '@/molecules/FormField/FormField';

const MAX_LENGTH = 2000;
const dateTime = new Intl.DateTimeFormat('it-IT', { dateStyle: 'short', timeStyle: 'short' });

type NoteFormValues = { text: string };
type Outcome = 'added' | 'error';

/** Diario delle note dell'azienda (US-505): note datate con autore, che si aggiungono e non si modificano. */
export const CompanyNotesSection = ({ companyId }: { companyId: string }) => {
  const { t } = useTranslation();
  const notes = useCompanyNotes(companyId);
  const addNote = useAddCompanyNote();
  const [outcome, setOutcome] = useState<Outcome | null>(null);
  const outcomeRef = useRef<HTMLParagraphElement>(null);

  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<NoteFormValues>({ defaultValues: { text: '' } });

  // esito annunciato portandovi il focus
  useEffect(() => {
    if (outcome) outcomeRef.current?.focus();
  }, [outcome]);

  const onValid = async ({ text }: NoteFormValues): Promise<void> => {
    setOutcome(null);
    try {
      await addNote.mutateAsync({ companyId, text: text.trim() });
      reset();
      setOutcome('added');
    } catch (error) {
      const fieldError = apiError(error)?.fieldErrors?.find((fe) => fe.field === 'text');
      if (fieldError) {
        setError('text', { type: 'server', message: fieldError.message });
      } else {
        setOutcome('error');
      }
    }
  };

  return (
    <section
      aria-labelledby="company-notes"
      className="space-y-4 rounded-xl border border-border bg-card p-4"
    >
      <h2 id="company-notes" className="text-lg font-semibold text-primary">
        {t('companyNote.heading')}
      </h2>
      <p className="text-sm text-muted-foreground">{t('companyNote.intro')}</p>

      <form noValidate onSubmit={handleSubmit(onValid)} className="space-y-3">
        <FormField id="company-note-text" label={t('companyNote.text')} required error={errors.text?.message}>
          {(p) => (
            <Textarea
              {...p}
              rows={3}
              {...register('text', {
                validate: (v) => v.trim() !== '' || t('form.errors.required'),
                maxLength: { value: MAX_LENGTH, message: t('form.errors.maxLength', { max: MAX_LENGTH }) },
              })}
            />
          )}
        </FormField>
        <Button type="submit" size="lg" variant="outline" disabled={isSubmitting}>
          <Plus aria-hidden="true" />
          {isSubmitting ? t('companyNote.adding') : t('companyNote.add')}
        </Button>
      </form>

      {outcome && (
        <p
          ref={outcomeRef}
          tabIndex={-1}
          className={
            outcome === 'error'
              ? 'rounded-md border-2 border-destructive bg-card p-4 font-medium text-destructive'
              : 'rounded-md border-l-4 border-primary bg-card p-4 font-medium'
          }
        >
          {t(outcome === 'error' ? 'form.errors.generic' : 'companyNote.added')}
        </p>
      )}

      {notes.isError ? (
        <p role="alert" className="font-medium text-destructive">
          {t('common.loadError')}
        </p>
      ) : !notes.data ? (
        <p>{t('common.loading')}</p>
      ) : notes.data.length === 0 ? (
        <p>{t('companyNote.empty')}</p>
      ) : (
        <ol aria-label={t('companyNote.listLabel')} className="space-y-3">
          {notes.data.map((n) => (
            <li key={n.id} className="border-l-4 border-primary/40 pl-3">
              <p className="text-sm text-muted-foreground">
                <time dateTime={n.createdAt}>{dateTime.format(new Date(n.createdAt))}</time> · {n.authorName}
              </p>
              <p className="break-words whitespace-pre-line">{n.text}</p>
            </li>
          ))}
        </ol>
      )}
    </section>
  );
};
