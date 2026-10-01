-- US-501: blocco ottimistico sulle aziende, modificabili da più operatori del Call Center.
ALTER TABLE company ADD COLUMN version bigint NOT NULL DEFAULT 0;
