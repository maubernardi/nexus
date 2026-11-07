-- Conservazione dei dati dei beneficiari (US-1303, decisioni del PO del 07/11). Il job notturno segna «da
-- anonimizzare» chi non ha attività da 2 anni (anonymization_due_since); il Tutor o l'ADMIN confermano
-- l'anonimizzazione (anonymized_at, già presente) oppure tengono i dati per sempre con un motivo (kept_reason).
ALTER TABLE beneficiary ADD COLUMN anonymization_due_since timestamptz;
ALTER TABLE beneficiary ADD COLUMN kept_reason varchar(500);
ALTER TABLE beneficiary ADD CONSTRAINT ck_beneficiary_kept_reason CHECK (kept_reason IS NULL OR btrim(kept_reason) <> '');
-- un beneficiario anonimizzato o tenuto per sempre non è più da anonimizzare
ALTER TABLE beneficiary ADD CONSTRAINT ck_beneficiary_retention CHECK (
    anonymization_due_since IS NULL OR (anonymized_at IS NULL AND kept_reason IS NULL));
CREATE INDEX ix_beneficiary_anonymization_due ON beneficiary (anonymization_due_since)
    WHERE anonymization_due_since IS NOT NULL;
