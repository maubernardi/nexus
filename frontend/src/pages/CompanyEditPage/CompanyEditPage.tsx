import { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useLocation, useParams } from 'react-router';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { Button } from '@/components/ui/button';
import { apiError } from '@/config/api/errors';
import { useCompany, useSetCompanyActive, useUpdateCompany } from '@/config/hooks/useCompanies';
import { CompanyForm } from '@/organisms/CompanyForm/CompanyForm';

type Outcome = 'created' | 'saved' | 'activated' | 'deactivated' | 'conflict' | 'error';

/** Scheda dell'azienda (US-501): modifica dei dati e attivazione. Le mansioni arrivano con US-502. */
export const CompanyEditPage = () => {
  const { t } = useTranslation();
  const { id = '' } = useParams();
  const location = useLocation();
  const company = useCompany(id);
  const updateCompany = useUpdateCompany();
  const setActive = useSetCompanyActive();
  const initial = (location.state as { outcome?: Outcome } | null)?.outcome ?? null;
  const [outcome, setOutcome] = useState<Outcome | null>(initial);
  const outcomeRef = useRef<HTMLDivElement>(null);

  // esito annunciato portandovi il focus (anche all'arrivo dalla creazione)
  useEffect(() => {
    if (outcome && company.data) outcomeRef.current?.focus();
  }, [outcome, company.data]);

  if (company.isError) {
    const notFound = apiError(company.error)?.status === 404;
    return (
      <div className="space-y-4">
        <PageHeading>{t(notFound ? 'company.notFound' : 'company.loadError')}</PageHeading>
        <Button asChild size="lg" variant="outline">
          <Link to="/aziende">{t('company.backToList')}</Link>
        </Button>
      </div>
    );
  }
  if (!company.data) {
    return <p role="status">{t('queue.loading')}</p>;
  }
  const c = company.data;

  const toggleActive = (): void => {
    setOutcome(null);
    setActive.mutate(
      { id: c.id, version: c.version, active: !c.active },
      {
        onSuccess: (updated) => setOutcome(updated.active ? 'activated' : 'deactivated'),
        onError: (error) => setOutcome(apiError(error)?.status === 409 ? 'conflict' : 'error'),
      },
    );
  };

  const isError = outcome === 'conflict' || outcome === 'error';

  return (
    <div className="space-y-6">
      <PageHeading>{c.name}</PageHeading>
      <p className="text-muted-foreground">
        {t('company.vatLine', { vat: c.vatCode })} · {t(c.active ? 'company.active' : 'company.inactive')}
      </p>

      {outcome && (
        <div
          ref={outcomeRef}
          tabIndex={-1}
          className={
            isError
              ? 'rounded-md border-2 border-destructive bg-card p-4 font-medium text-destructive'
              : 'rounded-md border-l-4 border-primary bg-card p-4 font-medium'
          }
        >
          {t(`company.outcome.${outcome}`)}
        </div>
      )}

      <section aria-labelledby="company-data" className="space-y-4">
        <h2 id="company-data" className="text-lg font-semibold text-primary">
          {t('company.sections.data')}
        </h2>
        <CompanyForm
          // nuova versione dopo il salvataggio: il modulo riparte dai dati aggiornati
          key={c.version}
          company={c}
          submitLabel={t('company.save')}
          onSubmit={(payload) => updateCompany.mutateAsync({ id: c.id, payload })}
          onSuccess={() => setOutcome('saved')}
        />
      </section>

      <section
        aria-labelledby="company-status"
        className="space-y-3 rounded-xl border border-border bg-card p-4"
      >
        <h2 id="company-status" className="text-lg font-semibold text-primary">
          {t('company.sections.status')}
        </h2>
        <p>{t(c.active ? 'company.deactivateHelp' : 'company.activateHelp')}</p>
        <Button variant="outline" size="lg" onClick={toggleActive} disabled={setActive.isPending}>
          {t(c.active ? 'company.deactivate' : 'company.activate')}
        </Button>
      </section>

      <Link to="/aziende" className="inline-block font-medium text-primary underline underline-offset-4">
        {t('company.backToList')}
      </Link>
    </div>
  );
};
