-- Sede di competenza della segnalazione (US-308): scelta alla creazione tra le sedi del progetto (obbligatoria se il
-- progetto ne ha), poi la correggono solo ADMIN e Coordinatore. Le segnalazioni già esistenti restano senza sede.
ALTER TABLE ticket ADD COLUMN competence_site_id bigint;
ALTER TABLE ticket ADD CONSTRAINT fk_ticket_competence_site FOREIGN KEY (competence_site_id)
    REFERENCES competence_site (id);
CREATE INDEX ix_ticket_competence_site ON ticket (competence_site_id);
