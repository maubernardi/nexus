import { describe, expect, it } from 'vitest';

import { suggestUsername } from '@/lib/username';

describe('suggestUsername', () => {
  it('nome.cognome in minuscolo, senza accenti, spazi e apostrofi', () => {
    expect(suggestUsername('Mario', 'Rossi')).toBe('mario.rossi');
    expect(suggestUsername(' Niccolò ', "D'Àngelo De Santis")).toBe('niccolo.dangelodesantis');
    expect(suggestUsername('', 'Rossi')).toBe('rossi');
  });
});
