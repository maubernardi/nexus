-- US-1103: motivi di chiusura del tirocinio, catalogo dell'ADMIN (PO, 15/11). «Chiusura regolare» (codice
-- CHIUSURA_REGOLARE) è il motivo automatico e non si disattiva (controllo nel servizio); le altre voci attive sono le
-- interruzioni anticipate che il Tutor sceglie nel report di restituzione. Solo additiva (A16).
CREATE TABLE closure_reason (
    id          bigint       NOT NULL,
    code        varchar(60)  NOT NULL,
    name        varchar(200) NOT NULL,
    active      boolean      NOT NULL DEFAULT true,
    created_at  timestamptz  NOT NULL,
    created_by  varchar(100) NOT NULL,
    updated_at  timestamptz  NOT NULL,
    updated_by  varchar(100) NOT NULL,
    CONSTRAINT pk_closure_reason PRIMARY KEY (id),
    CONSTRAINT uq_closure_reason_code UNIQUE (code),
    CONSTRAINT ck_closure_reason_code CHECK (code ~ '^[A-Z0-9_]+$'),
    CONSTRAINT ck_closure_reason_name CHECK (btrim(name) <> '')
);

CREATE UNIQUE INDEX uq_closure_reason_active_name ON closure_reason (lower(name)) WHERE active;

INSERT INTO closure_reason (id, code, name, active, created_at, created_by, updated_at, updated_by) VALUES
    (893700000000000361, 'CHIUSURA_REGOLARE', 'Chiusura regolare', true, now(), 'system', now(), 'system'),
    (893700000000000362, 'INTERRUZIONE_AZIENDA', 'Interruzione anticipata per volontà dell''azienda', true, now(),
        'system', now(), 'system'),
    (893700000000000363, 'INTERRUZIONE_DESTINATARIO', 'Interruzione anticipata per volontà del destinatario', true,
        now(), 'system', now(), 'system')
ON CONFLICT (code) DO NOTHING;

-- Motivo del report di restituzione: facoltativo nel database per i report già inviati e per la release precedente
-- (A16); obbligatorio nel servizio per i report nuovi.
ALTER TABLE closure_report ADD COLUMN closure_reason_id bigint;
ALTER TABLE closure_report
    ADD CONSTRAINT fk_closure_report_closure_reason FOREIGN KEY (closure_reason_id) REFERENCES closure_reason (id);
CREATE INDEX ix_closure_report_closure_reason ON closure_report (closure_reason_id);
