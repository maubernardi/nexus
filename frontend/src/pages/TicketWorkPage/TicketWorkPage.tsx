import { useEffect, useRef, useState } from 'react';
import { Zap } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Link, useParams } from 'react-router';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { Button } from '@/components/ui/button';
import { NativeSelect } from '@/components/ui/native-select';
import { apiError } from '@/config/api/errors';
import type { JobSlotMatch, TicketDetail } from '@/config/api/ticketApi';
import { useCurrentUser } from '@/config/hooks/useCurrentUser';
import { useJobCategories, useZones } from '@/config/hooks/useReferenceData';
import { useCompatibleJobSlots, useMatchTicket, useTicketWork } from '@/config/hooks/useTickets';

const dateTime = new Intl.DateTimeFormat('it-IT', { dateStyle: 'short', timeStyle: 'short' });

// valore del select per "nessun filtro"
const ALL = '';

type Outcome =
  | { kind: 'proposed'; title: string; company: string }
  | { kind: 'conflict'; reason: string }
  | { kind: 'error' };

/** Lavorazione di una segnalazione da parte del Call Center: compatibili e abbinamento (US-601, US-602). */
export const TicketWorkPage = () => {
  const { t } = useTranslation();
  const { id = '' } = useParams();
  const ticket = useTicketWork(id);
  const { data: user } = useCurrentUser();
  const outcomeRef = useRef<HTMLDivElement>(null);
  const [outcome, setOutcome] = useState<Outcome | null>(null);

  useEffect(() => {
    if (outcome) outcomeRef.current?.focus();
  }, [outcome]);

  if (ticket.isError) {
    return (
      <div className="space-y-4">
        <PageHeading>
          {t(apiError(ticket.error)?.status === 404 ? 'work.notFound' : 'queue.loadError')}
        </PageHeading>
        <Link to="/lavorazioni" className="font-medium text-primary underline underline-offset-4">
          {t('work.backToMyWork')}
        </Link>
      </div>
    );
  }
  if (!ticket.data) {
    return <p role="status">{t('queue.loading')}</p>;
  }
  const d = ticket.data;
  const isAdmin = user?.roles.includes('ADMIN') ?? false;
  const canMatch = d.status === 'IN_LAVORAZIONE' && (d.assignedToMe || isAdmin);
  const assignment = d.assignedToMe
    ? t('work.assignedToYou')
    : d.assignedOperatorName
      ? t('work.assignedTo', { name: d.assignedOperatorName })
      : t('work.unassigned');

  return (
    <div className="space-y-6">
      <PageHeading>{t('pages.ticketWork.heading', { number: d.number })}</PageHeading>
      <p className="font-medium">
        {t('work.status', { status: t(`enums.ticketStatus.${d.status}`) })} · {assignment}
      </p>

      {outcome && (
        <div
          ref={outcomeRef}
          tabIndex={-1}
          className={
            outcome.kind === 'proposed'
              ? 'rounded-md border-l-4 border-primary bg-card p-4 font-medium'
              : 'rounded-md border-2 border-destructive bg-card p-4 font-medium text-destructive'
          }
        >
          {outcome.kind === 'proposed'
            ? t('work.proposed', { title: outcome.title, company: outcome.company })
            : outcome.kind === 'conflict'
              ? t('work.conflict', { reason: outcome.reason })
              : t('work.error')}
        </div>
      )}

      <TicketData ticket={d} />

      {d.proposal && (
        <section
          aria-labelledby="work-proposal"
          className="space-y-3 rounded-xl border border-l-4 border-border border-l-primary bg-card p-4"
        >
          <h2 id="work-proposal" className="text-lg font-semibold text-primary">
            {t('work.sections.proposal')}
          </h2>
          <dl className="grid gap-x-6 gap-y-2 sm:grid-cols-[max-content_1fr]">
            <dt className="font-medium">{t('work.fields.company')}</dt>
            <dd>{d.proposal.companyName}</dd>
            <dt className="font-medium">{t('work.fields.jobSlot')}</dt>
            <dd>{d.proposal.title}</dd>
            <dt className="font-medium">{t('work.fields.jobCategory')}</dt>
            <dd>{d.proposal.jobCategory.name}</dd>
            <dt className="font-medium">{t('work.fields.zone')}</dt>
            <dd>{d.proposal.zone.name}</dd>
          </dl>
        </section>
      )}

      {d.status === 'IN_LAVORAZIONE' && !canMatch && (
        <p className="rounded-xl border border-border bg-card p-4">
          {d.assignedOperatorName ? t('work.notMine', { name: d.assignedOperatorName }) : t('work.notTaken')}
        </p>
      )}
      {canMatch && <CompatibleSection ticket={d} onOutcome={setOutcome} />}

      <Link to="/lavorazioni" className="inline-block font-medium text-primary underline underline-offset-4">
        {t('work.backToMyWork')}
      </Link>
    </div>
  );
};

