import { Plus, Trash2 } from 'lucide-react';
import {
  useFieldArray,
  type Control,
  type FieldErrors,
  type UseFieldArrayReturn,
  type UseFormGetValues,
  type UseFormRegister,
} from 'react-hook-form';
import { useTranslation } from 'react-i18next';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import type { Extension } from '@/config/api/internshipApi';
import { dayAfter, formatIsoDate } from '@/lib/isoDate';
import { FormField } from '@/molecules/FormField/FormField';
import type { InternshipFormValues } from '@/organisms/InternshipForm/InternshipForm';
import { ProjectRows, type ProjectLabels, type ProjectsPath } from '@/organisms/InternshipForm/ProjectRows';

/** Massimo delle proroghe, come nel server. */
const MAX_EXTENSIONS = 10;

type Common = {
  control: Control<InternshipFormValues>;
  register: UseFormRegister<InternshipFormValues>;
  errors: FieldErrors<InternshipFormValues>;
  getValues: UseFormGetValues<InternshipFormValues>;
  /** Mansione della segnalazione, proposta per il primo progetto. */
  jobSlotTitle?: string | null;
  fieldId: (path: string) => string;
};

type ExtensionsFieldsProps = Common & {
  /** Proroghe salvate, con mesi e monte ore dei progetti calcolati dal server. */
  saved: Extension[];
  /** Tirocinio avviato e non ancora chiuso: solo allora le proroghe cambiano. */
  extensible: boolean;
};

/**
 * Proroghe del tirocinio (US-1012). Ogni proroga è una convenzione con numero, date e progetti formativi propri; inizia
 * dopo la fine della convenzione precedente, non per forza il giorno dopo. La sezione c'è a tirocinio avviato oppure se
 * ci sono già proroghe, che dopo la chiusura restano in sola lettura.
 */
