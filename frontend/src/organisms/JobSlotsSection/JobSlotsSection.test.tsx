import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AxiosError, AxiosHeaders } from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { createBoardPost } from '@/config/api/boardPostApi';
import { createJobSlot, fetchJobSlots, setJobSlotActive, type JobSlot } from '@/config/api/jobSlotApi';
import { fetchJobCategories, fetchZones } from '@/config/api/referenceApi';
import { JobSlotsSection } from '@/organisms/JobSlotsSection/JobSlotsSection';
import { expectNoAxeViolations } from '@/tests/axe';
import { boardPost } from '@/tests/fixtures/boardPost';
import { renderRoutes, renderWithProviders } from '@/tests/renderWithProviders';

vi.mock('@/config/api/jobSlotApi', () => ({
  fetchJobSlots: vi.fn(),
  createJobSlot: vi.fn(),
  updateJobSlot: vi.fn(),
  setJobSlotActive: vi.fn(),
}));
vi.mock('@/config/api/boardPostApi', () => ({
  createBoardPost: vi.fn(),
}));
vi.mock('@/config/api/referenceApi', () => ({
  fetchZones: vi.fn(),
  fetchJobCategories: vi.fn(),
  fetchMyProjects: vi.fn(),
  fetchProjects: vi.fn(),
}));

const slot = (id: string, title: string, overrides: Partial<JobSlot> = {}): JobSlot => ({
  id,
  companyId: 'C1',
  title,
  description: null,
  jobCategory: { id: 'J1', code: 'MAG', name: 'Magazziniere' },
  zone: { id: 'Z1', code: 'NOVOLI', name: 'Novoli' },
  status: 'LIBERA',
  blockedByTicketNumber: null,
  active: true,
  version: 0,
  ...overrides,
});

const renderSection = (companyActive = true) =>
  renderWithProviders(
    <main>
      <JobSlotsSection companyId="C1" companyActive={companyActive} />
    </main>,
  );

