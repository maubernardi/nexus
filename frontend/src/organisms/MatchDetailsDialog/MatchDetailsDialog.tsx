import { useRef, useState, type ReactNode, type RefObject } from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router';

import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
import { Dialog, DialogClose, DialogContent, DialogDescription, DialogTitle } from '@/components/ui/dialog';
import { apiError } from '@/config/api/errors';
import type { JobSlotMatch } from '@/config/api/ticketApi';
import { useCompany } from '@/config/hooks/useCompanies';
import { useCompanyNotes } from '@/config/hooks/useCompanyNotes';
import { FormField } from '@/molecules/FormField/FormField';
import { CompanyHistorySection } from '@/organisms/CompanyHistorySection/CompanyHistorySection';

const RECENT_NOTES = 3;
const RECENT_EVENTS = 5;
const dateTime = new Intl.DateTimeFormat('it-IT', { dateStyle: 'short', timeStyle: 'short' });

const Card = ({ title, children }: { title: string; children: ReactNode }) => (
  <section className="space-y-3 rounded-xl border border-border bg-card p-4">
    <h3 className="text-lg font-semibold text-primary">{title}</h3>
    {children}
  </section>
);

const Fields = ({ rows }: { rows: [string, ReactNode][] }) => (
  <dl className="grid gap-x-6 gap-y-2 sm:grid-cols-[max-content_1fr]">
    {rows.map(([label, value]) => (
      <div key={label} className="contents">
        <dt className="text-sm font-medium text-muted-foreground">{label}</dt>
        <dd className="break-words whitespace-pre-line">{value}</dd>
      </div>
    ))}
  </dl>
);

type MatchDetailsDialogProps = {
  /** Mansione di cui mostrare i dettagli; null = dialogo chiuso. */
  slot: JobSlotMatch | null;
  onClose: () => void;
  /** "Proponi" dal dialogo: apre la stessa conferma della tabella. */
  onPropose: (slot: JobSlotMatch) => void;
  proposeDisabled?: boolean;
  /** "Escludi questa azienda" (US-603): esclude l'azienda per la segnalazione, con il motivo. */
  onExclude?: (slot: JobSlotMatch, reason: string) => Promise<void>;
  /** Pulsante da cui si è aperto il dialogo: alla chiusura il focus torna lì (salvo proposta). */
  returnFocusRef?: RefObject<HTMLElement | null>;
};

/**
 * Dettagli durante l'abbinamento (US-604): scheda dell'azienda e della mansione, note più recenti (US-505) e pregressi
 * (US-503), per scegliere la proposta con tutte le informazioni.
 */
export const MatchDetailsDialog = ({
  slot,
  onClose,
  onPropose,
  proposeDisabled = false,
  onExclude,
  returnFocusRef,
}: MatchDetailsDialogProps) => {
  const { t } = useTranslation();
  // proponendo o escludendo, il focus va all'esito e non torna sul pulsante "Dettagli"
  const proposing = useRef(false);
  // il contenuto resta montato durante la chiusura: Radix può così riportare il focus sul pulsante di partenza
  const [shown, setShown] = useState(slot);
  if (slot && slot !== shown) setShown(slot);

  return (
    <Dialog open={slot !== null} onOpenChange={(open) => !open && onClose()}>
      {shown && (
        <DialogContent
          onCloseAutoFocus={(event) => {
            // focus gestito qui: alla conferma se si propone, altrimenti sul pulsante di partenza
            event.preventDefault();
            if (proposing.current) {
              proposing.current = false;
            } else {
              returnFocusRef?.current?.focus();
            }
          }}
        >
          <DialogTitle>
            {t('matchDetails.title', { title: shown.title, company: shown.companyName })}
          </DialogTitle>
          <DialogDescription>{t('matchDetails.description')}</DialogDescription>
          <MatchDetailsBody slot={shown} />
          <div className="flex flex-wrap gap-3 border-t border-border pt-4">
            <Button
              size="lg"
              disabled={proposeDisabled}
              onClick={() => {
                proposing.current = true;
                onPropose(shown);
              }}
            >
              {t('work.propose')}
            </Button>
            <DialogClose asChild>
              <Button size="lg" variant="outline">
                {t('matchDetails.close')}
              </Button>
            </DialogClose>
          </div>
          {onExclude && (
            <ExcludeCompany
              key={shown.id}
              company={shown.companyName}
              onExclude={async (reason) => {
                proposing.current = true;
                try {
                  await onExclude(shown, reason);
                } catch (error) {
                  proposing.current = false;
                  throw error;
                }
              }}
            />
          )}
        </DialogContent>
      )}
    </Dialog>
  );
};

