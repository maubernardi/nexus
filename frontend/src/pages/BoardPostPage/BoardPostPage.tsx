import { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useLocation, useParams } from 'react-router';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { Button } from '@/components/ui/button';
import type { BoardPost } from '@/config/api/boardPostApi';
import { apiError } from '@/config/api/errors';
import {
  useArchiveBoardPost,
  useBoardPost,
  useMakePublicBoardPost,
  usePublishBoardPost,
  useUpdateBoardPost,
} from '@/config/hooks/useBoardPosts';
import { BoardPostForm } from '@/organisms/BoardPostForm/BoardPostForm';

type Outcome =
  | { kind: 'created' | 'saved' | 'published' | 'archived' | 'madePublic'; number: number }
  | { kind: 'refused'; reason: string }
  | { kind: 'error' };

type Action = 'publish' | 'makePublic' | 'archive';

const date = new Intl.DateTimeFormat('it-IT', { dateStyle: 'long' });
const dateTime = new Intl.DateTimeFormat('it-IT', { dateStyle: 'long', timeStyle: 'short' });

/** Scheda di un annuncio per Call Center e ADMIN (US-902): mansione, dati modificabili, pubblicazione, archiviazione. */
export const BoardPostPage = () => {
  const { t } = useTranslation();
  const { id = '' } = useParams();
  const post = useBoardPost(id);
  const update = useUpdateBoardPost();
  const location = useLocation();
  // arrivo dalla creazione nella scheda dell'azienda (US-904)
  const created = (location.state as { createdNumber?: number } | null)?.createdNumber;
  const [outcome, setOutcome] = useState<Outcome | null>(
    created === undefined ? null : { kind: 'created', number: created },
  );
  const outcomeRef = useRef<HTMLDivElement>(null);

  // esito annunciato portandovi il focus
  useEffect(() => {
    if (outcome && post.data) outcomeRef.current?.focus();
  }, [outcome, post.data]);

  if (post.isError) {
    const notFound = apiError(post.error)?.status === 404;
    return (
      <div className="space-y-4">
        <PageHeading>{t(notFound ? 'board.notFound' : 'board.loadError')}</PageHeading>
        <Button asChild size="lg" variant="outline">
          <Link to="/bacheca">{t('board.backToList')}</Link>
        </Button>
      </div>
    );
  }
  if (!post.data) {
    return <p role="status">{t('common.loading')}</p>;
  }
  const p = post.data;
  const isError = outcome?.kind === 'refused' || outcome?.kind === 'error';
  // il server verifica di nuovo i 7 giorni: qui si decide solo se mostrare il comando (US-903)
  const canMakePublic = Boolean(p.makePublicFrom) && new Date(p.makePublicFrom as string) <= new Date();

  return (
    <div className="space-y-6">
      <PageHeading>{t('board.numbered', { number: p.number })}</PageHeading>
      <p className="text-muted-foreground">
        {t('board.statusLine', { status: t(`board.status.${p.status}`) })}
        {p.publishedAt && (
          <>
            {' · '}
            {t('board.publishedOn', { date: date.format(new Date(p.publishedAt)) })}
          </>
        )}
        {p.madePublicAt && (
          <>
            {' · '}
            {t('board.madePublicOn', { date: date.format(new Date(p.madePublicAt)) })}
          </>
        )}
      </p>

      {p.candidatureTicketId && p.candidatureTicketNumber && (
        // la candidatura blocca l'annuncio finché non si decide sulla corsia veloce (US-306)
        <p className="rounded-md border-l-4 border-gold bg-card p-4">
          {t('board.candidature')}{' '}
          <Link
            to={`/segnalazioni/${p.candidatureTicketId}`}
            className="font-medium text-primary underline underline-offset-4"
          >
            {t('board.candidatureTicket', { number: p.candidatureTicketNumber })}
          </Link>
        </p>
      )}

      {outcome && (
        <div
          ref={outcomeRef}
          tabIndex={-1}
          className={
            isError
              ? 'rounded-md border-2 border-destructive bg-card p-4 font-medium text-destructive'
              : 'rounded-md border-l-4 border-primary bg-card p-4 font-medium'
          }
        >
          {outcome.kind === 'refused'
            ? t('board.outcome.refused', { reason: outcome.reason })
            : outcome.kind === 'error'
              ? t('form.errors.generic')
              : t(`board.outcome.${outcome.kind}`, { number: outcome.number })}
        </div>
      )}

      <section
        aria-labelledby="board-job-slot"
        className="space-y-3 rounded-xl border border-border bg-card p-4"
      >
        <h2 id="board-job-slot" className="text-lg font-semibold text-primary">
          {t('board.jobSlot.heading')}
        </h2>
        <dl className="grid gap-x-6 gap-y-2 sm:grid-cols-[max-content_1fr]">
          <dt className="font-medium">{t('board.jobSlot.title')}</dt>
          <dd>{p.jobSlot.title}</dd>
          <dt className="font-medium">{t('board.jobSlot.company')}</dt>
          <dd>
            <Link
              to={`/aziende/${p.jobSlot.companyId}`}
              className="text-primary underline underline-offset-4"
            >
              {p.jobSlot.companyName}
            </Link>
          </dd>
          <dt className="font-medium">{t('board.jobSlot.jobCategory')}</dt>
          <dd>{p.jobSlot.jobCategory.name}</dd>
          <dt className="font-medium">{t('board.jobSlot.zone')}</dt>
          <dd>{p.jobSlot.zone.name}</dd>
          {p.createdFromTicketId && (
            <>
              <dt className="font-medium">{t('board.fromTicket')}</dt>
              <dd>
                <Link
                  to={`/segnalazioni/${p.createdFromTicketId}`}
                  className="text-primary underline underline-offset-4"
                >
                  {t('board.ticketNumber', { number: p.createdFromTicketNumber })}
                </Link>
              </dd>
            </>
          )}
        </dl>
      </section>

      <section
        aria-labelledby="board-post-data"
        className="space-y-4 rounded-xl border border-border bg-card p-4"
      >
        <h2 id="board-post-data" className="text-lg font-semibold text-primary">
          {t('board.form.heading')}
        </h2>
        {p.status === 'ARCHIVED' ? (
          <ArchivedData post={p} />
        ) : (
          <BoardPostForm
            // nuova versione = valori aggiornati nel modulo
            key={`${p.id}-${p.version}`}
            post={p}
            onSubmit={(payload) => update.mutateAsync({ id: p.id, payload })}
            onSuccess={(saved) => setOutcome({ kind: 'saved', number: saved.number })}
            onConflict={(reason) => setOutcome({ kind: 'refused', reason })}
          />
        )}
      </section>

      {p.status !== 'ARCHIVED' && (
        <div className="flex flex-wrap items-start gap-3">
          {p.status === 'DRAFT' && <PostAction action="publish" post={p} onOutcome={setOutcome} />}
          {canMakePublic && <PostAction action="makePublic" post={p} onOutcome={setOutcome} />}
          <PostAction action="archive" post={p} onOutcome={setOutcome} />
        </div>
      )}
      {p.makePublicFrom && !canMakePublic && (
        <p className="text-muted-foreground">
          {t('board.makePublic.notYet', {
            project: p.project?.name,
            date: dateTime.format(new Date(p.makePublicFrom)),
          })}
        </p>
      )}

      <Button asChild size="lg" variant="outline">
        <Link to="/bacheca">{t('board.backToList')}</Link>
      </Button>
    </div>
  );
};

