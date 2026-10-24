-- Diario delle note dell'azienda (US-505): note datate in sola aggiunta.
-- created_at/created_by sono istante e autore della nota; le note non si modificano né si cancellano:
-- UPDATE, DELETE e TRUNCATE sono rifiutati da trigger, come per company_audit_event.

CREATE TABLE company_note (
    id          bigint        NOT NULL,
    company_id  bigint        NOT NULL,
    text        varchar(2000) NOT NULL,
    created_at  timestamptz   NOT NULL,
    created_by  varchar(100)  NOT NULL,
    CONSTRAINT pk_company_note PRIMARY KEY (id),
    CONSTRAINT fk_company_note_company FOREIGN KEY (company_id) REFERENCES company (id),
    CONSTRAINT ck_company_note_text CHECK (btrim(text) <> '')
);

CREATE INDEX ix_company_note_company ON company_note (company_id, created_at);

CREATE FUNCTION company_note_reject_change() RETURNS trigger
    LANGUAGE plpgsql AS
$$
BEGIN
    RAISE EXCEPTION 'company_note è in sola aggiunta: % non consentito', TG_OP
        USING ERRCODE = 'integrity_constraint_violation';
END;
$$;

CREATE TRIGGER trg_company_note_no_update_delete
    BEFORE UPDATE OR DELETE ON company_note
    FOR EACH ROW EXECUTE FUNCTION company_note_reject_change();

CREATE TRIGGER trg_company_note_no_truncate
    BEFORE TRUNCATE ON company_note
    FOR EACH STATEMENT EXECUTE FUNCTION company_note_reject_change();
