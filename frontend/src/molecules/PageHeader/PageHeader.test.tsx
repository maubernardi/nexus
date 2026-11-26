import { screen } from '@testing-library/react';
import { afterEach, describe, expect, it } from 'vitest';

import { BackLink } from '@/molecules/BackLink/BackLink';
import { PageHeader } from '@/molecules/PageHeader/PageHeader';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderRoutes } from '@/tests/renderWithProviders';

describe('<PageHeader>', () => {
  afterEach(() => document.documentElement.style.removeProperty('--page-header-height'));

  it('ritorno e unico titolo insieme; l’altezza va nello spazio di scorrimento e si toglie all’uscita', async () => {
    const { unmount } = renderRoutes({
      routes: [
        {
          path: '/',
          element: (
            <main>
              <PageHeader back={<BackLink to="/aziende">Torna all’elenco delle aziende</BackLink>}>
                Officina Rossi
              </PageHeader>
            </main>
          ),
        },
      ],
      initialPath: '/',
    });
    expect(screen.getByRole('heading', { level: 1, name: 'Officina Rossi' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Torna all’elenco delle aziende' })).toHaveAttribute(
      'href',
      '/aziende',
    );
    // jsdom non calcola le altezze: la variabile c'è, a 0
    expect(document.documentElement.style.getPropertyValue('--page-header-height')).toBe('0px');
    await expectNoAxeViolations();

    unmount();
    expect(document.documentElement.style.getPropertyValue('--page-header-height')).toBe('');
  });
});
