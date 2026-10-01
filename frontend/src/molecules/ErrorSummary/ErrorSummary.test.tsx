import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';

import { ErrorSummary } from '@/molecules/ErrorSummary/ErrorSummary';
import { expectNoAxeViolations } from '@/tests/axe';

describe('<ErrorSummary>', () => {
  it('non mostra nulla senza errori', () => {
    const { container } = render(<ErrorSummary errors={[]} />);
    expect(container).toBeEmptyDOMElement();
  });

  it('elenca gli errori e porta il focus al campo', async () => {
    render(
      <main>
        <ErrorSummary errors={[{ fieldId: 'cognome', message: 'Cognome: Campo obbligatorio' }]} />
        <input id="cognome" aria-label="Cognome" />
      </main>,
    );

    expect(screen.getByRole('alert')).toHaveTextContent('Correggi 1 errore prima di continuare');
    await userEvent.click(screen.getByRole('link', { name: 'Cognome: Campo obbligatorio' }));
    expect(screen.getByRole('textbox', { name: 'Cognome' })).toHaveFocus();
    await expectNoAxeViolations();
  });
});
