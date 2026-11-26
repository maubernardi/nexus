import { screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';

import { BackLink } from '@/molecules/BackLink/BackLink';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderRoutes } from '@/tests/renderWithProviders';

describe('<BackLink>', () => {
  it('collegamento con la freccia nascosta al lettore di schermo', async () => {
    renderRoutes({
      routes: [
        {
          path: '/',
          element: (
            <main>
              <BackLink to="/aziende">Torna all’elenco delle aziende</BackLink>
            </main>
          ),
        },
      ],
      initialPath: '/',
    });
    const link = screen.getByRole('link', { name: 'Torna all’elenco delle aziende' });
    expect(link).toHaveAttribute('href', '/aziende');
    expect(link.querySelector('svg')).toHaveAttribute('aria-hidden', 'true');
    await expectNoAxeViolations();
  });
});
