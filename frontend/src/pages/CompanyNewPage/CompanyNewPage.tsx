import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { useCreateCompany } from '@/config/hooks/useCompanies';
import { CompanyForm } from '@/organisms/CompanyForm/CompanyForm';

export const CompanyNewPage = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const createCompany = useCreateCompany();

  return (
    <div className="space-y-6">
      <PageHeading>{t('pages.companyNew.heading')}</PageHeading>
      <p className="text-muted-foreground">{t('pages.companyNew.intro')}</p>
      <CompanyForm
        submitLabel={t('company.create')}
        onSubmit={(payload) => createCompany.mutateAsync(payload)}
        // la scheda dell'azienda annuncia l'esito
        onSuccess={(company) => void navigate(`/aziende/${company.id}`, { state: { outcome: 'created' } })}
      />
    </div>
  );
};
