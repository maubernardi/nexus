import { useEffect, useId, useRef, useState, type FormEvent } from 'react';
import { useTranslation } from 'react-i18next';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { apiError } from '@/config/api/errors';
import { ISTAT_FILE_MAX_BYTES, type MunicipalityImportResult } from '@/config/api/municipalityApi';
import {
  useImportMunicipalities,
  useMunicipalitySearch,
  useMunicipalityStatus,
  usePreviewMunicipalityImport,
} from '@/config/hooks/useMunicipalities';
import { useFocusAfterRender } from '@/hooks/useFocusAfterRender';
import { dayBefore, formatIsoDate, todayIso } from '@/lib/isoDate';

const ISTAT_PAGE = 'https://www.istat.it/classificazione/codici-dei-comuni-delle-province-e-delle-regioni/';

type Feedback = { kind: 'status' | 'alert'; text: string };

const number = new Intl.NumberFormat('it-IT');
const dateTime = new Intl.DateTimeFormat('it-IT', { dateStyle: 'short', timeStyle: 'short' });

/**
 * Comuni e province dell'ISTAT per l'ADMIN (US-114): stato dell'elenco, caricamento del file con l'anteprima delle
 * differenze e la conferma, ricerca anche tra le versioni passate.
 */
export const MunicipalitiesSection = () => {
  const { t } = useTranslation();
  const headingId = useId();
  const status = useMunicipalityStatus();

  return (
    <section aria-labelledby={headingId} className="space-y-4">
      <h2 id={headingId} className="text-lg font-semibold text-primary">
        {t('municipalities.heading')}
      </h2>
      <p className="text-muted-foreground">{t('municipalities.intro')}</p>
      {status.isError && !status.data ? (
        <p
          role="alert"
          className="rounded-md border-2 border-destructive bg-card p-4 font-medium text-destructive"
        >
          {t('municipalities.status.error')}
        </p>
      ) : !status.data ? (
        <p>{t('common.loading')}</p>
      ) : status.data.municipalities === 0 ? (
        <p className="font-medium">{t('municipalities.status.empty')}</p>
      ) : (
        <div className="space-y-1">
          <p className="font-medium">
            {t('municipalities.status.counts', {
              municipalities: number.format(status.data.municipalities),
              provinces: number.format(status.data.provinces),
            })}
          </p>
          {status.data.lastImport && (
            <p className="text-sm text-muted-foreground">
              {t('municipalities.status.lastImport', {
                validFrom: formatIsoDate(status.data.lastImport.validFrom),
                importedAt: dateTime.format(new Date(status.data.lastImport.importedAt)),
                by: status.data.lastImport.importedBy ?? '—',
              })}
            </p>
          )}
        </div>
      )}
      <ImportForm />
      <MunicipalitySearch />
    </section>
  );
};

