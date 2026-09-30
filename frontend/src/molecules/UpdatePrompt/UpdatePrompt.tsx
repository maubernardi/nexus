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
    // striscia nel flusso della pagina (non sovrapposta): non copre mai contenuti o elementi con il focus (WCAG 2.4.11)
    <div role="status" aria-live="polite">
      {(needRefresh || offlineReady) && (
        <div className="border-b border-border bg-accent text-accent-foreground">
          <div className="mx-auto flex max-w-6xl flex-wrap items-center gap-x-4 gap-y-2 px-4 py-3">
            <p className="text-sm font-medium">
              {needRefresh ? t('pwa.updateAvailable') : t('pwa.offlineReady')}
            </p>
            <div className="flex flex-wrap gap-2">
              {needRefresh && (
                <Button onClick={() => void updateServiceWorker(true)}>{t('pwa.update')}</Button>
              )}
              <Button variant="outline" onClick={handleClose}>
                {needRefresh ? t('pwa.later') : t('pwa.close')}
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
