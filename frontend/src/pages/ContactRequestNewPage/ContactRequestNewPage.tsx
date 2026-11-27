import { useEffect, useRef, useState } from 'react';
import { useForm, useWatch } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import { Link, useNavigate } from 'react-router';

import { TrainingShipBadge } from '@/atoms/TrainingShipBadge/TrainingShipBadge';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { NativeSelect } from '@/components/ui/native-select';
import { fetchSimilarCompanies, type CompanyDirectoryItem } from '@/config/api/companyApi';
import type { ContactRequestForm } from '@/config/api/contactRequestApi';
import { apiError } from '@/config/api/errors';
import { useCreateContactRequest } from '@/config/hooks/useContactRequests';
import { useBusinessCategories, useZones } from '@/config/hooks/useReferenceData';
import { useErrorSummaryFocus } from '@/hooks/useErrorSummaryFocus';
import { BackLink } from '@/molecules/BackLink/BackLink';
import { ErrorSummary, type SummaryError } from '@/molecules/ErrorSummary/ErrorSummary';
import { FormField } from '@/molecules/FormField/FormField';
import { PageHeader } from '@/molecules/PageHeader/PageHeader';
import type { ContactRequestSentState } from '@/pages/MyContactRequestsPage/MyContactRequestsPage';

type Values = { companyName: string; zoneId: string; businessCategoryId: string };

const FIELDS = ['companyName', 'zoneId', 'businessCategoryId'] as const satisfies readonly (keyof Values)[];
const fieldId = (name: keyof Values): string => `contact-request-${name}`;
const MAX_NAME = 200;

const toPayload = (v: Values): ContactRequestForm => ({
  companyName: v.companyName.trim(),
  zoneId: v.zoneId,
  businessCategoryId: v.businessCategoryId || null,
});

/**
 * Richiesta di contatto per un'azienda non ancora in anagrafica (US-507, PO 27/11): nome e zona obbligatori, categoria
 * facoltativa. Prima dell'invio mostra le aziende attive con un nome simile: se è una di quelle è già in NEXUS e la
 * richiesta non serve (il Tutor ne apre la scheda); altrimenti conferma che l'azienda è nuova. Dopo l'invio torna a «Le
 * mie richieste», con il numero della richiesta.
 */
