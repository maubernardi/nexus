import { useId, useRef, useState, type FormEvent } from 'react';
import { useTranslation } from 'react-i18next';

import { Button } from '@/components/ui/button';
import { apiError } from '@/config/api/errors';
import { CV_MAX_BYTES, type Beneficiary } from '@/config/api/beneficiaryApi';
import {
  useDeleteBeneficiaryCv,
  useDownloadBeneficiaryCv,
  useUploadBeneficiaryCv,
} from '@/config/hooks/useBeneficiaries';
import { saveBlob } from '@/lib/download';

type Feedback = { kind: 'status' | 'alert'; text: string };

const size = (bytes: number): string =>
  bytes >= 1024 * 1024
    ? `${(bytes / (1024 * 1024)).toLocaleString('it-IT', { maximumFractionDigits: 1 })} MB`
    : `${Math.max(1, Math.round(bytes / 1024)).toLocaleString('it-IT')} KB`;

/**
 * Curriculum del beneficiario (US-205): chi vede la scheda lo scarica; il Tutor proprietario lo carica (solo PDF fino a
 * 5 MB), lo sostituisce o lo toglie.
 */
export const BeneficiaryCvSection = ({ beneficiary }: { beneficiary: Beneficiary }) => {
  const { t } = useTranslation();
  const headingId = useId();
  const inputId = useId();
  const hintId = useId();
  const upload = useUploadBeneficiaryCv();
  const remove = useDeleteBeneficiaryCv();
  const download = useDownloadBeneficiaryCv();
  const [file, setFile] = useState<File | null>(null);
  const [feedback, setFeedback] = useState<Feedback | null>(null);
  const feedbackRef = useRef<HTMLParagraphElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);
  const hasCv = Boolean(beneficiary.cvFileName);
  const busy = upload.isPending || remove.isPending;

  const show = (next: Feedback): void => {
    setFeedback(next);
    requestAnimationFrame(() => feedbackRef.current?.focus());
  };

  const failure = (e: unknown): string => {
    const body = apiError(e);
    return (
      body?.fieldErrors?.[0]?.message ?? (body?.status === 400 && body.message ? body.message : t('cv.error'))
    );
  };

  const onUpload = async (event: FormEvent<HTMLFormElement>): Promise<void> => {
    event.preventDefault();
    if (!file) return show({ kind: 'alert', text: t('cv.errors.none') });
    // controlli anche qui per non caricare file inutili; il server li ripete
    if (!file.name.toLowerCase().endsWith('.pdf') && file.type !== 'application/pdf') {
      return show({ kind: 'alert', text: t('cv.errors.type') });
    }
    if (file.size > CV_MAX_BYTES) return show({ kind: 'alert', text: t('cv.errors.size') });
    try {
      await upload.mutateAsync({ id: beneficiary.id, file });
      setFile(null);
      if (inputRef.current) inputRef.current.value = '';
      show({ kind: 'status', text: t(hasCv ? 'cv.replaced' : 'cv.uploaded') });
    } catch (e) {
      show({ kind: 'alert', text: failure(e) });
    }
  };

  const onRemove = async (): Promise<void> => {
    try {
      await remove.mutateAsync(beneficiary.id);
      show({ kind: 'status', text: t('cv.removed') });
    } catch (e) {
      show({ kind: 'alert', text: failure(e) });
    }
  };

  const onDownload = async (): Promise<void> => {
    try {
      saveBlob(await download.mutateAsync(beneficiary.id), beneficiary.cvFileName ?? 'CV.pdf');
    } catch (e) {
      show({ kind: 'alert', text: failure(e) });
    }
  };

  return (
    <section aria-labelledby={headingId} className="space-y-3 rounded-xl border border-border bg-card p-4">
      <h2 id={headingId} className="text-lg font-semibold text-primary">
        {t('cv.heading')}
      </h2>
      {feedback && (
        <p
          ref={feedbackRef}
          role={feedback.kind}
          tabIndex={-1}
          className={
            feedback.kind === 'alert'
              ? 'rounded-md border-2 border-destructive p-3 font-medium text-destructive'
              : 'rounded-md border-l-4 border-primary bg-background p-3 font-medium'
          }
        >
          {feedback.text}
        </p>
      )}
      {hasCv ? (
        <div className="flex flex-wrap items-center gap-3">
          <span className="break-all">
            {beneficiary.cvFileName}
            {beneficiary.cvSizeBytes !== undefined && ` (${size(beneficiary.cvSizeBytes)})`}
          </span>
          <Button
            type="button"
            variant="outline"
            disabled={download.isPending}
            onClick={() => void onDownload()}
          >
            {download.isPending ? t('cv.downloading') : t('cv.download')}
          </Button>
          {beneficiary.editable && (
            <Button type="button" variant="destructive" disabled={busy} onClick={() => void onRemove()}>
              {t('cv.remove')}
            </Button>
          )}
        </div>
      ) : (
        <p>{t('cv.none')}</p>
      )}
      {beneficiary.editable && (
        <form noValidate onSubmit={(e) => void onUpload(e)} className="space-y-2">
          <label htmlFor={inputId} className="block text-sm font-medium">
            {t(hasCv ? 'cv.replaceLabel' : 'cv.uploadLabel')}
          </label>
          <p id={hintId} className="text-sm text-muted-foreground">
            {t('cv.hint')}
          </p>
          <div className="flex flex-wrap items-center gap-3">
            <input
              ref={inputRef}
              id={inputId}
              type="file"
              accept="application/pdf,.pdf"
              aria-describedby={hintId}
              onChange={(e) => setFile(e.target.files?.[0] ?? null)}
              className="min-h-11 max-w-full text-sm file:mr-3 file:min-h-11 file:rounded-md file:border file:border-border file:bg-background file:px-3 file:font-medium"
            />
            <Button type="submit" disabled={busy}>
              {upload.isPending ? t('cv.uploading') : t(hasCv ? 'cv.replace' : 'cv.upload')}
            </Button>
          </div>
        </form>
      )}
    </section>
  );
};
