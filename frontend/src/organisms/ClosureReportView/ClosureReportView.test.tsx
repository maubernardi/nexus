import { screen, within } from '@testing-library/react';
import { describe, expect, it } from 'vitest';

import type { ClosureReport } from '@/config/api/ticketApi';
import { ClosureReportView } from '@/organisms/ClosureReportView/ClosureReportView';
import { expectNoAxeViolations } from '@/tests/axe';
import { renderWithProviders } from '@/tests/renderWithProviders';

// come lo manda l'API: i campi vuoti sono omessi
const report: ClosureReport = {
  companySatisfaction: 5,
  beneficiarySatisfaction: 3,
  objectives: 'Autonomia in cucina',
  hired: false,
  noHireReason: { id: 'R1', code: 'AZIENDA_SENZA_RISORSE', name: 'L’azienda non ha risorse' },
  jobSlotStillAvailable: true,
  submittedAt: '2026-10-30T09:30:00Z',
  submittedBy: 'Mario Rossi',
};

const value = (label: string): string | null =>
  screen.getByText(label, { selector: 'dt' }).nextElementSibling?.textContent ?? null;

describe('<ClosureReportView>', () => {
  it('mostra gradimenti, esito e testi, con chi l’ha inviato', async () => {
    renderWithProviders(<ClosureReportView report={report} />);

    const section = screen.getByRole('region', { name: 'Report di restituzione' });
    expect(within(section).getByText(/Inviato da Mario Rossi il/)).toBeInTheDocument();
    expect(value('Soddisfazione dell’azienda')).toBe('5 su 5');
    expect(value('Soddisfazione del beneficiario')).toBe('3 su 5');
    expect(value('Il beneficiario è stato assunto?')).toBe('No: L’azienda non ha risorse');
    expect(value('La mansione è ancora disponibile?')).toBe('Sì');
    expect(value('Obiettivi raggiunti')).toBe('Autonomia in cucina');
    expect(value('Difficoltà riscontrate')).toBe('Non indicate');
    // il report non si modifica: nessun comando
    expect(screen.queryByRole('button')).not.toBeInTheDocument();
    await expectNoAxeViolations();
  });

  it('assunto: la formula di assunzione', () => {
    renderWithProviders(
      <ClosureReportView
        report={{
          ...report,
          hired: true,
          noHireReason: undefined,
          hiringFormula: { id: 'F1', code: 'TEMPO_INDETERMINATO', name: 'Tempo indeterminato' },
        }}
      />,
    );
    expect(value('Il beneficiario è stato assunto?')).toBe('Sì, con Tempo indeterminato');
  });
});
