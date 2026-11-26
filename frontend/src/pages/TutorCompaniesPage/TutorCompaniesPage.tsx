import { useEffect, useRef, type FormEvent } from 'react';
import { useTranslation } from 'react-i18next';
import { useSearchParams } from 'react-router';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { RatingDisplay } from '@/atoms/RatingDisplay/RatingDisplay';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { NativeSelect } from '@/components/ui/native-select';
import {
  COMPANY_PAGE_SIZES,
  DEFAULT_COMPANY_PAGE_SIZE,
  type CompanyDirectoryItem,
} from '@/config/api/companyApi';
import { useCompanyDirectory } from '@/config/hooks/useCompanies';
import { useBusinessCategories, useZones } from '@/config/hooks/useReferenceData';
import { parsePage, parsePageSize } from '@/lib/pageParams';
import { Pagination } from '@/molecules/Pagination/Pagination';

// parametri dell'indirizzo, come nell'elenco del Call Center
const SEARCH_PARAM = 'q';
const CATEGORY_PARAM = 'categoria';
const ZONE_PARAM = 'zona';
const PAGE_PARAM = 'pagina';
const PAGE_SIZE_PARAM = 'perPagina';

const number = new Intl.NumberFormat('it-IT');

type Filters = { q: string; category: string; zone: string };

/** Indirizzo e comune su una riga: «Via Pratese 9, Prato (PO)». */
const fullAddress = (c: CompanyDirectoryItem): string =>
  [c.address, c.municipality].filter(Boolean).join(', ') || '—';

/**
 * Aziende per il Tutor (US-511): elenco in sola lettura delle aziende attive, con i soli dati essenziali (nome,
 * categoria, zona, indirizzo, stelle e pollici). Ricerca sul nome, filtri per categoria e zona e pagine stanno
 * nell'indirizzo, come nell'elenco del Call Center (US-513).
 */
