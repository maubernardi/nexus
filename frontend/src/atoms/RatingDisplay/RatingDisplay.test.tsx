import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';

import { RatingDisplay } from '@/atoms/RatingDisplay/RatingDisplay';
import { expectNoAxeViolations } from '@/tests/axe';

describe('<RatingDisplay>', () => {
  it('stelle: piene fino al valore, con il testo per il lettore di schermo', async () => {
    const { container } = render(
      <main>
        <RatingDisplay kind="stars" value={3} />
      </main>,
    );
    expect(screen.getByText('3 stelle su 4')).toHaveClass('sr-only');
    const icons = container.querySelectorAll('svg');
    expect(icons).toHaveLength(4);
    expect([...icons].map((i) => i.dataset.filled)).toEqual(['true', 'true', 'true', 'false']);
    await expectNoAxeViolations();
  });

  it('un pollice su due', () => {
    render(<RatingDisplay kind="thumbs" value={1} />);
    expect(screen.getByText('1 pollice alzato su 2')).toBeInTheDocument();
  });

  it('senza valutazione: un trattino, e «Nessuna valutazione» per il lettore di schermo', () => {
    const { container } = render(<RatingDisplay kind="stars" value={null} />);
    expect(container).toHaveTextContent('—');
    expect(screen.getByText('Nessuna valutazione')).toHaveClass('sr-only');
    expect(container.querySelector('svg')).toBeNull();
  });
});
