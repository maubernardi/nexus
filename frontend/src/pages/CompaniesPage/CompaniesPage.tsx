import { useEffect, useRef, type FormEvent } from 'react';
import { Plus } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Link, useSearchParams } from 'react-router';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { RatingDisplay } from '@/atoms/RatingDisplay/RatingDisplay';
import { ToCompleteBadge } from '@/atoms/ToCompleteBadge/ToCompleteBadge';
import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import { Input } from '@/components/ui/input';
import { NativeSelect } from '@/components/ui/native-select';
import { COMPANY_PAGE_SIZES, DEFAULT_COMPANY_PAGE_SIZE, type Company } from '@/config/api/companyApi';
import { useCompanySearch } from '@/config/hooks/useCompanies';
import { useBusinessCategories, useZones } from '@/config/hooks/useReferenceData';
import { isMissing } from '@/lib/companyCompletion';
import { contactName } from '@/lib/companyContacts';
import { parsePage, parsePageSize } from '@/lib/pageParams';
import { RATING_MAX } from '@/lib/rating';
import { Pagination } from '@/molecules/Pagination/Pagination';

// parametri dell'indirizzo: Indietro e link condivisi mantengono la ricerca
const SEARCH_PARAM = 'q';
const INACTIVE_PARAM = 'disattivate';
const TO_COMPLETE_PARAM = 'daCompletare';
const CATEGORY_PARAM = 'categoria';
const ZONE_PARAM = 'zona';
const RELIABILITY_PARAM = 'stelle';
const PROJECT_INTEREST_PARAM = 'interesse';
const PAGE_PARAM = 'pagina';
const PAGE_SIZE_PARAM = 'perPagina';

const number = new Intl.NumberFormat('it-IT');

/** Stelle o pollici minimi dell'indirizzo (US-514): da 1 al massimo, altrimenti nessun filtro. */
const parseMin = (value: string | null, max: number): number | null => {
  const n = Number(value);
  return Number.isInteger(n) && n >= 1 && n <= max ? n : null;
};

/** 1, 2, … fino al massimo: le voci dei menu di stelle e pollici. */
const upTo = (max: number): number[] => Array.from({ length: max }, (_, i) => i + 1);

type Filters = {
  q: string;
  inactive: boolean;
  toComplete: boolean;
  category: string;
  zone: string;
  minReliability: number | null;
  minProjectInterest: number | null;
};

/**
 * Anagrafica aziende (US-501): ricerca ed elenco per Call Center e ADMIN, con le aziende da completare (US-508) e il
 * filtro per categoria merceologica (US-509). L'elenco arriva una pagina alla volta (US-513): pagina e aziende per
 * pagina stanno nell'indirizzo, come i filtri; un filtro nuovo riparte dalla prima pagina. Stelle e pollici hanno le
 * loro colonne e i loro filtri (US-514).
 */
