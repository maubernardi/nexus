import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';

import { Input } from '@/components/ui/input';
import { FormField } from '@/molecules/FormField/FormField';
import { expectNoAxeViolations } from '@/tests/axe';

const renderField = () =>
  render(
    <main>
      <FormField id="piva" label="Partita IVA" required hint="11 cifre, senza prefisso IT.">
        {(p) => <Input {...p} />}
      </FormField>
    </main>,
  );

describe('<InfoTip> nel FormField', () => {
  it('il suggerimento è nascosto ma descrive comunque il campo', async () => {
    renderField();
    const tip = screen.getByRole('tooltip', { hidden: true });
    expect(tip).toHaveClass('sr-only');
    expect(screen.getByRole('textbox', { name: /Partita IVA/ })).toHaveAccessibleDescription(
      '11 cifre, senza prefisso IT.',
    );
    expect(screen.getByRole('button', { name: 'Informazioni su Partita IVA' })).toHaveAttribute(
      'aria-expanded',
      'false',
    );
    await expectNoAxeViolations();
  });

  it('si apre al focus e al clic, si chiude con Esc senza perdere il focus', async () => {
    const user = userEvent.setup();
    renderField();
    const button = screen.getByRole('button', { name: 'Informazioni su Partita IVA' });

    // l'icona precede il campo: è il primo elemento raggiunto con Tab
    await user.tab();
    expect(button).toHaveFocus();
    expect(button).toHaveAttribute('aria-expanded', 'true');
    expect(screen.getByRole('tooltip')).not.toHaveClass('sr-only');
    await expectNoAxeViolations();

    await user.keyboard('{Escape}');
    expect(button).toHaveAttribute('aria-expanded', 'false');
    expect(button).toHaveFocus();

    await user.click(button);
    expect(button).toHaveAttribute('aria-expanded', 'true');
  });

  it('si apre al passaggio del mouse e il clic non lo richiude', async () => {
    const user = userEvent.setup();
    renderField();
    const button = screen.getByRole('button', { name: 'Informazioni su Partita IVA' });
    await user.hover(button);
    expect(screen.getByRole('tooltip')).toHaveTextContent('11 cifre, senza prefisso IT.');
    await user.click(button);
    expect(button).toHaveAttribute('aria-expanded', 'true');
  });
});