describe('<JobSlotsSection>', () => {
  beforeEach(() => {
    vi.mocked(fetchJobSlots).mockResolvedValue([
      slot('S1', 'Carrellista', { status: 'BLOCCATA', blockedByTicketNumber: 12 }),
      slot('S2', 'Magazziniere'),
    ]);
    vi.mocked(fetchZones).mockResolvedValue([{ id: 'Z1', code: 'NOVOLI', name: 'Novoli' }]);
    vi.mocked(fetchJobCategories).mockResolvedValue([{ id: 'J1', code: 'MAG', name: 'Magazziniere' }]);
    vi.mocked(createJobSlot).mockReset();
    vi.mocked(setJobSlotActive).mockReset();
    vi.mocked(createBoardPost).mockReset();
  });

  it('mostra stato e disponibilità in testo; una mansione bloccata non si può ritirare', async () => {
    renderSection();
    const table = await screen.findByRole('table', { name: /Mansioni dell’azienda/ });
    const [blocked, free] = within(table).getAllByRole('row').slice(1);

    expect(within(blocked).getByText('Bloccata – segnalazione n. 12')).toBeInTheDocument();
    expect(within(blocked).queryByRole('button', { name: /^Ritira/ })).not.toBeInTheDocument();
    expect(within(free).getByRole('button', { name: 'Ritira Magazziniere' })).toBeInTheDocument();
    expect(within(free).getByRole('button', { name: 'Modifica Magazziniere' })).toBeInTheDocument();
    await expectNoAxeViolations();
  });

  it('aggiunge una mansione: focus sul modulo, poi sull’esito', async () => {
    const user = userEvent.setup();
    vi.mocked(createJobSlot).mockResolvedValue(slot('S3', 'Mulettista'));
    renderSection();

    await user.click(await screen.findByRole('button', { name: 'Aggiungi mansione' }));
    expect(screen.getByRole('heading', { name: 'Nuova mansione' })).toHaveFocus();
    await user.type(screen.getByLabelText(/^Titolo/), 'Mulettista');
    await user.selectOptions(await screen.findByLabelText(/^Tipologia/), 'J1');
    await user.selectOptions(screen.getByLabelText(/^Zona/), 'Z1');
    await user.click(screen.getByRole('button', { name: 'Aggiungi' }));

    const outcome = await screen.findByText('Mansione «Mulettista» aggiunta.');
    await waitFor(() => expect(outcome).toHaveFocus());
    expect(createJobSlot).toHaveBeenCalledWith(
      {
        companyId: 'C1',
        payload: {
          title: 'Mulettista',
          jobCategoryId: 'J1',
          zoneId: 'Z1',
          description: null,
          version: undefined,
        },
      },
      expect.anything(),
    );
  });

  it('ritiro rifiutato dal server: spiega il motivo', async () => {
    const user = userEvent.setup();
    vi.mocked(setJobSlotActive).mockRejectedValue(
      new AxiosError('Conflict', '409', undefined, undefined, {
        status: 409,
        statusText: 'Conflict',
        headers: {},
        config: { headers: new AxiosHeaders() },
        data: {
          status: 409,
          message: 'La mansione è bloccata dalla segnalazione n. 14: non si può ritirare',
        },
      }),
    );
    renderSection();

    await user.click(await screen.findByRole('button', { name: 'Ritira Magazziniere' }));
    expect(await screen.findByText(/Operazione non possibile: La mansione è bloccata/)).toHaveFocus();
  });

  it('azienda disattivata: niente aggiunta, spiegazione', async () => {
    renderSection(false);
    expect(await screen.findByText(/L’azienda è disattivata/)).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Aggiungi mansione' })).not.toBeInTheDocument();
  });

  it('annunci: link a quello attivo, "Crea annuncio" solo per le mansioni libere', async () => {
    vi.mocked(fetchJobSlots).mockResolvedValue([
      slot('S1', 'Carrellista', { status: 'BLOCCATA', blockedByTicketNumber: 12 }),
      slot('S2', 'Magazziniere', { activeBoardPostId: 'P3', activeBoardPostNumber: 3 }),
      slot('S3', 'Mulettista'),
      slot('S4', 'Cassiere', { active: false }),
    ]);
    renderSection();
    const table = await screen.findByRole('table', { name: /Mansioni dell’azienda/ });
    const [blocked, withPost, free, withdrawn] = within(table).getAllByRole('row').slice(1);

    expect(within(withPost).getByRole('link', { name: 'Annuncio #3 Magazziniere' })).toHaveAttribute(
      'href',
      '/bacheca/P3',
    );
    expect(within(withPost).queryByRole('button', { name: /Crea annuncio/ })).not.toBeInTheDocument();
    expect(within(free).getByRole('button', { name: 'Crea annuncio Mulettista' })).toBeInTheDocument();
    expect(within(blocked).queryByRole('button', { name: /Crea annuncio/ })).not.toBeInTheDocument();
    expect(within(withdrawn).queryByRole('button', { name: /Crea annuncio/ })).not.toBeInTheDocument();
    await expectNoAxeViolations();
  });

  it('crea la bozza e apre la scheda dell’annuncio', async () => {
    const user = userEvent.setup();
    vi.mocked(createBoardPost).mockResolvedValue(boardPost(8, { id: 'P8' }));
    renderRoutes({
      routes: [
        { path: '/aziende/C1', element: <JobSlotsSection companyId="C1" companyActive /> },
        { path: '/bacheca/:id', element: <p>Scheda dell’annuncio</p> },
      ],
      initialPath: '/aziende/C1',
    });

    await user.click(await screen.findByRole('button', { name: 'Crea annuncio Magazziniere' }));

    expect(await screen.findByText('Scheda dell’annuncio')).toBeInTheDocument();
    expect(vi.mocked(createBoardPost).mock.calls[0][0]).toBe('S2');
  });

  it('creazione rifiutata: il motivo del server', async () => {
    const user = userEvent.setup();
    vi.mocked(createBoardPost).mockRejectedValue(
      new AxiosError('Conflict', '409', undefined, undefined, {
        status: 409,
        statusText: 'Conflict',
        headers: {},
        config: { headers: new AxiosHeaders() },
        data: { status: 409, message: 'La mansione «Magazziniere» ha già l’annuncio #3' },
      }),
    );
    renderSection();

    await user.click(await screen.findByRole('button', { name: 'Crea annuncio Magazziniere' }));

    expect(
      await screen.findByText('Operazione non possibile: La mansione «Magazziniere» ha già l’annuncio #3'),
    ).toHaveFocus();
  });

  it('ritirando una mansione con annuncio, l’esito dice che l’annuncio è archiviato', async () => {
    const user = userEvent.setup();
    vi.mocked(fetchJobSlots).mockResolvedValue([
      slot('S2', 'Magazziniere', { activeBoardPostId: 'P3', activeBoardPostNumber: 3 }),
    ]);
    vi.mocked(setJobSlotActive).mockResolvedValue(slot('S2', 'Magazziniere', { active: false, version: 1 }));
    renderSection();

    await user.click(await screen.findByRole('button', { name: 'Ritira Magazziniere' }));

    expect(
      await screen.findByText('Mansione «Magazziniere» ritirata: l’annuncio #3 è stato archiviato.'),
    ).toHaveFocus();
  });
});
