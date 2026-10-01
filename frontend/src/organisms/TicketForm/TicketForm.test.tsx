import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AxiosError, AxiosHeaders } from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { fetchMyBeneficiaries, type BeneficiarySummary } from '@/config/api/beneficiaryApi';
import { fetchJobCategories, fetchMyProjects } from '@/config/api/referenceApi';
import { submitTicket, type Ticket } from '@/config/api/ticketApi';
import { TicketForm } from '@/organisms/TicketForm/TicketForm';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderWithProviders } from '@/tests/renderWithProviders';

vi.mock('@/config/api/beneficiaryApi', () => ({ createBeneficiary: vi.fn(), fetchMyBeneficiaries: vi.fn() }));
vi.mock('@/config/api/referenceApi', () => ({
  fetchZones: vi.fn(),
  fetchJobCategories: vi.fn(),
  fetchMyProjects: vi.fn(),
}));
vi.mock('@/config/api/ticketApi', () => ({ submitTicket: vi.fn() }));

const MARIO: BeneficiarySummary = {
  id: 'C1',
  firstName: 'Mario',
  lastName: 'Rossi',
  birthYear: 1998,
  residenceZoneName: 'Nord',
};
const GIULIA: BeneficiarySummary = {
  id: 'C2',
  firstName: 'Giulia',
  lastName: 'Verdi',
  birthYear: 2001,
  residenceZoneName: 'Sud',
};
const GOL = { id: 'P1', code: 'GOL', name: 'GOL' };
const POLIS = { id: 'P2', code: 'POLIS', name: 'POLIS' };

const renderForm = (props: Partial<Parameters<typeof TicketForm>[0]> = {}) =>
  renderWithProviders(
    <main>
      <TicketForm onSuccess={vi.fn()} {...props} />
    </main>,
  );

