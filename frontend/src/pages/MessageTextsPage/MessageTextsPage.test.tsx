import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AxiosError, AxiosHeaders } from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import {
  fetchMessageTexts,
  resetMessageText,
  updateMessageText,
  type MessageText,
} from '@/config/api/messageTextApi';
import { MessageTextsPage } from '@/pages/MessageTextsPage/MessageTextsPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderWithProviders } from '@/tests/renderWithProviders';

vi.mock('@/config/api/messageTextApi', () => ({
  fetchMessageTexts: vi.fn(),
  updateMessageText: vi.fn(),
  resetMessageText: vi.fn(),
}));

const proposal = (overrides: Partial<MessageText['parts'][number]> = {}): MessageText => ({
  key: 'PROPOSAL_SENT',
  notification: true,
  placeholders: ['numero'],
  parts: [
    {
      part: 'APP',
      text: 'C’è una proposta per la segnalazione n. {numero}.',
      defaultText: 'C’è una proposta per la segnalazione n. {numero}.',
      custom: false,
    },
    {
      part: 'EMAIL_SUBJECT',
      text: 'Nuova proposta di tirocinio',
      defaultText: 'Nuova proposta di tirocinio',
      custom: false,
      ...overrides,
    },
    {
      part: 'EMAIL_BODY',
      text: 'Proposta n. {numero}.',
      defaultText: 'Proposta n. {numero}.',
      custom: false,
    },
  ],
});

const notice: MessageText = {
  key: 'EMAIL_CHANGE_NOTICE',
  notification: false,
  placeholders: [],
  parts: [
    {
      part: 'EMAIL_SUBJECT',
      text: 'Il tuo indirizzo email è stato cambiato',
      defaultText: 'Il tuo indirizzo email è stato cambiato',
      custom: false,
    },
    { part: 'EMAIL_BODY', text: 'Ciao.', defaultText: 'Ciao.', custom: false },
  ],
};

/** Apre il messaggio e restituisce il suo riquadro: le etichette dei campi si ripetono da un messaggio all'altro. */
const open = async (user: ReturnType<typeof userEvent.setup>, title: string): Promise<HTMLElement> => {
  const summary = await screen.findByText(title, { selector: 'summary span' });
  await user.click(summary);
  return summary.closest('details') as HTMLElement;
};

const renderPage = () =>
  renderWithProviders(
    <main>
      <MessageTextsPage />
    </main>,
  );

describe('<MessageTextsPage>', () => {
  beforeEach(() => {
    vi.mocked(fetchMessageTexts).mockResolvedValue([proposal(), notice]);
    vi.mocked(updateMessageText).mockReset();
    vi.mocked(resetMessageText).mockReset();
  });

  it('due gruppi; ogni messaggio dice a chi va e quando, con le sue parti e l’anteprima composta', async () => {
    const user = userEvent.setup();
    renderPage();

    expect(await screen.findByRole('heading', { level: 2, name: 'Notifiche' })).toBeInTheDocument();
    expect(
      screen.getByRole('heading', { level: 2, name: 'Email del cambio di indirizzo' }),
    ).toBeInTheDocument();
    const box = await open(user, 'Proposta di tirocinio');
    expect(
      screen.getByText('Al Tutor, quando il Call Center propone un’azienda per la segnalazione.'),
    ).toBeVisible();
    expect(within(box).getByRole('textbox', { name: /Testo della notifica nell’app/ })).toHaveValue(
      'C’è una proposta per la segnalazione n. {numero}.',
    );
    expect(
      screen.getByText('PORTALE NEXUS - Segnalazione n. 12 - Nuova proposta di tirocinio'),
    ).toBeVisible();
    expect(screen.getByText(/C’è una proposta per la segnalazione n\. 12\./)).toBeVisible();
    await expectNoAxeViolations();
  });

  it('salva un testo e lo dice; l’errore del server finisce sul campo', async () => {
    const user = userEvent.setup();
    vi.mocked(updateMessageText)
      .mockRejectedValueOnce(
        new AxiosError('Bad Request', '400', undefined, undefined, {
          status: 400,
          statusText: 'Bad Request',
          headers: {},
          config: { headers: new AxiosHeaders() },
          data: {
            status: 400,
            fieldErrors: [
              { field: 'text', message: 'Segnaposto sconosciuto {nome}: si possono usare {numero}' },
            ],
          },
        }),
      )
      .mockResolvedValueOnce(proposal({ text: 'Valuta la proposta', custom: true }));
    renderPage();
    const box = await open(user, 'Proposta di tirocinio');

    const subject = within(box).getByRole('textbox', { name: /Oggetto dell’email: parte distintiva/ });
    await user.clear(subject);
    await user.type(subject, 'Per {{nome}');
    const save = screen.getByRole('button', {
      name: 'Salva: Oggetto dell’email: parte distintiva, «Proposta di tirocinio»',
    });
    await user.click(save);
    expect(
      await screen.findByText('Segnaposto sconosciuto {nome}: si possono usare {numero}'),
    ).toBeInTheDocument();
    expect(subject).toHaveAttribute('aria-invalid', 'true');

    await user.clear(subject);
    await user.type(subject, 'Valuta la proposta');
    await user.click(save);
    await waitFor(() =>
      expect(updateMessageText).toHaveBeenLastCalledWith(
        { key: 'PROPOSAL_SENT', part: 'EMAIL_SUBJECT', text: 'Valuta la proposta' },
        expect.anything(),
      ),
    );
    const form = subject.closest('form') as HTMLFormElement;
    expect(await within(form).findByRole('status')).toHaveTextContent('Testo salvato.');
    expect(
      within(form).getByRole('button', { name: /^Ripristina il testo predefinito/ }),
    ).toBeInTheDocument();
  });

  it('ripristina il testo predefinito di una parte cambiata', async () => {
    const user = userEvent.setup();
    vi.mocked(fetchMessageTexts).mockResolvedValue([
      proposal({ text: 'Valuta la proposta', custom: true }),
      notice,
    ]);
    vi.mocked(resetMessageText).mockResolvedValue(proposal());
    renderPage();

    expect(await screen.findByText('Testi cambiati')).toBeInTheDocument();
    const box = await open(user, 'Proposta di tirocinio');
    await user.click(
      within(box).getByRole('button', {
        name: 'Ripristina il testo predefinito: Oggetto dell’email: parte distintiva, «Proposta di tirocinio»',
      }),
    );

    await waitFor(() =>
      expect(within(box).getByRole('textbox', { name: /Oggetto dell’email: parte distintiva/ })).toHaveValue(
        'Nuova proposta di tirocinio',
      ),
    );
    expect(screen.getByText('Ripristinato il testo predefinito.')).toBeInTheDocument();
    expect(resetMessageText).toHaveBeenCalledWith(
      { key: 'PROPOSAL_SENT', part: 'EMAIL_SUBJECT' },
      expect.anything(),
    );
  });

  it('le email del cambio di indirizzo: niente testo nell’app, nessun segnaposto, oggetto senza numero', async () => {
    const user = userEvent.setup();
    renderPage();
    const box = await open(user, 'Avviso di indirizzo cambiato');

    expect(within(box).queryAllByRole('textbox', { name: /Testo della notifica nell’app/ })).toHaveLength(0);
    expect(within(box).getAllByRole('textbox')).toHaveLength(2);
    expect(within(box).getAllByText(/Nessun segnaposto\./)).not.toHaveLength(0);
    expect(screen.getByText('PORTALE NEXUS - Il tuo indirizzo email è stato cambiato')).toBeVisible();
  });
});
