-- Tabelle di riferimento: progetti, zone, tipologie di mansione e metadati dei file.
-- Convenzioni: PK TSID (bigint generato dall'applicazione), colonne di audit su ogni tabella.

CREATE TABLE project (
    id          bigint       NOT NULL,
    code        varchar(20)  NOT NULL,
    name        varchar(200) NOT NULL,
    active      boolean      NOT NULL DEFAULT true,
    created_at  timestamptz  NOT NULL,
    created_by  varchar(100) NOT NULL,
    updated_at  timestamptz  NOT NULL,
    updated_by  varchar(100) NOT NULL,
    CONSTRAINT pk_project PRIMARY KEY (id),
    CONSTRAINT uq_project_code UNIQUE (code),
    CONSTRAINT ck_project_code CHECK (code ~ '^[A-Z0-9_]+$')
);

-- Territori della cooperativa: criterio di matching condiviso da candidati, mansioni e bacheca.
CREATE TABLE zone (
    id          bigint       NOT NULL,
    code        varchar(30)  NOT NULL,
    name        varchar(200) NOT NULL,
    active      boolean      NOT NULL DEFAULT true,
    created_at  timestamptz  NOT NULL,
    created_by  varchar(100) NOT NULL,
    updated_at  timestamptz  NOT NULL,
    updated_by  varchar(100) NOT NULL,
    CONSTRAINT pk_zone PRIMARY KEY (id),
    CONSTRAINT uq_zone_code UNIQUE (code),
    CONSTRAINT ck_zone_code CHECK (code ~ '^[A-Z0-9_]+$')
);

-- Catalogo delle tipologie di mansione (es. Magazziniere): scelta del tutor e matching del CC.
CREATE TABLE job_category (
    id          bigint       NOT NULL,
    code        varchar(30)  NOT NULL,
    name        varchar(200) NOT NULL,
    active      boolean      NOT NULL DEFAULT true,
    created_at  timestamptz  NOT NULL,
    created_by  varchar(100) NOT NULL,
    updated_at  timestamptz  NOT NULL,
    updated_by  varchar(100) NOT NULL,
    CONSTRAINT pk_job_category PRIMARY KEY (id),
    CONSTRAINT uq_job_category_code UNIQUE (code),
    CONSTRAINT ck_job_category_code CHECK (code ~ '^[A-Z0-9_]+$')
);

-- Metadati dei file (CV, documenti generati); il contenuto sta nello storage indicato da storage_key.
CREATE TABLE stored_file (
    id               bigint       NOT NULL,
    original_name    varchar(255) NOT NULL,
    content_type     varchar(150) NOT NULL,
    size_bytes       bigint       NOT NULL,
    checksum_sha256  char(64)     NOT NULL,
    storage_key      varchar(500) NOT NULL,
    created_at       timestamptz  NOT NULL,
    created_by       varchar(100) NOT NULL,
    updated_at       timestamptz  NOT NULL,
    updated_by       varchar(100) NOT NULL,
    CONSTRAINT pk_stored_file PRIMARY KEY (id),
    CONSTRAINT uq_stored_file_storage_key UNIQUE (storage_key),
    CONSTRAINT ck_stored_file_size CHECK (size_bytes >= 0),
    CONSTRAINT ck_stored_file_checksum CHECK (checksum_sha256 ~ '^[0-9a-f]{64}$')
);