const TicketData = ({ ticket: d }: { ticket: TicketDetail }) => {
  const { t } = useTranslation();
  return (
    <section aria-labelledby="work-data" className="space-y-3 rounded-xl border border-border bg-card p-4">
      <h2 id="work-data" className="text-lg font-semibold text-primary">
        {t('work.sections.data')}
      </h2>
      <dl className="grid gap-x-6 gap-y-2 sm:grid-cols-[max-content_1fr]">
        <dt className="font-medium">{t('work.fields.beneficiary')}</dt>
        <dd>
          {t('work.beneficiaryLine', {
            lastName: d.beneficiary.lastName,
            firstName: d.beneficiary.firstName,
            birthYear: d.beneficiary.birthYear,
          })}
        </dd>
        <dt className="font-medium">{t('work.fields.zone')}</dt>
        <dd>{d.beneficiaryZone.name}</dd>
        <dt className="font-medium">{t('work.fields.project')}</dt>
        <dd>{d.project.name}</dd>
        <dt className="font-medium">{t('work.fields.requestedJob')}</dt>
        <dd>{d.requestedJobCategory?.name ?? d.requestedJobFreeText ?? '—'}</dd>
        <dt className="font-medium">{t('work.fields.tutor')}</dt>
        <dd>{d.tutorName}</dd>
        <dt className="font-medium">{t('work.fields.arrivedAt')}</dt>
        <dd>
          <time dateTime={d.createdAt}>{dateTime.format(new Date(d.createdAt))}</time>
        </dd>
        <dt className="font-medium">{t('work.fields.type')}</dt>
        <dd>
          {d.fastTrack ? (
            <span className="inline-flex items-center gap-1 rounded-full bg-gold px-2 py-0.5 text-xs font-semibold text-brand">
              <Zap aria-hidden="true" className="size-3.5" />
              {t('queue.fastTrack')}
            </span>
          ) : (
            t('work.normal')
          )}
        </dd>
      </dl>
    </section>
  );
};

type CompatibleSectionProps = {
  ticket: TicketDetail;
  onOutcome: (outcome: Outcome) => void;
};

