import { useState } from 'react';
import { Plus, Trash2 } from 'lucide-react';
import { Controller, useFieldArray, useForm, type FieldPath } from 'react-hook-form';
import { useTranslation } from 'react-i18next';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { NativeSelect } from '@/components/ui/native-select';
import { Textarea } from '@/components/ui/textarea';
import { apiError, toFormPath } from '@/config/api/errors';
import type { Internship, InternshipForm as InternshipPayload } from '@/config/api/internshipApi';
import type { ReferenceItem } from '@/config/api/referenceApi';
import { useErrorSummaryFocus } from '@/hooks/useErrorSummaryFocus';
import { ErrorSummary, type SummaryError } from '@/molecules/ErrorSummary/ErrorSummary';
import { FormField } from '@/molecules/FormField/FormField';
import { MunicipalityField } from '@/molecules/MunicipalityField/MunicipalityField';
import {
  isUnchosen,
  toMunicipalityValue,
  type MunicipalityValue,
} from '@/molecules/MunicipalityField/municipalityValue';
import {
  DAYS,
  minutesToHoursText,
  parseHours,
  slotMinutes,
  timeToMinutes,
  weeklyMinutes,
  type ScheduleDayValues,
} from '@/organisms/InternshipForm/schedule';
import { ScheduleFields } from '@/organisms/InternshipForm/ScheduleFields';

type Values = {
  internshipTypeId: string;
  startDate: string;
  expectedEndDate: string;
  site: string;
  siteMunicipality: MunicipalityValue;
  atecoCodeId: string;
  companyTutors: { fullName: string; role: string; contact: string }[];
  fiscalCode: string;
  birthDate: string;
  birthPlace: string;
  residenceMunicipality: MunicipalityValue;
  residenceStreet: string;
  residenceStreetNumber: string;
  suspensions: { from: string; to: string }[];
  notes: string;
  /** Griglia oraria (US-1002): fasce "hh:mm" e i sette giorni da lunedì. */
  firstSlotStart: string;
  firstSlotEnd: string;
  secondSlotStart: string;
  secondSlotEnd: string;
  excludeHolidays: boolean;
  days: ScheduleDayValues[];
};

export type InternshipFormValues = Values;

type Scalar = Exclude<
  keyof Values,
  | 'companyTutors'
  | 'suspensions'
  | 'days'
  | 'excludeHolidays'
  | 'firstSlotStart'
  | 'firstSlotEnd'
  | 'secondSlotStart'
  | 'secondSlotEnd'
>;

/** Totale settimanale massimo: segnaposto di US-1001, da confermare con i moduli ufficiali (Q5). */
const MAX_WEEKLY_MINUTES = 40 * 60;

const fieldId = (path: string): string => `internship-${path.replace(/\./g, '-')}`;

// l'API omette i campi vuoti: null e assente valgono uguale
const toValues = (i: Internship): Values => ({
  internshipTypeId: i.internshipType?.id ?? '',
  startDate: i.startDate ?? '',
  expectedEndDate: i.expectedEndDate ?? '',
  site: i.site ?? '',
  siteMunicipality: toMunicipalityValue(i.siteMunicipality ?? undefined),
  atecoCodeId: i.atecoCodeId ?? '',
  companyTutors: i.companyTutors.map((c) => ({
    fullName: c.fullName,
    role: c.role ?? '',
    contact: c.contact ?? '',
  })),
  fiscalCode: i.fiscalCode ?? '',
  birthDate: i.birthDate ?? '',
  birthPlace: i.birthPlace ?? '',
  residenceMunicipality: toMunicipalityValue(i.residenceMunicipality ?? undefined),
  residenceStreet: i.residenceStreet ?? '',
  residenceStreetNumber: i.residenceStreetNumber ?? '',
  suspensions: i.suspensions.map((s) => ({ from: s.from, to: s.to })),
  notes: i.notes ?? '',
  // l'API manda "hh:mm:ss", il campo orario vuole "hh:mm"
  firstSlotStart: i.firstSlotStart.slice(0, 5),
  firstSlotEnd: i.firstSlotEnd.slice(0, 5),
  secondSlotStart: i.secondSlotStart.slice(0, 5),
  secondSlotEnd: i.secondSlotEnd.slice(0, 5),
  excludeHolidays: i.excludeHolidays,
  days: DAYS.map((dow) => {
    const day = i.schedule.find((d) => d.dayOfWeek === dow);
    return {
      first: day?.firstSlot ?? false,
      second: day?.secondSlot ?? false,
      hours: typeof day?.minutes === 'number' ? minutesToHoursText(day.minutes) : '',
    };
  }),
});

