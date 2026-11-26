import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';

import { TrainingShipBadge } from '@/atoms/TrainingShipBadge/TrainingShipBadge';
import { expectNoAxeViolations } from '@/tests/axe';

describe('<TrainingShipBadge>', () => {
  it('la nave con «Nave scuola» per il lettore di schermo e come suggerimento', async () => {
    const { container } = render(
      <main>
        <p>
          Trattoria Bianchi <TrainingShipBadge />
        </p>
      </main>,
    );
    expect(screen.getByText('Nave scuola')).toHaveClass('sr-only');
    expect(screen.getByTitle('Nave scuola')).toBeInTheDocument();
    expect(container.querySelector('svg')).toHaveAttribute('aria-hidden', 'true');
    await expectNoAxeViolations();
  });
});
