-- Cataloghi del report di restituzione (US-109, review dello Sprint 7 §8.4): formule di assunzione e motivi della
-- mancata assunzione, gestiti dall'ADMIN come gli altri cataloghi (US-106) perché le voci "devono poter cambiare senza
-- rilascio" (Q6). Il report di restituzione (US-1101) propone solo le voci attive.
CREATE TABLE hiring_formula (
    id          bigint       NOT NULL,
    code        varchar(30)  NOT NULL,
    name        varchar(200) NOT NULL,
    active      boolean      NOT NULL DEFAULT true,
    created_at  timestamptz  NOT NULL,
    created_by  varchar(100) NOT NULL,
    updated_at  timestamptz  NOT NULL,
    updated_by  varchar(100) NOT NULL,
    CONSTRAINT pk_hiring_formula PRIMARY KEY (id),
    CONSTRAINT uq_hiring_formula_code UNIQUE (code),
    CONSTRAINT ck_hiring_formula_code CHECK (code ~ '^[A-Z0-9_]+$'),
    CONSTRAINT ck_hiring_formula_name CHECK (btrim(name) <> '')
);

CREATE UNIQUE INDEX uq_hiring_formula_active_name ON hiring_formula (lower(name)) WHERE active;

CREATE TABLE no_hire_reason (
    id          bigint       NOT NULL,
    code        varchar(30)  NOT NULL,
    name        varchar(200) NOT NULL,
    active      boolean      NOT NULL DEFAULT true,
    created_at  timestamptz  NOT NULL,
    created_by  varchar(100) NOT NULL,
    updated_at  timestamptz  NOT NULL,
    updated_by  varchar(100) NOT NULL,
    CONSTRAINT pk_no_hire_reason PRIMARY KEY (id),
    CONSTRAINT uq_no_hire_reason_code UNIQUE (code),
    CONSTRAINT ck_no_hire_reason_code CHECK (code ~ '^[A-Z0-9_]+$'),
    CONSTRAINT ck_no_hire_reason_name CHECK (btrim(name) <> '')
);

CREATE UNIQUE INDEX uq_no_hire_reason_active_name ON no_hire_reason (lower(name)) WHERE active;

-- Voci provvisorie indicate dal PO (Q6, 01/10), presenti in ogni ambiente; l'ADMIN le cambia dalla pagina "Cataloghi".
INSERT INTO hiring_formula (id, code, name, active, created_at, created_by, updated_at, updated_by) VALUES
    (893700000000000201, 'TEMPO_DETERMINATO_3_MESI', 'Tempo determinato 3 mesi', true, now(), 'system', now(), 'system'),
    (893700000000000202, 'TEMPO_DETERMINATO_6_MESI', 'Tempo determinato 6 mesi', true, now(), 'system', now(), 'system'),
    (893700000000000203, 'TEMPO_DETERMINATO_12_MESI', 'Tempo determinato 12 mesi', true, now(), 'system', now(), 'system'),
    (893700000000000204, 'TEMPO_INDETERMINATO', 'Tempo indeterminato', true, now(), 'system', now(), 'system'),
    (893700000000000205, 'CONTRATTO_A_CHIAMATA', 'Contratto a chiamata', true, now(), 'system', now(), 'system'),
    (893700000000000206, 'PRESTAZIONE_OCCASIONALE', 'Prestazione occasionale', true, now(), 'system', now(), 'system')
ON CONFLICT (code) DO NOTHING;

INSERT INTO no_hire_reason (id, code, name, active, created_at, created_by, updated_at, updated_by) VALUES
    (893700000000000301, 'AZIENDA_SENZA_RISORSE', 'L''azienda non ha risorse', true, now(), 'system', now(), 'system'),
    (893700000000000302, 'TIROCINANTE_NON_IDONEO', 'L''azienda giudica il tirocinante non idoneo', true, now(), 'system', now(), 'system'),
    (893700000000000303, 'NESSUNA_INTENZIONE', 'L''azienda non aveva mai espresso l''intenzione di assumere', true, now(), 'system', now(), 'system')
ON CONFLICT (code) DO NOTHING;
