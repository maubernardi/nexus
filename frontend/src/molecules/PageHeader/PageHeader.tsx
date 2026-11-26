import { useRef, type ReactNode } from 'react';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { useHeightVariable } from '@/hooks/useHeightVariable';

type PageHeaderProps = {
  /** Ritorno all'elenco o alla pagina di partenza: un `BackLink`. */
  back: ReactNode;
  /** Titolo della scheda: l'unico h1 della pagina. */
  children: ReactNode;
};

/**
 * Intestazione di una scheda (US-1312, PO 26/11): ritorno all'elenco e titolo restano fissi sotto la barra in alto
 * mentre la scheda scorre, e il ritorno non serve più in fondo. Sugli schermi bassi (smartphone in orizzontale, zoom
 * forte) scorre con la pagina, per non coprirla. La sua altezza va nello spazio di scorrimento (index.css): l'elemento
 * con il focus non le finisce sotto (WCAG 2.4.11).
 */
export const PageHeader = ({ back, children }: PageHeaderProps) => {
  const ref = useRef<HTMLDivElement>(null);
  useHeightVariable(ref, '--page-header-height');
  return (
    <div
      ref={ref}
      data-page-header=""
      className="z-20 -mx-4 space-y-2 border-b border-border bg-background px-4 pt-2 pb-3 [@media(min-height:32rem)]:sticky [@media(min-height:32rem)]:top-(--header-height)"
    >
      {back}
      <PageHeading>{children}</PageHeading>
    </div>
  );
};
