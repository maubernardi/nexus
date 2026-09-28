-- Candidati (tirocinanti) e lingue conosciute.
-- Dati personali e di categoria particolare (has_law68, constraints) protetti dal controllo degli accessi.
-- anonymized_at: dopo l'anonimizzazione nome e cognome possono essere svuotati; restano i dati statistici.

CREATE TABLE candidate (
    id                   bigint        NOT NULL,
    owner_tutor_id       bigint        NOT NULL,
    first_name           varchar(100),
    last_name            varchar(100),
    birth_year           smallint      NOT NULL,
    gender               varchar(20)   NOT NULL,
    nationality          char(2),
    citizenship          char(2),
    residence_zone_id    bigint        NOT NULL,
    has_driving_license  boolean       NOT NULL DEFAULT false,
    license_types        varchar(5)[]  NOT NULL DEFAULT '{}',
    has_vehicle          boolean       NOT NULL DEFAULT false,
    transport_mode       varchar(20),
    has_law68            boolean       NOT NULL DEFAULT false,
    education_level      varchar(30),
    constraints          text,
    cv_file_id           bigint,
    anonymized_at        timestamptz,
    created_at           timestamptz   NOT NULL,
    created_by           varchar(100)  NOT NULL,
    updated_at           timestamptz   NOT NULL,
    updated_by           varchar(100)  NOT NULL,
    CONSTRAINT pk_candidate PRIMARY KEY (id),
    CONSTRAINT fk_candidate_owner_tutor FOREIGN KEY (owner_tutor_id) REFERENCES app_user (id),
    CONSTRAINT fk_candidate_residence_zone FOREIGN KEY (residence_zone_id) REFERENCES zone (id),
    CONSTRAINT fk_candidate_cv_file FOREIGN KEY (cv_file_id) REFERENCES stored_file (id),
    CONSTRAINT ck_candidate_name CHECK (anonymized_at IS NOT NULL OR (first_name IS NOT NULL AND last_name IS NOT NULL)),
    -- limite superiore fisso: "non nel futuro" è verificato dall'applicazione (un CHECK non può usare current_date in modo sicuro)
    CONSTRAINT ck_candidate_birth_year CHECK (birth_year BETWEEN 1900 AND 2100),
    CONSTRAINT ck_candidate_gender CHECK (gender IN ('M', 'F', 'ALTRO', 'NON_DICHIARATO')),
    CONSTRAINT ck_candidate_nationality CHECK (nationality ~ '^[A-Z]{2}$'),
    CONSTRAINT ck_candidate_citizenship CHECK (citizenship ~ '^[A-Z]{2}$'),
    CONSTRAINT ck_candidate_license_types CHECK (license_types <@ ARRAY[
        'AM', 'A1', 'A2', 'A', 'B', 'BE', 'C1', 'C1E', 'C', 'CE', 'D1', 'D1E', 'D', 'DE', 'CQC', 'KB']::varchar(5)[]),
    CONSTRAINT ck_candidate_driving_license CHECK (has_driving_license = (cardinality(license_types) > 0)),
    CONSTRAINT ck_candidate_transport_mode CHECK (transport_mode IN (
        'AUTO_PROPRIA', 'MEZZI_PUBBLICI', 'BICICLETTA', 'A_PIEDI', 'ALTRO')),
    CONSTRAINT ck_candidate_education_level CHECK (education_level IN (
        'NESSUN_TITOLO', 'LICENZA_ELEMENTARE', 'LICENZA_MEDIA', 'QUALIFICA_PROFESSIONALE', 'DIPLOMA', 'ITS',
        'LAUREA_TRIENNALE', 'LAUREA_MAGISTRALE', 'DOTTORATO'))
);

CREATE INDEX ix_candidate_owner_tutor ON candidate (owner_tutor_id);
CREATE INDEX ix_candidate_residence_zone ON candidate (residence_zone_id);

CREATE TABLE candidate_language (
    id            bigint       NOT NULL,
    candidate_id  bigint       NOT NULL,
    language      char(2)      NOT NULL,
    level         varchar(12)  NOT NULL,
    created_at    timestamptz  NOT NULL,
    created_by    varchar(100) NOT NULL,
    updated_at    timestamptz  NOT NULL,
    updated_by    varchar(100) NOT NULL,
    CONSTRAINT pk_candidate_language PRIMARY KEY (id),
    CONSTRAINT fk_candidate_language_candidate FOREIGN KEY (candidate_id) REFERENCES candidate (id) ON DELETE CASCADE,
    CONSTRAINT uq_candidate_language UNIQUE (candidate_id, language),
    CONSTRAINT ck_candidate_language_code CHECK (language ~ '^[a-z]{2}$'),
    CONSTRAINT ck_candidate_language_level CHECK (level IN ('A1', 'A2', 'B1', 'B2', 'C1', 'C2', 'MADRELINGUA'))
);
