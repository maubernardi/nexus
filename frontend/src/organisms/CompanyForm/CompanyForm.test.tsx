import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AxiosError, AxiosHeaders } from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import type { Company } from '@/config/api/companyApi';
import { searchMunicipalities } from '@/config/api/municipalityApi';
import { fetchBusinessCategories } from '@/config/api/referenceApi';
import { CompanyForm } from '@/organisms/CompanyForm/CompanyForm';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderWithProviders } from '@/tests/renderWithProviders';

vi.mock('@/config/api/municipalityApi', () => ({ searchMunicipalities: vi.fn() }));
vi.mock('@/config/api/referenceApi', () => ({ fetchBusinessCategories: vi.fn() }));

const RISTORANTE = {
  id: 'K11',
  code: 'RISTORANTE_TRATTORIA_BISTR',
  name: 'Ristorante / Trattoria / Bistrot',
};
const BAR = { id: 'K14', code: 'BAR_PUB', name: 'Bar / Pub' };

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
  beforeEach(() => {
    vi.mocked(fetchBusinessCategories).mockResolvedValue([BAR, RISTORANTE]);
  });

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
      businessCategoryId: null,
      activity: null,
      contactPerson: null,
      phone: null,
      email: null,
      version: undefined,
    });
  });

  it('categoria merceologica dal catalogo e attività (US-509)', async () => {
    const user = userEvent.setup();
    const { onSubmit } = renderForm();
    const category = screen.getByRole('combobox', { name: 'Categoria merceologica' });
    await waitFor(() => expect(category).toBeEnabled());
    expect(category).toHaveDisplayValue('Nessuna');
    expect(screen.getByLabelText(/^Attività/)).toHaveAccessibleDescription(
      'Che cosa fa l’azienda, in breve: per esempio «Pizzeria».',
    );
    await expectNoAxeViolations();

    await user.type(screen.getByLabelText(/^Ragione sociale/), 'Pizzeria Napoli');
    await user.type(screen.getByLabelText(/^Partita IVA/), '12345678901');
    await user.selectOptions(category, 'Ristorante / Trattoria / Bistrot');
    await user.type(screen.getByLabelText(/^Attività/), ' Pizzeria ');
    await user.click(screen.getByRole('button', { name: 'Registra azienda' }));

    await waitFor(() =>
      expect(onSubmit).toHaveBeenCalledWith(
        expect.objectContaining({ businessCategoryId: 'K11', activity: 'Pizzeria' }),
      ),
    );
  });

  it('una categoria disattivata resta sull’azienda che l’ha già (US-509)', async () => {
    renderWithProviders(
      <main>
        <CompanyForm
          company={{
            ...saved,
            businessCategory: { id: 'K5', code: 'PENSIONE', name: 'Pensione' },
            activity: 'Pensione familiare',
          }}
          submitLabel="Salva modifiche"
          onSubmit={vi.fn()}
          onSuccess={vi.fn()}
        />
      </main>,
    );

    const category = screen.getByRole('combobox', { name: 'Categoria merceologica' });
    await waitFor(() => expect(category).toBeEnabled());
    expect(category).toHaveDisplayValue('Pensione');
    expect(screen.getAllByRole('option').map((o) => o.textContent)).toEqual([
      'Nessuna',
      'Pensione',
      'Bar / Pub',
      'Ristorante / Trattoria / Bistrot',
    ]);
    expect(screen.getByLabelText(/^Attività/)).toHaveValue('Pensione familiare');
  });

  it('rifiuta i segnaposto del database attuale con lo stesso messaggio del server (US-508)', async () => {
    const user = userEvent.setup();
    const { onSubmit } = renderForm();
    await user.type(screen.getByLabelText(/^Ragione sociale/), ' azienda SENZA nome n. 5');
    await user.type(screen.getByLabelText(/^Partita IVA/), '00000000000');

    await user.click(screen.getByRole('button', { name: 'Registra azienda' }));

    expect(
      await screen.findByRole('link', {
        name: 'Ragione sociale: Inserisci la ragione sociale vera: «Azienda senza nome n. …» vuol dire che manca',
      }),
    ).toBeInTheDocument();
    expect(
      screen.getByRole('link', {
        name: 'Partita IVA: Inserisci la partita IVA vera: «00000000000» vuol dire che manca',
      }),
    ).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('azienda da completare: i campi con il segnaposto partono vuoti e lo spiegano (US-508)', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockResolvedValue(saved);
    renderWithProviders(
      <main>
        <CompanyForm
          company={{
            ...saved,
            name: 'Azienda senza nome n. 1234',
            vatCode: '00000000000',
            phone: '055 123456',
            missingData: ['NAME', 'VAT_CODE'],
          }}
          submitLabel="Salva modifiche"
          onSubmit={onSubmit}
          onSuccess={vi.fn()}
        />
      </main>,
    );

    const name = screen.getByLabelText(/^Ragione sociale/);
    const vat = screen.getByLabelText(/^Partita IVA/);
    expect(name).toHaveValue('');
    expect(name).toHaveAccessibleDescription(
      'Da completare: il database attuale non aveva la ragione sociale.',
    );
    expect(vat).toHaveValue('');
    expect(vat).toHaveAccessibleDescription(/^Da completare: il database attuale non aveva la partita IVA/);
    expect(screen.getByLabelText(/^Telefono/)).toHaveValue('055 123456');
    await expectNoAxeViolations();

    await user.click(screen.getByRole('button', { name: 'Salva modifiche' }));
    expect(await screen.findByRole('alert')).toHaveTextContent('Correggi 2 errori');
    expect(onSubmit).not.toHaveBeenCalled();

    await user.type(name, 'Trattoria da Mario');
    await user.type(vat, '98765432109');
    await user.click(screen.getByRole('button', { name: 'Salva modifiche' }));
    await waitFor(() =>
      expect(onSubmit).toHaveBeenCalledWith(
        expect.objectContaining({ name: 'Trattoria da Mario', vatCode: '98765432109', version: 0 }),
      ),
    );
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
