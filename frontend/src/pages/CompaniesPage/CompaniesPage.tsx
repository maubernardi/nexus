import { type FormEvent } from 'react';
import { Plus } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Link, useSearchParams } from 'react-router';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { ToCompleteBadge } from '@/atoms/ToCompleteBadge/ToCompleteBadge';
import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import { Input } from '@/components/ui/input';
import { useCompanySearch } from '@/config/hooks/useCompanies';
import { isMissing } from '@/lib/companyCompletion';

// parametri dell'indirizzo: Indietro e link condivisi mantengono la ricerca
const SEARCH_PARAM = 'q';
const INACTIVE_PARAM = 'disattivate';
const TO_COMPLETE_PARAM = 'daCompletare';

type Filters = { q: string; inactive: boolean; toComplete: boolean };

/** Anagrafica aziende (US-501): ricerca ed elenco per Call Center e ADMIN, con le aziende da completare (US-508). */
export const CompaniesPage = () => {
  const { t } = useTranslation();
  const [searchParams, setSearchParams] = useSearchParams();
  const search = searchParams.get(SEARCH_PARAM) ?? '';
  const includeInactive = searchParams.get(INACTIVE_PARAM) === '1';
  const toComplete = searchParams.get(TO_COMPLETE_PARAM) === '1';
  const companies = useCompanySearch({
    search: search || undefined,
    includeInactive,
    toComplete: toComplete || undefined,
  });
  const current: Filters = { q: search, inactive: includeInactive, toComplete };

  const update = ({ q, inactive, toComplete: onlyToComplete }: Filters): void => {
    const next = new URLSearchParams();
    if (q.trim()) next.set(SEARCH_PARAM, q.trim());
    if (inactive) next.set(INACTIVE_PARAM, '1');
    if (onlyToComplete) next.set(TO_COMPLETE_PARAM, '1');
    setSearchParams(next);
  };

  const onSearch = (event: FormEvent<HTMLFormElement>): void => {
    event.preventDefault();
    const value = new FormData(event.currentTarget).get('q');
    update({ ...current, q: typeof value === 'string' ? value : '' });
  };

  const th = 'px-3 py-2 text-left text-sm font-semibold whitespace-nowrap';
  const td = 'px-3 py-2 align-top';

  return (
    <div className="space-y-6">
      <PageHeading>{t('pages.companies.heading')}</PageHeading>
      <div className="flex flex-wrap items-center justify-between gap-3">
        <p className="text-muted-foreground">{t('pages.companies.intro')}</p>
        <Button asChild size="lg">
          <Link to="/aziende/nuova">
            <Plus aria-hidden="true" />
            {t('company.new')}
          </Link>
        </Button>
      </div>

      <form
        role="search"
        onSubmit={onSearch}
        className="space-y-3 rounded-xl border border-border bg-card p-4"
      >
        <label htmlFor="company-search" className="block text-sm font-medium">
          {t('company.search')}
        </label>
        <div className="flex flex-wrap gap-3">
          {/* key: il campo si riallinea all'indirizzo (es. tasto Indietro) */}
          <Input
            key={search}
            id="company-search"
            name="q"
            type="search"
            defaultValue={search}
            className="max-w-md flex-1"
          />
          <Button type="submit" size="lg" variant="outline">
            {t('company.searchSubmit')}
          </Button>
        </div>
        <div className="flex flex-wrap gap-x-6 gap-y-3">
          <div className="flex items-center gap-2">
            <Checkbox
              id="company-inactive"
              checked={includeInactive}
              onChange={(e) => update({ ...current, inactive: e.target.checked })}
            />
            <label htmlFor="company-inactive" className="text-sm">
              {t('company.includeInactive')}
            </label>
          </div>
          <div className="flex items-center gap-2">
            <Checkbox
              id="company-to-complete"
              checked={toComplete}
              onChange={(e) => update({ ...current, toComplete: e.target.checked })}
            />
            <label htmlFor="company-to-complete" className="text-sm">
              {t('company.onlyToComplete')}
            </label>
          </div>
        </div>
      </form>

      <p role="status" className="font-medium">
        {companies.data ? t('company.count', { count: companies.data.length }) : ''}
      </p>

      {companies.isError && !companies.data ? (
        <div role="alert" className="space-y-3 rounded-md border-2 border-destructive bg-card p-4">
          <p className="font-medium text-destructive">{t('company.loadError')}</p>
          <Button variant="outline" onClick={() => void companies.refetch()}>
            {t('common.retry')}
          </Button>
        </div>
      ) : !companies.data ? (
        <p>{t('common.loading')}</p>
      ) : companies.data.length === 0 ? (
        <p className="rounded-xl border border-border bg-card p-6">{t('company.empty')}</p>
      ) : (
        <div
          role="region"
          aria-labelledby="companies-caption"
          // eslint-disable-next-line jsx-a11y/no-noninteractive-tabindex -- regione scorrevole: deve essere raggiungibile da tastiera
          tabIndex={0}
          className="relative overflow-x-auto rounded-xl border border-border bg-card focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
        >
          <table className="w-full border-collapse text-sm max-lg:w-max max-lg:whitespace-nowrap">
            <caption id="companies-caption" className="sr-only">
              {t('company.caption')}
            </caption>
            <thead className="border-b border-border bg-muted">
              <tr>
                <th scope="col" className={th}>
                  {t('company.fields.name')}
                </th>
                <th scope="col" className={th}>
                  {t('company.fields.vatCode')}
                </th>
                <th scope="col" className={th}>
                  {t('company.fields.contactPerson')}
                </th>
                <th scope="col" className={th}>
                  {t('company.fields.phone')}
                </th>
                <th scope="col" className={th}>
                  {t('company.fields.email')}
                </th>
                <th scope="col" className={th}>
                  {t('company.status')}
                </th>
              </tr>
            </thead>
            <tbody>
              {companies.data.map((c) => (
                <tr key={c.id} className="border-b border-border last:border-b-0">
                  <th scope="row" className={`${td} text-left font-semibold`}>
                    <Link to={`/aziende/${c.id}`} className="text-primary underline underline-offset-4">
                      {c.name}
                    </Link>
                    {isMissing(c, 'NAME') && (
                      <>
                        {' '}
                        <ToCompleteBadge />
                      </>
                    )}
                  </th>
                  <td className={`${td} tabular-nums`}>
                    {isMissing(c, 'VAT_CODE') ? <ToCompleteBadge /> : c.vatCode}
                  </td>
                  <td className={td}>{c.contactPerson ?? '—'}</td>
                  <td className={td}>{c.phone ?? '—'}</td>
                  <td className={td}>{c.email ?? '—'}</td>
                  <td className={td}>{t(c.active ? 'company.active' : 'company.inactive')}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};
