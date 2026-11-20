import type { TFunction } from 'i18next';

import type { JobSlotAssignment } from '@/config/api/ticketApi';

/** «n. 12 (Extracurriculare), n. 15 (tipo non indicato)»: le segnalazioni a cui è assegnata la mansione (US-605). */
export const assignmentList = (assignments: JobSlotAssignment[], t: TFunction): string =>
  assignments
    .map((a) =>
      t('jobSlot.assignment', {
        number: a.ticketNumber,
        type: a.internshipType?.name ?? t('jobSlot.noInternshipType'),
      }),
    )
    .join(', ');
