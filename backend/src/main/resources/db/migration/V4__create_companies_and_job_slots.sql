-- Aziende e mansioni. La FK job_slot.blocked_by_ticket_id verso ticket è aggiunta in V6.

CREATE TABLE company (
    id              bigint       NOT NULL,
    name            varchar(200) NOT NULL,
    vat_code        char(11)     NOT NULL,
    legal_address   varchar(300),
    contact_person  varchar(200),
    phone           varchar(30),
    email           varchar(254),
    active          boolean      NOT NULL DEFAULT true,
    created_at      timestamptz  NOT NULL,
    created_by      varchar(100) NOT NULL,
    updated_at      timestamptz  NOT NULL,
    updated_by      varchar(100) NOT NULL,
    CONSTRAINT pk_company PRIMARY KEY (id),
    CONSTRAINT uq_company_vat_code UNIQUE (vat_code),
    CONSTRAINT ck_company_vat_code CHECK (vat_code ~ '^[0-9]{11}$')
);

-- Una mansione è BLOCCATA se e solo se indica il ticket che la blocca; un ticket blocca al massimo una mansione.
CREATE TABLE job_slot (
    id                    bigint       NOT NULL,
    company_id            bigint       NOT NULL,
    job_category_id       bigint       NOT NULL,
    zone_id               bigint       NOT NULL,
    title                 varchar(200) NOT NULL,
    description           text,
    status                varchar(20)  NOT NULL DEFAULT 'LIBERA',
    blocked_by_ticket_id  bigint,
    version               bigint       NOT NULL DEFAULT 0,
    created_at            timestamptz  NOT NULL,
    created_by            varchar(100) NOT NULL,
    updated_at            timestamptz  NOT NULL,
    updated_by            varchar(100) NOT NULL,
    CONSTRAINT pk_job_slot PRIMARY KEY (id),
    CONSTRAINT fk_job_slot_company FOREIGN KEY (company_id) REFERENCES company (id),
    CONSTRAINT fk_job_slot_job_category FOREIGN KEY (job_category_id) REFERENCES job_category (id),
    CONSTRAINT fk_job_slot_zone FOREIGN KEY (zone_id) REFERENCES zone (id),
    CONSTRAINT uq_job_slot_blocked_by_ticket UNIQUE (blocked_by_ticket_id),
    CONSTRAINT ck_job_slot_status CHECK (status IN ('LIBERA', 'BLOCCATA')),
    CONSTRAINT ck_job_slot_blocked CHECK ((status = 'BLOCCATA') = (blocked_by_ticket_id IS NOT NULL))
);

CREATE INDEX ix_job_slot_company ON job_slot (company_id);
CREATE INDEX ix_job_slot_job_category ON job_slot (job_category_id);
CREATE INDEX ix_job_slot_zone ON job_slot (zone_id);
CREATE INDEX ix_job_slot_status ON job_slot (status);
