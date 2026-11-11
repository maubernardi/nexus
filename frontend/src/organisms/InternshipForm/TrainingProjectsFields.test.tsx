import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AxiosError, AxiosHeaders } from 'axios';
import { describe, expect, it, vi } from 'vitest';

import type { Internship } from '@/config/api/internshipApi';
import { InternshipForm } from '@/organisms/InternshipForm/InternshipForm';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderWithProviders } from '@/tests/renderWithProviders';

vi.mock('@/config/api/municipalityApi', () => ({ searchMunicipalities: vi.fn() }));

const error = (status: number, body: Record<string, unknown>) =>
  new AxiosError(String(status), String(status), undefined, undefined, {
    status,
    statusText: String(status),
    headers: {},
    config: { headers: new AxiosHeaders() },
    data: { status, ...body },
  });

// come lo manda l'API: i campi vuoti sono omessi
const base: Internship = {
  version: 4,
  companyName: 'Demo Ristorazione',
  atecoCodes: [],
  internshipTypes: [],
  startDate: '2026-12-01',
  expectedEndDate: '2027-05-31',
  jobSlotTitle: 'Aiuto cuoco',
  companyTutors: [],
  suspensions: [],
  trainingProjects: [],
  firstSlotStart: '09:00:00',
  firstSlotEnd: '13:00:00',
  secondSlotStart: '14:00:00',
  secondSlotEnd: '18:00:00',
  excludeHolidays: true,
  schedule: [],
  missingForStart: [],
};

const withProject: Internship = {
  ...base,
  agreementNumber: '2026/145',
  agreementDate: '2026-11-20',
  agreementSignedDate: '2026-11-25',
  trainingProjects: [
    {
      number: 1,
      from: '2026-12-01',
      to: '2027-02-28',
      jobTitle: 'Aiuto cuoco',
      jobDetails: 'Piatti freddi',
      months: 3,
      totalMinutes: 3000,
    },
  ],
};

const renderForm = (internship: Internship) => {
  const onSubmit = vi.fn();
  renderWithProviders(
    <main>
      <h1>Scheda del tirocinio</h1>
      <InternshipForm internship={internship} onSubmit={onSubmit} onSuccess={vi.fn()} onConflict={vi.fn()} />
    </main>,
  );
  return { onSubmit };
};

const project = (n: number): HTMLElement => screen.getByRole('group', { name: `Progetto formativo ${n}` });

