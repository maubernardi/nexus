-- Tentativi di contatto ed esito della richiesta (US-512, analisi con il PO del 23/11). L'operatore che ha la richiesta
-- in carico segna i tentativi (data, esito fisso, nota facoltativa per il Tutor) e la chiude a mano con l'esito finale
-- e una nota, collegandola se serve all'azienda dell'anagrafica. Alla chiusura il Tutor riceve l'avviso, che porta alla
-- richiesta. Tentativi e chiusura vanno nell'audit (EN-2); le note, testo libero, no.

CREATE TABLE contact_request_attempt (
    id                  bigint       NOT NULL,
    contact_request_id  bigint       NOT NULL,
    attempt_date        date         NOT NULL,
    outcome             varchar(20)  NOT NULL,
    note                varchar(500),
    created_at          timestamptz  NOT NULL,
    created_by          varchar(100) NOT NULL,
    CONSTRAINT pk_contact_request_attempt PRIMARY KEY (id),
    CONSTRAINT fk_contact_request_attempt_request FOREIGN KEY (contact_request_id) REFERENCES contact_request (id),
    CONSTRAINT ck_contact_request_attempt_outcome CHECK (outcome IN ('DISPONIBILE', 'NON_INTERESSATA', 'NON_RAGGIUNGIBILE'))
);

CREATE INDEX ix_contact_request_attempt_request ON contact_request_attempt (contact_request_id);

ALTER TABLE contact_request
    ADD COLUMN outcome varchar(20),
    ADD COLUMN closing_note varchar(500),
    ADD COLUMN closed_at timestamptz,
    ADD CONSTRAINT ck_contact_request_outcome CHECK (outcome IN ('DISPONIBILE', 'NON_INTERESSATA', 'NON_RAGGIUNGIBILE')),
    -- chiusa: esito, nota (non vuota) e istante; altrimenti nessuno dei tre
    ADD CONSTRAINT ck_contact_request_closed CHECK (
        (status = 'CHIUSA' AND outcome IS NOT NULL AND closed_at IS NOT NULL AND closing_note IS NOT NULL
            AND btrim(closing_note) <> '')
        OR (status <> 'CHIUSA' AND outcome IS NULL AND closed_at IS NULL AND closing_note IS NULL));

-- un avviso può portare alla richiesta di contatto (US-512), invece che a una segnalazione o a un'azienda
ALTER TABLE notification
    ADD COLUMN contact_request_id bigint,
    ADD CONSTRAINT fk_notification_contact_request FOREIGN KEY (contact_request_id) REFERENCES contact_request (id);
