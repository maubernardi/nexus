import { Plus, Trash2 } from 'lucide-react';
import {
  useWatch,
  type Control,
  type FieldErrors,
  type UseFieldArrayReturn,
  type UseFormGetValues,
  type UseFormRegister,
} from 'react-hook-form';
import { useTranslation } from 'react-i18next';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import type { TrainingProject } from '@/config/api/internshipApi';
import { dayAfter, formatIsoDate } from '@/lib/isoDate';
import { FormField } from '@/molecules/FormField/FormField';
import type { InternshipFormValues } from '@/organisms/InternshipForm/InternshipForm';
import { minutesToHoursText } from '@/organisms/InternshipForm/schedule';

/** Massimo dei progetti formativi di una convenzione, come nel server. */
const MAX_PROJECTS = 10;

type TrainingProjectsFieldsProps = {
  control: Control<InternshipFormValues>;
  register: UseFormRegister<InternshipFormValues>;
  errors: FieldErrors<InternshipFormValues>;
  getValues: UseFormGetValues<InternshipFormValues>;
  rows: UseFieldArrayReturn<InternshipFormValues, 'trainingProjects'>;
  /** Progetti salvati, con mesi e monte ore calcolati dal server. */
  saved: TrainingProject[];
  /** Mansione della segnalazione, proposta per il primo progetto. */
  jobSlotTitle?: string | null;
  fieldId: (path: string) => string;
};

/**
 * Convenzione e progetti formativi della scheda (US-1008). La convenzione ha numero e date; la durata è quella del
 * tirocinio. I progetti sono numerati nell'ordine delle date: ognuno sta dentro il tirocinio e inizia dopo la fine del
 * precedente, con gli stessi controlli del server. Durata in mesi e monte ore arrivano dal server per i progetti salvati.
 */
