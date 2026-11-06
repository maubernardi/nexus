import { useId, useRef, useState, type FormEvent } from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router';

import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import { NativeSelect } from '@/components/ui/native-select';
import { apiError } from '@/config/api/errors';
import type { TransferGroup, TransferResult } from '@/config/api/ticketTransferApi';
import { useTransferTickets } from '@/config/hooks/useTicketTransfer';

const link = 'font-medium text-primary underline underline-offset-4';

type TransferGroupFormProps = {
  group: TransferGroup;
  onMoved: (result: TransferResult) => void;
  /** Mostra il Tutor di ogni segnalazione: nel coordinamento sono di Tutor diversi (US-112). */
  showTutor?: boolean;
};

/**
 * Un progetto: scelta delle segnalazioni e del Tutor che le riceve (US-111). Per l'ADMIN le segnalazioni sono tutte
 * dello stesso Tutor e partono già selezionate; per il Coordinatore (US-112) sono di tutto il progetto e si scelgono.
 */
export const TransferGroupForm = ({ group, onMoved, showTutor = false }: TransferGroupFormProps) => {
  const { t } = useTranslation();
  const baseId = useId();
  const transfer = useTransferTickets();
  const [selected, setSelected] = useState<string[]>(() => (showTutor ? [] : group.tickets.map((x) => x.id)));
  const [tutorId, setTutorId] = useState('');
  const [error, setError] = useState<string | null>(null);
  const errorRef = useRef<HTMLParagraphElement>(null);
  const headingId = `${baseId}-heading`;
  const selectId = `${baseId}-tutor`;

  const showError = (message: string): void => {
    setError(message);
    requestAnimationFrame(() => errorRef.current?.focus());
  };

  const onSubmit = async (event: FormEvent<HTMLFormElement>): Promise<void> => {
    event.preventDefault();
    if (selected.length === 0) return showError(t('users.tickets.errors.noTicket'));
    if (tutorId === '') return showError(t('users.tickets.errors.noTutor'));
    setError(null);
    try {
      onMoved(await transfer.mutateAsync({ ticketIds: selected, tutorId }));
    } catch (e) {
      const body = apiError(e);
      showError(
        body?.fieldErrors?.[0]?.message ??
          (body?.status === 400 && body.message ? body.message : t('users.error')),
      );
    }
  };

  return (
    <section aria-labelledby={headingId} className="space-y-4 rounded-xl border border-border bg-card p-4">
      <h2 id={headingId} className="text-lg font-semibold text-primary">
        {t('users.tickets.project', { name: group.project.name, count: group.tickets.length })}
      </h2>
      <form noValidate onSubmit={(e) => void onSubmit(e)} className="space-y-4">
        {error && (
          <p
            ref={errorRef}
            role="alert"
            tabIndex={-1}
            className="rounded-md border-2 border-destructive bg-card p-4 font-medium text-destructive"
          >
            {error}
          </p>
        )}
        <fieldset className="space-y-2">
          <legend className="mb-2 font-medium">{t('users.tickets.legend')}</legend>
          {group.tickets.map((ticket) => {
            const inputId = `${baseId}-${ticket.id}`;
            return (
              <label
                key={ticket.id}
                htmlFor={inputId}
                className="flex min-h-11 flex-wrap items-center gap-x-3 gap-y-1 rounded-md border border-border px-3 py-2"
              >
                <Checkbox
                  id={inputId}
                  checked={selected.includes(ticket.id)}
                  onChange={(e) =>
                    setSelected((current) =>
                      e.target.checked ? [...current, ticket.id] : current.filter((v) => v !== ticket.id),
                    )
                  }
                />
                <span>
                  {t(showTutor ? 'users.tickets.itemWithTutor' : 'users.tickets.item', {
                    number: ticket.number,
                    name: `${ticket.beneficiary.lastName} ${ticket.beneficiary.firstName}`,
                    status: t(`enums.ticketStatus.${ticket.status}`),
                    tutor: ticket.tutorName,
                  })}
                </span>
              </label>
            );
          })}
        </fieldset>
        {group.targets.length === 0 ? (
          <p>
            {t('users.tickets.noTargets', { name: group.project.name })}{' '}
            <Link to="/utenti" className={link}>
              {t('users.tickets.toUsers')}
            </Link>
          </p>
        ) : (
          <div className="flex flex-wrap items-end gap-4">
            <div className="space-y-1">
              <label htmlFor={selectId} className="block text-sm font-medium">
                {t('users.tickets.target')}
              </label>
              <NativeSelect id={selectId} value={tutorId} onChange={(e) => setTutorId(e.target.value)}>
                <option value="">{t('form.select')}</option>
                {group.targets.map((target) => (
                  <option key={target.id} value={target.id}>
                    {target.name}
                  </option>
                ))}
              </NativeSelect>
            </div>
            <Button type="submit" size="lg" disabled={transfer.isPending}>
              {transfer.isPending
                ? t('users.tickets.moving')
                : t('users.tickets.move', { count: selected.length })}
            </Button>
          </div>
        )}
      </form>
    </section>
  );
};
