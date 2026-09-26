import { useState } from 'react';
import { vi } from 'vitest';

/** Stub di `virtual:pwa-register/react` per i test (il modulo virtuale esiste solo nel build Vite). */
export const pwaRegisterState = {
  needRefresh: false,
  offlineReady: false,
  updateServiceWorker: vi.fn(async () => {}),
};

export const useRegisterSW = () => {
  const needRefresh = useState(pwaRegisterState.needRefresh);
  const offlineReady = useState(pwaRegisterState.offlineReady);
  return { needRefresh, offlineReady, updateServiceWorker: pwaRegisterState.updateServiceWorker };
};
