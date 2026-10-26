import { useEffect, useRef, useState, type FormEvent } from 'react';
import { Plus } from 'lucide-react';
import { useForm, type FieldPath } from 'react-hook-form';
import { useTranslation } from 'react-i18next';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { NativeSelect } from '@/components/ui/native-select';
import type { CalendarClosure, WorkCalendar } from '@/config/api/calendarApi';
import { apiError } from '@/config/api/errors';
import {
  useAddClosure,
  useCalendar,
  useDeadlinePreview,
  useRemoveClosure,
  useUpdatePatron,
} from '@/config/hooks/useCalendar';
import { FormField } from '@/molecules/FormField/FormField';

const MONTHS = Array.from({ length: 12 }, (_, i) =>
  new Intl.DateTimeFormat('it-IT', { month: 'long' }).format(new Date(2026, i, 1)),
);
const DAYS = Array.from({ length: 31 }, (_, i) => i + 1);
const dateFormat = new Intl.DateTimeFormat('it-IT', { day: '2-digit', month: '2-digit', year: 'numeric' });
const longDateFormat = new Intl.DateTimeFormat('it-IT', {
  weekday: 'long',
  day: '2-digit',
  month: '2-digit',
  year: 'numeric',
});

const toDate = (iso: string): Date => {
  const [y, m, d] = iso.split('-').map(Number);
  return new Date(y, m - 1, d);
};

/** Data ISO (aaaa-mm-gg) in formato italiano, senza passare dal fuso orario. */
const formatDate = (iso: string): string => dateFormat.format(toDate(iso));

type Outcome = { message: string; error?: boolean };
type ClosureValues = { startDate: string; endDate: string; description: string };

/** Calendario unico dei giorni lavorativi (US-107): patrono e chiusure, per l'ADMIN. */
export const CalendarPage = () => {
  const { t } = useTranslation();
  const calendar = useCalendar();
  const [outcome, setOutcome] = useState<Outcome | null>(null);
  const outcomeRef = useRef<HTMLParagraphElement>(null);

  // ogni esito riceve il focus, così è annunciato
  useEffect(() => {
    if (outcome) outcomeRef.current?.focus();
  }, [outcome]);

  if (calendar.isError && !calendar.data) {
    return (
      <div className="space-y-4">
        <PageHeading>{t('pages.calendar.heading')}</PageHeading>
        <div role="alert" className="space-y-3 rounded-md border-2 border-destructive bg-card p-4">
          <p className="font-medium text-destructive">{t('common.loadError')}</p>
          <Button variant="outline" onClick={() => void calendar.refetch()}>
            {t('common.retry')}
          </Button>
        </div>
      </div>
    );
  }
  if (!calendar.data) {
    return <p role="status">{t('common.loading')}</p>;
  }

  return (
    <div className="space-y-6">
      <PageHeading>{t('pages.calendar.heading')}</PageHeading>
      <p className="text-muted-foreground">{t('pages.calendar.intro')}</p>

      {outcome && (
        <p
          ref={outcomeRef}
          tabIndex={-1}
          className={
            outcome.error
              ? 'rounded-md border-2 border-destructive bg-card p-4 font-medium text-destructive'
              : 'rounded-md border-l-4 border-primary bg-card p-4 font-medium'
          }
        >
          {outcome.message}
        </p>
      )}

      {/* key: dopo un salvataggio il modulo riparte dai dati aggiornati */}
      <PatronSection key={calendar.data.version} calendar={calendar.data} onOutcome={setOutcome} />
      <HolidaysSection />
      <ClosuresSection closures={calendar.data.closures} onOutcome={setOutcome} />
      <PreviewSection />
    </div>
  );
};

