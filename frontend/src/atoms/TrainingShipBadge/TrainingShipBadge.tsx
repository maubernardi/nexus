import { Ship } from 'lucide-react';
import { useTranslation } from 'react-i18next';

import { cn } from '@/lib/utils';

type TrainingShipBadgeProps = {
  className?: string;
};

/**
 * Nave scuola (US-515): la nave accanto al nome dell'azienda, disegnata e non emoji, con il testo «Nave scuola» per il
 * lettore di schermo e come suggerimento al passaggio del mouse.
 */
export const TrainingShipBadge = ({ className }: TrainingShipBadgeProps) => {
  const { t } = useTranslation();
  const label = t('company.trainingShip');
  return (
    <span
      title={label}
      className={cn('inline-flex shrink-0 items-center align-middle text-primary', className)}
    >
      <Ship aria-hidden="true" className="size-4" />
      <span className="sr-only">{label}</span>
    </span>
  );
};
