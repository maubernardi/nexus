-- Cataloghi per i documenti reali (US-115, decisioni del PO del 09/11): sedi di competenza, tipi di tirocinio ammessi
-- per progetto, enti che hanno in carico i beneficiari. Gestiti dall'ADMIN come gli altri cataloghi (US-106).

-- Sede di competenza: il luogo della relazione finale; copre più comuni, vale per uno o più progetti.
CREATE TABLE competence_site (
    id          bigint       NOT NULL,
    code        varchar(60)  NOT NULL,
    name        varchar(200) NOT NULL,
    active      boolean      NOT NULL DEFAULT true,
    created_at  timestamptz  NOT NULL,
    created_by  varchar(100) NOT NULL,
    updated_at  timestamptz  NOT NULL,
    updated_by  varchar(100) NOT NULL,
    CONSTRAINT pk_competence_site PRIMARY KEY (id),
    CONSTRAINT uq_competence_site_code UNIQUE (code),
    CONSTRAINT ck_competence_site_code CHECK (code ~ '^[A-Z0-9_]+$'),
    CONSTRAINT ck_competence_site_name CHECK (btrim(name) <> '')
);

CREATE UNIQUE INDEX uq_competence_site_active_name ON competence_site (lower(name)) WHERE active;

CREATE TABLE competence_site_project (
    competence_site_id  bigint NOT NULL,
    project_id          bigint NOT NULL,
    CONSTRAINT pk_competence_site_project PRIMARY KEY (competence_site_id, project_id),
    CONSTRAINT fk_competence_site_project_site FOREIGN KEY (competence_site_id) REFERENCES competence_site (id),
    CONSTRAINT fk_competence_site_project_project FOREIGN KEY (project_id) REFERENCES project (id)
);

CREATE INDEX ix_competence_site_project_project ON competence_site_project (project_id);

-- Tipi di tirocinio ammessi da un progetto; un progetto senza righe ammette tutti i tipi (fino al censimento).
CREATE TABLE project_internship_type (
    project_id          bigint NOT NULL,
    internship_type_id  bigint NOT NULL,
    CONSTRAINT pk_project_internship_type PRIMARY KEY (project_id, internship_type_id),
    CONSTRAINT fk_project_internship_type_project FOREIGN KEY (project_id) REFERENCES project (id),
    CONSTRAINT fk_project_internship_type_type FOREIGN KEY (internship_type_id) REFERENCES internship_type (id)
);

-- Ente che ha in carico i beneficiari dei tirocini di inclusione, con i dati per convenzione e progetto personalizzato.
-- I dati sono facoltativi: l'ADMIN li completa dai Cataloghi.
CREATE TABLE care_agency (
    id                          bigint       NOT NULL,
    code                        varchar(60)  NOT NULL,
    name                        varchar(200) NOT NULL,
    active                      boolean      NOT NULL DEFAULT true,
    kind                        varchar(30)  NOT NULL,
    fiscal_code                 varchar(16),
    legal_address               varchar(300),
    representative_name         varchar(200),
    representative_role         varchar(200),
    representative_birth_place  varchar(200),
    representative_birth_date   date,
    referent_name               varchar(200),
    referent_phone              varchar(30),
    referent_email              varchar(254),
    version                     bigint       NOT NULL DEFAULT 0,
    created_at                  timestamptz  NOT NULL,
    created_by                  varchar(100) NOT NULL,
    updated_at                  timestamptz  NOT NULL,
    updated_by                  varchar(100) NOT NULL,
    CONSTRAINT pk_care_agency PRIMARY KEY (id),
    CONSTRAINT uq_care_agency_code UNIQUE (code),
    CONSTRAINT ck_care_agency_code CHECK (code ~ '^[A-Z0-9_]+$'),
    CONSTRAINT ck_care_agency_name CHECK (btrim(name) <> ''),
    CONSTRAINT ck_care_agency_kind CHECK (kind IN ('COMUNE', 'UNIONE_COMUNI', 'COMUNITA_MONTANA',
        'SOCIETA_DELLA_SALUTE', 'AZIENDA_SANITARIA', 'UIEPE', 'ALTRO')),
    CONSTRAINT ck_care_agency_fiscal_code CHECK (fiscal_code ~ '^([0-9]{11}|[A-Z0-9]{16})$')
);

CREATE UNIQUE INDEX uq_care_agency_active_name ON care_agency (lower(name)) WHERE active;

-- Voci iniziali indicate dal PO, presenti in ogni ambiente (nessun dato di persone).
INSERT INTO competence_site (id, code, name, active, created_at, created_by, updated_at, updated_by) VALUES
    (893700000000000341, 'FIRENZE', 'Firenze', true, now(), 'system', now(), 'system'),
    (893700000000000342, 'BORGO_SAN_LORENZO', 'Borgo San Lorenzo', true, now(), 'system', now(), 'system'),
    (893700000000000343, 'SESTO_FIORENTINO', 'Sesto Fiorentino', true, now(), 'system', now(), 'system')
ON CONFLICT (code) DO NOTHING;

-- le sedi e i tipi ammessi valgono per GOL, se il progetto esiste già (nella demo sì; altrove li imposta l'ADMIN)
INSERT INTO competence_site_project (competence_site_id, project_id)
SELECT s.id, p.id FROM competence_site s CROSS JOIN project p
WHERE p.code = 'GOL' AND s.code IN ('FIRENZE', 'BORGO_SAN_LORENZO', 'SESTO_FIORENTINO')
ON CONFLICT DO NOTHING;

INSERT INTO project_internship_type (project_id, internship_type_id)
SELECT p.id, t.id FROM project p CROSS JOIN internship_type t
WHERE p.code = 'GOL' AND t.code IN ('EXTRACURRICULARE', 'INCLUSIONE')
ON CONFLICT DO NOTHING;

INSERT INTO care_agency (id, code, name, active, kind, created_at, created_by, updated_at, updated_by) VALUES
    (893700000000000351, 'COMUNE_DI_FIRENZE', 'Comune di Firenze', true, 'COMUNE', now(), 'system', now(), 'system'),
    (893700000000000352, 'SOCIETA_DELLA_SALUTE_DI_FIRENZE', 'Società della Salute di Firenze', true,
        'SOCIETA_DELLA_SALUTE', now(), 'system', now(), 'system'),
    (893700000000000353, 'SOCIETA_DELLA_SALUTE_MUGELLO', 'Società della Salute Mugello', true,
        'SOCIETA_DELLA_SALUTE', now(), 'system', now(), 'system'),
    (893700000000000354, 'SOCIETA_DELLA_SALUTE_FIORENTINA_NORD_OVEST', 'Società della Salute Fiorentina Nord Ovest',
        true, 'SOCIETA_DELLA_SALUTE', now(), 'system', now(), 'system')
ON CONFLICT (code) DO NOTHING;
