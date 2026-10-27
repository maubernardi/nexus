import { useEffect, useRef, useState } from 'react';
import { FilePlus2 } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Link, useSearchParams } from 'react-router';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { Button } from '@/components/ui/button';
import { NativeSelect } from '@/components/ui/native-select';
import { apiError } from '@/config/api/errors';
import type { TicketListItem, TicketListView } from '@/config/api/ticketApi';
import { useCurrentUser } from '@/config/hooks/useCurrentUser';
import { useMyProjects, useProjects } from '@/config/hooks/useReferenceData';
import { useTakeCharge, useTicketList } from '@/config/hooks/useTickets';
import { TICKET_STATUSES, type TicketStatus } from '@/lib/domainValues';
import { cn } from '@/lib/utils';
import { TicketsTable, type TicketsTableColumns } from '@/organisms/TicketsTable/TicketsTable';

// parametri dell'indirizzo (in italiano, leggibili): vista e filtri sopravvivono a Indietro e ai link condivisi
const VIEW_PARAM = 'vista';
const STATUS_PARAM = 'stato';
const PROJECT_PARAM = 'progetto';

const VIEW_SLUGS: Record<TicketListView, string> = {
  MINE: 'le-mie',
  PROJECT: 'del-progetto',
  TO_TAKE: 'da-prendere',
  TO_APPROVE: 'da-approvare',
  ASSIGNED_TO_ME: 'in-carico',
  ALL: 'tutte',
};
const TUTOR_VIEWS: TicketListView[] = ['MINE', 'PROJECT'];
const CC_VIEWS: TicketListView[] = ['TO_TAKE', 'TO_APPROVE', 'ASSIGNED_TO_ME', 'ALL'];

const COLUMNS: Record<TicketListView, TicketsTableColumns> = {
  MINE: {},
  PROJECT: { tutor: true },
  TO_TAKE: { priority: true, zone: true, tutor: true },
  TO_APPROVE: { zone: true, tutor: true },
  ASSIGNED_TO_ME: { priority: true, zone: true, tutor: true },
  ALL: { priority: true, zone: true, tutor: true, operator: true },
};

type Outcome = { kind: 'taken' | 'conflict' | 'error'; number: number; id: string };

