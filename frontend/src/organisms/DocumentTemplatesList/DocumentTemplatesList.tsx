import { useEffect, useState, type FormEvent } from 'react';
import { CircleAlert, FileDown, FileUp, Power } from 'lucide-react';
import { useTranslation } from 'react-i18next';

import { Button } from '@/components/ui/button';
import type { DocumentTemplate } from '@/config/api/documentTemplateApi';
import { apiError } from '@/config/api/errors';
import {
  useDocumentTemplates,
  useDownloadDocumentTemplateFile,
  useReplaceDocumentTemplateFile,
  useSetDocumentTemplateActive,
} from '@/config/hooks/useDocumentTemplates';
import { saveBlob } from '@/lib/download';
import { cn } from '@/lib/utils';

const dateTime = new Intl.DateTimeFormat('it-IT', { dateStyle: 'short', timeStyle: 'short' });

/** Dimensione leggibile: «48 KB», «1,2 MB». */
const size = (bytes: number): string =>
  bytes < 1024 * 1024
    ? `${Math.max(1, Math.round(bytes / 1024))} KB`
    : `${(bytes / (1024 * 1024)).toFixed(1).replace('.', ',')} MB`;

type Status = { text: string; error?: boolean };

/**
 * Modelli caricati (US-1010), attivi e disattivati: per ognuno documento, condizioni, file e le azioni. L'esito delle
 * azioni si annuncia nel messaggio di stato in cima all'elenco; la sostituzione del file si apre nella scheda del
 * modello, con il focus sul campo del file.
 */
export const DocumentTemplatesList = () => {
  const { t } = useTranslation();
  const templates = useDocumentTemplates();
  const download = useDownloadDocumentTemplateFile();
  const setActive = useSetDocumentTemplateActive();
  const [status, setStatus] = useState<Status | null>(null);
  const [replacing, setReplacing] = useState<string | null>(null);
  // dopo un'azione il focus va a un elemento che esiste solo dopo il render
  const [focusTarget, setFocusTarget] = useState<{ id: string } | null>(null);

  useEffect(() => {
    if (focusTarget) document.getElementById(focusTarget.id)?.focus();
  }, [focusTarget]);

  const failed = (error: unknown): void => {
    const body = apiError(error);
    setStatus({
      text: body?.message
        ? t('documentTemplates.status.error', { reason: body.message })
        : t('documentTemplates.status.genericError'),
      error: true,
    });
  };

  const onDownload = async (template: DocumentTemplate): Promise<void> => {
    try {
      saveBlob(await download.mutateAsync(template.id), template.fileName);
      setStatus({ text: t('documentTemplates.status.downloaded', { name: template.name }) });
    } catch (error) {
      failed(error);
    }
  };

  const onToggle = async (template: DocumentTemplate): Promise<void> => {
    try {
      await setActive.mutateAsync({ id: template.id, active: !template.active, version: template.version });
      setStatus({
        text: t(
          template.active ? 'documentTemplates.status.deactivated' : 'documentTemplates.status.activated',
          {
            name: template.name,
          },
        ),
      });
      // il pulsante cambia nome: il focus resta lì
      setFocusTarget({ id: `template-${template.id}-toggle` });
    } catch (error) {
      failed(error);
    }
  };

  if (templates.isPending) return <p role="status">{t('documentTemplates.list.loading')}</p>;
  if (templates.isError) {
    return <p className="font-medium text-destructive">{t('documentTemplates.list.loadError')}</p>;
  }

  return (
    <div className="space-y-3">
      <p role="status" className={status?.error ? 'font-medium text-destructive' : 'text-muted-foreground'}>
        {status?.text}
      </p>
      {templates.data.length === 0 ? (
        <p className="text-muted-foreground">{t('documentTemplates.list.empty')}</p>
      ) : (
        <ul className="space-y-3">
          {templates.data.map((template) => {
            const nameId = `template-${template.id}-name`;
            return (
              <li key={template.id}>
                <article
                  aria-labelledby={nameId}
                  className={cn(
                    'space-y-2 rounded-xl border border-border bg-card p-4',
                    !template.active && 'border-dashed',
                  )}
                >
                  <div className="flex flex-wrap items-baseline gap-x-3 gap-y-1">
                    <h3 id={nameId} className="font-semibold">
                      {template.name}
                    </h3>
                    <span
                      className={cn(
                        'rounded-md px-2 py-0.5 text-sm font-medium',
                        template.active
                          ? 'bg-secondary text-secondary-foreground'
                          : 'bg-muted text-muted-foreground',
                      )}
                    >
                      {template.active
                        ? t('documentTemplates.list.active')
                        : t('documentTemplates.list.inactive')}
                    </span>
                  </div>
                  <p className="font-medium">{t(`documentTemplates.documents.${template.document}`)}</p>
                  <p>
                    {t('documentTemplates.list.conditions', {
                      project: template.project.name,
                      type: template.internshipType.name,
                      agency: template.careAgency?.name ?? t('documentTemplates.allAgencies'),
                    })}
                  </p>
                  <p className="text-sm text-muted-foreground">
                    {t('documentTemplates.list.file', {
                      name: template.fileName,
                      size: size(template.sizeBytes),
                    })}
                    {' · '}
                    {t('documentTemplates.list.uploaded', {
                      date: dateTime.format(new Date(template.uploadedAt)),
                    })}
                    {template.updatedAt !== template.uploadedAt &&
                      `, ${t('documentTemplates.list.updated', { date: dateTime.format(new Date(template.updatedAt)) })}`}
                  </p>
                  <div className="flex flex-wrap gap-2">
                    <Button
                      type="button"
                      variant="outline"
                      aria-label={t('documentTemplates.actions.download', { name: template.name })}
                      onClick={() => void onDownload(template)}
                      disabled={download.isPending}
                    >
                      <FileDown aria-hidden="true" />
                      {t('documentTemplates.actions.downloadShort')}
                    </Button>
                    <Button
                      id={`template-${template.id}-replace`}
                      type="button"
                      variant="outline"
                      aria-label={t('documentTemplates.actions.replaceOf', { name: template.name })}
                      aria-expanded={replacing === template.id}
                      onClick={() => {
                        setReplacing(replacing === template.id ? null : template.id);
                        if (replacing !== template.id) setFocusTarget({ id: `template-${template.id}-file` });
                      }}
                    >
                      <FileUp aria-hidden="true" />
                      {t('documentTemplates.actions.replace')}
                    </Button>
                    <Button
                      id={`template-${template.id}-toggle`}
                      type="button"
                      variant="outline"
                      aria-label={t(
                        template.active
                          ? 'documentTemplates.actions.deactivateOf'
                          : 'documentTemplates.actions.activateOf',
                        { name: template.name },
                      )}
                      onClick={() => void onToggle(template)}
                      disabled={setActive.isPending}
                    >
                      <Power aria-hidden="true" />
                      {template.active
                        ? t('documentTemplates.actions.deactivate')
                        : t('documentTemplates.actions.activate')}
                    </Button>
                  </div>
                  {replacing === template.id && (
                    <ReplaceFileForm
                      template={template}
                      onDone={(text) => {
                        setReplacing(null);
                        setStatus({ text });
                        setFocusTarget({ id: `template-${template.id}-replace` });
                      }}
                      onCancel={() => {
                        setReplacing(null);
                        setFocusTarget({ id: `template-${template.id}-replace` });
                      }}
                    />
                  )}
                </article>
              </li>
            );
          })}
        </ul>
      )}
    </div>
  );
};

