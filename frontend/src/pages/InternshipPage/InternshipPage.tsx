import { useEffect, useRef, useState } from 'react';
import { FileDown, Plus } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Link, useParams } from 'react-router';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { Button } from '@/components/ui/button';
import { apiError } from '@/config/api/errors';
import type { Internship, InternshipDocumentKind, StartRequirement } from '@/config/api/internshipApi';
import type { TicketDetail } from '@/config/api/ticketApi';
import {
  useAddInternshipAtecoCode,
  useAddInternshipLegalRepresentative,
  useDownloadInternshipDocument,
  useInternship,
  useSaveInternship,
} from '@/config/hooks/useInternship';
import { useStartInternship, useTicket } from '@/config/hooks/useTickets';
import { AtecoCodeForm } from '@/organisms/AtecoCodesSection/AtecoCodesSection';
import { InternshipForm } from '@/organisms/InternshipForm/InternshipForm';
import { saveBlob } from '@/lib/download';
import {
  LegalRepresentativeDetails,
  LegalRepresentativeForm,
} from '@/organisms/LegalRepresentativeSection/LegalRepresentativeSection';

type Outcome = { text: string; error?: boolean };

/** Campo o sezione della pagina a cui porta ciascun dato mancante per l'avvio (US-1003). */
const START_TARGETS: Record<StartRequirement, string> = {
  internshipType: 'internship-internshipTypeId',
  startDate: 'internship-startDate',
  expectedEndDate: 'internship-expectedEndDate',
  site: 'internship-site',
  atecoCode: 'internship-atecoCodeId',
  companyTutors: 'internship-section-tutors',
  schedule: 'internship-days',
  fiscalCode: 'internship-fiscalCode',
  birthDate: 'internship-birthDate',
  birthPlace: 'internship-birthPlace',
  residence: 'internship-residence',
  legalRepresentative: 'internship-company',
  legalRepresentativeData: 'internship-company',
};

/**
 * Scheda amministrativa del tirocinio (US-1001), solo per il Tutor della segnalazione: in alto i dati dell'azienda
 * (legale rappresentante e ATECO, che si salvano subito), sotto il modulo della scheda. Il Tutor la corregge in
 * qualunque momento, anche a tirocinio avviato (PO, 29/10).
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
      <p>{t('internship.intro')}</p>
      {(ticket.data.status === 'IN_TIROCINIO' || ticket.data.status === 'FORM_RESTITUZIONE') && (
        <p className="rounded-md border-l-4 border-gold bg-card p-4">{t('internship.start.started')}</p>
      )}

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

      {ticket.data.status === 'APPUNTAMENTO' && (
        <StartSection ticket={ticket.data} internship={data} onOutcome={setOutcome} />
      )}

      <DocumentsSection ticket={ticket.data} internship={data} />

      <CompanyData ticketId={id} internship={data} onOutcome={setOutcome} />

      <div className="rounded-xl border border-border bg-card p-4">
        <InternshipForm
          // nuova versione = valori aggiornati nel modulo
          key={data.version ?? 'new'}
          internship={data}
          onSubmit={(payload) => save.mutateAsync({ ticketId: id, payload })}
          onSuccess={() => setOutcome({ text: t('internship.saved') })}
          onConflict={(reason) => setOutcome({ text: t('internship.conflict', { reason }), error: true })}
        />
      </div>

      {backLink}
    </div>
  );
};

type StartSectionProps = {
  ticket: TicketDetail;
  internship: Internship;
  onOutcome: (o: Outcome) => void;
};

/**
 * Avvio del tirocinio (US-1003): con la scheda salvata completa il Tutor lo conferma; altrimenti l'elenco di ciò che
 * manca porta a ogni campo. La conferma prende il focus e, se si annulla, il focus torna al pulsante.
 */
