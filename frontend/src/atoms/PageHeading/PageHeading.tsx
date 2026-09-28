import { type Ref } from 'react';

import { cn } from '@/lib/utils';

type PageHeadingProps = {
  children: React.ReactNode;
  className?: string;
  ref?: Ref<HTMLHeadingElement>;
};

/** Unico h1 della pagina; focalizzabile via script per annunciare il cambio route. */
export const PageHeading = ({ children, className, ref }: PageHeadingProps) => (
  <h1
    ref={ref}
    tabIndex={-1}
    className={cn(
      // filetto oro decorativo sotto il titolo
      'text-2xl font-semibold tracking-tight text-primary after:mt-2 after:block after:h-1 after:w-14 after:rounded-full after:bg-gold sm:text-3xl',
      className,
    )}
  >
    {children}
  </h1>
);
