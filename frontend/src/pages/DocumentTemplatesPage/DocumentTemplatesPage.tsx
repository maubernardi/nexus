import { useTranslation } from 'react-i18next';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { useTemplatePlaceholders } from '@/config/hooks/useDocumentTemplates';
import { DocumentTemplatesList } from '@/organisms/DocumentTemplatesList/DocumentTemplatesList';
import { DocumentTemplateUploadForm } from '@/organisms/DocumentTemplateUploadForm/DocumentTemplateUploadForm';

/**
 * Modelli Word dei documenti per l'ADMIN (US-1010): i segnaposto che si possono usare, il caricamento con le condizioni
 * e l'elenco dei modelli caricati.
 */
export const DocumentTemplatesPage = () => {
  const { t } = useTranslation();
  const placeholders = useTemplatePlaceholders();

  return (
    <div className="space-y-8">
      <PageHeading>{t('pages.documentTemplates.heading')}</PageHeading>
      <p className="text-muted-foreground">{t('pages.documentTemplates.intro')}</p>

      <details className="rounded-xl border border-border bg-card p-4">
        <summary className="min-h-11 cursor-pointer content-center font-semibold text-primary">
          {t('documentTemplates.placeholders.summary')}
        </summary>
        <div className="mt-3 space-y-3">
          <p>{t('documentTemplates.placeholders.intro')}</p>
          {placeholders.data ? (
            <ul className="grid gap-1 sm:grid-cols-2 lg:grid-cols-3">
              {placeholders.data.map((p) => (
                <li key={p}>
                  <code className="rounded bg-muted px-1.5 py-0.5 text-sm">{`{${p}}`}</code>
                </li>
              ))}
            </ul>
          ) : (
            <p role="status">{t('documentTemplates.placeholders.loading')}</p>
          )}
        </div>
      </details>

      <section
        aria-labelledby="templates-upload"
        className="space-y-4 rounded-xl border border-border bg-card p-4"
      >
        <h2 id="templates-upload" className="text-lg font-semibold text-primary">
          {t('documentTemplates.upload.heading')}
        </h2>
        <DocumentTemplateUploadForm />
      </section>

      <section aria-labelledby="templates-list" className="space-y-4">
        <h2 id="templates-list" className="text-lg font-semibold text-primary">
          {t('documentTemplates.list.heading')}
        </h2>
        <DocumentTemplatesList />
      </section>
    </div>
  );
};
