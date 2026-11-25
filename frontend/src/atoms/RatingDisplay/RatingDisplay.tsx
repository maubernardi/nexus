import { Star, ThumbsUp } from 'lucide-react';
import { useTranslation } from 'react-i18next';

import { RATING_MAX, type RatingKind } from '@/lib/rating';
import { cn } from '@/lib/utils';

type RatingIconProps = {
  kind: RatingKind;
  filled: boolean;
  className?: string;
};

/**
 * Una stella o un pollice, disegnati e non emoji: vuoti sono bianchi con il contorno (contrasto di almeno 3:1), pieni
 * sono colorati. Pieno e vuoto si distinguono anche per la forma, non solo per il colore.
 */
export const RatingIcon = ({ kind, filled, className }: RatingIconProps) => {
  const Icon = kind === 'stars' ? Star : ThumbsUp;
  return (
    <Icon
      aria-hidden="true"
      data-filled={filled}
      className={cn(
        'shrink-0',
        !filled && 'fill-card text-input',
        filled && kind === 'stars' && 'fill-gold text-warning-foreground',
        filled && kind === 'thumbs' && 'fill-primary text-primary',
        className,
      )}
    />
  );
};

type RatingDisplayProps = {
  kind: RatingKind;
  /** Assente = non valutata: un trattino. */
  value?: number | null;
  className?: string;
};

/** Valutazione in sola lettura, per esempio nell'elenco delle aziende, con il testo per il lettore di schermo. */
export const RatingDisplay = ({ kind, value, className }: RatingDisplayProps) => {
  const { t } = useTranslation();
  const max = RATING_MAX[kind];
  if (!value) {
    return (
      <span className={className}>
        <span aria-hidden="true">—</span>
        <span className="sr-only">{t('rating.none')}</span>
      </span>
    );
  }
  return (
    <span className={cn('inline-flex items-center gap-0.5 whitespace-nowrap', className)}>
      {Array.from({ length: max }, (_, i) => (
        <RatingIcon key={i} kind={kind} filled={i < value} className="size-4" />
      ))}
      <span className="sr-only">{t(`rating.${kind}`, { count: value, max })}</span>
    </span>
  );
};
