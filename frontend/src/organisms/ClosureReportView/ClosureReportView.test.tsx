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
  it('mostra fine effettiva, gradimenti, esito e testi, con chi l’ha inviato', async () => {
    renderWithProviders(
      <ClosureReportView
        report={{
          ...report,
          actualEndDate: '2027-05-31',
          closureReason: { id: 'C2', code: 'INTERRUZIONE_AZIENDA', name: 'Interruzione anticipata' },
          interruptionDetails: 'L’azienda ha chiuso il reparto',
        }}
      />,
    );

    const section = screen.getByRole('region', { name: 'Report di restituzione' });
    expect(within(section).getByText(/Inviato da Mario Rossi il/)).toBeInTheDocument();
    expect(value('Fine effettiva del tirocinio')).toBe('31/05/2027');
    expect(value('Motivo di chiusura')).toBe('Interruzione anticipata');
    expect(value('Descrizione dell’interruzione')).toBe('L’azienda ha chiuso il reparto');
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

  it('assunto: la formula di assunzione; un report inviato prima, senza la fine effettiva', () => {
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
    expect(screen.queryByText('Fine effettiva del tirocinio')).not.toBeInTheDocument();
    expect(screen.queryByText('Motivo di chiusura')).not.toBeInTheDocument();
  });

  it('con la valutazione dell’azienda: tre faccine con la parola, la media e la nota (US-1104)', async () => {
    renderWithProviders(
      <main>
        <ClosureReportView
          report={{
            ...report,
            companyInteraction: 4,
            companyWelcome: 5,
            companyTraining: 4,
            companyReviewNote: 'Poca formazione',
          }}
        />
      </main>,
    );
    expect(value('Soddisfazione nell’interazione con l’azienda')).toBe('Buona');
    expect(value('Capacità dell’azienda di accogliere il beneficiario')).toBe('Ottima');
    expect(value('Capacità formativa dell’azienda e del tutor aziendale')).toBe('Buona');
    expect(value('Valutazione dell’azienda')).toContain('Buona · 4,3');
    expect(value('Nota sulla valutazione')).toBe('Poca formazione');
    await expectNoAxeViolations();
  });

  it('report inviati prima: nessuna valutazione dell’azienda', () => {
    renderWithProviders(<ClosureReportView report={report} />);
    expect(screen.queryByText('Valutazione dell’azienda')).not.toBeInTheDocument();
  });
});
