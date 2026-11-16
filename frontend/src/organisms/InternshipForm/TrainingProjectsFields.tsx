import {
  useWatch,
  type Control,
  type FieldErrors,
  type UseFieldArrayReturn,
  type UseFormGetValues,
  type UseFormRegister,
} from 'react-hook-form';
import { useTranslation } from 'react-i18next';

import { Input } from '@/components/ui/input';
import type { TrainingProject } from '@/config/api/internshipApi';
import { formatIsoDate } from '@/lib/isoDate';
import { FormField } from '@/molecules/FormField/FormField';
import type { InternshipFormValues } from '@/organisms/InternshipForm/InternshipForm';
import { ProjectRows, type ProjectLabels, type ProjectsPath } from '@/organisms/InternshipForm/ProjectRows';

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
 * tirocinio, e con le proroghe (US-1012) si dice anche quando finisce l'ultima. I progetti sono numerati nell'ordine
 * delle date: ognuno sta dentro il tirocinio e inizia dopo la fine del precedente.
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
  const [startDate, expectedEndDate, extensions] = useWatch({
    control,
    name: ['startDate', 'expectedEndDate', 'extensions'],
  });
  const max = (n: number) => ({ value: n, message: t('form.errors.maxLength', { max: n }) });
  const currentEnd = extensions.at(-1)?.to;
  const labels: ProjectLabels = {
    heading: (n) => t('internship.trainingProjects.heading', { n }),
    from: (n) => t('internship.fields.projectFrom', { n }),
    to: (n) => t('internship.fields.projectTo', { n }),
    jobTitle: (n) => t('internship.fields.jobTitle', { n }),
    jobDetails: (n) => t('internship.fields.jobDetails', { n }),
    remove: (n) => t('internship.trainingProjects.remove', { n }),
    add: t('internship.trainingProjects.add'),
    none: t('internship.trainingProjects.none'),
    beforeStart: (date) => t('internship.errors.projectBeforeStart', { date }),
    afterEnd: (date) => t('internship.errors.projectAfterEnd', { date }),
  };

  return (
    <>
      <section aria-labelledby="internship-section-agreement" className="space-y-4">
        {/* focalizzabili: l'elenco di ciò che manca per l'avvio porta qui */}
        <h2 id="internship-section-agreement" tabIndex={-1} className="text-lg font-semibold text-primary">
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
          {currentEnd && ` ${t('internship.extensions.currentEnd', { date: formatIsoDate(currentEnd) })}`}
        </p>
      </section>

      <section aria-labelledby="internship-section-trainingProjects" className="space-y-4">
        <h2
          id="internship-section-trainingProjects"
          tabIndex={-1}
          className="text-lg font-semibold text-primary"
        >
          {t('internship.sections.trainingProjects')}
        </h2>
        <p className="text-muted-foreground">{t('internship.trainingProjects.intro')}</p>
        <ProjectRows
          name="trainingProjects"
          control={control}
          register={register}
          getValues={getValues}
          rows={rows as UseFieldArrayReturn<InternshipFormValues, ProjectsPath>}
          errors={errors.trainingProjects}
          saved={saved}
          bounds={() => ({ start: getValues('startDate'), end: getValues('expectedEndDate') })}
          labels={labels}
          headingLevel="h3"
          proposedTitle={() => jobSlotTitle ?? ''}
          fieldId={fieldId}
        />
      </section>
    </>
  );
};
