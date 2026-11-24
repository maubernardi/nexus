import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';

import { Pagination } from '@/molecules/Pagination/Pagination';
import { expectNoAxeViolations } from '@/tests/axe';

const renderPagination = (page: number, pageCount: number) => {
  const onPageChange = vi.fn();
  const onPageSizeChange = vi.fn();
  render(
    <main>
      <Pagination
        id="page-size"
        page={page}
        pageCount={pageCount}
        pageSize={25}
        pageSizes={[10, 25, 50, 100]}
        label="Pagine dell’elenco"
        pageSizeLabel="Righe per pagina"
        onPageChange={onPageChange}
        onPageSizeChange={onPageSizeChange}
      />
    </main>,
  );
  return {
    onPageChange,
    onPageSizeChange,
    nav: screen.getByRole('navigation', { name: 'Pagine dell’elenco' }),
  };
};

describe('<Pagination>', () => {
  it('pagina in mezzo: tutti i pulsanti, con il numero della pagina e delle pagine', async () => {
    const user = userEvent.setup();
    const { onPageChange, nav } = renderPagination(3, 15520);

    expect(nav).toHaveTextContent('Pagina 3 di 15.520');
    await user.click(within(nav).getByRole('button', { name: 'Prima' }));
    await user.click(within(nav).getByRole('button', { name: 'Precedente' }));
    await user.click(within(nav).getByRole('button', { name: 'Successiva' }));
    await user.click(within(nav).getByRole('button', { name: 'Ultima' }));
    expect(onPageChange.mock.calls).toEqual([[1], [2], [4], [15520]]);
    await expectNoAxeViolations();
  });

  it('prima e ultima pagina: i pulsanti che non portano altrove sono disattivati', () => {
    const { nav } = renderPagination(1, 1);
    for (const name of ['Prima', 'Precedente', 'Successiva', 'Ultima']) {
      expect(within(nav).getByRole('button', { name })).toBeDisabled();
    }
  });

  it('il menu propone le dimensioni e avvisa della scelta', async () => {
    const user = userEvent.setup();
    const { onPageSizeChange } = renderPagination(2, 4);
    const select = screen.getByRole('combobox', { name: 'Righe per pagina' });

    expect(select).toHaveDisplayValue('25');
    await user.selectOptions(select, '50');
    expect(onPageSizeChange).toHaveBeenCalledWith(50);
  });
});
