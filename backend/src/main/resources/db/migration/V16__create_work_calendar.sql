-- Calendario unico dei giorni lavorativi (US-107): vale per tutti i progetti e per tutte le segnalazioni.
-- Le festività nazionali sono calcolate nel codice (US-703); qui il patrono e le chiusure, impostati dall'ADMIN.

-- Una sola riga (id = 1): il patrono come giorno e mese, inizialmente il 24 giugno (San Giovanni, Firenze).
CREATE TABLE work_calendar (
    id            smallint     NOT NULL,
    patron_month  smallint     NOT NULL,
    patron_day    smallint     NOT NULL,
    version       bigint       NOT NULL DEFAULT 0,
    created_at    timestamptz  NOT NULL,
    created_by    varchar(100) NOT NULL,
    updated_at    timestamptz  NOT NULL,
    updated_by    varchar(100) NOT NULL,
    CONSTRAINT pk_work_calendar PRIMARY KEY (id),
    CONSTRAINT ck_work_calendar_single_row CHECK (id = 1),
    CONSTRAINT ck_work_calendar_patron_month CHECK (patron_month BETWEEN 1 AND 12),
    CONSTRAINT ck_work_calendar_patron_day CHECK (patron_day BETWEEN 1 AND 31)
);

INSERT INTO work_calendar (id, patron_month, patron_day, created_at, created_by, updated_at, updated_by)
VALUES (1, 6, 24, now(), 'system', now(), 'system');

-- Chiusure della cooperativa: un giorno (start_date = end_date) o un periodo, con descrizione.
CREATE TABLE calendar_closure (
    id           bigint       NOT NULL,
    start_date   date         NOT NULL,
    end_date     date         NOT NULL,
    description  varchar(200) NOT NULL,
    created_at   timestamptz  NOT NULL,
    created_by   varchar(100) NOT NULL,
    updated_at   timestamptz  NOT NULL,
    updated_by   varchar(100) NOT NULL,
    CONSTRAINT pk_calendar_closure PRIMARY KEY (id),
    CONSTRAINT ck_calendar_closure_period CHECK (end_date >= start_date),
    CONSTRAINT ck_calendar_closure_description CHECK (btrim(description) <> '')
);

CREATE INDEX ix_calendar_closure_period ON calendar_closure (start_date, end_date);
