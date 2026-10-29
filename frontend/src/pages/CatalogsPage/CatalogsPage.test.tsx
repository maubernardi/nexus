import { screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';

import { fetchCatalog } from '@/config/api/catalogApi';
import { CatalogsPage } from '@/pages/CatalogsPage/CatalogsPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderWithProviders } from '@/tests/renderWithProviders';

vi.mock('@/config/api/catalogApi', () => ({
  fetchCatalog: vi.fn(),
  createCatalogItem: vi.fn(),
  renameCatalogItem: vi.fn(),
  setCatalogItemActive: vi.fn(),
}));

describe('<CatalogsPage>', () => {
  it('un titolo per la pagina e una sezione per catalogo', async () => {
    vi.mocked(fetchCatalog).mockImplementation((kind) =>
      Promise.resolve(
        kind === 'zones'
          ? [{ id: 'Z1', code: 'NORD', name: 'Firenze Nord', active: true }]
          : kind === 'job-categories'
            ? [{ id: 'C1', code: 'MAGAZZINIERE', name: 'Magazziniere', active: true }]
            : [{ id: 'T1', code: 'CURRICULARE', name: 'Curriculare', active: true }],
      ),
    );
    renderWithProviders(
      <main>
        <CatalogsPage />
      </main>,
    );

    expect(screen.getByRole('heading', { level: 1, name: 'Cataloghi' })).toBeInTheDocument();
    expect(await screen.findByRole('table', { name: 'Zone: 1 voce' })).toBeInTheDocument();
    expect(await screen.findByRole('table', { name: 'Tipologie di mansione: 1 voce' })).toBeInTheDocument();
    expect(fetchCatalog).toHaveBeenCalledWith('zones');
    expect(await screen.findByRole('table', { name: 'Tipi di tirocinio: 1 voce' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Aggiungi tipo' })).toBeInTheDocument();
    expect(fetchCatalog).toHaveBeenCalledWith('job-categories');
    expect(fetchCatalog).toHaveBeenCalledWith('internship-types');
    await expectNoAxeViolations();
  });
});
