import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AxiosError, AxiosHeaders } from 'axios';
import { describe, expect, it, vi } from 'vitest';

import type { Company } from '@/config/api/companyApi';
import { searchMunicipalities } from '@/config/api/municipalityApi';
import { CompanyForm } from '@/organisms/CompanyForm/CompanyForm';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderWithProviders } from '@/tests/renderWithProviders';

vi.mock('@/config/api/municipalityApi', () => ({ searchMunicipalities: vi.fn() }));

const FIRENZE = {
  id: 'M1',
  name: 'Firenze',
  istatCode: '048017',
  cadastralCode: 'D612',
  provinceName: 'Firenze',
  provinceAbbreviation: 'FI',
  validFrom: '2026-01-01',
};

const saved: Company = {
  id: 'C1',
  name: 'Officina Rossi',
  vatCode: '12345678901',
  legalAddress: null,
  contactPerson: null,
  phone: null,
  email: null,
  active: true,
  version: 0,
  updatedAt: '2026-10-02T08:00:00Z',
};

const renderForm = (onSubmit = vi.fn().mockResolvedValue(saved), onSuccess = vi.fn()) => {
  renderWithProviders(
    <main>
      <CompanyForm submitLabel="Registra azienda" onSubmit={onSubmit} onSuccess={onSuccess} />
    </main>,
  );
  return { onSubmit, onSuccess };
};

describe('<CompanyForm>', () => {
  it('senza dati obbligatori e con P.IVA errata mostra il riepilogo con focus', async () => {
    const user = userEvent.setup();
    const { onSubmit } = renderForm();
    await user.type(screen.getByLabelText(/^Partita IVA/), '1234');
    await user.type(screen.getByLabelText(/^Email/), 'non-una-mail');

    await user.click(screen.getByRole('button', { name: 'Registra azienda' }));

    const summary = await screen.findByRole('alert');
    await waitFor(() => expect(summary).toHaveFocus());
    expect(summary).toHaveTextContent('Correggi 3 errori');
    expect(
      screen.getByRole('link', { name: 'Partita IVA: La partita IVA è composta da 11 cifre' }),
    ).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();
    await expectNoAxeViolations();
  });

  it('invia i dati puliti e segnala il successo', async () => {
    const user = userEvent.setup();
    const { onSubmit, onSuccess } = renderForm();
    await user.type(screen.getByLabelText(/^Ragione sociale/), '  Officina Rossi ');
    await user.type(screen.getByLabelText(/^Partita IVA/), '12345678901');

    await user.click(screen.getByRole('button', { name: 'Registra azienda' }));

    await waitFor(() => expect(onSuccess).toHaveBeenCalledWith(saved));
    expect(onSubmit).toHaveBeenCalledWith({
      name: 'Officina Rossi',
      vatCode: '12345678901',
      legalAddress: null,
      legalMunicipalityId: null,
      contactPerson: null,
      phone: null,
      email: null,
      version: undefined,
    });
  });

  it('riporta sul campo la partita IVA già registrata', async () => {
    const user = userEvent.setup();
    renderForm(
      vi.fn().mockRejectedValue(
        new AxiosError('Bad Request', '400', undefined, undefined, {
          status: 400,
          statusText: 'Bad Request',
          headers: {},
          config: { headers: new AxiosHeaders() },
          data: {
            status: 400,
            message: 'Dati non validi',
            fieldErrors: [{ field: 'vatCode', message: "Esiste già un'azienda con questa partita IVA" }],
          },
        }),
      ),
    );
    await user.type(screen.getByLabelText(/^Ragione sociale/), 'Officina Rossi');
    await user.type(screen.getByLabelText(/^Partita IVA/), '12345678901');
    await user.click(screen.getByRole('button', { name: 'Registra azienda' }));

    expect(
      await screen.findByRole('link', { name: "Partita IVA: Esiste già un'azienda con questa partita IVA" }),
    ).toBeInTheDocument();
    expect(screen.getByLabelText(/^Partita IVA/)).toHaveAttribute('aria-invalid', 'true');
  });

  it('sede legale: indirizzo e comune scelto dall’elenco (US-1007)', async () => {
    vi.mocked(searchMunicipalities).mockResolvedValue([FIRENZE]);
    const user = userEvent.setup();
    const { onSubmit } = renderForm();
    await user.type(screen.getByLabelText(/^Ragione sociale/), 'Officina Rossi');
    await user.type(screen.getByLabelText(/^Partita IVA/), '12345678901');
    await user.type(screen.getByLabelText(/^Indirizzo della sede legale/), 'Via Roma 1');
    const municipality = screen.getByRole('combobox', { name: /^Comune della sede legale/ });

    // testo senza scelta: errore sul campo, nulla inviato
    await user.type(municipality, 'fire');
    await user.click(screen.getByRole('button', { name: 'Registra azienda' }));
    expect(
      await screen.findByRole('link', { name: 'Comune della sede legale: Scegli il comune dall’elenco' }),
    ).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();

    await user.click(municipality);
    await user.keyboard('{Backspace}');
    await user.click(await screen.findByRole('option', { name: 'Firenze (FI)' }));
    await expectNoAxeViolations();
    await user.click(screen.getByRole('button', { name: 'Registra azienda' }));

    await waitFor(() => expect(onSubmit).toHaveBeenCalled());
    expect(onSubmit.mock.calls[0][0]).toMatchObject({
      legalAddress: 'Via Roma 1',
      legalMunicipalityId: 'M1',
    });
  });

  it('modifica: il comune salvato è già scelto; l’errore del server va sul suo campo', async () => {
    const onSubmit = vi.fn().mockRejectedValue(
      new AxiosError('Bad Request', '400', undefined, undefined, {
        status: 400,
        statusText: 'Bad Request',
        headers: {},
        config: { headers: new AxiosHeaders() },
        data: {
          status: 400,
          fieldErrors: [
            {
              field: 'legalMunicipalityId',
              message: 'Il comune Firenze non è più valido: sceglilo di nuovo',
            },
          ],
        },
      }),
    );
    const user = userEvent.setup();
    renderWithProviders(
      <main>
        <CompanyForm
          company={{ ...saved, legalAddress: 'Via Roma 1', legalMunicipality: FIRENZE }}
          submitLabel="Salva modifiche"
          onSubmit={onSubmit}
          onSuccess={vi.fn()}
        />
      </main>,
    );
    const municipality = screen.getByRole('combobox', { name: /^Comune della sede legale/ });
    expect(municipality).toHaveValue('Firenze (FI)');

    await user.click(screen.getByRole('button', { name: 'Salva modifiche' }));
    expect(onSubmit.mock.calls[0][0]).toMatchObject({ legalMunicipalityId: 'M1', version: 0 });
    expect(
      await screen.findByRole('link', {
        name: 'Comune della sede legale: Il comune Firenze non è più valido: sceglilo di nuovo',
      }),
    ).toBeInTheDocument();
    expect(municipality).toHaveAttribute('aria-invalid', 'true');
  });
});