export const TrainingProjectsFields = ({
  control,
  register,
  errors,
  getValues,
  rows,
  saved,
  jobSlotTitle,
  fieldId,
}: TrainingProjectsFieldsProps) => {
  const { t } = useTranslation();
  const [startDate, expectedEndDate, projects] = useWatch({
    control,
    name: ['startDate', 'expectedEndDate', 'trainingProjects'],
  });
  const max = (n: number) => ({ value: n, message: t('form.errors.maxLength', { max: n }) });
  const required = (v: string) => v.trim() !== '' || t('form.errors.required');

  /** Dal: obbligatorio, non prima del tirocinio, dopo la fine del progetto precedente. */
  const validateFrom = (i: number) => (from: string) => {
    if (from === '') return t('form.errors.required');
    const start = getValues('startDate');
    if (start && from < start) {
      return t('internship.errors.projectBeforeStart', { date: formatIsoDate(start) });
    }
    const previous = i > 0 ? getValues(`trainingProjects.${i - 1}.to`) : '';
    if (previous && from <= previous) {
      return t('internship.errors.projectAfterPrevious', { n: i, date: formatIsoDate(previous) });
    }
    return true;
  };

  /** Al: obbligatorio, non prima del "dal" né dopo la fine prevista del tirocinio. */
  const validateTo = (i: number) => (to: string) => {
    if (to === '') return t('form.errors.required');
    const from = getValues(`trainingProjects.${i}.from`);
    if (from && to < from) return t('internship.errors.toBeforeFrom');
    const end = getValues('expectedEndDate');
    if (end && to > end) return t('internship.errors.projectAfterEnd', { date: formatIsoDate(end) });
    return true;
  };

  /** Nuovo progetto: dal giorno dopo il precedente alla fine del tirocinio, con la stessa mansione (proposte). */
  const add = (): void => {
    const last = getValues('trainingProjects').at(-1);
    rows.append({
      from: last?.to ? dayAfter(last.to) : getValues('startDate'),
      to: getValues('expectedEndDate'),
      jobTitle: last?.jobTitle || jobSlotTitle || '',
      jobDetails: '',
    });
  };

  /** Durata e monte ore del progetto salvato, se le sue date non sono cambiate nel modulo. */
  const computed = (i: number): string => {
    const stored = saved[i];
    const current = projects[i];
    if (!stored || !current || stored.from !== current.from || stored.to !== current.to) {
      return t('internship.trainingProjects.pending');
    }
    const months = t('internship.trainingProjects.months', { count: stored.months });
    if (typeof stored.totalMinutes !== 'number') {
      return t('internship.trainingProjects.summaryNoHours', { months });
    }
    const hours = t('internship.schedule.hoursValue', {
      count: stored.totalMinutes / 60,
      value: minutesToHoursText(stored.totalMinutes),
    });
    return t('internship.trainingProjects.summary', { months, hours });
  };

  return (
    <>
      <section aria-labelledby="internship-section-agreement" className="space-y-4">
        <h2 id="internship-section-agreement" className="text-lg font-semibold text-primary">
          {t('internship.sections.agreement')}
        </h2>
        <div className="grid gap-4 sm:grid-cols-3">
          <FormField
            id={fieldId('agreementNumber')}
            label={t('internship.fields.agreementNumber')}
            hint={t('internship.hints.agreementNumber')}
            error={errors.agreementNumber?.message}
          >
            {(p) => (
              <Input {...p} autoComplete="off" {...register('agreementNumber', { maxLength: max(50) })} />
            )}
          </FormField>
          <FormField
            id={fieldId('agreementDate')}
            label={t('internship.fields.agreementDate')}
            error={errors.agreementDate?.message}
          >
            {(p) => <Input {...p} type="date" className="max-w-48" {...register('agreementDate')} />}
          </FormField>
          <FormField
            id={fieldId('agreementSignedDate')}
            label={t('internship.fields.agreementSignedDate')}
            error={errors.agreementSignedDate?.message}
          >
            {(p) => <Input {...p} type="date" className="max-w-48" {...register('agreementSignedDate')} />}
          </FormField>
        </div>
        <p className="text-muted-foreground">
          {startDate && expectedEndDate
            ? t('internship.agreement.duration', {
                from: formatIsoDate(startDate),
                to: formatIsoDate(expectedEndDate),
              })
            : t('internship.agreement.durationMissing')}
        </p>
      </section>

      <section aria-labelledby="internship-section-trainingProjects" className="space-y-4">
        <h2 id="internship-section-trainingProjects" className="text-lg font-semibold text-primary">
          {t('internship.sections.trainingProjects')}
        </h2>
        <p className="text-muted-foreground">{t('internship.trainingProjects.intro')}</p>
        {rows.fields.length === 0 && (
          <p className="text-muted-foreground">{t('internship.trainingProjects.none')}</p>
        )}
        {rows.fields.map((row, i) => {
          const n = i + 1;
          const headingId = `${fieldId(`trainingProjects.${i}`)}-heading`;
          const rowErrors = errors.trainingProjects?.[i];
          return (
            <div
              key={row.id}
              role="group"
              aria-labelledby={headingId}
              className="space-y-3 rounded-md border border-border p-4"
            >
              <h3 id={headingId} className="font-semibold">
                {t('internship.trainingProjects.heading', { n })}
              </h3>
              <div className="grid gap-3 sm:grid-cols-[12rem_12rem]">
                <FormField
                  id={fieldId(`trainingProjects.${i}.from`)}
                  label={t('internship.fields.projectFrom', { n })}
                  required
                  error={rowErrors?.from?.message}
                >
                  {(p) => (
                    <Input
                      {...p}
                      type="date"
                      {...register(`trainingProjects.${i}.from` as const, { validate: validateFrom(i) })}
                    />
                  )}
                </FormField>
                <FormField
                  id={fieldId(`trainingProjects.${i}.to`)}
                  label={t('internship.fields.projectTo', { n })}
                  required
                  error={rowErrors?.to?.message}
                >
                  {(p) => (
                    <Input
                      {...p}
                      type="date"
                      {...register(`trainingProjects.${i}.to` as const, { validate: validateTo(i) })}
                    />
                  )}
                </FormField>
              </div>
              <FormField
                id={fieldId(`trainingProjects.${i}.jobTitle`)}
                label={t('internship.fields.jobTitle', { n })}
                required
                error={rowErrors?.jobTitle?.message}
              >
                {(p) => (
                  <Input
                    {...p}
                    autoComplete="off"
                    {...register(`trainingProjects.${i}.jobTitle` as const, {
                      validate: required,
                      maxLength: max(200),
                    })}
                  />
                )}
              </FormField>
              <FormField
                id={fieldId(`trainingProjects.${i}.jobDetails`)}
                label={t('internship.fields.jobDetails', { n })}
                hint={t('internship.hints.jobDetails')}
                error={rowErrors?.jobDetails?.message}
              >
                {(p) => (
                  <Textarea
                    {...p}
                    rows={3}
                    {...register(`trainingProjects.${i}.jobDetails` as const, { maxLength: max(2000) })}
                  />
                )}
              </FormField>
              <p className="text-sm text-muted-foreground">{computed(i)}</p>
              <Button type="button" variant="outline" onClick={() => rows.remove(i)}>
                <Trash2 aria-hidden="true" />
                {t('internship.trainingProjects.remove', { n })}
              </Button>
            </div>
          );
        })}
        <Button type="button" variant="outline" disabled={rows.fields.length >= MAX_PROJECTS} onClick={add}>
          <Plus aria-hidden="true" />
          {t('internship.trainingProjects.add')}
        </Button>
      </section>
    </>
  );
};
