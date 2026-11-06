-- Progetti gestiti dall'ADMIN nella pagina "Cataloghi" (US-102), con le regole degli altri cataloghi (V20):
-- nome non vuoto e unico tra i progetti attivi senza distinguere maiuscole e minuscole; codice ricavato dal nome,
-- fino a 30 caratteri come negli altri cataloghi (era 20).
ALTER TABLE project ALTER COLUMN code TYPE varchar(30);
ALTER TABLE project ADD CONSTRAINT ck_project_name CHECK (btrim(name) <> '');
CREATE UNIQUE INDEX uq_project_active_name ON project (lower(name)) WHERE active;
