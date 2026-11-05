-- Cambio del proprio indirizzo email (US-110): richiesta in attesa della conferma con il codice (OTP) inviato al nuovo
-- indirizzo. Una per utente; una nuova richiesta sostituisce la precedente, conferma e annullamento la cancellano.
-- È uno stato di lavoro: la traccia immodificabile del cambio è l'evento di audit dell'utente.
-- Il codice c'è solo come hash BCrypt; il nuovo indirizzo è un dato personale: mai nell'audit né nei log.
CREATE TABLE email_change_request (
    id            bigint        NOT NULL,
    user_id       bigint        NOT NULL,
    new_email     varchar(254)  NOT NULL,
    code_hash     varchar(100)  NOT NULL,
    attempts      smallint      NOT NULL DEFAULT 0,
    expires_at    timestamptz   NOT NULL,
    sent_at       timestamptz   NOT NULL,
    created_at    timestamptz   NOT NULL,
    created_by    varchar(100)  NOT NULL,
    updated_at    timestamptz   NOT NULL,
    updated_by    varchar(100)  NOT NULL,
    CONSTRAINT pk_email_change_request PRIMARY KEY (id),
    CONSTRAINT uq_email_change_request_user UNIQUE (user_id),
    CONSTRAINT fk_email_change_request_user FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT ck_email_change_request_new_email CHECK (new_email ~ '^[^@\s]+@[^@\s]+$'),
    CONSTRAINT ck_email_change_request_attempts CHECK (attempts BETWEEN 0 AND 5),
    CONSTRAINT ck_email_change_request_expiry CHECK (expires_at > sent_at)
);
