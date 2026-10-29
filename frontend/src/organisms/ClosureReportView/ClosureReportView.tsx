import { useTranslation } from 'react-i18next';

import type { ClosureReport } from '@/config/api/ticketApi';

const dateTime = new Intl.DateTimeFormat('it-IT', { dateStyle: 'short', timeStyle: 'short' });

type ClosureReportViewProps = {
  report: ClosureReport;
};

/** Report di restituzione inviato (US-1101), in sola lettura per chi vede la segnalazione: non si modifica più. */
export const ClosureReportView = ({ report }: ClosureReportViewProps) => {
  const { t } = useTranslation();
  const rows: { label: string; value: string }[] = [
    {
      label: t('closureReport.fields.companySatisfaction'),
      value: t('closureReport.view.score', { value: report.companySatisfaction }),
    },
    {
      label: t('closureReport.fields.beneficiarySatisfaction'),
      value: t('closureReport.view.score', { value: report.beneficiarySatisfaction }),
    },
    {
      label: t('closureReport.fields.hired'),
      value: report.hired
        ? t('closureReport.view.hiredYes', { formula: report.hiringFormula?.name ?? '' })
        : t('closureReport.view.hiredNo', { reason: report.noHireReason?.name ?? '' }),
    },
    {
      label: t('closureReport.fields.jobSlotStillAvailable'),
      value: report.jobSlotStillAvailable ? t('beneficiary.yes') : t('beneficiary.no'),
    },
    {
      label: t('closureReport.fields.objectives'),
      value: report.objectives ?? t('closureReport.view.none'),
    },
    {
      label: t('closureReport.fields.difficulties'),
      value: report.difficulties ?? t('closureReport.view.none'),
    },
  ];

  return (
    <section
      aria-labelledby="closure-report-view"
      className="space-y-3 rounded-xl border border-border bg-card p-4"
    >
      <h2 id="closure-report-view" className="text-lg font-semibold text-primary">
        {t('closureReport.view.heading')}
      </h2>
      <p className="text-sm text-muted-foreground">
        {t('closureReport.view.submitted', {
          name: report.submittedBy,
          date: dateTime.format(new Date(report.submittedAt)),
        })}
      </p>
      <dl className="grid gap-x-6 gap-y-3 sm:grid-cols-[max-content_1fr]">
        {rows.map((row) => (
          <div key={row.label} className="contents">
            <dt className="font-medium">{row.label}</dt>
            {/* i testi liberi mantengono gli a capo del Tutor */}
            <dd className="whitespace-pre-line">{row.value}</dd>
          </div>
        ))}
      </dl>
    </section>
  );
};
