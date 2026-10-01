-- Terminologia di dominio: il soggetto della segnalazione è il BENEFICIARIO, non il "candidato".
-- Rinomina tabelle e colonne, poi vincoli e indici il cui nome contiene "candidate" (compresi i NOT NULL con nome
-- generato da PostgreSQL), così i nomi restano coerenti con il modello e con i test che li verificano.

ALTER TABLE candidate RENAME TO beneficiary;
ALTER TABLE candidate_language RENAME TO beneficiary_language;
ALTER TABLE beneficiary_language RENAME COLUMN candidate_id TO beneficiary_id;
ALTER TABLE ticket RENAME COLUMN candidate_id TO beneficiary_id;

DO $$
DECLARE
    r record;
BEGIN
    -- vincoli (i PRIMARY KEY/UNIQUE rinominano anche il proprio indice)
    FOR r IN
        SELECT c.conrelid::regclass AS tbl, c.conname
        FROM pg_constraint c
        WHERE c.connamespace = current_schema()::regnamespace AND c.conname LIKE '%candidate%'
    LOOP
        EXECUTE format('ALTER TABLE %s RENAME CONSTRAINT %I TO %I', r.tbl, r.conname,
                       replace(r.conname, 'candidate', 'beneficiary'));
    END LOOP;
    -- indici rimasti (quelli non legati a un vincolo)
    FOR r IN
        SELECT i.relname
        FROM pg_class i
        WHERE i.relnamespace = current_schema()::regnamespace AND i.relkind = 'i' AND i.relname LIKE '%candidate%'
    LOOP
        EXECUTE format('ALTER INDEX %I RENAME TO %I', r.relname, replace(r.relname, 'candidate', 'beneficiary'));
    END LOOP;
END
$$;
