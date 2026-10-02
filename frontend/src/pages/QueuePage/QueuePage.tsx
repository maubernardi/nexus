import { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useSearchParams } from 'react-router';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { Button } from '@/components/ui/button';
import { NativeSelect } from '@/components/ui/native-select';
import { useProjects, useZones } from '@/config/hooks/useReferenceData';
import { apiError } from '@/config/api/errors';
import type { QueueItem } from '@/config/api/ticketApi';
import { useQueue, useTakeCharge } from '@/config/hooks/useTickets';
import { QueueTable } from '@/organisms/QueueTable/QueueTable';

// parametri dell'indirizzo: link condivisibili e tasto Indietro coerenti con i filtri
const PROJECT_PARAM = 'progetto';
const ZONE_PARAM = 'zona';

export const QueuePage = () => {
  const { t } = useTranslation();
  const [searchParams, setSearchParams] = useSearchParams();
  const projectId = searchParams.get(PROJECT_PARAM) ?? '';
  const zoneId = searchParams.get(ZONE_PARAM) ?? '';
  const projects = useProjects();
  const zones = useZones();
  const queue = useQueue({ projectId: projectId || undefined, zoneId: zoneId || undefined });
  const filtered = projectId !== '' || zoneId !== '';
  const takeCharge = useTakeCharge();
  // esito dell'ultima presa in carico: la riga sparisce dalla coda, quindi il focus va al messaggio
  const [outcome, setOutcome] = useState<{
    kind: 'taken' | 'conflict' | 'error';
    number: number;
    id: string;
  } | null>(null);
  const outcomeRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (outcome) outcomeRef.current?.focus();
  }, [outcome]);

  const onTakeCharge = (item: QueueItem): void => {
    setOutcome(null);
    takeCharge.mutate(
      { id: item.id, version: item.version },
      {
        onSuccess: () => setOutcome({ kind: 'taken', number: item.number, id: item.id }),
        onError: (error) =>
          setOutcome({
            kind: apiError(error)?.status === 409 ? 'conflict' : 'error',
            number: item.number,
            id: item.id,
          }),
      },
    );
  };

  const setFilter = (param: string, value: string): void => {
    const next = new URLSearchParams(searchParams);
    if (value) next.set(param, value);
    else next.delete(param);
    setSearchParams(next);
  };

  return (
    <div className="space-y-6">
      <PageHeading>{t('pages.queue.heading')}</PageHeading>
      <p className="text-muted-foreground">{t('pages.queue.intro')}</p>

      <fieldset className="space-y-3 rounded-xl border border-border bg-card p-4">
        <legend className="px-1 text-lg font-semibold text-primary">{t('queue.filters')}</legend>
        <div className="grid gap-4 md:grid-cols-[1fr_1fr_auto] md:items-end">
          <div className="space-y-1.5">
            <label htmlFor="queue-project" className="block text-sm font-medium">
              {t('queue.project')}
            </label>
            <NativeSelect
              id="queue-project"
              value={projectId}
              disabled={!projects.data}
              onChange={(e) => setFilter(PROJECT_PARAM, e.target.value)}
            >
              <option value="">{t('queue.allProjects')}</option>
              {projects.data?.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.name}
                </option>
              ))}
            </NativeSelect>
          </div>
          <div className="space-y-1.5">
            <label htmlFor="queue-zone" className="block text-sm font-medium">
              {t('queue.zone')}
            </label>
            <NativeSelect
              id="queue-zone"
              value={zoneId}
              disabled={!zones.data}
              onChange={(e) => setFilter(ZONE_PARAM, e.target.value)}
            >
              <option value="">{t('queue.allZones')}</option>
              {zones.data?.map((z) => (
                <option key={z.id} value={z.id}>
                  {z.name}
                </option>
              ))}
            </NativeSelect>
          </div>
          {filtered && (
            <Button variant="outline" size="lg" onClick={() => setSearchParams(new URLSearchParams())}>
              {t('queue.reset')}
            </Button>
          )}
        </div>
      </fieldset>

      {/* sempre nel DOM, così il cambio del conteggio viene annunciato */}
      <p id="queue-count" role="status" className="font-medium">
        {queue.data ? t('queue.count', { count: queue.data.length }) : ''}
      </p>

      {outcome && (
        <div
          ref={outcomeRef}
          tabIndex={-1}
          className={
            outcome.kind === 'taken'
              ? 'space-y-2 rounded-md border-l-4 border-primary bg-card p-4'
              : 'rounded-md border-2 border-destructive bg-card p-4 font-medium text-destructive'
          }
        >
          {outcome.kind === 'taken' ? (
            <>
              <p className="font-medium">{t('queue.takenTitle', { number: outcome.number })}</p>
              <Link
                to={`/lavorazioni/${outcome.id}`}
                className="font-medium text-primary underline underline-offset-4"
              >
                {t('queue.takenLink', { number: outcome.number })}
              </Link>
            </>
          ) : (
            <p>
              {t(outcome.kind === 'conflict' ? 'queue.conflict' : 'queue.takeError', {
                number: outcome.number,
              })}
            </p>
          )}
        </div>
      )}

      {queue.isError && !queue.data ? (
        <div role="alert" className="space-y-3 rounded-md border-2 border-destructive bg-card p-4">
          <p className="font-medium text-destructive">{t('queue.loadError')}</p>
          <Button variant="outline" onClick={() => void queue.refetch()}>
            {t('queue.retry')}
          </Button>
        </div>
      ) : !queue.data ? (
        <p>{t('queue.loading')}</p>
      ) : queue.data.length === 0 ? (
        <p className="rounded-xl border border-border bg-card p-6">
          {filtered ? t('queue.emptyFiltered') : t('queue.empty')}
        </p>
      ) : (
        <QueueTable
          items={queue.data}
          caption={t('queue.caption')}
          onTakeCharge={onTakeCharge}
          pendingId={takeCharge.isPending ? (takeCharge.variables?.id ?? null) : null}
        />
      )}
    </div>
  );
};
