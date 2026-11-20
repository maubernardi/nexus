-- US-1309: archivio delle chiamate del database attuale (Access), importate con l'importazione dal file. Non compaiono
-- nell'interfaccia: restano disponibili se serviranno (PO, 19/11). Esito, progetto e operatore sono testi, così
-- l'archivio si legge senza Access. L'id è 893700000050000000 + il numero della chiamata in Access. Solo additiva (A16).
CREATE TABLE legacy_call (
    id                     bigint       NOT NULL,
    legacy_number          integer      NOT NULL,
    company_id             bigint,
    legacy_company_number  integer      NOT NULL,
    called_at              timestamptz,
    subject                varchar(255),
    outcome                varchar(50),
    outcome_at             timestamptz,
    notes                  text,
    handled                boolean      NOT NULL,
    internship             boolean      NOT NULL,
    stage                  boolean      NOT NULL,
    ist                    boolean      NOT NULL,
    work_grant             boolean      NOT NULL,
    hiring                 boolean      NOT NULL,
    giovanisi              boolean      NOT NULL,
    project                varchar(50),
    operator               varchar(200),
    legacy_updated_at      timestamptz,
    created_at             timestamptz  NOT NULL,
    created_by             varchar(100) NOT NULL,
    CONSTRAINT pk_legacy_call PRIMARY KEY (id),
    CONSTRAINT uq_legacy_call_legacy_number UNIQUE (legacy_number),
    CONSTRAINT fk_legacy_call_company FOREIGN KEY (company_id) REFERENCES company (id),
    CONSTRAINT ck_legacy_call_legacy_number CHECK (legacy_number > 0)
);

CREATE INDEX ix_legacy_call_company ON legacy_call (company_id);
