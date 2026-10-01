import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AxiosError, AxiosHeaders } from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { createJobSlot, fetchJobSlots, setJobSlotActive, type JobSlot } from '@/config/api/jobSlotApi';
import { fetchJobCategories, fetchZones } from '@/config/api/referenceApi';
import { JobSlotsSection } from '@/organisms/JobSlotsSection/JobSlotsSection';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderWithProviders } from '@/tests/renderWithProviders';

vi.mock('@/config/api/jobSlotApi', () => ({
  fetchJobSlots: vi.fn(),
  createJobSlot: vi.fn(),
  updateJobSlot: vi.fn(),
  setJobSlotActive: vi.fn(),
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
});
