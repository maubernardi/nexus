import { describe, expect, it } from 'vitest';

import { dayBefore, formatIsoDate } from '@/lib/isoDate';

describe('isoDate', () => {
  it('formatta senza fusi orari', () => {
    expect(formatIsoDate('2026-01-01')).toBe('01/01/2026');
  });

  it('il giorno prima, anche a cavallo di mese e anno', () => {
    expect(dayBefore('2026-03-01')).toBe('2026-02-28');
    expect(dayBefore('2024-03-01')).toBe('2024-02-29');
    expect(dayBefore('2026-01-01')).toBe('2025-12-31');
  });
});
