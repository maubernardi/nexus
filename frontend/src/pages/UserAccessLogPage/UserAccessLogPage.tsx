import { useTranslation } from 'react-i18next';
import { Link, useParams } from 'react-router';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { useUserAccessLog } from '@/config/hooks/useAccessLog';
import { useUser } from '@/config/hooks/useUsers';
import { AccessLogTable } from '@/organisms/AccessLogTable/AccessLogTable';

const link = 'font-medium text-primary underline underline-offset-4';

/** Accessi di un utente (US-113): l'ADMIN vede quando e da dove è entrato, e i tentativi falliti. */
export const UserAccessLogPage = () => {
  const { t } = useTranslation();
  const { id = '' } = useParams();
  const user = useUser(id);
  const log = useUserAccessLog(id);
  const name = user.data ? `${user.data.firstName} ${user.data.lastName}` : null;

  return (
    <div className="space-y-6">
      <PageHeading>
        {name ? t('accessLog.userHeading', { name }) : t('pages.userAccessLog.title')}
      </PageHeading>
      <p className="text-muted-foreground">{t('accessLog.intro')}</p>
      <AccessLogTable query={log} caption={t('accessLog.userCaption', { name: name ?? '' })} />
      <Link to="/utenti" className={link}>
        {t('users.backToList')}
      </Link>
    </div>
  );
};
