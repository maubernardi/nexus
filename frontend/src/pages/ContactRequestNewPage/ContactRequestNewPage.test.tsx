import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { fetchSimilarCompanies } from '@/config/api/companyApi';
import { createContactRequest } from '@/config/api/contactRequestApi';
import { ContactRequestNewPage } from '@/pages/ContactRequestNewPage/ContactRequestNewPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { contactRequest } from '@/tests/fixtures/contactRequest';
import { renderRoutes } from '@/tests/renderWithProviders';

vi.mock('@/config/api/referenceApi', () => ({
  fetchZones: vi.fn().mockResolvedValue([
    { id: 'Z1', code: 'RIFREDI', name: 'Rifredi' },
    { id: 'Z2', code: 'NOVOLI', name: 'Novoli' },
  ]),
  fetchBusinessCategories: vi.fn().mockResolvedValue([{ id: 'K14', code: 'BAR_PUB', name: 'Bar / Pub' }]),
}));
vi.mock('@/config/api/companyApi', async (importOriginal) => ({
  ...(await importOriginal<object>()),
  fetchSimilarCompanies: vi.fn(),
}));
vi.mock('@/config/api/contactRequestApi', () => ({ createContactRequest: vi.fn() }));

const renderPage = () =>
  renderRoutes({
    routes: [
      {
        path: '/richieste/nuova',
        element: (
          <main>
            <ContactRequestNewPage />
          </main>
        ),
      },
      { path: '/richieste', element: <p>Le mie richieste</p> },
    ],
    initialPath: '/richieste/nuova',
  });

/** Compila nome e zona; la categoria resta vuota. */
const fill = async (user: ReturnType<typeof userEvent.setup>, name: string): Promise<void> => {
  await user.type(screen.getByRole('textbox', { name: /Nome dell’azienda/ }), name);
  const zone = screen.getByRole('combobox', { name: /Zona/ });
  await waitFor(() => expect(zone).toBeEnabled());
  await user.selectOptions(zone, 'Rifredi');
};

describe('<ContactRequestNewPage>', () => {
  beforeEach(() => {
    vi.mocked(fetchSimilarCompanies).mockReset();
    vi.mocked(createContactRequest).mockReset();
    vi.mocked(createContactRequest).mockResolvedValue(contactRequest(5));
  });

  it('nome e zona obbligatori: il riepilogo li indica e non parte niente (US-507)', async () => {
    const user = userEvent.setup();
    renderPage();
    await user.click(await screen.findByRole('button', { name: 'Invia la richiesta' }));
    const summary = await screen.findByRole('alert');
    expect(summary).toHaveTextContent('Nome dell’azienda: Scrivi il nome dell’azienda');
    expect(summary).toHaveTextContent('Zona: Scegli la zona');
    expect(fetchSimilarCompanies).not.toHaveBeenCalled();
    expect(createContactRequest).not.toHaveBeenCalled();
    await expectNoAxeViolations();
  });

  it('senza nomi simili la richiesta parte subito e si torna a «Le mie richieste»', async () => {
    vi.mocked(fetchSimilarCompanies).mockResolvedValue([]);
    const user = userEvent.setup();
    const { router } = renderPage();
    await fill(user, '  Officina del Ponte ');
    await user.click(screen.getByRole('button', { name: 'Invia la richiesta' }));

    await waitFor(() => expect(router.state.location.pathname).toBe('/richieste'));
    expect(router.state.location.state).toEqual({ sent: 5 });
    expect(fetchSimilarCompanies).toHaveBeenCalledWith('Officina del Ponte');
    expect(vi.mocked(createContactRequest).mock.calls[0][0]).toEqual({
      companyName: 'Officina del Ponte',
      zoneId: 'Z1',
      businessCategoryId: null,
    });
  });

  it('nomi simili prima dell’invio: con il focus, e si conferma che l’azienda è nuova', async () => {
    vi.mocked(fetchSimilarCompanies).mockResolvedValue([
      { id: 'C1', name: 'Trattoria Bianchi S.n.c.', zone: { id: 'Z1', code: 'RIFREDI', name: 'Rifredi' } },
    ]);
    const user = userEvent.setup();
    renderPage();
    await fill(user, 'Trattoria Bianki');
    await user.click(screen.getByRole('button', { name: 'Invia la richiesta' }));

    const heading = await screen.findByRole('heading', { name: 'Aziende con un nome simile' });
    await waitFor(() => expect(heading).toHaveFocus());
    const region = screen.getByRole('region', { name: 'Aziende con un nome simile' });
    expect(region).toHaveTextContent('In NEXUS c’è già un’azienda con un nome simile a «Trattoria Bianki».');
    expect(within(region).getByRole('link', { name: 'Trattoria Bianchi S.n.c.' })).toHaveAttribute(
      'href',
      '/aziende/C1',
    );
    // finché non si sceglie, non parte niente e il pulsante d'invio non c'è
    expect(createContactRequest).not.toHaveBeenCalled();
    expect(screen.queryByRole('button', { name: 'Invia la richiesta' })).not.toBeInTheDocument();
    await expectNoAxeViolations();

    await user.click(screen.getByRole('button', { name: 'No, è un’azienda nuova: invia la richiesta' }));
    await waitFor(() =>
      expect(vi.mocked(createContactRequest).mock.calls[0][0]).toEqual({
        companyName: 'Trattoria Bianki',
        zoneId: 'Z1',
        businessCategoryId: null,
      }),
    );
  });

  it('nomi simili: si chiede di contattare quella già presente', async () => {
    vi.mocked(fetchSimilarCompanies).mockResolvedValue([{ id: 'C1', name: 'Trattoria Bianchi S.n.c.' }]);
    const user = userEvent.setup();
    renderPage();
    await fill(user, 'Trattoria Bianki');
    await user.click(screen.getByRole('button', { name: 'Invia la richiesta' }));
    await user.click(
      await screen.findByRole('button', { name: 'Chiedi di contattare questa Trattoria Bianchi S.n.c.' }),
    );
    await waitFor(() =>
      expect(vi.mocked(createContactRequest).mock.calls[0][0]).toEqual({ companyId: 'C1' }),
    );
  });

  it('cambiato il nome, i nomi simili spariscono; «Correggi il nome» riporta al campo', async () => {
    vi.mocked(fetchSimilarCompanies).mockResolvedValue([{ id: 'C1', name: 'Trattoria Bianchi S.n.c.' }]);
    const user = userEvent.setup();
    renderPage();
    await fill(user, 'Trattoria Bianki');
    await user.click(screen.getByRole('button', { name: 'Invia la richiesta' }));
    await user.click(await screen.findByRole('button', { name: 'Correggi il nome' }));
    expect(screen.getByRole('textbox', { name: /Nome dell’azienda/ })).toHaveFocus();
    expect(screen.queryByRole('heading', { name: 'Aziende con un nome simile' })).not.toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Invia la richiesta' }));
    await screen.findByRole('heading', { name: 'Aziende con un nome simile' });
    await user.type(screen.getByRole('textbox', { name: /Nome dell’azienda/ }), ' Due');
    expect(screen.queryByRole('heading', { name: 'Aziende con un nome simile' })).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Invia la richiesta' })).toBeInTheDocument();
  });
});
