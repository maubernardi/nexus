import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AxiosError, AxiosHeaders } from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import {
  createCatalogItem,
  fetchCatalog,
  renameCatalogItem,
  setCatalogItemActive,
  type CatalogItem,
} from '@/config/api/catalogApi';
import { CatalogSection } from '@/organisms/CatalogSection/CatalogSection';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderWithProviders } from '@/tests/renderWithProviders';

vi.mock('@/config/api/catalogApi', () => ({
  fetchCatalog: vi.fn(),
  createCatalogItem: vi.fn(),
  renameCatalogItem: vi.fn(),
  setCatalogItemActive: vi.fn(),
}));

const item = (id: string, name: string, code: string, active = true): CatalogItem => ({
  id,
  code,
  name,
  active,
});

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
      <h1>Cataloghi</h1>
      <CatalogSection kind="zones" />
    </main>,
  );

describe('<CatalogSection>', () => {
  beforeEach(() => {
    vi.mocked(fetchCatalog).mockResolvedValue([
      item('Z1', 'Firenze Nord', 'FIRENZE_NORD'),
      item('Z2', 'Vecchia zona', 'VECCHIA_ZONA', false),
    ]);
    vi.mocked(createCatalogItem).mockReset();
    vi.mocked(renameCatalogItem).mockReset();
    vi.mocked(setCatalogItemActive).mockReset();
  });

  it('elenco con stato in testo e comandi con il nome della voce', async () => {
    renderSection();

    const table = await screen.findByRole('table', { name: 'Zone: 2 voci' });
    const [active, inactive] = within(table).getAllByRole('row').slice(1);
    expect(within(active).getByText('Attiva')).toBeInTheDocument();
    expect(within(active).getByText('FIRENZE_NORD')).toBeInTheDocument();
    expect(within(active).getByRole('button', { name: 'Rinomina Firenze Nord' })).toBeInTheDocument();
    expect(within(active).getByRole('button', { name: 'Disattiva Firenze Nord' })).toBeInTheDocument();
    expect(within(inactive).getByText('Disattivata')).toBeInTheDocument();
    expect(within(inactive).getByRole('button', { name: 'Riattiva Vecchia zona' })).toBeInTheDocument();
    await expectNoAxeViolations();
  });

  it('aggiunge una zona: nome obbligatorio, nome già presente dal server, poi esito con il codice', async () => {
    const user = userEvent.setup();
    vi.mocked(createCatalogItem)
      .mockRejectedValueOnce(
        error(400, { fieldErrors: [{ field: 'name', message: '«Firenze Nord» è già nel catalogo' }] }),
      )
      .mockResolvedValueOnce(item('Z3', 'Sesto Fiorentino', 'SESTO_FIORENTINO'));
    renderSection();

    await user.click(await screen.findByRole('button', { name: 'Aggiungi zona' }));
    expect(screen.getByRole('heading', { name: 'Nuova zona' })).toHaveFocus();
    await user.click(screen.getByRole('button', { name: 'Aggiungi' }));
    expect(await screen.findByRole('link', { name: 'Nome: Campo obbligatorio' })).toBeInTheDocument();
    expect(createCatalogItem).not.toHaveBeenCalled();

    const name = screen.getByLabelText(/^Nome/);
    await user.type(name, 'firenze nord');
    await user.click(screen.getByRole('button', { name: 'Aggiungi' }));
    expect(
      await screen.findByRole('link', { name: 'Nome: «Firenze Nord» è già nel catalogo' }),
    ).toBeInTheDocument();

    await user.clear(name);
    await user.type(name, 'Sesto Fiorentino');
    await user.click(screen.getByRole('button', { name: 'Aggiungi' }));
    const outcome = await screen.findByText(
      '«Sesto Fiorentino» aggiunta al catalogo (codice SESTO_FIORENTINO).',
    );
    await waitFor(() => expect(outcome).toHaveFocus());
    expect(vi.mocked(createCatalogItem).mock.calls[1][0]).toEqual({
      kind: 'zones',
      name: 'Sesto Fiorentino',
    });
  });

  it('rinomina una voce', async () => {
    const user = userEvent.setup();
    vi.mocked(renameCatalogItem).mockResolvedValue(item('Z1', 'Firenze Nord-Est', 'FIRENZE_NORD'));
    renderSection();

    await user.click(await screen.findByRole('button', { name: 'Rinomina Firenze Nord' }));
    expect(screen.getByRole('heading', { name: 'Nuovo nome per «Firenze Nord»' })).toHaveFocus();
    const name = screen.getByLabelText(/^Nome/);
    expect(name).toHaveValue('Firenze Nord');
    await user.clear(name);
    await user.type(name, 'Firenze Nord-Est');
    await user.click(screen.getByRole('button', { name: 'Salva' }));

    expect(await screen.findByText('Nome salvato: «Firenze Nord-Est».')).toHaveFocus();
    expect(vi.mocked(renameCatalogItem).mock.calls[0][0]).toEqual({
      kind: 'zones',
      id: 'Z1',
      name: 'Firenze Nord-Est',
    });
  });

  it('disattiva; riattivare con un nome già attivo è rifiutato con il motivo', async () => {
    const user = userEvent.setup();
    vi.mocked(setCatalogItemActive)
      .mockResolvedValueOnce(item('Z1', 'Firenze Nord', 'FIRENZE_NORD', false))
      .mockRejectedValueOnce(
        error(409, { message: '«Vecchia zona» è già attiva: rinomina questa voce prima di riattivarla' }),
      );
    renderSection();

    await user.click(await screen.findByRole('button', { name: 'Disattiva Firenze Nord' }));
    expect(
      await screen.findByText(
        '«Firenze Nord» disattivata: non si può più scegliere, resta sui dati che la usano.',
      ),
    ).toHaveFocus();

    await user.click(screen.getByRole('button', { name: 'Riattiva Vecchia zona' }));
    expect(
      await screen.findByText(
        'Operazione non possibile: «Vecchia zona» è già attiva: rinomina questa voce prima di riattivarla',
      ),
    ).toHaveFocus();
  });
});