export const CompaniesPage = () => {
  const { t } = useTranslation();
  /** Il primo referente, con quanti sono gli altri (US-510). */
  const firstContact = (c: Company): string => {
    const [first, ...others] = c.contacts ?? [];
    if (!first) return '—';
    const name = contactName(first);
    return others.length > 0 ? t('company.contacts.more', { name, count: others.length }) : name;
  };
  const [searchParams, setSearchParams] = useSearchParams();
  const search = searchParams.get(SEARCH_PARAM) ?? '';
  const includeInactive = searchParams.get(INACTIVE_PARAM) === '1';
  const toComplete = searchParams.get(TO_COMPLETE_PARAM) === '1';
  const category = searchParams.get(CATEGORY_PARAM) ?? '';
  const zone = searchParams.get(ZONE_PARAM) ?? '';
  const minReliability = parseMin(searchParams.get(RELIABILITY_PARAM), RATING_MAX.stars);
  const minProjectInterest = parseMin(searchParams.get(PROJECT_INTEREST_PARAM), RATING_MAX.thumbs);
  const page = parsePage(searchParams.get(PAGE_PARAM));
  const pageSize = parsePageSize(searchParams.get(PAGE_SIZE_PARAM));
  const categories = useBusinessCategories();
  const zones = useZones();
  const companies = useCompanySearch({
    search: search || undefined,
    includeInactive,
    toComplete: toComplete || undefined,
    businessCategoryId: category || undefined,
    zoneId: zone || undefined,
    minReliability: minReliability ?? undefined,
    minProjectInterest: minProjectInterest ?? undefined,
    page: page - 1,
    size: pageSize,
  });
  const current: Filters = {
    q: search,
    inactive: includeInactive,
    toComplete,
    category,
    zone,
    minReliability,
    minProjectInterest,
  };
  const result = companies.data;
  const total = result?.total ?? 0;
  const pageCount = Math.max(1, Math.ceil(total / pageSize));
  const tableRef = useRef<HTMLDivElement>(null);

  /** Filtri, pagina e aziende per pagina nell'indirizzo; i valori predefiniti restano fuori. */
  const update = (
    {
      q,
      inactive,
      toComplete: onlyToComplete,
      category: categoryId,
      zone: zoneId,
      minReliability: stars,
      minProjectInterest: thumbs,
    }: Filters,
    nextPage = 1,
    nextPageSize = pageSize,
  ): void => {
    const next = new URLSearchParams();
    if (q.trim()) next.set(SEARCH_PARAM, q.trim());
    if (categoryId) next.set(CATEGORY_PARAM, categoryId);
    if (zoneId) next.set(ZONE_PARAM, zoneId);
    if (stars) next.set(RELIABILITY_PARAM, String(stars));
    if (thumbs) next.set(PROJECT_INTEREST_PARAM, String(thumbs));
    if (inactive) next.set(INACTIVE_PARAM, '1');
    if (onlyToComplete) next.set(TO_COMPLETE_PARAM, '1');
    if (nextPage > 1) next.set(PAGE_PARAM, String(nextPage));
    if (nextPageSize !== DEFAULT_COMPANY_PAGE_SIZE) next.set(PAGE_SIZE_PARAM, String(nextPageSize));
    setSearchParams(next);
  };

  const goToPage = (nextPage: number): void => {
    update(current, nextPage);
    // la pagina nuova si legge dall'inizio: si torna in cima alla tabella, con il conteggio sopra e sotto l'intestazione
    // fissa (scroll-mt), e il focus va lì invece di restare sui pulsanti in fondo. Solo focus() non basta: la tabella è
    // già in parte visibile e il browser non la scorre
    const region = tableRef.current;
    region?.focus({ preventScroll: true });
    region?.scrollIntoView?.({ block: 'start' });
  };

  // pagina oltre l'ultima (indirizzo vecchio, aziende disattivate nel frattempo): si passa all'ultima
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

  /** Filtri o ricerca attivi: solo allora c'è «Azzera i filtri». */
  const filtered =
    Boolean(search || category || zone || minReliability || minProjectInterest) ||
    includeInactive ||
    toComplete;

  /**
   * Toglie ricerca e filtri, dalla prima pagina; le aziende per pagina restano. Il pulsante sparisce: il focus va al
   * campo di ricerca, ricreato dall'indirizzo nuovo.
   */
  const resetFilters = (): void => {
    update({
      q: '',
      inactive: false,
      toComplete: false,
      category: '',
      zone: '',
      minReliability: null,
      minProjectInterest: null,
    });
    window.requestAnimationFrame(() => document.getElementById('company-search')?.focus());
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
          <div className="w-full max-w-md space-y-1">
            <label htmlFor="company-category" className="block text-sm font-medium">
              {t('company.categoryFilter')}
            </label>
            {/* il filtro si applica alla scelta, come le caselle: niente pulsante */}
            <NativeSelect
              id="company-category"
              value={category}
              disabled={!categories.data}
              onChange={(e) => update({ ...current, category: e.target.value })}
            >
              <option value="">{t('company.allCategories')}</option>
              {/* una categoria nell'indirizzo ma non più attiva resta scelta: l'elenco la filtra comunque */}
              {category && categories.data && !categories.data.some((c) => c.id === category) && (
                <option value={category}>{t('company.otherCategory')}</option>
              )}
              {(categories.data ?? []).map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </NativeSelect>
          </div>
          <div className="w-full max-w-3xs space-y-1">
            <label htmlFor="company-zone" className="block text-sm font-medium">
              {t('company.zoneFilter')}
            </label>
            <NativeSelect
              id="company-zone"
              value={zone}
              disabled={!zones.data}
              onChange={(e) => update({ ...current, zone: e.target.value })}
            >
              <option value="">{t('company.allZones')}</option>
              {/* una zona nell'indirizzo ma non più attiva resta scelta: l'elenco la filtra comunque */}
              {zone && zones.data && !zones.data.some((z) => z.id === zone) && (
                <option value={zone}>{t('company.otherZone')}</option>
              )}
              {(zones.data ?? []).map((z) => (
                <option key={z.id} value={z.id}>
                  {z.name}
                </option>
              ))}
            </NativeSelect>
          </div>
          <div className="w-full max-w-3xs space-y-1">
            <label htmlFor="company-reliability" className="block text-sm font-medium">
              {t('company.reliabilityFilter')}
            </label>
            <NativeSelect
              id="company-reliability"
              value={minReliability ?? ''}
              onChange={(e) =>
                update({ ...current, minReliability: e.target.value ? Number(e.target.value) : null })
              }
            >
              <option value="">{t('company.anyReliability')}</option>
              {upTo(RATING_MAX.stars).map((n) => (
                <option key={n} value={n}>
                  {t('company.minReliability', { count: n })}
                </option>
              ))}
            </NativeSelect>
          </div>
          <div className="w-full max-w-3xs space-y-1">
            <label htmlFor="company-project-interest" className="block text-sm font-medium">
              {t('company.projectInterestFilter')}
            </label>
            <NativeSelect
              id="company-project-interest"
              value={minProjectInterest ?? ''}
              onChange={(e) =>
                update({ ...current, minProjectInterest: e.target.value ? Number(e.target.value) : null })
              }
            >
              <option value="">{t('company.anyProjectInterest')}</option>
              {upTo(RATING_MAX.thumbs).map((n) => (
                <option key={n} value={n}>
                  {t('company.minProjectInterest', { count: n })}
                </option>
              ))}
            </NativeSelect>
          </div>
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
        {filtered && (
          <Button type="button" variant="outline" onClick={resetFilters}>
            {t('company.resetFilters')}
          </Button>
        )}
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
            aria-labelledby="companies-caption"
            // eslint-disable-next-line jsx-a11y/no-noninteractive-tabindex -- regione scorrevole: deve essere raggiungibile da tastiera
            tabIndex={0}
            className="relative scroll-mt-28 overflow-x-auto rounded-xl border border-border bg-card focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
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
                    {t('company.category')}
                  </th>
                  <th scope="col" className={th}>
                    {t('company.fields.zone')}
                  </th>
                  <th scope="col" className={th}>
                    {t('company.fields.reliability')}
                  </th>
                  <th scope="col" className={th}>
                    {t('company.fields.projectInterest')}
                  </th>
                  <th scope="col" className={th}>
                    {t('company.contacts.column')}
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
                {result.companies.map((c) => (
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
                    <td className={`${td} whitespace-nowrap tabular-nums`}>
                      {isMissing(c, 'VAT_CODE') ? <ToCompleteBadge /> : c.vatCode}
                    </td>
                    <td className={td}>{c.businessCategory?.name ?? '—'}</td>
                    <td className={td}>{c.zone?.name ?? '—'}</td>
                    <td className={td}>
                      <RatingDisplay kind="stars" value={c.reliability} />
                    </td>
                    <td className={td}>
                      <RatingDisplay kind="thumbs" value={c.projectInterest} />
                    </td>
                    <td className={td}>{firstContact(c)}</td>
                    <td className={`${td} whitespace-nowrap`}>{c.phone ?? '—'}</td>
                    <td className={td}>{c.email ?? '—'}</td>
                    <td className={td}>{t(c.active ? 'company.active' : 'company.inactive')}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          {/* fino a 10 aziende basta una pagina, qualunque sia la scelta: niente pulsanti */}
          {total > COMPANY_PAGE_SIZES[0] && (
            <Pagination
              id="companies-page-size"
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
