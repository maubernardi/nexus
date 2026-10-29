-- Report di restituzione (US-1101): uno per segnalazione, lo invia il Tutor e chiude il percorso (FORM_RESTITUZIONE).
-- Non si modifica dopo l'invio: UPDATE, DELETE e TRUNCATE sono rifiutati da trigger, come per company_note.
-- Obiettivi e difficoltà possono contenere dati personali: mai nell'audit né nei log.
CREATE TABLE closure_report (
    id                        bigint        NOT NULL,
    ticket_id                 bigint        NOT NULL,
    company_satisfaction      smallint      NOT NULL,
    beneficiary_satisfaction  smallint      NOT NULL,
    objectives                varchar(2000),
    difficulties              varchar(2000),
    hired                     boolean       NOT NULL,
    hiring_formula_id         bigint,
    no_hire_reason_id         bigint,
    job_slot_still_available  boolean       NOT NULL,
    created_at                timestamptz   NOT NULL,
    created_by                varchar(100)  NOT NULL,
    CONSTRAINT pk_closure_report PRIMARY KEY (id),
    CONSTRAINT uq_closure_report_ticket UNIQUE (ticket_id),
    CONSTRAINT fk_closure_report_ticket FOREIGN KEY (ticket_id) REFERENCES ticket (id),
    CONSTRAINT fk_closure_report_hiring_formula FOREIGN KEY (hiring_formula_id) REFERENCES hiring_formula (id),
    CONSTRAINT fk_closure_report_no_hire_reason FOREIGN KEY (no_hire_reason_id) REFERENCES no_hire_reason (id),
    CONSTRAINT ck_closure_report_company_satisfaction CHECK (company_satisfaction BETWEEN 1 AND 5),
    CONSTRAINT ck_closure_report_beneficiary_satisfaction CHECK (beneficiary_satisfaction BETWEEN 1 AND 5),
    -- assunto: con la formula e senza motivo; non assunto: con il motivo e senza formula
    CONSTRAINT ck_closure_report_outcome CHECK (
        (hired AND hiring_formula_id IS NOT NULL AND no_hire_reason_id IS NULL)
        OR (NOT hired AND no_hire_reason_id IS NOT NULL AND hiring_formula_id IS NULL)),
    CONSTRAINT ck_closure_report_objectives CHECK (objectives IS NULL OR btrim(objectives) <> ''),
    CONSTRAINT ck_closure_report_difficulties CHECK (difficulties IS NULL OR btrim(difficulties) <> '')
);

CREATE INDEX ix_closure_report_hiring_formula ON closure_report (hiring_formula_id);
CREATE INDEX ix_closure_report_no_hire_reason ON closure_report (no_hire_reason_id);

CREATE FUNCTION closure_report_reject_change() RETURNS trigger
    LANGUAGE plpgsql AS
$$
BEGIN
    RAISE EXCEPTION 'closure_report non si modifica dopo l''invio: % non consentito', TG_OP
        USING ERRCODE = 'integrity_constraint_violation';
END;
$$;

CREATE TRIGGER trg_closure_report_no_update_delete
    BEFORE UPDATE OR DELETE ON closure_report
    FOR EACH ROW EXECUTE FUNCTION closure_report_reject_change();

CREATE TRIGGER trg_closure_report_no_truncate
    BEFORE TRUNCATE ON closure_report
    FOR EACH STATEMENT EXECUTE FUNCTION closure_report_reject_change();
