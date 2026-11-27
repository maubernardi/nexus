import { screen, waitFor, within } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { fetchMyContactRequests } from '@/config/api/contactRequestApi';
import { MyContactRequestsPage } from '@/pages/MyContactRequestsPage/MyContactRequestsPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { contactRequest } from '@/tests/fixtures/contactRequest';
import { renderRoutes } from '@/tests/renderWithProviders';

vi.mock('@/config/api/contactRequestApi', () => ({ fetchMyContactRequests: vi.fn() }));

const renderPage = (state?: unknown) =>
  renderRoutes({
    routes: [
      {
        path: '/richieste',
        element: (
          <main>
            <MyContactRequestsPage />
          </main>
        ),
      },
    ],
    initialPath: { pathname: '/richieste', state },
  });

describe('<MyContactRequestsPage>', () => {
  beforeEach(() => {
    vi.mocked(fetchMyContactRequests).mockResolvedValue([
      contactRequest(3),
      contactRequest(2, {
        companyName: 'Bar Pasticceria Stella',
        status: 'IN_CARICO',
        assignedOperatorName: 'Olga Operatrice',
      }),
      // chiusa e collegata all'anagrafica dall'operatore (US-512): il nome apre la scheda
      contactRequest(1, {
        companyId: 'C1',
        companyName: 'Trattoria Bianchi',
        status: 'CHIUSA',
        outcome: 'NON_INTERESSATA',
      }),
    ]);
  });

  it('le richieste con lo stato in testo e l’operatore; collegata all’anagrafica apre la scheda (US-507)', async () => {
    renderPage();
    const table = await screen.findByRole('table', { name: /Le tue richieste di contatto/ });
    const [header, nuova, inCarico, collegata] = within(table).getAllByRole('row');
    expect(
      within(header)
        .getAllByRole('columnheader')
        .map((h) => h.textContent),
    ).toEqual(['N.', 'Azienda', 'Zona', 'Categoria', 'Inviata il', 'Stato', 'Operatore']);
    expect(within(nuova).getByRole('rowheader')).toHaveTextContent('Officina del Ponte');
    expect(within(nuova).getAllByRole('cell')[4]).toHaveTextContent('Aperta');
    expect(within(inCarico).getAllByRole('cell')[4]).toHaveTextContent('In carico');
    expect(within(inCarico).getAllByRole('cell')[5]).toHaveTextContent('Olga Operatrice');
    expect(within(inCarico).queryByRole('link', { name: 'Bar Pasticceria Stella' })).not.toBeInTheDocument();
    // il numero apre la pagina della richiesta (US-512)
    expect(
      within(inCarico).getByRole('link', { name: 'Apri la richiesta di contatto n. 2' }),
    ).toHaveAttribute('href', '/richieste/R2');
    expect(within(collegata).getByRole('link', { name: 'Trattoria Bianchi' })).toHaveAttribute(
      'href',
      '/aziende/C1',
    );
    expect(within(collegata).getAllByRole('cell')[4]).toHaveTextContent('Chiusa: Non interessata');
    expect(screen.getByRole('link', { name: 'Chiedi di contattare un’azienda nuova' })).toHaveAttribute(
      'href',
      '/richieste/nuova',
    );
    await expectNoAxeViolations();
  });

  it('dopo l’invio: l’esito con il numero, con il focus', async () => {
    renderPage({ sent: 4 });
    const sent = await screen.findByRole('status');
    expect(sent).toHaveTextContent(
      'Richiesta di contatto n. 4 inviata: il Call Center la prenderà in carico.',
    );
    await waitFor(() => expect(sent).toHaveFocus());
  });

  it('nessuna richiesta: lo dice', async () => {
    vi.mocked(fetchMyContactRequests).mockResolvedValue([]);
    renderPage();
    expect(await screen.findByText('Non hai ancora chiesto di contattare un’azienda.')).toBeInTheDocument();
  });
});
