import { useEffect, useId, useRef, useState } from 'react';
import { ChevronDown, Menu, Settings, X } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { NavLink, useLocation } from 'react-router';

import { Button } from '@/components/ui/button';
import type { Role } from '@/config/auth/types';
import { useOpenContactRequestCount } from '@/config/hooks/useContactRequests';
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
    'flex min-h-11 items-center gap-2 rounded-md px-3 text-sm font-medium whitespace-nowrap text-brand-foreground hover:bg-brand-foreground/10 xl:min-h-9',
    // voce attiva: oro su Deep Emerald (4,9:1) + sottolineatura, non solo colore
    isActive && 'font-semibold text-gold underline decoration-gold decoration-2 underline-offset-8',
  );

const NavItemLink = ({ item, count = 0 }: { item: NavItem; count?: number }) => {
  const { t } = useTranslation();
  const Icon = item.icon;
  return (
    // la voce resta attiva anche nelle pagine interne della sezione (es. scheda di un beneficiario)
    <NavLink to={item.to} end={item.to === '/'} className={linkClass}>
      <Icon aria-hidden="true" className="size-4" />
      {t(item.labelKey)}
      {count > 0 && (
        <>
          {/* il numero si vede nel bollino e si sente nel nome della voce (US-507) */}
          <span
            aria-hidden="true"
            className="inline-flex min-w-5 items-center justify-center rounded-full bg-gold px-1.5 text-xs font-bold text-brand"
          >
            {count}
          </span>
          {/* spazio come nodo a sé: «Richieste di contatto (2 da prendere in carico)» */}{' '}
          <span className="sr-only">{t('nav.openContactRequests', { count })}</span>
        </>
      )}
    </NavLink>
  );
};

/**
 * Navigazione principale: lista orizzontale da desktop (da 1280 px), menu a scomparsa (disclosure) sotto. Da 1024 a
 * 1279 px le voci non ci stavano più, per l'ADMIN già prima delle richieste di contatto (US-507). Per l'ADMIN
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
  // il numero delle richieste di contatto lo chiede solo chi vede la voce (US-507)
  const openRequests = useOpenContactRequestCount(
    visible.some((item) => item.count === 'openContactRequests'),
  ).data;
  const countOf = (item: NavItem): number => (item.count === 'openContactRequests' ? (openRequests ?? 0) : 0);
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
        className="xl:hidden"
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
          'absolute inset-x-0 top-full flex-col gap-3 border-b-2 border-gold bg-brand p-4 shadow-md xl:static xl:flex xl:border-0 xl:bg-transparent xl:p-0 xl:shadow-none',
          isOpen ? 'flex' : 'hidden',
        )}
      >
        {mobileHeader && (
          <div className="border-b border-brand-foreground/20 pb-3 xl:hidden">{mobileHeader}</div>
        )}
        <ul className="flex flex-col gap-1 xl:flex-row">
          {items.map((item) => (
            <li key={item.to}>
              <NavItemLink item={item} count={countOf(item)} />
            </li>
          ))}
          {adminItems.length > 0 && (
            <li ref={adminRef} className="xl:relative">
              {/* nel menu mobile il gruppo è sempre aperto: il titolo è solo testo */}
              <span className="flex min-h-11 items-center gap-2 px-3 text-xs font-semibold tracking-wide text-gold uppercase xl:hidden">
                {t('nav.admin')}
              </span>
              <button
                ref={adminToggleRef}
                type="button"
                aria-expanded={isAdminOpen}
                aria-controls={adminListId}
                onClick={() => setAdminOpenOnPath(isAdminOpen ? null : location.pathname)}
                className={cn(linkClass({ isActive: adminActive }), 'hidden cursor-pointer xl:flex')}
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
                  'flex flex-col gap-1 pl-3 xl:absolute xl:top-full xl:right-0 xl:z-40 xl:mt-2 xl:min-w-48 xl:rounded-md xl:border xl:border-gold xl:bg-brand xl:p-2 xl:shadow-md',
                  !isAdminOpen && 'xl:hidden',
                )}
              >
                {adminItems.map((item) => (
                  <li key={item.to}>
                    <NavItemLink item={item} count={countOf(item)} />
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
