import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AxiosError, AxiosHeaders } from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { fetchMyBeneficiaries, type BeneficiarySummary } from '@/config/api/beneficiaryApi';
import { fetchTutorBoardPost, type TutorBoardPost } from '@/config/api/boardPostApi';
import { fetchMyProjects } from '@/config/api/referenceApi';
import { submitSpecialTicket, type Ticket } from '@/config/api/ticketApi';
import { CandidaturePage } from '@/pages/CandidaturePage/CandidaturePage';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderRoutes } from '@/tests/renderWithProviders';

vi.mock('@/config/api/boardPostApi', () => ({ fetchTutorBoardPost: vi.fn() }));
vi.mock('@/config/api/beneficiaryApi', () => ({ fetchMyBeneficiaries: vi.fn() }));
vi.mock('@/config/api/referenceApi', () => ({
  fetchZones: vi.fn(),
  fetchJobCategories: vi.fn(),
  fetchMyProjects: vi.fn(),
  fetchProjects: vi.fn(),
}));
vi.mock('@/config/api/ticketApi', () => ({ submitSpecialTicket: vi.fn() }));

const GOL = { id: 'PR1', code: 'GOL', name: 'GOL' };
const POLIS = { id: 'PR2', code: 'POLIS', name: 'POLIS' };

// come lo manda l'API: i campi vuoti (età, progetto se aperto a tutti) sono omessi
const post = (overrides: Partial<TutorBoardPost> = {}): TutorBoardPost => ({
  id: 'P4',
  number: 4,
  jobCategory: { id: 'J1', code: 'MAG', name: 'Magazziniere' },
  zone: { id: 'Z1', code: 'NORD', name: 'Firenze Nord' },
  weeklyHours: 30,
  publishedAt: '2026-10-27T09:00:00Z',
  candidatureInProgress: false,
  ...overrides,
});

const beneficiary = (id: string, lastName: string, openTicketNumber?: number): BeneficiarySummary => ({
  id,
  firstName: 'Mario',
  lastName,
  birthYear: 1998,
  residenceZoneName: 'Firenze Nord',
  ...(openTicketNumber ? { openTicketNumber } : {}),
});

const created: Ticket = {
  id: 'T41',
  number: 41,
  type: 'SPECIAL',
  status: 'IN_LAVORAZIONE',
  fastTrack: true,
  version: 0,
  beneficiary: beneficiary('B2', 'Verdi'),
  project: GOL,
  requestedJobCategory: { id: 'J1', code: 'MAG', name: 'Magazziniere' },
  createdAt: '2026-10-28T10:00:00Z',
};

const conflict = (message: string) =>
  new AxiosError('Conflict', '409', undefined, undefined, {
    status: 409,
    statusText: 'Conflict',
    headers: {},
    config: { headers: new AxiosHeaders() },
    data: { status: 409, message },
  });

const renderPage = () =>
  renderRoutes({
    routes: [
      {
        path: '/bacheca/:id/candidatura',
        element: (
          <main>
            <CandidaturePage />
          </main>
        ),
      },
    ],
    initialPath: '/bacheca/P4/candidatura',
  });

