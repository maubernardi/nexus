import { useRef, useState } from 'react';
import { FileDown } from 'lucide-react';
import { useTranslation } from 'react-i18next';

import { Button } from '@/components/ui/button';
import { NativeSelect } from '@/components/ui/native-select';
import type { DocumentTarget, Internship, InternshipDocumentKind } from '@/config/api/internshipApi';
import type { TicketDetail } from '@/config/api/ticketApi';
import { useDocumentTemplateChoices, useDownloadInternshipDocument } from '@/config/hooks/useInternship';
import { saveBlob } from '@/lib/download';

/** I documenti: tipo per l'API, prefisso del nome del file (lo stesso del server) e chiave dei testi. */
const KINDS = [
  { kind: 'convenzione', filePrefix: 'Convenzione', key: 'convenzione' },
  { kind: 'progetto-formativo', filePrefix: 'Progetto-formativo', key: 'progettoFormativo' },
  { kind: 'relazione-finale', filePrefix: 'Relazione-finale', key: 'relazioneFinale' },
] as const satisfies readonly { kind: InternshipDocumentKind; filePrefix: string; key: string }[];

type Kind = (typeof KINDS)[number];

/** Un pulsante: la convenzione o il progetto formativo che scarica, con il suo testo. */
type DocumentItem = { target: DocumentTarget; label: string };

/** Il nome del file, come lo scrive il server: «Progetto-formativo-proroga-1-progetto-2-segnalazione-12.docx». */
const filename = (kind: Kind, ticketNumber: number, { extension, project }: DocumentTarget): string => {
  let name: string = kind.filePrefix;
  if (extension !== undefined) name += `-proroga-${extension}${project !== undefined ? '-progetto' : ''}`;
  if (project !== undefined) name += `-${project}`;
  return `${name}-segnalazione-${ticketNumber}.docx`;
};

type InternshipDocumentsSectionProps = { ticket: TicketDetail; internship: Internship };

/**
 * Documenti del tirocinio (US-1004, US-1005, US-1011): compilati dal server con la scheda salvata, si scaricano come
 * .docx. Un gruppo per documento: una convenzione per ogni convenzione (iniziale e proroghe), un progetto formativo per
 * ogni progetto, la relazione finale solo a tirocinio chiuso. Con più modelli caricati dall'ADMIN per un documento il
 * Tutor sceglie quale usare (US-1010). L'esito si annuncia nella regione di stato, il focus resta sul pulsante.
 */
export const InternshipDocumentsSection = ({ ticket: d, internship }: InternshipDocumentsSectionProps) => {
  const { t } = useTranslation();
  const download = useDownloadInternshipDocument();
  const [status, setStatus] = useState<{ text: string; error?: boolean } | null>(null);
  const closed = d.status === 'FORM_RESTITUZIONE';

  const items: Record<Kind['key'], DocumentItem[]> = {
    convenzione: [
      { target: {}, label: t('internship.documents.download.convenzione') },
      ...internship.extensions.map((e) => ({
        target: { extension: e.number },
        label: t('internship.documents.download.convenzioneProroga', { k: e.number }),
      })),
    ],
    progettoFormativo: [
      ...internship.trainingProjects.map((p) => ({
        target: { project: p.number },
        label: t('internship.documents.download.progettoFormativoN', { n: p.number }),
      })),
      ...internship.extensions.flatMap((e) =>
        e.trainingProjects.map((p) => ({
          target: { extension: e.number, project: p.number },
          label: t('internship.documents.download.progettoFormativoProroga', { k: e.number, n: p.number }),
        })),
      ),
    ],
    relazioneFinale: closed
      ? [{ target: {}, label: t('internship.documents.download.relazioneFinale') }]
      : [],
  };
  // senza progetti formativi resta il documento unico, con i dati del progetto da completare
  if (items.progettoFormativo.length === 0) {
    items.progettoFormativo.push({ target: {}, label: t('internship.documents.download.progettoFormativo') });
  }

  const onDownload = async (kind: Kind, target: DocumentTarget, template?: string): Promise<void> => {
    const name = filename(kind, d.number, target);
    setStatus({ text: t('internship.documents.preparing') });
    try {
      saveBlob(await download.mutateAsync({ ticketId: d.id, kind: kind.kind, template, target }), name);
      setStatus({ text: t('internship.documents.downloaded', { filename: name }) });
    } catch {
      setStatus({ text: t('internship.documents.error'), error: true });
    }
  };

  return (
    <section
      aria-labelledby="internship-documents"
      className="space-y-4 rounded-xl border border-border bg-card p-4"
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
      {KINDS.map((kind) => (
        <DocumentGroup
          key={kind.kind}
          ticketId={d.id}
          kind={kind}
          items={items[kind.key]}
          busy={download.isPending}
          onDownload={(target, template) => void onDownload(kind, target, template)}
          onMissingTemplate={(label) =>
            setStatus({ text: t('internship.documents.templateMissing', { label }), error: true })
          }
        />
      ))}
      <p role="status" className={status?.error ? 'font-medium text-destructive' : 'text-muted-foreground'}>
        {status?.text}
      </p>
    </section>
  );
};

