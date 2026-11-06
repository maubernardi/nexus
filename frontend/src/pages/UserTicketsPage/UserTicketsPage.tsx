import { useEffect, useId, useRef, useState, type FormEvent } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useParams } from 'react-router';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import { NativeSelect } from '@/components/ui/native-select';
import { apiError } from '@/config/api/errors';
import type { TransferGroup, TransferResult } from '@/config/api/ticketTransferApi';
import { useOpenTickets, useTransferTickets } from '@/config/hooks/useTicketTransfer';
import { useUser } from '@/config/hooks/useUsers';

const link = 'font-medium text-primary underline underline-offset-4';

/**
 * Segnalazioni aperte di un Tutor (US-111): l'ADMIN le sposta, progetto per progetto, su un altro Tutor attivo dello
 * stesso progetto; con ogni segnalazione passa anche il beneficiario.
 */
export const UserTicketsPage = () => {
  const { t } = useTranslation();
  const { id = '' } = useParams();
  const user = useUser(id);
  const groups = useOpenTickets(id);
  const [done, setDone] = useState<string | null>(null);
  const doneRef = useRef<HTMLParagraphElement>(null);

  useEffect(() => {
    if (done) doneRef.current?.focus();
  }, [done]);

  const onMoved = (result: TransferResult): void =>
    setDone(t('users.tickets.moved', { count: result.moved, name: result.tutorName }));

  const failed = (user.isError && !user.data) || (groups.isError && !groups.data);

  return (
    <div className="space-y-6">
      <PageHeading>
        {user.data
          ? t('users.tickets.heading', { name: `${user.data.firstName} ${user.data.lastName}` })
          : t('pages.userTickets.title')}
      </PageHeading>
      <p className="text-muted-foreground">{t('users.tickets.intro')}</p>
      {done && (
        <p
          ref={doneRef}
          role="status"
          tabIndex={-1}
          className="rounded-md border-l-4 border-primary bg-card p-4 font-medium"
        >
          {done}
        </p>
      )}
      {failed ? (
        <div role="alert" className="space-y-3 rounded-md border-2 border-destructive bg-card p-4">
          <p className="font-medium text-destructive">{t('users.tickets.loadError')}</p>
          <Button
            variant="outline"
            onClick={() => {
              void user.refetch();
              void groups.refetch();
            }}
          >
            {t('common.retry')}
          </Button>
        </div>
      ) : !user.data || !groups.data ? (
        <p>{t('common.loading')}</p>
      ) : groups.data.length === 0 ? (
        <p>{t('users.tickets.none')}</p>
      ) : (
        groups.data.map((g) => (
          <GroupForm
            key={`${g.project.id}-${g.tickets.map((x) => x.id).join()}`}
            group={g}
            onMoved={onMoved}
          />
        ))
      )}
      <Link to="/utenti" className={link}>
        {t('users.backToList')}
      </Link>
    </div>
  );
};

type GroupFormProps = { group: TransferGroup; onMoved: (result: TransferResult) => void };

/** Un progetto: scelta delle segnalazioni (tutte già selezionate) e del Tutor che le riceve. */
const GroupForm = ({ group, onMoved }: GroupFormProps) => {
  const { t } = useTranslation();
  const baseId = useId();
  const transfer = useTransferTickets();
  const [selected, setSelected] = useState<string[]>(() => group.tickets.map((x) => x.id));
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
                  {t('users.tickets.item', {
                    number: ticket.number,
                    name: `${ticket.beneficiary.lastName} ${ticket.beneficiary.firstName}`,
                    status: t(`enums.ticketStatus.${ticket.status}`),
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
