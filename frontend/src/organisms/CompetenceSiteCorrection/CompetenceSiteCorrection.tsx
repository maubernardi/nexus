import { useEffect, useId, useRef, useState, type FormEvent } from 'react';
import { useTranslation } from 'react-i18next';

import { Button } from '@/components/ui/button';
import { NativeSelect } from '@/components/ui/native-select';
import { apiError } from '@/config/api/errors';
import type { TicketDetail } from '@/config/api/ticketApi';
import { useProjectCompetenceSites } from '@/config/hooks/useReferenceData';
import { useChangeCompetenceSite } from '@/config/hooks/useTickets';
import { useFocusAfterRender } from '@/hooks/useFocusAfterRender';
import { FormField } from '@/molecules/FormField/FormField';

type Feedback = { kind: 'status' | 'alert'; text: string };

/**
 * Correzione della sede di competenza (US-308): solo l'ADMIN e il Coordinatore del progetto, tra le sedi attive del
 * progetto della segnalazione. La correzione finisce nella cronologia.
 */
export const CompetenceSiteCorrection = ({ ticket }: { ticket: TicketDetail }) => {
  const { t } = useTranslation();
  const fieldId = useId();
  const sites = useProjectCompetenceSites(ticket.project.id);
  const change = useChangeCompetenceSite();
  const [open, setOpen] = useState(false);
  const [value, setValue] = useState(ticket.competenceSite?.id ?? '');
  const [error, setError] = useState<string | undefined>();
  const [feedback, setFeedback] = useState<Feedback | null>(null);
  const feedbackRef = useRef<HTMLParagraphElement>(null);
  const selectRef = useRef<HTMLSelectElement>(null);
  // il pulsante torna con la chiusura del modulo: il focus ci va dopo il render
  const { ref: toggleRef, requestFocus: focusToggle } = useFocusAfterRender<HTMLButtonElement>();

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
      const saved = await change.mutateAsync({ id: ticket.id, competenceSiteId: value });
      setOpen(false);
      setError(undefined);
      setFeedback({
        kind: 'status',
        text: t('work.competenceSite.saved', { name: saved.competenceSite?.name ?? '' }),
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
            label={t('work.competenceSite.label')}
            hint={t('work.competenceSite.hint')}
            required
            error={error}
            className="max-w-md"
          >
            {(p) => (
              <NativeSelect
                {...p}
                ref={selectRef}
                value={value}
                disabled={!sites.data}
                onChange={(e) => {
                  setValue(e.target.value);
                  setError(undefined);
                }}
              >
                <option value="">{t('form.select')}</option>
                {(sites.data ?? []).map((site) => (
                  <option key={site.id} value={site.id}>
                    {site.name}
                  </option>
                ))}
              </NativeSelect>
            )}
          </FormField>
          <div className="flex flex-wrap gap-3">
            <Button type="submit" disabled={change.isPending}>
              {t('work.competenceSite.save')}
            </Button>
            <Button type="button" variant="outline" onClick={close}>
              {t('work.competenceSite.cancel')}
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
            setValue(ticket.competenceSite?.id ?? '');
            setOpen(true);
          }}
        >
          {t('work.competenceSite.change')}
        </Button>
      )}
    </div>
  );
};
