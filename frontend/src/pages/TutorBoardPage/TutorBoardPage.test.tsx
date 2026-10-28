import { screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { fetchTutorBoard, type TutorBoardPost } from '@/config/api/boardPostApi';
import { fetchJobCategories, fetchZones } from '@/config/api/referenceApi';
import { TutorBoardPage } from '@/pages/TutorBoardPage/TutorBoardPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderRoutes } from '@/tests/renderWithProviders';

vi.mock('@/config/api/boardPostApi', () => ({ fetchTutorBoard: vi.fn() }));
vi.mock('@/config/api/referenceApi', () => ({
  fetchZones: vi.fn(),
  fetchJobCategories: vi.fn(),
  fetchMyProjects: vi.fn(),
  fetchProjects: vi.fn(),
}));

const post = (number: number, overrides: Partial<TutorBoardPost> = {}): TutorBoardPost => ({
  id: `P${number}`,
  number,
  jobCategory: { id: 'J1', code: 'MAG', name: 'Magazziniere' },
  zone: { id: 'Z1', code: 'NORD', name: 'Firenze Nord' },
  publishedAt: '2026-10-27T09:00:00Z',
  ...overrides,
});

const renderPage = (path = '/bacheca') =>
  renderRoutes({
    routes: [
      {
        path: '/bacheca',
        element: (
          <main>
            <TutorBoardPage />
          </main>
        ),
      },
    ],
    initialPath: path,
  });

describe('<TutorBoardPage>', () => {
  beforeEach(() => {
    vi.mocked(fetchTutorBoard).mockReset();
    vi.mocked(fetchZones).mockResolvedValue([
      { id: 'Z1', code: 'NORD', name: 'Firenze Nord' },
      { id: 'Z2', code: 'SUD', name: 'Firenze Sud' },
    ]);
    vi.mocked(fetchJobCategories).mockResolvedValue([{ id: 'J1', code: 'MAG', name: 'Magazziniere' }]);
  });

  it('schede con tipologia, zona, condizioni e riservato in testo', async () => {
    vi.mocked(fetchTutorBoard).mockResolvedValue([
      post(4, {
        project: { id: 'PR1', code: 'GOL', name: 'GOL' },
        ageMin: 18,
        ageMax: 29,
        weeklyHours: 30,
        durationMonths: 6,
      }),
      post(2, { durationMonths: 1 }),
    ]);
    renderPage();

    const list = await screen.findByRole('list', { name: 'Annunci aperti' });
    const [reserved, open] = within(list).getAllByRole('article');
    expect(within(reserved).getByRole('heading', { name: 'Annuncio #4 · Magazziniere' })).toBeInTheDocument();
    expect(within(reserved).getByText('Riservato al tuo progetto GOL')).toBeInTheDocument();
    expect(within(reserved).getByText('da 18 a 29 anni')).toBeInTheDocument();
    expect(within(reserved).getByText('30 ore a settimana')).toBeInTheDocument();
    expect(within(reserved).getByText('6 mesi')).toBeInTheDocument();
    expect(within(open).getByText('Aperto a tutti i progetti')).toBeInTheDocument();
    expect(within(open).getByText('1 mese')).toBeInTheDocument();
    expect(within(open).getAllByText('Non indicato')).toHaveLength(2);
    expect(screen.getByRole('status')).toHaveTextContent('2 annunci');
    await expectNoAxeViolations();
  });

  it('filtri per zona e tipologia nell’indirizzo, con azzeramento', async () => {
    const user = userEvent.setup();
    vi.mocked(fetchTutorBoard).mockResolvedValue([]);
    renderPage();

    expect(await screen.findByText('Nessun annuncio aperto con questi filtri.')).toBeInTheDocument();
    const zone = screen.getByRole('combobox', { name: 'Zona' });
    await screen.findByRole('option', { name: 'Firenze Sud' });
    await user.selectOptions(zone, 'Firenze Sud');
    expect(fetchTutorBoard).toHaveBeenLastCalledWith({ zoneId: 'Z2', jobCategoryId: undefined });

    await user.click(screen.getByRole('button', { name: 'Azzera i filtri' }));
    expect(fetchTutorBoard).toHaveBeenLastCalledWith({ zoneId: undefined, jobCategoryId: undefined });
    expect(zone).toHaveValue('');
  });
});