const ImportForm = () => {
  const { t } = useTranslation();
  const fileId = useId();
  const dateId = useId();
  const hintId = useId();
  const dateHintId = useId();
  const headingId = useId();
  const previewHeadingId = useId();
  const preview = usePreviewMunicipalityImport();
  const confirm = useImportMunicipalities();
  const [file, setFile] = useState<File | null>(null);
  const [validFrom, setValidFrom] = useState(todayIso);
  const [result, setResult] = useState<MunicipalityImportResult | null>(null);
  const [feedback, setFeedback] = useState<Feedback | null>(null);
  const { ref: feedbackRef, requestFocus: focusFeedback } = useFocusAfterRender<HTMLParagraphElement>();
  const { ref: previewRef, requestFocus: focusPreview } = useFocusAfterRender<HTMLHeadingElement>();
  const inputRef = useRef<HTMLInputElement>(null);

  const show = (next: Feedback): void => {
    setFeedback(next);
    focusFeedback();
  };

  const failure = (e: unknown): string => {
    const body = apiError(e);
    return body?.status === 400 && body.message ? body.message : t('municipalities.errors.generic');
  };

  // un'anteprima vale solo per il file e la data da cui è nata
  const reset = (): void => {
    setResult(null);
    setFeedback(null);
  };

  const onCheck = async (event: FormEvent<HTMLFormElement>): Promise<void> => {
    event.preventDefault();
    reset();
    if (!file) return show({ kind: 'alert', text: t('municipalities.errors.file') });
    if (!file.name.toLowerCase().endsWith('.csv'))
      return show({ kind: 'alert', text: t('municipalities.errors.type') });
    if (file.size > ISTAT_FILE_MAX_BYTES)
      return show({ kind: 'alert', text: t('municipalities.errors.size') });
    if (!validFrom) return show({ kind: 'alert', text: t('municipalities.errors.date') });
    try {
      setResult(await preview.mutateAsync(file));
      focusPreview();
    } catch (e) {
      show({ kind: 'alert', text: failure(e) });
    }
  };

  const onConfirm = async (): Promise<void> => {
    if (!file) return;
    try {
      const done = await confirm.mutateAsync({ file, validFrom });
      setResult(null);
      setFile(null);
      if (inputRef.current) inputRef.current.value = '';
      show({
        kind: 'status',
        text: t('municipalities.done', {
          validFrom: formatIsoDate(validFrom),
          added: number.format(done.municipalities.added),
          removed: number.format(done.municipalities.removed),
          changed: number.format(done.municipalities.changed),
        }),
      });
    } catch (e) {
      show({ kind: 'alert', text: failure(e) });
    }
  };

  const nothingChanged =
    result !== null &&
    [result.municipalities, result.provinces].every((c) => c.added + c.removed + c.changed === 0);

  return (
    <div className="space-y-3 rounded-xl border border-border bg-card p-4">
      <h3 id={headingId} className="text-base font-semibold">
        {t('municipalities.upload.heading')}
      </h3>
      <p id={hintId} className="text-sm">
        {t('municipalities.upload.hint')}{' '}
        <a href={ISTAT_PAGE} target="_blank" rel="noreferrer" className="font-medium text-primary underline">
          {t('municipalities.upload.link')}{' '}
          <span className="sr-only">{t('municipalities.upload.newTab')}</span>
        </a>
      </p>
      {feedback && (
        <p
          ref={feedbackRef}
          role={feedback.kind}
          tabIndex={-1}
          className={
            feedback.kind === 'alert'
              ? 'rounded-md border-2 border-destructive p-3 font-medium text-destructive'
              : 'rounded-md border-l-4 border-primary bg-background p-3 font-medium'
          }
        >
          {feedback.text}
        </p>
      )}
      <form noValidate aria-labelledby={headingId} onSubmit={(e) => void onCheck(e)} className="space-y-3">
        <div className="space-y-1">
          <label htmlFor={fileId} className="block text-sm font-medium">
            {t('municipalities.upload.file')}
          </label>
          <input
            ref={inputRef}
            id={fileId}
            type="file"
            accept=".csv,text/csv"
            aria-describedby={hintId}
            onChange={(e) => {
              reset();
              setFile(e.target.files?.[0] ?? null);
            }}
            className="min-h-11 max-w-full text-sm file:mr-3 file:min-h-11 file:rounded-md file:border file:border-border file:bg-background file:px-3 file:font-medium"
          />
        </div>
        <div className="space-y-1">
          <label htmlFor={dateId} className="block text-sm font-medium">
            {t('municipalities.upload.validFrom')}
          </label>
          <p id={dateHintId} className="text-sm text-muted-foreground">
            {t('municipalities.upload.validFromHint')}
          </p>
          <Input
            id={dateId}
            type="date"
            value={validFrom}
            max={todayIso()}
            aria-describedby={dateHintId}
            onChange={(e) => {
              reset();
              setValidFrom(e.target.value);
            }}
            className="w-auto"
          />
        </div>
        <Button type="submit" disabled={preview.isPending || confirm.isPending}>
          {preview.isPending ? t('municipalities.upload.checking') : t('municipalities.upload.check')}
        </Button>
      </form>

      {result && (
        <div
          aria-labelledby={previewHeadingId}
          role="region"
          className="space-y-3 border-t border-border pt-3"
        >
          <h4 ref={previewRef} id={previewHeadingId} tabIndex={-1} className="font-semibold">
            {t('municipalities.preview.heading')}
          </h4>
          <p>{t('municipalities.preview.rows', { count: number.format(result.rows) })}</p>
          {nothingChanged ? (
            <p className="font-medium">{t('municipalities.preview.none')}</p>
          ) : (
            <>
              <ul className="list-disc space-y-1 pl-6">
                {(['municipalities', 'provinces'] as const).map((kind) => (
                  <li key={kind}>
                    {t(`municipalities.preview.${kind}`, {
                      added: number.format(result[kind].added),
                      removed: number.format(result[kind].removed),
                      changed: number.format(result[kind].changed),
                    })}
                  </li>
                ))}
              </ul>
              <ChangeGroup
                label="addedMunicipalities"
                total={result.municipalities.added}
                items={result.municipalities.addedExamples}
              />
              <ChangeGroup
                label="removedMunicipalities"
                total={result.municipalities.removed}
                items={result.municipalities.removedExamples}
              />
              <ChangeGroup
                label="changedMunicipalities"
                total={result.municipalities.changed}
                items={result.municipalities.changedExamples}
              />
              <ChangeGroup
                label="addedProvinces"
                total={result.provinces.added}
                items={result.provinces.addedExamples}
              />
              <ChangeGroup
                label="removedProvinces"
                total={result.provinces.removed}
                items={result.provinces.removedExamples}
              />
              <ChangeGroup
                label="changedProvinces"
                total={result.provinces.changed}
                items={result.provinces.changedExamples}
              />
            </>
          )}
          <div className="flex flex-wrap gap-3">
            {!nothingChanged && (
              <Button type="button" disabled={confirm.isPending} onClick={() => void onConfirm()}>
                {confirm.isPending
                  ? t('municipalities.preview.confirming')
                  : t('municipalities.preview.confirm')}
              </Button>
            )}
            <Button type="button" variant="outline" disabled={confirm.isPending} onClick={reset}>
              {t('municipalities.preview.cancel')}
            </Button>
          </div>
        </div>
      )}
    </div>
  );
};