export const ExtensionsFields = ({ saved, extensible, ...common }: ExtensionsFieldsProps) => {
  const { t } = useTranslation();
  const { control, register, errors, getValues, fieldId } = common;
  const rows = useFieldArray({ control, name: 'extensions' });
  if (!extensible && saved.length === 0) return null;
  const headingId = fieldId('extensions');
  const errorId = `${headingId}-error`;
  const max = (n: number) => ({ value: n, message: t('form.errors.maxLength', { max: n }) });
  const required = (v: string) => v.trim() !== '' || t('form.errors.required');

  /** Dal: dopo la fine della convenzione precedente, cioè quella iniziale per la prima proroga. */
  const validateFrom = (k: number) => (from: string) => {
    if (from === '') return t('form.errors.required');
    const previous = k === 0 ? getValues('expectedEndDate') : getValues(`extensions.${k - 1}.to`);
    // proroga già salvata e fine prevista spostata: l'errore è sulla fine prevista, come nel server
    if (k === 0 && from === saved[0]?.from) return true;
    if (previous && from <= previous) {
      const date = formatIsoDate(previous);
      return k === 0
        ? t('internship.errors.extensionAfterAgreement', { date })
        : t('internship.errors.extensionAfterPrevious', { n: k, date });
    }
    return true;
  };

  const validateTo = (k: number) => (to: string) => {
    if (to === '') return t('form.errors.required');
    const from = getValues(`extensions.${k}.from`);
    return !from || to >= from || t('internship.errors.toBeforeFrom');
  };

  /** Nuova proroga: dal giorno dopo la fine attuale del tirocinio (proposta), il resto da compilare. */
  const add = (): void => {
    const end = getValues('extensions').at(-1)?.to || getValues('expectedEndDate');
    rows.append({
      agreementNumber: '',
      agreementDate: '',
      agreementSignedDate: '',
      from: end ? dayAfter(end) : '',
      to: '',
      trainingProjects: [],
    });
  };

  return (
    <section aria-labelledby={headingId} className="space-y-4">
      {/* focalizzabile: il riepilogo degli errori porta qui */}
      <h2 id={headingId} tabIndex={-1} className="text-lg font-semibold text-primary">
        {t('internship.sections.extensions')}
      </h2>
      <p className="text-muted-foreground">
        {extensible ? t('internship.extensions.intro') : t('internship.extensions.closed')}
      </p>
      {errors.extensions?.message && (
        <p id={errorId} className="text-sm font-medium text-destructive">
          {errors.extensions.message}
        </p>
      )}
      {!extensible && <SavedExtensions saved={saved} />}
      {extensible && rows.fields.length === 0 && (
        <p className="text-muted-foreground">{t('internship.extensions.none')}</p>
      )}
      {extensible &&
        rows.fields.map((row, k) => {
          const n = k + 1;
          const groupHeadingId = `${fieldId(`extensions.${k}`)}-heading`;
          const extErrors = errors.extensions?.[k];
          return (
            <div
              key={row.id}
              role="group"
              aria-labelledby={groupHeadingId}
              className="space-y-4 rounded-md border border-border p-4"
            >
              <h3 id={groupHeadingId} className="font-semibold">
                {t('internship.extensions.heading', { k: n })}
              </h3>
              <div className="grid gap-4 sm:grid-cols-3">
                <FormField
                  id={fieldId(`extensions.${k}.agreementNumber`)}
                  label={t('internship.fields.extensionAgreementNumber', { k: n })}
                  required
                  error={extErrors?.agreementNumber?.message}
                >
                  {(p) => (
                    <Input
                      {...p}
                      autoComplete="off"
                      {...register(`extensions.${k}.agreementNumber` as const, {
                        validate: required,
                        maxLength: max(50),
                      })}
                    />
                  )}
                </FormField>
                <FormField
                  id={fieldId(`extensions.${k}.agreementDate`)}
                  label={t('internship.fields.extensionAgreementDate', { k: n })}
                  required
                  error={extErrors?.agreementDate?.message}
                >
                  {(p) => (
                    <Input
                      {...p}
                      type="date"
                      className="max-w-48"
                      {...register(`extensions.${k}.agreementDate` as const, { validate: required })}
                    />
                  )}
                </FormField>
                <FormField
                  id={fieldId(`extensions.${k}.agreementSignedDate`)}
                  label={t('internship.fields.extensionAgreementSignedDate', { k: n })}
                  required
                  error={extErrors?.agreementSignedDate?.message}
                >
                  {(p) => (
                    <Input
                      {...p}
                      type="date"
                      className="max-w-48"
                      {...register(`extensions.${k}.agreementSignedDate` as const, { validate: required })}
                    />
                  )}
                </FormField>
              </div>
              <div className="grid gap-3 sm:grid-cols-[12rem_12rem]">
                <FormField
                  id={fieldId(`extensions.${k}.from`)}
                  label={t('internship.fields.extensionFrom', { k: n })}
                  required
                  error={extErrors?.from?.message}
                >
                  {(p) => (
                    <Input
                      {...p}
                      type="date"
                      {...register(`extensions.${k}.from` as const, { validate: validateFrom(k) })}
                    />
                  )}
                </FormField>
                <FormField
                  id={fieldId(`extensions.${k}.to`)}
                  label={t('internship.fields.extensionTo', { k: n })}
                  required
                  error={extErrors?.to?.message}
                >
                  {(p) => (
                    <Input
                      {...p}
                      type="date"
                      {...register(`extensions.${k}.to` as const, { validate: validateTo(k) })}
                    />
                  )}
                </FormField>
              </div>
              <ExtensionProjects k={k} saved={saved[k]?.trainingProjects ?? []} {...common} />
              <Button
                type="button"
                variant="outline"
                className="h-auto min-h-9 py-2 whitespace-normal"
                onClick={() => rows.remove(k)}
              >
                <Trash2 aria-hidden="true" />
                {t('internship.extensions.remove', { k: n })}
              </Button>
            </div>
          );
        })}
      {extensible && (
        <Button type="button" variant="outline" disabled={rows.fields.length >= MAX_EXTENSIONS} onClick={add}>
          <Plus aria-hidden="true" />
          {t('internship.extensions.add')}
        </Button>
      )}
    </section>
  );
};

