-- Audit trail delle aziende: registro in sola aggiunta.
-- created_at/created_by sono istante e autore dell'evento; nessuna colonna di modifica.
-- L'immutabilità è imposta dal database: UPDATE, DELETE e TRUNCATE sono rifiutati da trigger.

CREATE TABLE company_audit_event (
    id          bigint       NOT NULL,
    company_id  bigint       NOT NULL,
    ticket_id   bigint,
    event_type  varchar(50)  NOT NULL,
    details     jsonb        NOT NULL DEFAULT '{}'::jsonb,
    created_at  timestamptz  NOT NULL,
    created_by  varchar(100) NOT NULL,
    CONSTRAINT pk_company_audit_event PRIMARY KEY (id),
    CONSTRAINT fk_company_audit_event_company FOREIGN KEY (company_id) REFERENCES company (id),
    CONSTRAINT fk_company_audit_event_ticket FOREIGN KEY (ticket_id) REFERENCES ticket (id),
    CONSTRAINT ck_company_audit_event_type CHECK (event_type ~ '^[A-Z][A-Z_]*$'),
    CONSTRAINT ck_company_audit_event_details CHECK (jsonb_typeof(details) = 'object')
);

CREATE INDEX ix_company_audit_event_company ON company_audit_event (company_id, created_at);
CREATE INDEX ix_company_audit_event_ticket ON company_audit_event (ticket_id);

CREATE FUNCTION company_audit_event_reject_change() RETURNS trigger
    LANGUAGE plpgsql AS
$$
BEGIN
    RAISE EXCEPTION 'company_audit_event è in sola aggiunta: % non consentito', TG_OP
        USING ERRCODE = 'integrity_constraint_violation';
END;
$$;

CREATE TRIGGER trg_company_audit_event_no_update_delete
    BEFORE UPDATE OR DELETE ON company_audit_event
    FOR EACH ROW EXECUTE FUNCTION company_audit_event_reject_change();

CREATE TRIGGER trg_company_audit_event_no_truncate
    BEFORE TRUNCATE ON company_audit_event
    FOR EACH STATEMENT EXECUTE FUNCTION company_audit_event_reject_change();
