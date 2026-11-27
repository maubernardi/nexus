import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AxiosError, AxiosHeaders } from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { fetchContactRequestQueue, takeContactRequest } from '@/config/api/contactRequestApi';
import { ContactRequestsPage } from '@/pages/ContactRequestsPage/ContactRequestsPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { contactRequest } from '@/tests/fixtures/contactRequest';
import { renderWithProviders } from '@/tests/renderWithProviders';

vi.mock('@/config/api/contactRequestApi', () => ({
  fetchContactRequestQueue: vi.fn(),
  takeContactRequest: vi.fn(),
}));

const renderPage = () =>
  renderWithProviders(
    <main>
      <ContactRequestsPage />
    </main>,
  );

const conflict = (message: string): AxiosError => {
  const headers = new AxiosHeaders();
  return new AxiosError('409', 'ERR_BAD_REQUEST', undefined, undefined, {
    status: 409,
    statusText: 'Conflict',
    headers,
    config: { headers },
    data: { status: 409, message },
  });
};

describe('<ContactRequestsPage>', () => {
  beforeEach(() => {
    vi.mocked(fetchContactRequestQueue).mockResolvedValue([
      contactRequest(1, {
        companyId: 'C1',
        companyName: 'Trattoria Bianchi S.n.c.',
        status: 'IN_CARICO',
        assignedOperatorName: 'Olga Operatrice',
        requestedByName: 'Ugo Tutor',
      }),
      contactRequest(2),
    ]);
    vi.mocked(takeContactRequest).mockReset();
  });

  it('dalla più vecchia, con il Tutor, quante da prendere in carico e chi ce l’ha (US-507)', async () => {
    renderPage();
    const table = await screen.findByRole('table', { name: /Richieste di contatto aperte e in carico/ });
    expect(screen.getByText('2 richieste: 1 da prendere in carico')).toBeInTheDocument();
    const [, first, second] = within(table).getAllByRole('row');
    expect(within(first).getByRole('rowheader')).toHaveTextContent('Trattoria Bianchi S.n.c.');
    expect(within(first).getAllByRole('cell')[3]).toHaveTextContent('Ugo Tutor');
    expect(within(first).getAllByRole('cell')[6]).toHaveTextContent('Olga Operatrice');
    expect(within(first).queryByRole('button')).not.toBeInTheDocument();
    expect(
      within(second).getByRole('button', { name: 'Prendi in carico la richiesta di contatto n. 2' }),
    ).toBeInTheDocument();
    await expectNoAxeViolations();
  });

  it('presa in carico: esito con il focus e versione inviata', async () => {
    vi.mocked(takeContactRequest).mockResolvedValue(
      contactRequest(2, { status: 'IN_CARICO', assignedOperatorName: 'Olga Operatrice', version: 1 }),
    );
    const user = userEvent.setup();
    renderPage();
    await user.click(await screen.findByRole('button', { name: /Prendi in carico/ }));
    const outcome = await screen.findByRole('status');
    expect(outcome).toHaveTextContent('Hai preso in carico la richiesta di contatto n. 2.');
    await waitFor(() => expect(outcome).toHaveFocus());
    expect(vi.mocked(takeContactRequest).mock.calls[0][0]).toEqual({ id: 'R2', version: 0 });
  });

  it('già presa da un altro: l’avviso dice chi ce l’ha', async () => {
    vi.mocked(takeContactRequest).mockRejectedValue(
      conflict('La richiesta di contatto n. 2 è già in carico a Piero Operatore'),
    );
    const user = userEvent.setup();
    renderPage();
    await user.click(await screen.findByRole('button', { name: /Prendi in carico/ }));
    expect(await screen.findByRole('alert')).toHaveTextContent(
      'La richiesta di contatto n. 2 è già in carico a Piero Operatore',
    );
  });

  it('nessuna richiesta: lo dice', async () => {
    vi.mocked(fetchContactRequestQueue).mockResolvedValue([]);
    renderPage();
    expect(await screen.findByText('Nessuna richiesta di contatto da seguire.')).toBeInTheDocument();
  });
});