const StartSection = ({ ticket: d, internship, onOutcome }: StartSectionProps) => {
  const { t } = useTranslation();
  const start = useStartInternship();
  const [confirming, setConfirming] = useState(false);
  const confirmRef = useRef<HTMLHeadingElement>(null);
  const buttonRef = useRef<HTMLButtonElement>(null);
  const wasConfirming = useRef(false);
  const missing = internship.missingForStart;

  useEffect(() => {
    if (confirming) confirmRef.current?.focus();
    else if (wasConfirming.current) buttonRef.current?.focus();
    wasConfirming.current = confirming;
  }, [confirming]);

  // il campo del codice ATECO c'è solo se l'azienda ha dei codici: altrimenti si va ai dati dell'azienda
  const focusTarget = (event: React.MouseEvent<HTMLAnchorElement>, code: StartRequirement): void => {
    const target =
      document.getElementById(START_TARGETS[code]) ?? document.getElementById('internship-company');
    if (target) {
      event.preventDefault();
      target.focus();
    }
  };

  // mutateAsync: l'esito arriva anche se, avviato il tirocinio, la sezione sparisce
  const onConfirm = async (): Promise<void> => {
    try {
      await start.mutateAsync({ id: d.id, version: d.version });
      onOutcome({ text: t('internship.start.done') });
    } catch (error) {
      const body = apiError(error);
      onOutcome({
        text:
          body?.status === 409 ? t('internship.start.refused', { reason: body.message }) : t('work.error'),
        error: true,
      });
      setConfirming(false);
    }
  };

  return (
    <section
      aria-labelledby="internship-start"
      className="space-y-3 rounded-xl border border-border bg-card p-4"
    >
      <h2 id="internship-start" className="text-lg font-semibold text-primary">
        {t('internship.start.heading')}
      </h2>
      {missing.length > 0 ? (
        <>
          <p>{t('internship.start.missingIntro')}</p>
          <ul className="list-disc space-y-1 pl-5">
            {missing.map((code) => (
              <li key={code}>
                <a
                  href={`#${START_TARGETS[code]}`}
                  onClick={(e) => focusTarget(e, code)}
                  className="text-primary underline underline-offset-4"
                >
                  {t(`internship.start.missing.${code}`)}
                </a>
              </li>
            ))}
          </ul>
        </>
      ) : !confirming ? (
        <>
          <p>{t('internship.start.ready')}</p>
          <Button ref={buttonRef} size="lg" onClick={() => setConfirming(true)}>
            {t('internship.start.action')}
          </Button>
        </>
      ) : (
        <div className="space-y-3 rounded-xl border-2 border-primary p-4">
          <h3 ref={confirmRef} tabIndex={-1} className="font-semibold">
            {t('internship.start.confirmHeading', {
              beneficiary: `${d.beneficiary.firstName} ${d.beneficiary.lastName}`,
              company: internship.companyName ?? t('internship.none'),
            })}
          </h3>
          <p>{t('internship.start.confirmText')}</p>
          <div className="flex flex-wrap gap-3">
            <Button size="lg" onClick={() => void onConfirm()} disabled={start.isPending}>
              {t('internship.start.confirm')}
            </Button>
            <Button
              size="lg"
              variant="outline"
              onClick={() => setConfirming(false)}
              disabled={start.isPending}
            >
              {t('internship.start.cancel')}
            </Button>
          </div>
        </div>
      )}
    </section>
  );
};

/** Documenti del tirocinio: tipo per l'API, prefisso del nome del file (lo stesso del server) e testi. */
const DOCUMENTS = [
  { kind: 'convenzione', filePrefix: 'Convenzione', key: 'convenzione' },
  { kind: 'progetto-formativo', filePrefix: 'Progetto-formativo', key: 'progettoFormativo' },
] as const satisfies readonly { kind: InternshipDocumentKind; filePrefix: string; key: string }[];

/**
 * Documenti del tirocinio (US-1004, US-1005): compilati dal server con la scheda salvata, si scaricano come .docx.
 * L'esito si annuncia lì accanto (stato), il focus resta sul pulsante.
 */
const DocumentsSection = ({ ticket: d, internship }: { ticket: TicketDetail; internship: Internship }) => {
  const { t } = useTranslation();
  const download = useDownloadInternshipDocument();
  const [status, setStatus] = useState<{ text: string; error?: boolean } | null>(null);

  const onDownload = async (item: (typeof DOCUMENTS)[number]): Promise<void> => {
    const filename = `${item.filePrefix}-segnalazione-${d.number}.docx`;
    setStatus({ text: t('internship.documents.preparing') });
    try {
      saveBlob(await download.mutateAsync({ ticketId: d.id, kind: item.kind }), filename);
      setStatus({ text: t(`internship.documents.downloaded.${item.key}`, { filename }) });
    } catch {
      setStatus({ text: t('internship.documents.error'), error: true });
    }
  };

  return (
    <section
      aria-labelledby="internship-documents"
      className="space-y-3 rounded-xl border border-border bg-card p-4"
    >
      <h2 id="internship-documents" className="text-lg font-semibold text-primary">
        {t('internship.documents.heading')}
      </h2>
      <p className="text-muted-foreground">{t('internship.documents.intro')}</p>
      {internship.missingForStart.length > 0 && (
        <p className="rounded-md bg-warning p-3 text-warning-foreground">
          {t('internship.documents.incomplete')}
        </p>
      )}
      <div className="flex flex-wrap gap-3">
        {DOCUMENTS.map((item) => (
          <Button
            key={item.kind}
            size="lg"
            variant="outline"
            onClick={() => void onDownload(item)}
            disabled={download.isPending}
          >
            <FileDown aria-hidden="true" />
            {t(`internship.documents.download.${item.key}`)}
          </Button>
        ))}
      </div>
      <p role="status" className={status?.error ? 'font-medium text-destructive' : 'text-muted-foreground'}>
        {status?.text}
      </p>
    </section>
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
      {/* focalizzabile: l'elenco di ciò che manca per l'avvio porta qui */}
      <h2 id="internship-company" tabIndex={-1} className="text-lg font-semibold text-primary">
        {t('internship.sections.company')}
      </h2>
      <p className="text-muted-foreground">{t('internship.companyIntro')}</p>

      <h3 className="font-semibold">{t('legalRep.heading')}</h3>
      {rep ? <LegalRepresentativeDetails rep={rep} /> : <p>{t('internship.legalRepNone')}</p>}
      {!rep && form !== 'legalRep' && (
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
      {form !== 'ateco' && (
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
