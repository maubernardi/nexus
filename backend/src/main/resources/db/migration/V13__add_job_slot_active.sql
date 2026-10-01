-- US-502: una mansione che l'azienda non offre più si ritira (non si cancella: può avere uno storico).
-- Una mansione bloccata da un ticket non può essere ritirata.
ALTER TABLE job_slot ADD COLUMN active boolean NOT NULL DEFAULT true;
ALTER TABLE job_slot ADD CONSTRAINT ck_job_slot_active_free CHECK (active OR status = 'LIBERA');
CREATE INDEX ix_job_slot_matching ON job_slot (status, active, job_category_id, zone_id);
