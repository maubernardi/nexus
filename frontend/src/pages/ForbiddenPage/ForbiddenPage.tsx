import { useTranslation } from 'react-i18next';
import { Link } from 'react-router';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { Button } from '@/components/ui/button';

export const ForbiddenPage = () => {
  const { t } = useTranslation();

  return (
    <div className="space-y-4">
      <PageHeading>{t('pages.forbidden.heading')}</PageHeading>
      <p>{t('pages.forbidden.description')}</p>
      <Button asChild size="lg">
        <Link to="/">{t('pages.notFound.backHome')}</Link>
      </Button>
    </div>
  );
};
