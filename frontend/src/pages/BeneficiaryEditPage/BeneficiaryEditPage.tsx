import { useTranslation } from 'react-i18next';
import { Link, useNavigate, useParams } from 'react-router';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { Button } from '@/components/ui/button';
import { apiError } from '@/config/api/errors';
import { useBeneficiary, useUpdateBeneficiary } from '@/config/hooks/useBeneficiaries';
import { BeneficiaryForm } from '@/organisms/BeneficiaryForm/BeneficiaryForm';

/** Modifica del beneficiario (US-203), riservata al Tutor proprietario. */
export const BeneficiaryEditPage = () => {
  const { t } = useTranslation();
  const { id = '' } = useParams();
  const navigate = useNavigate();
  const beneficiary = useBeneficiary(id);
  const updateBeneficiary = useUpdateBeneficiary();

  if (beneficiary.isError) {
    const notFound = apiError(beneficiary.error)?.status === 404;
    return (
      <div className="space-y-4">
        <PageHeading>{t(notFound ? 'beneficiary.notFound' : 'beneficiary.loadError')}</PageHeading>
        <Button asChild size="lg" variant="outline">
          <Link to="/beneficiari">{t('beneficiary.backToList')}</Link>
        </Button>
      </div>
    );
  }
  if (!beneficiary.data) {
    return <p role="status">{t('common.loading')}</p>;
  }
  const b = beneficiary.data;
  const backToDetail = (
    <Link
      to={`/beneficiari/${b.id}`}
      className="inline-block font-medium text-primary underline underline-offset-4"
    >
      {t('beneficiary.backToDetail')}
    </Link>
  );

  return (
    <div className="space-y-6">
      <PageHeading>
        {t('pages.beneficiaryEdit.heading', { name: `${b.firstName} ${b.lastName}` })}
      </PageHeading>
      {b.editable ? (
        <BeneficiaryForm
          beneficiary={b}
          submitLabel={t('beneficiary.save')}
          submittingLabel={t('beneficiary.saving')}
          onSubmit={(payload) => updateBeneficiary.mutateAsync({ id: b.id, payload })}
          onSuccess={(updated) => void navigate(`/beneficiari/${updated.id}`, { state: { saved: true } })}
        />
      ) : (
        <p className="rounded-md border-2 border-destructive bg-card p-4 font-medium text-destructive">
          {t('beneficiary.notEditable')}
        </p>
      )}
      {backToDetail}
    </div>
  );
};
