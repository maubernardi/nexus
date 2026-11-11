-- Indirizzi con il comune (US-1007): la versione del comune (US-114) scelta per la sede legale dell'azienda, la sede del
-- tirocinio e la residenza del beneficiario nella scheda, più il numero civico della residenza. Solo colonne nuove: la
-- release precedente deve poter ripartire su questo schema (A16). La colonna internship.residence resta e diventa la
-- via; i testi già inseriti restano negli indirizzi, con il comune da scegliere.
ALTER TABLE company ADD COLUMN legal_municipality_id bigint;
ALTER TABLE company ADD CONSTRAINT fk_company_legal_municipality FOREIGN KEY (legal_municipality_id)
    REFERENCES municipality (id);
CREATE INDEX ix_company_legal_municipality ON company (legal_municipality_id);

ALTER TABLE internship ADD COLUMN site_municipality_id bigint;
ALTER TABLE internship ADD CONSTRAINT fk_internship_site_municipality FOREIGN KEY (site_municipality_id)
    REFERENCES municipality (id);
CREATE INDEX ix_internship_site_municipality ON internship (site_municipality_id);

ALTER TABLE internship ADD COLUMN residence_municipality_id bigint;
ALTER TABLE internship ADD CONSTRAINT fk_internship_residence_municipality FOREIGN KEY (residence_municipality_id)
    REFERENCES municipality (id);
CREATE INDEX ix_internship_residence_municipality ON internship (residence_municipality_id);

ALTER TABLE internship ADD COLUMN residence_street_number varchar(20);
ALTER TABLE internship ADD CONSTRAINT ck_internship_residence_street_number
    CHECK (residence_street_number IS NULL OR btrim(residence_street_number) <> '');