type ExtensionProjectsProps = Common & { k: number; saved: Extension['trainingProjects'] };

/** Progetti formativi di una proroga: le stesse righe della convenzione iniziale, dentro le date della proroga. */
const ExtensionProjects = ({
  k,
  saved,
  control,
  register,
  errors,
  getValues,
  jobSlotTitle,
  fieldId,
}: ExtensionProjectsProps) => {
  const { t } = useTranslation();
  const name = `extensions.${k}.trainingProjects` as const;
  const rows = useFieldArray({ control, name });
  const n = k + 1;
  const labels: ProjectLabels = {
    heading: (m) => t('internship.extensions.projectHeading', { k: n, n: m }),
    from: (m) => t('internship.fields.extensionProjectFrom', { k: n, n: m }),
    to: (m) => t('internship.fields.extensionProjectTo', { k: n, n: m }),
    jobTitle: (m) => t('internship.fields.extensionJobTitle', { k: n, n: m }),
    jobDetails: (m) => t('internship.fields.extensionJobDetails', { k: n, n: m }),
    remove: (m) => t('internship.extensions.projectRemove', { k: n, n: m }),
    add: t('internship.extensions.projectAdd', { k: n }),
    none: t('internship.extensions.projectsNone', { k: n }),
    beforeStart: (date) => t('internship.errors.extensionProjectBeforeStart', { date }),
    afterEnd: (date) => t('internship.errors.extensionProjectAfterEnd', { date }),
  };
  return (
    <ProjectRows
      name={name}
      control={control}
      register={register}
      getValues={getValues}
      rows={rows as UseFieldArrayReturn<InternshipFormValues, ProjectsPath>}
      errors={errors.extensions?.[k]?.trainingProjects}
      saved={saved}
      bounds={() => ({ start: getValues(`extensions.${k}.from`), end: getValues(`extensions.${k}.to`) })}
      labels={labels}
      headingLevel="h4"
      // la mansione dell'ultimo progetto della convenzione precedente, altrimenti quella della segnalazione
      proposedTitle={() =>
        (k === 0 ? getValues('trainingProjects') : getValues(`extensions.${k - 1}.trainingProjects`)).at(-1)
          ?.jobTitle ||
        jobSlotTitle ||
        ''
      }
      fieldId={fieldId}
    />
  );
};

/** Proroghe dopo la chiusura del tirocinio: in sola lettura. */
const SavedExtensions = ({ saved }: { saved: Extension[] }) => {
  const { t } = useTranslation();
  return (
    <ol className="space-y-4">
      {saved.map((e) => (
        <li key={e.number} className="space-y-2 rounded-md border border-border p-4">
          <h3 className="font-semibold">{t('internship.extensions.heading', { k: e.number })}</h3>
          <p>
            {t('internship.extensions.summary', {
              number: e.agreementNumber,
              date: formatIsoDate(e.agreementDate),
              signed: formatIsoDate(e.agreementSignedDate),
              from: formatIsoDate(e.from),
              to: formatIsoDate(e.to),
            })}
          </p>
          {e.trainingProjects.length > 0 && (
            <ul className="list-disc space-y-1 pl-6">
              {e.trainingProjects.map((p) => (
                <li key={p.number}>
                  {t('internship.extensions.projectSummary', {
                    n: p.number,
                    from: formatIsoDate(p.from),
                    to: formatIsoDate(p.to),
                    jobTitle: p.jobTitle,
                  })}
                </li>
              ))}
            </ul>
          )}
        </li>
      ))}
    </ol>
  );
};
