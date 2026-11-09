import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AxiosError, AxiosHeaders } from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import {
  fetchMunicipalityStatus,
  importMunicipalities,
  previewMunicipalityImport,
  searchMunicipalities,
  type MunicipalityChanges,
  type MunicipalityImportResult,
} from '@/config/api/municipalityApi';
import { MunicipalitiesSection } from '@/organisms/MunicipalitiesSection/MunicipalitiesSection';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderWithProviders } from '@/tests/renderWithProviders';

vi.mock('@/config/api/municipalityApi', async (original) => ({
  ...(await original<Record<string, unknown>>()),
  fetchMunicipalityStatus: vi.fn(),
  searchMunicipalities: vi.fn(),
  previewMunicipalityImport: vi.fn(),
  importMunicipalities: vi.fn(),
}));

const none: MunicipalityChanges = {
  added: 0,
  removed: 0,
  changed: 0,
  addedExamples: [],
  removedExamples: [],
  changedExamples: [],
};

const preview: MunicipalityImportResult = {
  rows: 7896,
  municipalities: {
    added: 1,
    removed: 1,
    changed: 102,
    addedExamples: ['Vaiano (PO, 100006)'],
    removedExamples: ['Fiesole (FI, 048015)'],
    changedExamples: ['Sesto Fiorentino (FI, 048043) → Sesto Fiorentino (PO, 100099)'],
  },
  provinces: none,
};

const badRequest = (message: string) =>
  new AxiosError('400', '400', undefined, undefined, {
    status: 400,
    statusText: '400',
    headers: {},
    config: { headers: new AxiosHeaders() },
    data: { status: 400, message },
  });

const csv = () => new File(['x'], 'Elenco-comuni-italiani.csv', { type: 'text/csv' });

const renderSection = () =>
  renderWithProviders(
    <main>
      <h1>Cataloghi</h1>
      <MunicipalitiesSection />
    </main>,
  );

