-- US-1008: convenzione del tirocinio e progetti formativi. Solo additiva (A16): la release precedente ignora colonne e
-- tabella nuove.

-- Convenzione, una per tirocinio: numero, data e data di stipula, inseriti dal Tutor; la durata è quella del tirocinio.
ALTER TABLE internship ADD COLUMN agreement_number varchar(50);
ALTER TABLE internship ADD COLUMN agreement_date date;
ALTER TABLE internship ADD COLUMN agreement_signed_date date;
ALTER TABLE internship
    ADD CONSTRAINT ck_internship_agreement_number CHECK (agreement_number IS NULL OR btrim(agreement_number) <> '');

-- Progetti formativi della convenzione: righe sostituite in blocco a ogni salvataggio della scheda, numerate
-- nell'ordine delle date. Dentro le date del tirocinio (controllo nel servizio, perché confronta la scheda) e senza
-- sovrapposizioni, neanche di un giorno: lo stesso vincolo di esclusione delle sospensioni, senza btree_gist.
CREATE TABLE internship_training_project (
    id             bigint         NOT NULL,
    internship_id  bigint         NOT NULL,
    date_from      date           NOT NULL,
    date_to        date           NOT NULL,
    job_title      varchar(200)   NOT NULL,
    job_details    varchar(2000),
    created_at     timestamptz    NOT NULL,
    created_by     varchar(100)   NOT NULL,
    CONSTRAINT pk_internship_training_project PRIMARY KEY (id),
    CONSTRAINT fk_internship_training_project_internship FOREIGN KEY (internship_id) REFERENCES internship (id),
    CONSTRAINT ck_internship_training_project_dates CHECK (date_to >= date_from),
    CONSTRAINT ck_internship_training_project_job_title CHECK (btrim(job_title) <> ''),
    CONSTRAINT ck_internship_training_project_job_details CHECK (job_details IS NULL OR btrim(job_details) <> ''),
    CONSTRAINT ex_internship_training_project_overlap EXCLUDE USING gist (
        int8range(internship_id, internship_id, '[]') WITH =,
        daterange(date_from, date_to, '[]') WITH &&)
);

CREATE INDEX ix_internship_training_project_internship ON internship_training_project (internship_id);
