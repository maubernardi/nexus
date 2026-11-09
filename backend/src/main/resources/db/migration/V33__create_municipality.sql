-- Comuni e province italiani (US-114), dal file ISTAT «Elenco dei comuni italiani» caricato dall'ADMIN.
-- Temporizzati: una riga per versione, valida da valid_from (compreso) a valid_to (escluso); la versione attuale ha
-- valid_to vuoto. Un cambio o una soppressione chiude la versione e ne apre un'altra: nulla si cancella, così i dati
-- e i documenti che puntano a una versione restano com'erano.

-- Ogni caricamento confermato: data da cui vale e conteggi (audit MUNICIPALITY_IMPORT).
CREATE TABLE municipality_import (
    id                      bigint       NOT NULL,
    valid_from              date         NOT NULL,
    municipalities_added    integer      NOT NULL,
    municipalities_removed  integer      NOT NULL,
    municipalities_changed  integer      NOT NULL,
    provinces_added         integer      NOT NULL,
    provinces_removed       integer      NOT NULL,
    provinces_changed       integer      NOT NULL,
    created_at              timestamptz  NOT NULL,
    created_by              varchar(100) NOT NULL,
    CONSTRAINT pk_municipality_import PRIMARY KEY (id),
    CONSTRAINT uq_municipality_import_valid_from UNIQUE (valid_from),
    CONSTRAINT ck_municipality_import_counts CHECK (municipalities_added >= 0 AND municipalities_removed >= 0
        AND municipalities_changed >= 0 AND provinces_added >= 0 AND provinces_removed >= 0 AND provinces_changed >= 0)
);

-- Provincia (o città metropolitana, libero consorzio, unità non amministrativa): chiave il codice dell'unità
-- territoriale sovracomunale dell'ISTAT.
CREATE TABLE province (
    id            bigint       NOT NULL,
    code          varchar(3)   NOT NULL,
    name          varchar(100) NOT NULL,
    abbreviation  varchar(2)   NOT NULL,
    valid_from    date         NOT NULL,
    valid_to      date,
    created_at    timestamptz  NOT NULL,
    created_by    varchar(100) NOT NULL,
    updated_at    timestamptz  NOT NULL,
    updated_by    varchar(100) NOT NULL,
    CONSTRAINT pk_province PRIMARY KEY (id),
    CONSTRAINT ck_province_code CHECK (code ~ '^[0-9]{3}$'),
    CONSTRAINT ck_province_abbreviation CHECK (abbreviation ~ '^[A-Z]{2}$'),
    CONSTRAINT ck_province_name CHECK (btrim(name) <> ''),
    CONSTRAINT ck_province_validity CHECK (valid_to IS NULL OR valid_to > valid_from)
);

CREATE UNIQUE INDEX uq_province_current_code ON province (code) WHERE valid_to IS NULL;

-- Comune: chiave il codice catastale, che non cambia quando il comune passa a un'altra provincia (il codice ISTAT sì).
CREATE TABLE municipality (
    id              bigint       NOT NULL,
    cadastral_code  varchar(4)   NOT NULL,
    istat_code      varchar(6)   NOT NULL,
    name            varchar(150) NOT NULL,
    province_id     bigint       NOT NULL,
    valid_from      date         NOT NULL,
    valid_to        date,
    created_at      timestamptz  NOT NULL,
    created_by      varchar(100) NOT NULL,
    updated_at      timestamptz  NOT NULL,
    updated_by      varchar(100) NOT NULL,
    CONSTRAINT pk_municipality PRIMARY KEY (id),
    CONSTRAINT fk_municipality_province FOREIGN KEY (province_id) REFERENCES province (id),
    CONSTRAINT ck_municipality_cadastral_code CHECK (cadastral_code ~ '^[A-Z][0-9]{3}$'),
    CONSTRAINT ck_municipality_istat_code CHECK (istat_code ~ '^[0-9]{6}$'),
    CONSTRAINT ck_municipality_name CHECK (btrim(name) <> ''),
    CONSTRAINT ck_municipality_validity CHECK (valid_to IS NULL OR valid_to > valid_from)
);

CREATE UNIQUE INDEX uq_municipality_current_cadastral_code ON municipality (cadastral_code) WHERE valid_to IS NULL;
CREATE INDEX ix_municipality_province ON municipality (province_id);
CREATE INDEX ix_municipality_name ON municipality (lower(name));
