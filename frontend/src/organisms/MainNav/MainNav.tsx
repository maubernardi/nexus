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
};

/** Navigazione principale: lista orizzontale da desktop, menu a scomparsa (disclosure) su mobile. */
export const MainNav = ({ roles }: MainNavProps) => {
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
        variant="outline"
        size="icon-lg"
        className="md:hidden"
        aria-expanded={isOpen}
        aria-controls={listId}
        aria-label={isOpen ? t('a11y.closeMenu') : t('a11y.openMenu')}
        onClick={() => setOpenOnPath(isOpen ? null : location.pathname)}
      >
        {isOpen ? <X aria-hidden="true" /> : <Menu aria-hidden="true" />}
      </Button>
      <ul
        id={listId}
        className={cn(
          'absolute inset-x-0 top-full flex-col gap-1 border-b bg-background p-4 shadow-md md:static md:flex md:flex-row md:border-0 md:p-0 md:shadow-none',
          isOpen ? 'flex' : 'hidden',
        )}
      >
        {items.map(({ to, labelKey, icon: Icon }) => (
          <li key={to}>
            <NavLink
              to={to}
              end
              className={({ isActive }) =>
                cn(
                  'flex min-h-11 items-center gap-2 rounded-md px-3 text-sm font-medium hover:bg-accent hover:text-accent-foreground md:min-h-9',
                  isActive && 'bg-accent text-accent-foreground underline decoration-2 underline-offset-4',
                )
              }
            >
              <Icon aria-hidden="true" className="size-4" />
              {t(labelKey)}
            </NavLink>
          </li>
        ))}
      </ul>
    </nav>
  );
};
