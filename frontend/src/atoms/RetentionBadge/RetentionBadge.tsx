import { useTranslation } from 'react-i18next';

import type { RetentionStatus } from '@/config/api/beneficiaryApi';
import { cn } from '@/lib/utils';

type RetentionBadgeProps = { status?: RetentionStatus };

/** Stato della conservazione dei dati di un beneficiario (US-1303); niente se normale. */
export const RetentionBadge = ({ status }: RetentionBadgeProps) => {
  const { t } = useTranslation();
  if (!status) return null;
  return (
    <span
      className={cn(
        'inline-block rounded-full border px-2 py-0.5 text-xs font-semibold whitespace-nowrap',
        status === 'TO_ANONYMIZE'
          ? 'border-warning-foreground bg-warning text-warning-foreground'
          : 'border-border bg-muted text-foreground',
      )}
    >
      {t(`retention.status.${status}`)}
    </span>
  );
};
