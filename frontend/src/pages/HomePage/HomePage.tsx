import { ClipboardList, FilePlus2, ListOrdered, UserPlus } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { Button } from '@/components/ui/button';
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
      <div className="space-y-3 rounded-xl border border-l-4 border-border border-l-primary bg-card p-6 text-card-foreground shadow-sm">
        {user && (
          <p className="text-lg font-medium">
            {t('pages.home.greeting', { name: user.firstName ?? user.username })}
          </p>
        )}
        {primaryRole && <p>{t(`pages.home.intro.${primaryRole}`)}</p>}
        {user?.roles.includes('TUTOR') && (
          <div aria-label={t('pages.home.actions')} role="group" className="flex flex-wrap gap-3 pt-2">
            <Button asChild size="lg">
              <Link to="/beneficiari/nuovo">
                <UserPlus aria-hidden="true" />
                {t('nav.newBeneficiary')}
              </Link>
            </Button>
            <Button asChild size="lg" variant="outline">
              <Link to="/segnalazioni/nuova">
                <FilePlus2 aria-hidden="true" />
                {t('nav.newTicket')}
              </Link>
            </Button>
          </div>
        )}
        {(user?.roles.includes('CALL_CENTER') || user?.roles.includes('ADMIN')) && (
          <div aria-label={t('pages.home.actions')} role="group" className="flex flex-wrap gap-3 pt-2">
            <Button asChild size="lg">
              <Link to="/coda">
                <ListOrdered aria-hidden="true" />
                {t('nav.queue')}
              </Link>
            </Button>
            <Button asChild size="lg" variant="outline">
              <Link to="/lavorazioni">
                <ClipboardList aria-hidden="true" />
                {t('nav.myWork')}
              </Link>
            </Button>
          </div>
        )}
      </div>
      <p className="text-muted-foreground">{t('pages.home.comingSoon')}</p>
    </section>
  );
};