export const TutorCompaniesPage = () => {
  const { t } = useTranslation();
  const [searchParams, setSearchParams] = useSearchParams();
  const search = searchParams.get(SEARCH_PARAM) ?? '';
  const category = searchParams.get(CATEGORY_PARAM) ?? '';
  const zone = searchParams.get(ZONE_PARAM) ?? '';
  const page = parsePage(searchParams.get(PAGE_PARAM));
  const pageSize = parsePageSize(searchParams.get(PAGE_SIZE_PARAM));
  const categories = useBusinessCategories();
  const zones = useZones();
  const companies = useCompanyDirectory({
    search: search || undefined,
    businessCategoryId: category || undefined,
    zoneId: zone || undefined,
    page: page - 1,
    size: pageSize,
  });
  const current: Filters = { q: search, category, zone };
  const result = companies.data;
  const total = result?.total ?? 0;
  const pageCount = Math.max(1, Math.ceil(total / pageSize));
  const tableRef = useRef<HTMLDivElement>(null);

  /** Filtri e pagina nell'indirizzo; i valori predefiniti restano fuori. */
  const update = (
    { q, category: categoryId, zone: zoneId }: Filters,
    nextPage = 1,
    nextPageSize = pageSize,
  ): void => {
    const next = new URLSearchParams();
    if (q.trim()) next.set(SEARCH_PARAM, q.trim());
    if (categoryId) next.set(CATEGORY_PARAM, categoryId);
    if (zoneId) next.set(ZONE_PARAM, zoneId);
    if (nextPage > 1) next.set(PAGE_PARAM, String(nextPage));
    if (nextPageSize !== DEFAULT_COMPANY_PAGE_SIZE) next.set(PAGE_SIZE_PARAM, String(nextPageSize));
    setSearchParams(next);
  };

  const goToPage = (nextPage: number): void => {
    update(current, nextPage);
    // la pagina nuova si legge dall'inizio, come nell'elenco del Call Center
    const region = tableRef.current;
    region?.focus({ preventScroll: true });
    region?.scrollIntoView?.({ block: 'start' });
  };

  // pagina oltre l'ultima (indirizzo vecchio): si passa all'ultima
  useEffect(() => {
    if (!result || companies.isPlaceholderData || page <= pageCount) return;
    setSearchParams(
      (previous) => {
        const next = new URLSearchParams(previous);
        if (pageCount > 1) next.set(PAGE_PARAM, String(pageCount));
        else next.delete(PAGE_PARAM);
        return next;
      },
      { replace: true },
    );
  }, [result, companies.isPlaceholderData, page, pageCount, setSearchParams]);

  const first = result ? result.page * result.size + 1 : 0;
  const status = !result
    ? ''
    : total > result.companies.length && result.companies.length > 0
      ? t('company.countRange', {
          total: number.format(total),
          from: number.format(first),
          to: number.format(first + result.companies.length - 1),
        })
      : t('company.count', { count: total, total: number.format(total) });

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
      <p className="text-muted-foreground">{t('companyDirectory.intro')}</p>

      <form
        role="search"
        onSubmit={onSearch}
        className="space-y-3 rounded-xl border border-border bg-card p-4"
      >
        <label htmlFor="directory-search" className="block text-sm font-medium">
          {t('companyDirectory.search')}
        </label>
        <div className="flex flex-wrap gap-3">
          {/* key: il campo si riallinea all'indirizzo (es. tasto Indietro) */}
          <Input
            key={search}
            id="directory-search"
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
          <div className="w-full max-w-md space-y-1">
            <label htmlFor="directory-category" className="block text-sm font-medium">
              {t('company.categoryFilter')}
            </label>
            <NativeSelect
              id="directory-category"
              value={category}
              disabled={!categories.data}
              onChange={(e) => update({ ...current, category: e.target.value })}
            >
              <option value="">{t('company.allCategories')}</option>
              {(categories.data ?? []).map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </NativeSelect>
          </div>
          <div className="w-full max-w-3xs space-y-1">
            <label htmlFor="directory-zone" className="block text-sm font-medium">
              {t('company.zoneFilter')}
            </label>
            <NativeSelect
              id="directory-zone"
              value={zone}
              disabled={!zones.data}
              onChange={(e) => update({ ...current, zone: e.target.value })}
            >
              <option value="">{t('company.allZones')}</option>
              {(zones.data ?? []).map((z) => (
                <option key={z.id} value={z.id}>
                  {z.name}
                </option>
              ))}
            </NativeSelect>
          </div>
        </div>
      </form>

      <p role="status" className="font-medium">
        {status}
      </p>

      {companies.isError && !result ? (
        <div role="alert" className="space-y-3 rounded-md border-2 border-destructive bg-card p-4">
          <p className="font-medium text-destructive">{t('company.loadError')}</p>
          <Button variant="outline" onClick={() => void companies.refetch()}>
            {t('common.retry')}
          </Button>
        </div>
      ) : !result ? (
        <p>{t('common.loading')}</p>
      ) : total === 0 ? (
        <p className="rounded-xl border border-border bg-card p-6">{t('company.empty')}</p>
      ) : (
        <>
          <div
            ref={tableRef}
            role="region"
            aria-labelledby="directory-caption"
            // eslint-disable-next-line jsx-a11y/no-noninteractive-tabindex -- regione scorrevole: deve essere raggiungibile da tastiera
            tabIndex={0}
            className="relative scroll-mt-28 overflow-x-auto rounded-xl border border-border bg-card focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
          >
            <table className="w-full border-collapse text-sm max-lg:w-max max-lg:whitespace-nowrap">
              <caption id="directory-caption" className="sr-only">
                {t('companyDirectory.caption')}
              </caption>
              <thead className="border-b border-border bg-muted">
                <tr>
                  <th scope="col" className={th}>
                    {t('company.fields.name')}
                  </th>
                  <th scope="col" className={th}>
                    {t('company.category')}
                  </th>
                  <th scope="col" className={th}>
                    {t('company.fields.zone')}
                  </th>
                  <th scope="col" className={th}>
                    {t('companyDirectory.address')}
                  </th>
                  <th scope="col" className={th}>
                    {t('company.fields.reliability')}
                  </th>
                  <th scope="col" className={th}>
                    {t('company.fields.projectInterest')}
                  </th>
                </tr>
              </thead>
              <tbody>
                {result.companies.map((c) => (
                  <tr key={c.id} className="border-b border-border last:border-b-0">
                    <th scope="row" className={`${td} text-left font-semibold`}>
                      {c.name}
                    </th>
                    <td className={td}>{c.businessCategory?.name ?? '—'}</td>
                    <td className={td}>{c.zone?.name ?? '—'}</td>
                    <td className={td}>{fullAddress(c)}</td>
                    <td className={td}>
                      <RatingDisplay kind="stars" value={c.reliability} />
                    </td>
                    <td className={td}>
                      <RatingDisplay kind="thumbs" value={c.projectInterest} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          {total > COMPANY_PAGE_SIZES[0] && (
            <Pagination
              id="directory-page-size"
              page={Math.min(page, pageCount)}
              pageCount={pageCount}
              pageSize={pageSize}
              pageSizes={COMPANY_PAGE_SIZES}
              label={t('company.pages')}
              pageSizeLabel={t('company.pageSize')}
              onPageChange={goToPage}
              onPageSizeChange={(size) => update(current, 1, size)}
            />
          )}
        </>
      )}
    </div>
  );
};
