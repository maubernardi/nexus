import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { fetchProjects, fetchZones } from '@/config/api/referenceApi';
import { fetchQueue, type QueueItem } from '@/config/api/ticketApi';
import { QueuePage } from '@/pages/QueuePage/QueuePage';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderRoutes } from '@/tests/renderWithProviders';

vi.mock('@/config/api/referenceApi', () => ({
  fetchZones: vi.fn(),
  fetchJobCategories: vi.fn(),
  fetchMyProjects: vi.fn(),
  fetchProjects: vi.fn(),
}));
vi.mock('@/config/api/ticketApi', () => ({ submitTicket: vi.fn(), fetchQueue: vi.fn() }));

const item = (number: number, overrides: Partial<QueueItem> = {}): QueueItem => ({
  id: `T${number}`,
  number,
  type: 'NORMAL',
  status: 'NUOVA',
  fastTrack: false,
  version: 0,
  candidate: {
    id: `C${number}`,
    firstName: 'Mario',
    lastName: `Rossi${number}`,
    birthYear: 1998,
    residenceZoneName: 'Zona Nord',
  },
  project: { id: 'P1', code: 'GOL', name: 'GOL' },
  requestedJobCategory: { id: 'J1', code: 'MAG', name: 'Magazziniere' },
  tutorName: 'Tutor Uno',
  createdAt: '2026-09-30T08:15:00Z',
  ...overrides,
});

const renderPage = (path = '/coda') =>
  renderRoutes({
    routes: [
      {
        path: '/coda',
        element: (
          <main>
            <QueuePage />
          </main>
        ),
      },
    ],
    initialPath: path,
  });

describe('<QueuePage>', () => {
  beforeEach(() => {
    vi.mocked(fetchProjects).mockResolvedValue([
      { id: 'P1', code: 'GOL', name: 'GOL' },
      { id: 'P2', code: 'POLIS', name: 'POLIS' },
    ]);
    vi.mocked(fetchZones).mockResolvedValue([{ id: 'Z1', code: 'NORD', name: 'Zona Nord' }]);
    vi.mocked(fetchQueue).mockReset();
    vi.mocked(fetchQueue).mockResolvedValue([
      item(7, { type: 'SPECIAL', fastTrack: true, status: 'IN_LAVORAZIONE' }),
      item(3),
    ]);
  });

  it('mostra la coda nell’ordine ricevuto con il badge testuale del fast-track', async () => {
    renderPage();

    const table = await screen.findByRole('table', { name: /prima le fast-track/ });
    const rows = within(table).getAllByRole('row').slice(1);
    expect(rows).toHaveLength(2);
    expect(within(rows[0]).getByRole('rowheader')).toHaveTextContent('7');
    expect(within(rows[0]).getByText('Fast-track')).toBeInTheDocument();
    expect(within(rows[0]).getByText('In lavorazione')).toBeInTheDocument();
    expect(within(rows[1]).getByText('Normale')).toBeInTheDocument();
    expect(within(rows[1]).getByText('Rossi3 Mario')).toBeInTheDocument();
    expect(screen.getByRole('status')).toHaveTextContent('2 segnalazioni in coda');
    expect(screen.getByRole('region', { name: /prima le fast-track/ })).toHaveAttribute('tabindex', '0');
    await expectNoAxeViolations();
  });

  it('il filtro per progetto finisce nell’indirizzo, filtra la richiesta e annuncia il risultato', async () => {
    const user = userEvent.setup();
    const { router } = renderPage();
    await screen.findByRole('table');
    vi.mocked(fetchQueue).mockResolvedValue([item(3)]);

    await user.selectOptions(await screen.findByRole('combobox', { name: 'Progetto' }), 'P1');

    await waitFor(() => expect(screen.getByRole('status')).toHaveTextContent('1 segnalazione in coda'));
    expect(router.state.location.search).toBe('?progetto=P1');
    expect(fetchQueue).toHaveBeenLastCalledWith({ projectId: 'P1', zoneId: undefined });

    await user.click(screen.getByRole('button', { name: 'Azzera filtri' }));
    await waitFor(() => expect(router.state.location.search).toBe(''));
  });

  it('riprende i filtri dall’indirizzo', async () => {
    renderPage('/coda?zona=Z1');

    await waitFor(() => expect(fetchQueue).toHaveBeenCalledWith({ projectId: undefined, zoneId: 'Z1' }));
    await waitFor(() => expect(screen.getByRole('combobox', { name: /^Zona/ })).toHaveValue('Z1'));
  });

  it('coda vuota: messaggio al posto della tabella', async () => {
    vi.mocked(fetchQueue).mockResolvedValue([]);
    renderPage();

    expect(
      await screen.findByText('Nessuna segnalazione in coda', { selector: 'p:not([role])' }),
    ).toBeInTheDocument();
    expect(screen.queryByRole('table')).not.toBeInTheDocument();
  });

  it('nessun risultato con i filtri: lo dice esplicitamente', async () => {
    vi.mocked(fetchQueue).mockResolvedValue([]);
    renderPage('/coda?progetto=P2');

    expect(await screen.findByText('Nessuna segnalazione corrisponde ai filtri scelti.')).toBeInTheDocument();
  });
});
