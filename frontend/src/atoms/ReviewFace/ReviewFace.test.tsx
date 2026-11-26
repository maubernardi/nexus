import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';

import { ReviewFace } from '@/atoms/ReviewFace/ReviewFace';
import { formatAverage, reviewAverage, reviewLevel } from '@/lib/reviews';
import { expectNoAxeViolations } from '@/tests/axe';

describe('<ReviewFace>', () => {
  it('faccina del numero intero più vicino, media con un decimale e testo per il lettore di schermo', async () => {
    const { container } = render(
      <main>
        <ReviewFace average={13 / 3} withWord />
      </main>,
    );
    expect(container.querySelector('svg')).toHaveAttribute('data-level', '4');
    expect(container).toHaveTextContent('Buona · 4,3');
    expect(screen.getByText('Media 4,3 su 5: buona')).toHaveClass('sr-only');
    await expectNoAxeViolations();
  });

  it('livelli e medie', () => {
    expect(reviewLevel(4.33)).toBe(4);
    expect(reviewLevel(4.67)).toBe(5);
    expect(reviewLevel(4.5)).toBe(5);
    expect(reviewLevel(0.2)).toBe(1);
    expect(formatAverage(5)).toBe('5,0');
    expect(reviewAverage([4, 5, null])).toBeNull();
    expect(reviewAverage([4, 5, 4])).toBeCloseTo(4.333, 3);
  });
});
