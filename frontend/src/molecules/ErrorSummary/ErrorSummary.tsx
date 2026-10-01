import { type Ref } from 'react';
import { useTranslation } from 'react-i18next';

export type SummaryError = { fieldId: string; message: string };

type ErrorSummaryProps = {
  errors: readonly SummaryError[];
  ref?: Ref<HTMLDivElement>;
};

/**
 * Riepilogo degli errori in cima al modulo (WCAG 3.3.1): annunciato e focalizzabile, con un collegamento a ogni campo.
 * Il chiamante sposta il focus qui all'invio non riuscito.
 */
export const ErrorSummary = ({ errors, ref }: ErrorSummaryProps) => {
  const { t } = useTranslation();
  if (errors.length === 0) {
    return null;
  }

  const focusField = (event: React.MouseEvent<HTMLAnchorElement>, fieldId: string): void => {
    const field = document.getElementById(fieldId);
    if (field) {
      event.preventDefault();
      field.focus();
    }
  };

  return (
    <div
      ref={ref}
      role="alert"
      tabIndex={-1}
      aria-labelledby="error-summary-title"
      className="rounded-md border-2 border-destructive bg-card p-4"
    >
      <h2 id="error-summary-title" className="text-base font-semibold text-destructive">
        {t('form.errorSummary', { count: errors.length })}
      </h2>
      <ul className="mt-2 list-disc space-y-1 pl-5">
        {errors.map((error) => (
          <li key={error.fieldId}>
            <a
              href={`#${error.fieldId}`}
              onClick={(e) => focusField(e, error.fieldId)}
              className="text-primary underline underline-offset-4"
            >
              {error.message}
            </a>
          </li>
        ))}
      </ul>
    </div>
  );
};
