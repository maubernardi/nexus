import { useWatch, type Control, type FieldErrors, type UseFormRegister } from 'react-hook-form';
import { useTranslation } from 'react-i18next';

import { Input } from '@/components/ui/input';
import type { InternshipFormValues } from '@/organisms/InternshipForm/InternshipForm';
import {
  DAYS,
  minutesToHoursText,
  slotMinutes,
  weeklyMinutes,
  type SlotValues,
} from '@/organisms/InternshipForm/schedule';
import { FormField } from '@/molecules/FormField/FormField';

type ScheduleFieldsProps = {
  control: Control<InternshipFormValues>;
  register: UseFormRegister<InternshipFormValues>;
  errors: FieldErrors<InternshipFormValues>;
  /** Regole di validazione delle fasce e delle ore, definite dal modulo che conosce tutti i valori. */
  rules: {
    firstSlotEnd: (v: string) => true | string;
    secondSlotStart: (v: string) => true | string;
    secondSlotEnd: (v: string) => true | string;
    hours: (k: number) => (v: string) => true | string;
  };
  /** Monte ore sui dati salvati, in minuti (US-1002). */
  totalMinutes?: number | null;
  fieldId: (path: string) => string;
};

/**
 * Griglia oraria e monte ore della scheda (US-1002): due fasce modificabili, per ogni giorno le fasce scelte e le ore
 * (vuote = fasce intere), il totale settimanale mentre si compila e il monte ore calcolato dal server.
 */
