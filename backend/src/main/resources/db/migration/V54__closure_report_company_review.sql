-- Valutazione dell'azienda alla fine del tirocinio (US-1104, PO 25 – 26/11): nel report di restituzione tre voti da 1 a 5
-- (interazione con l'azienda, accoglienza del beneficiario, capacità formativa dell'azienda e del tutor aziendale) e una
-- nota facoltativa, senza dati personali. L'azienda è quella del tirocinio al momento della chiusura: la valutazione
-- resta sua anche se la mansione cambia. I report inviati prima restano senza. La tabella resta immodificabile dopo
-- l'invio (V26): qui si aggiungono solo colonne, nessuna riga cambia.

ALTER TABLE closure_report
    ADD COLUMN company_id          bigint,
    ADD COLUMN company_interaction smallint,
    ADD COLUMN company_welcome     smallint,
    ADD COLUMN company_training    smallint,
    ADD COLUMN company_review_note varchar(500),
    ADD CONSTRAINT fk_closure_report_company FOREIGN KEY (company_id) REFERENCES company (id),
    ADD CONSTRAINT ck_closure_report_company_interaction CHECK (company_interaction BETWEEN 1 AND 5),
    ADD CONSTRAINT ck_closure_report_company_welcome CHECK (company_welcome BETWEEN 1 AND 5),
    ADD CONSTRAINT ck_closure_report_company_training CHECK (company_training BETWEEN 1 AND 5),
    -- i tre voti tutti insieme e con l'azienda; senza voti niente nota
    ADD CONSTRAINT ck_closure_report_company_review CHECK (
        (company_interaction IS NULL AND company_welcome IS NULL AND company_training IS NULL
            AND company_review_note IS NULL)
        OR (company_interaction IS NOT NULL AND company_welcome IS NOT NULL AND company_training IS NOT NULL
            AND company_id IS NOT NULL)),
    ADD CONSTRAINT ck_closure_report_company_review_note CHECK (
        company_review_note IS NULL OR btrim(company_review_note) <> '');

-- valutazioni di un'azienda, dalla più recente
CREATE INDEX ix_closure_report_company ON closure_report (company_id, actual_end_date);
