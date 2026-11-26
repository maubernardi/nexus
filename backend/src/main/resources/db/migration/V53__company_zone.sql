-- Zona dell'azienda (US-511, analisi con il PO del 23/11): una voce del catalogo delle zone (US-106), facoltativa, che
-- sceglie il Call Center. Le aziende importate partono senza: in Access il campo non è mai stato usato. Il Tutor la
-- vede nella sua lista delle aziende e la usa come filtro.

ALTER TABLE company
    ADD COLUMN zone_id bigint,
    ADD CONSTRAINT fk_company_zone FOREIGN KEY (zone_id) REFERENCES zone (id);

CREATE INDEX ix_company_zone ON company (zone_id);
