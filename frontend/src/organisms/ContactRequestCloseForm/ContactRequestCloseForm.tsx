import { useEffect, useRef, useState } from 'react';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { NativeSelect } from '@/components/ui/native-select';
import { Textarea } from '@/components/ui/textarea';
import { searchCompanies, type Company } from '@/config/api/companyApi';
import {
  CONTACT_OUTCOMES,
  type ContactOutcome,
  type ContactRequest,
  type ContactRequestCloseForm as ClosePayload,
} from '@/config/api/contactRequestApi';
import { apiError } from '@/config/api/errors';
import { useCloseContactRequest } from '@/config/hooks/useContactRequests';
import { useErrorSummaryFocus } from '@/hooks/useErrorSummaryFocus';
import { ErrorSummary, type SummaryError } from '@/molecules/ErrorSummary/ErrorSummary';
import { FormField } from '@/molecules/FormField/FormField';

type Values = { outcome: ContactOutcome | ''; note: string; companyId: string };

const FIELDS = ['outcome', 'note', 'companyId'] as const satisfies readonly (keyof Values)[];
const fieldId = (name: keyof Values): string => `contact-close-${name}`;
const MAX_NOTE = 500;
/** Aziende mostrate dalla ricerca: le prime per nome. */
const SEARCH_SIZE = 10;

type ContactRequestCloseFormProps = {
  request: ContactRequest;
  /** Dopo la chiusura: la pagina mostra l'esito, perché il modulo sparisce (la richiesta non è più in carico). */
  onClosed: () => void;
};

/**
 * Chiusura della richiesta di contatto (US-512), per l'operatore che la ha in carico: esito finale e nota per il Tutor,
 * obbligatori; l'azienda dell'anagrafica, cercata per nome e facoltativa. Prima della chiusura una conferma, con il
 * focus: la richiesta non si riapre.
 */
