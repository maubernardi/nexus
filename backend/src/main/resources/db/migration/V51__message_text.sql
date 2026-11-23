-- Testi dei messaggi modificabili dall'ADMIN (US-1204, PO 22/11): per ogni notifica il testo nell'app, la parte
-- distintiva dell'oggetto dell'email e il testo dell'email; per le email del cambio di indirizzo oggetto e testo.
-- Qui stanno solo i testi cambiati: i predefiniti sono nel codice, e «ripristina» cancella la riga.

CREATE TABLE message_text (
    id          bigint        NOT NULL,
    message     varchar(40)   NOT NULL,
    part        varchar(20)   NOT NULL,
    text        varchar(4000) NOT NULL,
    created_at  timestamptz   NOT NULL,
    created_by  varchar(100)  NOT NULL,
    updated_at  timestamptz   NOT NULL,
    updated_by  varchar(100)  NOT NULL,
    CONSTRAINT pk_message_text PRIMARY KEY (id),
    CONSTRAINT uq_message_text UNIQUE (message, part),
    CONSTRAINT ck_message_text_message CHECK (message ~ '^[A-Z][A-Z_]*$'),
    CONSTRAINT ck_message_text_part CHECK (part IN ('APP', 'EMAIL_SUBJECT', 'EMAIL_BODY')),
    CONSTRAINT ck_message_text_text CHECK (btrim(text) <> '')
);
