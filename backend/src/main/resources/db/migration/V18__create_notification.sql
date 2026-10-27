-- Notifiche in app (US-1201): ogni passaggio della segnalazione avvisa la controparte (Tutor <-> operatore).
-- Nessun dato personale: solo il tipo e la segnalazione collegata (il testo lo compone l'interfaccia).

CREATE TABLE notification (
    id            bigint       NOT NULL,
    recipient_id  bigint       NOT NULL,
    type          varchar(40)  NOT NULL,
    ticket_id     bigint,
    read_at       timestamptz,
    created_at    timestamptz  NOT NULL,
    created_by    varchar(100) NOT NULL,
    CONSTRAINT pk_notification PRIMARY KEY (id),
    CONSTRAINT fk_notification_recipient FOREIGN KEY (recipient_id) REFERENCES app_user (id),
    CONSTRAINT fk_notification_ticket FOREIGN KEY (ticket_id) REFERENCES ticket (id),
    CONSTRAINT ck_notification_type CHECK (type ~ '^[A-Z][A-Z_]*$')
);

-- elenco e contatore delle non lette del destinatario
CREATE INDEX ix_notification_recipient ON notification (recipient_id, created_at DESC);
CREATE INDEX ix_notification_unread ON notification (recipient_id) WHERE read_at IS NULL;

-- L'avviso provvisorio "tipologia cambiata" (US-302) diventa una notifica (review dello Sprint 4, §8.3);
-- come id si riusa quello della segnalazione, un TSID già univoco.
INSERT INTO notification (id, recipient_id, type, ticket_id, created_at, created_by)
SELECT t.id, t.tutor_id, 'CATEGORY_CHANGED', t.id, now(), 'system'
FROM ticket t
WHERE t.category_change_unseen;

ALTER TABLE ticket DROP COLUMN category_change_unseen;