const toPayload = (v: Values, version: number | null): InternshipPayload => ({
  internshipTypeId: v.internshipTypeId || null,
  startDate: v.startDate || null,
  expectedEndDate: v.expectedEndDate || null,
  site: v.site.trim() || null,
  siteMunicipalityId: v.siteMunicipality.municipality?.id ?? null,
  atecoCodeId: v.atecoCodeId || null,
  companyTutors: v.companyTutors.map((c) => ({
    fullName: c.fullName.trim(),
    role: c.role.trim() || null,
    contact: c.contact.trim() || null,
  })),
  fiscalCode: v.fiscalCode.trim().toUpperCase() || null,
  birthDate: v.birthDate || null,
  birthPlace: v.birthPlace.trim() || null,
  residenceMunicipalityId: v.residenceMunicipality.municipality?.id ?? null,
  residenceStreet: v.residenceStreet.trim() || null,
  residenceStreetNumber: v.residenceStreetNumber.trim() || null,
  suspensions: v.suspensions.map((s) => ({ from: s.from, to: s.to })),
  notes: v.notes.trim() || null,
  firstSlotStart: v.firstSlotStart,
  firstSlotEnd: v.firstSlotEnd,
  secondSlotStart: v.secondSlotStart,
  secondSlotEnd: v.secondSlotEnd,
  excludeHolidays: v.excludeHolidays,
  // solo i giorni con almeno una fascia
  schedule: v.days.flatMap((d, k) =>
    d.first || d.second
      ? [{ dayOfWeek: DAYS[k], firstSlot: d.first, secondSlot: d.second, minutes: parseHours(d.hours) }]
      : [],
  ),
  version,
});

/**
 * Campo del modulo per un errore del server. La griglia inviata ha solo i giorni con una fascia: `schedule[i]` si
 * riporta al giorno della settimana.
 */
const toFieldPath = (field: string, payload: InternshipPayload): FieldPath<Values> => {
  // il server parla dell'id del comune, il modulo del campo del comune (US-1007)
  if (field === 'siteMunicipalityId' || field === 'residenceMunicipalityId') {
    return field.replace(/Id$/, '') as FieldPath<Values>;
  }
  const match = /^schedule(?:\[(\d+)\](\.minutes)?)?/.exec(field);
  if (!match) return toFormPath(field) as FieldPath<Values>;
  const day = match[1] === undefined ? undefined : payload.schedule[Number(match[1])];
  return day && match[2] ? `days.${day.dayOfWeek - 1}.hours` : 'days';
};

/** Tipi attivi più quello già scelto, anche se nel frattempo disattivato. */
const withCurrent = (items: ReferenceItem[] | undefined, current?: ReferenceItem | null): ReferenceItem[] => {
  const list = items ?? [];
  return current && !list.some((i) => i.id === current.id) ? [current, ...list] : list;
};

const INCLUSION_ITEMS = ['careAgency', 'caseManagerName', 'caseManagerEmail', 'caseManagerPhone'] as const;
type InclusionItem = (typeof INCLUSION_ITEMS)[number];

type InternshipFormProps = {
  internship: Internship;
  onSubmit: (payload: InternshipPayload) => Promise<Internship>;
  onSuccess: () => void;
  onConflict: (reason: string) => void;
};

/**
 * Modulo della scheda del tirocinio (US-1001): ogni campo è facoltativo, si salva anche una parte. I controlli qui
 * anticipano quelli del server, che restano decisivi (codice fiscale, ATECO, tipo).
 */
