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

/** Elenco dei progetti di una convenzione: quella iniziale (US-1008) o una proroga (US-1012). */
export type ProjectsPath = 'trainingProjects' | `extensions.${number}.trainingProjects`;

/** Testi che cambiano tra la convenzione iniziale e le proroghe; `n` è il numero del progetto. */
export type ProjectLabels = {
  heading: (n: number) => string;
  from: (n: number) => string;
  to: (n: number) => string;
  jobTitle: (n: number) => string;
  jobDetails: (n: number) => string;
  remove: (n: number) => string;
  add: string;
  none: string;
  beforeStart: (date: string) => string;
  afterEnd: (date: string) => string;
};

type ProjectRowsProps = {
  name: ProjectsPath;
  control: Control<InternshipFormValues>;
  register: UseFormRegister<InternshipFormValues>;
  getValues: UseFormGetValues<InternshipFormValues>;
  rows: UseFieldArrayReturn<InternshipFormValues, ProjectsPath>;
  errors: FieldErrors<InternshipFormValues>['trainingProjects'];
  /** Progetti salvati della stessa convenzione, con mesi e monte ore calcolati dal server. */
  saved: TrainingProject[];
  /** Date della convenzione che contiene i progetti, lette al momento del controllo. */
  bounds: () => { start: string; end: string };
  labels: ProjectLabels;
  headingLevel: 'h3' | 'h4';
  /** Mansione proposta per il primo progetto, letta quando lo si aggiunge. */
  proposedTitle: () => string;
  fieldId: (path: string) => string;
};

/**
 * Righe dei progetti formativi di una convenzione: numerati nell'ordine delle date, ognuno dentro la convenzione e dopo
 * la fine del precedente, con gli stessi controlli del server. Durata in mesi e monte ore arrivano dal server per i
 * progetti salvati.
 */
export const ProjectRows = ({
  name,
  control,
  register,
  getValues,
  rows,
  errors,
  saved,
  bounds,
  labels,
  headingLevel: Heading,
  proposedTitle,
  fieldId,
}: ProjectRowsProps) => {
  const { t } = useTranslation();
  const projects = useWatch({ control, name });
  const max = (n: number) => ({ value: n, message: t('form.errors.maxLength', { max: n }) });
  const required = (v: string) => v.trim() !== '' || t('form.errors.required');

  /** Dal: obbligatorio, non prima della convenzione, dopo la fine del progetto precedente. */
  const validateFrom = (i: number) => (from: string) => {
    if (from === '') return t('form.errors.required');
    const { start } = bounds();
    if (start && from < start) return labels.beforeStart(formatIsoDate(start));
    const previous = i > 0 ? getValues(`${name}.${i - 1}.to`) : '';
    if (previous && from <= previous) {
      return t('internship.errors.projectAfterPrevious', { n: i, date: formatIsoDate(previous) });
    }
    return true;
  };

  /** Al: obbligatorio, non prima del "dal" né dopo la fine della convenzione. */
  const validateTo = (i: number) => (to: string) => {
    if (to === '') return t('form.errors.required');
    const from = getValues(`${name}.${i}.from`);
    if (from && to < from) return t('internship.errors.toBeforeFrom');
    const { end } = bounds();
    if (end && to > end) return labels.afterEnd(formatIsoDate(end));
    return true;
  };

  /** Nuovo progetto: dal giorno dopo il precedente alla fine della convenzione, con la stessa mansione (proposte). */
  const add = (): void => {
    const last = getValues(name).at(-1);
    const { start, end } = bounds();
    rows.append({
      from: last?.to ? dayAfter(last.to) : start,
      to: end,
      jobTitle: last?.jobTitle || proposedTitle(),
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
      {rows.fields.length === 0 && <p className="text-muted-foreground">{labels.none}</p>}
      {rows.fields.map((row, i) => {
        const n = i + 1;
        const headingId = `${fieldId(`${name}.${i}`)}-heading`;
        const rowErrors = errors?.[i];
        return (
          <div
            key={row.id}
            role="group"
            aria-labelledby={headingId}
            className="space-y-3 rounded-md border border-border p-4"
          >
            <Heading id={headingId} className="font-semibold">
              {labels.heading(n)}
            </Heading>
            <div className="grid gap-3 sm:grid-cols-[12rem_12rem]">
              <FormField
                id={fieldId(`${name}.${i}.from`)}
                label={labels.from(n)}
                required
                error={rowErrors?.from?.message}
              >
                {(p) => (
                  <Input
                    {...p}
                    type="date"
                    {...register(`${name}.${i}.from` as const, { validate: validateFrom(i) })}
                  />
                )}
              </FormField>
              <FormField
                id={fieldId(`${name}.${i}.to`)}
                label={labels.to(n)}
                required
                error={rowErrors?.to?.message}
              >
                {(p) => (
                  <Input
                    {...p}
                    type="date"
                    {...register(`${name}.${i}.to` as const, { validate: validateTo(i) })}
                  />
                )}
              </FormField>
            </div>
            <FormField
              id={fieldId(`${name}.${i}.jobTitle`)}
              label={labels.jobTitle(n)}
              required
              error={rowErrors?.jobTitle?.message}
            >
              {(p) => (
                <Input
                  {...p}
                  autoComplete="off"
                  {...register(`${name}.${i}.jobTitle` as const, { validate: required, maxLength: max(200) })}
                />
              )}
            </FormField>
            <FormField
              id={fieldId(`${name}.${i}.jobDetails`)}
              label={labels.jobDetails(n)}
              hint={t('internship.hints.jobDetails')}
              error={rowErrors?.jobDetails?.message}
            >
              {(p) => (
                <Textarea
                  {...p}
                  rows={3}
                  {...register(`${name}.${i}.jobDetails` as const, { maxLength: max(2000) })}
                />
              )}
            </FormField>
            <p className="text-sm text-muted-foreground">{computed(i)}</p>
            <Button
              type="button"
              variant="outline"
              className="h-auto min-h-9 py-2 whitespace-normal"
              onClick={() => rows.remove(i)}
            >
              <Trash2 aria-hidden="true" />
              {labels.remove(n)}
            </Button>
          </div>
        );
      })}
      <Button
        type="button"
        variant="outline"
        className="h-auto min-h-9 py-2 whitespace-normal"
        disabled={rows.fields.length >= MAX_PROJECTS}
        onClick={add}
      >
        <Plus aria-hidden="true" />
        {labels.add}
      </Button>
    </>
  );
};
