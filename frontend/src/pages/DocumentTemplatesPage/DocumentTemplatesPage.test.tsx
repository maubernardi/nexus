import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AxiosError, AxiosHeaders } from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { fetchCatalog } from '@/config/api/catalogApi';
import { fetchProjectInternshipTypes } from '@/config/api/documentCatalogApi';
import {
  downloadDocumentTemplateFile,
  fetchDocumentTemplates,
  fetchTemplatePlaceholders,
  replaceDocumentTemplateFile,
  setDocumentTemplateActive,
  uploadDocumentTemplate,
  type DocumentTemplate,
} from '@/config/api/documentTemplateApi';
import { fetchCareAgencies } from '@/config/api/referenceApi';
import { saveBlob } from '@/lib/download';
import { DocumentTemplatesPage } from '@/pages/DocumentTemplatesPage/DocumentTemplatesPage';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderWithProviders } from '@/tests/renderWithProviders';

vi.mock('@/config/api/catalogApi', () => ({ fetchCatalog: vi.fn() }));
vi.mock('@/config/api/documentCatalogApi', async (original) => ({
  ...(await original<Record<string, unknown>>()),
  fetchProjectInternshipTypes: vi.fn(),
}));
vi.mock('@/config/api/referenceApi', async (original) => ({
  ...(await original<Record<string, unknown>>()),
  fetchCareAgencies: vi.fn(),
}));
vi.mock('@/config/api/documentTemplateApi', async (original) => ({
  ...(await original<Record<string, unknown>>()),
  fetchDocumentTemplates: vi.fn(),
  fetchTemplatePlaceholders: vi.fn(),
  uploadDocumentTemplate: vi.fn(),
  replaceDocumentTemplateFile: vi.fn(),
  setDocumentTemplateActive: vi.fn(),
  downloadDocumentTemplateFile: vi.fn(),
}));
vi.mock('@/lib/download', () => ({ saveBlob: vi.fn() }));

const error = (status: number, body: Record<string, unknown>) =>
  new AxiosError(String(status), String(status), undefined, undefined, {
    status,
    statusText: String(status),
    headers: {},
    config: { headers: new AxiosHeaders() },
    data: { status, ...body },
  });

const GOL = { id: 'P1', code: 'GOL', name: 'GOL' };
const ALTRO = { id: 'P2', code: 'ALTRO', name: 'Altro progetto' };
const EXTRA = { id: 'T2', code: 'EXTRACURRICULARE', name: 'Extracurriculare' };
const INCLUSIONE = { id: 'T3', code: 'INCLUSIONE', name: 'Di inclusione' };
const CURRICULARE = { id: 'T1', code: 'CURRICULARE', name: 'Curriculare' };
const COMUNE = { id: 'A1', code: 'COMUNE_DI_FIRENZE', name: 'Comune di Firenze' };

// come lo manda l'API: senza ente il campo è omesso
const convenzione: DocumentTemplate = {
  id: 'D1',
  document: 'CONVENZIONE',
  project: GOL,
  internshipType: EXTRA,
  name: 'Convenzione GOL',
  fileName: 'convenzione.docx',
  sizeBytes: 48_000,
  active: true,
  uploadedAt: '2026-11-11T10:00:00Z',
  updatedAt: '2026-11-11T10:00:00Z',
  version: 0,
};
const vecchio: DocumentTemplate = {
  ...convenzione,
  id: 'D2',
  document: 'PROGETTO_FORMATIVO',
  internshipType: INCLUSIONE,
  careAgency: COMUNE,
  name: 'Progetto GOL vecchio',
  active: false,
  version: 3,
};