export const InternshipForm = ({ internship, onSubmit, onSuccess, onConflict }: InternshipFormProps) => {
  const { t } = useTranslation();
  const [genericError, setGenericError] = useState(false);
  // il tipo «Di inclusione» richiede ente e case manager del beneficiario (US-206), salvo se è già quello scelto
  const typeOptions = withCurrent(internship.internshipTypes, internship.internshipType);
  const inclusionMissing = (internship.inclusionMissing ?? []).filter((m): m is InclusionItem =>
    (INCLUSION_ITEMS as readonly string[]).includes(m),
  );
  const isUnavailable = (type: ReferenceItem): boolean =>
    type.code === 'INCLUSIONE' && inclusionMissing.length > 0 && type.id !== internship.internshipType?.id;
  const inclusionType = typeOptions.find(isUnavailable);
  const {
    register,
    control,
    handleSubmit,
    getValues,
    setError,
    formState: { errors, isSubmitting, submitCount },
  } = useForm<Values>({
    defaultValues: toValues(internship),
    mode: 'onSubmit',
    reValidateMode: 'onChange',
    shouldFocusError: false,
  });
  const tutorRows = useFieldArray({ control, name: 'companyTutors' });
  const suspensionRows = useFieldArray({ control, name: 'suspensions' });
  const { summaryRef, requestSummaryFocus } = useErrorSummaryFocus(submitCount, errors);
  const today = new Date().toISOString().slice(0, 10);
  const max = (n: number) => ({ value: n, message: t('form.errors.maxLength', { max: n }) });
  const required = (v: string) => v.trim() !== '' || t('form.errors.required');
  const chosen = (v: MunicipalityValue) => !isUnchosen(v) || t('municipalityField.choose');

  const labels: Record<Scalar, string> = {
    internshipTypeId: t('internship.fields.internshipType'),
    startDate: t('internship.fields.startDate'),
    expectedEndDate: t('internship.fields.expectedEndDate'),
    site: t('internship.fields.site'),
    siteMunicipality: t('internship.fields.siteMunicipality'),
    atecoCodeId: t('internship.fields.atecoCode'),
    fiscalCode: t('internship.fields.fiscalCode'),
    birthDate: t('internship.fields.birthDate'),
    birthPlace: t('internship.fields.birthPlace'),
    residenceMunicipality: t('internship.fields.residenceMunicipality'),
    residenceStreet: t('internship.fields.residenceStreet'),
    residenceStreetNumber: t('internship.fields.residenceStreetNumber'),
    notes: t('internship.fields.notes'),
  };

  // riepilogo nell'ordine della pagina
  const summary: SummaryError[] = [];
  const push = (path: string, label: string, message?: string): void => {
    if (message) summary.push({ fieldId: fieldId(path), message: `${label}: ${message}` });
  };
  (
    ['internshipTypeId', 'startDate', 'expectedEndDate', 'site', 'siteMunicipality', 'atecoCodeId'] as const
  ).forEach((f) => push(f, labels[f], errors[f]?.message));
  const first = t('internship.schedule.first');
  const second = t('internship.schedule.second');
  push('firstSlotStart', t('internship.schedule.start', { slot: first }), errors.firstSlotStart?.message);
  push('firstSlotEnd', t('internship.schedule.end', { slot: first }), errors.firstSlotEnd?.message);
  push('secondSlotStart', t('internship.schedule.start', { slot: second }), errors.secondSlotStart?.message);
  push('secondSlotEnd', t('internship.schedule.end', { slot: second }), errors.secondSlotEnd?.message);
  DAYS.forEach((dow, k) =>
    push(
      `days.${k}.hours`,
      t('internship.schedule.dayHours', { day: t(`internship.schedule.days.${dow}`).toLowerCase() }),
      errors.days?.[k]?.hours?.message,
    ),
  );
  push('days', t('internship.schedule.heading'), errors.days?.message);
  tutorRows.fields.forEach((_, i) => {
    const n = i + 1;
    push(
      `companyTutors.${i}.fullName`,
      t('internship.fields.fullName', { n }),
      errors.companyTutors?.[i]?.fullName?.message,
    );
    push(
      `companyTutors.${i}.role`,
      t('internship.fields.role', { n }),
      errors.companyTutors?.[i]?.role?.message,
    );
    push(
      `companyTutors.${i}.contact`,
      t('internship.fields.contact', { n }),
      errors.companyTutors?.[i]?.contact?.message,
    );
  });
  suspensionRows.fields.forEach((_, i) => {
    const n = i + 1;
    push(`suspensions.${i}.from`, t('internship.fields.from', { n }), errors.suspensions?.[i]?.from?.message);
    push(`suspensions.${i}.to`, t('internship.fields.to', { n }), errors.suspensions?.[i]?.to?.message);
  });
  (
    [
      'fiscalCode',
      'birthDate',
      'birthPlace',
      'residenceMunicipality',
      'residenceStreet',
      'residenceStreetNumber',
      'notes',
    ] as const
  ).forEach((f) => push(f, labels[f], errors[f]?.message));

  /** Dal: obbligatorio, non prima dell'inizio del tirocinio, senza sovrapporsi ai periodi precedenti. */
  const validateFrom = (i: number) => (from: string) => {
    if (from === '') return t('form.errors.required');
    const start = getValues('startDate');
    if (start && from < start) return t('internship.errors.beforeStart');
    const to = getValues(`suspensions.${i}.to`) || from;
    const rows = getValues('suspensions');
    for (let j = 0; j < i; j++) {
      if (rows[j].from && rows[j].to && from <= rows[j].to && rows[j].from <= to) {
        return t('internship.errors.overlap', { n: j + 1 });
      }
    }
    return true;
  };

  /** Al: obbligatorio, non prima del "dal" né dopo la fine prevista del tirocinio. */
  const validateTo = (i: number) => (to: string) => {
    if (to === '') return t('form.errors.required');
    const from = getValues(`suspensions.${i}.from`);
    if (from && to < from) return t('internship.errors.toBeforeFrom');
    const end = getValues('expectedEndDate');
    if (end && to > end) return t('internship.errors.afterEnd');
    return true;
  };

  const hoursText = (minutes: number): string =>
    t('internship.schedule.hoursValue', { count: minutes / 60, value: minutesToHoursText(minutes) });
  const slotValues = () => getValues(['firstSlotStart', 'firstSlotEnd', 'secondSlotStart', 'secondSlotEnd']);
  const after = (start: string, end: string): boolean => {
    const from = timeToMinutes(start);
    const to = timeToMinutes(end);
    return from === null || to === null || to > from;
  };
  const scheduleRules = {
    firstSlotEnd: (v: string) =>
      after(getValues('firstSlotStart'), v) || t('internship.schedule.errors.slotOrder'),
    secondSlotEnd: (v: string) =>
      after(getValues('secondSlotStart'), v) || t('internship.schedule.errors.slotOrder'),
    secondSlotStart: (v: string) =>
      !after(v, getValues('firstSlotEnd')) || t('internship.schedule.errors.slotsOverlap'),
    /** Ore del giorno: facoltative, a quarti d'ora, con una fascia scelta e non oltre le fasce scelte. */
    hours: (k: number) => (v: string) => {
      const minutes = parseHours(v);
      if (minutes === null) return true;
      const day = getValues(`days.${k}`);
      if (!day.first && !day.second) return t('internship.schedule.errors.hoursWithoutSlot');
      if (Number.isNaN(minutes)) return t('internship.schedule.errors.hoursFormat');
      const [firstSlotStart, firstSlotEnd, secondSlotStart, secondSlotEnd] = slotValues();
      const max = slotMinutes({ firstSlotStart, firstSlotEnd, secondSlotStart, secondSlotEnd }, day);
      return minutes <= max || t('internship.schedule.errors.hoursOverSlots', { hours: hoursText(max) });
    },
  };

  const onValid = async (v: Values): Promise<void> => {
    setGenericError(false);
    const weekly = weeklyMinutes(v, v.days);
    if (weekly !== null && weekly > MAX_WEEKLY_MINUTES) {
      setError('days', {
        type: 'max',
        message: t('internship.schedule.errors.weeklyMax', { hours: hoursText(weekly) }),
      });
      requestSummaryFocus();
      return;
    }
    const payload = toPayload(v, internship.version ?? null);
    try {
      await onSubmit(payload);
      onSuccess();
    } catch (error) {
      const body = apiError(error);
      if (body?.status === 409) {
        onConflict(body.message);
        return;
      }
      const fieldErrors = body?.fieldErrors ?? [];
      if (fieldErrors.length > 0) {
        fieldErrors.forEach((fe) =>
          setError(toFieldPath(fe.field, payload), { type: 'server', message: fe.message }),
        );
      } else {
        setGenericError(true);
      }
      requestSummaryFocus();
    }
  };

  const atecoError = errors.atecoCodeId?.message;
  const inclusionNoteId = `${fieldId('internshipTypeId')}-inclusion`;
  const atecoHintId = `${fieldId('atecoCodeId')}-hint`;
  const atecoErrorId = `${fieldId('atecoCodeId')}-error`;

  return (
    <form noValidate onSubmit={handleSubmit(onValid, requestSummaryFocus)} className="space-y-8">
      {submitCount > 0 && <ErrorSummary ref={summaryRef} errors={summary} />}
      {genericError && (
        <p
          role="alert"
          className="rounded-md border-2 border-destructive bg-card p-4 font-medium text-destructive"
        >
          {t('form.errors.generic')}
        </p>
      )}

      <section aria-labelledby="internship-section-data" className="space-y-4">
        <h2 id="internship-section-data" className="text-lg font-semibold text-primary">
          {t('internship.sections.internship')}
        </h2>
        <div className="grid gap-4 md:grid-cols-2">
          <FormField
            id={fieldId('internshipTypeId')}
            label={labels.internshipTypeId}
            error={errors.internshipTypeId?.message}
          >
            {(p) => (
              <NativeSelect
                {...p}
                // la spiegazione dell'inclusione non disponibile si legge anche con lo screen reader
                aria-describedby={
                  [p['aria-describedby'], inclusionType ? inclusionNoteId : null].filter(Boolean).join(' ') ||
                  undefined
                }
                {...register('internshipTypeId')}
              >
                <option value="">{t('form.select')}</option>
                {typeOptions.map((type) => {
                  const unavailable = isUnavailable(type);
                  return (
                    <option key={type.id} value={type.id} disabled={unavailable}>
                      {unavailable ? `${type.name} ${t('internship.inclusionUnavailableSuffix')}` : type.name}
                    </option>
                  );
                })}
              </NativeSelect>
            )}
          </FormField>
          {inclusionType && (
            <p id={inclusionNoteId} className="text-sm text-muted-foreground md:col-span-2">
              {t('internship.inclusionUnavailable', {
                type: inclusionType.name,
                items: inclusionMissing.map((m) => t(`internship.inclusionItems.${m}`)).join(', '),
              })}
            </p>
          )}
          <FormField id={fieldId('startDate')} label={labels.startDate} error={errors.startDate?.message}>
            {(p) => <Input {...p} type="date" className="max-w-48" {...register('startDate')} />}
          </FormField>
          <FormField
            id={fieldId('expectedEndDate')}
            label={labels.expectedEndDate}
            error={errors.expectedEndDate?.message}
          >
            {(p) => (
              <Input
                {...p}
                type="date"
                className="max-w-48"
                {...register('expectedEndDate', {
                  validate: (v) => {
                    const start = getValues('startDate');
                    return !v || !start || v >= start || t('internship.errors.endBeforeStart');
                  },
                })}
              />
            )}
          </FormField>
          <FormField
            id={fieldId('site')}
            label={labels.site}
            hint={t('internship.hints.site')}
            error={errors.site?.message}
          >
            {(p) => <Input {...p} autoComplete="off" {...register('site', { maxLength: max(300) })} />}
          </FormField>
          <FormField
            id={fieldId('siteMunicipality')}
            label={labels.siteMunicipality}
            hint={t('municipalityField.hint')}
            error={errors.siteMunicipality?.message}
          >
            {(p) => (
              <Controller
                control={control}
                name="siteMunicipality"
                rules={{ validate: chosen }}
                render={({ field }) => (
                  <MunicipalityField
                    {...p}
                    ref={field.ref}
                    value={field.value}
                    onChange={field.onChange}
                    onBlur={field.onBlur}
                  />
                )}
              />
            )}
          </FormField>
        </div>

        <fieldset
          className="space-y-2"
          aria-describedby={[atecoHintId, atecoError ? atecoErrorId : null].filter(Boolean).join(' ')}
        >
          <legend className="text-sm font-medium">{labels.atecoCodeId}</legend>
          <p id={atecoHintId} className="text-sm text-muted-foreground">
            {t('internship.hints.atecoCode')}
          </p>
          {internship.atecoCodes.length === 0 ? (
            <p>{t('internship.atecoNone')}</p>
          ) : (
            <div className="space-y-1">
              {[
                { id: '', code: t('internship.atecoNotChosen'), description: null },
                ...internship.atecoCodes,
              ].map((option, index) => {
                const id = index === 0 ? fieldId('atecoCodeId') : `${fieldId('atecoCodeId')}-${option.id}`;
                return (
                  <label key={option.id || 'none'} htmlFor={id} className="flex min-h-11 items-center gap-3">
                    <input
                      id={id}
                      type="radio"
                      value={option.id}
                      className="size-5 accent-primary"
                      {...register('atecoCodeId')}
                    />
                    <span>
                      <span className={option.id ? 'font-mono font-semibold' : undefined}>{option.code}</span>
                      {option.description && <> – {option.description}</>}
                    </span>
                  </label>
                );
              })}
            </div>
          )}
          {atecoError && (
            <p id={atecoErrorId} className="text-sm font-medium text-destructive">
              {atecoError}
            </p>
          )}
        </fieldset>
      </section>

      <ScheduleFields
        control={control}
        register={register}
        errors={errors}
        rules={scheduleRules}
        totalMinutes={internship.totalMinutes}
        fieldId={fieldId}
      />

      <section aria-labelledby="internship-section-tutors" className="space-y-4">
        {/* focalizzabile: l'elenco di ciò che manca per l'avvio porta qui */}
        <h2 id="internship-section-tutors" tabIndex={-1} className="text-lg font-semibold text-primary">
          {t('internship.sections.companyTutors')}
        </h2>
        {tutorRows.fields.length === 0 && (
          <p className="text-muted-foreground">{t('internship.noCompanyTutors')}</p>
        )}
        {tutorRows.fields.map((row, i) => (
          <div key={row.id} className="grid items-end gap-3 sm:grid-cols-2 lg:grid-cols-[1fr_1fr_1fr_auto]">
            <FormField
              id={fieldId(`companyTutors.${i}.fullName`)}
              label={t('internship.fields.fullName', { n: i + 1 })}
              required
              error={errors.companyTutors?.[i]?.fullName?.message}
            >
              {(p) => (
                <Input
                  {...p}
                  autoComplete="off"
                  {...register(`companyTutors.${i}.fullName` as const, {
                    validate: required,
                    maxLength: max(200),
                  })}
                />
              )}
            </FormField>
            <FormField
              id={fieldId(`companyTutors.${i}.role`)}
              label={t('internship.fields.role', { n: i + 1 })}
              hint={t('internship.hints.role')}
              error={errors.companyTutors?.[i]?.role?.message}
            >
              {(p) => (
                <Input
                  {...p}
                  autoComplete="off"
                  {...register(`companyTutors.${i}.role` as const, { maxLength: max(200) })}
                />
              )}
            </FormField>
            <FormField
              id={fieldId(`companyTutors.${i}.contact`)}
              label={t('internship.fields.contact', { n: i + 1 })}
              hint={t('internship.hints.contact')}
              error={errors.companyTutors?.[i]?.contact?.message}
            >
              {(p) => (
                <Input
                  {...p}
                  autoComplete="off"
                  {...register(`companyTutors.${i}.contact` as const, { maxLength: max(200) })}
                />
              )}
            </FormField>
            <Button
              type="button"
              variant="outline"
              size="icon-lg"
              aria-label={t('internship.removeCompanyTutor', { n: i + 1 })}
              onClick={() => tutorRows.remove(i)}
            >
              <Trash2 aria-hidden="true" />
            </Button>
          </div>
        ))}
        <Button
          type="button"
          variant="outline"
          disabled={tutorRows.fields.length >= 10}
          onClick={() => tutorRows.append({ fullName: '', role: '', contact: '' })}
        >
          <Plus aria-hidden="true" />
          {t('internship.addCompanyTutor')}
        </Button>
      </section>

      <section aria-labelledby="internship-section-suspensions" className="space-y-4">
        <h2 id="internship-section-suspensions" className="text-lg font-semibold text-primary">
          {t('internship.sections.suspensions')}
        </h2>
        <p className="text-muted-foreground">{t('internship.hints.suspensions')}</p>
        {suspensionRows.fields.length === 0 && (
          <p className="text-muted-foreground">{t('internship.noSuspensions')}</p>
        )}
        {suspensionRows.fields.map((row, i) => (
          <div key={row.id} className="grid items-end gap-3 sm:grid-cols-[12rem_12rem_auto]">
            <FormField
              id={fieldId(`suspensions.${i}.from`)}
              label={t('internship.fields.from', { n: i + 1 })}
              required
              error={errors.suspensions?.[i]?.from?.message}
            >
              {(p) => (
                <Input
                  {...p}
                  type="date"
                  {...register(`suspensions.${i}.from` as const, { validate: validateFrom(i) })}
                />
              )}
            </FormField>
            <FormField
              id={fieldId(`suspensions.${i}.to`)}
              label={t('internship.fields.to', { n: i + 1 })}
              required
              error={errors.suspensions?.[i]?.to?.message}
            >
              {(p) => (
                <Input
                  {...p}
                  type="date"
                  {...register(`suspensions.${i}.to` as const, { validate: validateTo(i) })}
                />
              )}
            </FormField>
            <Button
              type="button"
              variant="outline"
              size="icon-lg"
              aria-label={t('internship.removeSuspension', { n: i + 1 })}
              onClick={() => suspensionRows.remove(i)}
            >
              <Trash2 aria-hidden="true" />
            </Button>
          </div>
        ))}
        <Button
          type="button"
          variant="outline"
          disabled={suspensionRows.fields.length >= 20}
          onClick={() => suspensionRows.append({ from: '', to: '' })}
        >
          <Plus aria-hidden="true" />
          {t('internship.addSuspension')}
        </Button>
      </section>

      <section aria-labelledby="internship-section-beneficiary" className="space-y-4">
        <h2 id="internship-section-beneficiary" className="text-lg font-semibold text-primary">
          {t('internship.sections.beneficiary')}
        </h2>
        <p className="text-muted-foreground">{t('internship.beneficiaryIntro')}</p>
        <div className="grid gap-4 md:grid-cols-2">
          <FormField
            id={fieldId('fiscalCode')}
            label={labels.fiscalCode}
            hint={t('internship.hints.fiscalCode')}
            error={errors.fiscalCode?.message}
          >
            {(p) => (
              <Input
                {...p}
                autoComplete="off"
                className="max-w-64 uppercase"
                {...register('fiscalCode', {
                  pattern: { value: /^\s*[A-Za-z0-9]{16}\s*$/, message: t('internship.errors.fiscalCode') },
                })}
              />
            )}
          </FormField>
          <FormField id={fieldId('birthDate')} label={labels.birthDate} error={errors.birthDate?.message}>
            {(p) => (
              <Input
                {...p}
                type="date"
                max={today}
                className="max-w-48"
                {...register('birthDate', {
                  validate: (v) => !v || v < today || t('internship.errors.birthDate'),
                })}
              />
            )}
          </FormField>
          <FormField id={fieldId('birthPlace')} label={labels.birthPlace} error={errors.birthPlace?.message}>
            {(p) => <Input {...p} autoComplete="off" {...register('birthPlace', { maxLength: max(200) })} />}
          </FormField>
          <FormField
            id={fieldId('residenceMunicipality')}
            label={labels.residenceMunicipality}
            hint={t('municipalityField.hint')}
            error={errors.residenceMunicipality?.message}
          >
            {(p) => (
              <Controller
                control={control}
                name="residenceMunicipality"
                rules={{ validate: chosen }}
                render={({ field }) => (
                  <MunicipalityField
                    {...p}
                    ref={field.ref}
                    value={field.value}
                    onChange={field.onChange}
                    onBlur={field.onBlur}
                  />
                )}
              />
            )}
          </FormField>
          <FormField
            id={fieldId('residenceStreet')}
            label={labels.residenceStreet}
            error={errors.residenceStreet?.message}
          >
            {(p) => (
              <Input {...p} autoComplete="off" {...register('residenceStreet', { maxLength: max(300) })} />
            )}
          </FormField>
          <FormField
            id={fieldId('residenceStreetNumber')}
            label={labels.residenceStreetNumber}
            hint={t('internship.hints.residenceStreetNumber')}
            error={errors.residenceStreetNumber?.message}
          >
            {(p) => (
              <Input
                {...p}
                autoComplete="off"
                className="max-w-32"
                {...register('residenceStreetNumber', { maxLength: max(20) })}
              />
            )}
          </FormField>
        </div>
      </section>

      <FormField id={fieldId('notes')} label={labels.notes} error={errors.notes?.message}>
        {(p) => <Textarea {...p} rows={3} {...register('notes', { maxLength: max(2000) })} />}
      </FormField>

      <Button type="submit" size="lg" disabled={isSubmitting}>
        {isSubmitting ? t('internship.saving') : t('internship.save')}
      </Button>
    </form>
  );
};
