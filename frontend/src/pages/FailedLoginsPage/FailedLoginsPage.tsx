import { useTranslation } from 'react-i18next';
import { Link } from 'react-router';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { useFailedLogins } from '@/config/hooks/useAccessLog';
import { AccessLogTable } from '@/organisms/AccessLogTable/AccessLogTable';

const link = 'font-medium text-primary underline underline-offset-4';

/**
 * Accessi non riusciti (US-113): i tentativi falliti di tutti, anche con uno username che non esiste, per accorgersi
 * di chi prova a indovinare una password.
 */
export const FailedLoginsPage = () => {
  const { t } = useTranslation();
  const log = useFailedLogins();

  return (
    <div className="space-y-6">
      <PageHeading>{t('accessLog.failedHeading')}</PageHeading>
      <p className="text-muted-foreground">{t('accessLog.failedIntro')}</p>
      <AccessLogTable query={log} caption={t('accessLog.failedCaption')} allUsers />
      <Link to="/utenti" className={link}>
        {t('users.backToList')}
      </Link>
    </div>
  );
};
