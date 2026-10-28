-- Cataloghi gestiti dall'ADMIN (US-106): il nome non è vuoto e, tra le voci attive, è unico senza distinguere
-- maiuscole e minuscole. Le voci disattivate restano sui dati esistenti e possono avere il nome di una voce nuova.
ALTER TABLE zone ADD CONSTRAINT ck_zone_name CHECK (btrim(name) <> '');
ALTER TABLE job_category ADD CONSTRAINT ck_job_category_name CHECK (btrim(name) <> '');
CREATE UNIQUE INDEX uq_zone_active_name ON zone (lower(name)) WHERE active;
CREATE UNIQUE INDEX uq_job_category_active_name ON job_category (lower(name)) WHERE active;
