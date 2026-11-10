import { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';

import { Button } from '@/components/ui/button';
import type { ProjectInternshipTypes } from '@/config/api/documentCatalogApi';
import { useCatalog } from '@/config/hooks/useCatalogs';
import {
  useCareAgencies,
  useCompetenceSites,
  useProjectInternshipTypes,
  useSetCompetenceSiteProjects,
  useSetProjectInternshipTypes,
} from '@/config/hooks/useDocumentCatalogs';
import { CareAgencyForm } from '@/organisms/CareAgencyForm/CareAgencyForm';
import { CatalogSection } from '@/organisms/CatalogSection/CatalogSection';
import { ChecklistForm } from '@/organisms/ChecklistForm/ChecklistForm';

/** Voci attive di un catalogo più quelle già scelte (che restano anche se disattivate), come opzioni. */
const useOptions = (kind: 'projects' | 'internship-types', chosen: readonly string[]) => {
  const items = useCatalog(kind);
  return (items.data ?? [])
    .filter((i) => i.active || chosen.includes(i.id))
    .map((i) => ({ value: i.id, label: i.name }));
};

const SiteProjectsEditor = ({
  siteId,
  initial,
  onDone,
  onCancel,
}: {
  siteId: string;
  initial: string[];
  onDone: () => void;
  onCancel: () => void;
}) => {
  const { t } = useTranslation();
  const save = useSetCompetenceSiteProjects();
  const options = useOptions('projects', initial);
  return (
    <ChecklistForm
      legend={t('documentCatalogs.sites.legend')}
      hint={t('documentCatalogs.sites.hint')}
      options={options}
      initial={initial}
      onSave={async (ids) => {
        await save.mutateAsync({ id: siteId, ids });
        onDone();
      }}
      onCancel={onCancel}
    />
  );
};

/** Sedi di competenza (US-115): catalogo con i progetti di ogni sede. */
export const CompetenceSitesSection = () => {
  const { t } = useTranslation();
  const sites = useCompetenceSites();
  const byId = new Map((sites.data ?? []).map((s) => [s.id, s]));
  return (
    <CatalogSection
      kind="competence-sites"
      details={{
        header: t('documentCatalogs.sites.projects'),
        cell: (item) => {
          const projects = byId.get(item.id)?.projects ?? [];
          return projects.length > 0
            ? projects.map((p) => p.name).join(', ')
            : t('documentCatalogs.sites.none');
        },
        action: t('documentCatalogs.sites.action'),
        heading: (item) => t('documentCatalogs.sites.heading', { name: item.name }),
        editor: (item, onDone, onCancel) => (
          <SiteProjectsEditor
            siteId={item.id}
            initial={(byId.get(item.id)?.projects ?? []).map((p) => p.id)}
            onDone={() => onDone(t('documentCatalogs.sites.saved', { name: item.name }))}
            onCancel={onCancel}
          />
        ),
      }}
    />
  );
};

/** Enti di presa in carico (US-115): catalogo con tipologia e dati per i documenti di inclusione. */
export const CareAgenciesSection = () => {
  const { t } = useTranslation();
  const agencies = useCareAgencies();
  const byId = new Map((agencies.data ?? []).map((a) => [a.id, a]));
  return (
    <CatalogSection
      kind="care-agencies"
      details={{
        header: t('documentCatalogs.agencies.kind'),
        cell: (item) => {
          const agency = byId.get(item.id);
          return agency ? t(`documentCatalogs.agencies.kinds.${agency.kind}`) : '';
        },
        action: t('documentCatalogs.agencies.action'),
        heading: (item) => t('documentCatalogs.agencies.heading', { name: item.name }),
        editor: (item, onDone, onCancel) => {
          const agency = byId.get(item.id);
          return agency ? (
            <CareAgencyForm
              key={`${agency.id}-${agency.version}`}
              agency={agency}
              onDone={onDone}
              onCancel={onCancel}
            />
          ) : (
            <p>{t('common.loading')}</p>
          );
        },
      }}
    />
  );
};

const ProjectTypesEditor = ({
  row,
  onDone,
  onCancel,
}: {
  row: ProjectInternshipTypes;
  onDone: () => void;
  onCancel: () => void;
}) => {
  const { t } = useTranslation();
  const save = useSetProjectInternshipTypes();
  const initial = row.internshipTypes.map((i) => i.id);
  const options = useOptions('internship-types', initial);
  return (
    <ChecklistForm
      legend={t('documentCatalogs.projectTypes.legend')}
      hint={t('documentCatalogs.projectTypes.hint')}
      options={options}
      initial={initial}
      onSave={async (ids) => {
        await save.mutateAsync({ id: row.project.id, ids });
        onDone();
      }}
      onCancel={onCancel}
    />
  );
};

/** Tipi di tirocinio ammessi da ogni progetto (US-115); nessun tipo indicato = tutti. */
export const ProjectInternshipTypesSection = () => {
  const { t } = useTranslation();
  const rows = useProjectInternshipTypes();
  const [editing, setEditing] = useState<ProjectInternshipTypes | null>(null);
  const [outcome, setOutcome] = useState<string | null>(null);
  const headingRef = useRef<HTMLHeadingElement>(null);
  const outcomeRef = useRef<HTMLDivElement>(null);
  const th = 'px-3 py-2 text-left text-sm font-semibold whitespace-nowrap';
  const td = 'px-3 py-2 align-top';

  useEffect(() => {
    if (editing) headingRef.current?.focus();
  }, [editing]);
  useEffect(() => {
    if (outcome) outcomeRef.current?.focus();
  }, [outcome]);

  return (
    <section aria-labelledby="catalog-project-types-heading" className="space-y-4">
      <h2 id="catalog-project-types-heading" className="text-lg font-semibold text-primary">
        {t('documentCatalogs.projectTypes.heading')}
      </h2>
      <p className="text-muted-foreground">{t('documentCatalogs.projectTypes.intro')}</p>
      {outcome && (
        <div
          ref={outcomeRef}
          tabIndex={-1}
          className="rounded-md border-l-4 border-primary bg-card p-4 font-medium"
        >
          {outcome}
        </div>
      )}
      {editing && (
        <div className="space-y-4 rounded-xl border border-border bg-card p-4">
          <h3 ref={headingRef} tabIndex={-1} className="text-base font-semibold">
            {t('documentCatalogs.projectTypes.editHeading', { name: editing.project.name })}
          </h3>
          <ProjectTypesEditor
            key={editing.project.id}
            row={editing}
            onDone={() => {
              setOutcome(t('documentCatalogs.projectTypes.saved', { name: editing.project.name }));
              setEditing(null);
            }}
            onCancel={() => setEditing(null)}
          />
        </div>
      )}
      {rows.isError && !rows.data ? (
        <p
          role="alert"
          className="rounded-md border-2 border-destructive bg-card p-4 font-medium text-destructive"
        >
          {t('catalog.loadError')}
        </p>
      ) : !rows.data ? (
        <p>{t('common.loading')}</p>
      ) : (
        <div className="overflow-x-auto rounded-xl border border-border bg-card">
          <table className="w-full border-collapse text-sm">
            <caption className="sr-only">{t('documentCatalogs.projectTypes.caption')}</caption>
            <thead className="border-b border-border bg-muted">
              <tr>
                <th scope="col" className={th}>
                  {t('documentCatalogs.projectTypes.project')}
                </th>
                <th scope="col" className={th}>
                  {t('documentCatalogs.projectTypes.types')}
                </th>
                <th scope="col" className={th}>
                  {t('catalog.columns.actions')}
                </th>
              </tr>
            </thead>
            <tbody>
              {rows.data.map((row) => (
                <tr key={row.project.id} className="border-b border-border last:border-b-0">
                  <th scope="row" className={`${td} text-left font-semibold`}>
                    {row.project.name}
                    {!row.active && ` ${t('documentCatalogs.projectTypes.inactive')}`}
                  </th>
                  <td className={td}>
                    {row.internshipTypes.length > 0
                      ? row.internshipTypes.map((i) => i.name).join(', ')
                      : t('documentCatalogs.projectTypes.all')}
                  </td>
                  <td className={`${td} whitespace-nowrap`}>
                    <Button
                      size="sm"
                      variant="outline"
                      disabled={editing !== null}
                      onClick={() => {
                        setOutcome(null);
                        setEditing(row);
                      }}
                    >
                      {t('documentCatalogs.projectTypes.action')}{' '}
                      <span className="sr-only">{row.project.name}</span>
                    </Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
};
