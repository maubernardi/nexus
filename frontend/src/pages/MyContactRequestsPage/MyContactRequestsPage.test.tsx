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
        companyId: 'C1',
        companyName: 'Trattoria Bianchi S.n.c.',
        status: 'IN_CARICO',
        assignedOperatorName: 'Olga Operatrice',
      }),
    ]);
  });

  it('le richieste con lo stato in testo e l’operatore; l’azienda presente apre la scheda (US-507)', async () => {
    renderPage();
    const table = await screen.findByRole('table', { name: /Le tue richieste di contatto/ });
    const [header, nuova, presente] = within(table).getAllByRole('row');
    expect(
      within(header)
        .getAllByRole('columnheader')
        .map((h) => h.textContent),
    ).toEqual(['N.', 'Azienda', 'Zona', 'Categoria', 'Inviata il', 'Stato', 'Operatore']);
    expect(within(nuova).getByRole('rowheader')).toHaveTextContent('Officina del Ponte');
    expect(within(nuova).getByText('Azienda nuova, non ancora in NEXUS')).toHaveClass('sr-only');
    expect(within(nuova).getAllByRole('cell')[4]).toHaveTextContent('Aperta');
    expect(within(presente).getByRole('link', { name: 'Trattoria Bianchi S.n.c.' })).toHaveAttribute(
      'href',
      '/aziende/C1',
    );
    expect(within(presente).getAllByRole('cell')[4]).toHaveTextContent('In carico');
    expect(within(presente).getAllByRole('cell')[5]).toHaveTextContent('Olga Operatrice');
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