type ReplaceFileFormProps = {
  template: DocumentTemplate;
  onDone: (text: string) => void;
  onCancel: () => void;
};

/** Sostituzione del file di un modello: stessi controlli del caricamento, errore sotto il campo. */
const ReplaceFileForm = ({ template, onDone, onCancel }: ReplaceFileFormProps) => {
  const { t } = useTranslation();
  const replace = useReplaceDocumentTemplateFile();
  const [file, setFile] = useState<File | null>(null);
  const [error, setError] = useState<string | null>(null);
  const inputId = `template-${template.id}-file`;
  const errorId = `${inputId}-error`;

  const onSubmit = async (event: FormEvent<HTMLFormElement>): Promise<void> => {
    event.preventDefault();
    if (!file) {
      setError(t('form.errors.required'));
      document.getElementById(inputId)?.focus();
      return;
    }
    try {
      await replace.mutateAsync({ id: template.id, version: template.version, file });
      onDone(t('documentTemplates.status.replaced', { name: template.name }));
    } catch (e) {
      const body = apiError(e);
      setError(
        body?.fieldErrors?.[0]?.message ?? body?.message ?? t('documentTemplates.status.genericError'),
      );
      document.getElementById(inputId)?.focus();
    }
  };

  return (
    <form noValidate onSubmit={(e) => void onSubmit(e)} className="space-y-2 rounded-md bg-muted p-3">
      <label htmlFor={inputId} className="block text-sm font-medium">
        {t('documentTemplates.actions.replaceFile', { name: template.name })}
      </label>
      <input
        id={inputId}
        type="file"
        accept=".docx,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        aria-invalid={error ? true : undefined}
        aria-describedby={error ? errorId : undefined}
        onChange={(e) => {
          setError(null);
          setFile(e.target.files?.[0] ?? null);
        }}
        className="min-h-11 max-w-full text-sm file:mr-3 file:min-h-11 file:rounded-md file:border file:border-border file:bg-background file:px-3 file:font-medium"
      />
      {error && (
        <p id={errorId} className="flex items-start gap-1.5 text-sm font-medium text-destructive">
          <CircleAlert aria-hidden="true" className="mt-0.5 size-4 shrink-0" />
          {error}
        </p>
      )}
      <div className="flex flex-wrap gap-2">
        <Button type="submit" disabled={replace.isPending}>
          {t('documentTemplates.actions.replaceSubmit')}
        </Button>
        <Button type="button" variant="outline" onClick={onCancel} disabled={replace.isPending}>
          {t('documentTemplates.actions.cancel')}
        </Button>
      </div>
    </form>
  );
};
