import { useTranslation } from 'react-i18next';
import { useParams } from 'react-router';

import { useUserAccessLog } from '@/config/hooks/useAccessLog';
import { useUser } from '@/config/hooks/useUsers';
import { AccessLogTable } from '@/organisms/AccessLogTable/AccessLogTable';
import { BackLink } from '@/molecules/BackLink/BackLink';
import { PageHeader } from '@/molecules/PageHeader/PageHeader';

/** Accessi di un utente (US-113): l'ADMIN vede quando e da dove è entrato, e i tentativi falliti. */
export const UserAccessLogPage = () => {
  const { t } = useTranslation();
  const { id = '' } = useParams();
  const user = useUser(id);
  const log = useUserAccessLog(id);
  const name = user.data ? `${user.data.firstName} ${user.data.lastName}` : null;

  return (
    <div className="space-y-6">
      <PageHeader back={<BackLink to="/utenti">{t('users.backToList')}</BackLink>}>
        {name ? t('accessLog.userHeading', { name }) : t('pages.userAccessLog.title')}
      </PageHeader>
      <p className="text-muted-foreground">{t('accessLog.intro')}</p>
      <AccessLogTable query={log} caption={t('accessLog.userCaption', { name: name ?? '' })} />
    </div>
  );
};
