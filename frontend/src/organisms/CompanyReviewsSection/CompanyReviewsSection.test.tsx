import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { fetchCompanyReviews, type CompanyReview } from '@/config/api/companyApi';
import { CompanyReviewsSection } from '@/organisms/CompanyReviewsSection/CompanyReviewsSection';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderWithProviders } from '@/tests/renderWithProviders';

vi.mock('@/config/api/companyApi', async (importOriginal) => ({
  ...(await importOriginal<object>()),
  fetchCompanyReviews: vi.fn(),
}));

const review = (n: number, overrides: Partial<CompanyReview> = {}): CompanyReview => ({
  endDate: `2026-10-${String(30 - n).padStart(2, '0')}`,
  average: 5,
  tutorName: `Tutor ${n}`,
  ...overrides,
});

describe('<CompanyReviewsSection>', () => {
  beforeEach(() => vi.mocked(fetchCompanyReviews).mockReset());

  it('media delle medie, quanti tirocini e l’elenco con data, faccina, nota e nome del Tutor (US-1104)', async () => {
    vi.mocked(fetchCompanyReviews).mockResolvedValue({
      average: (13 / 3 + 5) / 2,
      reviews: [
        review(0, { endDate: '2026-10-31' }),
        review(1, {
          endDate: '2026-09-30',
          average: 13 / 3,
          note: 'Poca formazione',
          tutorName: 'Tutor Uno',
        }),
      ],
      total: 2,
      page: 0,
      size: 10,
    });
    renderWithProviders(
      <main>
        <CompanyReviewsSection companyId="C1" />
      </main>,
    );
    const section = await screen.findByRole('region', { name: 'Valutazioni dei tirocini' });
    expect(await within(section).findByText('Media di 2 tirocini valutati')).toBeInTheDocument();
    expect(section).toHaveTextContent('Ottima · 4,7');
    const items = within(screen.getByRole('list', { name: 'Valutazioni, dalla più recente' })).getAllByRole(
      'listitem',
    );
    expect(items[0]).toHaveTextContent('Tirocinio finito il 31/10/2026');
    expect(items[1]).toHaveTextContent('Buona · 4,3');
    expect(items[1]).toHaveTextContent('Poca formazione');
    expect(items[1]).toHaveTextContent('Valutazione di Tutor Uno');
    // fino a 10 valutazioni basta una pagina
    expect(within(section).queryByRole('navigation')).not.toBeInTheDocument();
    await expectNoAxeViolations();
  });

  it('più di 10 valutazioni: pagine, senza il menu delle dimensioni', async () => {
    const user = userEvent.setup();
    vi.mocked(fetchCompanyReviews).mockResolvedValue({
      average: 5,
      reviews: Array.from({ length: 10 }, (_, i) => review(i)),
      total: 12,
      page: 0,
      size: 10,
    });
    renderWithProviders(<CompanyReviewsSection companyId="C1" />);
    const nav = await screen.findByRole('navigation', { name: 'Pagine delle valutazioni' });
    expect(screen.queryByRole('combobox')).not.toBeInTheDocument();
    await user.click(within(nav).getByRole('button', { name: 'Successiva' }));
    await waitFor(() => expect(fetchCompanyReviews).toHaveBeenLastCalledWith('C1', 1));
  });

  it('senza valutazioni lo dice', async () => {
    vi.mocked(fetchCompanyReviews).mockResolvedValue({ reviews: [], total: 0, page: 0, size: 10 });
    renderWithProviders(<CompanyReviewsSection companyId="C1" />);
    expect(await screen.findByText('Nessun tirocinio valutato.')).toBeInTheDocument();
  });
});