const PatronSection = ({
  calendar,
  onOutcome,
}: {
  calendar: WorkCalendar;
  onOutcome: (o: Outcome) => void;
}) => {
  const { t } = useTranslation();
  const updatePatron = useUpdatePatron();
  const [day, setDay] = useState(calendar.patronDay);
  const [month, setMonth] = useState(calendar.patronMonth);

  const onSubmit = (event: FormEvent<HTMLFormElement>): void => {
    event.preventDefault();
    updatePatron.mutate(
      { day, month, version: calendar.version },
      {
        onSuccess: (c) =>
          onOutcome({
            message: t('calendar.patronSaved', { date: `${c.patronDay} ${MONTHS[c.patronMonth - 1]}` }),
          }),
        onError: (error) => {
          const body = apiError(error);
          onOutcome({
            message: body?.fieldErrors?.[0]?.message ?? body?.message ?? t('form.errors.generic'),
            error: true,
          });
        },
      },
    );
  };

  return (
    <section
      aria-labelledby="calendar-patron"
      className="space-y-3 rounded-xl border border-border bg-card p-4"
    >
      <h2 id="calendar-patron" className="text-lg font-semibold text-primary">
        {t('calendar.patron')}
      </h2>
      <p className="text-sm text-muted-foreground">{t('calendar.patronHelp')}</p>
      <form onSubmit={onSubmit} className="flex flex-wrap items-end gap-3">
        <div className="space-y-1.5">
          <label htmlFor="calendar-patron-day" className="block text-sm font-medium">
            {t('calendar.day')}
          </label>
          <NativeSelect id="calendar-patron-day" value={day} onChange={(e) => setDay(Number(e.target.value))}>
            {DAYS.map((d) => (
              <option key={d} value={d}>
                {d}
              </option>
            ))}
          </NativeSelect>
        </div>
        <div className="space-y-1.5">
          <label htmlFor="calendar-patron-month" className="block text-sm font-medium">
            {t('calendar.month')}
          </label>
          <NativeSelect
            id="calendar-patron-month"
            value={month}
            onChange={(e) => setMonth(Number(e.target.value))}
          >
            {MONTHS.map((name, i) => (
              <option key={name} value={i + 1}>
                {name}
              </option>
            ))}
          </NativeSelect>
        </div>
        <Button type="submit" size="lg" variant="outline" disabled={updatePatron.isPending}>
          {t('calendar.savePatron')}
        </Button>
      </form>
    </section>
  );
};

const HolidaysSection = () => {
  const { t } = useTranslation();
  const holidays = t('calendar.holidays', { returnObjects: true }) as string[];
  return (
    <section
      aria-labelledby="calendar-holidays"
      className="space-y-3 rounded-xl border border-border bg-card p-4"
    >
      <h2 id="calendar-holidays" className="text-lg font-semibold text-primary">
        {t('calendar.holidaysHeading')}
      </h2>
      <p className="text-sm text-muted-foreground">{t('calendar.holidaysHelp')}</p>
      <ul className="grid list-disc gap-x-8 gap-y-1 pl-5 sm:grid-cols-2">
        {holidays.map((h) => (
          <li key={h}>{h}</li>
        ))}
      </ul>
    </section>
  );
};

