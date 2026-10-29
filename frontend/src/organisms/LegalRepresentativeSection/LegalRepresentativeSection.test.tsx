import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AxiosError, AxiosHeaders } from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import {
  fetchLegalRepresentative,
  saveLegalRepresentative,
  type LegalRepresentative,
} from '@/config/api/companyLegalDataApi';
import { LegalRepresentativeSection } from '@/organisms/LegalRepresentativeSection/LegalRepresentativeSection';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderWithProviders } from '@/tests/renderWithProviders';

vi.mock('@/config/api/companyLegalDataApi', () => ({
  fetchLegalRepresentative: vi.fn(),
  saveLegalRepresentative: vi.fn(),
}));

const error = (status: number, body: Record<string, unknown>) =>
  new AxiosError(String(status), String(status), undefined, undefined, {
    status,
    statusText: String(status),
    headers: {},
    config: { headers: new AxiosHeaders() },
    data: { status, ...body },
  });

const renderSection = () =>
  renderWithProviders(
    <main>
      <h1>Azienda</h1>
      <LegalRepresentativeSection companyId="C1" />
    </main>,
  );

describe('<LegalRepresentativeSection>', () => {
  beforeEach(() => {
    vi.mocked(saveLegalRepresentative).mockReset();
  });

  it('non indicato: lo si registra, con errori dal modulo e dal server', async () => {
    const user = userEvent.setup();
    vi.mocked(fetchLegalRepresentative).mockResolvedValue(null);
    vi.mocked(saveLegalRepresentative)
      .mockRejectedValueOnce(
        error(400, { fieldErrors: [{ field: 'fiscalCode', message: 'Codice fiscale non valido' }] }),
      )
      .mockResolvedValueOnce({ firstName: 'Laura', lastName: 'Bianchi', version: 0 });
    renderSection();

    expect(await screen.findByText('Non ancora indicato.')).toBeInTheDocument();
    await expectNoAxeViolations();
    await user.click(screen.getByRole('button', { name: 'Indica il legale rappresentante' }));
    expect(screen.getByRole('heading', { name: 'Legale rappresentante', level: 3 })).toHaveFocus();
    await user.click(screen.getByRole('button', { name: 'Salva' }));
    expect(await screen.findByRole('link', { name: 'Nome: Campo obbligatorio' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Cognome: Campo obbligatorio' })).toBeInTheDocument();

    await user.type(screen.getByLabelText(/^Nome/), 'Laura');
    await user.type(screen.getByLabelText(/^Cognome/), 'Bianchi');
    await user.type(screen.getByLabelText(/^Codice fiscale/), 'rssmra85t10a562t');
    await user.click(screen.getByRole('button', { name: 'Salva' }));
    expect(
      await screen.findByRole('link', { name: 'Codice fiscale: Codice fiscale non valido' }),
    ).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Salva' }));
    const outcome = await screen.findByText('Legale rappresentante salvato.');
    await waitFor(() => expect(outcome).toHaveFocus());
    expect(vi.mocked(saveLegalRepresentative).mock.calls[0][0]).toEqual({
      companyId: 'C1',
      payload: {
        firstName: 'Laura',
        lastName: 'Bianchi',
        fiscalCode: 'RSSMRA85T10A562T',
        birthDate: null,
        birthPlace: null,
        residence: null,
        version: null,
      },
    });
  });

  it('indicato: dati con quelli mancanti in testo; la modifica manda la versione, il conflitto si spiega', async () => {
    const user = userEvent.setup();
    // come lo manda l'API: i campi vuoti sono omessi
    const rep: LegalRepresentative = {
      firstName: 'Laura',
      lastName: 'Bianchi',
      fiscalCode: 'BNCLRA80A41H501D',
      birthDate: '1980-01-01',
      version: 2,
    };
    vi.mocked(fetchLegalRepresentative).mockResolvedValue(rep);
    vi.mocked(saveLegalRepresentative).mockRejectedValue(
      error(409, { message: 'Il legale rappresentante è stato modificato nel frattempo' }),
    );
    renderSection();

    expect(await screen.findByText('Laura Bianchi')).toBeInTheDocument();
    expect(screen.getByText('BNCLRA80A41H501D')).toBeInTheDocument();
    expect(screen.getByText('1 gennaio 1980')).toBeInTheDocument();
    expect(screen.getByText('Non indicato')).toBeInTheDocument();
    await expectNoAxeViolations();

    await user.click(screen.getByRole('button', { name: 'Modifica' }));
    expect(screen.getByLabelText(/^Cognome/)).toHaveValue('Bianchi');
    await user.type(screen.getByLabelText(/^Residenza/), 'Via Dante 2, Firenze');
    await user.click(screen.getByRole('button', { name: 'Salva' }));

    expect(
      await screen.findByText('Non salvato: Il legale rappresentante è stato modificato nel frattempo'),
    ).toHaveFocus();
    expect(vi.mocked(saveLegalRepresentative).mock.calls[0][0].payload).toMatchObject({
      residence: 'Via Dante 2, Firenze',
      version: 2,
    });
  });
});