type GroupLabel =
  | 'addedMunicipalities'
  | 'removedMunicipalities'
  | 'changedMunicipalities'
  | 'addedProvinces'
  | 'removedProvinces'
  | 'changedProvinces';

const ChangeGroup = ({ label, total, items }: { label: GroupLabel; total: number; items: string[] }) => {
  const { t } = useTranslation();
  if (total === 0) return null;
  const more = total - items.length;
  return (
    <details className="rounded-md border border-border bg-background p-3">
      <summary className="cursor-pointer font-medium">
        {t(`municipalities.preview.groups.${label}`, { count: total })}
      </summary>
      <ul className="mt-2 list-disc space-y-1 pl-6 text-sm">
        {items.map((item) => (
          <li key={item}>{item}</li>
        ))}
        {more > 0 && <li>{t('municipalities.preview.more', { count: more })}</li>}
      </ul>
    </details>
  );
};

const MunicipalitySearch = () => {
  const { t } = useTranslation();
  const inputId = useId();
  const hintId = useId();
  const headingId = useId();
  const [text, setText] = useState('');
  const [query, setQuery] = useState('');
  const results = useMunicipalitySearch(query, true);

  // si cerca quando si smette di scrivere
  useEffect(() => {
    const timer = setTimeout(() => setQuery(text.trim()), 300);
    return () => clearTimeout(timer);
  }, [text]);

  const active = query.length >= 2;

  return (
    <div className="space-y-3 rounded-xl border border-border bg-card p-4">
      <h3 id={headingId} className="text-base font-semibold">
        {t('municipalities.search.heading')}
      </h3>
      <div className="space-y-1">
        <label htmlFor={inputId} className="block text-sm font-medium">
          {t('municipalities.search.label')}
        </label>
        <p id={hintId} className="text-sm text-muted-foreground">
          {t('municipalities.search.hint')}
        </p>
        <Input
          id={inputId}
          type="search"
          value={text}
          autoComplete="off"
          aria-describedby={hintId}
          onChange={(e) => setText(e.target.value)}
          className="max-w-md"
        />
      </div>
      <p role="status" className="text-sm font-medium">
        {active && results.data && !results.isError
          ? results.data.length === 0
            ? t('municipalities.search.none')
            : t('municipalities.search.results', { count: results.data.length })
          : ''}
      </p>
      {active && results.isError && (
        <p role="alert" className="font-medium text-destructive">
          {t('municipalities.search.error')}
        </p>
      )}
      {active && results.data && results.data.length > 0 && (
        <ul aria-labelledby={headingId} className="divide-y divide-border">
          {results.data.map((m) => (
            <li key={m.id} className="py-2">
              <span className="font-semibold">
                {m.name} ({m.provinceAbbreviation})
              </span>{' '}
              <span className="text-sm text-muted-foreground">
                — {m.provinceName}, {t('municipalities.search.istat', { code: m.istatCode })},{' '}
                {m.validTo
                  ? t('municipalities.search.between', {
                      from: formatIsoDate(m.validFrom),
                      to: formatIsoDate(dayBefore(m.validTo)),
                    })
                  : t('municipalities.search.since', { from: formatIsoDate(m.validFrom) })}
              </span>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};