describe('<TicketForm>', () => {
  beforeEach(() => {
    vi.mocked(fetchMyBeneficiaries).mockResolvedValue([MARIO, GIULIA]);
    vi.mocked(fetchMyProjects).mockResolvedValue([GOL, POLIS]);
    vi.mocked(fetchJobCategories).mockResolvedValue([
      { id: 'J1', code: 'MAGAZZINIERE', name: 'Magazziniere' },
    ]);
    vi.mocked(submitTicket).mockReset();
  });

  it('senza scelte mostra il riepilogo con tre errori e vi sposta il focus', async () => {
    const user = userEvent.setup();
    renderForm();

    await user.click(await screen.findByRole('button', { name: 'Invia segnalazione' }));

    const summary = await screen.findByRole('alert');
    await waitFor(() => expect(summary).toHaveFocus());
    expect(summary).toHaveTextContent('Correggi 3 errori');
    expect(screen.getByRole('link', { name: 'Progetto: Campo obbligatorio' })).toHaveAttribute(
      'href',
      '#ticket-projectId',
    );
    expect(screen.getByLabelText(/^Beneficiario/)).toHaveAttribute('aria-invalid', 'true');
    expect(submitTicket).not.toHaveBeenCalled();
    await expectNoAxeViolations();
  });

  it('preseleziona beneficiario e unico progetto e invia il payload', async () => {
    vi.mocked(fetchMyProjects).mockResolvedValue([GOL]);
    const ticket = { id: 'T1', number: 42, status: 'NUOVA', beneficiary: MARIO } as Ticket;
    vi.mocked(submitTicket).mockResolvedValue(ticket);
    const onSuccess = vi.fn();
    const user = userEvent.setup();
    renderForm({ onSuccess, initialBeneficiaryId: 'C1' });

    expect(await screen.findByLabelText(/^Beneficiario/)).toHaveValue('C1');
    expect(screen.getByRole('option', { name: 'Rossi Mario (1998)' })).toBeInTheDocument();
    expect(screen.getByLabelText(/^Progetto/)).toHaveValue('P1');
    await user.selectOptions(screen.getByLabelText(/^Mansione richiesta/), 'J1');
    await user.click(screen.getByRole('button', { name: 'Invia segnalazione' }));

    await waitFor(() => expect(onSuccess).toHaveBeenCalledWith(ticket));
    expect(vi.mocked(submitTicket).mock.calls[0][0]).toEqual({
      beneficiaryId: 'C1',
      projectId: 'P1',
      jobCategoryId: 'J1',
    });
  });

  it('ignora un beneficiario preselezionato che non è del Tutor', async () => {
    renderForm({ initialBeneficiaryId: 'ALTRUI' });
    expect(await screen.findByLabelText(/^Beneficiario/)).toHaveValue('');
  });

  it('riporta sul campo l’errore del server', async () => {
    vi.mocked(submitTicket).mockRejectedValue(
      new AxiosError('Bad Request', '400', undefined, undefined, {
        status: 400,
        statusText: 'Bad Request',
        headers: {},
        config: { headers: new AxiosHeaders() },
        data: {
          status: 400,
          message: 'Dati non validi',
          fieldErrors: [{ field: 'projectId', message: 'Progetto non valido o non assegnato' }],
        },
      }),
    );
    const user = userEvent.setup();
    renderForm({ initialBeneficiaryId: 'C2' });
    await user.selectOptions(await screen.findByLabelText(/^Progetto/), 'P2');
    await user.selectOptions(screen.getByLabelText(/^Mansione richiesta/), 'J1');
    await user.click(screen.getByRole('button', { name: 'Invia segnalazione' }));

    expect(
      await screen.findByRole('link', { name: 'Progetto: Progetto non valido o non assegnato' }),
    ).toBeInTheDocument();
    expect(screen.getByLabelText(/^Progetto/)).toHaveAccessibleDescription(
      /Progetto non valido o non assegnato/,
    );
  });

  it('senza beneficiari spiega cosa fare e rimanda alla registrazione', async () => {
    vi.mocked(fetchMyBeneficiaries).mockResolvedValue([]);
    renderForm();

    expect(
      await screen.findByRole('heading', { name: 'Nessun beneficiario da segnalare' }),
    ).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Registra un beneficiario' })).toHaveAttribute(
      'href',
      '/beneficiari/nuovo',
    );
    expect(screen.queryByRole('button', { name: 'Invia segnalazione' })).not.toBeInTheDocument();
    await expectNoAxeViolations();
  });

  it('chi ha una segnalazione aperta è visibile ma non selezionabile', async () => {
    vi.mocked(fetchMyBeneficiaries).mockResolvedValue([{ ...MARIO, openTicketNumber: 12 }, GIULIA]);
    renderForm({ initialBeneficiaryId: 'C1' });

    const open = await screen.findByRole('option', {
      name: 'Rossi Mario (1998) – segnalazione n. 12 aperta',
    });
    expect(open).toBeDisabled();
    // la preselezione non vale per chi ha già una segnalazione aperta; resta l'unico disponibile
    expect(screen.getByLabelText(/^Beneficiario/)).toHaveValue('C2');
    expect(screen.getByLabelText(/^Beneficiario/)).toHaveAccessibleDescription(/ne è ammessa una alla volta/);
    await expectNoAxeViolations();
  });

  it('se tutti hanno una segnalazione aperta lo spiega al posto del modulo', async () => {
    vi.mocked(fetchMyBeneficiaries).mockResolvedValue([{ ...MARIO, openTicketNumber: 12 }]);
    renderForm();

    expect(
      await screen.findByRole('heading', {
        name: 'Tutti i tuoi beneficiari hanno già una segnalazione aperta',
      }),
    ).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Registra un beneficiario' })).toHaveAttribute(
      'href',
      '/beneficiari/nuovo',
    );
    expect(screen.queryByRole('button', { name: 'Invia segnalazione' })).not.toBeInTheDocument();
  });

  it('senza progetti assegnati lo spiega', async () => {
    vi.mocked(fetchMyProjects).mockResolvedValue([]);
    renderForm();

    expect(await screen.findByRole('heading', { name: 'Nessun progetto assegnato' })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Invia segnalazione' })).not.toBeInTheDocument();
  });
});