describe('<TrainingProjectsFields>', () => {
  it('convenzione e progetto salvati: durata della convenzione, mesi e monte ore del progetto', async () => {
    renderForm(withProject);

    expect(await screen.findByRole('heading', { level: 2, name: 'Convenzione' })).toBeInTheDocument();
    expect(screen.getByLabelText(/^Numero della convenzione/)).toHaveValue('2026/145');
    expect(screen.getByLabelText(/^Data della convenzione/)).toHaveValue('2026-11-20');
    expect(screen.getByLabelText(/^Data di stipula/)).toHaveValue('2026-11-25');
    expect(
      screen.getByText(
        'Durata: dal 01/12/2026 al 31/05/2027, come il tirocinio. La convenzione è per n. 1 tirocinio.',
      ),
    ).toBeInTheDocument();

    const first = project(1);
    expect(
      within(first).getByRole('heading', { level: 3, name: 'Progetto formativo 1' }),
    ).toBeInTheDocument();
    expect(within(first).getByLabelText(/^Progetto formativo 1: dal/)).toHaveValue('2026-12-01');
    expect(within(first).getByLabelText(/^Mansione del progetto formativo 1/)).toHaveValue('Aiuto cuoco');
    expect(within(first).getByLabelText(/^Dettaglio della mansione del progetto formativo 1/)).toHaveValue(
      'Piatti freddi',
    );
    expect(within(first).getByText('Durata: 3 mesi. Monte ore: 50 ore.')).toBeInTheDocument();
    await expectNoAxeViolations();
  });

  it('riformulazione: il nuovo progetto parte dopo il precedente, con la sua mansione', async () => {
    const user = userEvent.setup();
    const { onSubmit } = renderForm(withProject);
    onSubmit.mockResolvedValue(withProject);

    await user.click(await screen.findByRole('button', { name: 'Aggiungi progetto formativo' }));

    const second = project(2);
    expect(within(second).getByLabelText(/^Progetto formativo 2: dal/)).toHaveValue('2027-03-01');
    expect(within(second).getByLabelText(/^Progetto formativo 2: al/)).toHaveValue('2027-05-31');
    const job = within(second).getByLabelText(/^Mansione del progetto formativo 2/);
    expect(job).toHaveValue('Aiuto cuoco');
    expect(within(second).getByText('Durata e monte ore si calcolano al salvataggio.')).toBeInTheDocument();
    await user.clear(job);
    await user.type(job, ' Cuoco ');
    await user.clear(screen.getByLabelText(/^Numero della convenzione/));
    await user.type(screen.getByLabelText(/^Numero della convenzione/), ' 2026/146 ');
    await expectNoAxeViolations();
    await user.click(screen.getByRole('button', { name: 'Salva la scheda' }));

    await waitFor(() => expect(onSubmit).toHaveBeenCalled());
    expect(onSubmit.mock.calls[0][0]).toMatchObject({
      version: 4,
      agreementNumber: '2026/146',
      agreementDate: '2026-11-20',
      agreementSignedDate: '2026-11-25',
      trainingProjects: [
        { from: '2026-12-01', to: '2027-02-28', jobTitle: 'Aiuto cuoco', jobDetails: 'Piatti freddi' },
        { from: '2027-03-01', to: '2027-05-31', jobTitle: 'Cuoco', jobDetails: null },
      ],
    });
  });

  it('controlli: dentro il tirocinio, dopo la fine del precedente', async () => {
    const user = userEvent.setup();
    const { onSubmit } = renderForm(withProject);
    await user.click(await screen.findByRole('button', { name: 'Aggiungi progetto formativo' }));

    const from = within(project(2)).getByLabelText(/^Progetto formativo 2: dal/);
    await user.clear(from);
    await user.type(from, '2027-02-28');
    const to = within(project(1)).getByLabelText(/^Progetto formativo 1: al/);
    await user.clear(to);
    await user.type(to, '2027-06-30');
    await user.click(screen.getByRole('button', { name: 'Salva la scheda' }));

    expect(
      await screen.findByRole('link', {
        name: 'Progetto formativo 1: al: Il progetto finisce dopo la fine prevista del tirocinio (31/05/2027)',
      }),
    ).toBeInTheDocument();
    expect(
      screen.getByRole('link', {
        name: 'Progetto formativo 2: dal: Deve iniziare dopo la fine del progetto formativo 1 (30/06/2027)',
      }),
    ).toBeInTheDocument();
    expect(from).toHaveAttribute('aria-invalid', 'true');
    expect(onSubmit).not.toHaveBeenCalled();
    await expectNoAxeViolations();
  });

  it('senza le date del tirocinio i progetti non si salvano; il primo propone la mansione della segnalazione', async () => {
    const user = userEvent.setup();
    const { onSubmit } = renderForm({
      ...base,
      version: undefined,
      startDate: undefined,
      expectedEndDate: undefined,
    });

    expect(
      await screen.findByText(
        'La durata è quella del tirocinio: indicane le date in «Dati del tirocinio». La convenzione è per n. 1 tirocinio.',
      ),
    ).toBeInTheDocument();
    expect(screen.getByText('Nessun progetto formativo.')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'Aggiungi progetto formativo' }));
    expect(within(project(1)).getByLabelText(/^Mansione del progetto formativo 1/)).toHaveValue(
      'Aiuto cuoco',
    );
    await user.click(screen.getByRole('button', { name: 'Salva la scheda' }));

    expect(
      await screen.findByRole('link', {
        name: 'Data di inizio: Serve per i progetti formativi, che stanno dentro le date del tirocinio',
      }),
    ).toBeInTheDocument();
    expect(
      screen.getByRole('link', { name: 'Progetto formativo 1: dal: Campo obbligatorio' }),
    ).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();

    // tolto il progetto, la scheda si salva anche senza date
    await user.click(screen.getByRole('button', { name: 'Togli il progetto formativo 1' }));
    expect(screen.getByText('Nessun progetto formativo.')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'Salva la scheda' }));
    await waitFor(() => expect(onSubmit).toHaveBeenCalled());
    expect(onSubmit.mock.calls[0][0]).toMatchObject({ trainingProjects: [] });
  });

  it('l’errore del server su un progetto va sul suo campo', async () => {
    const user = userEvent.setup();
    const { onSubmit } = renderForm(withProject);
    onSubmit.mockRejectedValueOnce(
      error(400, {
        fieldErrors: [
          {
            field: 'trainingProjects[0].to',
            message: 'Il progetto finisce dopo la fine prevista del tirocinio (31/05/2027)',
          },
        ],
      }),
    );

    await user.click(await screen.findByRole('button', { name: 'Salva la scheda' }));

    expect(
      await screen.findByRole('link', {
        name: 'Progetto formativo 1: al: Il progetto finisce dopo la fine prevista del tirocinio (31/05/2027)',
      }),
    ).toBeInTheDocument();
    expect(within(project(1)).getByLabelText(/^Progetto formativo 1: al/)).toHaveAttribute(
      'aria-invalid',
      'true',
    );
  });
});
