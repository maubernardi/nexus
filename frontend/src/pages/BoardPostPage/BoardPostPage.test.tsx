import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AxiosError, AxiosHeaders } from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import {
  fetchBoardPost,
  makePublicBoardPost,
  publishBoardPost,
  updateBoardPost,
  type BoardPost,
} from '@/config/api/boardPostApi';
import { fetchProjects } from '@/config/api/referenceApi';
import { BoardPostPage } from '@/pages/BoardPostPage/BoardPostPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { boardPost } from '@/tests/fixtures/boardPost';
import { renderRoutes } from '@/tests/renderWithProviders';

vi.mock('@/config/api/boardPostApi', () => ({
  fetchBoardPosts: vi.fn(),
  fetchBoardPost: vi.fn(),
  updateBoardPost: vi.fn(),
  publishBoardPost: vi.fn(),
  makePublicBoardPost: vi.fn(),
  archiveBoardPost: vi.fn(),
}));
vi.mock('@/config/api/referenceApi', () => ({
  fetchZones: vi.fn(),
  fetchJobCategories: vi.fn(),
  fetchMyProjects: vi.fn(),
  fetchProjects: vi.fn(),
}));

const conflict = (message: string) =>
  new AxiosError('Conflict', '409', undefined, undefined, {
    status: 409,
    statusText: 'Conflict',
    headers: {},
    config: { headers: new AxiosHeaders() },
    data: { status: 409, message },
  });

const renderPage = (state?: unknown) =>
  renderRoutes({
    routes: [
      {
        path: '/bacheca/:id',
        element: (
          <main>
            <BoardPostPage />
          </main>
        ),
      },
    ],
    initialPath: state === undefined ? '/bacheca/P3' : { pathname: '/bacheca/P3', state },
  });

