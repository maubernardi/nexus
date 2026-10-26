import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import {
  addClosure,
  fetchCalendar,
  fetchDeadlinePreview,
  removeClosure,
  updatePatron,
  type WorkCalendar,
} from '@/config/api/calendarApi';
import { CalendarPage } from '@/pages/CalendarPage/CalendarPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderWithProviders } from '@/tests/renderWithProviders';

vi.mock('@/config/api/calendarApi', () => ({
  fetchCalendar: vi.fn(),
  fetchDeadlinePreview: vi.fn(),
  updatePatron: vi.fn(),
  addClosure: vi.fn(),
  removeClosure: vi.fn(),
}));

const calendar: WorkCalendar = {
  patronDay: 24,
  patronMonth: 6,
  version: 0,
  closures: [
    { id: 'K1', startDate: '2026-08-10', endDate: '2026-08-21', description: 'Ferie estive' },
    { id: 'K2', startDate: '2026-12-24', endDate: '2026-12-24', description: 'Vigilia di Natale' },
  ],
};

const renderPage = () =>
  renderWithProviders(
    <main>
      <CalendarPage />
    </main>,
  );

describe('<CalendarPage>', () => {
  beforeEach(() => {
    vi.mocked(fetchCalendar).mockResolvedValue(calendar);
    vi.mocked(updatePatron).mockClear();
    vi.mocked(addClosure).mockClear();
    vi.mocked(removeClosure).mockClear();
  });

  it('mostra patrono, festività nazionali e chiusure', async () => {
    renderPage();

    expect(await screen.findByRole('heading', { level: 1, name: 'Calendario' })).toBeInTheDocument();
    expect(screen.getByLabelText('Giorno')).toHaveValue('24');
    expect(screen.getByLabelText('Mese')).toHaveDisplayValue('giugno');
    expect(screen.getByText('Pasqua e Lunedì dell’Angelo')).toBeInTheDocument();
    expect(screen.queryByText(/4 ottobre/)).not.toBeInTheDocument();
    const list = screen.getByRole('list', { name: 'Chiusure dalla prima' });
    expect(within(list).getByText('dal 10/08/2026 al 21/08/2026')).toBeInTheDocument();
    expect(within(list).getByText('24/12/2026')).toBeInTheDocument();
    await expectNoAxeViolations();
  });

  it('salva il patrono con la versione e annuncia l’esito', async () => {
    const user = userEvent.setup();
    vi.mocked(updatePatron).mockResolvedValue({ ...calendar, patronDay: 29, version: 1 });
    renderPage();

    await user.selectOptions(await screen.findByLabelText('Giorno'), '29');
    await user.click(screen.getByRole('button', { name: 'Salva patrono' }));

    const outcome = await screen.findByText('Patrono salvato: 29 giugno.');
    await waitFor(() => expect(outcome).toHaveFocus());
    expect(updatePatron).toHaveBeenCalledWith({ day: 29, month: 6, version: 0 }, expect.anything());
  });

  it('aggiunge una chiusura di un giorno e ne rimuove una', async () => {
    const user = userEvent.setup();
    vi.mocked(addClosure).mockResolvedValue(calendar);
    vi.mocked(removeClosure).mockResolvedValue({ ...calendar, closures: [calendar.closures[1]] });
    renderPage();

    await user.type(await screen.findByLabelText(/^Dal/), '2026-11-02');
    await user.type(screen.getByLabelText(/^Descrizione/), 'Ponte');
    await user.click(screen.getByRole('button', { name: 'Aggiungi chiusura' }));
    expect(await screen.findByText('Chiusura aggiunta.')).toBeInTheDocument();
    expect(addClosure).toHaveBeenCalledWith(
      { startDate: '2026-11-02', endDate: null, description: 'Ponte' },
      expect.anything(),
    );

    await user.click(screen.getByRole('button', { name: 'Rimuovi Ferie estive' }));
    expect(await screen.findByText('Chiusura rimossa: Ferie estive.')).toBeInTheDocument();
    expect(removeClosure).toHaveBeenCalledWith('K1', expect.anything());
  });

  it('prova del calcolo: promemoria e scadenza con il giorno della settimana', async () => {
    const user = userEvent.setup();
    vi.mocked(fetchDeadlinePreview).mockResolvedValue({
      from: '2026-10-23',
      reminder: '2026-10-29',
      deadline: '2026-11-03',
    });
    renderPage();

    await user.type(await screen.findByLabelText('Data di accettazione'), '2026-10-23');
    await user.click(screen.getByRole('button', { name: 'Calcola' }));

    expect(await screen.findByText('giovedì 29/10/2026')).toBeInTheDocument();
    expect(screen.getByText('martedì 03/11/2026')).toBeInTheDocument();
    expect(fetchDeadlinePreview).toHaveBeenCalledWith('2026-10-23');
    await expectNoAxeViolations();
  });

  it('senza data e descrizione non invia', async () => {
    const user = userEvent.setup();
    renderPage();

    await user.click(await screen.findByRole('button', { name: 'Aggiungi chiusura' }));
    expect(await screen.findAllByText('Campo obbligatorio')).toHaveLength(2);
    expect(addClosure).not.toHaveBeenCalled();
  });
});
