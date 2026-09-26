import { WifiOff } from 'lucide-react';
import { useTranslation } from 'react-i18next';

import { useOnlineStatus } from '@/hooks/useOnlineStatus';

/**
 * La regione live resta sempre nel DOM (vuota quando online) così che gli screen reader
 * annuncino in modo affidabile la comparsa del messaggio.
 */
export const OfflineBanner = () => {
  const { t } = useTranslation();
  const isOnline = useOnlineStatus();

  return (
    <div role="status" aria-live="polite">
      {!isOnline && (
        <p className="flex items-center gap-2 bg-warning px-4 py-2 text-sm font-medium text-warning-foreground">
          <WifiOff aria-hidden="true" className="size-4 shrink-0" />
          {t('pwa.offline')}
        </p>
      )}
    </div>
  );
};
