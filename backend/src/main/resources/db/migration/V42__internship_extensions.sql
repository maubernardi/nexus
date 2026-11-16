-- US-1012: proroghe del tirocinio. Una proroga è una convenzione in tutto e per tutto (numero, data, data di stipula,
-- date proprie); la convenzione iniziale resta sulla scheda (US-1008). Solo additiva (A16): la release precedente
-- ignora la tabella e la colonna nuove.
CREATE TABLE internship_extension (
    id                     bigint        NOT NULL,
    internship_id          bigint        NOT NULL,
    agreement_number       varchar(50)   NOT NULL,
    agreement_date         date          NOT NULL,
    agreement_signed_date  date          NOT NULL,
    date_from              date          NOT NULL,
    date_to                date          NOT NULL,
    created_at             timestamptz   NOT NULL,
    created_by             varchar(100)  NOT NULL,
    CONSTRAINT pk_internship_extension PRIMARY KEY (id),
    -- per la chiave esterna composta dei progetti: un progetto e la sua proroga sono dello stesso tirocinio
    CONSTRAINT uq_internship_extension_internship UNIQUE (id, internship_id),
    CONSTRAINT fk_internship_extension_internship FOREIGN KEY (internship_id) REFERENCES internship (id),
    CONSTRAINT ck_internship_extension_dates CHECK (date_to >= date_from),
    CONSTRAINT ck_internship_extension_agreement_number CHECK (btrim(agreement_number) <> ''),
    CONSTRAINT ex_internship_extension_overlap EXCLUDE USING gist (
        int8range(internship_id, internship_id, '[]') WITH =,
        daterange(date_from, date_to, '[]') WITH &&)
);

CREATE INDEX ix_internship_extension_internship ON internship_extension (internship_id);

-- Progetti formativi della proroga: vuoto = convenzione iniziale. Numerati dentro la loro convenzione, nell'ordine delle
-- date (controllo nel servizio); l'esclusione delle sovrapposizioni resta sul tirocinio intero.
ALTER TABLE internship_training_project ADD COLUMN extension_id bigint;
ALTER TABLE internship_training_project
    ADD CONSTRAINT fk_internship_training_project_extension FOREIGN KEY (extension_id, internship_id)
        REFERENCES internship_extension (id, internship_id);
CREATE INDEX ix_internship_training_project_extension ON internship_training_project (extension_id);
