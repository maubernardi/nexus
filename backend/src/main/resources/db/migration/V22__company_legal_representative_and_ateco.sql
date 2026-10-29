-- Dati dell'azienda per i documenti dei tirocini (US-506, richiesta del PO del 29/10).

-- Legale rappresentante: uno per azienda, può non coincidere con il referente. Dati personali: mai nei log, nell'audit
-- solo i campi cambiati, senza i valori. Codice fiscale in formato nel database, carattere di controllo
-- nell'applicazione.
CREATE TABLE company_legal_representative (
    id           bigint       NOT NULL,
    company_id   bigint       NOT NULL,
    first_name   varchar(100) NOT NULL,
    last_name    varchar(100) NOT NULL,
    fiscal_code  varchar(16),
    birth_date   date,
    birth_place  varchar(200),
    residence    varchar(300),
    version      bigint       NOT NULL DEFAULT 0,
    created_at   timestamptz  NOT NULL,
    created_by   varchar(100) NOT NULL,
    updated_at   timestamptz  NOT NULL,
    updated_by   varchar(100) NOT NULL,
    CONSTRAINT pk_company_legal_representative PRIMARY KEY (id),
    CONSTRAINT uq_company_legal_representative_company UNIQUE (company_id),
    CONSTRAINT fk_company_legal_representative_company FOREIGN KEY (company_id) REFERENCES company (id),
    CONSTRAINT ck_company_legal_representative_names CHECK (btrim(first_name) <> '' AND btrim(last_name) <> ''),
    CONSTRAINT ck_company_legal_representative_fiscal_code CHECK (fiscal_code IS NULL OR fiscal_code ~ '^[A-Z0-9]{16}$')
);

-- Codici ATECO: uno o più per azienda; nel tirocinio si sceglie quello giusto (US-1001). Formato: 2, 4 o 6 cifre con i
-- punti (es. 56, 56.10, 56.10.11).
CREATE TABLE company_ateco_code (
    id           bigint       NOT NULL,
    company_id   bigint       NOT NULL,
    code         varchar(8)   NOT NULL,
    description  varchar(200),
    created_at   timestamptz  NOT NULL,
    created_by   varchar(100) NOT NULL,
    CONSTRAINT pk_company_ateco_code PRIMARY KEY (id),
    CONSTRAINT fk_company_ateco_code_company FOREIGN KEY (company_id) REFERENCES company (id),
    CONSTRAINT uq_company_ateco_code UNIQUE (company_id, code),
    CONSTRAINT ck_company_ateco_code_format CHECK (code ~ '^[0-9]{2}(\.[0-9]{1,2}){0,2}$')
);

CREATE INDEX ix_company_ateco_code_company ON company_ateco_code (company_id);
