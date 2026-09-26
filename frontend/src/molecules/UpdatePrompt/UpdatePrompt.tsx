import { useTranslation } from 'react-i18next';
import { useRegisterSW } from 'virtual:pwa-register/react';

import { Button } from '@/components/ui/button';

/** Avvisa quando è disponibile una nuova versione; l'aggiornamento avviene solo su azione dell'utente. */
export const UpdatePrompt = () => {
  const { t } = useTranslation();
  const {
    needRefresh: [needRefresh, setNeedRefresh],
    offlineReady: [offlineReady, setOfflineReady],
    updateServiceWorker,
  } = useRegisterSW();

  const handleClose = (): void => {
    setNeedRefresh(false);
    setOfflineReady(false);
  };

  return (
    <div role="status" aria-live="polite" className="fixed inset-x-4 bottom-4 z-40 sm:left-auto sm:max-w-sm">
      {(needRefresh || offlineReady) && (
        <div className="rounded-lg border border-input bg-card p-4 text-card-foreground shadow-lg">
          <p className="text-sm">{needRefresh ? t('pwa.updateAvailable') : t('pwa.offlineReady')}</p>
          <div className="mt-3 flex flex-wrap gap-2">
            {needRefresh && <Button onClick={() => void updateServiceWorker(true)}>{t('pwa.update')}</Button>}
            <Button variant="outline" onClick={handleClose}>
              {needRefresh ? t('pwa.later') : t('pwa.close')}
            </Button>
          </div>
        </div>
      )}
    </div>
  );
};
