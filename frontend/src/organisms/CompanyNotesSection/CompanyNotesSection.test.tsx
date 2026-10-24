import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { addCompanyNote, fetchCompanyNotes, type CompanyNote } from '@/config/api/companyNoteApi';
import { CompanyNotesSection } from '@/organisms/CompanyNotesSection/CompanyNotesSection';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderWithProviders } from '@/tests/renderWithProviders';

vi.mock('@/config/api/companyNoteApi', () => ({ fetchCompanyNotes: vi.fn(), addCompanyNote: vi.fn() }));

const notes: CompanyNote[] = [
  {
    id: 'N2',
    text: 'Chiusura per ferie ad agosto',
    createdAt: '2026-10-20T09:30:00Z',
    authorName: 'Paolo Verdi',
  },
  { id: 'N1', text: 'Contattare al mattino', createdAt: '2026-10-01T08:00:00Z', authorName: 'Anna Neri' },
];

describe('<CompanyNotesSection>', () => {
  beforeEach(() => {
    vi.mocked(fetchCompanyNotes).mockClear();
    vi.mocked(addCompanyNote).mockClear();
  });

  it('elenca le note dalla più recente con data e autore', async () => {
    vi.mocked(fetchCompanyNotes).mockResolvedValue(notes);
    renderWithProviders(<CompanyNotesSection companyId="C1" />);

    const items = await screen.findAllByRole('listitem');
    expect(items[0]).toHaveTextContent('Chiusura per ferie ad agosto');
    expect(items[0]).toHaveTextContent('Paolo Verdi');
    expect(items[0].querySelector('time')).toHaveAttribute('datetime', '2026-10-20T09:30:00Z');
    expect(screen.getByRole('list', { name: 'Note dalla più recente' })).toBeInTheDocument();
    // le note non si modificano né si cancellano: nessun comando sulle righe
    expect(screen.getAllByRole('button')).toHaveLength(1);
    await expectNoAxeViolations();
  });

  it('aggiunge una nota, svuota il campo e annuncia l’esito', async () => {
    const user = userEvent.setup();
    vi.mocked(fetchCompanyNotes).mockResolvedValue([]);
    vi.mocked(addCompanyNote).mockResolvedValue(notes[0]);
    renderWithProviders(<CompanyNotesSection companyId="C1" />);
    expect(await screen.findByText('Nessuna nota.')).toBeInTheDocument();

    const field = screen.getByLabelText(/^Nuova nota/);
    await user.type(field, '  Chiusura per ferie ad agosto ');
    await user.click(screen.getByRole('button', { name: 'Aggiungi nota' }));

    const outcome = await screen.findByText('Nota aggiunta.');
    await waitFor(() => expect(outcome).toHaveFocus());
    expect(addCompanyNote).toHaveBeenCalledWith(
      { companyId: 'C1', text: 'Chiusura per ferie ad agosto' },
      expect.anything(),
    );
    expect(field).toHaveValue('');
  });

  it('una nota vuota non si invia', async () => {
    const user = userEvent.setup();
    vi.mocked(fetchCompanyNotes).mockResolvedValue([]);
    renderWithProviders(<CompanyNotesSection companyId="C1" />);
    await screen.findByText('Nessuna nota.');

    await user.type(screen.getByLabelText(/^Nuova nota/), '   ');
    await user.click(screen.getByRole('button', { name: 'Aggiungi nota' }));

    expect(await screen.findByText('Campo obbligatorio')).toBeInTheDocument();
    expect(screen.getByLabelText(/^Nuova nota/)).toHaveAttribute('aria-invalid', 'true');
    expect(addCompanyNote).not.toHaveBeenCalled();
  });
});
