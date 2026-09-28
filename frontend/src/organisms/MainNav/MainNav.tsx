import { useEffect, useId, useRef, useState } from 'react';
import { Menu, X } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { NavLink, useLocation } from 'react-router';

import { Button } from '@/components/ui/button';
import type { Role } from '@/config/auth/types';
import { cn } from '@/lib/utils';
import { NAV_ITEMS } from '@/organisms/MainNav/navItems';

type MainNavProps = {
  roles: readonly Role[];
  /** Contenuto mostrato in cima al menu a tendina, solo su mobile (es. identità dell'utente). */
  mobileHeader?: React.ReactNode;
};

/** Navigazione principale: lista orizzontale da desktop, menu a scomparsa (disclosure) su mobile. */
export const MainNav = ({ roles, mobileHeader }: MainNavProps) => {
  const { t } = useTranslation();
  const listId = useId();
  const location = useLocation();
  // il menu è aperto solo per la pagina in cui è stato aperto: cambiando route si chiude da solo
  const [openOnPath, setOpenOnPath] = useState<string | null>(null);
  const isOpen = openOnPath === location.pathname;
  const toggleRef = useRef<HTMLButtonElement>(null);

  const items = NAV_ITEMS.filter(
    (item) => item.roles.length === 0 || item.roles.some((r) => roles.includes(r)),
  );

  // Escape chiude il menu mobile e riporta il focus sul pulsante
  useEffect(() => {
    if (!isOpen) {
      return undefined;
    }
    const handleKeyDown = (event: KeyboardEvent): void => {
      if (event.key === 'Escape') {
        setOpenOnPath(null);
        toggleRef.current?.focus();
      }
    };
    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [isOpen]);

  return (
    <nav aria-label={t('a11y.mainNav')}>
      <Button
        ref={toggleRef}
        variant="brand"
        size="icon-lg"
        className="md:hidden"
        aria-expanded={isOpen}
        aria-controls={listId}
        aria-label={isOpen ? t('a11y.closeMenu') : t('a11y.openMenu')}
        onClick={() => setOpenOnPath(isOpen ? null : location.pathname)}
      >
        {isOpen ? <X aria-hidden="true" /> : <Menu aria-hidden="true" />}
      </Button>
      <div
        id={listId}
        className={cn(
          'absolute inset-x-0 top-full flex-col gap-3 border-b-2 border-gold bg-brand p-4 shadow-md md:static md:flex md:border-0 md:bg-transparent md:p-0 md:shadow-none',
          isOpen ? 'flex' : 'hidden',
        )}
      >
        {mobileHeader && (
          <div className="border-b border-brand-foreground/20 pb-3 md:hidden">{mobileHeader}</div>
        )}
        <ul className="flex flex-col gap-1 md:flex-row">
          {items.map(({ to, labelKey, icon: Icon }) => (
            <li key={to}>
              <NavLink
                to={to}
                end
                className={({ isActive }) =>
                  cn(
                    'flex min-h-11 items-center gap-2 rounded-md px-3 text-sm font-medium text-brand-foreground hover:bg-brand-foreground/10 md:min-h-9',
                    // voce attiva: oro su Deep Emerald (4,9:1) + sottolineatura, non solo colore
                    isActive &&
                      'font-semibold text-gold underline decoration-gold decoration-2 underline-offset-8',
                  )
                }
              >
                <Icon aria-hidden="true" className="size-4" />
                {t(labelKey)}
              </NavLink>
            </li>
          ))}
        </ul>
      </div>
    </nav>
  );
};
