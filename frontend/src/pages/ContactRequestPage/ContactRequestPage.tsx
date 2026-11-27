import { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useParams } from 'react-router';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { Button } from '@/components/ui/button';
import type { ContactRequestDetail } from '@/config/api/contactRequestApi';
import { apiError } from '@/config/api/errors';
import { useContactRequest, useTakeContactRequest } from '@/config/hooks/useContactRequests';
import { useCurrentUser } from '@/config/hooks/useCurrentUser';
import { BackLink } from '@/molecules/BackLink/BackLink';
import { PageHeader } from '@/molecules/PageHeader/PageHeader';
import { ContactAttemptForm } from '@/organisms/ContactAttemptForm/ContactAttemptForm';
import { ContactRequestCloseForm } from '@/organisms/ContactRequestCloseForm/ContactRequestCloseForm';

const dateTime = new Intl.DateTimeFormat('it-IT', { dateStyle: 'short', timeStyle: 'short' });
const day = new Intl.DateTimeFormat('it-IT', { dateStyle: 'short', timeZone: 'UTC' });

/** Data aaaa-mm-gg come giorno italiano, senza spostamenti di fuso. */
const formatDay = (iso: string): string => day.format(new Date(`${iso}T00:00:00Z`));

/**
 * Pagina della richiesta di contatto (US-512): dati, esito e tentativi dal più recente. Il Tutor la vede in sola
 * lettura da «Le mie richieste»; Call Center e ADMIN da «Richieste di contatto». Chi ha la richiesta in carico segna i
 * tentativi e la chiude; se è ancora aperta, un operatore la prende in carico da qui.
 */
