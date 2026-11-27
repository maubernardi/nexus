import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AxiosError, AxiosHeaders } from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { searchCompanies } from '@/config/api/companyApi';
import {
  addContactAttempt,
  closeContactRequest,
  fetchContactRequest,
  takeContactRequest,
  type ContactRequestDetail,
} from '@/config/api/contactRequestApi';
import { fetchCurrentUser } from '@/config/api/userApi';
import type { CurrentUser } from '@/config/auth/types';
import { ContactRequestPage } from '@/pages/ContactRequestPage/ContactRequestPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { contactRequest } from '@/tests/fixtures/contactRequest';
import { renderRoutes } from '@/tests/renderWithProviders';

vi.mock('@/config/api/userApi', () => ({ fetchCurrentUser: vi.fn() }));
vi.mock('@/config/api/contactRequestApi', async (importOriginal) => ({
  ...(await importOriginal<object>()),
  fetchContactRequest: vi.fn(),
  takeContactRequest: vi.fn(),
  addContactAttempt: vi.fn(),
  closeContactRequest: vi.fn(),
}));
vi.mock('@/config/api/companyApi', async (importOriginal) => ({
  ...(await importOriginal<object>()),
  searchCompanies: vi.fn(),
}));

const tutor: CurrentUser = {
  id: 'tutor1',
  username: 'tutor1',
  firstName: 'Tina',
  lastName: 'Tutor',
  roles: ['TUTOR'],
};
const operator: CurrentUser = {
  id: 'operatore.cc',
  username: 'operatore.cc',
  firstName: 'Olga',
  lastName: 'Operatrice',
  roles: ['CALL_CENTER'],
};

const inCharge = (overrides: Partial<ContactRequestDetail> = {}): ContactRequestDetail => ({
  request: contactRequest(3, {
    status: 'IN_CARICO',
    assignedOperatorName: 'Olga Operatrice',
    takenAt: '2026-11-27T10:00:00Z',
    version: 1,
  }),
  attempts: [
    {
      id: 'A2',
      date: '2026-11-27',
      outcome: 'DISPONIBILE',
      note: 'Richiamano lunedì.',
      operatorName: 'Olga Operatrice',
      createdAt: '2026-11-27T11:00:00Z',
    },
    {
      id: 'A1',
      date: '2026-11-26',
      outcome: 'NON_RAGGIUNGIBILE',
      operatorName: 'Olga Operatrice',
      createdAt: '2026-11-26T11:00:00Z',
    },
  ],
  assignedToMe: true,
  ...overrides,
});

const renderPage = (path = '/richieste-contatto/R3') =>
  renderRoutes({
    routes: [
      {
        path: '/richieste-contatto/:id',
        element: (
          <main>
            <ContactRequestPage />
          </main>
        ),
      },
      {
        path: '/richieste/:id',
        element: (
          <main>
            <ContactRequestPage />
          </main>
        ),
      },
    ],
    initialPath: path,
  });

