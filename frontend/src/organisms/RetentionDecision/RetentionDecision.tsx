import { useId, useState, type FormEvent } from 'react';
import { useTranslation } from 'react-i18next';

import { Button } from '@/components/ui/button';
import { Dialog, DialogClose, DialogContent, DialogDescription, DialogTitle } from '@/components/ui/dialog';
import { Textarea } from '@/components/ui/textarea';
import { apiError } from '@/config/api/errors';
import { useAnonymizeBeneficiary, useKeepBeneficiary } from '@/config/hooks/useBeneficiaries';

const REASON_MAX = 500;

type RetentionDecisionProps = {
  id: string;
  /** Nome per i testi e per i nomi accessibili dei pulsanti. */
  name: string;
  /** Esito per il messaggio della pagina. */
  onDone: (message: string) => void;
};

/**
 * Decisione su un beneficiario da anonimizzare (US-1303): anonimizzarlo, dopo una conferma, oppure tenerne i dati per
 * sempre con un motivo obbligatorio.
 */
export const RetentionDecision = ({ id, name, onDone }: RetentionDecisionProps) => {
  const { t } = useTranslation();
  const [open, setOpen] = useState<'anonymize' | 'keep' | null>(null);

  return (
    <div className="flex flex-wrap gap-2">
      <Button
        type="button"
        variant="destructive"
        size="sm"
        aria-label={t('retention.anonymizeFor', { name })}
        onClick={() => setOpen('anonymize')}
      >
        {t('retention.anonymize')}
      </Button>
      <Button
        type="button"
        variant="outline"
        size="sm"
        aria-label={t('retention.keepFor', { name })}
        onClick={() => setOpen('keep')}
      >
        {t('retention.keep')}
      </Button>
      <AnonymizeDialog
        open={open === 'anonymize'}
        id={id}
        name={name}
        onClose={() => setOpen(null)}
        onDone={onDone}
      />
      <KeepDialog open={open === 'keep'} id={id} name={name} onClose={() => setOpen(null)} onDone={onDone} />
    </div>
  );
};

type DialogProps = RetentionDecisionProps & { open: boolean; onClose: () => void };

const failure = (e: unknown, fallback: string): string => {
  const body = apiError(e);
  return body?.fieldErrors?.[0]?.message ?? (body?.status === 400 && body.message ? body.message : fallback);
};

const AnonymizeDialog = ({ open, id, name, onClose, onDone }: DialogProps) => {
  const { t } = useTranslation();
  const anonymize = useAnonymizeBeneficiary();
  const [error, setError] = useState<string | null>(null);

  const onConfirm = async (): Promise<void> => {
    setError(null);
    try {
      await anonymize.mutateAsync(id);
      onClose();
      onDone(t('retention.anonymized', { name }));
    } catch (e) {
      setError(failure(e, t('retention.error')));
    }
  };

  return (
    <Dialog open={open} onOpenChange={(o) => !o && onClose()}>
      <DialogContent className="sm:max-w-xl">
        <DialogTitle>{t('retention.anonymizeTitle', { name })}</DialogTitle>
        <DialogDescription>{t('retention.anonymizeDescription')}</DialogDescription>
        {error && (
          <p role="alert" className="font-medium text-destructive">
            {error}
          </p>
        )}
        <div className="flex flex-wrap justify-end gap-3">
          <DialogClose asChild>
            <Button type="button" variant="outline">
              {t('common.cancel')}
            </Button>
          </DialogClose>
          <Button
            type="button"
            variant="destructive"
            disabled={anonymize.isPending}
            onClick={() => void onConfirm()}
          >
            {t('retention.anonymizeConfirm')}
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
};

const KeepDialog = ({ open, id, name, onClose, onDone }: DialogProps) => {
  const { t } = useTranslation();
  const keep = useKeepBeneficiary();
  const fieldId = useId();
  const errorId = useId();
  const [reason, setReason] = useState('');
  const [error, setError] = useState<string | null>(null);

  const onSubmit = async (event: FormEvent<HTMLFormElement>): Promise<void> => {
    event.preventDefault();
    if (reason.trim() === '') {
      setError(t('retention.reasonRequired'));
      return;
    }
    setError(null);
    try {
      await keep.mutateAsync({ id, reason: reason.trim() });
      onClose();
      onDone(t('retention.kept', { name }));
    } catch (e) {
      setError(failure(e, t('retention.error')));
    }
  };

  return (
    <Dialog open={open} onOpenChange={(o) => !o && onClose()}>
      <DialogContent className="sm:max-w-xl">
        <DialogTitle>{t('retention.keepTitle', { name })}</DialogTitle>
        <DialogDescription>{t('retention.keepDescription')}</DialogDescription>
        <form noValidate onSubmit={(e) => void onSubmit(e)} className="space-y-4">
          <div className="space-y-1">
            <label htmlFor={fieldId} className="block text-sm font-medium">
              {t('retention.reason')}
            </label>
            <Textarea
              id={fieldId}
              value={reason}
              maxLength={REASON_MAX}
              required
              aria-invalid={error ? true : undefined}
              aria-describedby={error ? errorId : undefined}
              onChange={(e) => setReason(e.target.value)}
            />
            {error && (
              <p id={errorId} role="alert" className="text-sm font-medium text-destructive">
                {error}
              </p>
            )}
          </div>
          <div className="flex flex-wrap justify-end gap-3">
            <DialogClose asChild>
              <Button type="button" variant="outline">
                {t('common.cancel')}
              </Button>
            </DialogClose>
            <Button type="submit" disabled={keep.isPending}>
              {t('retention.keepConfirm')}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};
