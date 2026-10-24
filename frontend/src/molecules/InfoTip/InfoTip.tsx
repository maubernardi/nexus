import { useEffect, useRef, useState } from 'react';
import { Info } from 'lucide-react';
import { useTranslation } from 'react-i18next';

import { cn } from '@/lib/utils';

type InfoTipProps = {
  /** Id del testo: il campo lo usa in aria-describedby, così è letto anche a suggerimento chiuso. */
  id: string;
  /** Etichetta del campo, per il nome del pulsante ("Informazioni su Partita IVA"). */
  label: string;
  text: string;
};

/**
 * Suggerimento di un campo dietro un'icona "i" (WCAG 1.4.13): si apre al passaggio del mouse, al focus e al tocco;
 * resta aperto spostando il mouse sul testo; si chiude con Esc senza spostare il focus, o allontanando mouse e focus
 * (su smartphone, toccando altrove).
 */
export const InfoTip = ({ id, label, text }: InfoTipProps) => {
  const { t } = useTranslation();
  const [open, setOpen] = useState(false);
  const closeTimer = useRef<number | undefined>(undefined);

  const show = (): void => {
    window.clearTimeout(closeTimer.current);
    setOpen(true);
  };
  // piccolo ritardo: permette di spostare il mouse dall'icona al testo senza che si chiuda
  const hideSoon = (): void => {
    window.clearTimeout(closeTimer.current);
    closeTimer.current = window.setTimeout(() => setOpen(false), 150);
  };

  useEffect(() => {
    if (!open) return undefined;
    const onKey = (e: KeyboardEvent): void => {
      if (e.key === 'Escape') setOpen(false);
    };
    document.addEventListener('keydown', onKey);
    return () => document.removeEventListener('keydown', onKey);
  }, [open]);

  useEffect(() => () => window.clearTimeout(closeTimer.current), []);

  return (
    // solo per il mouse: tiene aperto il suggerimento passando dall'icona al testo; tastiera e tocco usano il pulsante
    // il testo si posiziona rispetto al primo antenato "relative" (la riga dell'etichetta nel FormField): occupa la
    // larghezza del campo e non esce mai dallo schermo, anche se l'icona è vicina al bordo
    // eslint-disable-next-line jsx-a11y/no-static-element-interactions
    <span className="inline-flex" onMouseEnter={show} onMouseLeave={hideSoon}>
      <button
        type="button"
        aria-label={t('form.hintFor', { label })}
        aria-expanded={open}
        aria-controls={id}
        // il clic apre e basta: con il mouse il passaggio l'ha già aperto, un "toggle" lo richiuderebbe subito
        onClick={show}
        onFocus={show}
        onBlur={hideSoon}
        className="inline-flex size-6 items-center justify-center rounded-full text-muted-foreground hover:text-foreground focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
      >
        <Info aria-hidden="true" className="size-4" />
      </button>
      {/* sempre nel DOM (descrive il campo); visibile solo da aperto */}
      <span
        id={id}
        role="tooltip"
        className={cn(
          open
            ? 'absolute inset-x-0 top-full z-20 mt-1 rounded-md border border-border bg-popover p-2 text-sm font-normal text-popover-foreground shadow-md'
            : 'sr-only',
        )}
      >
        {text}
      </span>
    </span>
  );
};
