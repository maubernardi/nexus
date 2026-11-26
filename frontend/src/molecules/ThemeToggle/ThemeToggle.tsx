import { Moon, Sun } from 'lucide-react';
import { useTranslation } from 'react-i18next';

import { useTheme } from '@/hooks/useTheme';

/**
 * Pulsante del tema nella barra in alto (US-1312, PO 26/11): con il tema chiaro mostra la luna e passa allo scuro, con
 * quello scuro mostra il sole e passa al chiaro. Il nome dice che cosa fa («Passa al tema scuro»).
 */
export const ThemeToggle = () => {
  const { t } = useTranslation();
  const { theme, toggle } = useTheme();
  const label = t(theme === 'dark' ? 'theme.toLight' : 'theme.toDark');
  const Icon = theme === 'dark' ? Sun : Moon;
  return (
    <button
      type="button"
      onClick={toggle}
      aria-label={label}
      title={label}
      className="flex size-11 shrink-0 items-center justify-center rounded-md text-brand-foreground hover:bg-brand-foreground/10 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
    >
      <Icon aria-hidden="true" className="size-5" />
    </button>
  );
};
