import { Ship } from 'lucide-react';
import { useTranslation } from 'react-i18next';

import { cn } from '@/lib/utils';

type TrainingShipBadgeProps = {
  /** `lg` nei titoli delle schede; `md` negli elenchi e nei dati. */
  size?: 'md' | 'lg';
  /** Accanto a un testo che dice già «Nave scuola» (per esempio l'etichetta della casella): solo decorativo. */
  decorative?: boolean;
  className?: string;
};

/**
 * Nave scuola (US-515): un bollino oro con la nave verde scura, dopo il nome dell'azienda e staccato da esso, ben
 * visibile anche negli elenchi fitti (PO, 26/11, dal collaudo). La nave sul fondo oro ha un contrasto di 4,9:1 (WCAG
 * 1.4.11); il significato è anche nel testo «Nave scuola», per il lettore di schermo e come suggerimento al passaggio del
 * mouse.
 */
export const TrainingShipBadge = ({ size = 'md', decorative = false, className }: TrainingShipBadgeProps) => {
  const { t } = useTranslation();
  const label = t('company.trainingShip');
  return (
    <span
      title={decorative ? undefined : label}
      aria-hidden={decorative || undefined}
      data-size={size}
      className={cn(
        'ml-1.5 inline-flex shrink-0 items-center justify-center rounded-full border-2 border-brand bg-gold align-middle text-brand shadow-sm',
        size === 'md' ? 'size-7' : 'size-10',
        className,
      )}
    >
      <Ship aria-hidden="true" strokeWidth={2.25} className={size === 'md' ? 'size-[18px]' : 'size-6'} />
      {!decorative && <span className="sr-only">{label}</span>}
    </span>
  );
};
