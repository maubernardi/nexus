import { useEffect, useRef, useState } from 'react';
import { CircleCheck } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { Button } from '@/components/ui/button';
import type { Beneficiary } from '@/config/api/beneficiaryApi';
import { BeneficiaryForm } from '@/organisms/BeneficiaryForm/BeneficiaryForm';
import { BENEFICIARY_PARAM } from '@/pages/TicketNewPage/TicketNewPage';

export const BeneficiaryNewPage = () => {
  const { t } = useTranslation();
  const [created, setCreated] = useState<Beneficiary | null>(null);
  const [formKey, setFormKey] = useState(0);
  const successRef = useRef<HTMLHeadingElement>(null);

  // conferma annunciata: il focus va sul titolo del riquadro di esito
  useEffect(() => {
    if (created) {
      successRef.current?.focus();
    }
  }, [created]);

  return (
    <div className="space-y-6">
      <PageHeading>{t('pages.beneficiaryNew.heading')}</PageHeading>
      {created ? (
        <section
          aria-labelledby="beneficiary-success"
          className="space-y-4 rounded-xl border border-l-4 border-border border-l-primary bg-card p-6"
        >
          <h2
            id="beneficiary-success"
            ref={successRef}
            tabIndex={-1}
            className="flex items-center gap-2 text-xl font-semibold text-primary"
          >
            <CircleCheck aria-hidden="true" className="size-6" />
            {t('beneficiary.success.heading')}
          </h2>
          <p>{t('beneficiary.success.text', { name: `${created.firstName} ${created.lastName}` })}</p>
          <div className="flex flex-wrap gap-3">
            <Button asChild size="lg">
              <Link to={`/segnalazioni/nuova?${BENEFICIARY_PARAM}=${encodeURIComponent(created.id)}`}>
                {t('beneficiary.success.report')}
              </Link>
            </Button>
            <Button
              size="lg"
              variant="outline"
              onClick={() => {
                setCreated(null);
                setFormKey((k) => k + 1);
              }}
            >
              {t('beneficiary.success.another')}
            </Button>
            <Button asChild size="lg" variant="outline">
              <Link to="/">{t('beneficiary.success.home')}</Link>
            </Button>
          </div>
        </section>
      ) : (
        <>
          <p className="text-muted-foreground">{t('pages.beneficiaryNew.intro')}</p>
          <BeneficiaryForm key={formKey} onSuccess={setCreated} />
        </>
      )}
    </div>
  );
};
