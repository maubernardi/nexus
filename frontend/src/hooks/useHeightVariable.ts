import { useLayoutEffect, type RefObject } from 'react';

/**
 * Tiene nella variabile CSS `name` della pagina l'altezza dell'elemento, anche quando cambia (per esempio un titolo
 * che va a capo): serve alle intestazioni fisse (US-1312), per mettersi una sotto l'altra e per lo spazio di
 * scorrimento che non le fa coprire l'elemento con il focus (WCAG 2.4.11). Si toglie quando l'elemento sparisce.
 */
export const useHeightVariable = (ref: RefObject<HTMLElement | null>, name: string): void => {
  useLayoutEffect(() => {
    const element = ref.current;
    if (!element) return;
    const root = document.documentElement;
    const update = (): void => root.style.setProperty(name, `${element.offsetHeight}px`);
    update();
    const observer = typeof ResizeObserver === 'undefined' ? null : new ResizeObserver(update);
    observer?.observe(element);
    return () => {
      observer?.disconnect();
      root.style.removeProperty(name);
    };
  }, [ref, name]);
};
