import { useTranslation } from 'react-i18next';

import { cn } from '@/lib/utils';

type SkipLinkProps = {
  targetId?: string;
  className?: string;
};

/** Primo elemento focalizzabile della pagina (WCAG 2.4.1 Bypass Blocks). */
export const SkipLink = ({ targetId = 'main-content', className }: SkipLinkProps) => {
  const { t } = useTranslation();

  const handleClick = (event: React.MouseEvent<HTMLAnchorElement>): void => {
    const target = document.getElementById(targetId);
    if (target) {
      event.preventDefault();
      target.focus(); // focus() porta anche l'elemento in vista
    }
  };

  return (
    <a
      href={`#${targetId}`}
      onClick={handleClick}
      className={cn(
        'sr-only z-50 rounded-md bg-primary px-4 py-3 font-medium text-primary-foreground',
        'focus:not-sr-only focus:fixed focus:top-2 focus:left-2',
        className,
      )}
    >
      {t('a11y.skipToContent')}
    </a>
  );
};