describe('<ContactRequestPage>', () => {
  beforeEach(() => {
    vi.mocked(fetchCurrentUser).mockResolvedValue(operator);
    vi.mocked(fetchContactRequest).mockResolvedValue(inCharge());
    vi.mocked(addContactAttempt).mockReset();
    vi.mocked(closeContactRequest).mockReset();
    vi.mocked(searchCompanies).mockReset();
  });

  it('il Tutor la vede in sola lettura, con i tentativi dal più recente (US-512)', async () => {
    vi.mocked(fetchCurrentUser).mockResolvedValue(tutor);
    vi.mocked(fetchContactRequest).mockResolvedValue(inCharge({ assignedToMe: false }));
    renderPage('/richieste/R3');
    expect(
      await screen.findByRole('heading', { level: 1, name: 'Richiesta di contatto n. 3' }),
    ).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Le mie richieste' })).toHaveAttribute('href', '/richieste');
    const attempts = within(screen.getByRole('region', { name: 'Tentativi di contatto' })).getAllByRole(
      'listitem',
    );
    expect(attempts[0]).toHaveTextContent('27/11/26 · Disponibile a ospitare tirocini');
    expect(attempts[0]).toHaveTextContent('Richiamano lunedì.');
    expect(attempts[1]).toHaveTextContent('26/11/26 · Non raggiungibile');
    expect(screen.queryByRole('form')).not.toBeInTheDocument();
    expect(
      screen.queryByRole('button', { name: /Segna il tentativo|Chiudi la richiesta|Prendi in carico/ }),
    ).toBeNull();
    await expectNoAxeViolations();
  });

  it('chi la ha in carico segna un tentativo: non futuro, con l’esito', async () => {
    vi.mocked(addContactAttempt).mockResolvedValue(inCharge());
    const user = userEvent.setup();
    renderPage();
    const form = await screen.findByRole('region', { name: 'Nuovo tentativo' });
    await user.click(within(form).getByRole('button', { name: 'Segna il tentativo' }));
    expect(await within(form).findByRole('alert')).toHaveTextContent('Esito: Scegli l’esito');
    expect(addContactAttempt).not.toHaveBeenCalled();

    await user.selectOptions(within(form).getByRole('combobox', { name: /Esito/ }), 'Non interessata');
    await user.type(
      within(form).getByRole('textbox', { name: /Nota per il Tutor/ }),
      ' Hanno già un tirocinante ',
    );
    await user.click(within(form).getByRole('button', { name: 'Segna il tentativo' }));
    expect(await within(form).findByRole('status')).toHaveTextContent('Tentativo segnato.');
    const call = vi.mocked(addContactAttempt).mock.calls[0][0];
    expect(call.id).toBe('R3');
    expect(call.form).toEqual(
      expect.objectContaining({ outcome: 'NON_INTERESSATA', note: 'Hanno già un tirocinante' }),
    );
    expect(call.form.date).toMatch(/^\d{4}-\d{2}-\d{2}$/);
    await expectNoAxeViolations();
  });

  it('chiusura: esito e nota obbligatori, azienda cercata e collegata, conferma con il focus', async () => {
    vi.mocked(searchCompanies).mockResolvedValue({
      companies: [
        {
          id: 'C7',
          name: 'Officina del Ponte S.r.l.',
          vatCode: '12345678901',
          legalAddress: null,
          phone: null,
          email: null,
          active: true,
          version: 0,
          updatedAt: '2026-11-27T08:00:00Z',
        },
      ],
      total: 1,
      page: 0,
      size: 10,
    });
    vi.mocked(closeContactRequest).mockResolvedValue(
      inCharge({
        request: contactRequest(3, { status: 'CHIUSA', outcome: 'DISPONIBILE', companyId: 'C7', version: 2 }),
        assignedToMe: false,
      }),
    );
    const user = userEvent.setup();
    renderPage();
    const form = await screen.findByRole('region', { name: 'Chiudi la richiesta' });
    await user.click(within(form).getByRole('button', { name: 'Chiudi la richiesta' }));
    const summary = await within(form).findByRole('alert');
    expect(summary).toHaveTextContent('Esito finale: Scegli l’esito finale');
    expect(summary).toHaveTextContent('Nota per il Tutor: Scrivi la nota per il Tutor');

    await user.selectOptions(
      within(form).getByRole('combobox', { name: /Esito finale/ }),
      'Disponibile a ospitare tirocini',
    );
    await user.type(within(form).getByRole('textbox', { name: /Nota per il Tutor/ }), 'Ospitano da gennaio.');
    // la ricerca parte dal nome scritto dal Tutor
    expect(within(form).getByRole('searchbox')).toHaveValue('Officina del Ponte');
    await user.click(within(form).getByRole('button', { name: 'Cerca' }));
    await user.click(await within(form).findByRole('radio', { name: 'Officina del Ponte S.r.l.' }));
    await user.click(within(form).getByRole('button', { name: 'Chiudi la richiesta' }));

    const confirm = await screen.findByRole('heading', { name: 'Confermi la chiusura?' });
    await waitFor(() => expect(confirm).toHaveFocus());
    expect(screen.getByText(/si chiude con l’esito «Disponibile a ospitare tirocini»/)).toBeInTheDocument();
    expect(closeContactRequest).not.toHaveBeenCalled();
    await expectNoAxeViolations();

    await user.click(screen.getByRole('button', { name: 'Sì, chiudi la richiesta' }));
    await waitFor(() =>
      expect(vi.mocked(closeContactRequest).mock.calls[0][0]).toEqual({
        id: 'R3',
        form: { version: 1, outcome: 'DISPONIBILE', note: 'Ospitano da gennaio.', companyId: 'C7' },
      }),
    );
    expect(await screen.findByRole('status')).toHaveTextContent(
      'Richiesta chiusa: il Tutor riceve l’avviso con l’esito e la nota.',
    );
  });

  it('chiusa: esito, nota e azienda collegata; niente moduli', async () => {
    vi.mocked(fetchContactRequest).mockResolvedValue({
      request: contactRequest(3, {
        status: 'CHIUSA',
        outcome: 'DISPONIBILE',
        companyId: 'C7',
        closedAt: '2026-11-27T12:00:00Z',
      }),
      closingNote: 'Ospitano da gennaio.',
      linkedCompanyName: 'Officina del Ponte S.r.l.',
      attempts: [],
      assignedToMe: false,
    });
    renderPage();
    const outcome = await screen.findByRole('region', { name: 'Esito' });
    expect(outcome).toHaveTextContent('Disponibile a ospitare tirocini');
    expect(outcome).toHaveTextContent('Ospitano da gennaio.');
    expect(within(outcome).getByRole('link', { name: 'Officina del Ponte S.r.l.' })).toHaveAttribute(
      'href',
      '/aziende/C7',
    );
    expect(screen.getByText('Chiusa: Disponibile a ospitare tirocini')).toBeInTheDocument();
    expect(screen.getByText('Nessun tentativo.')).toBeInTheDocument();
    expect(screen.queryByRole('region', { name: 'Chiudi la richiesta' })).not.toBeInTheDocument();
  });

  it('in carico a un altro: lo dice; aperta: «Prendi in carico» da qui', async () => {
    vi.mocked(fetchContactRequest).mockResolvedValue(inCharge({ assignedToMe: false }));
    const { unmount } = renderPage();
    expect(
      await screen.findByText(
        /In carico a Olga Operatrice: i tentativi e la chiusura li segna chi l’ha in carico/,
      ),
    ).toBeInTheDocument();
    expect(screen.queryByRole('region', { name: 'Nuovo tentativo' })).not.toBeInTheDocument();
    unmount();

    vi.mocked(fetchContactRequest).mockResolvedValue({
      request: contactRequest(3),
      attempts: [],
      assignedToMe: false,
    });
    vi.mocked(takeContactRequest).mockRejectedValue(
      new AxiosError('409', 'ERR_BAD_REQUEST', undefined, undefined, {
        status: 409,
        statusText: 'Conflict',
        headers: new AxiosHeaders(),
        config: { headers: new AxiosHeaders() },
        data: { status: 409, message: 'La richiesta di contatto n. 3 è già in carico a Piero Operatore' },
      }),
    );
    const user = userEvent.setup();
    renderPage();
    await user.click(await screen.findByRole('button', { name: 'Prendi in carico' }));
    expect(await screen.findByRole('alert')).toHaveTextContent('già in carico a Piero Operatore');
  });
});
