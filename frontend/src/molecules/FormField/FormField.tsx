import { type ReactNode } from 'react';
import { CircleAlert } from 'lucide-react';
import { useTranslation } from 'react-i18next';

import { cn } from '@/lib/utils';
import { InfoTip } from '@/molecules/InfoTip/InfoTip';

export type FieldControlProps = {
  id: string;
  'aria-invalid': boolean;
  'aria-describedby'?: string;
  'aria-required'?: boolean;
};

type FormFieldProps = {
  id: string;
  label: string;
  required?: boolean;
  hint?: string;
  error?: string;
  className?: string;
  /** Riceve id e attributi ARIA da applicare al controllo nativo. */
  children: (control: FieldControlProps) => ReactNode;
};

/** Etichetta visibile, testo di aiuto ed errore collegati al controllo (WCAG 1.3.1, 3.3.1, 3.3.2). */
export const FormField = ({
  id,
  label,
  required = false,
  hint,
  error,
  className,
  children,
}: FormFieldProps) => {
  const { t } = useTranslation();
  const hintId = hint ? `${id}-hint` : undefined;
  const errorId = error ? `${id}-error` : undefined;
  const describedBy = [hintId, errorId].filter(Boolean).join(' ') || undefined;

  return (
    <div className={cn('space-y-1.5', className)}>
      {/* il suggerimento sta in un'icona accanto all'etichetta: un testo sotto abbasserebbe il campo e lo
          disallineerebbe da quelli vicini (richiesta del PO, 25/10) */}
      <div className="relative flex min-h-6 items-center gap-1">
        <label htmlFor={id} className="text-sm font-medium">
          {label}
          {/* spazio come nodo di testo a sé: dentro lo span verrebbe normalizzato e il nome accessibile
              diventerebbe "Cognome(obbligatorio)" */}
          {required && ' '}
          {required && <span className="font-normal text-muted-foreground">({t('form.required')})</span>}
        </label>
        {hint && hintId && <InfoTip id={hintId} label={label} text={hint} />}
      </div>
      {children({
        id,
        'aria-invalid': Boolean(error),
        'aria-describedby': describedBy,
        'aria-required': required || undefined,
      })}
      {error && (
        <p id={errorId} className="flex items-start gap-1.5 text-sm font-medium text-destructive">
          <CircleAlert aria-hidden="true" className="mt-0.5 size-4 shrink-0" />
          {error}
        </p>
      )}
    </div>
  );
};
