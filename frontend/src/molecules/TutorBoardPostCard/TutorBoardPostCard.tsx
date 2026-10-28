import { type ReactNode } from 'react';
import { useTranslation } from 'react-i18next';

import type { TutorBoardPost } from '@/config/api/boardPostApi';

const date = new Intl.DateTimeFormat('it-IT', { dateStyle: 'long' });

type TutorBoardPostCardProps = {
  post: TutorBoardPost;
  /** Comando o stato in fondo alla scheda (es. "Candida un beneficiario" in bacheca). */
  action?: ReactNode;
};

/**
 * Annuncio come lo vede il Tutor (US-901): tipologia e zona in evidenza, poi le condizioni; il riservato è indicato in
 * testo. Mai azienda, titolo o note: non arrivano nemmeno dall'API.
 */
export const TutorBoardPostCard = ({ post: p, action }: TutorBoardPostCardProps) => {
  const { t } = useTranslation();
  const headingId = `board-post-${p.id}`;
  const age =
    typeof p.ageMin === 'number' && typeof p.ageMax === 'number'
      ? t('board.tutor.ageRange', { min: p.ageMin, max: p.ageMax })
      : typeof p.ageMin === 'number'
        ? t('board.tutor.ageFrom', { min: p.ageMin })
        : typeof p.ageMax === 'number'
          ? t('board.tutor.ageTo', { max: p.ageMax })
          : null;

  return (
    <article
      aria-labelledby={headingId}
      className="flex h-full flex-col gap-3 rounded-xl border border-border bg-card p-4"
    >
      <h2 id={headingId} className="text-lg font-semibold text-primary">
        {t('board.numbered', { number: p.number })} · {p.jobCategory.name}
      </h2>
      <p className="font-medium">{p.zone.name}</p>
      <p className={p.project ? 'font-semibold' : 'text-muted-foreground'}>
        {p.project ? t('board.tutor.reserved', { project: p.project.name }) : t('board.tutor.forAll')}
      </p>
      <dl className="grid grid-cols-[max-content_1fr] gap-x-4 gap-y-1 text-sm">
        <dt className="font-medium">{t('board.tutor.age')}</dt>
        <dd>{age ?? t('board.tutor.notSpecified')}</dd>
        <dt className="font-medium">{t('board.tutor.hours')}</dt>
        <dd>
          {typeof p.weeklyHours === 'number'
            ? t('board.tutor.hoursValue', { count: p.weeklyHours })
            : t('board.tutor.notSpecified')}
        </dd>
        <dt className="font-medium">{t('board.tutor.duration')}</dt>
        <dd>
          {typeof p.durationMonths === 'number'
            ? t('board.tutor.durationValue', { count: p.durationMonths })
            : t('board.tutor.notSpecified')}
        </dd>
        <dt className="font-medium">{t('board.tutor.published')}</dt>
        <dd>
          <time dateTime={p.publishedAt}>{date.format(new Date(p.publishedAt))}</time>
        </dd>
      </dl>
      {action && <div className="mt-auto pt-1">{action}</div>}
    </article>
  );
};
