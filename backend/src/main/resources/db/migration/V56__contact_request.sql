-- Richieste di contatto di un'azienda dal Tutor al Call Center (US-507, analisi con il PO del 23/11). La richiesta è
-- generale: niente beneficiario né segnalazione. Riguarda un'azienda già presente (company_id) oppure un'azienda nuova
-- (nome e zona obbligatori, categoria facoltativa). Stati: aperta, in carico (a un operatore qualsiasi), chiusa (con
-- US-512). Invio e presa in carico vanno nell'audit (EN-2).

-- nomi simili prima dell'invio (pg_trgm): estensione «trusted», la crea il proprietario del database
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE SEQUENCE contact_request_number_seq START WITH 1;

CREATE TABLE contact_request (
    id                    bigint       NOT NULL,
    number                bigint       NOT NULL DEFAULT nextval('contact_request_number_seq'),
    requested_by_id       bigint       NOT NULL,
    company_id            bigint,
    company_name          varchar(200),
    zone_id               bigint,
    business_category_id  bigint,
    status                varchar(20)  NOT NULL,
    assigned_operator_id  bigint,
    taken_at              timestamptz,
    version               bigint       NOT NULL DEFAULT 0,
    created_at            timestamptz  NOT NULL,
    created_by            varchar(100) NOT NULL,
    updated_at            timestamptz  NOT NULL,
    updated_by            varchar(100) NOT NULL,
    CONSTRAINT pk_contact_request PRIMARY KEY (id),
    CONSTRAINT uq_contact_request_number UNIQUE (number),
    CONSTRAINT fk_contact_request_requested_by FOREIGN KEY (requested_by_id) REFERENCES app_user (id),
    CONSTRAINT fk_contact_request_company FOREIGN KEY (company_id) REFERENCES company (id),
    CONSTRAINT fk_contact_request_zone FOREIGN KEY (zone_id) REFERENCES zone (id),
    CONSTRAINT fk_contact_request_business_category FOREIGN KEY (business_category_id)
        REFERENCES business_category (id),
    CONSTRAINT fk_contact_request_assigned_operator FOREIGN KEY (assigned_operator_id) REFERENCES app_user (id),
    CONSTRAINT ck_contact_request_status CHECK (status IN ('APERTA', 'IN_CARICO', 'CHIUSA')),
    -- un'azienda già presente, oppure una nuova con nome e zona (la categoria solo per quella nuova)
    CONSTRAINT ck_contact_request_company CHECK (
        (company_id IS NOT NULL AND company_name IS NULL AND zone_id IS NULL AND business_category_id IS NULL)
        OR (company_id IS NULL AND company_name IS NOT NULL AND btrim(company_name) <> '' AND zone_id IS NOT NULL)),
    -- aperta: nessun operatore; in carico o chiusa: l'operatore e l'istante della presa in carico
    CONSTRAINT ck_contact_request_assigned CHECK (
        (status = 'APERTA' AND assigned_operator_id IS NULL AND taken_at IS NULL)
        OR (status <> 'APERTA' AND assigned_operator_id IS NOT NULL AND taken_at IS NOT NULL))
);

CREATE INDEX ix_contact_request_requested_by ON contact_request (requested_by_id);
CREATE INDEX ix_contact_request_status ON contact_request (status);
