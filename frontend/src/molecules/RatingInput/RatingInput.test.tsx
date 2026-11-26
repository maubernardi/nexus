import { useState } from 'react';
import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';

import { RatingInput } from '@/molecules/RatingInput/RatingInput';
import { expectNoAxeViolations } from '@/tests/axe';

/** Come nel modulo: il valore scelto torna al componente. */
const Stars = ({
  onChange,
  initial = null,
}: {
  onChange: (v: number | null) => void;
  initial?: number | null;
}) => {
  const [value, setValue] = useState<number | null>(initial);
  return (
    <main>
      <RatingInput
        id="reliability"
        legend="Affidabilità"
        hint="Da 1 a 4 stelle."
        kind="stars"
        value={value}
        onChange={(v) => {
          setValue(v);
          onChange(v);
        }}
      />
    </main>
  );
};

const filled = (group: HTMLElement): string[] =>
  [...group.querySelectorAll('svg')].map((i) => i.dataset.filled ?? '');

describe('<RatingInput>', () => {
  it('quattro stelle vuote; il clic riempie fino a quella scelta', async () => {
    const user = userEvent.setup();
    const onChange = vi.fn();
    render(<Stars onChange={onChange} />);
    const group = screen.getByRole('group', { name: 'Affidabilità' });
    expect(group).toHaveAccessibleDescription('Da 1 a 4 stelle.');
    expect(filled(group)).toEqual(['false', 'false', 'false', 'false']);
    expect(within(group).queryByRole('button', { name: /Togli/ })).not.toBeInTheDocument();
    await expectNoAxeViolations();

    await user.click(within(group).getByRole('radio', { name: '3 stelle su 4' }));
    expect(onChange).toHaveBeenLastCalledWith(3);
    expect(filled(group)).toEqual(['true', 'true', 'true', 'false']);
    expect(within(group).getByRole('radio', { name: '3 stelle su 4' })).toBeChecked();
    await expectNoAxeViolations();
  });

  it('da tastiera con le frecce, come ogni gruppo di opzioni', async () => {
    const user = userEvent.setup();
    const onChange = vi.fn();
    render(<Stars onChange={onChange} initial={2} />);
    const two = screen.getByRole('radio', { name: '2 stelle su 4' });
    two.focus();
    await user.keyboard('{ArrowRight}');
    expect(onChange).toHaveBeenLastCalledWith(3);
    expect(screen.getByRole('radio', { name: '3 stelle su 4' })).toHaveFocus();
  });

  it('«Togli» toglie la valutazione', async () => {
    const user = userEvent.setup();
    const onChange = vi.fn();
    render(<Stars onChange={onChange} initial={4} />);
    await user.click(screen.getByRole('button', { name: 'Togli: Affidabilità' }));
    expect(onChange).toHaveBeenLastCalledWith(null);
    for (const radio of screen.getAllByRole('radio')) expect(radio).not.toBeChecked();
  });

  it('pollici: due opzioni', () => {
    render(
      <RatingInput id="interest" legend="Interesse al progetto" kind="thumbs" value={1} onChange={vi.fn()} />,
    );
    expect(screen.getAllByRole('radio')).toHaveLength(2);
    expect(screen.getByRole('radio', { name: '1 pollice alzato su 2' })).toBeChecked();
    expect(screen.getByRole('radio', { name: '2 pollici alzati su 2' })).not.toBeChecked();
  });
});
