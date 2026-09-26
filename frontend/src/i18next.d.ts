import 'i18next';

import type { resources } from '@/languages/dictionary';

declare module 'i18next' {
  interface CustomTypeOptions {
    defaultNS: 'translation';
    resources: (typeof resources)['it'];
  }
}
