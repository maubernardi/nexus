import { useState } from 'react';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';

import { useFocusAfterRender } from '@/hooks/useFocusAfterRender';

/** Un messaggio che compare con lo stesso aggiornamento che chiede il focus, come gli esiti dei moduli. */
const Harness = () => {
  const [message, setMessage] = useState<string | null>(null);
  const { ref, requestFocus } = useFocusAfterRender<HTMLParagraphElement>();
  return (
    <div>
      <button
        type="button"
        onClick={() => {
          setMessage('Salvataggio non riuscito');
          requestFocus();
        }}
      >
        Salva
      </button>
      <button type="button">Altro</button>
      {message && (
        <p ref={ref} role="alert" tabIndex={-1}>
          {message}
        </p>
      )}
    </div>
  );
};

describe('useFocusAfterRender', () => {
  it('senza richieste non sposta il focus', () => {
    render(<Harness />);
    expect(document.body).toHaveFocus();
  });

  it('il focus arriva sull’elemento che compare con la richiesta', async () => {
    const user = userEvent.setup();
    render(<Harness />);
    await user.click(screen.getByRole('button', { name: 'Salva' }));
    expect(screen.getByRole('alert')).toHaveFocus();
  });

  it('una nuova richiesta con lo stesso messaggio riporta il focus', async () => {
    const user = userEvent.setup();
    render(<Harness />);
    await user.click(screen.getByRole('button', { name: 'Salva' }));
    await user.click(screen.getByRole('button', { name: 'Altro' }));
    expect(screen.getByRole('button', { name: 'Altro' })).toHaveFocus();

    await user.click(screen.getByRole('button', { name: 'Salva' }));
    expect(screen.getByRole('alert')).toHaveFocus();
  });
});