export const ContactRequestNewPage = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const zones = useZones();
  const categories = useBusinessCategories();
  const create = useCreateContactRequest();
  const {
    register,
    handleSubmit,
    setError,
    setFocus,
    control,
    formState: { errors, submitCount },
  } = useForm<Values>({
    defaultValues: { companyName: '', zoneId: '', businessCategoryId: '' },
    mode: 'onSubmit',
    reValidateMode: 'onChange',
    shouldFocusError: false,
  });
  const { summaryRef, requestSummaryFocus } = useErrorSummaryFocus(submitCount, errors);
  const [similar, setSimilar] = useState<{ values: Values; companies: CompanyDirectoryItem[] } | null>(null);
  const [checking, setChecking] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const similarRef = useRef<HTMLHeadingElement>(null);
  const companyName = useWatch({ control, name: 'companyName' });

  useEffect(() => {
    if (similar) similarRef.current?.focus();
  }, [similar]);

  // cambiato il nome, i nomi simili mostrati non valgono più: si torna al pulsante d'invio
  const shown = similar && companyName.trim() === similar.values.companyName.trim() ? similar : null;

  const labels: Record<keyof Values, string> = {
    companyName: t('contactRequest.fields.companyName'),
    zoneId: t('contactRequest.fields.zoneId'),
    businessCategoryId: t('contactRequest.fields.businessCategoryId'),
  };
  const summary: SummaryError[] = FIELDS.flatMap((field) => {
    const message = errors[field]?.message;
    return message ? [{ fieldId: fieldId(field), message: `${labels[field]}: ${message}` }] : [];
  });

  const send = async (form: ContactRequestForm): Promise<void> => {
    setFormError(null);
    try {
      const saved = await create.mutateAsync(form);
      navigate('/richieste', { state: { sent: saved.number } satisfies ContactRequestSentState });
    } catch (error) {
      const fieldErrors = (apiError(error)?.fieldErrors ?? []).filter(
        (fe): fe is { field: keyof Values; message: string } =>
          (FIELDS as readonly string[]).includes(fe.field),
      );
      if (fieldErrors.length > 0) {
        setSimilar(null);
        fieldErrors.forEach((fe) => setError(fe.field, { type: 'server', message: fe.message }));
        requestSummaryFocus();
      } else {
        setFormError(t('contactRequest.askError'));
      }
    }
  };

  const onValid = async (values: Values): Promise<void> => {
    setFormError(null);
    setChecking(true);
    let found: CompanyDirectoryItem[];
    try {
      found = await fetchSimilarCompanies(values.companyName.trim());
    } catch {
      setChecking(false);
      setFormError(t('contactRequest.askError'));
      return;
    }
    setChecking(false);
    if (found.length > 0) {
      setSimilar({ values, companies: found });
      return;
    }
    await send(toPayload(values));
  };

  const busy = checking || create.isPending;

  return (
    <div className="space-y-6">
      <PageHeader back={<BackLink to="/richieste">{t('pages.myContactRequests.heading')}</BackLink>}>
        {t('pages.contactRequestNew.heading')}
      </PageHeader>
      <p className="text-muted-foreground">{t('pages.contactRequestNew.intro')}</p>

      <form noValidate onSubmit={handleSubmit(onValid, requestSummaryFocus)} className="max-w-xl space-y-6">
        {submitCount > 0 && <ErrorSummary ref={summaryRef} errors={summary} />}
        <FormField
          id={fieldId('companyName')}
          label={labels.companyName}
          required
          hint={t('contactRequest.hints.companyName')}
          error={errors.companyName?.message}
        >
          {(p) => (
            <Input
              {...p}
              autoComplete="off"
              {...register('companyName', {
                validate: (v) => v.trim() !== '' || t('contactRequest.errors.companyName'),
                maxLength: { value: MAX_NAME, message: t('form.errors.maxLength', { max: MAX_NAME }) },
              })}
            />
          )}
        </FormField>
        <FormField
          id={fieldId('zoneId')}
          label={labels.zoneId}
          required
          hint={t('contactRequest.hints.zoneId')}
          error={errors.zoneId?.message}
        >
          {(p) => (
            <NativeSelect
              {...p}
              disabled={!zones.data}
              {...register('zoneId', { required: t('contactRequest.errors.zoneId') })}
            >
              <option value="">{t('contactRequest.chooseZone')}</option>
              {(zones.data ?? []).map((z) => (
                <option key={z.id} value={z.id}>
                  {z.name}
                </option>
              ))}
            </NativeSelect>
          )}
        </FormField>
        <FormField
          id={fieldId('businessCategoryId')}
          label={labels.businessCategoryId}
          hint={t('contactRequest.hints.businessCategoryId')}
          error={errors.businessCategoryId?.message}
        >
          {(p) => (
            <NativeSelect {...p} disabled={!categories.data} {...register('businessCategoryId')}>
              <option value="">{t('contactRequest.noCategory')}</option>
              {(categories.data ?? []).map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </NativeSelect>
          )}
        </FormField>

        {formError && (
          <p
            role="alert"
            className="rounded-md border-2 border-destructive bg-card p-4 font-medium text-destructive"
          >
            {formError}
          </p>
        )}

        {!shown && (
          <Button type="submit" size="lg" disabled={busy} aria-busy={busy}>
            {checking
              ? t('contactRequest.checking')
              : create.isPending
                ? t('contactRequest.asking')
                : t('contactRequest.submit')}
          </Button>
        )}
      </form>

      {shown && (
        <section
          aria-labelledby="contact-request-similar"
          className="max-w-3xl space-y-4 rounded-xl border-2 border-primary bg-card p-4"
        >
          <h2
            id="contact-request-similar"
            ref={similarRef}
            tabIndex={-1}
            className="text-lg font-semibold text-primary"
          >
            {t('contactRequest.similar.heading')}
          </h2>
          <p>
            {t('contactRequest.similar.intro', {
              count: shown.companies.length,
              name: shown.values.companyName.trim(),
            })}
          </p>
          <ul className="divide-y divide-border">
            {shown.companies.map((c) => (
              <li key={c.id} className="flex flex-wrap items-center justify-between gap-3 py-3">
                <div>
                  <Link
                    to={`/aziende/${c.id}`}
                    className="font-semibold text-primary underline underline-offset-4"
                  >
                    {c.name}
                  </Link>
                  {c.trainingShip && (
                    <>
                      {' '}
                      <TrainingShipBadge />
                    </>
                  )}
                  {(c.zone?.name || c.municipality) && (
                    <p className="text-sm text-muted-foreground">
                      {[c.zone?.name, c.municipality].filter(Boolean).join(' · ')}
                    </p>
                  )}
                </div>
              </li>
            ))}
          </ul>
          <div className="flex flex-wrap gap-3">
            <Button size="lg" disabled={create.isPending} onClick={() => void send(toPayload(shown.values))}>
              {create.isPending ? t('contactRequest.asking') : t('contactRequest.similar.confirmNew')}
            </Button>
            <Button
              size="lg"
              variant="outline"
              disabled={create.isPending}
              onClick={() => {
                setSimilar(null);
                setFocus('companyName');
              }}
            >
              {t('contactRequest.similar.back')}
            </Button>
          </div>
        </section>
      )}
    </div>
  );
};
