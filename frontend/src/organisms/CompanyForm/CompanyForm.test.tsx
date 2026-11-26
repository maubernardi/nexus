import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AxiosError, AxiosHeaders } from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import type { Company } from '@/config/api/companyApi';
import { searchMunicipalities } from '@/config/api/municipalityApi';
import { fetchBusinessCategories, fetchContactRoles } from '@/config/api/referenceApi';
import { CompanyForm } from '@/organisms/CompanyForm/CompanyForm';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderWithProviders } from '@/tests/renderWithProviders';

vi.mock('@/config/api/municipalityApi', () => ({ searchMunicipalities: vi.fn() }));
vi.mock('@/config/api/referenceApi', () => ({
  fetchBusinessCategories: vi.fn(),
  fetchContactRoles: vi.fn(),
  fetchZones: vi.fn().mockResolvedValue([
    { id: 'Z1', code: 'RIFREDI', name: 'Rifredi' },
    { id: 'Z2', code: 'NOVOLI', name: 'Novoli' },
  ]),
}));

const RISTORANTE = {
  id: 'K11',
  code: 'RISTORANTE_TRATTORIA_BISTR',
  name: 'Ristorante / Trattoria / Bistrot',
};
const BAR = { id: 'K14', code: 'BAR_PUB', name: 'Bar / Pub' };
const TITOLARE = { id: 'R13', code: 'TITOLARE', name: 'Titolare' };
const PERSONALE = { id: 'R3', code: 'RESPONSABILE_DEL_PERSONALE', name: 'Responsabile del personale' };

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
    vi.mocked(fetchContactRoles).mockResolvedValue([PERSONALE, TITOLARE]);
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
      operatingAddress: null,
      operatingMunicipalityId: null,
      phone: null,
      mobile: null,
      fax: null,
      email: null,
      secondaryEmail: null,
      contacts: [],
      reliability: null,
      projectInterest: null,
      trainingShip: false,
      zoneId: null,
      version: undefined,
    });
  });

  it('nave scuola: casella con l’aiuto, parte dal valore salvato e si toglie (US-515)', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockResolvedValue(saved);
    renderWithProviders(
      <main>
        <CompanyForm
          company={{ ...saved, trainingShip: true }}
          submitLabel="Salva modifiche"
          onSubmit={onSubmit}
          onSuccess={vi.fn()}
        />
      </main>,
    );
    const box = screen.getByRole('checkbox', { name: 'Nave scuola' });
    expect(box).toBeChecked();
    expect(box).toHaveAccessibleDescription(/Azienda adatta a un primo tirocinio/);
    await expectNoAxeViolations();

    await user.click(box);
    await user.click(screen.getByRole('button', { name: 'Salva modifiche' }));
    await waitFor(() =>
      expect(onSubmit).toHaveBeenCalledWith(expect.objectContaining({ trainingShip: false })),
    );
  });

  it('stelle e pollici: vuoti all’inizio, si sceglie con un clic e si tolgono (US-514)', async () => {
    const user = userEvent.setup();
    const { onSubmit } = renderForm();
    const stars = screen.getByRole('group', { name: 'Affidabilità' });
    const thumbs = screen.getByRole('group', { name: 'Interesse al progetto' });
    expect(within(stars).getAllByRole('radio')).toHaveLength(4);
    expect(within(thumbs).getAllByRole('radio')).toHaveLength(2);
    for (const radio of screen.getAllByRole('radio')) expect(radio).not.toBeChecked();

    await user.click(within(stars).getByRole('radio', { name: '3 stelle su 4' }));
    await user.click(within(thumbs).getByRole('radio', { name: '2 pollici alzati su 2' }));
    await user.click(screen.getByRole('button', { name: 'Togli: Interesse al progetto' }));
    expect(within(thumbs).queryByRole('button')).not.toBeInTheDocument();
    await expectNoAxeViolations();

    await user.type(screen.getByLabelText(/^Ragione sociale/), 'Officina Rossi');
    await user.type(screen.getByLabelText(/^Partita IVA/), '12345678901');
    await user.click(screen.getByRole('button', { name: 'Registra azienda' }));
    await waitFor(() =>
      expect(onSubmit).toHaveBeenCalledWith(
        expect.objectContaining({ reliability: 3, projectInterest: null }),
      ),
    );
  });

  it('zona dal catalogo, con quella già dell’azienda anche se disattivata (US-511)', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockResolvedValue(saved);
    renderWithProviders(
      <main>
        <CompanyForm
          company={{ ...saved, zone: { id: 'Z9', code: 'VECCHIA', name: 'Zona chiusa' } }}
          submitLabel="Salva modifiche"
          onSubmit={onSubmit}
          onSuccess={vi.fn()}
        />
      </main>,
    );
    const zone = screen.getByRole('combobox', { name: 'Zona' });
    await waitFor(() => expect(zone).toBeEnabled());
    expect(zone).toHaveDisplayValue('Zona chiusa');
    expect(Array.from(zone.querySelectorAll('option')).map((o) => o.textContent)).toEqual([
      'Nessuna',
      'Zona chiusa',
      'Rifredi',
      'Novoli',
    ]);
    expect(zone).toHaveAccessibleDescription(
      'La zona del territorio: il Tutor la usa per cercare le aziende.',
    );
    await user.selectOptions(zone, 'Rifredi');
    await user.click(screen.getByRole('button', { name: 'Salva modifiche' }));
    await waitFor(() => expect(onSubmit).toHaveBeenCalledWith(expect.objectContaining({ zoneId: 'Z1' })));
  });

  it('azienda con stelle e pollici: il modulo parte con i valori salvati (US-514)', () => {
    renderWithProviders(
      <main>
        <CompanyForm
          company={{ ...saved, reliability: 2, projectInterest: 1 }}
          submitLabel="Salva modifiche"
          onSubmit={vi.fn()}
          onSuccess={vi.fn()}
        />
      </main>,
    );
    expect(screen.getByRole('radio', { name: '2 stelle su 4' })).toBeChecked();
    expect(screen.getByRole('radio', { name: '1 pollice alzato su 2' })).toBeChecked();
  });

  it('il nome non può iniziare con i simboli del database attuale (US-514)', async () => {
    const user = userEvent.setup();
    const { onSubmit } = renderForm();
    await user.type(screen.getByLabelText(/^Ragione sociale/), '$$ Officina Rossi');
    await user.type(screen.getByLabelText(/^Partita IVA/), '12345678901');
    await user.click(screen.getByRole('button', { name: 'Registra azienda' }));

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'La ragione sociale non può iniziare con $, £, & o ^: per affidabilità e interesse al progetto usa stelle e pollici',
    );
    expect(onSubmit).not.toHaveBeenCalled();
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
    expect(Array.from(category.querySelectorAll('option')).map((o) => o.textContent)).toEqual([
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

  it('referenti: aggiunta con il focus sul nome, ruolo dal catalogo, rimozione (US-510)', async () => {
    const user = userEvent.setup();
    const { onSubmit } = renderForm();
    expect(screen.getByRole('group', { name: 'Referenti' })).toHaveTextContent('Nessun referente.');
    const add = screen.getByRole('button', { name: 'Aggiungi referente' });

    await user.click(add);
    expect(screen.getByRole('textbox', { name: 'Referente 1: nome' })).toHaveFocus();
    await user.keyboard(' Paola Neri ');
    const role = screen.getByRole('combobox', { name: 'Referente 1: ruolo' });
    await waitFor(() => expect(role).toBeEnabled());
    await user.selectOptions(role, 'Titolare');
    await user.type(screen.getByRole('textbox', { name: 'Referente 1: cellulare' }), '+39 333 1234567');

    await user.click(add);
    expect(screen.getByRole('textbox', { name: 'Referente 2: nome' })).toHaveFocus();
    await user.selectOptions(
      screen.getByRole('combobox', { name: 'Referente 2: ruolo' }),
      'Responsabile del personale',
    );
    await user.click(add);
    expect(screen.getByRole('group', { name: 'Referente 3' })).toBeInTheDocument();
    await expectNoAxeViolations();

    await user.click(screen.getByRole('button', { name: 'Togli il referente 3' }));
    expect(screen.queryByRole('group', { name: 'Referente 3' })).not.toBeInTheDocument();
    expect(add).toHaveFocus();

    await user.type(screen.getByLabelText(/^Ragione sociale/), 'Hotel Bellavista');
    await user.type(screen.getByLabelText(/^Partita IVA/), '12345678901');
    await user.click(screen.getByRole('button', { name: 'Registra azienda' }));
    await waitFor(() => expect(onSubmit).toHaveBeenCalled());
    expect(onSubmit.mock.calls[0][0].contacts).toEqual([
      { name: 'Paola Neri', roleId: 'R13', phone: null, mobile: '+39 333 1234567', email: null },
      { name: null, roleId: 'R3', phone: null, mobile: null, email: null },
    ]);
  });

  it('referente senza nome né ruolo: errore nel riepilogo, che sparisce scegliendo il ruolo (US-510)', async () => {
    const user = userEvent.setup();
    const { onSubmit } = renderForm();
    await user.type(screen.getByLabelText(/^Ragione sociale/), 'Hotel Bellavista');
    await user.type(screen.getByLabelText(/^Partita IVA/), '12345678901');
    await user.click(screen.getByRole('button', { name: 'Aggiungi referente' }));
    await user.type(screen.getByRole('textbox', { name: 'Referente 1: telefono' }), '055 1234');
    await user.type(screen.getByRole('textbox', { name: 'Referente 1: email' }), 'non-una-mail');

    await user.click(screen.getByRole('button', { name: 'Registra azienda' }));
    expect(
      await screen.findByRole('link', { name: 'Referente 1: nome: Inserisci il nome o scegli il ruolo' }),
    ).toBeInTheDocument();
    expect(
      screen.getByRole('link', { name: 'Referente 1: email: Indirizzo email non valido' }),
    ).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();

    const role = screen.getByRole('combobox', { name: 'Referente 1: ruolo' });
    await waitFor(() => expect(role).toBeEnabled());
    await user.selectOptions(role, 'Titolare');
    await waitFor(() =>
      expect(screen.getByRole('textbox', { name: 'Referente 1: nome' })).toHaveAttribute(
        'aria-invalid',
        'false',
      ),
    );
  });

  it('un ruolo disattivato resta sul referente che l’ha; l’errore del server va sul suo campo (US-510)', async () => {
    const onSubmit = vi.fn().mockRejectedValue(
      new AxiosError('Bad Request', '400', undefined, undefined, {
        status: 400,
        statusText: 'Bad Request',
        headers: {},
        config: { headers: new AxiosHeaders() },
        data: { status: 400, fieldErrors: [{ field: 'contacts[1].roleId', message: 'Ruolo non valido' }] },
      }),
    );
    const user = userEvent.setup();
    renderWithProviders(
      <main>
        <CompanyForm
          company={{
            ...saved,
            contacts: [
              { name: 'Paola Neri', role: { id: 'R24', code: 'SOCIO', name: 'Socio' }, mobile: '333 1' },
              { role: TITOLARE },
            ],
          }}
          submitLabel="Salva modifiche"
          onSubmit={onSubmit}
          onSuccess={vi.fn()}
        />
      </main>,
    );
    const first = screen.getByRole('combobox', { name: 'Referente 1: ruolo' });
    await waitFor(() => expect(first).toBeEnabled());
    expect(first).toHaveDisplayValue('Socio');
    expect(screen.getByRole('textbox', { name: 'Referente 1: cellulare' })).toHaveValue('333 1');
    expect(screen.getByRole('textbox', { name: 'Referente 2: nome' })).toHaveValue('');
    expect(
      Array.from(
        screen.getByRole('combobox', { name: 'Categoria merceologica' }).querySelectorAll('option'),
      ).map((o) => o.textContent),
    ).toEqual(['Nessuna', 'Bar / Pub', 'Ristorante / Trattoria / Bistrot']);
    expect(Array.from(first.querySelectorAll('option')).map((o) => o.textContent)).toEqual([
      'Nessuno',
      'Socio',
      'Responsabile del personale',
      'Titolare',
    ]);

    await user.click(screen.getByRole('button', { name: 'Salva modifiche' }));
    expect(
      await screen.findByRole('link', { name: 'Referente 2: ruolo: Ruolo non valido' }),
    ).toBeInTheDocument();
    expect(screen.getByRole('combobox', { name: 'Referente 2: ruolo' })).toHaveAttribute(
      'aria-invalid',
      'true',
    );
  });

  it('recapiti e sede operativa dell’azienda (US-510)', async () => {
    vi.mocked(searchMunicipalities).mockResolvedValue([FIRENZE]);
    const user = userEvent.setup();
    const { onSubmit } = renderForm();
    expect(screen.getByRole('group', { name: 'Sede operativa' })).toBeInTheDocument();
    expect(screen.getByRole('group', { name: 'Recapiti dell’azienda' })).toBeInTheDocument();
    await user.type(screen.getByLabelText(/^Ragione sociale/), 'Officina Rossi');
    await user.type(screen.getByLabelText(/^Partita IVA/), '12345678901');
    await user.type(screen.getByLabelText(/^Indirizzo della sede operativa/), ' Via dei Mille 3 ');
    await user.type(screen.getByRole('combobox', { name: /^Comune della sede operativa/ }), 'fire');
    await user.click(await screen.findByRole('option', { name: 'Firenze (FI)' }));
    await user.type(screen.getByLabelText(/^Cellulare/), '333 7654321');
    await user.type(screen.getByLabelText(/^Fax/), '055 ABC');
    await user.type(screen.getByLabelText(/^Seconda email/), 'ordini@azienda.test');

    await user.click(screen.getByRole('button', { name: 'Registra azienda' }));
    expect(
      await screen.findByRole('link', { name: 'Fax: Usa solo cifre, spazi e i simboli + ( ) . - /' }),
    ).toBeInTheDocument();
    await user.clear(screen.getByLabelText(/^Fax/));
    await user.type(screen.getByLabelText(/^Fax/), '055 765432');
    await user.click(screen.getByRole('button', { name: 'Registra azienda' }));

    await waitFor(() => expect(onSubmit).toHaveBeenCalled());
    expect(onSubmit.mock.calls[0][0]).toMatchObject({
      operatingAddress: 'Via dei Mille 3',
      operatingMunicipalityId: 'M1',
      mobile: '333 7654321',
      fax: '055 765432',
      secondaryEmail: 'ordini@azienda.test',
    });
  });

  it('al massimo 10 referenti: il pulsante si disattiva e lo spiega (US-510)', async () => {
    renderWithProviders(
      <main>
        <CompanyForm
          company={{ ...saved, contacts: Array.from({ length: 10 }, (_, i) => ({ name: `Referente ${i}` })) }}
          submitLabel="Salva modifiche"
          onSubmit={vi.fn()}
          onSuccess={vi.fn()}
        />
      </main>,
    );
    const add = screen.getByRole('button', { name: 'Aggiungi referente' });
    expect(add).toBeDisabled();
    expect(add).toHaveAccessibleDescription('Al massimo 10 referenti.');
  });
});
