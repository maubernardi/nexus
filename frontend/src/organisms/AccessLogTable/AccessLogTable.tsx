import { useId } from 'react';
import { useTranslation } from 'react-i18next';

import { Button } from '@/components/ui/button';
import { apiError } from '@/config/api/errors';
import type { AccessLogEntry } from '@/config/api/accessLogApi';
import type { AccessLogQuery } from '@/config/hooks/useAccessLog';

/** Data e ora italiane, come nelle email di sicurezza: 07/11/2026 12:46. */
const dateTime = new Intl.DateTimeFormat('it-IT', {
  day: '2-digit',
  month: '2-digit',
  year: 'numeric',
  hour: '2-digit',
  minute: '2-digit',
  timeZone: 'Europe/Rome',
});

/** Motivi dei tentativi falliti che hanno un testo; gli altri sono «Non riuscito». */
const KNOWN_ERRORS = [
  'invalid_user_credentials',
  'user_not_found',
  'user_disabled',
  'user_temporarily_disabled',
] as const;
type KnownError = (typeof KNOWN_ERRORS)[number];

const th = 'px-3 py-2 text-left text-sm font-semibold whitespace-nowrap';
const td = 'px-3 py-2 align-top';

type AccessLogTableProps = {
  query: AccessLogQuery;
  /** Testo della didascalia, per i lettori di schermo. */
  caption: string;
  /** Tentativi di tutti gli utenti: mostra chi e cosa ha scritto nel modulo. */
  allUsers?: boolean;
};

/**
 * Registro degli accessi (US-113): data e ora, esito con il motivo, indirizzo IP; dal più recente, 20 alla volta con
 * «Mostra accessi più vecchi».
 */
export const AccessLogTable = ({ query, caption, allUsers = false }: AccessLogTableProps) => {
  const { t } = useTranslation();
  const captionId = useId();
  const entries = query.data?.pages.flatMap((p) => p.entries) ?? [];

  const outcome = (e: AccessLogEntry): string => {
    if (e.success) return t('accessLog.success');
    const reason = (KNOWN_ERRORS as readonly string[]).includes(e.error ?? '')
      ? t(`accessLog.errors.${e.error as KnownError}`)
      : null;
    return reason ? t('accessLog.failureWith', { reason }) : t('accessLog.failure');
  };

  if (query.isError && !query.data) {
    const body = apiError(query.error);
    return (
      <div role="alert" className="space-y-3 rounded-md border-2 border-destructive bg-card p-4">
        <p className="font-medium text-destructive">
          {body?.status === 503 && body.message ? body.message : t('common.loadError')}
        </p>
        <Button variant="outline" onClick={() => void query.refetch()}>
          {t('common.retry')}
        </Button>
      </div>
    );
  }
  if (!query.data) return <p>{t('common.loading')}</p>;
  if (entries.length === 0) return <p>{t(allUsers ? 'accessLog.noFailures' : 'accessLog.none')}</p>;

  return (
    <div className="space-y-3">
      <div
        role="region"
        aria-labelledby={captionId}
        // eslint-disable-next-line jsx-a11y/no-noninteractive-tabindex -- regione scorrevole: deve essere raggiungibile da tastiera
        tabIndex={0}
        className="relative overflow-x-auto rounded-xl border border-border bg-card focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
      >
        <table className="w-full border-collapse text-sm max-md:w-max max-md:whitespace-nowrap">
          <caption id={captionId} className="sr-only">
            {caption}
          </caption>
          <thead className="border-b border-border bg-muted">
            <tr>
              <th scope="col" className={th}>
                {t('accessLog.fields.at')}
              </th>
              {allUsers && (
                <>
                  <th scope="col" className={th}>
                    {t('accessLog.fields.user')}
                  </th>
                  <th scope="col" className={th}>
                    {t('accessLog.fields.username')}
                  </th>
                </>
              )}
              <th scope="col" className={th}>
                {t('accessLog.fields.outcome')}
              </th>
              <th scope="col" className={th}>
                {t('accessLog.fields.ip')}
              </th>
            </tr>
          </thead>
          <tbody>
            {entries.map((e, i) => (
              <tr key={`${e.at}-${i}`} className="border-b border-border last:border-b-0">
                <td className={td}>
                  <time dateTime={e.at}>{dateTime.format(new Date(e.at))}</time>
                </td>
                {allUsers && (
                  <>
                    <td className={td}>{e.userName ?? t('accessLog.unknownUser')}</td>
                    <td className={`${td} break-all`}>{e.username ?? '—'}</td>
                  </>
                )}
                <td className={`${td} ${e.success ? '' : 'font-medium text-destructive'}`}>{outcome(e)}</td>
                <td className={td}>{e.ipAddress ?? '—'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {query.hasNextPage && (
        <Button
          type="button"
          variant="outline"
          disabled={query.isFetchingNextPage}
          onClick={() => void query.fetchNextPage()}
        >
          {query.isFetchingNextPage ? t('common.loading') : t('accessLog.more')}
        </Button>
      )}
    </div>
  );
};
