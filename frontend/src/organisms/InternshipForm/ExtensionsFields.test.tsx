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

// tirocinio avviato, come lo manda l'API: i campi vuoti sono omessi
const started: Internship = {
  version: 7,
  companyName: 'Demo Ristorazione',
  atecoCodes: [],
  internshipTypes: [],
  startDate: '2026-12-01',
  expectedEndDate: '2027-05-31',
  currentEndDate: '2027-05-31',
  jobSlotTitle: 'Aiuto cuoco',
  companyTutors: [],
  suspensions: [],
  agreementNumber: '2026/145',
  agreementDate: '2026-11-20',
  agreementSignedDate: '2026-11-25',
  trainingProjects: [],
  extensions: [],
  extensible: true,
  firstSlotStart: '09:00:00',
  firstSlotEnd: '13:00:00',
  secondSlotStart: '14:00:00',
  secondSlotEnd: '18:00:00',
  excludeHolidays: true,
  schedule: [],
  missingForStart: [],
};

const withExtension: Internship = {
  ...started,
  currentEndDate: '2027-09-30',
  extensions: [
    {
      number: 1,
      agreementNumber: '2027/200',
      agreementDate: '2027-06-01',
      agreementSignedDate: '2027-06-10',
      from: '2027-06-15',
      to: '2027-09-30',
      trainingProjects: [
        {
          number: 1,
          from: '2027-06-15',
          to: '2027-09-30',
          jobTitle: 'Cuoco',
          jobDetails: 'Cucina calda',
          months: 4,
          totalMinutes: 3600,
        },
      ],
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

const extension = (k: number): HTMLElement => screen.getByRole('group', { name: `Proroga ${k}` });
const save = () => screen.getByRole('button', { name: 'Salva la scheda' });

describe('<ExtensionsFields>', () => {
  it('prima dell’avvio, senza proroghe, la sezione non c’è', async () => {
    renderForm({ ...started, extensible: false });

    expect(await screen.findByRole('heading', { level: 2, name: 'Convenzione' })).toBeInTheDocument();
    expect(screen.queryByRole('heading', { name: 'Proroghe' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Aggiungi proroga' })).not.toBeInTheDocument();
  });

  it('nuova proroga: parte dopo la fine prevista, il progetto dentro le sue date; la convenzione dice la nuova fine', async () => {
    const user = userEvent.setup();
    const initialProject = {
      number: 1,
      from: '2026-12-01',
      to: '2027-05-31',
      jobTitle: 'Commis di cucina',
      months: 6,
      totalMinutes: 6000,
    };
    const { onSubmit } = renderForm({ ...started, trainingProjects: [initialProject] });
    onSubmit.mockResolvedValue(withExtension);

    expect(await screen.findByRole('heading', { level: 2, name: 'Proroghe' })).toBeInTheDocument();
    expect(screen.getByText('Nessuna proroga.')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'Aggiungi proroga' }));

    const first = extension(1);
    expect(within(first).getByRole('heading', { level: 3, name: 'Proroga 1' })).toBeInTheDocument();
    // proposta: il giorno dopo la fine prevista, ma si può spostare più avanti
    const from = within(first).getByLabelText(/^Proroga 1: dal/);
    expect(from).toHaveValue('2027-06-01');
    await user.clear(from);
    await user.type(from, '2027-06-15');
    await user.type(within(first).getByLabelText(/^Proroga 1: al/), '2027-09-30');
    await user.type(within(first).getByLabelText(/^Proroga 1: numero della convenzione/), ' 2027/200 ');
    await user.type(within(first).getByLabelText(/^Proroga 1: data della convenzione/), '2027-06-01');
    await user.type(within(first).getByLabelText(/^Proroga 1: data di stipula/), '2027-06-10');
    expect(screen.getByText(/Con le proroghe il tirocinio finisce il 30\/09\/2027\./)).toBeInTheDocument();

    expect(within(first).getByText('Nessun progetto formativo per la proroga 1.')).toBeInTheDocument();
    await user.click(
      within(first).getByRole('button', { name: 'Aggiungi progetto formativo alla proroga 1' }),
    );
    const project = within(first).getByRole('group', { name: 'Proroga 1, progetto formativo 1' });
    expect(
      within(project).getByRole('heading', { level: 4, name: 'Proroga 1, progetto formativo 1' }),
    ).toBeInTheDocument();
    expect(within(project).getByLabelText(/^Proroga 1, progetto formativo 1: dal/)).toHaveValue('2027-06-15');
    expect(within(project).getByLabelText(/^Proroga 1, progetto formativo 1: al/)).toHaveValue('2027-09-30');
    // la mansione dell'ultimo progetto della convenzione precedente
    expect(within(project).getByLabelText(/^Mansione del progetto formativo 1 della proroga 1/)).toHaveValue(
      'Commis di cucina',
    );
    await expectNoAxeViolations();
    await user.click(save());

    await waitFor(() => expect(onSubmit).toHaveBeenCalled());
    expect(onSubmit.mock.calls[0][0]).toMatchObject({
      version: 7,
      trainingProjects: [{ from: '2026-12-01', to: '2027-05-31', jobTitle: 'Commis di cucina' }],
      extensions: [
        {
          agreementNumber: '2027/200',
          agreementDate: '2027-06-01',
          agreementSignedDate: '2027-06-10',
          from: '2027-06-15',
          to: '2027-09-30',
          trainingProjects: [
            { from: '2027-06-15', to: '2027-09-30', jobTitle: 'Commis di cucina', jobDetails: null },
          ],
        },
      ],
    });
  });

  it('proroga salvata: dati, progetto con mesi e monte ore; si toglie', async () => {
    const user = userEvent.setup();
    const { onSubmit } = renderForm(withExtension);
    onSubmit.mockResolvedValue(started);

    const first = await screen.findByRole('group', { name: 'Proroga 1' });
    expect(within(first).getByLabelText(/^Proroga 1: numero della convenzione/)).toHaveValue('2027/200');
    expect(within(first).getByLabelText(/^Proroga 1: dal/)).toHaveValue('2027-06-15');
    expect(within(first).getByText('Durata: 4 mesi. Monte ore: 60 ore.')).toBeInTheDocument();

    await user.click(within(first).getByRole('button', { name: 'Togli la proroga 1' }));
    expect(screen.getByText('Nessuna proroga.')).toBeInTheDocument();
    await user.click(save());
    await waitFor(() => expect(onSubmit).toHaveBeenCalled());
    expect(onSubmit.mock.calls[0][0]).toMatchObject({ extensions: [] });
  });

  it('controlli: dopo la convenzione precedente, progetti dentro la proroga, fine prevista prima della proroga', async () => {
    const user = userEvent.setup();
    const { onSubmit } = renderForm(withExtension);

    const first = await screen.findByRole('group', { name: 'Proroga 1' });
    const project = within(first).getByRole('group', { name: 'Proroga 1, progetto formativo 1' });
    const projectFrom = within(project).getByLabelText(/^Proroga 1, progetto formativo 1: dal/);
    await user.clear(projectFrom);
    await user.type(projectFrom, '2027-06-14');
    const end = screen.getByLabelText(/^Data di fine prevista/);
    await user.clear(end);
    await user.type(end, '2027-06-20');
    await user.click(screen.getByRole('button', { name: 'Aggiungi proroga' }));
    const second = extension(2);
    expect(within(second).getByLabelText(/^Proroga 2: dal/)).toHaveValue('2027-10-01');
    const secondFrom = within(second).getByLabelText(/^Proroga 2: dal/);
    await user.clear(secondFrom);
    await user.type(secondFrom, '2027-09-30');
    await user.click(save());

    expect(
      await screen.findByRole('link', {
        name: 'Data di fine prevista: Deve finire prima dell’inizio della proroga 1 (15/06/2027)',
      }),
    ).toBeInTheDocument();
    // proroga 1 già salvata: l'errore è solo sulla fine prevista, come nel server
    expect(screen.queryByRole('link', { name: /^Proroga 1: dal/ })).not.toBeInTheDocument();
    expect(
      screen.getByRole('link', {
        name: 'Proroga 1, progetto formativo 1: dal: Il progetto inizia prima della proroga (15/06/2027)',
      }),
    ).toBeInTheDocument();
    expect(
      screen.getByRole('link', {
        name: 'Proroga 2: dal: Deve iniziare dopo la fine della proroga 1 (30/09/2027)',
      }),
    ).toBeInTheDocument();
    expect(
      screen.getByRole('link', { name: 'Proroga 2: numero della convenzione: Campo obbligatorio' }),
    ).toBeInTheDocument();
    expect(secondFrom).toHaveAttribute('aria-invalid', 'true');
    expect(onSubmit).not.toHaveBeenCalled();
    await expectNoAxeViolations();
  });

  it('proroga nuova che inizia alla fine della convenzione: l’errore è solo sul suo inizio', async () => {
    const user = userEvent.setup();
    const { onSubmit } = renderForm(started);
    await user.click(await screen.findByRole('button', { name: 'Aggiungi proroga' }));
    const from = within(extension(1)).getByLabelText(/^Proroga 1: dal/);
    await user.clear(from);
    await user.type(from, '2027-05-31');
    await user.click(save());

    expect(
      await screen.findByRole('link', {
        name: 'Proroga 1: dal: Deve iniziare dopo la fine della convenzione (31/05/2027)',
      }),
    ).toBeInTheDocument();
    expect(screen.queryByRole('link', { name: /^Data di fine prevista/ })).not.toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('l’errore del server su un progetto della proroga va sul suo campo', async () => {
    const user = userEvent.setup();
    const { onSubmit } = renderForm(withExtension);
    onSubmit.mockRejectedValueOnce(
      error(400, {
        fieldErrors: [
          {
            field: 'extensions[0].trainingProjects[0].to',
            message: 'Il progetto finisce dopo la fine della proroga (30/09/2027)',
          },
        ],
      }),
    );

    await user.click(await screen.findByRole('button', { name: 'Salva la scheda' }));

    expect(
      await screen.findByRole('link', {
        name: 'Proroga 1, progetto formativo 1: al: Il progetto finisce dopo la fine della proroga (30/09/2027)',
      }),
    ).toBeInTheDocument();
    expect(screen.getByLabelText(/^Proroga 1, progetto formativo 1: al/)).toHaveAttribute(
      'aria-invalid',
      'true',
    );
  });

  it('dopo la chiusura le proroghe restano in sola lettura e si salvano come sono', async () => {
    const user = userEvent.setup();
    const { onSubmit } = renderForm({ ...withExtension, extensible: false });
    onSubmit.mockResolvedValue(withExtension);

    expect(await screen.findByRole('heading', { level: 2, name: 'Proroghe' })).toBeInTheDocument();
    expect(
      screen.getByText('Le proroghe si registrano a tirocinio avviato e non ancora chiuso.'),
    ).toBeInTheDocument();
    expect(screen.getByRole('heading', { level: 3, name: 'Proroga 1' })).toBeInTheDocument();
    expect(
      screen.getByText(
        'Convenzione n. 2027/200 del 01/06/2027, stipulata il 10/06/2027: dal 15/06/2027 al 30/09/2027.',
      ),
    ).toBeInTheDocument();
    expect(
      screen.getByText('Progetto formativo 1: dal 15/06/2027 al 30/09/2027, Cuoco.'),
    ).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Aggiungi proroga' })).not.toBeInTheDocument();
    expect(screen.queryByLabelText(/^Proroga 1: dal/)).not.toBeInTheDocument();
    await expectNoAxeViolations();

    await user.click(save());
    await waitFor(() => expect(onSubmit).toHaveBeenCalled());
    expect(onSubmit.mock.calls[0][0]).toMatchObject({
      extensions: [{ agreementNumber: '2027/200', from: '2027-06-15', to: '2027-09-30' }],
    });
  });
});
