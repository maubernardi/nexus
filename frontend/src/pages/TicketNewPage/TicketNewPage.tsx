import { useEffect, useRef, useState } from 'react';
import { CircleCheck } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Link, useSearchParams } from 'react-router';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { Button } from '@/components/ui/button';
import type { Ticket } from '@/config/api/ticketApi';
import { TicketForm } from '@/organisms/TicketForm/TicketForm';

/** Parametro con cui altre pagine preselezionano il beneficiario (es. dopo la registrazione). */
export const BENEFICIARY_PARAM = 'beneficiario';

export const TicketNewPage = () => {
  const { t } = useTranslation();
  const [searchParams] = useSearchParams();
  const [created, setCreated] = useState<Ticket | null>(null);
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
      <PageHeading>{t('pages.ticketNew.heading')}</PageHeading>
      {created ? (
        <section
          aria-labelledby="ticket-success"
          className="space-y-4 rounded-xl border border-l-4 border-border border-l-primary bg-card p-6"
        >
          <h2
            id="ticket-success"
            ref={successRef}
            tabIndex={-1}
            className="flex items-center gap-2 text-xl font-semibold text-primary"
          >
            <CircleCheck aria-hidden="true" className="size-6" />
            {t('ticket.success.heading', { number: created.number })}
          </h2>
          <p>
            {t('ticket.success.text', {
              name: `${created.beneficiary.firstName} ${created.beneficiary.lastName}`,
              status: t(`enums.ticketStatus.${created.status}`),
            })}
          </p>
          <div className="flex flex-wrap gap-3">
            <Button
              size="lg"
              onClick={() => {
                setCreated(null);
                setFormKey((k) => k + 1);
              }}
            >
              {t('ticket.success.another')}
            </Button>
            <Button asChild size="lg" variant="outline">
              <Link to="/">{t('ticket.success.home')}</Link>
            </Button>
          </div>
        </section>
      ) : (
        <>
          <p className="text-muted-foreground">{t('pages.ticketNew.intro')}</p>
          <TicketForm
            key={formKey}
            // dopo "Invia un'altra segnalazione" il beneficiario non va ripreso dall'indirizzo
            initialBeneficiaryId={formKey === 0 ? searchParams.get(BENEFICIARY_PARAM) : null}
            onSuccess={setCreated}
          />
        </>
      )}
    </div>
  );
};
