import { screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';

import { InternshipsToCloseNotice } from '@/molecules/InternshipsToCloseNotice/InternshipsToCloseNotice';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderWithProviders } from '@/tests/renderWithProviders';

describe('<InternshipsToCloseNotice>', () => {
  it('spiega il blocco ed elenca i tirocini da chiudere, con la fine prevista e il collegamento', async () => {
    renderWithProviders(
      <main>
        <InternshipsToCloseNotice items={[{ ticketId: 'T7', ticketNumber: 7, endDate: '2026-12-10' }]} />
      </main>,
    );

    const region = screen.getByRole('region', { name: 'Prima chiudi i tirocini finiti' });
    expect(region).toHaveTextContent('Non puoi aprire nuove segnalazioni');
    expect(
      screen.getByRole('link', { name: 'Segnalazione n. 7, fine prevista il 10/12/2026' }),
    ).toHaveAttribute('href', '/segnalazioni/T7');
    await expectNoAxeViolations();
  });
});
