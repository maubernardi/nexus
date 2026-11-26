import { type Ref } from 'react';
import { useTranslation } from 'react-i18next';

import { RatingIcon } from '@/atoms/RatingDisplay/RatingDisplay';
import { Button } from '@/components/ui/button';
import { RATING_MAX, type RatingKind } from '@/lib/rating';

type RatingInputProps = {
  /** Id del gruppo e nome dei pulsanti di opzione. */
  id: string;
  legend: string;
  kind: RatingKind;
  /** {@code null} = nessuna valutazione: tutte le icone vuote. */
  value: number | null;
  onChange: (value: number | null) => void;
  hint?: string;
  error?: string;
  /** Al primo pulsante di opzione: il focus dal riepilogo degli errori. */
  ref?: Ref<HTMLInputElement>;
};

/**
 * Scelta di stelle o pollici (US-514): tutte le icone vuote finché non si sceglie; un clic riempie fino a quella scelta,
 * e «Togli» toglie la valutazione. Sono pulsanti di opzione nativi, nascosti alla vista: le frecce, il Tab e il lettore
 * di schermo funzionano come in ogni gruppo di opzioni, e ogni opzione ha il suo nome («3 stelle su 4»).
 */
export const RatingInput = ({ id, legend, kind, value, onChange, hint, error, ref }: RatingInputProps) => {
  const { t } = useTranslation();
  const max = RATING_MAX[kind];
  const hintId = `${id}-hint`;
  const errorId = `${id}-error`;
  const describedBy = [hint ? hintId : null, error ? errorId : null].filter(Boolean).join(' ') || undefined;

  return (
    <fieldset id={id} aria-describedby={describedBy} className="space-y-1">
      <legend className="text-sm font-medium">{legend}</legend>
      {hint && (
        <p id={hintId} className="text-sm text-muted-foreground">
          {hint}
        </p>
      )}
      <div className="flex flex-wrap items-center gap-1">
        {Array.from({ length: max }, (_, i) => {
          const n = i + 1;
          return (
            <label key={n} className="relative cursor-pointer">
              <input
                ref={n === 1 ? ref : undefined}
                type="radio"
                name={id}
                value={n}
                checked={value === n}
                onChange={() => onChange(n)}
                className="peer sr-only"
              />
              <span className="inline-flex rounded-md p-1 peer-focus-visible:outline-2 peer-focus-visible:outline-offset-1 peer-focus-visible:outline-ring hover:bg-muted">
                <RatingIcon kind={kind} filled={value !== null && n <= value} className="size-7" />
              </span>
              <span className="sr-only">{t(`rating.${kind}`, { count: n, max })}</span>
            </label>
          );
        })}
        {value !== null && (
          <Button
            type="button"
            variant="link"
            aria-label={t('rating.clearLabel', { legend })}
            onClick={() => onChange(null)}
          >
            {t('rating.clear')}
          </Button>
        )}
      </div>
      {error && (
        <p id={errorId} className="text-sm font-medium text-destructive">
          {error}
        </p>
      )}
    </fieldset>
  );
};
