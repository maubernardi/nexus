import { useTranslation } from 'react-i18next';

/** Dato di un'azienda arrivato vuoto dal database attuale, da completare (US-508): testo, non solo colore. */
export const ToCompleteBadge = () => {
  const { t } = useTranslation();
  return (
    <span className="inline-block rounded-full border border-warning-foreground bg-warning px-2 py-0.5 text-xs font-semibold whitespace-nowrap text-warning-foreground">
      {t('company.toComplete')}
    </span>
  );
};
