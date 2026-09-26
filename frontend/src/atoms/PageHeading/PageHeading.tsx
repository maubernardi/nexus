import { type Ref } from 'react';

import { cn } from '@/lib/utils';

type PageHeadingProps = {
  children: React.ReactNode;
  className?: string;
  ref?: Ref<HTMLHeadingElement>;
};

/** Unico h1 della pagina; focalizzabile via script per annunciare il cambio route. */
export const PageHeading = ({ children, className, ref }: PageHeadingProps) => (
  <h1 ref={ref} tabIndex={-1} className={cn('text-2xl font-semibold tracking-tight sm:text-3xl', className)}>
    {children}
  </h1>
);
