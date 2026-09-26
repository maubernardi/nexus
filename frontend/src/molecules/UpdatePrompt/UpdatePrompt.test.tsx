import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, describe, expect, it } from 'vitest';

import { UpdatePrompt } from '@/molecules/UpdatePrompt/UpdatePrompt';
import { expectNoAxeViolations } from '@/tests/axe';
import { pwaRegisterState } from '@/tests/mocks/pwaRegister';

describe('<UpdatePrompt>', () => {
  afterEach(() => {
    pwaRegisterState.needRefresh = false;
    pwaRegisterState.offlineReady = false;
    pwaRegisterState.updateServiceWorker.mockClear();
  });

  it('non mostra nulla senza aggiornamenti', () => {
    render(<UpdatePrompt />);
    expect(screen.queryByRole('button')).not.toBeInTheDocument();
  });

  it("aggiorna solo dopo il click dell'utente", async () => {
    pwaRegisterState.needRefresh = true;
    render(<UpdatePrompt />);
    expect(screen.getByRole('status')).toHaveTextContent('È disponibile una nuova versione');
    expect(pwaRegisterState.updateServiceWorker).not.toHaveBeenCalled();
    await expectNoAxeViolations();

    await userEvent.click(screen.getByRole('button', { name: 'Aggiorna' }));
    expect(pwaRegisterState.updateServiceWorker).toHaveBeenCalledWith(true);
  });

  it('permette di rimandare', async () => {
    pwaRegisterState.needRefresh = true;
    render(<UpdatePrompt />);
    await userEvent.click(screen.getByRole('button', { name: 'Più tardi' }));
    expect(screen.queryByRole('button', { name: 'Aggiorna' })).not.toBeInTheDocument();
  });
});