/** Esclusione dell'azienda dalla finestra dei dettagli, con il motivo obbligatorio (US-603). */
const ExcludeCompany = ({
  company,
  onExclude,
}: {
  company: string;
  onExclude: (reason: string) => Promise<void>;
}) => {
  const { t } = useTranslation();
  const [open, setOpen] = useState(false);
  const [reason, setReason] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [pending, setPending] = useState(false);

  if (!open) {
    return (
      <Button variant="outline" className="self-start" onClick={() => setOpen(true)}>
        {t('exclusion.action')}
      </Button>
    );
  }
  const submit = async (): Promise<void> => {
    if (reason.trim() === '') {
      setError(t('form.errors.required'));
      return;
    }
    setPending(true);
    try {
      await onExclude(reason.trim());
    } catch (e) {
      setError(apiError(e)?.message ?? t('form.errors.generic'));
      setPending(false);
    }
  };
  return (
    <div className="space-y-3 rounded-xl border-2 border-primary p-4">
      <p>{t('exclusion.text', { company })}</p>
      <FormField
        id="match-exclusion-reason"
        label={t('exclusion.reason')}
        required
        error={error ?? undefined}
      >
        {(p) => (
          <Textarea
            {...p}
            rows={2}
            maxLength={500}
            value={reason}
            onChange={(e) => {
              setReason(e.target.value);
              setError(null);
            }}
          />
        )}
      </FormField>
      <div className="flex flex-wrap gap-3">
        <Button onClick={() => void submit()} disabled={pending}>
          {t('exclusion.confirm')}
        </Button>
        <Button variant="outline" onClick={() => setOpen(false)} disabled={pending}>
          {t('work.cancel')}
        </Button>
      </div>
    </div>
  );
};

const MatchDetailsBody = ({ slot }: { slot: JobSlotMatch }) => {
  const { t } = useTranslation();
  const company = useCompany(slot.companyId);
  const notes = useCompanyNotes(slot.companyId);
  const none = t('matchDetails.notSpecified');
  const c = company.data;

  return (
    <div className="space-y-4">
      <Card title={t('matchDetails.company')}>
        {company.isError ? (
          <p role="alert" className="font-medium text-destructive">
            {t('common.loadError')}
          </p>
        ) : !c ? (
          <p>{t('common.loading')}</p>
        ) : (
          <Fields
            rows={[
              [t('company.fields.name'), c.name],
              [t('company.fields.vatCode'), c.vatCode],
              [t('company.fields.legalAddress'), c.legalAddress ?? none],
              [t('company.fields.contactPerson'), c.contactPerson ?? none],
              [t('company.fields.phone'), c.phone ?? none],
              [t('company.fields.email'), c.email ?? none],
              [t('company.status'), t(c.active ? 'company.active' : 'company.inactive')],
            ]}
          />
        )}
      </Card>

      <Card title={t('matchDetails.jobSlot')}>
        <Fields
          rows={[
            [t('work.fields.jobSlot'), slot.title],
            [t('work.fields.jobCategory'), slot.jobCategory.name],
            [t('work.zone'), slot.zone.name],
            [t('matchDetails.jobDescription'), slot.description ?? none],
          ]}
        />
      </Card>

      <Card title={t('matchDetails.notes')}>
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
            {notes.data.slice(0, RECENT_NOTES).map((n) => (
              <li key={n.id} className="border-l-4 border-primary/40 pl-3">
                <p className="text-sm text-muted-foreground">
                  <time dateTime={n.createdAt}>{dateTime.format(new Date(n.createdAt))}</time> ·{' '}
                  {n.authorName}
                </p>
                <p className="break-words whitespace-pre-line">{n.text}</p>
              </li>
            ))}
          </ol>
        )}
        <Link
          to={`/aziende/${slot.companyId}`}
          className="inline-block font-medium text-primary underline underline-offset-4"
        >
          {t('matchDetails.openCompany')}
        </Link>
      </Card>

      <CompanyHistorySection companyId={slot.companyId} headingLevel={3} maxEvents={RECENT_EVENTS} />
    </div>
  );
};
