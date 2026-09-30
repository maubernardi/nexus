import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';

import { Input } from '@/components/ui/input';
import { FormField } from '@/molecules/FormField/FormField';
import { expectNoAxeViolations } from '@/tests/axe';

describe('<FormField>', () => {
  it('collega etichetta, aiuto ed errore al controllo', async () => {
    render(
      <main>
        <FormField id="cognome" label="Cognome" required hint="Come da documento" error="Campo obbligatorio">
          {(p) => <Input {...p} />}
        </FormField>
      </main>,
    );

    const input = screen.getByRole('textbox', { name: /Cognome/ });
    expect(input).toHaveAccessibleName('Cognome (obbligatorio)');
    expect(input).toHaveAttribute('aria-invalid', 'true');
    expect(input).toHaveAccessibleDescription('Come da documento Campo obbligatorio');
    await expectNoAxeViolations();
  });
});
