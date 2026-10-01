import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AxiosError, AxiosHeaders } from 'axios';
import { describe, expect, it, vi } from 'vitest';

import type { Company } from '@/config/api/companyApi';
import { CompanyForm } from '@/organisms/CompanyForm/CompanyForm';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderWithProviders } from '@/tests/renderWithProviders';

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
});
