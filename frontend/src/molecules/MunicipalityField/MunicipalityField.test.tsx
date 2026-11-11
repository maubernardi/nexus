import { useState } from 'react';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { searchMunicipalities, type Municipality } from '@/config/api/municipalityApi';
import { MunicipalityField } from '@/molecules/MunicipalityField/MunicipalityField';
import {
  addressLine,
  isUnchosen,
  toMunicipalityValue,
  type MunicipalityValue,
} from '@/molecules/MunicipalityField/municipalityValue';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderWithProviders } from '@/tests/renderWithProviders';

vi.mock('@/config/api/municipalityApi', () => ({ searchMunicipalities: vi.fn() }));

const municipality = (id: string, name: string, provinceAbbreviation: string): Municipality => ({
  id,
  name,
  istatCode: '048043',
  cadastralCode: 'I684',
  provinceName: 'Provincia',
  provinceAbbreviation,
  validFrom: '2026-01-01',
});

const SESTO = municipality('M1', 'Sesto Fiorentino', 'FI');
const SESTRIERE = municipality('M2', 'Sestriere', 'TO');

/** La casella dentro un modulo finto, con il comune scelto visibile per le verifiche. */
const Harness = ({ initial = { text: '' } }: { initial?: MunicipalityValue }) => {
  const [value, setValue] = useState<MunicipalityValue>(initial);
  return (
    <main>
      <label htmlFor="comune">Comune</label>
      <MunicipalityField id="comune" aria-invalid={false} value={value} onChange={setValue} />
      <p data-testid="chosen">{value.municipality?.id ?? 'nessuno'}</p>
    </main>
  );
};

describe('<MunicipalityField>', () => {
  beforeEach(() => {
    vi.mocked(searchMunicipalities).mockReset();
    vi.mocked(searchMunicipalities).mockResolvedValue([SESTO, SESTRIERE]);
  });

  it('suggerisce dalla seconda lettera e si sceglie da tastiera', async () => {
    const user = userEvent.setup();
    renderWithProviders(<Harness />);
    const input = screen.getByRole('combobox', { name: 'Comune' });

    await user.type(input, 's');
    expect(input).toHaveAttribute('aria-expanded', 'false');
    await user.type(input, 'est');

    const list = await screen.findByRole('listbox', { name: 'Comuni suggeriti' });
    expect(searchMunicipalities).toHaveBeenLastCalledWith({ q: 'sest', history: false });
    expect(input).toHaveAttribute('aria-expanded', 'true');
    expect(input).toHaveAttribute('aria-controls', list.id);
    expect(screen.getByRole('status')).toHaveTextContent('2 comuni: scegli con le frecce e Invio');
    await expectNoAxeViolations();

    await user.keyboard('{ArrowDown}{ArrowDown}');
    const sestriere = screen.getByRole('option', { name: 'Sestriere (TO)' });
    expect(sestriere).toHaveAttribute('aria-selected', 'true');
    expect(input).toHaveAttribute('aria-activedescendant', sestriere.id);
    // in fondo si riparte dal primo
    await user.keyboard('{ArrowDown}');
    expect(screen.getByRole('option', { name: 'Sesto Fiorentino (FI)' })).toHaveAttribute(
      'aria-selected',
      'true',
    );
    await user.keyboard('{Enter}');

    expect(input).toHaveValue('Sesto Fiorentino (FI)');
    expect(input).toHaveAttribute('aria-expanded', 'false');
    expect(input).not.toHaveAttribute('aria-activedescendant');
    expect(input).toHaveFocus();
    expect(screen.getByTestId('chosen')).toHaveTextContent('M1');
  });

  it('si sceglie anche con il mouse, senza perdere il focus', async () => {
    const user = userEvent.setup();
    renderWithProviders(<Harness />);
    const input = screen.getByRole('combobox', { name: 'Comune' });
    await user.type(input, 'sest');
    await user.click(await screen.findByRole('option', { name: 'Sestriere (TO)' }));

    expect(input).toHaveValue('Sestriere (TO)');
    expect(input).toHaveFocus();
    expect(screen.getByTestId('chosen')).toHaveTextContent('M2');
  });

  it('scrivere dopo la scelta la annulla; Esc chiude e poi svuota', async () => {
    const user = userEvent.setup();
    renderWithProviders(<Harness initial={toMunicipalityValue(SESTO)} />);
    const input = screen.getByRole('combobox', { name: 'Comune' });
    expect(input).toHaveValue('Sesto Fiorentino (FI)');

    await user.clear(input);
    await user.type(input, 'sest');
    expect(screen.getByTestId('chosen')).toHaveTextContent('nessuno');
    await screen.findByRole('listbox');

    await user.keyboard('{Escape}');
    expect(input).toHaveAttribute('aria-expanded', 'false');
    expect(input).toHaveValue('sest');
    await user.keyboard('{Escape}');
    expect(input).toHaveValue('');
  });

  it('senza risultati lo dice', async () => {
    vi.mocked(searchMunicipalities).mockResolvedValue([]);
    const user = userEvent.setup();
    renderWithProviders(<Harness />);
    await user.type(screen.getByRole('combobox', { name: 'Comune' }), 'zzz');

    await waitFor(() => expect(screen.getByRole('status')).toHaveTextContent('Nessun comune trovato'));
    expect(screen.queryByRole('listbox')).not.toBeInTheDocument();
  });

  it('con l’elenco al limite avvisa che ci possono essere altri comuni', async () => {
    vi.mocked(searchMunicipalities).mockResolvedValue(
      Array.from({ length: 20 }, (_, i) => municipality(`M${i}`, `Pra ${i}`, 'TO')),
    );
    const user = userEvent.setup();
    renderWithProviders(<Harness />);
    await user.type(screen.getByRole('combobox', { name: 'Comune' }), 'pra');

    const text = 'Solo i primi 20 comuni: scrivi altre lettere per trovare quello giusto.';
    await waitFor(() => expect(screen.getByRole('status')).toHaveTextContent(text));
    // visibile in fondo all'elenco, non sotto la casella dove l'elenco lo coprirebbe
    const note = screen.getAllByText(text).find((el) => el.getAttribute('role') !== 'status');
    expect(note).toHaveAttribute('aria-hidden', 'true');
    expect(screen.getByRole('listbox').parentElement).toContainElement(note ?? null);
  });

  it('valore e testo per i moduli e i dettagli', () => {
    expect(isUnchosen({ text: 'Sesto' })).toBe(true);
    expect(isUnchosen({ text: '  ' })).toBe(false);
    expect(isUnchosen(toMunicipalityValue(SESTO))).toBe(false);
    expect(addressLine('Via Roma 1', SESTO)).toBe('Via Roma 1, Sesto Fiorentino (FI)');
    expect(addressLine(null, SESTO)).toBe('Sesto Fiorentino (FI)');
    expect(addressLine(' ', undefined)).toBeUndefined();
  });
});