describe('<MunicipalitiesSection>', () => {
  beforeEach(() => {
    vi.mocked(fetchMunicipalityStatus).mockResolvedValue({
      municipalities: 7896,
      provinces: 107,
      lastImport: {
        validFrom: '2026-01-01',
        importedAt: '2026-01-02T09:30:00Z',
        importedBy: 'Amministratore',
      },
    });
    vi.mocked(searchMunicipalities).mockReset();
    vi.mocked(previewMunicipalityImport).mockReset();
    vi.mocked(importMunicipalities).mockReset();
  });

  it('mostra lo stato dell’elenco', async () => {
    renderSection();
    expect(await screen.findByText('7896 comuni e 107 province validi oggi.')).toBeInTheDocument();
    expect(
      screen.getByText(/elenco valido dal 01\/01\/2026, caricato il .* da Amministratore/),
    ).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /Pagina dell’ISTAT.*nuova scheda/ })).toHaveAttribute(
      'target',
      '_blank',
    );
    await expectNoAxeViolations();
  });

  it('elenco vuoto', async () => {
    vi.mocked(fetchMunicipalityStatus).mockResolvedValue({ municipalities: 0, provinces: 0 });
    renderSection();
    expect(await screen.findByText('L’elenco è vuoto: carica il file dell’ISTAT.')).toBeInTheDocument();
  });

  it('controlla il file, mostra le differenze e conferma', async () => {
    vi.mocked(previewMunicipalityImport).mockResolvedValue(preview);
    vi.mocked(importMunicipalities).mockResolvedValue(preview);
    const user = userEvent.setup({ applyAccept: false });
    renderSection();

    await user.click(await screen.findByRole('button', { name: 'Controlla il file' }));
    expect(await screen.findByRole('alert')).toHaveTextContent('Scegli il file dell’ISTAT.');

    await user.upload(screen.getByLabelText('File dell’ISTAT (CSV)'), new File(['x'], 'elenco.xlsx'));
    await user.click(screen.getByRole('button', { name: 'Controlla il file' }));
    expect(await screen.findByRole('alert')).toHaveTextContent('Il file deve essere in formato CSV.');
    expect(previewMunicipalityImport).not.toHaveBeenCalled();

    const file = csv();
    await user.upload(screen.getByLabelText('File dell’ISTAT (CSV)'), file);
    const date = screen.getByLabelText('Valido dal');
    await user.clear(date);
    await user.type(date, '2026-03-01');
    await user.click(screen.getByRole('button', { name: 'Controlla il file' }));

    const region = await screen.findByRole('region', { name: 'Differenze rispetto all’elenco attuale' });
    expect(within(region).getByText('Il file contiene 7896 comuni.')).toBeInTheDocument();
    expect(within(region).getByText('Comuni: 1 nuovi, 1 soppressi, 102 cambiati.')).toBeInTheDocument();
    expect(within(region).getByText('Comuni cambiati (102)')).toBeInTheDocument();
    expect(within(region).getByText('e altri 101')).toBeInTheDocument();
    expect(within(region).queryByText(/Province nuove/)).not.toBeInTheDocument();
    await waitFor(() => expect(within(region).getByRole('heading', { level: 4 })).toHaveFocus());
    await expectNoAxeViolations();

    await user.click(within(region).getByRole('button', { name: 'Conferma il caricamento' }));
    expect(vi.mocked(importMunicipalities).mock.calls[0][0]).toEqual({ file, validFrom: '2026-03-01' });
    expect(
      await screen.findByText(
        'Elenco caricato, valido dal 01/03/2026: 1 comuni nuovi, 1 soppressi, 102 cambiati.',
      ),
    ).toBeInTheDocument();
    expect(screen.queryByRole('region', { name: /Differenze/ })).not.toBeInTheDocument();
  });

  it('nessuna differenza: niente da confermare', async () => {
    vi.mocked(previewMunicipalityImport).mockResolvedValue({
      rows: 7896,
      municipalities: none,
      provinces: none,
    });
    const user = userEvent.setup({ applyAccept: false });
    renderSection();
    await user.upload(await screen.findByLabelText('File dell’ISTAT (CSV)'), csv());
    await user.click(screen.getByRole('button', { name: 'Controlla il file' }));
    expect(await screen.findByText('Nessuna differenza: l’elenco è già aggiornato.')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Conferma il caricamento' })).not.toBeInTheDocument();
  });

  it('il motivo del rifiuto viene dal server', async () => {
    vi.mocked(previewMunicipalityImport).mockRejectedValue(
      badRequest('Il file non è l’elenco dei comuni dell’ISTAT: manca la colonna «sigla automobilistica»'),
    );
    const user = userEvent.setup({ applyAccept: false });
    renderSection();
    await user.upload(await screen.findByLabelText('File dell’ISTAT (CSV)'), csv());
    await user.click(screen.getByRole('button', { name: 'Controlla il file' }));
    expect(await screen.findByRole('alert')).toHaveTextContent('manca la colonna «sigla automobilistica»');
  });

  it('cerca un comune, anche tra le versioni passate', async () => {
    vi.mocked(searchMunicipalities).mockResolvedValue([
      {
        id: 'M2',
        name: 'Sesto Fiorentino',
        istatCode: '100099',
        cadastralCode: 'I684',
        provinceName: 'Prato',
        provinceAbbreviation: 'PO',
        validFrom: '2026-03-01',
      },
      {
        id: 'M1',
        name: 'Sesto Fiorentino',
        istatCode: '048043',
        cadastralCode: 'I684',
        provinceName: 'Firenze',
        provinceAbbreviation: 'FI',
        validFrom: '2026-01-01',
        validTo: '2026-03-01',
      },
    ]);
    const user = userEvent.setup();
    renderSection();
    await user.type(await screen.findByLabelText('Nome del comune'), 'sesto');

    expect(await screen.findByText('2 risultati')).toBeInTheDocument();
    expect(searchMunicipalities).toHaveBeenLastCalledWith({ q: 'sesto', history: true });
    expect(screen.getByText(/codice ISTAT 100099, valido dal 01\/03\/2026$/)).toBeInTheDocument();
    expect(
      screen.getByText(/codice ISTAT 048043, valido dal 01\/01\/2026 al 28\/02\/2026/),
    ).toBeInTheDocument();
    await expectNoAxeViolations();
  });
});
