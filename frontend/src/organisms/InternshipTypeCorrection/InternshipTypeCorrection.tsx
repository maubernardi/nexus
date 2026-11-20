import { useEffect, useId, useRef, useState, type FormEvent } from 'react';
import { useTranslation } from 'react-i18next';

import { Button } from '@/components/ui/button';
import { NativeSelect } from '@/components/ui/native-select';
import { apiError } from '@/config/api/errors';
import type { TicketDetail } from '@/config/api/ticketApi';
import { useChangeInternshipType } from '@/config/hooks/useTickets';
import { useFocusAfterRender } from '@/hooks/useFocusAfterRender';
import { FormField } from '@/molecules/FormField/FormField';

type Feedback = { kind: 'status' | 'alert'; text: string };

/**
 * Tipo di tirocinio indicato o corretto dal Tutor della segnalazione, fino all'avvio (US-605), tra i tipi ammessi dal
 * progetto. Se la segnalazione ha una mansione, il tipo nuovo non deve esserci già: lo dice il server, sul campo.
 */
export const InternshipTypeCorrection = ({ ticket }: { ticket: TicketDetail }) => {
  const { t } = useTranslation();
  const fieldId = useId();
  const change = useChangeInternshipType();
  const [open, setOpen] = useState(false);
  const [value, setValue] = useState(ticket.internshipType?.id ?? '');
  const [error, setError] = useState<string | undefined>();
  const [feedback, setFeedback] = useState<Feedback | null>(null);
  const feedbackRef = useRef<HTMLParagraphElement>(null);
  const selectRef = useRef<HTMLSelectElement>(null);
  // il pulsante torna con la chiusura del modulo: il focus ci va dopo il render
  const { ref: toggleRef, requestFocus: focusToggle } = useFocusAfterRender<HTMLButtonElement>();
  const types = ticket.internshipTypes ?? [];

  useEffect(() => {
    if (open) selectRef.current?.focus();
  }, [open]);
  useEffect(() => {
    if (feedback) feedbackRef.current?.focus();
  }, [feedback]);

  const close = (): void => {
    setOpen(false);
    setError(undefined);
    focusToggle();
  };

  const submit = async (event: FormEvent<HTMLFormElement>): Promise<void> => {
    event.preventDefault();
    if (value === '') {
      setError(t('form.errors.required'));
      return;
    }
    try {
      const saved = await change.mutateAsync({ id: ticket.id, internshipTypeId: value });
      setOpen(false);
      setError(undefined);
      setFeedback({
        kind: 'status',
        text: t('work.internshipType.saved', { name: saved.internshipType?.name ?? '' }),
      });
    } catch (e) {
      const body = apiError(e);
      const fieldError = body?.fieldErrors?.[0]?.message;
      if (fieldError) {
        setError(fieldError);
      } else {
        setFeedback({ kind: 'alert', text: body?.message ?? t('form.errors.generic') });
      }
    }
  };

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
        <form noValidate onSubmit={(e) => void submit(e)} className="space-y-3">
          <FormField
            id={fieldId}
            label={t('ticket.fields.internshipType')}
            hint={t('ticket.hints.internshipType')}
            required
            error={error}
            className="max-w-md"
          >
            {(p) => (
              <NativeSelect
                {...p}
                ref={selectRef}
                value={value}
                onChange={(e) => {
                  setValue(e.target.value);
                  setError(undefined);
                }}
              >
                <option value="">{t('form.select')}</option>
                {types.map((type) => (
                  <option key={type.id} value={type.id}>
                    {type.name}
                  </option>
                ))}
              </NativeSelect>
            )}
          </FormField>
          <div className="flex flex-wrap gap-3">
            <Button type="submit" disabled={change.isPending}>
              {t('work.internshipType.save')}
            </Button>
            <Button type="button" variant="outline" onClick={close}>
              {t('work.internshipType.cancel')}
            </Button>
          </div>
        </form>
      ) : (
        <Button
          ref={toggleRef}
          type="button"
          variant="outline"
          size="sm"
          onClick={() => {
            setFeedback(null);
            setValue(ticket.internshipType?.id ?? '');
            setOpen(true);
          }}
        >
          {t(ticket.internshipType ? 'work.internshipType.change' : 'work.internshipType.indicate')}
        </Button>
      )}
    </div>
  );
};