/** Home di tutti i ruoli (US-303, nota operativa del PO): l'elenco delle segnalazioni con viste per ruolo e filtri. */
export const TicketsPage = () => {
  const { t } = useTranslation();
  const { data: user } = useCurrentUser();
  const isTutor = (user?.roles.includes('TUTOR') ?? false) && !user?.roles.some((r) => r !== 'TUTOR');
  const views = isTutor ? TUTOR_VIEWS : CC_VIEWS;

  const [searchParams, setSearchParams] = useSearchParams();
  const slug = searchParams.get(VIEW_PARAM);
  const view = views.find((v) => VIEW_SLUGS[v] === slug) ?? views[0];
  const status = (TICKET_STATUSES as readonly string[]).includes(searchParams.get(STATUS_PARAM) ?? '')
    ? (searchParams.get(STATUS_PARAM) as TicketStatus)
    : undefined;
  const projectId = searchParams.get(PROJECT_PARAM) ?? '';
  const filtered = status !== undefined || projectId !== '';

  // nessuna richiesta prima di conoscere il ruolo: un Tutor non deve chiedere viste o progetti del Call Center
  const userLoaded = user !== undefined;
  const myProjects = useMyProjects(userLoaded && isTutor);
  const allProjects = useProjects(userLoaded && !isTutor);
  const projects = isTutor ? myProjects : allProjects;
  const list = useTicketList({ view, status, projectId: projectId || undefined }, userLoaded);

  const takeCharge = useTakeCharge();
  const [outcome, setOutcome] = useState<Outcome | null>(null);
  const outcomeRef = useRef<HTMLDivElement>(null);
  useEffect(() => {
    if (outcome) outcomeRef.current?.focus();
  }, [outcome]);

  const setParam = (name: string, value: string): void => {
    const next = new URLSearchParams(searchParams);
    if (value) next.set(name, value);
    else next.delete(name);
    setSearchParams(next);
  };

  const viewHref = (v: TicketListView): string => {
    const next = new URLSearchParams(searchParams);
    next.set(VIEW_PARAM, VIEW_SLUGS[v]);
    return `?${next.toString()}`;
  };

  const onTakeCharge = (item: TicketListItem): void => {
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

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <PageHeading>{t('pages.tickets.heading')}</PageHeading>
        {isTutor && (
          <Button asChild size="lg">
            <Link to="/segnalazioni/nuova">
              <FilePlus2 aria-hidden="true" />
              {t('tickets.new')}
            </Link>
          </Button>
        )}
      </div>

      <nav aria-label={t('tickets.views')}>
        <ul className="flex flex-wrap gap-2">
          {views.map((v) => (
            <li key={v}>
              <Link
                to={viewHref(v)}
                aria-current={v === view ? 'page' : undefined}
                className={cn(
                  'inline-flex min-h-11 items-center rounded-full border px-4 font-medium focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring',
                  v === view
                    ? 'border-primary bg-primary text-primary-foreground'
                    : 'border-border bg-card text-foreground hover:bg-accent',
                )}
              >
                {t(`tickets.view.${v}`)}
              </Link>
            </li>
          ))}
        </ul>
      </nav>
      <p className="text-muted-foreground">{t(`tickets.viewHint.${view}`)}</p>

      <fieldset className="space-y-3 rounded-xl border border-border bg-card p-4">
        <legend className="px-1 font-semibold">{t('tickets.filters')}</legend>
        <div className="grid gap-4 md:grid-cols-[1fr_1fr_auto] md:items-end">
          <div className="space-y-1.5">
            <label htmlFor="tickets-status" className="block text-sm font-medium">
              {t('tickets.status')}
            </label>
            <NativeSelect
              id="tickets-status"
              value={status ?? ''}
              onChange={(e) => setParam(STATUS_PARAM, e.target.value)}
            >
              <option value="">{t('tickets.allStatuses')}</option>
              {TICKET_STATUSES.map((s) => (
                <option key={s} value={s}>
                  {t(`enums.ticketStatus.${s}`)}
                </option>
              ))}
            </NativeSelect>
          </div>
          <div className="space-y-1.5">
            <label htmlFor="tickets-project" className="block text-sm font-medium">
              {t('tickets.project')}
            </label>
            <NativeSelect
              id="tickets-project"
              value={projectId}
              disabled={!projects.data}
              onChange={(e) => setParam(PROJECT_PARAM, e.target.value)}
            >
              <option value="">{t('tickets.allProjects')}</option>
              {projects.data?.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.name}
                </option>
              ))}
            </NativeSelect>
          </div>
          {filtered && (
            <Button
              variant="outline"
              size="lg"
              onClick={() => {
                const next = new URLSearchParams(searchParams);
                next.delete(STATUS_PARAM);
                next.delete(PROJECT_PARAM);
                setSearchParams(next);
              }}
            >
              {t('tickets.reset')}
            </Button>
          )}
        </div>
      </fieldset>

      {/* sempre nel DOM, così il cambio del conteggio viene annunciato */}
      <p id="tickets-count" role="status" className="font-medium">
        {list.data ? t('tickets.count', { count: list.data.length }) : ''}
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
              <p className="font-medium">{t('tickets.taken', { number: outcome.number })}</p>
              <Link
                to={`/segnalazioni/${outcome.id}`}
                className="font-medium text-primary underline underline-offset-4"
              >
                {t('tickets.workIt', { number: outcome.number })}
              </Link>
            </>
          ) : (
            <p>
              {t(outcome.kind === 'conflict' ? 'tickets.conflict' : 'tickets.takeError', {
                number: outcome.number,
              })}
            </p>
          )}
        </div>
      )}

      {list.isError && !list.data ? (
        <div role="alert" className="space-y-3 rounded-md border-2 border-destructive bg-card p-4">
          <p className="font-medium text-destructive">{t('tickets.loadError')}</p>
          <Button variant="outline" onClick={() => void list.refetch()}>
            {t('tickets.retry')}
          </Button>
        </div>
      ) : !list.data ? (
        <p>{t('tickets.loading')}</p>
      ) : list.data.length === 0 ? (
        <p className="rounded-xl border border-border bg-card p-6">
          {filtered ? t('tickets.emptyFiltered') : t(`tickets.empty.${view}`)}
        </p>
      ) : (
        <TicketsTable
          items={list.data}
          caption={t(`tickets.caption.${view}`)}
          columns={COLUMNS[view]}
          rowLink={(item) => `/segnalazioni/${item.id}`}
          onTakeCharge={view === 'TO_TAKE' ? onTakeCharge : undefined}
          pendingId={takeCharge.isPending ? (takeCharge.variables?.id ?? null) : null}
        />
      )}
    </div>
  );
};
