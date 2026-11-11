import { useState, type FormEvent } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useNavigate, useParams } from 'react-router';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import type { CatalogItem } from '@/config/api/catalogApi';
import { apiError } from '@/config/api/errors';
import type { AdminUser } from '@/config/api/userAdminApi';
import { useCatalog } from '@/config/hooks/useCatalogs';
import { useUpdateUserProjects, useUser } from '@/config/hooks/useUsers';
import { useFocusAfterRender } from '@/hooks/useFocusAfterRender';
import type { UsersPageState } from '@/pages/UsersPage/UsersPage';

const backLink = 'font-medium text-primary underline underline-offset-4';

/**
 * Progetti di un Tutor (US-104): l'ADMIN sceglie i progetti di cui il Tutor vede i ticket in sola lettura e su cui può
 * segnalare. Un progetto disattivato già assegnato resta in elenco per poterlo togliere, ma non si assegna. Per ogni
 * progetto assegnato l'ADMIN può nominarlo Coordinatore, che ne sposta le segnalazioni tra i Tutor (US-112).
 */
export const UserProjectsPage = () => {
  const { t } = useTranslation();
  const { id = '' } = useParams();
  const user = useUser(id);
  const catalog = useCatalog('projects');

  const failed = (user.isError && !user.data) || (catalog.isError && !catalog.data);

  return (
    <div className="space-y-6">
      <PageHeading>
        {user.data
          ? t('users.projects.heading', { name: `${user.data.firstName} ${user.data.lastName}` })
          : t('pages.userProjects.title')}
      </PageHeading>
      {failed ? (
        <div role="alert" className="space-y-3 rounded-md border-2 border-destructive bg-card p-4">
          <p className="font-medium text-destructive">{t('users.projects.loadError')}</p>
          <Button
            variant="outline"
            onClick={() => {
              void user.refetch();
              void catalog.refetch();
            }}
          >
            {t('common.retry')}
          </Button>
        </div>
      ) : !user.data || !catalog.data ? (
        <p>{t('common.loading')}</p>
      ) : user.data.role !== 'TUTOR' ? (
        <>
          <p>{t('users.projects.notTutor', { role: t(`roles.${user.data.role}`) })}</p>
          <Link to="/utenti" className={backLink}>
            {t('users.backToList')}
          </Link>
        </>
      ) : (
        <ProjectsForm user={user.data} catalog={catalog.data} />
      )}
    </div>
  );
};

type ProjectsFormProps = { user: AdminUser; catalog: CatalogItem[] };

const ProjectsForm = ({ user, catalog }: ProjectsFormProps) => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const update = useUpdateUserProjects();
  const [selected, setSelected] = useState<string[]>(() => user.projects.map((p) => p.id));
  const [coordinated, setCoordinated] = useState<string[]>(() =>
    (user.coordinatedProjects ?? []).map((p) => p.id),
  );
  const [error, setError] = useState<string | null>(null);
  const { ref: errorRef, requestFocus: focusError } = useFocusAfterRender<HTMLParagraphElement>();

  const assigned = new Set(user.projects.map((p) => p.id));
  // attivi, più i disattivati ancora assegnati (si possono solo togliere); il catalogo è già in ordine
  const options = catalog.filter((p) => p.active || assigned.has(p.id));
  const name = `${user.firstName} ${user.lastName}`;

  const toggle = (projectId: string, checked: boolean): void =>
    setSelected((current) => (checked ? [...current, projectId] : current.filter((v) => v !== projectId)));

  const toggleCoordinator = (projectId: string, checked: boolean): void =>
    setCoordinated((current) => (checked ? [...current, projectId] : current.filter((v) => v !== projectId)));

  const onSubmit = async (event: FormEvent<HTMLFormElement>): Promise<void> => {
    event.preventDefault();
    setError(null);
    try {
      // si coordina solo un progetto assegnato: togliendo il progetto si toglie anche il coordinamento
      await update.mutateAsync({
        id: user.id,
        projectIds: selected,
        coordinatedProjectIds: coordinated.filter((v) => selected.includes(v)),
      });
      const state: UsersPageState = { projectsSaved: name };
      void navigate('/utenti', { state });
    } catch (e) {
      const body = apiError(e);
      setError(
        body?.fieldErrors?.[0]?.message ??
          (body?.status === 400 && body.message ? body.message : t('users.error')),
      );
      focusError();
    }
  };

  return (
    <form noValidate onSubmit={(e) => void onSubmit(e)} className="space-y-6">
      <p className="text-muted-foreground">{t('users.projects.intro')}</p>
      {error && (
        <p
          ref={errorRef}
          role="alert"
          tabIndex={-1}
          className="rounded-md border-2 border-destructive bg-card p-4 font-medium text-destructive"
        >
          {error}
        </p>
      )}
      {options.length === 0 ? (
        <p>
          {t('users.projects.noneInCatalog')}{' '}
          <Link to="/cataloghi" className={backLink}>
            {t('users.projects.toCatalogs')}
          </Link>
        </p>
      ) : (
        <fieldset className="max-w-xl space-y-2">
          <legend className="mb-2 font-semibold">{t('users.projects.legend', { name })}</legend>
          {options.map((p) => {
            const inputId = `project-${p.id}`;
            const coordinatorId = `coordinator-${p.id}`;
            const isSelected = selected.includes(p.id);
            return (
              <div
                key={p.id}
                className="flex min-h-11 flex-wrap items-center justify-between gap-x-4 rounded-md border border-border bg-card px-3"
              >
                <label htmlFor={inputId} className="flex min-h-11 items-center gap-3">
                  <Checkbox
                    id={inputId}
                    checked={isSelected}
                    onChange={(e) => toggle(p.id, e.target.checked)}
                  />
                  <span>{p.active ? p.name : t('users.projects.inactive', { name: p.name })}</span>
                </label>
                {isSelected && (
                  <label htmlFor={coordinatorId} className="flex min-h-11 items-center gap-3">
                    <Checkbox
                      id={coordinatorId}
                      // il nome visibile è «Coordinatore»: quello accessibile dice di quale progetto
                      aria-label={t('users.projects.coordinatorOf', { name: p.name })}
                      checked={coordinated.includes(p.id)}
                      onChange={(e) => toggleCoordinator(p.id, e.target.checked)}
                    />
                    <span>{t('users.projects.coordinator')}</span>
                  </label>
                )}
              </div>
            );
          })}
        </fieldset>
      )}
      <div className="flex flex-wrap items-center gap-4">
        <Button type="submit" size="lg" disabled={update.isPending || options.length === 0}>
          {update.isPending ? t('users.projects.saving') : t('users.projects.save')}
        </Button>
        <Link to="/utenti" className={backLink}>
          {t('users.backToList')}
        </Link>
      </div>
    </form>
  );
};
