-- Avvisi di fine tirocinio (US-1203, PO 22/11): al Tutor 7 e 3 giorni prima della fine prevista e 3 e 7 giorni dopo.
-- Sulla scheda resta a quale fine si riferiscono gli avvisi già partiti e fino a quale sono arrivati: una proroga cambia
-- la fine, e gli avvisi ripartono dalla nuova data. Solo additiva (A16): la release precedente ignora le colonne.
--   end_reminder_stage: 0 nessuno, 1 sette giorni prima, 2 tre giorni prima, 3 tre giorni dopo, 4 sette giorni dopo

ALTER TABLE internship
    ADD COLUMN end_reminder_for   date,
    ADD COLUMN end_reminder_stage integer NOT NULL DEFAULT 0,
    ADD CONSTRAINT ck_internship_end_reminder_stage CHECK (end_reminder_stage BETWEEN 0 AND 4),
    ADD CONSTRAINT ck_internship_end_reminder_for CHECK (end_reminder_stage = 0 OR end_reminder_for IS NOT NULL);
