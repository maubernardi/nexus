import { useTranslation } from 'react-i18next';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { useCurrentUser } from '@/config/hooks/useCurrentUser';

export const HomePage = () => {
  const { t } = useTranslation();
  const { data: user } = useCurrentUser();
  const primaryRole = user?.roles[0];

  return (
    <section aria-labelledby="home-heading" className="space-y-4">
      <PageHeading>
        <span id="home-heading">{t('pages.home.heading')}</span>
      </PageHeading>
      {user && (
        <p className="text-lg">{t('pages.home.greeting', { name: user.firstName ?? user.username })}</p>
      )}
      {primaryRole && <p>{t(`pages.home.intro.${primaryRole}`)}</p>}
      <p className="text-muted-foreground">{t('pages.home.comingSoon')}</p>
    </section>
  );
};
