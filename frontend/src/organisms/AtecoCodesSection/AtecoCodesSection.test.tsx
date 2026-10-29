import { screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AxiosError, AxiosHeaders } from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { addAtecoCode, fetchAtecoCodes, removeAtecoCode } from '@/config/api/companyLegalDataApi';
import { AtecoCodesSection } from '@/organisms/AtecoCodesSection/AtecoCodesSection';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderWithProviders } from '@/tests/renderWithProviders';

vi.mock('@/config/api/companyLegalDataApi', () => ({
  fetchAtecoCodes: vi.fn(),
  addAtecoCode: vi.fn(),
  removeAtecoCode: vi.fn(),
}));

const renderSection = () =>
  renderWithProviders(
    <main>
      <h1>Azienda</h1>
      <AtecoCodesSection companyId="C1" />
    </main>,
  );

describe('<AtecoCodesSection>', () => {
  beforeEach(() => {
    // come lo manda l'API: la descrizione vuota è omessa
    vi.mocked(fetchAtecoCodes).mockResolvedValue([
      { id: 'A1', code: '47.11' },
      { id: 'A2', code: '56.10.11', description: 'Ristorazione con somministrazione' },
    ]);
    vi.mocked(addAtecoCode).mockReset();
    vi.mocked(removeAtecoCode).mockReset();
  });

  it('elenco e rimozione con esito', async () => {
    const user = userEvent.setup();
    vi.mocked(removeAtecoCode).mockResolvedValue(undefined);
    renderSection();

    const list = await screen.findByRole('list', { name: 'Codici ATECO dell’azienda' });
    expect(within(list).getByText(/Ristorazione con somministrazione/)).toBeInTheDocument();
    await expectNoAxeViolations();
    await user.click(within(list).getByRole('button', { name: 'Togli il codice 47.11' }));

    expect(await screen.findByText('Codice 47.11 tolto.')).toHaveFocus();
    expect(vi.mocked(removeAtecoCode).mock.calls[0][0]).toEqual({ companyId: 'C1', id: 'A1' });
  });

  it('codice scelto nella scheda di un tirocinio: il motivo del server', async () => {
    const user = userEvent.setup();
    vi.mocked(removeAtecoCode).mockRejectedValue(
      new AxiosError('409', '409', undefined, undefined, {
        status: 409,
        statusText: '409',
        headers: {},
        config: { headers: new AxiosHeaders() },
        data: {
          status: 409,
          message: 'Il codice 47.11 è scelto nella scheda del tirocinio della segnalazione n. 9',
        },
      }),
    );
    renderSection();

    const list = await screen.findByRole('list', { name: 'Codici ATECO dell’azienda' });
    await user.click(within(list).getByRole('button', { name: 'Togli il codice 47.11' }));

    expect(
      await screen.findByText('Il codice 47.11 è scelto nella scheda del tirocinio della segnalazione n. 9'),
    ).toHaveFocus();
  });

  it('aggiunta: formato controllato, codice già presente dal server, poi esito', async () => {
    const user = userEvent.setup();
    vi.mocked(addAtecoCode)
      .mockRejectedValueOnce(
        new AxiosError('400', '400', undefined, undefined, {
          status: 400,
          statusText: '400',
          headers: {},
          config: { headers: new AxiosHeaders() },
          data: {
            status: 400,
            fieldErrors: [{ field: 'code', message: 'L’azienda ha già il codice 47.11' }],
          },
        }),
      )
      .mockResolvedValueOnce({ id: 'A3', code: '10.71' });
    renderSection();

    await user.click(await screen.findByRole('button', { name: 'Aggiungi codice' }));
    expect(screen.getByRole('heading', { name: 'Nuovo codice ATECO' })).toHaveFocus();
    const code = screen.getByLabelText(/^Codice/);
    await user.type(code, '4711');
    await user.click(screen.getByRole('button', { name: 'Aggiungi' }));
    expect(
      await screen.findByRole('link', { name: 'Codice: Formato: 56, 56.10 oppure 56.10.11' }),
    ).toBeInTheDocument();
    expect(addAtecoCode).not.toHaveBeenCalled();

    await user.clear(code);
    await user.type(code, '47.11');
    await user.click(screen.getByRole('button', { name: 'Aggiungi' }));
    expect(
      await screen.findByRole('link', { name: 'Codice: L’azienda ha già il codice 47.11' }),
    ).toBeInTheDocument();

    await user.clear(code);
    await user.type(code, '10.71');
    await user.click(screen.getByRole('button', { name: 'Aggiungi' }));
    expect(await screen.findByText('Codice 10.71 aggiunto.')).toHaveFocus();
    expect(vi.mocked(addAtecoCode).mock.calls[1][0]).toEqual({
      companyId: 'C1',
      code: '10.71',
      description: null,
    });
  });
});
