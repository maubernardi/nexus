import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AxiosError, AxiosHeaders } from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { createBeneficiary, type Beneficiary } from '@/config/api/beneficiaryApi';
import { fetchZones } from '@/config/api/referenceApi';
import { BeneficiaryForm } from '@/organisms/BeneficiaryForm/BeneficiaryForm';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderWithProviders } from '@/tests/renderWithProviders';

vi.mock('@/config/api/referenceApi', () => ({ fetchZones: vi.fn(), fetchJobCategories: vi.fn() }));
vi.mock('@/config/api/beneficiaryApi', () => ({ createBeneficiary: vi.fn(), fetchMyBeneficiaries: vi.fn() }));

const fillRequired = async (user: ReturnType<typeof userEvent.setup>): Promise<void> => {
  await user.type(screen.getByLabelText(/^Nome/), 'Mario');
  await user.type(screen.getByLabelText(/^Cognome/), 'Rossi');
  await user.type(screen.getByLabelText(/^Anno di nascita/), '1998');
  await user.selectOptions(screen.getByLabelText(/^Genere/), 'M');
  await user.selectOptions(await screen.findByLabelText(/^Zona di residenza/), 'Z1');
};

describe('<BeneficiaryForm>', () => {
  beforeEach(() => {
    vi.mocked(fetchZones).mockResolvedValue([{ id: 'Z1', code: 'NORD', name: 'Zona Nord' }]);
    vi.mocked(createBeneficiary).mockReset();
  });

  it('senza dati obbligatori mostra il riepilogo, vi sposta il focus e marca i campi', async () => {
    const user = userEvent.setup();
    renderWithProviders(<BeneficiaryForm onSuccess={vi.fn()} />);

    await user.click(screen.getByRole('button', { name: 'Registra beneficiario' }));

    const summary = await screen.findByRole('alert');
    await waitFor(() => expect(summary).toHaveFocus());
    expect(summary).toHaveTextContent('Correggi 5 errori');
    expect(screen.getByRole('link', { name: 'Cognome: Campo obbligatorio' })).toHaveAttribute(
      'href',
      '#beneficiary-lastName',
    );
    expect(screen.getByLabelText(/^Cognome/)).toHaveAttribute('aria-invalid', 'true');
    expect(createBeneficiary).not.toHaveBeenCalled();
    await expectNoAxeViolations();
  });

  it('rifiuta un anno di nascita futuro', async () => {
    const user = userEvent.setup();
    renderWithProviders(<BeneficiaryForm onSuccess={vi.fn()} />);
    await fillRequired(user);
    await user.clear(screen.getByLabelText(/^Anno di nascita/));
    await user.type(screen.getByLabelText(/^Anno di nascita/), String(new Date().getFullYear() + 1));

    await user.click(screen.getByRole('button', { name: 'Registra beneficiario' }));

    expect(
      await screen.findByRole('link', { name: /Anno di nascita: Inserisci un anno tra il 1900/ }),
    ).toBeInTheDocument();
  });

  it('invia il payload corretto e notifica il beneficiario creato', async () => {
    const created = { id: 'C1', firstName: 'Mario', lastName: 'Rossi' } as Beneficiary;
    vi.mocked(createBeneficiary).mockResolvedValue(created);
    const onSuccess = vi.fn();
    const user = userEvent.setup();
    renderWithProviders(<BeneficiaryForm onSuccess={onSuccess} />);

    await fillRequired(user);
    await user.click(screen.getByRole('checkbox', { name: 'B' }));
    await user.click(screen.getByRole('button', { name: 'Aggiungi lingua' }));
    await user.selectOptions(screen.getByLabelText(/^Lingua 1/), 'it');
    await user.selectOptions(screen.getByLabelText(/^Livello lingua 1/), 'MADRELINGUA');
    await user.click(screen.getByRole('button', { name: 'Registra beneficiario' }));

    await waitFor(() => expect(onSuccess).toHaveBeenCalledWith(created));
    expect(vi.mocked(createBeneficiary).mock.calls[0]?.[0]).toEqual({
      firstName: 'Mario',
      lastName: 'Rossi',
      birthYear: 1998,
      gender: 'M',
      nationality: null,
      citizenship: null,
      residenceZoneId: 'Z1',
      licenseTypes: ['B'],
      hasVehicle: false,
      transportMode: null,
      hasLaw68: false,
      educationLevel: null,
      constraints: null,
      languages: [{ language: 'it', level: 'MADRELINGUA' }],
    });
  });

  it('segnala la lingua ripetuta senza chiamare il server', async () => {
    const user = userEvent.setup();
    renderWithProviders(<BeneficiaryForm onSuccess={vi.fn()} />);
    await fillRequired(user);
    for (const n of [1, 2]) {
      await user.click(screen.getByRole('button', { name: 'Aggiungi lingua' }));
      await user.selectOptions(screen.getByLabelText(new RegExp(`^Lingua ${n}`)), 'it');
      await user.selectOptions(screen.getByLabelText(new RegExp(`^Livello lingua ${n}`)), 'B1');
    }

    await user.click(screen.getByRole('button', { name: 'Registra beneficiario' }));

    expect(
      await screen.findByRole('link', { name: 'Lingue conosciute: Hai indicato più volte la stessa lingua' }),
    ).toBeInTheDocument();
    expect(createBeneficiary).not.toHaveBeenCalled();
  });

  it('riporta sui campi gli errori restituiti dal server', async () => {
    vi.mocked(createBeneficiary).mockRejectedValue(
      new AxiosError('Bad Request', '400', undefined, undefined, {
        status: 400,
        statusText: 'Bad Request',
        headers: {},
        config: { headers: new AxiosHeaders() },
        data: {
          status: 400,
          message: 'Dati non validi',
          fieldErrors: [{ field: 'residenceZoneId', message: 'Zona non valida' }],
        },
      }),
    );
    const user = userEvent.setup();
    renderWithProviders(<BeneficiaryForm onSuccess={vi.fn()} />);
    await fillRequired(user);

    await user.click(screen.getByRole('button', { name: 'Registra beneficiario' }));

    expect(
      await screen.findByRole('link', { name: 'Zona di residenza: Zona non valida' }),
    ).toBeInTheDocument();
    expect(screen.getByLabelText(/^Zona di residenza/)).toHaveAttribute('aria-invalid', 'true');
  });
});
