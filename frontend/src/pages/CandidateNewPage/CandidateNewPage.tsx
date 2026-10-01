import { useEffect, useRef, useState } from 'react';
import { CircleCheck } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { Button } from '@/components/ui/button';
import type { Candidate } from '@/config/api/candidateApi';
import { CandidateForm } from '@/organisms/CandidateForm/CandidateForm';

export const CandidateNewPage = () => {
  const { t } = useTranslation();
  const [created, setCreated] = useState<Candidate | null>(null);
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
      <PageHeading>{t('pages.candidateNew.heading')}</PageHeading>
      {created ? (
        <section
          aria-labelledby="candidate-success"
          className="space-y-4 rounded-xl border border-l-4 border-border border-l-primary bg-card p-6"
        >
          <h2
            id="candidate-success"
            ref={successRef}
            tabIndex={-1}
            className="flex items-center gap-2 text-xl font-semibold text-primary"
          >
            <CircleCheck aria-hidden="true" className="size-6" />
            {t('candidate.success.heading')}
          </h2>
          <p>{t('candidate.success.text', { name: `${created.firstName} ${created.lastName}` })}</p>
          <div className="flex flex-wrap gap-3">
            <Button
              size="lg"
              onClick={() => {
                setCreated(null);
                setFormKey((k) => k + 1);
              }}
            >
              {t('candidate.success.another')}
            </Button>
            <Button asChild size="lg" variant="outline">
              <Link to="/">{t('candidate.success.home')}</Link>
            </Button>
          </div>
        </section>
      ) : (
        <>
          <p className="text-muted-foreground">{t('pages.candidateNew.intro')}</p>
          <CandidateForm key={formKey} onSuccess={setCreated} />
        </>
      )}
    </div>
  );
};