export const ContactRequestPage = () => {
  const { t } = useTranslation();
  const { id = '' } = useParams();
  const { data: user } = useCurrentUser();
  const detail = useContactRequest(id);
  const take = useTakeContactRequest();
  const [notice, setNotice] = useState<{ kind: 'done' | 'error'; message: string } | null>(null);
  const noticeRef = useRef<HTMLParagraphElement>(null);
  const isTutor = user?.roles.includes('TUTOR') ?? false;
  const back = isTutor ? (
    <BackLink to="/richieste">{t('contactRequest.detail.backTutor')}</BackLink>
  ) : (
    <BackLink to="/richieste-contatto">{t('contactRequest.detail.backCallCenter')}</BackLink>
  );

  useEffect(() => {
    if (notice) noticeRef.current?.focus();
  }, [notice]);

  if (detail.isError) {
    const notFound = apiError(detail.error)?.status === 404;
    return (
      <div className="space-y-4">
        <PageHeading>
          {t(notFound ? 'contactRequest.detail.notFound' : 'contactRequest.detail.loadError')}
        </PageHeading>
        <Button asChild size="lg" variant="outline">
          <Link to={isTutor ? '/richieste' : '/richieste-contatto'}>
            {t(isTutor ? 'contactRequest.detail.backTutor' : 'contactRequest.detail.backCallCenter')}
          </Link>
        </Button>
      </div>
    );
  }
  if (!detail.data) {
    return <p role="status">{t('common.loading')}</p>;
  }
  const {
    request: r,
    closingNote,
    linkedCompanyName,
    attempts = [],
    assignedToMe,
  }: ContactRequestDetail = detail.data;
  const status =
    r.status === 'CHIUSA' && r.outcome
      ? t('contactRequest.closedWith', { outcome: t(`contactRequest.outcome.${r.outcome}`) })
      : t(`contactRequest.status.${r.status}`);
  const rows = [
    { label: t('contactRequest.detail.company'), value: r.companyName },
    { label: t('contactRequest.columns.zone'), value: r.zone?.name ?? t('contactRequest.none') },
    {
      label: t('contactRequest.columns.category'),
      value: r.businessCategory?.name ?? t('contactRequest.none'),
    },
    { label: t('contactRequest.detail.tutor'), value: r.requestedByName },
    { label: t('contactRequest.detail.sentAt'), value: dateTime.format(new Date(r.createdAt)) },
    { label: t('contactRequest.detail.status'), value: status },
    { label: t('contactRequest.detail.operator'), value: r.assignedOperatorName ?? t('contactRequest.none') },
    ...(r.takenAt
      ? [{ label: t('contactRequest.detail.takenAt'), value: dateTime.format(new Date(r.takenAt)) }]
      : []),
  ];
  const canTake = r.status === 'APERTA' && !isTutor;

  const onTake = (): void => {
    setNotice(null);
    take.mutate(
      { id: r.id, version: r.version },
      {
        onSuccess: () =>
          setNotice({ kind: 'done', message: t('contactRequest.taken', { number: r.number }) }),
        onError: (error) => {
          const body = apiError(error);
          setNotice({
            kind: 'error',
            message:
              body?.status === 409 && body.message
                ? body.message
                : t('contactRequest.takeError', { number: r.number }),
          });
        },
      },
    );
  };

  return (
    <div className="space-y-6">
      <PageHeader back={back}>{t('contactRequest.detail.heading', { number: r.number })}</PageHeader>

      {notice && (
        <p
          ref={noticeRef}
          tabIndex={-1}
          role={notice.kind === 'done' ? 'status' : 'alert'}
          className={
            notice.kind === 'done'
              ? 'rounded-md border-l-4 border-primary bg-card p-4 font-medium'
              : 'rounded-md border-2 border-destructive bg-card p-4 font-medium text-destructive'
          }
        >
          {notice.message}
        </p>
      )}

      <section
        aria-labelledby="contact-request-data"
        className="space-y-3 rounded-xl border border-border bg-card p-4"
      >
        <h2 id="contact-request-data" className="text-lg font-semibold text-primary">
          {t('contactRequest.detail.dataHeading')}
        </h2>
        <dl className="grid gap-x-6 gap-y-3 sm:grid-cols-[max-content_1fr]">
          {rows.map((row) => (
            <div key={row.label} className="contents">
              <dt className="font-medium">{row.label}</dt>
              <dd>{row.value}</dd>
            </div>
          ))}
        </dl>
        {canTake && (
          <Button disabled={take.isPending} aria-busy={take.isPending} onClick={onTake}>
            {t('contactRequest.take')}
          </Button>
        )}
        {r.status === 'IN_CARICO' && !assignedToMe && !isTutor && (
          <p className="text-sm text-muted-foreground">
            {t('contactRequest.detail.notMine', { name: r.assignedOperatorName })}
          </p>
        )}
      </section>

      {r.status === 'CHIUSA' && (
        <section
          aria-labelledby="contact-request-outcome"
          className="space-y-3 rounded-xl border border-l-4 border-border border-l-primary bg-card p-4"
        >
          <h2 id="contact-request-outcome" className="text-lg font-semibold text-primary">
            {t('contactRequest.detail.outcomeHeading')}
          </h2>
          <dl className="grid gap-x-6 gap-y-3 sm:grid-cols-[max-content_1fr]">
            <dt className="font-medium">{t('contactRequest.detail.outcome')}</dt>
            <dd>{r.outcome ? t(`contactRequest.outcome.${r.outcome}`) : t('contactRequest.none')}</dd>
            <dt className="font-medium">{t('contactRequest.detail.closingNote')}</dt>
            <dd className="whitespace-pre-line">{closingNote ?? t('contactRequest.none')}</dd>
            <dt className="font-medium">{t('contactRequest.detail.closedAt')}</dt>
            <dd>{r.closedAt ? dateTime.format(new Date(r.closedAt)) : t('contactRequest.none')}</dd>
            <dt className="font-medium">{t('contactRequest.detail.linkedCompany')}</dt>
            <dd>
              {r.companyId && linkedCompanyName ? (
                <Link to={`/aziende/${r.companyId}`} className="text-primary underline underline-offset-4">
                  {linkedCompanyName}
                </Link>
              ) : (
                t('contactRequest.none')
              )}
            </dd>
          </dl>
        </section>
      )}

      <section
        aria-labelledby="contact-request-attempts"
        className="space-y-3 rounded-xl border border-border bg-card p-4"
      >
        <h2 id="contact-request-attempts" className="text-lg font-semibold text-primary">
          {t('contactRequest.detail.attemptsHeading')}
        </h2>
        <p className="text-sm text-muted-foreground">{t('contactRequest.detail.attemptsIntro')}</p>
        {attempts.length === 0 ? (
          <p>{t('contactRequest.detail.noAttempts')}</p>
        ) : (
          <ol className="space-y-3">
            {attempts.map((a) => (
              <li key={a.id} className="border-l-2 border-input pl-3">
                <p className="font-medium">
                  <time dateTime={a.date}>{formatDay(a.date)}</time> ·{' '}
                  {t(`contactRequest.outcome.${a.outcome}`)}
                </p>
                {a.note && <p className="whitespace-pre-line">{a.note}</p>}
                {a.operatorName && (
                  <p className="text-sm text-muted-foreground">
                    {t('contactRequest.detail.attemptBy', { name: a.operatorName })}
                  </p>
                )}
              </li>
            ))}
          </ol>
        )}
      </section>

      {assignedToMe && (
        <>
          <ContactAttemptForm requestId={r.id} />
          <ContactRequestCloseForm
            request={r}
            onClosed={() => setNotice({ kind: 'done', message: t('contactRequest.closeForm.closed') })}
          />
        </>
      )}
    </div>
  );
};
