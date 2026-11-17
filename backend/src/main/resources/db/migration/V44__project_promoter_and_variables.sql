-- US-1013: dati del soggetto promotore e variabili libere per progetto, al posto delle variabili NEXUS_PROMOTER_* del
-- .env (PO, 16/11). Tutto facoltativo: un dato che manca resta, nei documenti, una riga da completare. Solo additiva
-- (A16): la release precedente ignora colonne e tabella.
ALTER TABLE project ADD COLUMN promoter_name                       varchar(300);
ALTER TABLE project ADD COLUMN promoter_address                    varchar(300);
ALTER TABLE project ADD COLUMN promoter_fiscal_code                varchar(16);
ALTER TABLE project ADD COLUMN promoter_representative             varchar(200);
ALTER TABLE project ADD COLUMN promoter_representative_role        varchar(200);
ALTER TABLE project ADD COLUMN promoter_representative_birth_place varchar(200);
ALTER TABLE project ADD COLUMN promoter_representative_birth_date  date;
ALTER TABLE project ADD COLUMN promoter_phone                      varchar(50);
ALTER TABLE project ADD COLUMN promoter_email                      varchar(254);
ALTER TABLE project ADD COLUMN promoter_pec                        varchar(254);
ALTER TABLE project ADD COLUMN promoter_inail_position             varchar(100);
ALTER TABLE project ADD COLUMN promoter_rc_insurer                 varchar(200);
ALTER TABLE project ADD COLUMN promoter_rc_policy                  varchar(100);
ALTER TABLE project ADD COLUMN promoter_rc_expiry                  date;
ALTER TABLE project ADD CONSTRAINT ck_project_promoter_not_blank CHECK (
    (promoter_name IS NULL OR btrim(promoter_name) <> '')
    AND (promoter_address IS NULL OR btrim(promoter_address) <> '')
    AND (promoter_fiscal_code IS NULL OR btrim(promoter_fiscal_code) <> '')
    AND (promoter_representative IS NULL OR btrim(promoter_representative) <> '')
    AND (promoter_representative_role IS NULL OR btrim(promoter_representative_role) <> '')
    AND (promoter_representative_birth_place IS NULL OR btrim(promoter_representative_birth_place) <> '')
    AND (promoter_phone IS NULL OR btrim(promoter_phone) <> '')
    AND (promoter_email IS NULL OR btrim(promoter_email) <> '')
    AND (promoter_pec IS NULL OR btrim(promoter_pec) <> '')
    AND (promoter_inail_position IS NULL OR btrim(promoter_inail_position) <> '')
    AND (promoter_rc_insurer IS NULL OR btrim(promoter_rc_insurer) <> '')
    AND (promoter_rc_policy IS NULL OR btrim(promoter_rc_policy) <> ''));

-- Variabili libere: il nome è il segnaposto {NOME} dei modelli del progetto. Che non sia un segnaposto di NEXUS lo
-- controlla il servizio (l'elenco è nel codice).
CREATE TABLE project_variable (
    id           bigint        NOT NULL,
    project_id   bigint        NOT NULL,
    name         varchar(50)   NOT NULL,
    value        varchar(500)  NOT NULL,
    description  varchar(300),
    created_at   timestamptz   NOT NULL,
    created_by   varchar(100)  NOT NULL,
    updated_at   timestamptz   NOT NULL,
    updated_by   varchar(100)  NOT NULL,
    CONSTRAINT pk_project_variable PRIMARY KEY (id),
    CONSTRAINT fk_project_variable_project FOREIGN KEY (project_id) REFERENCES project (id),
    CONSTRAINT uq_project_variable_name UNIQUE (project_id, name),
    CONSTRAINT ck_project_variable_name CHECK (name ~ '^[A-Z][A-Z0-9_]{0,49}$'),
    CONSTRAINT ck_project_variable_value CHECK (btrim(value) <> ''),
    CONSTRAINT ck_project_variable_description CHECK (description IS NULL OR btrim(description) <> '')
);
