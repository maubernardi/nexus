import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';

import { SkipLink } from '@/atoms/SkipLink/SkipLink';
import { expectNoAxeViolations } from '@/tests/axe';

describe('<SkipLink>', () => {
  it('sposta il focus sul contenuto principale', async () => {
    render(
      <>
        <SkipLink />
        <main id="main-content" tabIndex={-1}>
          contenuto
        </main>
      </>,
    );
    const user = userEvent.setup();

    await user.tab();
    const link = screen.getByRole('link', { name: 'Salta al contenuto principale' });
    expect(link).toHaveFocus();

    await user.click(link);
    expect(screen.getByRole('main')).toHaveFocus();
    await expectNoAxeViolations();
  });
});