const CompatibleSection = ({ ticket: d, onOutcome }: CompatibleSectionProps) => {
  const { t } = useTranslation();
  const zones = useZones();
  const categories = useJobCategories();
  const [zoneId, setZoneId] = useState(d.beneficiaryZone.id);
  const [jobCategoryId, setJobCategoryId] = useState(d.requestedJobCategory?.id ?? ALL);
  const compatible = useCompatibleJobSlots(
    d.id,
    { zoneId: zoneId || undefined, jobCategoryId: jobCategoryId || undefined },
    true,
  );
  const match = useMatchTicket();
  const [candidate, setCandidate] = useState<JobSlotMatch | null>(null);
  const confirmRef = useRef<HTMLHeadingElement>(null);

  useEffect(() => {
    if (candidate) confirmRef.current?.focus();
  }, [candidate]);

  const confirm = (slot: JobSlotMatch): void => {
    match.mutate(
      { id: d.id, jobSlotId: slot.id, ticketVersion: d.version, jobSlotVersion: slot.version },
      {
        onSuccess: () => onOutcome({ kind: 'proposed', title: slot.title, company: slot.companyName }),
        onError: (error) => {
          const body = apiError(error);
          onOutcome(body?.status === 409 ? { kind: 'conflict', reason: body.message } : { kind: 'error' });
        },
        onSettled: () => setCandidate(null),
      },
    );
  };

  const th = 'px-3 py-2 text-left text-sm font-semibold whitespace-nowrap';
  const td = 'px-3 py-2 align-top';
  const beneficiary = `${d.beneficiary.firstName} ${d.beneficiary.lastName}`;

  return (
    <section aria-labelledby="work-compatible" className="space-y-4">
      <h2 id="work-compatible" className="text-lg font-semibold text-primary">
        {t('work.sections.compatible')}
      </h2>

      <fieldset className="space-y-3 rounded-xl border border-border bg-card p-4">
        <legend className="px-1 font-semibold">{t('work.filters')}</legend>
        <p className="text-sm text-muted-foreground">{t('work.defaultHint')}</p>
        <div className="grid gap-4 md:grid-cols-2">
          <div className="space-y-1.5">
            <label htmlFor="work-zone" className="block text-sm font-medium">
              {t('work.zone')}
            </label>
            <NativeSelect id="work-zone" value={zoneId} onChange={(e) => setZoneId(e.target.value)}>
              <option value={ALL}>{t('work.allZones')}</option>
              {(zones.data ?? [d.beneficiaryZone]).map((z) => (
                <option key={z.id} value={z.id}>
                  {z.name}
                </option>
              ))}
            </NativeSelect>
          </div>
          <div className="space-y-1.5">
            <label htmlFor="work-category" className="block text-sm font-medium">
              {t('work.jobCategory')}
            </label>
            <NativeSelect
              id="work-category"
              value={jobCategoryId}
              onChange={(e) => setJobCategoryId(e.target.value)}
            >
              <option value={ALL}>{t('work.allCategories')}</option>
              {(categories.data ?? (d.requestedJobCategory ? [d.requestedJobCategory] : [])).map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </NativeSelect>
          </div>
        </div>
      </fieldset>

      <p role="status" className="font-medium">
        {compatible.data ? t('work.count', { count: compatible.data.length }) : ''}
      </p>

      {candidate && (
        <div className="space-y-3 rounded-xl border-2 border-primary bg-card p-4">
          <h3 ref={confirmRef} tabIndex={-1} className="font-semibold">
            {t('work.confirmHeading')}
          </h3>
          <p>
            {t('work.confirmText', { beneficiary, title: candidate.title, company: candidate.companyName })}
          </p>
          <div className="flex flex-wrap gap-3">
            <Button size="lg" onClick={() => confirm(candidate)} disabled={match.isPending}>
              {t('work.confirm')}
            </Button>
            <Button size="lg" variant="outline" onClick={() => setCandidate(null)} disabled={match.isPending}>
              {t('work.cancel')}
            </Button>
          </div>
        </div>
      )}

      {compatible.isError && !compatible.data ? (
        <p
          role="alert"
          className="rounded-md border-2 border-destructive bg-card p-4 font-medium text-destructive"
        >
          {t('queue.loadError')}
        </p>
      ) : !compatible.data ? (
        <p>{t('queue.loading')}</p>
      ) : compatible.data.length === 0 ? (
        <p className="rounded-xl border border-border bg-card p-6">{t('work.empty')}</p>
      ) : (
        <div
          role="region"
          aria-labelledby="work-compatible-caption"
          // eslint-disable-next-line jsx-a11y/no-noninteractive-tabindex -- regione scorrevole: deve essere raggiungibile da tastiera
          tabIndex={0}
          className="relative overflow-x-auto rounded-xl border border-border bg-card focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
        >
          <table className="w-full border-collapse text-sm max-lg:w-max max-lg:whitespace-nowrap">
            <caption id="work-compatible-caption" className="sr-only">
              {t('work.caption')}
            </caption>
            <thead className="border-b border-border bg-muted">
              <tr>
                <th scope="col" className={th}>
                  {t('work.fields.company')}
                </th>
                <th scope="col" className={th}>
                  {t('work.fields.jobSlot')}
                </th>
                <th scope="col" className={th}>
                  {t('work.fields.jobCategory')}
                </th>
                <th scope="col" className={th}>
                  {t('work.fields.zone')}
                </th>
                <th scope="col" className={th}>
                  {t('queue.columns.actions')}
                </th>
              </tr>
            </thead>
            <tbody>
              {compatible.data.map((s) => (
                <tr key={s.id} className="border-b border-border last:border-b-0">
                  <td className={td}>{s.companyName}</td>
                  <th scope="row" className={`${td} text-left font-semibold`}>
                    {s.title}
                  </th>
                  <td className={td}>{s.jobCategory.name}</td>
                  <td className={td}>{s.zone.name}</td>
                  <td className={td}>
                    <Button
                      size="sm"
                      onClick={() => setCandidate(s)}
                      disabled={candidate !== null || match.isPending}
                    >
                      {t('work.propose')}
                      {/* spazio come nodo a sé: nome accessibile "Proponi Magazziniere presso Alfa" */}{' '}
                      <span className="sr-only">
                        {t('work.proposeTarget', { title: s.title, company: s.companyName })}
                      </span>
                    </Button>
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