type DocumentGroupProps = {
  ticketId: string;
  kind: Kind;
  items: DocumentItem[];
  /** Un download in corso: i pulsanti non ne avviano un altro. */
  busy: boolean;
  onDownload: (target: DocumentTarget, template?: string) => void;
  onMissingTemplate: (label: string) => void;
};

/**
 * I pulsanti di un documento, con la scelta del modello quando ce n'è più di uno (US-1010): la scelta vale per tutti i
 * pulsanti del gruppo. Senza pulsanti (la relazione finale prima della chiusura) il gruppo dice quando si scarica.
 */
const DocumentGroup = ({
  ticketId,
  kind,
  items,
  busy,
  onDownload,
  onMissingTemplate,
}: DocumentGroupProps) => {
  const { t } = useTranslation();
  const headingId = `internship-documents-${kind.kind}`;
  return (
    <div role="group" aria-labelledby={headingId} className="space-y-2">
      <h3 id={headingId} className="font-semibold">
        {t(`internship.documents.groups.${kind.key}`)}
      </h3>
      {items.length === 0 ? (
        <p className="text-muted-foreground">{t('internship.documents.relazioneFinaleClosed')}</p>
      ) : (
        <DocumentButtons
          ticketId={ticketId}
          kind={kind}
          items={items}
          busy={busy}
          onDownload={onDownload}
          onMissingTemplate={onMissingTemplate}
        />
      )}
    </div>
  );
};

const DocumentButtons = ({
  ticketId,
  kind,
  items,
  busy,
  onDownload,
  onMissingTemplate,
}: DocumentGroupProps) => {
  const { t } = useTranslation();
  const choices = useDocumentTemplateChoices(ticketId, kind.kind);
  const [template, setTemplate] = useState('');
  const selectRef = useRef<HTMLSelectElement>(null);
  const many = (choices.data?.length ?? 0) > 1;
  const selectId = `internship-template-${kind.kind}`;
  const hintId = `${selectId}-hint`;
  const label = t(`internship.documents.template.${kind.key}`);

  const onClick = (target: DocumentTarget): void => {
    if (busy) return;
    if (many && template === '') {
      onMissingTemplate(label);
      selectRef.current?.focus();
      return;
    }
    onDownload(target, many ? template : undefined);
  };

  return (
    <>
      {many && (
        <div className="max-w-md space-y-1">
          <label htmlFor={selectId} className="block text-sm font-medium">
            {label}
          </label>
          <p id={hintId} className="text-sm text-muted-foreground">
            {t('internship.documents.templateHint')}
          </p>
          <NativeSelect
            ref={selectRef}
            id={selectId}
            aria-describedby={hintId}
            value={template}
            onChange={(e) => setTemplate(e.target.value)}
          >
            <option value="">{t('internship.documents.chooseTemplate')}</option>
            {choices.data?.map((choice) => (
              <option key={choice.id} value={choice.id}>
                {choice.name}
              </option>
            ))}
          </NativeSelect>
        </div>
      )}
      <div className="flex flex-wrap gap-3">
        {items.map((item) => (
          <Button
            key={item.label}
            size="lg"
            variant="outline"
            className="h-auto min-h-10 max-w-full shrink py-2 whitespace-normal aria-disabled:cursor-not-allowed aria-disabled:opacity-50"
            onClick={() => onClick(item.target)}
            disabled={choices.isPending}
            // durante un download il pulsante resta attivo: disabilitato perderebbe il focus
            aria-disabled={busy || undefined}
          >
            <FileDown aria-hidden="true" />
            {item.label}
          </Button>
        ))}
      </div>
    </>
  );
};
