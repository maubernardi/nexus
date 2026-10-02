import { useEffect, useRef, useState } from 'react';
import { Plus } from 'lucide-react';
import { useTranslation } from 'react-i18next';

import { Button } from '@/components/ui/button';
import { apiError } from '@/config/api/errors';
import type { JobSlot } from '@/config/api/jobSlotApi';
import {
  useCreateJobSlot,
  useJobSlots,
  useSetJobSlotActive,
  useUpdateJobSlot,
} from '@/config/hooks/useJobSlots';
import { JobSlotForm } from '@/organisms/JobSlotForm/JobSlotForm';

type OutcomeKey =
  | 'jobSlot.outcome.created'
  | 'jobSlot.outcome.saved'
  | 'jobSlot.outcome.activated'
  | 'jobSlot.outcome.deactivated'
  | 'jobSlot.outcome.conflict'
  | 'jobSlot.outcome.refused'
  | 'form.errors.generic';

type Outcome = { key: OutcomeKey; params?: Record<string, string | number>; error?: boolean };

type JobSlotsSectionProps = {
  companyId: string;
  companyActive: boolean;
};

/** Mansioni dell'azienda nella scheda azienda (US-502): elenco, aggiunta, modifica, ritiro. */
export const JobSlotsSection = ({ companyId, companyActive }: JobSlotsSectionProps) => {
  const { t } = useTranslation();
  const slots = useJobSlots(companyId);
  const createSlot = useCreateJobSlot();
  const updateSlot = useUpdateJobSlot();
  const setActive = useSetJobSlotActive();
  const [editing, setEditing] = useState<JobSlot | 'new' | null>(null);
  const [outcome, setOutcome] = useState<Outcome | null>(null);
  const formHeadingRef = useRef<HTMLHeadingElement>(null);
  const outcomeRef = useRef<HTMLDivElement>(null);

  // all'apertura del modulo il focus va al suo titolo; dopo un'azione, all'esito
  useEffect(() => {
    if (editing) formHeadingRef.current?.focus();
  }, [editing]);
  useEffect(() => {
    if (outcome) outcomeRef.current?.focus();
  }, [outcome]);

  const open = (target: JobSlot | 'new'): void => {
    setOutcome(null);
    setEditing(target);
  };
  const close = (result: Outcome): void => {
    setEditing(null);
    setOutcome(result);
  };

  const toggle = (slot: JobSlot): void => {
    setOutcome(null);
    setActive.mutate(
      { id: slot.id, version: slot.version, active: !slot.active },
      {
        onSuccess: (s) =>
          setOutcome({
            key: s.active ? 'jobSlot.outcome.activated' : 'jobSlot.outcome.deactivated',
            params: { title: s.title },
          }),
        onError: (error) => {
          const body = apiError(error);
          setOutcome(
            body?.status === 409
              ? { key: 'jobSlot.outcome.refused', params: { reason: body.message }, error: true }
              : { key: 'form.errors.generic', error: true },
          );
        },
      },
    );
  };

  const th = 'px-3 py-2 text-left text-sm font-semibold whitespace-nowrap';
  const td = 'px-3 py-2 align-top';

  return (
    <section aria-labelledby="job-slots-heading" className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h2 id="job-slots-heading" className="text-lg font-semibold text-primary">
          {t('jobSlot.heading')}
        </h2>
        {companyActive && editing === null && (
          <Button size="lg" onClick={() => open('new')}>
            <Plus aria-hidden="true" />
            {t('jobSlot.add')}
          </Button>
        )}
      </div>
      <p className="text-muted-foreground">
        {t(companyActive ? 'jobSlot.intro' : 'jobSlot.companyInactive')}
      </p>

      {outcome && (
        <div
          ref={outcomeRef}
          tabIndex={-1}
          className={
            outcome.error
              ? 'rounded-md border-2 border-destructive bg-card p-4 font-medium text-destructive'
              : 'rounded-md border-l-4 border-primary bg-card p-4 font-medium'
          }
        >
          {t(outcome.key, outcome.params)}
        </div>
      )}

      {editing && (
        <div className="space-y-4 rounded-xl border border-border bg-card p-4">
          <h3 ref={formHeadingRef} tabIndex={-1} className="text-base font-semibold">
            {editing === 'new' ? t('jobSlot.newHeading') : t('jobSlot.editHeading', { title: editing.title })}
          </h3>
          <JobSlotForm
            key={editing === 'new' ? 'new' : `${editing.id}-${editing.version}`}
            slot={editing === 'new' ? undefined : editing}
            onSubmit={(payload) =>
              editing === 'new'
                ? createSlot.mutateAsync({ companyId, payload })
                : updateSlot.mutateAsync({ id: editing.id, payload })
            }
            onSuccess={(s) =>
              close({
                key: editing === 'new' ? 'jobSlot.outcome.created' : 'jobSlot.outcome.saved',
                params: { title: s.title },
              })
            }
            onCancel={() => setEditing(null)}
            onConflict={() => close({ key: 'jobSlot.outcome.conflict', error: true })}
          />
        </div>
      )}

      {slots.isError && !slots.data ? (
        <p
          role="alert"
          className="rounded-md border-2 border-destructive bg-card p-4 font-medium text-destructive"
        >
          {t('jobSlot.loadError')}
        </p>
      ) : !slots.data ? (
        <p>{t('queue.loading')}</p>
      ) : slots.data.length === 0 ? (
        <p className="rounded-xl border border-border bg-card p-6">{t('jobSlot.empty')}</p>
      ) : (
        <div
          role="region"
          aria-labelledby="job-slots-caption"
          // eslint-disable-next-line jsx-a11y/no-noninteractive-tabindex -- regione scorrevole: deve essere raggiungibile da tastiera
          tabIndex={0}
          className="relative overflow-x-auto rounded-xl border border-border bg-card focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
        >
          <table className="w-full border-collapse text-sm max-lg:w-max max-lg:whitespace-nowrap">
            <caption id="job-slots-caption" className="sr-only">
              {t('jobSlot.caption')}
            </caption>
            <thead className="border-b border-border bg-muted">
              <tr>
                <th scope="col" className={th}>
                  {t('jobSlot.fields.title')}
                </th>
                <th scope="col" className={th}>
                  {t('jobSlot.fields.jobCategory')}
                </th>
                <th scope="col" className={th}>
                  {t('jobSlot.fields.zone')}
                </th>
                <th scope="col" className={th}>
                  {t('jobSlot.status')}
                </th>
                <th scope="col" className={th}>
                  {t('jobSlot.availability')}
                </th>
                <th scope="col" className={th}>
                  {t('queue.columns.actions')}
                </th>
              </tr>
            </thead>
            <tbody>
              {slots.data.map((s) => (
                <tr key={s.id} className="border-b border-border last:border-b-0">
                  <th scope="row" className={`${td} text-left font-semibold`}>
                    {s.title}
                  </th>
                  <td className={td}>{s.jobCategory.name}</td>
                  <td className={td}>{s.zone.name}</td>
                  <td className={td}>
                    {s.status === 'BLOCCATA'
                      ? t('jobSlot.blockedBy', { number: s.blockedByTicketNumber })
                      : t('jobSlot.free')}
                  </td>
                  <td className={td}>{t(s.active ? 'jobSlot.available' : 'jobSlot.withdrawn')}</td>
                  <td className={`${td} whitespace-nowrap`}>
                    <div className="flex gap-2">
                      <Button size="sm" variant="outline" onClick={() => open(s)} disabled={editing !== null}>
                        {t('jobSlot.edit')}
                        {/* testo nascosto dopo uno spazio a sé: nome accessibile "Modifica Magazziniere" */}{' '}
                        <span className="sr-only">{s.title}</span>
                      </Button>
                      {(s.active ? s.status === 'LIBERA' : companyActive) && (
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => toggle(s)}
                          disabled={setActive.isPending}
                        >
                          {t(s.active ? 'jobSlot.withdraw' : 'jobSlot.restore')}{' '}
                          <span className="sr-only">{s.title}</span>
                        </Button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
};