const docx = (name = 'modello.docx') =>
  new File(['PK'], name, { type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document' });

const renderPage = () =>
  renderWithProviders(
    <main>
      <DocumentTemplatesPage />
    </main>,
  );

describe('<DocumentTemplatesPage>', () => {
  beforeEach(() => {
    vi.mocked(fetchDocumentTemplates).mockResolvedValue([convenzione, vecchio]);
    vi.mocked(fetchTemplatePlaceholders).mockResolvedValue(['NOME_TIROCINANTE', 'DATA_INIZIO']);
    vi.mocked(fetchCatalog).mockResolvedValue([
      { ...CURRICULARE, active: true },
      { ...EXTRA, active: true },
      { ...INCLUSIONE, active: true },
    ]);
    vi.mocked(fetchProjectInternshipTypes).mockResolvedValue([
      { project: GOL, active: true, internshipTypes: [EXTRA, INCLUSIONE] },
      { project: ALTRO, active: true, internshipTypes: [] },
    ]);
    vi.mocked(fetchCareAgencies).mockResolvedValue([COMUNE]);
    vi.mocked(uploadDocumentTemplate).mockReset();
    vi.mocked(replaceDocumentTemplateFile).mockReset();
    vi.mocked(setDocumentTemplateActive).mockReset();
    vi.mocked(downloadDocumentTemplateFile).mockReset();
    vi.mocked(saveBlob).mockReset();
  });

  it('segnaposto, modulo e modelli caricati con le loro condizioni', async () => {
    const user = userEvent.setup();
    renderPage();

    expect(
      await screen.findByRole('heading', { level: 1, name: 'Modelli dei documenti' }),
    ).toBeInTheDocument();
    await user.click(screen.getByText('Segnaposto che i modelli possono usare'));
    expect(await screen.findByText('{NOME_TIROCINANTE}')).toBeVisible();

    const list = await screen.findByRole('region', { name: 'Modelli caricati' });
    const first = within(list).getByRole('article', { name: 'Convenzione GOL' });
    expect(within(first).getByText('Attivo')).toBeInTheDocument();
    expect(within(first).getByText('GOL · Extracurriculare · Tutti gli enti')).toBeInTheDocument();
    expect(within(first).getByText(/File: convenzione\.docx \(47 KB\)/)).toBeInTheDocument();
    const second = within(list).getByRole('article', { name: 'Progetto GOL vecchio' });
    expect(within(second).getByText('Disattivato')).toBeInTheDocument();
    expect(within(second).getByText('GOL · Di inclusione · Comune di Firenze')).toBeInTheDocument();
    expect(
      within(second).getByRole('button', { name: 'Riattiva «Progetto GOL vecchio»' }),
    ).toBeInTheDocument();
    await expectNoAxeViolations();
  });

  it('caricamento: il tipo tra quelli ammessi dal progetto, poi il messaggio', async () => {
    const user = userEvent.setup();
    vi.mocked(uploadDocumentTemplate).mockResolvedValue({
      ...convenzione,
      id: 'D9',
      name: 'Convenzione 2026',
    });
    renderPage();

    const type = await screen.findByLabelText(/^Tipo di tirocinio/);
    expect(within(type).getByRole('option', { name: 'Scegli prima il progetto' })).toBeInTheDocument();
    await user.selectOptions(screen.getByLabelText(/^Documento/), 'Convenzione');
    await user.selectOptions(await screen.findByRole('combobox', { name: /^Progetto/ }), 'GOL');
    expect(
      within(type)
        .getAllByRole('option')
        .map((o) => o.textContent),
    ).toEqual(['Seleziona…', 'Extracurriculare', 'Di inclusione']);
    // un progetto senza tipi indicati li ammette tutti
    await user.selectOptions(screen.getByRole('combobox', { name: /^Progetto/ }), 'Altro progetto');
    expect(within(type).getAllByRole('option')).toHaveLength(4);
    await user.selectOptions(screen.getByRole('combobox', { name: /^Progetto/ }), 'GOL');
    await user.selectOptions(type, 'Extracurriculare');
    await user.type(screen.getByLabelText(/^Nome del modello/), ' Convenzione 2026 ');
    await user.upload(screen.getByLabelText(/^File Word/), docx('Convenzione 2026.docx'));
    await expectNoAxeViolations();
    await user.click(screen.getByRole('button', { name: 'Carica il modello' }));

    await waitFor(() => expect(uploadDocumentTemplate).toHaveBeenCalled());
    expect(vi.mocked(uploadDocumentTemplate).mock.calls[0][0]).toMatchObject({
      document: 'CONVENZIONE',
      projectId: 'P1',
      internshipTypeId: 'T2',
      careAgencyId: '',
      name: 'Convenzione 2026',
    });
    expect(vi.mocked(uploadDocumentTemplate).mock.calls[0][0].file.name).toBe('Convenzione 2026.docx');
    expect(await screen.findByText('Modello «Convenzione 2026» caricato.')).toBeInTheDocument();
    expect(screen.getByLabelText(/^Nome del modello/)).toHaveValue('');
  });

  it('controlli: campi obbligatori, poi i segnaposto sconosciuti dal server sul file', async () => {
    const user = userEvent.setup();
    vi.mocked(uploadDocumentTemplate).mockRejectedValue(
      error(400, {
        fieldErrors: [{ field: 'file', message: 'Il modello usa segnaposto che NEXUS non conosce: {CUP}' }],
      }),
    );
    renderPage();

    await user.click(await screen.findByRole('button', { name: 'Carica il modello' }));
    const summary = await screen.findByRole('alert');
    expect(summary).toHaveTextContent('Documento: Campo obbligatorio');
    expect(summary).toHaveTextContent('File Word (.docx): Campo obbligatorio');
    await waitFor(() => expect(summary).toHaveFocus());
    expect(uploadDocumentTemplate).not.toHaveBeenCalled();

    await user.selectOptions(screen.getByLabelText(/^Documento/), 'Progetto formativo');
    await user.selectOptions(screen.getByRole('combobox', { name: /^Progetto/ }), 'GOL');
    await user.selectOptions(screen.getByLabelText(/^Tipo di tirocinio/), 'Di inclusione');
    await user.selectOptions(screen.getByLabelText(/^Ente di presa in carico/), 'Comune di Firenze');
    await user.type(screen.getByLabelText(/^Nome del modello/), 'Progetto');
    await user.upload(screen.getByLabelText(/^File Word/), docx());
    await user.click(screen.getByRole('button', { name: 'Carica il modello' }));

    expect(
      await screen.findByRole('link', {
        name: 'File Word (.docx): Il modello usa segnaposto che NEXUS non conosce: {CUP}',
      }),
    ).toBeInTheDocument();
    expect(vi.mocked(uploadDocumentTemplate).mock.calls[0][0]).toMatchObject({ careAgencyId: 'A1' });
    expect(screen.getByLabelText(/^File Word/)).toHaveAttribute('aria-invalid', 'true');
  });

  it('azioni sui modelli: scarica il file, disattiva e sostituisci', async () => {
    const user = userEvent.setup();
    const blob = new Blob(['docx']);
    vi.mocked(downloadDocumentTemplateFile).mockResolvedValue(blob);
    vi.mocked(setDocumentTemplateActive).mockResolvedValue({ ...convenzione, active: false, version: 1 });
    vi.mocked(replaceDocumentTemplateFile)
      .mockRejectedValueOnce(
        error(400, {
          fieldErrors: [{ field: 'file', message: 'Il file deve essere un documento Word .docx' }],
        }),
      )
      .mockResolvedValueOnce({ ...convenzione, version: 1 });
    renderPage();

    await user.click(await screen.findByRole('button', { name: 'Scarica il file di «Convenzione GOL»' }));
    await waitFor(() => expect(saveBlob).toHaveBeenCalledWith(blob, 'convenzione.docx'));
    expect(screen.getByText('File di «Convenzione GOL» scaricato.')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Disattiva «Convenzione GOL»' }));
    expect(vi.mocked(setDocumentTemplateActive).mock.calls[0][0]).toEqual({
      id: 'D1',
      active: false,
      version: 0,
    });
    expect(
      await screen.findByText('Modello «Convenzione GOL» disattivato: non si sceglie più.'),
    ).toBeInTheDocument();

    const replace = screen.getByRole('button', { name: 'Sostituisci il file di «Convenzione GOL»' });
    await user.click(replace);
    expect(replace).toHaveAttribute('aria-expanded', 'true');
    const input = screen.getByLabelText('Nuovo file per «Convenzione GOL» (.docx)');
    await waitFor(() => expect(input).toHaveFocus());
    await user.click(screen.getByRole('button', { name: 'Carica il nuovo file' }));
    expect(screen.getByText('Campo obbligatorio')).toBeInTheDocument();

    await user.upload(input, docx('nuovo.docx'));
    await user.click(screen.getByRole('button', { name: 'Carica il nuovo file' }));
    expect(await screen.findByText('Il file deve essere un documento Word .docx')).toBeInTheDocument();
    expect(input).toHaveAttribute('aria-invalid', 'true');
    await expectNoAxeViolations();

    await user.click(screen.getByRole('button', { name: 'Carica il nuovo file' }));
    expect(await screen.findByText('File di «Convenzione GOL» sostituito.')).toBeInTheDocument();
    expect(vi.mocked(replaceDocumentTemplateFile).mock.calls[1][0]).toMatchObject({ id: 'D1', version: 0 });
    await waitFor(() =>
      expect(screen.getByRole('button', { name: 'Sostituisci il file di «Convenzione GOL»' })).toHaveFocus(),
    );
  });
});
