-- Tipi di tirocinio (US-108, richiesta del PO del 29/10): catalogo gestito dall'ADMIN come zone e tipologie (US-106).
-- Il tipo, scelto nella scheda del tirocinio (US-1001), determina il tipo di documento (US-1004, US-1005).
CREATE TABLE internship_type (
    id          bigint       NOT NULL,
    code        varchar(30)  NOT NULL,
    name        varchar(200) NOT NULL,
    active      boolean      NOT NULL DEFAULT true,
    created_at  timestamptz  NOT NULL,
    created_by  varchar(100) NOT NULL,
    updated_at  timestamptz  NOT NULL,
    updated_by  varchar(100) NOT NULL,
    CONSTRAINT pk_internship_type PRIMARY KEY (id),
    CONSTRAINT uq_internship_type_code UNIQUE (code),
    CONSTRAINT ck_internship_type_code CHECK (code ~ '^[A-Z0-9_]+$'),
    CONSTRAINT ck_internship_type_name CHECK (btrim(name) <> '')
);

CREATE UNIQUE INDEX uq_internship_type_active_name ON internship_type (lower(name)) WHERE active;

-- Voci iniziali indicate dal PO, presenti in ogni ambiente; l'ADMIN le modifica dalla pagina "Cataloghi".
INSERT INTO internship_type (id, code, name, active, created_at, created_by, updated_at, updated_by) VALUES
    (893700000000000101, 'CURRICULARE', 'Curriculare', true, now(), 'system', now(), 'system'),
    (893700000000000102, 'EXTRACURRICULARE', 'Extracurriculare', true, now(), 'system', now(), 'system'),
    (893700000000000103, 'INCLUSIONE', 'Di inclusione', true, now(), 'system', now(), 'system'),
    (893700000000000104, 'SOCIO_TERAPEUTICO', 'Di inserimento socio-terapeutico', true, now(), 'system', now(), 'system')
ON CONFLICT (code) DO NOTHING;
