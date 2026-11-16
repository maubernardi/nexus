import { useTranslation } from 'react-i18next';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import type { TemplatePlaceholder } from '@/config/api/documentTemplateApi';
import { useTemplatePlaceholders } from '@/config/hooks/useDocumentTemplates';
import { DocumentTemplatesList } from '@/organisms/DocumentTemplatesList/DocumentTemplatesList';
import { DocumentTemplateUploadForm } from '@/organisms/DocumentTemplateUploadForm/DocumentTemplateUploadForm';

/** I segnaposto divisi per gruppo, nell'ordine in cui arrivano (US-1011). */
const groups = (placeholders: TemplatePlaceholder[]): [string, TemplatePlaceholder[]][] => {
  const byGroup = new Map<string, TemplatePlaceholder[]>();
  placeholders.forEach((p) => byGroup.set(p.group, [...(byGroup.get(p.group) ?? []), p]));
  return [...byGroup.entries()];
};

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
            groups(placeholders.data).map(([group, items]) => (
              <div key={group} className="space-y-2">
                <p className="font-semibold">{group}</p>
                <dl className="grid gap-x-4 gap-y-2 sm:grid-cols-[minmax(0,24rem)_1fr]">
                  {items.map((p) => (
                    <div key={p.name} className="contents">
                      <dt>
                        <code className="rounded bg-muted px-1.5 py-0.5 text-sm break-all">{`{${p.name}}`}</code>
                      </dt>
                      <dd className="text-muted-foreground">{p.description}</dd>
                    </div>
                  ))}
                </dl>
              </div>
            ))
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
