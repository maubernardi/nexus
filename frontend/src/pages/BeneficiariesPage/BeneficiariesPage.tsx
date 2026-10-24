import { useMemo, type FormEvent } from 'react';
import { Plus } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Link, useSearchParams } from 'react-router';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { useMyBeneficiaries } from '@/config/hooks/useBeneficiaries';

// parametro dell'indirizzo: Indietro e link condivisi mantengono la ricerca
const SEARCH_PARAM = 'q';

const normalize = (value: string): string =>
  value
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '')
    .toLocaleLowerCase('it');

/** I miei beneficiari (US-202): elenco del Tutor proprietario con ricerca per nome. */
export const BeneficiariesPage = () => {
  const { t } = useTranslation();
  const [searchParams, setSearchParams] = useSearchParams();
  const search = searchParams.get(SEARCH_PARAM) ?? '';
  const beneficiaries = useMyBeneficiaries();

  // pochi beneficiari per Tutor: la ricerca avviene nel browser, su nome e cognome in qualunque ordine
  const shown = useMemo(() => {
    const words = normalize(search).split(/\s+/).filter(Boolean);
    return (beneficiaries.data ?? []).filter((b) => {
      const name = normalize(`${b.firstName} ${b.lastName}`);
      return words.every((w) => name.includes(w));
    });
  }, [beneficiaries.data, search]);

  const onSearch = (event: FormEvent<HTMLFormElement>): void => {
    event.preventDefault();
    const value = new FormData(event.currentTarget).get(SEARCH_PARAM);
    const q = typeof value === 'string' ? value.trim() : '';
    setSearchParams(q ? { [SEARCH_PARAM]: q } : {});
  };

  const th = 'px-3 py-2 text-left text-sm font-semibold whitespace-nowrap';
  const td = 'px-3 py-2 align-top';

  return (
    <div className="space-y-6">
      <PageHeading>{t('pages.beneficiaries.heading')}</PageHeading>
      <div className="flex flex-wrap items-center justify-between gap-3">
        <p className="text-muted-foreground">{t('pages.beneficiaries.intro')}</p>
        <Button asChild size="lg">
          <Link to="/beneficiari/nuovo">
            <Plus aria-hidden="true" />
            {t('beneficiary.new')}
          </Link>
        </Button>
      </div>

      <form
        role="search"
        onSubmit={onSearch}
        className="space-y-3 rounded-xl border border-border bg-card p-4"
      >
        <label htmlFor="beneficiary-search" className="block text-sm font-medium">
          {t('beneficiary.search')}
        </label>
        <div className="flex flex-wrap gap-3">
          {/* key: il campo si riallinea all'indirizzo (es. tasto Indietro) */}
          <Input
            key={search}
            id="beneficiary-search"
            name={SEARCH_PARAM}
            type="search"
            defaultValue={search}
            className="max-w-md flex-1"
          />
          <Button type="submit" size="lg" variant="outline">
            {t('beneficiary.searchSubmit')}
          </Button>
        </div>
      </form>

      <p role="status" className="font-medium">
        {beneficiaries.data ? t('beneficiary.count', { count: shown.length }) : ''}
      </p>

      {beneficiaries.isError && !beneficiaries.data ? (
        <div role="alert" className="space-y-3 rounded-md border-2 border-destructive bg-card p-4">
          <p className="font-medium text-destructive">{t('common.loadError')}</p>
          <Button variant="outline" onClick={() => void beneficiaries.refetch()}>
            {t('common.retry')}
          </Button>
        </div>
      ) : !beneficiaries.data ? (
        <p>{t('common.loading')}</p>
      ) : shown.length === 0 ? (
        <p className="rounded-xl border border-border bg-card p-6">
          {t(beneficiaries.data.length === 0 ? 'beneficiary.empty' : 'beneficiary.noMatch')}
        </p>
      ) : (
        <div
          role="region"
          aria-labelledby="beneficiaries-caption"
          // eslint-disable-next-line jsx-a11y/no-noninteractive-tabindex -- regione scorrevole: deve essere raggiungibile da tastiera
          tabIndex={0}
          className="relative overflow-x-auto rounded-xl border border-border bg-card focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
        >
          <table className="w-full border-collapse text-sm max-lg:w-max max-lg:whitespace-nowrap">
            <caption id="beneficiaries-caption" className="sr-only">
              {t('beneficiary.caption')}
            </caption>
            <thead className="border-b border-border bg-muted">
              <tr>
                <th scope="col" className={th}>
                  {t('beneficiary.name')}
                </th>
                <th scope="col" className={th}>
                  {t('beneficiary.fields.birthYear')}
                </th>
                <th scope="col" className={th}>
                  {t('beneficiary.fields.residenceZone')}
                </th>
                <th scope="col" className={th}>
                  {t('beneficiary.openTicket')}
                </th>
              </tr>
            </thead>
            <tbody>
              {shown.map((b) => (
                <tr key={b.id} className="border-b border-border last:border-b-0">
                  <th scope="row" className={`${td} text-left font-semibold`}>
                    <Link to={`/beneficiari/${b.id}`} className="text-primary underline underline-offset-4">
                      {b.lastName} {b.firstName}
                    </Link>
                  </th>
                  <td className={`${td} tabular-nums`}>{b.birthYear}</td>
                  <td className={td}>{b.residenceZoneName}</td>
                  <td className={`${td} tabular-nums`}>
                    {b.openTicketNumber
                      ? t('beneficiary.ticketNumber', { number: b.openTicketNumber })
                      : t('beneficiary.noTicket')}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};
