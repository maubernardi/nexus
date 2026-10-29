import { useEffect, useRef, useState } from 'react';
import { Plus } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import type { AtecoCode } from '@/config/api/companyLegalDataApi';
import { apiError } from '@/config/api/errors';
import { useAddAtecoCode, useAtecoCodes, useRemoveAtecoCode } from '@/config/hooks/useCompanyLegalData';
import { useErrorSummaryFocus } from '@/hooks/useErrorSummaryFocus';
import { ErrorSummary, type SummaryError } from '@/molecules/ErrorSummary/ErrorSummary';
import { FormField } from '@/molecules/FormField/FormField';

type Values = { code: string; description: string };

/** Codici ATECO dell'azienda (US-506): uno o più; nel tirocinio si sceglie quello giusto (US-1001). */
export const AtecoCodesSection = ({ companyId }: { companyId: string }) => {
  const { t } = useTranslation();
  const codes = useAtecoCodes(companyId);
  const remove = useRemoveAtecoCode();
  const [adding, setAdding] = useState(false);
  const [outcome, setOutcome] = useState<{ text: string; error?: boolean } | null>(null);
  const formHeadingRef = useRef<HTMLHeadingElement>(null);
  const outcomeRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (adding) formHeadingRef.current?.focus();
  }, [adding]);
  useEffect(() => {
    if (outcome) outcomeRef.current?.focus();
  }, [outcome]);

  const onRemove = (item: AtecoCode): void => {
    setOutcome(null);
    remove.mutate(
      { companyId, id: item.id },
      {
        onSuccess: () => setOutcome({ text: t('ateco.removed', { code: item.code }) }),
        onError: () => setOutcome({ text: t('form.errors.generic'), error: true }),
      },
    );
  };

  return (
    <section aria-labelledby="ateco-heading" className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h2 id="ateco-heading" className="text-lg font-semibold text-primary">
          {t('ateco.heading')}
        </h2>
        {!adding && (
          <Button
            size="lg"
            onClick={() => {
              setOutcome(null);
              setAdding(true);
            }}
          >
            <Plus aria-hidden="true" />
            {t('ateco.add')}
          </Button>
        )}
      </div>
      <p className="text-muted-foreground">{t('ateco.intro')}</p>

      {outcome && (
        <div
          ref={outcomeRef}
          tabIndex={-1}
          className={
            outcome.error
              ? 'rounded-md border-2 border-destructive bg-card p-4 font-medium text-destructive'
              : 'rounded-md border-l-4 border-primary bg-card p-4 font-medium'
          }
        >
          {outcome.text}
        </div>
      )}

      {adding && (
        <div className="space-y-4 rounded-xl border border-border bg-card p-4">
          <h3 ref={formHeadingRef} tabIndex={-1} className="text-base font-semibold">
            {t('ateco.addHeading')}
          </h3>
          <AtecoCodeForm
            companyId={companyId}
            onAdded={(item) => {
              setAdding(false);
              setOutcome({ text: t('ateco.added', { code: item.code }) });
            }}
            onCancel={() => setAdding(false)}
          />
        </div>
      )}

      {codes.isError && !codes.data ? (
        <p role="alert" className="font-medium text-destructive">
          {t('ateco.loadError')}
        </p>
      ) : !codes.data ? (
        <p>{t('common.loading')}</p>
      ) : codes.data.length === 0 ? (
        <p className="rounded-xl border border-border bg-card p-4">{t('ateco.empty')}</p>
      ) : (
        <ul
          aria-label={t('ateco.listLabel')}
          className="divide-y divide-border rounded-xl border border-border bg-card"
        >
          {codes.data.map((item) => (
            <li key={item.id} className="flex flex-wrap items-center justify-between gap-3 p-3">
              <span>
                <span className="font-mono font-semibold">{item.code}</span>
                {item.description && <> – {item.description}</>}
              </span>
              <Button size="sm" variant="outline" onClick={() => onRemove(item)} disabled={remove.isPending}>
                {t('ateco.remove')}
                {/* testo nascosto dopo uno spazio a sé: nome accessibile "Togli il codice 56.10.11" */}{' '}
                <span className="sr-only">{t('ateco.removeTarget', { code: item.code })}</span>
              </Button>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
};

const AtecoCodeForm = ({
  companyId,
  onAdded,
  onCancel,
}: {
  companyId: string;
  onAdded: (item: AtecoCode) => void;
  onCancel: () => void;
}) => {
  const { t } = useTranslation();
  const add = useAddAtecoCode();
  const [genericError, setGenericError] = useState(false);
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting, submitCount },
  } = useForm<Values>({
    defaultValues: { code: '', description: '' },
    mode: 'onSubmit',
    reValidateMode: 'onChange',
    shouldFocusError: false,
  });
  const { summaryRef, requestSummaryFocus } = useErrorSummaryFocus(submitCount, errors);
  const labels = { code: t('ateco.fields.code'), description: t('ateco.fields.description') };
  const summary: SummaryError[] = (['code', 'description'] as const).flatMap((field) => {
    const message = errors[field]?.message;
    return message ? [{ fieldId: `ateco-${field}`, message: `${labels[field]}: ${message}` }] : [];
  });

  const onValid = async (v: Values): Promise<void> => {
    setGenericError(false);
    try {
      onAdded(
        await add.mutateAsync({ companyId, code: v.code.trim(), description: v.description.trim() || null }),
      );
    } catch (error) {
      const message = apiError(error)?.fieldErrors?.find((fe) => fe.field === 'code')?.message;
      if (message) {
        setError('code', { type: 'server', message });
      } else {
        setGenericError(true);
      }
      requestSummaryFocus();
    }
  };

  return (
    <form noValidate onSubmit={handleSubmit(onValid, requestSummaryFocus)} className="space-y-4">
      {submitCount > 0 && <ErrorSummary ref={summaryRef} errors={summary} />}
      {genericError && (
        <p
          role="alert"
          className="rounded-md border-2 border-destructive bg-card p-4 font-medium text-destructive"
        >
          {t('form.errors.generic')}
        </p>
      )}
      <div className="grid gap-4 md:grid-cols-[12rem_1fr]">
        <FormField
          id="ateco-code"
          label={labels.code}
          hint={t('ateco.hints.code')}
          required
          error={errors.code?.message}
        >
          {(p) => (
            <Input
              {...p}
              inputMode="decimal"
              autoComplete="off"
              {...register('code', {
                validate: (v) => v.trim() !== '' || t('form.errors.required'),
                pattern: { value: /^\s*[0-9]{2}(\.[0-9]{1,2}){0,2}\s*$/, message: t('ateco.errors.code') },
              })}
            />
          )}
        </FormField>
        <FormField id="ateco-description" label={labels.description} error={errors.description?.message}>
          {(p) => (
            <Input
              {...p}
              autoComplete="off"
              {...register('description', {
                maxLength: { value: 200, message: t('form.errors.maxLength', { max: 200 }) },
              })}
            />
          )}
        </FormField>
      </div>
      <div className="flex flex-wrap gap-3">
        <Button type="submit" size="lg" disabled={isSubmitting}>
          {t('ateco.save')}
        </Button>
        <Button type="button" size="lg" variant="outline" onClick={onCancel}>
          {t('ateco.cancel')}
        </Button>
      </div>
    </form>
  );
};