const ClosuresSection = ({
  closures,
  onOutcome,
}: {
  closures: CalendarClosure[];
  onOutcome: (o: Outcome) => void;
}) => {
  const { t } = useTranslation();
  const addClosure = useAddClosure();
  const removeClosure = useRemoveClosure();
  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<ClosureValues>({ defaultValues: { startDate: '', endDate: '', description: '' } });

  const period = (c: CalendarClosure): string =>
    c.startDate === c.endDate
      ? formatDate(c.startDate)
      : t('calendar.period', { from: formatDate(c.startDate), to: formatDate(c.endDate) });

  const onValid = async (v: ClosureValues): Promise<void> => {
    try {
      await addClosure.mutateAsync({
        startDate: v.startDate,
        endDate: v.endDate || null,
        description: v.description.trim(),
      });
      reset();
      onOutcome({ message: t('calendar.closureAdded') });
    } catch (error) {
      const fieldErrors = apiError(error)?.fieldErrors ?? [];
      if (fieldErrors.length > 0) {
        fieldErrors.forEach((fe) =>
          setError(fe.field as FieldPath<ClosureValues>, { type: 'server', message: fe.message }),
        );
      } else {
        onOutcome({ message: t('form.errors.generic'), error: true });
      }
    }
  };

  const remove = (c: CalendarClosure): void => {
    removeClosure.mutate(c.id, {
      onSuccess: () => onOutcome({ message: t('calendar.closureRemoved', { description: c.description }) }),
      onError: () => onOutcome({ message: t('form.errors.generic'), error: true }),
    });
  };

  const required = { value: true, message: t('form.errors.required') };

  return (
    <section
      aria-labelledby="calendar-closures"
      className="space-y-4 rounded-xl border border-border bg-card p-4"
    >
      <h2 id="calendar-closures" className="text-lg font-semibold text-primary">
        {t('calendar.closures')}
      </h2>
      <p className="text-sm text-muted-foreground">{t('calendar.closuresHelp')}</p>

      <form noValidate onSubmit={handleSubmit(onValid)} className="space-y-3">
        <div className="grid gap-4 sm:grid-cols-2">
          <FormField
            id="calendar-closure-start"
            label={t('calendar.from')}
            required
            error={errors.startDate?.message}
          >
            {(p) => <Input {...p} type="date" {...register('startDate', { required })} />}
          </FormField>
          <FormField
            id="calendar-closure-end"
            label={t('calendar.to')}
            hint={t('calendar.toHint')}
            error={errors.endDate?.message}
          >
            {(p) => <Input {...p} type="date" {...register('endDate')} />}
          </FormField>
        </div>
        <FormField
          id="calendar-closure-description"
          label={t('calendar.description')}
          required
          error={errors.description?.message}
        >
          {(p) => (
            <Input
              {...p}
              {...register('description', {
                validate: (v) => v.trim() !== '' || t('form.errors.required'),
                maxLength: { value: 200, message: t('form.errors.maxLength', { max: 200 }) },
              })}
            />
          )}
        </FormField>
        <Button type="submit" size="lg" variant="outline" disabled={isSubmitting}>
          <Plus aria-hidden="true" />
          {t('calendar.addClosure')}
        </Button>
      </form>

      {closures.length === 0 ? (
        <p>{t('calendar.noClosures')}</p>
      ) : (
        <ul
          aria-label={t('calendar.closuresList')}
          className="divide-y divide-border rounded-md border border-border"
        >
          {closures.map((c) => (
            <li key={c.id} className="flex flex-wrap items-center justify-between gap-3 p-3">
              <div>
                <p className="font-medium tabular-nums">{period(c)}</p>
                <p className="break-words">{c.description}</p>
              </div>
              <Button
                size="sm"
                variant="outline"
                onClick={() => remove(c)}
                disabled={removeClosure.isPending}
              >
                {t('calendar.remove')}
                {/* spazio come nodo a sé: nome accessibile "Rimuovi Ferie estive" */}{' '}
                <span className="sr-only">{c.description}</span>
              </Button>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
};

/** Prova del calcolo dei giorni lavorativi (US-703): promemoria e scadenza del timer della proposta. */
const PreviewSection = () => {
  const { t } = useTranslation();
  const [from, setFrom] = useState('');
  const preview = useDeadlinePreview(from);

  const onSubmit = (event: FormEvent<HTMLFormElement>): void => {
    event.preventDefault();
    const value = new FormData(event.currentTarget).get('from');
    setFrom(typeof value === 'string' ? value : '');
  };

  return (
    <section
      aria-labelledby="calendar-preview"
      className="space-y-3 rounded-xl border border-border bg-card p-4"
    >
      <h2 id="calendar-preview" className="text-lg font-semibold text-primary">
        {t('calendar.previewHeading')}
      </h2>
      <p className="text-sm text-muted-foreground">{t('calendar.previewHelp')}</p>
      <form onSubmit={onSubmit} className="flex flex-wrap items-end gap-3">
        <div className="space-y-1.5">
          <label htmlFor="calendar-preview-from" className="block text-sm font-medium">
            {t('calendar.previewFrom')}
          </label>
          <Input id="calendar-preview-from" name="from" type="date" required className="w-auto" />
        </div>
        <Button type="submit" size="lg" variant="outline">
          {t('calendar.previewSubmit')}
        </Button>
      </form>
      <div role="status">
        {preview.data && (
          <dl className="grid gap-x-6 gap-y-1 sm:grid-cols-[max-content_1fr]">
            <dt className="font-medium">{t('calendar.reminder')}</dt>
            <dd>{longDateFormat.format(toDate(preview.data.reminder))}</dd>
            <dt className="font-medium">{t('calendar.deadline')}</dt>
            <dd>{longDateFormat.format(toDate(preview.data.deadline))}</dd>
          </dl>
        )}
        {preview.isError && <p className="font-medium text-destructive">{t('common.loadError')}</p>}
      </div>
    </section>
  );
};
