import { useTranslation } from 'react-i18next';
import { Link, useSearchParams } from 'react-router';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { Button } from '@/components/ui/button';
import type { BoardPostStatus } from '@/config/api/boardPostApi';
import { useBoardPosts } from '@/config/hooks/useBoardPosts';
import { cn } from '@/lib/utils';

// la vista sta nell'indirizzo: Indietro e link condivisi la mantengono
const VIEW_PARAM = 'stato';
const VIEWS: readonly BoardPostStatus[] = ['DRAFT', 'PUBLISHED', 'ARCHIVED'];
const VIEW_SLUGS: Record<BoardPostStatus, string> = {
  DRAFT: 'bozze',
  PUBLISHED: 'pubblicati',
  ARCHIVED: 'archiviati',
};

const date = new Intl.DateTimeFormat('it-IT', { dateStyle: 'short' });

/** Gestione della bacheca per Call Center e ADMIN (US-902): annunci per stato, con l'azienda. */
export const BoardPage = () => {
  const { t } = useTranslation();
  const [searchParams] = useSearchParams();
  const view = VIEWS.find((v) => VIEW_SLUGS[v] === searchParams.get(VIEW_PARAM)) ?? 'DRAFT';
  const posts = useBoardPosts(view);

  const th = 'px-3 py-2 text-left text-sm font-semibold whitespace-nowrap';
  const td = 'px-3 py-2 align-top';

  return (
    <div className="space-y-6">
      <PageHeading>{t('pages.board.heading')}</PageHeading>
      <p className="text-muted-foreground">{t('pages.board.intro')}</p>

      <nav aria-label={t('board.views')}>
        <ul className="flex flex-wrap gap-2">
          {VIEWS.map((v) => (
            <li key={v}>
              <Link
                to={`?${VIEW_PARAM}=${VIEW_SLUGS[v]}`}
                aria-current={v === view ? 'page' : undefined}
                className={cn(
                  'inline-flex min-h-11 items-center rounded-full border px-4 font-medium focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring',
                  v === view
                    ? 'border-primary bg-primary text-primary-foreground'
                    : 'border-border bg-card text-foreground hover:bg-accent',
                )}
              >
                {t(`board.view.${v}`)}
              </Link>
            </li>
          ))}
        </ul>
      </nav>
      <p className="text-muted-foreground">{t(`board.viewHint.${view}`)}</p>

      <p role="status" className="font-medium">
        {posts.data ? t('board.count', { count: posts.data.length }) : ''}
      </p>

      {posts.isError && !posts.data ? (
        <div role="alert" className="space-y-3 rounded-md border-2 border-destructive bg-card p-4">
          <p className="font-medium text-destructive">{t('board.loadError')}</p>
          <Button variant="outline" onClick={() => void posts.refetch()}>
            {t('common.retry')}
          </Button>
        </div>
      ) : !posts.data ? (
        <p>{t('common.loading')}</p>
      ) : posts.data.length === 0 ? (
        <p className="rounded-xl border border-border bg-card p-6">{t('board.empty')}</p>
      ) : (
        <div
          role="region"
          aria-labelledby="board-caption"
          // eslint-disable-next-line jsx-a11y/no-noninteractive-tabindex -- regione scorrevole: deve essere raggiungibile da tastiera
          tabIndex={0}
          className="relative overflow-x-auto rounded-xl border border-border bg-card focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
        >
          <table className="w-full border-collapse text-sm max-lg:w-max max-lg:whitespace-nowrap">
            <caption id="board-caption" className="sr-only">
              {t('board.caption', { view: t(`board.view.${view}`) })}
            </caption>
            <thead className="border-b border-border bg-muted">
              <tr>
                <th scope="col" className={th}>
                  {t('board.columns.number')}
                </th>
                <th scope="col" className={th}>
                  {t('board.columns.title')}
                </th>
                <th scope="col" className={th}>
                  {t('board.columns.company')}
                </th>
                <th scope="col" className={th}>
                  {t('board.columns.jobCategory')}
                </th>
                <th scope="col" className={th}>
                  {t('board.columns.zone')}
                </th>
                <th scope="col" className={th}>
                  {t('board.columns.project')}
                </th>
                <th scope="col" className={th}>
                  {t('board.columns.publishedAt')}
                </th>
              </tr>
            </thead>
            <tbody>
              {posts.data.map((p) => (
                <tr key={p.id} className="border-b border-border last:border-b-0">
                  <th scope="row" className={`${td} text-left font-semibold tabular-nums`}>
                    {/* il nome contiene il numero visibile (WCAG 2.5.3) */}
                    <Link
                      to={`/bacheca/${p.id}`}
                      aria-label={t('board.open', { number: p.number })}
                      className="text-primary underline underline-offset-4"
                    >
                      #{p.number}
                    </Link>
                  </th>
                  <td className={td}>{p.title}</td>
                  <td className={td}>{p.jobSlot.companyName}</td>
                  <td className={td}>{p.jobSlot.jobCategory.name}</td>
                  <td className={td}>{p.jobSlot.zone.name}</td>
                  <td className={td}>{p.project?.name ?? t('board.allProjects')}</td>
                  <td className={`${td} whitespace-nowrap tabular-nums`}>
                    {p.publishedAt ? (
                      <time dateTime={p.publishedAt}>{date.format(new Date(p.publishedAt))}</time>
                    ) : (
                      t('board.none')
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};
