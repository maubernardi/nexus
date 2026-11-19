-- US-510: referenti dell'azienda con il ruolo, recapiti e sede operativa (PO, 19/11). Solo additiva (A16): la release
-- precedente ignora tabelle e colonne nuove e continua a leggere company.contact_person, che resta com'è.

-- Ruoli dei referenti, catalogo dell'ADMIN, con i «Tipi di contatti» del database attuale ripuliti: fuori le righe
-- vuote (15, 17, 20), quelle che non sono ruoli (18, 21, 22) e il doppione 28 di «Legale Rappresentante». L'id di
-- ogni voce iniziale è 893700000000000500 + il numero del ruolo in Access, per l'importazione (US-1309).
CREATE TABLE contact_role (
    id          bigint       NOT NULL,
    code        varchar(60)  NOT NULL,
    name        varchar(200) NOT NULL,
    active      boolean      NOT NULL DEFAULT true,
    created_at  timestamptz  NOT NULL,
    created_by  varchar(100) NOT NULL,
    updated_at  timestamptz  NOT NULL,
    updated_by  varchar(100) NOT NULL,
    CONSTRAINT pk_contact_role PRIMARY KEY (id),
    CONSTRAINT uq_contact_role_code UNIQUE (code),
    CONSTRAINT ck_contact_role_code CHECK (code ~ '^[A-Z0-9_]+$'),
    CONSTRAINT ck_contact_role_name CHECK (btrim(name) <> '')
);

CREATE UNIQUE INDEX uq_contact_role_active_name ON contact_role (lower(name)) WHERE active;

INSERT INTO contact_role (id, code, name, active, created_at, created_by, updated_at, updated_by) VALUES
    (893700000000000501, 'REFERENTE', 'Referente', true, now(), 'system', now(), 'system'),
    (893700000000000502, 'ADDETTO_DI_SEGRETERIA', 'Addetto di segreteria', true, now(), 'system', now(), 'system'),
    (893700000000000503, 'RESPONSABILE_DEL_PERSONALE', 'Responsabile del personale', true, now(), 'system', now(), 'system'),
    (893700000000000513, 'TITOLARE', 'Titolare', true, now(), 'system', now(), 'system'),
    (893700000000000516, 'PRESIDENTE', 'Presidente', true, now(), 'system', now(), 'system'),
    (893700000000000519, 'AMMINISTRAZIONE', 'Amministrazione', true, now(), 'system', now(), 'system'),
    (893700000000000523, 'DIRETTORE', 'Direttore', true, now(), 'system', now(), 'system'),
    (893700000000000524, 'SOCIO', 'Socio', true, now(), 'system', now(), 'system'),
    (893700000000000525, 'CAPO_AREA', 'Capo area', true, now(), 'system', now(), 'system'),
    (893700000000000526, 'CENTRALINO', 'Centralino', true, now(), 'system', now(), 'system'),
    (893700000000000527, 'LEGALE_RAPPRESENTANTE', 'Legale rappresentante', true, now(), 'system', now(), 'system'),
    (893700000000000529, 'AMMINISTRATORE_DELEGATO', 'Amministratore delegato', true, now(), 'system', now(), 'system')
ON CONFLICT (code) DO NOTHING;

-- Referenti dell'azienda, in ordine: righe sostituite in blocco quando cambiano. Dati personali: nell'audit solo come
-- «cambiati». Serve almeno il nome o il ruolo: nel database attuale ci sono referenti con il solo ruolo.
CREATE TABLE company_contact (
    id          bigint       NOT NULL,
    company_id  bigint       NOT NULL,
    position    integer      NOT NULL,
    name        varchar(200),
    role_id     bigint,
    phone       varchar(30),
    mobile      varchar(30),
    email       varchar(254),
    created_at  timestamptz  NOT NULL,
    created_by  varchar(100) NOT NULL,
    CONSTRAINT pk_company_contact PRIMARY KEY (id),
    CONSTRAINT fk_company_contact_company FOREIGN KEY (company_id) REFERENCES company (id),
    CONSTRAINT fk_company_contact_role FOREIGN KEY (role_id) REFERENCES contact_role (id),
    CONSTRAINT uq_company_contact_position UNIQUE (company_id, position),
    CONSTRAINT ck_company_contact_position CHECK (position >= 0),
    CONSTRAINT ck_company_contact_name CHECK (name IS NULL OR btrim(name) <> ''),
    CONSTRAINT ck_company_contact_name_or_role CHECK (name IS NOT NULL OR role_id IS NOT NULL)
);

CREATE INDEX ix_company_contact_role ON company_contact (role_id);

-- Il referente di oggi diventa il primo, senza ruolo; l'id della riga è quello dell'azienda (una riga per azienda,
-- come V18 per le notifiche).
INSERT INTO company_contact (id, company_id, position, name, created_at, created_by)
SELECT c.id, c.id, 0, btrim(c.contact_person), now(), 'system'
FROM company c
WHERE c.contact_person IS NOT NULL
  AND btrim(c.contact_person) <> '';

-- Recapiti e sede operativa dell'azienda, facoltativi.
ALTER TABLE company ADD COLUMN mobile varchar(30);
ALTER TABLE company ADD COLUMN fax varchar(30);
ALTER TABLE company ADD COLUMN secondary_email varchar(254);
ALTER TABLE company ADD COLUMN operating_address varchar(300);
ALTER TABLE company ADD COLUMN operating_municipality_id bigint;
ALTER TABLE company ADD CONSTRAINT fk_company_operating_municipality FOREIGN KEY (operating_municipality_id)
    REFERENCES municipality (id);
CREATE INDEX ix_company_operating_municipality ON company (operating_municipality_id);
