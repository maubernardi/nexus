import { useEffect, useRef, type ReactNode } from 'react';
import { Pencil } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Link, useLocation, useParams } from 'react-router';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { Button } from '@/components/ui/button';
import { apiError } from '@/config/api/errors';
import { useBeneficiary } from '@/config/hooks/useBeneficiaries';
import { isoName } from '@/lib/isoNames';

type Row = { label: string; value: ReactNode };

/** Stato della navigazione verso la scheda: arrivando da una segnalazione, il link di ritorno riporta lì. */
export type BeneficiaryLinkState = { fromTicket?: { id: string; number: number } };

const Section = ({ id, title, rows }: { id: string; title: string; rows: Row[] }) => (
  <section aria-labelledby={id} className="space-y-3 rounded-xl border border-border bg-card p-4">
    <h2 id={id} className="text-lg font-semibold text-primary">
      {title}
    </h2>
    <dl className="grid gap-x-6 gap-y-3 sm:grid-cols-[18rem_1fr]">
      {rows.map(({ label, value }) => (
        <div key={label} className="contents">
          <dt className="text-sm font-medium text-muted-foreground">{label}</dt>
          <dd className="break-words whitespace-pre-line">{value}</dd>
        </div>
      ))}
    </dl>
  </section>
);

/**
 * Scheda del beneficiario (US-202): la vedono il Tutor proprietario, i Tutor dei progetti in cui è segnalato, il Call
 * Center e l'ADMIN (US-204); solo il proprietario può modificarla (US-203).
 */
export const BeneficiaryPage = () => {
  const { t } = useTranslation();
  const { id = '' } = useParams();
  const location = useLocation();
  const beneficiary = useBeneficiary(id);
  const state = location.state as ({ saved?: boolean } & BeneficiaryLinkState) | null;
  const saved = state?.saved === true;
  const fromTicket = state?.fromTicket;
  const outcomeRef = useRef<HTMLParagraphElement>(null);

  // esito del salvataggio annunciato portandovi il focus
  useEffect(() => {
    if (saved && beneficiary.data) outcomeRef.current?.focus();
  }, [saved, beneficiary.data]);

  if (beneficiary.isError) {
    const notFound = apiError(beneficiary.error)?.status === 404;
    return (
      <div className="space-y-4">
        <PageHeading>{t(notFound ? 'beneficiary.notFound' : 'beneficiary.loadError')}</PageHeading>
        <Button asChild size="lg" variant="outline">
          <Link to="/">{t('beneficiary.backToHome')}</Link>
        </Button>
      </div>
    );
  }
  if (!beneficiary.data) {
    return <p role="status">{t('common.loading')}</p>;
  }
  const b = beneficiary.data;
  const yesNo = (value: boolean): string => t(value ? 'beneficiary.yes' : 'beneficiary.no');
  const notSpecified = t('beneficiary.notSpecified');

  return (
    <div className="space-y-6">
      <PageHeading>
        {b.firstName} {b.lastName}
      </PageHeading>
      <div className="flex flex-wrap items-center justify-between gap-3">
        <p className="text-muted-foreground">{t('beneficiary.owner', { name: b.ownerTutorName })}</p>
        {b.editable && (
          <Button asChild size="lg">
            <Link to={`/beneficiari/${b.id}/modifica`}>
              <Pencil aria-hidden="true" />
              {t('beneficiary.edit')}
            </Link>
          </Button>
        )}
      </div>

      {saved && (
        <p
          ref={outcomeRef}
          tabIndex={-1}
          className="rounded-md border-l-4 border-primary bg-card p-4 font-medium"
        >
          {t('beneficiary.saved')}
        </p>
      )}

      <Section
        id="beneficiary-personal"
        title={t('beneficiary.sections.personal')}
        rows={[
          { label: t('beneficiary.fields.birthYear'), value: b.birthYear },
          { label: t('beneficiary.fields.gender'), value: t(`enums.gender.${b.gender}`) },
          {
            label: t('beneficiary.fields.nationality'),
            value: b.nationality ? isoName(b.nationality, 'region') : notSpecified,
          },
          {
            label: t('beneficiary.fields.citizenship'),
            value: b.citizenship ? isoName(b.citizenship, 'region') : notSpecified,
          },
          { label: t('beneficiary.fields.residenceZone'), value: b.residenceZone.name },
        ]}
      />
      <Section
        id="beneficiary-mobility"
        title={t('beneficiary.sections.mobility')}
        rows={[
          {
            label: t('beneficiary.fields.licenseTypes'),
            value: b.licenseTypes.length > 0 ? b.licenseTypes.join(', ') : t('beneficiary.none'),
          },
          { label: t('beneficiary.fields.hasVehicle'), value: yesNo(b.hasVehicle) },
          {
            label: t('beneficiary.fields.transportMode'),
            value: b.transportMode ? t(`enums.transportMode.${b.transportMode}`) : notSpecified,
          },
        ]}
      />
      <Section
        id="beneficiary-education"
        title={t('beneficiary.sections.education')}
        rows={[
          {
            label: t('beneficiary.fields.educationLevel'),
            value: b.educationLevel ? t(`enums.educationLevel.${b.educationLevel}`) : notSpecified,
          },
          {
            label: t('beneficiary.fields.languages'),
            value:
              b.languages.length > 0
                ? b.languages
                    .map((l) => `${isoName(l.language, 'language')} (${t(`enums.languageLevel.${l.level}`)})`)
                    .join(', ')
                : t('beneficiary.none'),
          },
        ]}
      />
      <Section
        id="beneficiary-confidential"
        title={t('beneficiary.sections.confidential')}
        rows={[
          { label: t('beneficiary.fields.hasLaw68'), value: yesNo(b.hasLaw68) },
          { label: t('beneficiary.fields.constraints'), value: b.constraints ?? t('beneficiary.none') },
        ]}
      />

      {fromTicket ? (
        <Link
          to={`/segnalazioni/${fromTicket.id}`}
          className="inline-block font-medium text-primary underline underline-offset-4"
        >
          {t('beneficiary.backToTicket', { number: fromTicket.number })}
        </Link>
      ) : (
        <Link
          to={b.editable ? '/beneficiari' : '/'}
          className="inline-block font-medium text-primary underline underline-offset-4"
        >
          {t(b.editable ? 'beneficiary.backToList' : 'beneficiary.backToHome')}
        </Link>
      )}
    </div>
  );
};
