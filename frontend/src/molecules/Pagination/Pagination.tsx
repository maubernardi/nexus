import { ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight } from 'lucide-react';
import { useTranslation } from 'react-i18next';

import { Button } from '@/components/ui/button';
import { NativeSelect } from '@/components/ui/native-select';

type PaginationProps = {
  /** Id del menu degli elementi per pagina. */
  id: string;
  /** Pagina corrente, da 1. */
  page: number;
  /** Numero di pagine, almeno 1. */
  pageCount: number;
  pageSize: number;
  pageSizes: readonly number[];
  /** Nome della navigazione per le tecnologie assistive ("Pagine dell'elenco delle aziende"). */
  label: string;
  /** Etichetta del menu ("Aziende per pagina"). */
  pageSizeLabel: string;
  onPageChange: (page: number) => void;
  onPageSizeChange: (size: number) => void;
};

/**
 * Pagine di un elenco (US-513): prima, precedente, «Pagina 3 di 552», successiva, ultima, e il menu degli elementi per
 * pagina. I pulsanti che non portano da nessuna parte sono disattivati; testi sempre visibili, non solo icone.
 */
export const Pagination = ({
  id,
  page,
  pageCount,
  pageSize,
  pageSizes,
  label,
  pageSizeLabel,
  onPageChange,
  onPageSizeChange,
}: PaginationProps) => {
  const { t } = useTranslation();
  const first = page <= 1;
  const last = page >= pageCount;

  return (
    <div className="flex flex-wrap items-center justify-between gap-3">
      <div className="flex items-center gap-2">
        <label htmlFor={id} className="text-sm font-medium whitespace-nowrap">
          {pageSizeLabel}
        </label>
        <NativeSelect
          id={id}
          value={pageSize}
          onChange={(e) => onPageSizeChange(Number(e.target.value))}
          className="w-24"
        >
          {pageSizes.map((size) => (
            <option key={size} value={size}>
              {size}
            </option>
          ))}
        </NativeSelect>
      </div>
      <nav aria-label={label}>
        <ul className="flex flex-wrap items-center gap-2">
          <li>
            <Button variant="outline" disabled={first} onClick={() => onPageChange(1)}>
              <ChevronsLeft aria-hidden="true" />
              {t('pagination.first')}
            </Button>
          </li>
          <li>
            <Button variant="outline" disabled={first} onClick={() => onPageChange(page - 1)}>
              <ChevronLeft aria-hidden="true" />
              {t('pagination.previous')}
            </Button>
          </li>
          <li className="px-2 text-sm font-medium whitespace-nowrap tabular-nums">
            {t('pagination.current', {
              page: page.toLocaleString('it-IT'),
              pages: pageCount.toLocaleString('it-IT'),
            })}
          </li>
          <li>
            <Button variant="outline" disabled={last} onClick={() => onPageChange(page + 1)}>
              {t('pagination.next')}
              <ChevronRight aria-hidden="true" />
            </Button>
          </li>
          <li>
            <Button variant="outline" disabled={last} onClick={() => onPageChange(pageCount)}>
              {t('pagination.last')}
              <ChevronsRight aria-hidden="true" />
            </Button>
          </li>
        </ul>
      </nav>
    </div>
  );
};
