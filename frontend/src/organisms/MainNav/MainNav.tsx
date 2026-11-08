import { useEffect, useId, useRef, useState } from 'react';
import { ChevronDown, Menu, Settings, X } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { NavLink, useLocation } from 'react-router';

import { Button } from '@/components/ui/button';
import type { Role } from '@/config/auth/types';
import { cn } from '@/lib/utils';
import { NAV_ITEMS, type NavItem } from '@/organisms/MainNav/navItems';

type MainNavProps = {
  roles: readonly Role[];
  /** Coordina almeno un progetto: vede la voce Coordinamento (US-112). */
  coordinator?: boolean;
  /** Contenuto mostrato in cima al menu a tendina, solo su mobile (es. identità dell'utente). */
  mobileHeader?: React.ReactNode;
};

const linkClass = ({ isActive }: { isActive: boolean }): string =>
  cn(
    'flex min-h-11 items-center gap-2 rounded-md px-3 text-sm font-medium whitespace-nowrap text-brand-foreground hover:bg-brand-foreground/10 lg:min-h-9',
    // voce attiva: oro su Deep Emerald (4,9:1) + sottolineatura, non solo colore
    isActive && 'font-semibold text-gold underline decoration-gold decoration-2 underline-offset-8',
  );

const NavItemLink = ({ item }: { item: NavItem }) => {
  const { t } = useTranslation();
  const Icon = item.icon;
  return (
    // la voce resta attiva anche nelle pagine interne della sezione (es. scheda di un beneficiario)
    <NavLink to={item.to} end={item.to === '/'} className={linkClass}>
      <Icon aria-hidden="true" className="size-4" />
      {t(item.labelKey)}
    </NavLink>
  );
};

/**
 * Navigazione principale: lista orizzontale da desktop (da 1024 px), menu a scomparsa (disclosure) sotto. Per l'ADMIN
 * Utenti, Cataloghi e Calendario stanno nel sottomenu «Amministrazione» (decisione del PO, 08/11): da desktop è un
 * disclosure con il suo elenco, nel menu mobile un gruppo sempre aperto.
 */
export const MainNav = ({ roles, coordinator = false, mobileHeader }: MainNavProps) => {
  const { t } = useTranslation();
  const listId = useId();
  const adminListId = useId();
  const location = useLocation();
  // menu e sottomenu sono aperti solo per la pagina in cui sono stati aperti: cambiando route si chiudono da soli
  const [openOnPath, setOpenOnPath] = useState<string | null>(null);
  const [adminOpenOnPath, setAdminOpenOnPath] = useState<string | null>(null);
  const isOpen = openOnPath === location.pathname;
  const isAdminOpen = adminOpenOnPath === location.pathname;
  const toggleRef = useRef<HTMLButtonElement>(null);
  const adminToggleRef = useRef<HTMLButtonElement>(null);
  const adminRef = useRef<HTMLLIElement>(null);

  const visible = NAV_ITEMS.filter(
    (item) =>
      (item.roles.length === 0 || item.roles.some((r) => roles.includes(r))) &&
      (!item.coordinatorOnly || coordinator),
  );
  const items = visible.filter((item) => !item.admin);
  const adminItems = visible.filter((item) => item.admin);
  const adminActive = adminItems.some((item) => location.pathname.startsWith(item.to));

  // Escape chiude prima il sottomenu, poi il menu mobile, e riporta il focus sul suo pulsante
  useEffect(() => {
    if (!isOpen && !isAdminOpen) {
      return undefined;
    }
    const handleKeyDown = (event: KeyboardEvent): void => {
      if (event.key !== 'Escape') return;
      if (isAdminOpen) {
        setAdminOpenOnPath(null);
        adminToggleRef.current?.focus();
      } else {
        setOpenOnPath(null);
        toggleRef.current?.focus();
      }
    };
    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, isAdminOpen]);

  // il sottomenu si chiude anche cliccando fuori
  useEffect(() => {
    if (!isAdminOpen) {
      return undefined;
    }
    const handleClick = (event: MouseEvent): void => {
      if (!adminRef.current?.contains(event.target as Node)) setAdminOpenOnPath(null);
    };
    document.addEventListener('mousedown', handleClick);
    return () => document.removeEventListener('mousedown', handleClick);
  }, [isAdminOpen]);

  return (
    <nav aria-label={t('a11y.mainNav')}>
      <Button
        ref={toggleRef}
        variant="brand"
        size="icon-lg"
        className="lg:hidden"
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
          'absolute inset-x-0 top-full flex-col gap-3 border-b-2 border-gold bg-brand p-4 shadow-md lg:static lg:flex lg:border-0 lg:bg-transparent lg:p-0 lg:shadow-none',
          isOpen ? 'flex' : 'hidden',
        )}
      >
        {mobileHeader && (
          <div className="border-b border-brand-foreground/20 pb-3 lg:hidden">{mobileHeader}</div>
        )}
        <ul className="flex flex-col gap-1 lg:flex-row">
          {items.map((item) => (
            <li key={item.to}>
              <NavItemLink item={item} />
            </li>
          ))}
          {adminItems.length > 0 && (
            <li ref={adminRef} className="lg:relative">
              {/* nel menu mobile il gruppo è sempre aperto: il titolo è solo testo */}
              <span className="flex min-h-11 items-center gap-2 px-3 text-xs font-semibold tracking-wide text-gold uppercase lg:hidden">
                {t('nav.admin')}
              </span>
              <button
                ref={adminToggleRef}
                type="button"
                aria-expanded={isAdminOpen}
                aria-controls={adminListId}
                onClick={() => setAdminOpenOnPath(isAdminOpen ? null : location.pathname)}
                className={cn(linkClass({ isActive: adminActive }), 'hidden cursor-pointer lg:flex')}
              >
                <Settings aria-hidden="true" className="size-4" />
                {t('nav.admin')}
                <ChevronDown
                  aria-hidden="true"
                  className={cn('size-4 transition-transform', isAdminOpen && 'rotate-180')}
                />
              </button>
              <ul
                id={adminListId}
                className={cn(
                  'flex flex-col gap-1 pl-3 lg:absolute lg:top-full lg:right-0 lg:z-40 lg:mt-2 lg:min-w-48 lg:rounded-md lg:border lg:border-gold lg:bg-brand lg:p-2 lg:shadow-md',
                  !isAdminOpen && 'lg:hidden',
                )}
              >
                {adminItems.map((item) => (
                  <li key={item.to}>
                    <NavItemLink item={item} />
                  </li>
                ))}
              </ul>
            </li>
          )}
        </ul>
      </div>
    </nav>
  );
};
