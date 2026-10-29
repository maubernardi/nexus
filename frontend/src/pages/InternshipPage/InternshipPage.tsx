import { useEffect, useRef, useState } from 'react';
import { Plus } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Link, useParams } from 'react-router';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { Button } from '@/components/ui/button';
import { apiError } from '@/config/api/errors';
import type { Internship } from '@/config/api/internshipApi';
import {
  useAddInternshipAtecoCode,
  useAddInternshipLegalRepresentative,
  useInternship,
  useSaveInternship,
} from '@/config/hooks/useInternship';
import { useTicket } from '@/config/hooks/useTickets';
import { AtecoCodeForm } from '@/organisms/AtecoCodesSection/AtecoCodesSection';
import { InternshipForm } from '@/organisms/InternshipForm/InternshipForm';
import {
  LegalRepresentativeDetails,
  LegalRepresentativeForm,
} from '@/organisms/LegalRepresentativeSection/LegalRepresentativeSection';

type Outcome = { text: string; error?: boolean };

const dateFormat = new Intl.DateTimeFormat('it-IT', { dateStyle: 'long' });

/** Giorno ISO (aaaa-mm-gg) in esteso, senza passare dal fuso orario. */
const formatDay = (iso: string): string => {
  const [y, m, d] = iso.split('-').map(Number);
  return dateFormat.format(new Date(y, m - 1, d));
};

/**
 * Scheda amministrativa del tirocinio (US-1001), solo per il Tutor della segnalazione: in alto i dati dell'azienda
 * (legale rappresentante e ATECO, che si salvano subito), sotto il modulo della scheda. Dopo l'avvio si legge soltanto.
 */
export const InternshipPage = () => {
  const { t } = useTranslation();
  const { id = '' } = useParams();
  const ticket = useTicket(id);
  const internship = useInternship(id);
  const save = useSaveInternship();
  const [outcome, setOutcome] = useState<Outcome | null>(null);
  const outcomeRef = useRef<HTMLDivElement>(null);

  // esito annunciato portandovi il focus
  useEffect(() => {
    if (outcome) outcomeRef.current?.focus();
  }, [outcome]);

  const backLink = (
    <Link
      to={`/segnalazioni/${id}`}
      className="inline-block font-medium text-primary underline underline-offset-4"
    >
      {t('internship.backToTicket')}
    </Link>
  );

  if (internship.isError || ticket.isError) {
    const status = apiError(internship.error ?? ticket.error)?.status;
    return (
      <div className="space-y-4">
        <PageHeading>
          {status === 409
            ? t('internship.notAvailable')
            : status === 404
              ? t('work.notFound')
              : t('internship.loadError')}
        </PageHeading>
        {backLink}
      </div>
    );
  }
  if (!internship.data || !ticket.data) {
    return <p role="status">{t('common.loading')}</p>;
  }
  const data = internship.data;
  const b = ticket.data.beneficiary;

  return (
    <div className="space-y-6">
      <PageHeading>{t('internship.heading', { number: ticket.data.number })}</PageHeading>
      <p className="text-muted-foreground">
        {t('internship.who', {
          beneficiary: `${b.firstName} ${b.lastName}`,
          company: data.companyName ?? t('internship.none'),
        })}
      </p>
      <p>{data.editable ? t('internship.intro') : t('internship.readOnly')}</p>

      {outcome && (
        <div
          ref={outcomeRef}
          tabIndex={-1}
          className={
            outcome.error
              ? 'rounded-md border-2 border-destructive bg-card p-4 font-medium text-destructive'
              : 'rounded-md border-l-4 border-primary bg-card p-4 font-medium'
          }
        >
          {outcome.text}
        </div>
      )}

      <CompanyData ticketId={id} internship={data} onOutcome={setOutcome} />

      <div className="rounded-xl border border-border bg-card p-4">
        {data.editable ? (
          <InternshipForm
            // nuova versione = valori aggiornati nel modulo
            key={data.version ?? 'new'}
            internship={data}
            onSubmit={(payload) => save.mutateAsync({ ticketId: id, payload })}
            onSuccess={() => setOutcome({ text: t('internship.saved') })}
            onConflict={(reason) => setOutcome({ text: t('internship.conflict', { reason }), error: true })}
          />
        ) : (
          <InternshipSummary internship={data} />
        )}
      </div>

      {backLink}
    </div>
  );
};

type CompanyDataProps = {
  ticketId: string;
  internship: Internship;
  onOutcome: (o: Outcome) => void;
};

/**
 * Legale rappresentante e codici ATECO dell'azienda (US-506): il Tutor li aggiunge se mancano, non li corregge (PO,
 * 29/10). Moduli propri, fuori da quello della scheda: i moduli annidati non sono validi.
 */
