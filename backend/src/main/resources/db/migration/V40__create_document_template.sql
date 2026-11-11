-- US-1010: modelli Word dei documenti caricati dall'ADMIN, con le condizioni in cui valgono (documento, progetto, tipo
-- di tirocinio e, facoltativo, ente di presa in carico). Il file è in stored_file / stored_file_content come il CV
-- (US-205): nel backup del database e fuori dal repository. Solo additiva (A16): la release precedente usa i modelli
-- nel codice.
CREATE TABLE document_template (
    id                  bigint        NOT NULL,
    document_kind       varchar(30)   NOT NULL,
    project_id          bigint        NOT NULL,
    internship_type_id  bigint        NOT NULL,
    care_agency_id      bigint,
    name                varchar(200)  NOT NULL,
    file_id             bigint        NOT NULL,
    active              boolean       NOT NULL DEFAULT true,
    version             bigint        NOT NULL DEFAULT 0,
    created_at          timestamptz   NOT NULL,
    created_by          varchar(100)  NOT NULL,
    updated_at          timestamptz   NOT NULL,
    updated_by          varchar(100)  NOT NULL,
    CONSTRAINT pk_document_template PRIMARY KEY (id),
    CONSTRAINT fk_document_template_project FOREIGN KEY (project_id) REFERENCES project (id),
    CONSTRAINT fk_document_template_internship_type FOREIGN KEY (internship_type_id) REFERENCES internship_type (id),
    CONSTRAINT fk_document_template_care_agency FOREIGN KEY (care_agency_id) REFERENCES care_agency (id),
    CONSTRAINT fk_document_template_file FOREIGN KEY (file_id) REFERENCES stored_file (id),
    CONSTRAINT uq_document_template_file UNIQUE (file_id),
    CONSTRAINT ck_document_template_kind CHECK (document_kind IN ('CONVENZIONE', 'PROGETTO_FORMATIVO', 'RELAZIONE_FINALE')),
    CONSTRAINT ck_document_template_name CHECK (btrim(name) <> '')
);

-- scelta del modello al download: documento, progetto e tipo tra i modelli attivi
CREATE INDEX ix_document_template_choice ON document_template (document_kind, project_id, internship_type_id)
    WHERE active;
CREATE INDEX ix_document_template_internship_type ON document_template (internship_type_id);
CREATE INDEX ix_document_template_care_agency ON document_template (care_agency_id);