export const ScheduleFields = ({
  control,
  register,
  errors,
  rules,
  totalMinutes,
  fieldId,
}: ScheduleFieldsProps) => {
  const { t } = useTranslation();
  const [days, firstSlotStart, firstSlotEnd, secondSlotStart, secondSlotEnd] = useWatch({
    control,
    name: ['days', 'firstSlotStart', 'firstSlotEnd', 'secondSlotStart', 'secondSlotEnd'],
  });
  const slots: SlotValues = { firstSlotStart, firstSlotEnd, secondSlotStart, secondSlotEnd };
  const hours = (minutes: number): string =>
    t('internship.schedule.hoursValue', { count: minutes / 60, value: minutesToHoursText(minutes) });
  const weekly = weeklyMinutes(slots, days);
  const first = t('internship.schedule.first');
  const second = t('internship.schedule.second');
  const hoursHintId = 'internship-schedule-hours-hint';
  const holidaysHintId = 'internship-schedule-holidays-hint';
  const daysError = errors.days?.message;

  const slotField = (name: keyof SlotValues, label: string, validate?: (v: string) => true | string) => (
    <FormField id={fieldId(name)} label={label} error={errors[name]?.message}>
      {(p) => <Input {...p} type="time" className="max-w-36" {...register(name, { validate })} />}
    </FormField>
  );

  return (
    <section aria-labelledby="internship-section-schedule" className="space-y-4">
      <h2 id="internship-section-schedule" className="text-lg font-semibold text-primary">
        {t('internship.schedule.heading')}
      </h2>
      <p className="text-muted-foreground">{t('internship.schedule.intro')}</p>

      <fieldset className="space-y-2" aria-describedby="internship-schedule-slots-hint">
        <legend className="text-sm font-medium">{t('internship.schedule.slots')}</legend>
        <p id="internship-schedule-slots-hint" className="text-sm text-muted-foreground">
          {t('internship.schedule.slotsHint')}
        </p>
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {slotField('firstSlotStart', t('internship.schedule.start', { slot: first }))}
          {slotField('firstSlotEnd', t('internship.schedule.end', { slot: first }), rules.firstSlotEnd)}
          {slotField(
            'secondSlotStart',
            t('internship.schedule.start', { slot: second }),
            rules.secondSlotStart,
          )}
          {slotField('secondSlotEnd', t('internship.schedule.end', { slot: second }), rules.secondSlotEnd)}
        </div>
      </fieldset>

      <div className="overflow-x-auto">
        <table className="w-full text-left">
          <caption className="sr-only">{t('internship.schedule.caption')}</caption>
          <thead>
            <tr className="text-sm">
              <th scope="col" className="py-2 pr-3 font-medium">
                {t('internship.schedule.day')}
              </th>
              <th scope="col" className="px-2 py-2 font-medium">
                {t('internship.schedule.columnSlot', { slot: first, from: firstSlotStart, to: firstSlotEnd })}
              </th>
              <th scope="col" className="px-2 py-2 font-medium">
                {t('internship.schedule.columnSlot', {
                  slot: second,
                  from: secondSlotStart,
                  to: secondSlotEnd,
                })}
              </th>
              <th scope="col" className="py-2 pl-2 font-medium">
                {t('internship.schedule.hours')}
              </th>
            </tr>
          </thead>
          <tbody>
            {DAYS.map((dow, k) => {
              const day = t(`internship.schedule.days.${dow}`);
              const error = errors.days?.[k]?.hours?.message;
              const hoursId = fieldId(`days.${k}.hours`);
              const full = days[k].first || days[k].second ? slotMinutes(slots, days[k]) : 0;
              return (
                <tr key={dow} className="border-t border-border align-top">
                  <th scope="row" className="py-3 pr-3 font-medium">
                    {day}
                  </th>
                  <td className="px-2 py-1">
                    {/* casella dentro un'etichetta ampia: bersaglio di almeno 24px (WCAG 2.5.8) */}
                    <label className="inline-flex p-2">
                      <input
                        // il riepilogo degli errori della griglia porta qui
                        id={k === 0 ? fieldId('days') : undefined}
                        type="checkbox"
                        className="size-5 accent-primary"
                        aria-label={t('internship.schedule.daySlot', { day, slot: first })}
                        {...register(`days.${k}.first`)}
                      />
                    </label>
                  </td>
                  <td className="px-2 py-1">
                    <label className="inline-flex p-2">
                      <input
                        type="checkbox"
                        className="size-5 accent-primary"
                        aria-label={t('internship.schedule.daySlot', { day, slot: second })}
                        {...register(`days.${k}.second`)}
                      />
                    </label>
                  </td>
                  <td className="py-1 pl-2">
                    <Input
                      id={hoursId}
                      inputMode="decimal"
                      autoComplete="off"
                      className="w-24"
                      placeholder={full > 0 ? minutesToHoursText(full) : undefined}
                      aria-label={t('internship.schedule.dayHours', { day: day.toLowerCase() })}
                      aria-invalid={Boolean(error)}
                      aria-describedby={[hoursHintId, error ? `${hoursId}-error` : null]
                        .filter(Boolean)
                        .join(' ')}
                      {...register(`days.${k}.hours`, { validate: rules.hours(k) })}
                    />
                    {error && (
                      <p id={`${hoursId}-error`} className="mt-1 text-sm font-medium text-destructive">
                        {error}
                      </p>
                    )}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
      <p id={hoursHintId} className="text-sm text-muted-foreground">
        {t('internship.schedule.hoursHint')}
      </p>
      {daysError && <p className="text-sm font-medium text-destructive">{daysError}</p>}
      <p className="font-medium">
        {weekly === null
          ? t('internship.schedule.weeklyNone')
          : t('internship.schedule.weekly', { hours: hours(weekly) })}
      </p>

      <div className="space-y-1">
        <label className="flex items-start gap-3">
          <input
            id={fieldId('excludeHolidays')}
            type="checkbox"
            className="mt-0.5 size-5 accent-primary"
            aria-describedby={holidaysHintId}
            {...register('excludeHolidays')}
          />
          <span>{t('internship.schedule.excludeHolidays')}</span>
        </label>
        <p id={holidaysHintId} className="text-sm text-muted-foreground">
          {t('internship.schedule.excludeHolidaysHint')}
        </p>
      </div>

      <div className="space-y-1 rounded-md border-l-4 border-primary bg-muted p-3">
        {typeof totalMinutes === 'number' ? (
          <>
            <p className="font-medium">{t('internship.schedule.total', { hours: hours(totalMinutes) })}</p>
            <p className="text-sm text-muted-foreground">{t('internship.schedule.totalNote')}</p>
          </>
        ) : (
          <p>{t('internship.schedule.totalMissing')}</p>
        )}
      </div>
    </section>
  );
};
