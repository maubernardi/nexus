import { useEffect, useRef, useState, type RefObject } from 'react';

type ErrorSummaryFocus = {
  summaryRef: RefObject<HTMLDivElement | null>;
  /** Da chiamare quando l'invio fallisce: il focus andrà al riepilogo appena è nel DOM. */
  requestSummaryFocus: () => void;
};

/**
 * Sposta il focus sul riepilogo degli errori dopo un invio fallito (WCAG 3.3.1). La richiesta resta in sospeso finché il
 * riepilogo non è montato: react-hook-form invoca il callback degli errori prima di aggiornare `submitCount` ed
 * `errors`, quindi nel render in cui si chiede il focus il riepilogo può non esserci ancora.
 */
export const useErrorSummaryFocus = (submitCount: number, errors: unknown): ErrorSummaryFocus => {
  const summaryRef = useRef<HTMLDivElement>(null);
  const [pending, setPending] = useState(false);

  useEffect(() => {
    if (pending && summaryRef.current) {
      summaryRef.current.focus();
      setPending(false);
    }
  }, [pending, submitCount, errors]);

  return { summaryRef, requestSummaryFocus: () => setPending(true) };
};
