import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';

import { TrainingShipBadge } from '@/atoms/TrainingShipBadge/TrainingShipBadge';
import { expectNoAxeViolations } from '@/tests/axe';

describe('<TrainingShipBadge>', () => {
  it('bollino con la nave, «Nave scuola» per il lettore di schermo e come suggerimento', async () => {
    const { container } = render(
      <main>
        <p>
          Trattoria Bianchi <TrainingShipBadge />
        </p>
      </main>,
    );
    expect(screen.getByText('Nave scuola')).toHaveClass('sr-only');
    const badge = screen.getByTitle('Nave scuola');
    // più grande e staccato dal nome (PO, 26/11, dal collaudo)
    expect(badge).toHaveClass('size-7', 'ml-1.5', 'bg-gold');
    expect(container.querySelector('svg')).toHaveAttribute('aria-hidden', 'true');
    await expectNoAxeViolations();
  });

  it('nei titoli più grande', () => {
    render(<TrainingShipBadge size="lg" />);
    expect(screen.getByTitle('Nave scuola')).toHaveClass('size-10');
  });

  it('decorativo accanto a un testo che lo dice già: niente testo né suggerimento', () => {
    const { container } = render(
      <p>
        Nave scuola
        <TrainingShipBadge decorative />
      </p>,
    );
    expect(container.querySelector('[aria-hidden="true"][data-size]')).toBeInTheDocument();
    expect(screen.queryByTitle('Nave scuola')).not.toBeInTheDocument();
    expect(container).toHaveTextContent(/^Nave scuola$/);
  });
});
