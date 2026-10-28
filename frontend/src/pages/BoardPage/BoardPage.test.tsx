import { screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { fetchBoardPosts } from '@/config/api/boardPostApi';
import { BoardPage } from '@/pages/BoardPage/BoardPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { boardPost } from '@/tests/fixtures/boardPost';
import { renderRoutes } from '@/tests/renderWithProviders';

vi.mock('@/config/api/boardPostApi', () => ({
  fetchBoardPosts: vi.fn(),
  fetchBoardPost: vi.fn(),
  updateBoardPost: vi.fn(),
  publishBoardPost: vi.fn(),
  archiveBoardPost: vi.fn(),
}));

const renderPage = (path = '/bacheca') =>
  renderRoutes({
    routes: [
      {
        path: '/bacheca',
        element: (
          <main>
            <BoardPage />
          </main>
        ),
      },
    ],
    initialPath: path,
  });

describe('<BoardPage>', () => {
  beforeEach(() => {
    vi.mocked(fetchBoardPosts).mockReset();
  });

  it('mostra le bozze con azienda e progetto, e passa ai pubblicati', async () => {
    const user = userEvent.setup();
    vi.mocked(fetchBoardPosts).mockImplementation((status) =>
      Promise.resolve(
        status === 'DRAFT'
          ? [boardPost(3)]
          : [boardPost(1, { status: 'PUBLISHED', project: null, publishedAt: '2026-10-20T09:00:00Z' })],
      ),
    );
    renderPage();

    const table = await screen.findByRole('table', { name: 'Annunci della bacheca: Bozze' });
    expect(fetchBoardPosts).toHaveBeenCalledWith('DRAFT');
    expect(screen.getByRole('link', { name: 'Bozze' })).toHaveAttribute('aria-current', 'page');
    const [row] = within(table).getAllByRole('row').slice(1);
    expect(within(row).getByRole('link', { name: 'Apri l’annuncio #3' })).toHaveAttribute(
      'href',
      '/bacheca/P3',
    );
    expect(within(row).getByText('Officina Rossi')).toBeInTheDocument();
    expect(within(row).getByText('GOL')).toBeInTheDocument();
    expect(screen.getByRole('status')).toHaveTextContent('1 annuncio');
    await expectNoAxeViolations();

    await user.click(screen.getByRole('link', { name: 'Pubblicati' }));
    const published = await screen.findByRole('table', { name: 'Annunci della bacheca: Pubblicati' });
    expect(fetchBoardPosts).toHaveBeenLastCalledWith('PUBLISHED');
    expect(within(published).getByText('Tutti i progetti')).toBeInTheDocument();
    expect(within(published).getByText('20/10/26')).toBeInTheDocument();
  });

  it('vista vuota', async () => {
    vi.mocked(fetchBoardPosts).mockResolvedValue([]);
    renderPage('/bacheca?stato=archiviati');

    expect(await screen.findByText('Nessun annuncio in questo stato.')).toBeInTheDocument();
    expect(fetchBoardPosts).toHaveBeenCalledWith('ARCHIVED');
    expect(screen.getByRole('link', { name: 'Archiviati' })).toHaveAttribute('aria-current', 'page');
  });
});