/** Dati di un annuncio archiviato, in sola lettura. */
const ArchivedData = ({ post: p }: { post: BoardPost }) => {
  const { t } = useTranslation();
  // l'API omette i campi vuoti: null e assente valgono uguale
  const value = (n: number | null | undefined): string =>
    typeof n === 'number' ? String(n) : t('board.none');
  return (
    <>
      <p>{t('board.readOnly')}</p>
      <dl className="grid gap-x-6 gap-y-2 sm:grid-cols-[max-content_1fr]">
        <dt className="font-medium">{t('board.form.title')}</dt>
        <dd>{p.title}</dd>
        <dt className="font-medium">{t('board.form.project')}</dt>
        <dd>{p.project?.name ?? t('board.allProjects')}</dd>
        <dt className="font-medium">{t('board.form.ageMin')}</dt>
        <dd>{value(p.ageMin)}</dd>
        <dt className="font-medium">{t('board.form.ageMax')}</dt>
        <dd>{value(p.ageMax)}</dd>
        <dt className="font-medium">{t('board.form.weeklyHours')}</dt>
        <dd>{value(p.weeklyHours)}</dd>
        <dt className="font-medium">{t('board.form.durationMonths')}</dt>
        <dd>{value(p.durationMonths)}</dd>
        <dt className="font-medium">{t('board.form.notes')}</dt>
        <dd className="whitespace-pre-line">{p.notes ?? t('board.none')}</dd>
      </dl>
    </>
  );
};