const CompanyData = ({ ticketId, internship, onOutcome }: CompanyDataProps) => {
  const { t } = useTranslation();
  const addRep = useAddInternshipLegalRepresentative();
  const addAteco = useAddInternshipAtecoCode();
  const [form, setForm] = useState<'legalRep' | 'ateco' | null>(null);
  const formHeadingRef = useRef<HTMLHeadingElement>(null);
  const rep = internship.legalRepresentative;

  useEffect(() => {
    if (form) formHeadingRef.current?.focus();
  }, [form]);

  return (
    <section
      aria-labelledby="internship-company"
      className="space-y-4 rounded-xl border border-border bg-card p-4"
    >
      <h2 id="internship-company" className="text-lg font-semibold text-primary">
        {t('internship.sections.company')}
      </h2>
      <p className="text-muted-foreground">{t('internship.companyIntro')}</p>

      <h3 className="font-semibold">{t('legalRep.heading')}</h3>
      {rep ? <LegalRepresentativeDetails rep={rep} /> : <p>{t('internship.legalRepNone')}</p>}
      {!rep && internship.editable && form !== 'legalRep' && (
        <Button size="lg" onClick={() => setForm('legalRep')}>
          {t('internship.legalRepAdd')}
        </Button>
      )}
      {form === 'legalRep' && (
        <div className="space-y-4 rounded-xl border border-border p-4">
          <h4 ref={formHeadingRef} tabIndex={-1} className="font-semibold">
            {t('internship.legalRepAdd')}
          </h4>
          <LegalRepresentativeForm
            current={null}
            submit={(payload) => addRep.mutateAsync({ ticketId, payload })}
            onSaved={() => {
              setForm(null);
              onOutcome({ text: t('internship.legalRepAdded') });
            }}
            onConflict={(reason) => {
              setForm(null);
              onOutcome({ text: t('legalRep.conflict', { reason }), error: true });
            }}
            onCancel={() => setForm(null)}
          />
        </div>
      )}

      <h3 className="font-semibold">{t('ateco.heading')}</h3>
      {internship.atecoCodes.length === 0 ? (
        <p>{t('internship.atecoNone')}</p>
      ) : (
        <ul aria-label={t('ateco.listLabel')} className="list-inside list-disc space-y-1">
          {internship.atecoCodes.map((item) => (
            <li key={item.id}>
              <span className="font-mono font-semibold">{item.code}</span>
              {item.description && <> – {item.description}</>}
            </li>
          ))}
        </ul>
      )}
      {internship.editable && form !== 'ateco' && (
        <Button size="lg" variant="outline" onClick={() => setForm('ateco')}>
          <Plus aria-hidden="true" />
          {t('internship.atecoAdd')}
        </Button>
      )}
      {form === 'ateco' && (
        <div className="space-y-4 rounded-xl border border-border p-4">
          <h4 ref={formHeadingRef} tabIndex={-1} className="font-semibold">
            {t('internship.atecoAdd')}
          </h4>
          <AtecoCodeForm
            submit={(v) => addAteco.mutateAsync({ ticketId, ...v })}
            onAdded={(item) => {
              setForm(null);
              onOutcome({ text: t('internship.atecoAdded', { code: item.code }) });
            }}
            onCancel={() => setForm(null)}
          />
        </div>
      )}
    </section>
  );
};

/** Scheda in sola lettura, dopo l'avvio del tirocinio. */
const InternshipSummary = ({ internship: i }: { internship: Internship }) => {
  const { t } = useTranslation();
  const value = (v?: string | null): string => v || t('internship.none');
  const day = (v?: string | null): string => (v ? formatDay(v) : t('internship.none'));
  const ateco = i.atecoCodes.find((a) => a.id === i.atecoCodeId);
  return (
    <section aria-labelledby="internship-summary" className="space-y-3">
      <h2 id="internship-summary" className="text-lg font-semibold text-primary">
        {t('internship.sections.internship')}
      </h2>
      <dl className="grid gap-x-6 gap-y-2 sm:grid-cols-[max-content_1fr]">
        <dt className="font-medium">{t('internship.fields.internshipType')}</dt>
        <dd>{value(i.internshipType?.name)}</dd>
        <dt className="font-medium">{t('internship.fields.startDate')}</dt>
        <dd>{day(i.startDate)}</dd>
        <dt className="font-medium">{t('internship.fields.expectedEndDate')}</dt>
        <dd>{day(i.expectedEndDate)}</dd>
        <dt className="font-medium">{t('internship.fields.weeklyHours')}</dt>
        <dd>{typeof i.weeklyHours === 'number' ? i.weeklyHours : t('internship.none')}</dd>
        <dt className="font-medium">{t('internship.fields.site')}</dt>
        <dd>{value(i.site)}</dd>
        <dt className="font-medium">{t('internship.fields.atecoCode')}</dt>
        <dd>{ateco ? ateco.code : t('internship.none')}</dd>
        <dt className="font-medium">{t('internship.sections.companyTutors')}</dt>
        <dd>
          {i.companyTutors.length === 0
            ? t('internship.none')
            : i.companyTutors.map((c) => [c.fullName, c.contact].filter(Boolean).join(', ')).join('; ')}
        </dd>
        <dt className="font-medium">{t('internship.sections.suspensions')}</dt>
        <dd>
          {i.suspensions.length === 0
            ? t('internship.none')
            : i.suspensions
                .map((s) => t('internship.period', { from: formatDay(s.from), to: formatDay(s.to) }))
                .join('; ')}
        </dd>
        <dt className="font-medium">{t('internship.fields.fiscalCode')}</dt>
        <dd className="font-mono">{value(i.fiscalCode)}</dd>
        <dt className="font-medium">{t('internship.fields.birthDate')}</dt>
        <dd>{day(i.birthDate)}</dd>
        <dt className="font-medium">{t('internship.fields.birthPlace')}</dt>
        <dd>{value(i.birthPlace)}</dd>
        <dt className="font-medium">{t('internship.fields.residence')}</dt>
        <dd>{value(i.residence)}</dd>
        <dt className="font-medium">{t('internship.fields.notes')}</dt>
        <dd className="whitespace-pre-line">{value(i.notes)}</dd>
      </dl>
    </section>
  );
};