describe('<BoardPostPage>', () => {
  beforeEach(() => {
    vi.mocked(fetchBoardPost).mockReset();
    vi.mocked(updateBoardPost).mockReset();
    vi.mocked(publishBoardPost).mockReset();
    vi.mocked(fetchProjects).mockResolvedValue([
      { id: 'PR1', code: 'GOL', name: 'GOL' },
      { id: 'PR2', code: 'POLIS', name: 'POLIS' },
    ]);
  });

  it('bozza: mansione con l’azienda, modifica anche del progetto, esito con il focus', async () => {
    const user = userEvent.setup();
    vi.mocked(fetchBoardPost).mockResolvedValue(
      boardPost(3, { createdFromTicketId: 'T9', createdFromTicketNumber: 9 }),
    );
    vi.mocked(updateBoardPost).mockResolvedValue(boardPost(3, { title: 'Magazziniere serale', version: 1 }));
    renderPage();

    expect(await screen.findByRole('heading', { level: 1, name: 'Annuncio #3' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Officina Rossi' })).toHaveAttribute('href', '/aziende/C1');
    expect(screen.getByRole('link', { name: 'n. 9' })).toHaveAttribute('href', '/segnalazioni/T9');
    const project = screen.getByRole('combobox', { name: /Progetto/ });
    await waitFor(() => expect(project).toHaveValue('PR1'));
    await expectNoAxeViolations();

    const title = screen.getByRole('textbox', { name: /^Titolo/ });
    await user.clear(title);
    await user.type(title, 'Magazziniere serale');
    await user.selectOptions(project, 'Tutti i progetti');
    await user.type(screen.getByRole('textbox', { name: /^Età minima/ }), '18');
    await user.click(screen.getByRole('button', { name: 'Salva le modifiche' }));

    const outcome = await screen.findByText('Annuncio #3 salvato.');
    await waitFor(() => expect(outcome).toHaveFocus());
    expect(vi.mocked(updateBoardPost).mock.calls[0][0]).toEqual({
      id: 'P3',
      payload: {
        title: 'Magazziniere serale',
        projectId: null,
        ageMin: 18,
        ageMax: null,
        weeklyHours: 20,
        durationMonths: 6,
        notes: null,
        version: 0,
      },
    });
  });

  it('campi vuoti omessi dall’API: il modulo li mostra vuoti', async () => {
    const omitted: Partial<BoardPost> = boardPost(3);
    delete omitted.ageMin;
    delete omitted.ageMax;
    delete omitted.notes;
    vi.mocked(fetchBoardPost).mockResolvedValue(omitted as BoardPost);
    renderPage();

    expect(await screen.findByRole('textbox', { name: /^Età minima/ })).toHaveValue('');
    expect(screen.getByRole('textbox', { name: /^Età massima/ })).toHaveValue('');
    expect(screen.getByRole('textbox', { name: /^Note/ })).toHaveValue('');
  });

  it('arrivo dalla creazione: l’esito ha il focus', async () => {
    vi.mocked(fetchBoardPost).mockResolvedValue(boardPost(3));
    renderPage({ createdNumber: 3 });

    const outcome = await screen.findByText('Bozza dell’annuncio #3 creata: controlla i dati e pubblicala.');
    await waitFor(() => expect(outcome).toHaveFocus());
  });

  it('età incoerente o fuori intervallo: errori nel riepilogo, nessuna richiesta', async () => {
    const user = userEvent.setup();
    vi.mocked(fetchBoardPost).mockResolvedValue(boardPost(3));
    renderPage();

    await user.type(await screen.findByRole('textbox', { name: /^Età minima/ }), '30');
    await user.type(screen.getByRole('textbox', { name: /^Età massima/ }), '20');
    await user.clear(screen.getByRole('textbox', { name: /^Ore settimanali/ }));
    await user.type(screen.getByRole('textbox', { name: /^Ore settimanali/ }), '80');
    await user.click(screen.getByRole('button', { name: 'Salva le modifiche' }));

    expect(
      await screen.findByRole('link', {
        name: 'Età massima: L’età massima non può essere minore della minima',
      }),
    ).toBeInTheDocument();
    expect(
      screen.getByRole('link', { name: 'Ore settimanali: Inserisci un numero da 1 a 60' }),
    ).toBeInTheDocument();
    expect(updateBoardPost).not.toHaveBeenCalled();
  });

  it('pubblica con conferma; dopo il ricaricamento l’esito resta e "Pubblica" sparisce', async () => {
    const user = userEvent.setup();
    const published = boardPost(3, { status: 'PUBLISHED', publishedAt: '2026-10-28T09:00:00Z', version: 1 });
    vi.mocked(fetchBoardPost).mockResolvedValueOnce(boardPost(3)).mockResolvedValue(published);
    vi.mocked(publishBoardPost).mockResolvedValue(published);
    renderPage();

    await user.click(await screen.findByRole('button', { name: 'Pubblica' }));
    const question = screen.getByRole('heading', { name: 'Pubblicare l’annuncio #3?' });
    await waitFor(() => expect(question).toHaveFocus());
    expect(screen.getByText(/Diventa visibile ai Tutor \(GOL\)/)).toBeInTheDocument();
    await expectNoAxeViolations();
    await user.click(screen.getByRole('button', { name: 'Sì, pubblica' }));

    const outcome = await screen.findByText('Annuncio #3 pubblicato: ora è visibile ai Tutor.');
    await waitFor(() => expect(outcome).toHaveFocus());
    expect(vi.mocked(publishBoardPost).mock.calls[0][0]).toEqual({ id: 'P3', version: 0 });
    await waitFor(() => expect(screen.queryByRole('button', { name: 'Pubblica' })).not.toBeInTheDocument());
    expect(screen.getByText(/Stato: Pubblicato/)).toBeInTheDocument();
    // pubblicato: il progetto non si cambia più dal modulo
    expect(screen.queryByRole('combobox', { name: /Progetto/ })).not.toBeInTheDocument();
    expect(screen.getByText(/Il progetto si sceglie solo nella bozza/)).toBeInTheDocument();
  });

  it('pubblicazione rifiutata: il motivo del server', async () => {
    const user = userEvent.setup();
    vi.mocked(fetchBoardPost).mockResolvedValue(boardPost(3));
    vi.mocked(publishBoardPost).mockRejectedValue(
      conflict('La mansione dell’annuncio #3 è bloccata dalla segnalazione n. 12'),
    );
    renderPage();

    await user.click(await screen.findByRole('button', { name: 'Pubblica' }));
    await user.click(screen.getByRole('button', { name: 'Sì, pubblica' }));

    expect(
      await screen.findByText(
        'Operazione non riuscita: La mansione dell’annuncio #3 è bloccata dalla segnalazione n. 12',
      ),
    ).toHaveFocus();
    expect(screen.getByRole('button', { name: 'Pubblica' })).toBeInTheDocument();
  });

  it('annullare la conferma riporta il focus al pulsante', async () => {
    const user = userEvent.setup();
    vi.mocked(fetchBoardPost).mockResolvedValue(boardPost(3));
    renderPage();

    await user.click(await screen.findByRole('button', { name: 'Archivia' }));
    expect(screen.getByRole('heading', { name: 'Archiviare l’annuncio #3?' })).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'Annulla' }));
    await waitFor(() => expect(screen.getByRole('button', { name: 'Archivia' })).toHaveFocus());
  });

  it('archiviato: sola lettura, nessuna azione', async () => {
    vi.mocked(fetchBoardPost).mockResolvedValue(boardPost(3, { status: 'ARCHIVED', notes: 'Turno serale' }));
    renderPage();

    expect(await screen.findByText('L’annuncio è archiviato: è in sola lettura.')).toBeInTheDocument();
    expect(screen.getByText('Turno serale')).toBeInTheDocument();
    expect(screen.queryByRole('textbox')).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /Pubblica|Archivia|Salva/ })).not.toBeInTheDocument();
    await expectNoAxeViolations();
  });

  it('riservato pubblicato da meno di 7 giorni: dice da quando si potrà rendere visibile a tutti', async () => {
    vi.mocked(fetchBoardPost).mockResolvedValue(
      boardPost(3, {
        status: 'PUBLISHED',
        publishedAt: '2098-12-25T09:00:00Z',
        makePublicFrom: '2099-01-01T09:00:00Z',
      }),
    );
    renderPage();

    expect(
      await screen.findByText(
        /Riservato al progetto GOL: si potrà rendere visibile a tutti dal 1 gennaio 2099/,
      ),
    ).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Rendi visibile a tutti' })).not.toBeInTheDocument();
  });

  it('dopo 7 giorni: rende visibile a tutti con conferma, poi la data dello sblocco', async () => {
    const user = userEvent.setup();
    const reserved = boardPost(3, {
      status: 'PUBLISHED',
      publishedAt: '2026-10-01T09:00:00Z',
      makePublicFrom: '2026-10-08T09:00:00Z',
    });
    // come lo manda l'API: senza progetto e senza "da quando" i campi sono omessi
    const forAll: Partial<BoardPost> = {
      ...reserved,
      madePublicAt: '2026-10-28T10:00:00Z',
      version: 1,
    };
    delete forAll.project;
    delete forAll.makePublicFrom;
    vi.mocked(fetchBoardPost)
      .mockResolvedValueOnce(reserved)
      .mockResolvedValue(forAll as BoardPost);
    vi.mocked(makePublicBoardPost).mockResolvedValue(forAll as BoardPost);
    renderPage();

    await user.click(await screen.findByRole('button', { name: 'Rendi visibile a tutti' }));
    expect(screen.getByRole('heading', { name: 'Rendere l’annuncio #3 visibile a tutti?' })).toHaveFocus();
    expect(screen.getByText(/Non sarà più riservato al progetto GOL/)).toBeInTheDocument();
    await expectNoAxeViolations();
    await user.click(screen.getByRole('button', { name: 'Sì, rendi visibile a tutti' }));

    const outcome = await screen.findByText('Annuncio #3 visibile ai Tutor di tutti i progetti.');
    await waitFor(() => expect(outcome).toHaveFocus());
    expect(vi.mocked(makePublicBoardPost).mock.calls[0][0]).toEqual({ id: 'P3', version: 0 });
    expect(await screen.findByText(/Visibile a tutti dal 28 ottobre 2026/)).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Rendi visibile a tutti' })).not.toBeInTheDocument();
  });

  it('candidatura in valutazione: link alla segnalazione che blocca l’annuncio', async () => {
    vi.mocked(fetchBoardPost).mockResolvedValue(
      boardPost(3, {
        status: 'PUBLISHED',
        publishedAt: '2026-10-27T09:00:00Z',
        candidatureTicketId: 'T41',
        candidatureTicketNumber: 41,
      }),
    );
    renderPage();

    expect(await screen.findByRole('link', { name: 'segnalazione n. 41' })).toHaveAttribute(
      'href',
      '/segnalazioni/T41',
    );
    expect(screen.getByText(/Candidatura in valutazione, in attesa della decisione/)).toBeInTheDocument();
  });
});
