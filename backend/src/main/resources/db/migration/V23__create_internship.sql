-- Scheda amministrativa del tirocinio (US-1001): una per segnalazione, la compila il Tutor dopo l'appuntamento fissato.
-- Campi segnaposto fino ai moduli ufficiali (Q5), tutti facoltativi: la completezza la verifica l'avvio (US-1003).
-- I dati del beneficiario stanno solo qui, non nell'anagrafica (minimizzazione), e rientrano nell'anonimizzazione
-- (US-1303). L'azienda è quella della mansione abbinata alla segnalazione.
CREATE TABLE internship (
    id                  bigint        NOT NULL,
    ticket_id           bigint        NOT NULL,
    internship_type_id  bigint,
    start_date          date,
    expected_end_date   date,
    weekly_hours        smallint,
    site                varchar(300),
    ateco_code_id       bigint,
    fiscal_code         varchar(16),
    birth_date          date,
    birth_place         varchar(200),
    residence           varchar(300),
    notes               varchar(2000),
    version             bigint        NOT NULL DEFAULT 0,
    created_at          timestamptz   NOT NULL,
    created_by          varchar(100)  NOT NULL,
    updated_at          timestamptz   NOT NULL,
    updated_by          varchar(100)  NOT NULL,
    CONSTRAINT pk_internship PRIMARY KEY (id),
    CONSTRAINT uq_internship_ticket UNIQUE (ticket_id),
    CONSTRAINT fk_internship_ticket FOREIGN KEY (ticket_id) REFERENCES ticket (id),
    CONSTRAINT fk_internship_type FOREIGN KEY (internship_type_id) REFERENCES internship_type (id),
    -- un codice scelto in una scheda non si toglie dall'azienda
    CONSTRAINT fk_internship_ateco_code FOREIGN KEY (ateco_code_id) REFERENCES company_ateco_code (id),
    CONSTRAINT ck_internship_dates CHECK (start_date IS NULL OR expected_end_date IS NULL
        OR expected_end_date >= start_date),
    CONSTRAINT ck_internship_weekly_hours CHECK (weekly_hours IS NULL OR weekly_hours BETWEEN 1 AND 40),
    CONSTRAINT ck_internship_fiscal_code CHECK (fiscal_code IS NULL OR fiscal_code ~ '^[A-Z0-9]{16}$')
);

CREATE INDEX ix_internship_ateco_code ON internship (ateco_code_id);

-- Tutor aziendali: uno o più per tirocinio. Righe sostituite in blocco a ogni salvataggio della scheda.
CREATE TABLE internship_company_tutor (
    id             bigint        NOT NULL,
    internship_id  bigint        NOT NULL,
    full_name      varchar(200)  NOT NULL,
    contact        varchar(200),
    created_at     timestamptz   NOT NULL,
    created_by     varchar(100)  NOT NULL,
    CONSTRAINT pk_internship_company_tutor PRIMARY KEY (id),
    CONSTRAINT fk_internship_company_tutor_internship FOREIGN KEY (internship_id) REFERENCES internship (id),
    CONSTRAINT ck_internship_company_tutor_name CHECK (btrim(full_name) <> '')
);

CREATE INDEX ix_internship_company_tutor_internship ON internship_company_tutor (internship_id);

-- Periodi di sospensione previsti (dal–al, estremi compresi), senza sovrapposizioni nello stesso tirocinio. Il vincolo
-- usa solo operatori nativi di GiST sui range (l'id come intervallo di un solo valore), senza l'estensione btree_gist.
CREATE TABLE internship_suspension (
    id             bigint       NOT NULL,
    internship_id  bigint       NOT NULL,
    date_from      date         NOT NULL,
    date_to        date         NOT NULL,
    created_at     timestamptz  NOT NULL,
    created_by     varchar(100) NOT NULL,
    CONSTRAINT pk_internship_suspension PRIMARY KEY (id),
    CONSTRAINT fk_internship_suspension_internship FOREIGN KEY (internship_id) REFERENCES internship (id),
    CONSTRAINT ck_internship_suspension_dates CHECK (date_to >= date_from),
    CONSTRAINT ex_internship_suspension_overlap EXCLUDE USING gist (
        int8range(internship_id, internship_id, '[]') WITH =,
        daterange(date_from, date_to, '[]') WITH &&)
);

CREATE INDEX ix_internship_suspension_internship ON internship_suspension (internship_id);
