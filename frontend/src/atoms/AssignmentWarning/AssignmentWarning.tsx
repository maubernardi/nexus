import { TriangleAlert } from 'lucide-react';
import { useTranslation } from 'react-i18next';

import type { JobSlotAssignment } from '@/config/api/ticketApi';
import { assignmentList } from '@/lib/jobSlotAssignments';
import { cn } from '@/lib/utils';

type AssignmentWarningProps = {
  assignments: JobSlotAssignment[];
  id?: string;
  className?: string;
};

/**
 * Avviso ben visibile (US-605): la mansione è già assegnata a segnalazioni di altri tipi di tirocinio. Testo con
 * un'icona decorativa nel colore di avviso, non solo colore.
 */
export const AssignmentWarning = ({ assignments, id, className }: AssignmentWarningProps) => {
  const { t } = useTranslation();
  if (assignments.length === 0) return null;
  return (
    <p
      id={id}
      className={cn(
        'flex items-start gap-1.5 rounded-md border border-warning-foreground bg-warning px-2 py-1 text-sm font-medium text-warning-foreground',
        className,
      )}
    >
      <TriangleAlert aria-hidden="true" className="mt-0.5 size-4 shrink-0" />
      <span>
        {t('matching.alreadyAssigned', { count: assignments.length, list: assignmentList(assignments, t) })}
      </span>
    </p>
  );
};
