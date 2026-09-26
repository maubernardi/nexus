import { useEffect, useRef } from 'react';
import { useTranslation } from 'react-i18next';
import { useLocation, useMatches } from 'react-router';

import type { RouteHandle } from '@/config/routing/types';

/**
 * A ogni cambio route aggiorna il titolo del documento (WCAG 2.4.2) e sposta il focus sull'h1 della
 * nuova pagina, così che gli screen reader annuncino la navigazione. Al primo caricamento il focus
 * non viene spostato.
 */
export const RouteAnnouncer = () => {
  const { t } = useTranslation();
  const matches = useMatches();
  const location = useLocation();
  const isFirstRender = useRef(true);

  const titleKey = [...matches].reverse().find((match) => (match.handle as RouteHandle | undefined)?.titleKey)
    ?.handle as RouteHandle | undefined;

  useEffect(() => {
    const pageTitle = titleKey ? t(titleKey.titleKey) : null;
    document.title = pageTitle ? `${pageTitle} · ${t('app.name')}` : t('app.name');
  }, [titleKey, t]);

  useEffect(() => {
    if (isFirstRender.current) {
      isFirstRender.current = false;
      return;
    }
    document.querySelector<HTMLElement>('main h1')?.focus();
  }, [location.pathname]);

  return null;
};
