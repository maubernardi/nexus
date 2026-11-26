import { useTranslation } from 'react-i18next';

import { useFailedLogins } from '@/config/hooks/useAccessLog';
import { AccessLogTable } from '@/organisms/AccessLogTable/AccessLogTable';
import { BackLink } from '@/molecules/BackLink/BackLink';
import { PageHeader } from '@/molecules/PageHeader/PageHeader';

/**
 * Accessi non riusciti (US-113): i tentativi falliti di tutti, anche con uno username che non esiste, per accorgersi
 * di chi prova a indovinare una password.
 */
export const FailedLoginsPage = () => {
  const { t } = useTranslation();
  const log = useFailedLogins();

  return (
    <div className="space-y-6">
      <PageHeader back={<BackLink to="/utenti">{t('users.backToList')}</BackLink>}>
        {t('accessLog.failedHeading')}
      </PageHeader>
      <p className="text-muted-foreground">{t('accessLog.failedIntro')}</p>
      <AccessLogTable query={log} caption={t('accessLog.failedCaption')} allUsers />
    </div>
  );
};