/** Pubblica o archivia, con conferma; il focus va alla domanda e, se si annulla, torna al pulsante. */
const PostAction = ({
  action,
  post: p,
  onOutcome,
}: {
  action: Action;
  post: BoardPost;
  onOutcome: (o: Outcome) => void;
}) => {
  const { t } = useTranslation();
  const publish = usePublishBoardPost();
  const makePublic = useMakePublicBoardPost();
  const archive = useArchiveBoardPost();
  const mutation = action === 'publish' ? publish : action === 'makePublic' ? makePublic : archive;
  const [confirming, setConfirming] = useState(false);
  const confirmRef = useRef<HTMLHeadingElement>(null);
  const buttonRef = useRef<HTMLButtonElement>(null);
  const wasConfirming = useRef(false);

  useEffect(() => {
    if (confirming) confirmRef.current?.focus();
    else if (wasConfirming.current) buttonRef.current?.focus();
    wasConfirming.current = confirming;
  }, [confirming]);

  // mutateAsync: l'esito arriva anche se, cambiato lo stato, questo pulsante sparisce
  const onConfirm = async (): Promise<void> => {
    try {
      const saved = await mutation.mutateAsync({ id: p.id, version: p.version });
      onOutcome({
        kind: action === 'publish' ? 'published' : action === 'makePublic' ? 'madePublic' : 'archived',
        number: saved.number,
      });
    } catch (error) {
      const body = apiError(error);
      onOutcome(body?.status === 409 ? { kind: 'refused', reason: body.message } : { kind: 'error' });
      setConfirming(false);
    }
  };

  if (!confirming) {
    return (
      <Button
        ref={buttonRef}
        size="lg"
        variant={action === 'publish' ? 'default' : 'outline'}
        onClick={() => setConfirming(true)}
      >
        {t(`board.${action}.action`)}
      </Button>
    );
  }
  return (
    <div className="w-full space-y-3 rounded-xl border-2 border-primary bg-card p-4">
      <h2 ref={confirmRef} tabIndex={-1} className="font-semibold">
        {t(`board.${action}.heading`, { number: p.number })}
      </h2>
      <p>
        {action === 'publish'
          ? t('board.publish.text', { audience: p.project?.name ?? t('board.allProjects') })
          : action === 'makePublic'
            ? t('board.makePublic.text', { project: p.project?.name })
            : t('board.archive.text')}
        {/* una candidatura in attesa su un annuncio archiviato perde la corsia veloce (Sprint 6, §8.1) */}
        {action === 'archive' && p.candidatureTicketNumber && (
          <> {t('board.archive.candidature', { number: p.candidatureTicketNumber })}</>
        )}
      </p>
      <div className="flex flex-wrap gap-3">
        <Button size="lg" onClick={() => void onConfirm()} disabled={mutation.isPending}>
          {t(`board.${action}.confirm`)}
        </Button>
        <Button
          size="lg"
          variant="outline"
          onClick={() => setConfirming(false)}
          disabled={mutation.isPending}
        >
          {t('board.cancel')}
        </Button>
      </div>
    </div>
  );
};
