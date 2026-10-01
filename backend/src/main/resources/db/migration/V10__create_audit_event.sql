-- Audit di ogni cambio di stato e scelta (EN-2): registro generico in sola aggiunta.
-- Nessuna FK verso gli oggetti: il registro sopravvive agli oggetti e non ne blocca l'anonimizzazione.
-- changes: campo -> {"before": …, "after": …} oppure {"redacted": true} per i dati personali (mai i valori).

CREATE TABLE audit_event (
    id           bigint       NOT NULL,
    entity_type  varchar(40)  NOT NULL,
    entity_id    bigint       NOT NULL,
    action       varchar(60)  NOT NULL,
    changes      jsonb        NOT NULL DEFAULT '{}'::jsonb,
    reason       text,
    created_at   timestamptz  NOT NULL,
    created_by   varchar(100) NOT NULL,
    CONSTRAINT pk_audit_event PRIMARY KEY (id),
    CONSTRAINT ck_audit_event_entity_type CHECK (entity_type ~ '^[A-Z][A-Z_]*$'),
    CONSTRAINT ck_audit_event_action CHECK (action ~ '^[A-Z][A-Z_]*$'),
    CONSTRAINT ck_audit_event_changes CHECK (jsonb_typeof(changes) = 'object'),
    CONSTRAINT ck_audit_event_reason CHECK (reason IS NULL OR btrim(reason) <> '')
);

CREATE INDEX ix_audit_event_entity ON audit_event (entity_type, entity_id, created_at);
CREATE INDEX ix_audit_event_author ON audit_event (created_by, created_at);
CREATE INDEX ix_audit_event_created_at ON audit_event (created_at);

CREATE FUNCTION audit_event_reject_change() RETURNS trigger
    LANGUAGE plpgsql AS
$$
BEGIN
    RAISE EXCEPTION 'audit_event è in sola aggiunta: % non consentito', TG_OP
        USING ERRCODE = 'integrity_constraint_violation';
END;
$$;

CREATE TRIGGER trg_audit_event_no_update_delete
    BEFORE UPDATE OR DELETE ON audit_event
    FOR EACH ROW EXECUTE FUNCTION audit_event_reject_change();

CREATE TRIGGER trg_audit_event_no_truncate
    BEFORE TRUNCATE ON audit_event
    FOR EACH STATEMENT EXECUTE FUNCTION audit_event_reject_change();
