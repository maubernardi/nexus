-- Contatti, ente di presa in carico e case manager del beneficiario (US-206, decisioni del PO del 09/11). Tutti
-- facoltativi: per scegliere un tirocinio di inclusione servono l'ente e nome, mail e telefono del case manager (il
-- controllo è nel servizio). Dati personali: l'anonimizzazione (US-1303) li cancella.
ALTER TABLE beneficiary
    ADD COLUMN email               varchar(254),
    ADD COLUMN phone               varchar(30),
    ADD COLUMN care_agency_id      bigint,
    ADD COLUMN case_manager_name   varchar(200),
    ADD COLUMN case_manager_office varchar(300),
    ADD COLUMN case_manager_phone  varchar(30),
    ADD COLUMN case_manager_email  varchar(254);

ALTER TABLE beneficiary
    ADD CONSTRAINT fk_beneficiary_care_agency FOREIGN KEY (care_agency_id) REFERENCES care_agency (id),
    ADD CONSTRAINT ck_beneficiary_email CHECK (email ~ '^[^@\s]+@[^@\s]+$'),
    ADD CONSTRAINT ck_beneficiary_phone CHECK (phone ~ '^[0-9 +./()-]+$'),
    ADD CONSTRAINT ck_beneficiary_case_manager_email CHECK (case_manager_email ~ '^[^@\s]+@[^@\s]+$'),
    ADD CONSTRAINT ck_beneficiary_case_manager_phone CHECK (case_manager_phone ~ '^[0-9 +./()-]+$'),
    ADD CONSTRAINT ck_beneficiary_case_manager_name CHECK (case_manager_name IS NULL OR btrim(case_manager_name) <> '');

CREATE INDEX ix_beneficiary_care_agency ON beneficiary (care_agency_id);
