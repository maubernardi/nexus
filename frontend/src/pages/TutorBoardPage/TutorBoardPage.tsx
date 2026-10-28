import { useTranslation } from 'react-i18next';
import { Link, useSearchParams } from 'react-router';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { Button } from '@/components/ui/button';
import { NativeSelect } from '@/components/ui/native-select';
import { useTutorBoard } from '@/config/hooks/useBoardPosts';
import { useJobCategories, useZones } from '@/config/hooks/useReferenceData';
import { TutorBoardPostCard } from '@/molecules/TutorBoardPostCard/TutorBoardPostCard';

// filtri nell'indirizzo: Indietro e link condivisi li mantengono
const ZONE_PARAM = 'zona';
const CATEGORY_PARAM = 'tipologia';

/**
 * Bacheca del Tutor (US-901), pensata per lo smartphone: annunci aperti a tutti o riservati ai suoi progetti, senza
 * azienda, titolo e note (review dello Sprint 5, §8.2).
 */
export const TutorBoardPage = () => {
  const { t } = useTranslation();
  const [searchParams, setSearchParams] = useSearchParams();
  const zoneId = searchParams.get(ZONE_PARAM) ?? '';
  const jobCategoryId = searchParams.get(CATEGORY_PARAM) ?? '';
  const zones = useZones();
  const categories = useJobCategories();
  const posts = useTutorBoard({ zoneId: zoneId || undefined, jobCategoryId: jobCategoryId || undefined });

  const setParam = (name: string, value: string): void => {
    const next = new URLSearchParams(searchParams);
    if (value) next.set(name, value);
    else next.delete(name);
    setSearchParams(next);
  };

  return (
    <div className="space-y-6">
      <PageHeading>{t('pages.board.heading')}</PageHeading>
      <p className="text-muted-foreground">{t('board.tutor.intro')}</p>

      <fieldset className="space-y-3 rounded-xl border border-border bg-card p-4">
        <legend className="px-1 font-semibold">{t('board.tutor.filters')}</legend>
        <div className="grid gap-4 md:grid-cols-[1fr_1fr_auto] md:items-end">
          <div className="space-y-1.5">
            <label htmlFor="board-zone" className="block text-sm font-medium">
              {t('board.tutor.zone')}
            </label>
            <NativeSelect
              id="board-zone"
              value={zoneId}
              disabled={!zones.data}
              onChange={(e) => setParam(ZONE_PARAM, e.target.value)}
            >
              <option value="">{t('board.tutor.allZones')}</option>
              {zones.data?.map((z) => (
                <option key={z.id} value={z.id}>
                  {z.name}
                </option>
              ))}
            </NativeSelect>
          </div>
          <div className="space-y-1.5">
            <label htmlFor="board-category" className="block text-sm font-medium">
              {t('board.tutor.jobCategory')}
            </label>
            <NativeSelect
              id="board-category"
              value={jobCategoryId}
              disabled={!categories.data}
              onChange={(e) => setParam(CATEGORY_PARAM, e.target.value)}
            >
              <option value="">{t('board.tutor.allJobCategories')}</option>
              {categories.data?.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </NativeSelect>
          </div>
          {(zoneId || jobCategoryId) && (
            <Button variant="outline" size="lg" onClick={() => setSearchParams(new URLSearchParams())}>
              {t('board.tutor.reset')}
            </Button>
          )}
        </div>
      </fieldset>

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
        <p className="rounded-xl border border-border bg-card p-6">{t('board.tutor.empty')}</p>
      ) : (
        <ul aria-label={t('board.tutor.list')} className="grid gap-4 md:grid-cols-2">
          {posts.data.map((p) => (
            <li key={p.id}>
              <TutorBoardPostCard
                post={p}
                action={
                  p.candidatureInProgress ? (
                    // la prima candidatura blocca l'annuncio finché il Call Center non decide (Sprint 6, §8.1)
                    <p className="font-semibold">{t('board.tutor.inProgress')}</p>
                  ) : (
                    <Button asChild size="lg" variant="outline">
                      <Link to={`/bacheca/${p.id}/candidatura`}>
                        {t('board.tutor.candidate')}
                        {/* spazio come nodo a sé: nome accessibile "Candida un beneficiario all'annuncio #4" */}{' '}
                        <span className="sr-only">
                          {t('board.tutor.candidateTarget', { number: p.number })}
                        </span>
                      </Link>
                    </Button>
                  )
                }
              />
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};