export const ContactRequestCloseForm = ({ request, onClosed }: ContactRequestCloseFormProps) => {
  const { t } = useTranslation();
  const close = useCloseContactRequest();
  const [pending, setPending] = useState<Values | null>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const [search, setSearch] = useState(request.companyName);
  const [results, setResults] = useState<Company[] | null>(null);
  const [searching, setSearching] = useState(false);
  const [searchFailed, setSearchFailed] = useState(false);
  const confirmRef = useRef<HTMLHeadingElement>(null);
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, submitCount },
  } = useForm<Values>({
    defaultValues: { outcome: '', note: '', companyId: '' },
    mode: 'onSubmit',
    reValidateMode: 'onChange',
    shouldFocusError: false,
  });
  const { summaryRef, requestSummaryFocus } = useErrorSummaryFocus(submitCount, errors);

  useEffect(() => {
    if (pending) confirmRef.current?.focus();
  }, [pending]);

  const labels: Record<keyof Values, string> = {
    outcome: t('contactRequest.closeForm.outcome'),
    note: t('contactRequest.closeForm.note'),
    companyId: t('contactRequest.closeForm.company'),
  };
  const summary: SummaryError[] = FIELDS.flatMap((field) => {
    const message = errors[field]?.message;
    return message ? [{ fieldId: fieldId(field), message: `${labels[field]}: ${message}` }] : [];
  });

  const onSearch = async (): Promise<void> => {
    setSearching(true);
    setSearchFailed(false);
    try {
      const page = await searchCompanies({ search: search.trim() || undefined, page: 0, size: SEARCH_SIZE });
      setResults(page.companies);
    } catch {
      setSearchFailed(true);
    } finally {
      setSearching(false);
    }
  };

  const onConfirm = (): void => {
    if (!pending) return;
    const form: ClosePayload = {
      version: request.version,
      outcome: pending.outcome as ContactOutcome,
      note: pending.note.trim(),
      companyId: pending.companyId || null,
    };
    close.mutate(
      { id: request.id, form },
      {
        onSuccess: () => {
          setPending(null);
          onClosed();
        },
        onError: (error) => {
          setPending(null);
          const body = apiError(error);
          const fieldErrors = (body?.fieldErrors ?? []).filter(
            (fe): fe is { field: keyof Values; message: string } =>
              (FIELDS as readonly string[]).includes(fe.field),
          );
          if (fieldErrors.length > 0) {
            fieldErrors.forEach((fe) => setError(fe.field, { type: 'server', message: fe.message }));
            requestSummaryFocus();
          } else {
            setNotice(
              body?.status === 409 && body.message ? body.message : t('contactRequest.closeForm.error'),
            );
          }
        },
      },
    );
  };

  return (
    <section
      aria-labelledby="contact-close-heading"
      className="space-y-4 rounded-xl border border-border bg-card p-4"
    >
      <h2 id="contact-close-heading" className="text-lg font-semibold text-primary">
        {t('contactRequest.closeForm.heading')}
      </h2>
      <p className="text-sm text-muted-foreground">{t('contactRequest.closeForm.intro')}</p>
      <form
        noValidate
        onSubmit={handleSubmit((v) => {
          setNotice(null);
          setPending(v);
        }, requestSummaryFocus)}
        className="max-w-xl space-y-4"
      >
        {submitCount > 0 && <ErrorSummary ref={summaryRef} errors={summary} />}
        <FormField id={fieldId('outcome')} label={labels.outcome} required error={errors.outcome?.message}>
          {(p) => (
            <NativeSelect
              {...p}
              {...register('outcome', { required: t('contactRequest.closeForm.errors.outcome') })}
            >
              <option value="">{t('contactRequest.attemptForm.chooseOutcome')}</option>
              {CONTACT_OUTCOMES.map((o) => (
                <option key={o} value={o}>
                  {t(`contactRequest.outcome.${o}`)}
                </option>
              ))}
            </NativeSelect>
          )}
        </FormField>
        <FormField
          id={fieldId('note')}
          label={labels.note}
          required
          hint={t('contactRequest.closeForm.noteHint')}
          error={errors.note?.message}
        >
          {(p) => (
            <Textarea
              {...p}
              rows={3}
              {...register('note', {
                validate: (v) => v.trim() !== '' || t('contactRequest.closeForm.errors.note'),
                maxLength: { value: MAX_NOTE, message: t('form.errors.maxLength', { max: MAX_NOTE }) },
              })}
            />
          )}
        </FormField>

        <fieldset className="space-y-3" aria-describedby="contact-close-company-hint">
          <legend className="text-sm font-medium">{labels.companyId}</legend>
          <p id="contact-close-company-hint" className="text-sm text-muted-foreground">
            {t('contactRequest.closeForm.companyHint')}
          </p>
          <div className="flex flex-wrap gap-2">
            <Input
              type="search"
              aria-label={t('company.search')}
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              onKeyDown={(e) => {
                // Invio cerca l'azienda, non chiude la richiesta
                if (e.key === 'Enter') {
                  e.preventDefault();
                  void onSearch();
                }
              }}
              className="max-w-sm flex-1"
            />
            <Button type="button" variant="outline" disabled={searching} onClick={() => void onSearch()}>
              {searching ? t('contactRequest.closeForm.searching') : t('contactRequest.closeForm.search')}
            </Button>
          </div>
          {searchFailed && (
            <p role="alert" className="text-sm font-medium text-destructive">
              {t('contactRequest.closeForm.searchError')}
            </p>
          )}
          <div id={fieldId('companyId')} className="space-y-1">
            <label className="flex min-h-11 items-center gap-2">
              <input type="radio" value="" className="size-5 accent-primary" {...register('companyId')} />
              {t('contactRequest.closeForm.noCompany')}
            </label>
            {results?.map((c) => (
              <label key={c.id} className="flex min-h-11 items-center gap-2">
                <input
                  type="radio"
                  value={c.id}
                  className="size-5 accent-primary"
                  {...register('companyId')}
                />
                {c.name}
              </label>
            ))}
            {results?.length === 0 && (
              <p role="status" className="text-sm text-muted-foreground">
                {t('contactRequest.closeForm.noResults')}
              </p>
            )}
          </div>
        </fieldset>

        {notice && (
          <p
            role="alert"
            className="rounded-md border-2 border-destructive bg-card p-4 font-medium text-destructive"
          >
            {notice}
          </p>
        )}

        {!pending && (
          <Button type="submit" size="lg">
            {t('contactRequest.closeForm.submit')}
          </Button>
        )}
      </form>

      {pending && (
        <div className="space-y-3 rounded-xl border-2 border-primary p-4">
          <h3 ref={confirmRef} tabIndex={-1} className="font-semibold">
            {t('contactRequest.closeForm.confirmHeading')}
          </h3>
          <p>
            {t('contactRequest.closeForm.confirmText', {
              number: request.number,
              outcome: t(`contactRequest.outcome.${pending.outcome as ContactOutcome}`),
            })}
          </p>
          <div className="flex flex-wrap gap-3">
            <Button size="lg" disabled={close.isPending} aria-busy={close.isPending} onClick={onConfirm}>
              {close.isPending
                ? t('contactRequest.closeForm.closing')
                : t('contactRequest.closeForm.confirm')}
            </Button>
            <Button size="lg" variant="outline" disabled={close.isPending} onClick={() => setPending(null)}>
              {t('contactRequest.closeForm.back')}
            </Button>
          </div>
        </div>
      )}
    </section>
  );
};
