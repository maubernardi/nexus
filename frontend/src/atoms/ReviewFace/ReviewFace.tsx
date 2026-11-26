import { Annoyed, Frown, Laugh, Meh, Smile, type LucideIcon } from 'lucide-react';
import { useTranslation } from 'react-i18next';

import { formatAverage, reviewLevel, type ReviewLevel } from '@/lib/reviews';
import { cn } from '@/lib/utils';

/** Una faccina per livello (US-1104, scelta B del PO): disegnate, non emoji. */
const FACES: Record<ReviewLevel, LucideIcon> = { 1: Frown, 2: Annoyed, 3: Meh, 4: Smile, 5: Laugh };

type FaceIconProps = {
  level: ReviewLevel;
  className?: string;
};

/** Faccina di un livello, nascosta al lettore di schermo: il testo lo mette chi la usa. */
export const FaceIcon = ({ level, className }: FaceIconProps) => {
  const Icon = FACES[level];
  return <Icon aria-hidden="true" data-level={level} className={cn('shrink-0', className)} />;
};

type ReviewFaceProps = {
  /** Media da 1 a 5. */
  average: number;
  /** Mostra anche la parola e il numero («Buona · 4,3»); altrimenti solo la faccina e il numero. */
  withWord?: boolean;
  className?: string;
};

/**
 * Media di una valutazione (US-1104): la faccina del numero intero più vicino e la media con un decimale, con il testo
 * per il lettore di schermo («Media 4,3 su 5: buona»).
 */
export const ReviewFace = ({ average, withWord = false, className }: ReviewFaceProps) => {
  const { t } = useTranslation();
  const level = reviewLevel(average);
  const word = t(`review.level${level}`);
  return (
    <span className={cn('inline-flex items-center gap-1.5 whitespace-nowrap', className)}>
      <FaceIcon level={level} className="size-5" />
      <span aria-hidden="true">
        {withWord
          ? t('review.wordAndAverage', { word, average: formatAverage(average) })
          : formatAverage(average)}
      </span>
      <span className="sr-only">
        {t('review.averageLabel', { average: formatAverage(average), word: word.toLowerCase() })}
      </span>
    </span>
  );
};