describe('<CandidaturePage>', () => {
  beforeEach(() => {
    vi.mocked(fetchTutorBoardPost).mockReset();
    vi.mocked(submitSpecialTicket).mockReset();
    vi.mocked(fetchMyBeneficiaries).mockResolvedValue([
      beneficiary('B1', 'Rossi', 12),
      beneficiary('B2', 'Verdi'),
    ]);
    vi.mocked(fetchMyProjects).mockResolvedValue([GOL, POLIS]);
  });

  it('annuncio riservato: progetto fisso, beneficiari con segnalazione aperta non scelti, esito con il focus', async () => {
    const user = userEvent.setup();
    vi.mocked(fetchTutorBoardPost).mockResolvedValue(post({ project: GOL }));
    vi.mocked(submitSpecialTicket).mockResolvedValue(created);
    renderPage();

    expect(
      await screen.findByRole('heading', { level: 1, name: 'Candidatura all’annuncio #4' }),
    ).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Annuncio #4 · Magazziniere' })).toBeInTheDocument();
    expect(
      await screen.findByText('Progetto della segnalazione: GOL (l’annuncio è riservato a questo progetto).'),
    ).toBeInTheDocument();
    expect(screen.queryByRole('combobox', { name: /Progetto/ })).not.toBeInTheDocument();
    expect(
      screen.getByRole('option', { name: /Rossi Mario \(1998\) – segnalazione n. 12 aperta/ }),
    ).toBeDisabled();
    // l'unico beneficiario disponibile è già scelto
    expect(screen.getByRole('combobox', { name: /Beneficiario/ })).toHaveValue('B2');
    await expectNoAxeViolations();

    await user.click(screen.getByRole('button', { name: 'Invia la candidatura' }));

    const heading = await screen.findByRole('heading', { name: 'Candidatura inviata: segnalazione n. 41' });
    await waitFor(() => expect(heading).toHaveFocus());
    expect(vi.mocked(submitSpecialTicket).mock.calls[0][0]).toEqual({
      boardPostId: 'P4',
      beneficiaryId: 'B2',
      projectId: null,
    });
    expect(screen.getByRole('link', { name: 'Apri la segnalazione' })).toHaveAttribute(
      'href',
      '/segnalazioni/T41',
    );
  });

  it('annuncio aperto a tutti: il progetto si sceglie ed è obbligatorio', async () => {
    const user = userEvent.setup();
    vi.mocked(fetchTutorBoardPost).mockResolvedValue(post());
    vi.mocked(submitSpecialTicket).mockResolvedValue(created);
    renderPage();

    const project = await screen.findByRole('combobox', { name: /Progetto/ });
    await user.click(screen.getByRole('button', { name: 'Invia la candidatura' }));
    expect(await screen.findByRole('link', { name: 'Progetto: Campo obbligatorio' })).toBeInTheDocument();
    expect(submitSpecialTicket).not.toHaveBeenCalled();

    await user.selectOptions(project, 'POLIS');
    await user.click(screen.getByRole('button', { name: 'Invia la candidatura' }));
    await screen.findByRole('heading', { name: 'Candidatura inviata: segnalazione n. 41' });
    expect(vi.mocked(submitSpecialTicket).mock.calls[0][0]).toMatchObject({ projectId: 'PR2' });
  });

  it('annuncio appena bloccato da un altro Tutor: il motivo del server con il focus', async () => {
    const user = userEvent.setup();
    vi.mocked(fetchTutorBoardPost).mockResolvedValue(post({ project: GOL }));
    vi.mocked(submitSpecialTicket).mockRejectedValue(
      conflict('L’annuncio #4 ha già una candidatura in valutazione'),
    );
    renderPage();

    await user.click(await screen.findByRole('button', { name: 'Invia la candidatura' }));

    expect(
      await screen.findByText('Candidatura non inviata: L’annuncio #4 ha già una candidatura in valutazione'),
    ).toHaveFocus();
  });

  it('candidatura già in valutazione: nessun modulo', async () => {
    vi.mocked(fetchTutorBoardPost).mockResolvedValue(post({ candidatureInProgress: true }));
    renderPage();

    expect(
      await screen.findByRole('heading', { name: 'Candidatura già in valutazione' }),
    ).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Invia la candidatura' })).not.toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Torna alla bacheca' })).toHaveAttribute('href', '/bacheca');
  });

  it('annuncio non più in bacheca o di un altro progetto', async () => {
    vi.mocked(fetchTutorBoardPost).mockRejectedValue(
      new AxiosError('Not Found', '404', undefined, undefined, {
        status: 404,
        statusText: 'Not Found',
        headers: {},
        config: { headers: new AxiosHeaders() },
        data: { status: 404, message: 'Annuncio non trovato' },
      }),
    );
    renderPage();

    expect(
      await screen.findByRole('heading', { level: 1, name: 'Annuncio non trovato' }),
    ).toBeInTheDocument();
  });
});
