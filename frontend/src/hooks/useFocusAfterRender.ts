import { useCallback, useEffect, useRef, useState, type RefObject } from 'react';

type FocusAfterRender<T extends HTMLElement> = {
  ref: RefObject<T | null>;
  /** Chiede il focus sull'elemento: arriva appena React ha aggiornato il DOM, anche se l'elemento compare ora. */
  requestFocus: () => void;
};

/**
 * Sposta il focus su un elemento che compare con lo stesso aggiornamento di stato che chiede il focus: un messaggio di
 * esito, un titolo, il pulsante che torna dopo la chiusura di un modulo. Il focus parte da un effetto, quindi dopo che
 * React ha scritto il DOM; con `requestAnimationFrame` il fotogramma poteva arrivare prima, l'elemento non c'era ancora
 * e il focus restava dov'era (EN-9). Ogni richiesta conta, anche quando il messaggio è uguale al precedente.
 */
export const useFocusAfterRender = <T extends HTMLElement>(): FocusAfterRender<T> => {
  const ref = useRef<T>(null);
  const [requests, setRequests] = useState(0);

  useEffect(() => {
    if (requests > 0) ref.current?.focus();
  }, [requests]);

  const requestFocus = useCallback(() => setRequests((n) => n + 1), []);

  return { ref, requestFocus };
};
