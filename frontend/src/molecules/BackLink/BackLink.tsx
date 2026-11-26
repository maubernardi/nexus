import { type ReactNode } from 'react';
import { ArrowLeft } from 'lucide-react';
import { Link } from 'react-router';

import { cn } from '@/lib/utils';

type BackLinkProps = {
  to: string;
  children: ReactNode;
  className?: string;
};

/**
 * Ritorno all'elenco (o alla pagina di partenza) di una scheda: lo stesso collegamento sta in cima, sopra il titolo, e
 * in fondo (US-1312, PO 26/11), così da una scheda lunga si torna indietro senza scorrerla tutta.
 */
export const BackLink = ({ to, children, className }: BackLinkProps) => (
  <Link
    to={to}
    className={cn(
      'inline-flex min-h-6 items-center gap-1 font-medium text-primary underline underline-offset-4',
      className,
    )}
  >
    <ArrowLeft aria-hidden="true" className="size-4 shrink-0" />
    {children}
  </Link>
);
