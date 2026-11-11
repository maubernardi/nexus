import { useEffect, useId, useState, type KeyboardEvent, type Ref } from 'react';
import { useTranslation } from 'react-i18next';

import { Input } from '@/components/ui/input';
import type { Municipality } from '@/config/api/municipalityApi';
import { useMunicipalitySearch } from '@/config/hooks/useMunicipalities';
import { cn } from '@/lib/utils';
import type { FieldControlProps } from '@/molecules/FormField/FormField';
import {
  municipalityLabel,
  toMunicipalityValue,
  type MunicipalityValue,
} from '@/molecules/MunicipalityField/municipalityValue';

const MIN_CHARS = 2;
const DELAY_MS = 300;
/** Quanti comuni manda al massimo il server (`MunicipalityServiceImpl.SEARCH_LIMIT`). */
const SEARCH_LIMIT = 20;

type MunicipalityFieldProps = FieldControlProps & {
  value: MunicipalityValue;
  onChange: (value: MunicipalityValue) => void;
  onBlur?: () => void;
  ref?: Ref<HTMLInputElement>;
};

/**
 * Comune dall'elenco ufficiale (US-1007), con i suggerimenti dalla seconda lettera. Segue il pattern combobox delle
 * WAI-ARIA APG (lista con scelta manuale): il focus resta nella casella, le frecce muovono l'opzione attiva
 * (`aria-activedescendant`), Invio sceglie, Esc chiude o svuota, Tab esce senza scegliere. Un messaggio di stato
 * annuncia quanti comuni ci sono (WCAG 4.1.3).
 */
export const MunicipalityField = ({ value, onChange, onBlur, ref, ...control }: MunicipalityFieldProps) => {
  const { t } = useTranslation();
  const listId = useId();
  const [open, setOpen] = useState(false);
  const [active, setActive] = useState(-1);
  const [query, setQuery] = useState('');
  // con un comune scelto il testo è il suo nome: non serve cercare
  const text = value.municipality ? '' : value.text.trim();

  // si cerca quando si smette di scrivere
  useEffect(() => {
    const timer = setTimeout(() => setQuery(text), DELAY_MS);
    return () => clearTimeout(timer);
  }, [text]);

  const search = useMunicipalitySearch(query);
  const ready =
    open && text.length >= MIN_CHARS && query === text && search.isSuccess && !search.isPlaceholderData;
  const results = ready ? search.data : [];
  const expanded = results.length > 0;
  const optionId = (index: number): string => `${listId}-${index}`;

  const close = (): void => {
    setOpen(false);
    setActive(-1);
  };

  const choose = (m: Municipality): void => {
    onChange(toMunicipalityValue(m));
    close();
  };

  const move = (next: number): void => {
    setActive(next);
    document.getElementById(optionId(next))?.scrollIntoView?.({ block: 'nearest' });
  };

  const onKeyDown = (event: KeyboardEvent<HTMLInputElement>): void => {
    switch (event.key) {
      case 'ArrowDown':
        event.preventDefault();
        setOpen(true);
        if (expanded) move(active < results.length - 1 ? active + 1 : 0);
        break;
      case 'ArrowUp':
        event.preventDefault();
        if (expanded) move(active > 0 ? active - 1 : results.length - 1);
        break;
      case 'Enter':
        // Invio sceglie solo con un'opzione attiva; altrimenti invia il modulo come sempre
        if (expanded && active >= 0) {
          event.preventDefault();
          choose(results[active]);
        }
        break;
      case 'Escape':
        if (expanded) {
          event.preventDefault();
          close();
        } else if (value.text !== '') {
          event.preventDefault();
          onChange({ text: '' });
        }
        break;
      case 'Tab':
        close();
        break;
      default:
    }
  };

  // con l'elenco al limite ci possono essere altri comuni: lo si dice anche a chi vede (controllo in Chrome, 11/11)
  const limited = results.length >= SEARCH_LIMIT;
  const status = !ready
    ? ''
    : results.length === 0
      ? t('municipalityField.noResults')
      : limited
        ? t('municipalityField.limited', { count: results.length })
        : t('municipalityField.results', { count: results.length });

  return (
    <div className="relative">
      <Input
        {...control}
        ref={ref}
        role="combobox"
        aria-autocomplete="list"
        aria-expanded={expanded}
        aria-controls={listId}
        aria-activedescendant={expanded && active >= 0 ? optionId(active) : undefined}
        autoComplete="off"
        value={value.text}
        onChange={(event) => {
          onChange({ text: event.target.value });
          setOpen(true);
          setActive(-1);
        }}
        onKeyDown={onKeyDown}
        onBlur={() => {
          close();
          onBlur?.();
        }}
      />
      <div
        hidden={!expanded}
        className="absolute z-20 mt-1 w-full rounded-md border border-border bg-popover text-popover-foreground shadow-md"
      >
        <div
          id={listId}
          role="listbox"
          aria-label={t('municipalityField.list')}
          className="max-h-72 overflow-auto py-1"
        >
          {results.map((m, index) => (
            <div
              key={m.id}
              id={optionId(index)}
              role="option"
              aria-selected={index === active}
              tabIndex={-1}
              // mousedown e non click: il focus non lascia la casella, così la scelta non chiude la lista prima
              onMouseDown={(event) => {
                event.preventDefault();
                choose(m);
              }}
              className={cn(
                'flex min-h-11 cursor-pointer items-center px-3',
                index === active ? 'bg-primary text-primary-foreground' : 'hover:bg-muted',
              )}
            >
              {municipalityLabel(m)}
            </div>
          ))}
        </div>
        {/* in fondo all'elenco, visibile: lo screen reader lo sente già dal messaggio di stato */}
        {limited && (
          <p aria-hidden="true" className="border-t border-border px-3 py-2 text-sm text-muted-foreground">
            {status}
          </p>
        )}
      </div>
      <p
        role="status"
        className={cn(ready && results.length === 0 ? 'mt-1 text-sm text-muted-foreground' : 'sr-only')}
      >
        {status}
      </p>
    </div>
  );
};
