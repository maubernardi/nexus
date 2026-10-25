import { screen, within } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';

import { fetchCompanyHistory, type CompanyHistory } from '@/config/api/companyHistoryApi';
import { CompanyHistorySection } from '@/organisms/CompanyHistorySection/CompanyHistorySection';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderWithProviders } from '@/tests/renderWithProviders';

vi.mock('@/config/api/companyHistoryApi', () => ({
  fetchCompanyHistory: vi.fn(),
}));

const empty: CompanyHistory = {
  summary: {
    proposals: 0,
    accepted: 0,
    failedAppointments: 0,
    internships: 0,
    hirings: 0,
    negativeOutcomes: 0,
  },
  events: [],
};

describe('<CompanyHistorySection>', () => {
  it('mostra sempre tutte le voci del riepilogo, anche a zero', async () => {
    vi.mocked(fetchCompanyHistory).mockResolvedValue(empty);
    renderWithProviders(<CompanyHistorySection companyId="C1" />);

    expect(await screen.findByText('Nessun evento registrato.')).toBeInTheDocument();
    const summary = screen.getByLabelText('Riepilogo dei pregressi');
    for (const label of [
      'Proposte',
      'Accettate',
      'Appuntamenti falliti',
      'Tirocini',
      'Assunzioni',
      'Esiti negativi',
    ]) {
      expect(within(summary).getByText(label).nextElementSibling).toHaveTextContent('0');
    }
    await expectNoAxeViolations();
  });

  it('elenca gli eventi con segnalazione collegata, dettaglio, data e autore', async () => {
    vi.mocked(fetchCompanyHistory).mockResolvedValue({
      summary: { ...empty.summary, proposals: 1 },
      events: [
        {
          at: '2026-10-20T09:30:00Z',
          type: 'ESCLUSIONE',
          ticketId: 'T7',
          ticketNumber: 7,
          detail: 'Orari non compatibili',
          authorName: 'Paolo Verdi',
        },
        {
          at: '2026-10-10T09:30:00Z',
          type: 'PROPOSTA_INVIATA',
          ticketId: 'T7',
          ticketNumber: 7,
          detail: '«Magazziniere»',
          authorName: 'Paolo Verdi',
        },
        {
          at: '2026-10-01T09:30:00Z',
          type: 'NUOVO_TIPO',
          ticketId: null,
          ticketNumber: null,
          detail: null,
          authorName: 'Sistema',
        },
      ],
    });
    renderWithProviders(<CompanyHistorySection companyId="C1" />);

    const items = await screen.findAllByRole('listitem');
    expect(items[0]).toHaveTextContent('Azienda esclusa – segnalazione n. 7');
    expect(items[0]).toHaveTextContent('Orari non compatibili');
    expect(within(items[0]).getByRole('link', { name: 'segnalazione n. 7' })).toHaveAttribute(
      'href',
      '/segnalazioni/T7',
    );
    expect(items[1]).toHaveTextContent('Proposta inviata');
    expect(items[1]).toHaveTextContent('«Magazziniere»');
    // un tipo non ancora noto all'interfaccia si mostra con il suo codice
    expect(items[2]).toHaveTextContent('NUOVO_TIPO');
    await expectNoAxeViolations();
  });
});
