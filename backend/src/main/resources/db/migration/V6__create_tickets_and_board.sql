-- Segnalazioni (ticket), bacheca, storico degli stati e blacklist delle aziende.
-- ticket, job_slot e board_post si riferiscono a vicenda: le FK cicliche sono aggiunte in fondo.

CREATE SEQUENCE ticket_number_seq START WITH 1;
CREATE SEQUENCE board_post_number_seq START WITH 1;

CREATE TABLE ticket (
    id                         bigint       NOT NULL,
    number                     bigint       NOT NULL DEFAULT nextval('ticket_number_seq'),
    tutor_id                   bigint       NOT NULL,
    project_id                 bigint       NOT NULL,
    candidate_id               bigint       NOT NULL,
    type                       varchar(10)  NOT NULL,
    status                     varchar(40)  NOT NULL,
    is_fast_track              boolean      NOT NULL DEFAULT false,
    requested_job_category_id  bigint,
    requested_job_free_text    varchar(200),
    board_post_id              bigint,
    assigned_cc_operator_id    bigint,
    job_slot_id                bigint,
    timer_started_at           timestamptz,
    timer_reminder_sent_at     timestamptz,
    timer_deadline_at          timestamptz,
    version                    bigint       NOT NULL DEFAULT 0,
    created_at                 timestamptz  NOT NULL,
    created_by                 varchar(100) NOT NULL,
    updated_at                 timestamptz  NOT NULL,
    updated_by                 varchar(100) NOT NULL,
    CONSTRAINT pk_ticket PRIMARY KEY (id),
    CONSTRAINT uq_ticket_number UNIQUE (number),
    CONSTRAINT fk_ticket_tutor FOREIGN KEY (tutor_id) REFERENCES app_user (id),
    CONSTRAINT fk_ticket_project FOREIGN KEY (project_id) REFERENCES project (id),
    CONSTRAINT fk_ticket_candidate FOREIGN KEY (candidate_id) REFERENCES candidate (id),
    CONSTRAINT fk_ticket_requested_job_category FOREIGN KEY (requested_job_category_id) REFERENCES job_category (id),
    CONSTRAINT fk_ticket_assigned_cc_operator FOREIGN KEY (assigned_cc_operator_id) REFERENCES app_user (id),
    CONSTRAINT fk_ticket_job_slot FOREIGN KEY (job_slot_id) REFERENCES job_slot (id),
    CONSTRAINT ck_ticket_type CHECK (type IN ('NORMAL', 'SPECIAL')),
    CONSTRAINT ck_ticket_status CHECK (status IN (
        'NUOVA', 'IN_ATTESA_APPROVAZIONE_ADMIN', 'IN_LAVORAZIONE', 'PROPOSTA_AZIENDA', 'PROPOSTA_ACCOLTA',
        'APPUNTAMENTO', 'IN_TIROCINIO', 'FORM_RESTITUZIONE', 'RIAPERTO')),
    -- mansione richiesta: una tipologia del catalogo oppure un testo libero, mai entrambi né nessuno
    CONSTRAINT ck_ticket_requested_job CHECK ((requested_job_category_id IS NULL) <> (requested_job_free_text IS NULL)),
    CONSTRAINT ck_ticket_requested_job_text CHECK (requested_job_free_text IS NULL OR btrim(requested_job_free_text) <> ''),
    CONSTRAINT ck_ticket_special_board_post CHECK (type <> 'SPECIAL' OR board_post_id IS NOT NULL),
    CONSTRAINT ck_ticket_fast_track CHECK (NOT is_fast_track OR type = 'SPECIAL'),
    CONSTRAINT ck_ticket_timer CHECK ((timer_started_at IS NULL) = (timer_deadline_at IS NULL)),
    CONSTRAINT ck_ticket_timer_deadline CHECK (timer_deadline_at IS NULL OR timer_deadline_at > timer_started_at),
    CONSTRAINT ck_ticket_timer_reminder CHECK (timer_reminder_sent_at IS NULL OR timer_started_at IS NOT NULL)
);

ALTER SEQUENCE ticket_number_seq OWNED BY ticket.number;

CREATE INDEX ix_ticket_tutor ON ticket (tutor_id);
CREATE INDEX ix_ticket_project ON ticket (project_id);
CREATE INDEX ix_ticket_candidate ON ticket (candidate_id);
CREATE INDEX ix_ticket_job_slot ON ticket (job_slot_id);
CREATE INDEX ix_ticket_assigned_cc_operator ON ticket (assigned_cc_operator_id);
-- coda del Call Center: prima i fast-track, poi in ordine di arrivo
CREATE INDEX ix_ticket_queue ON ticket (status, is_fast_track DESC, created_at);
-- scheduler del timer: solo i ticket con timer attivo
CREATE INDEX ix_ticket_timer_deadline ON ticket (timer_deadline_at) WHERE timer_deadline_at IS NOT NULL;

-- Post di bacheca: azienda e zona si ricavano dalla mansione; project_id NULL = visibile a tutti.
-- age_min/age_max esprimono requisiti del programma di finanziamento, non preferenze.
CREATE TABLE board_post (
    id                      bigint       NOT NULL,
    number                  bigint       NOT NULL DEFAULT nextval('board_post_number_seq'),
    job_slot_id             bigint       NOT NULL,
    title                   varchar(200) NOT NULL,
    project_id              bigint,
    status                  varchar(20)  NOT NULL DEFAULT 'DRAFT',
    age_min                 smallint,
    age_max                 smallint,
    weekly_hours            smallint,
    duration_months         smallint,
    notes                   text,
    published_at            timestamptz,
    made_public_at          timestamptz,
    created_from_ticket_id  bigint,
    version                 bigint       NOT NULL DEFAULT 0,
    created_at              timestamptz  NOT NULL,
    created_by              varchar(100) NOT NULL,
    updated_at              timestamptz  NOT NULL,
    updated_by              varchar(100) NOT NULL,
    CONSTRAINT pk_board_post PRIMARY KEY (id),
    CONSTRAINT uq_board_post_number UNIQUE (number),
    CONSTRAINT fk_board_post_job_slot FOREIGN KEY (job_slot_id) REFERENCES job_slot (id),
    CONSTRAINT fk_board_post_project FOREIGN KEY (project_id) REFERENCES project (id),
    CONSTRAINT fk_board_post_created_from_ticket FOREIGN KEY (created_from_ticket_id) REFERENCES ticket (id),
    CONSTRAINT ck_board_post_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    CONSTRAINT ck_board_post_published CHECK (status <> 'PUBLISHED' OR published_at IS NOT NULL),
    CONSTRAINT ck_board_post_made_public CHECK (made_public_at IS NULL OR project_id IS NULL),
    CONSTRAINT ck_board_post_age_min CHECK (age_min IS NULL OR age_min BETWEEN 14 AND 99),
    CONSTRAINT ck_board_post_age_max CHECK (age_max IS NULL OR age_max BETWEEN 14 AND 99),
    CONSTRAINT ck_board_post_age_range CHECK (age_min IS NULL OR age_max IS NULL OR age_min <= age_max),
    CONSTRAINT ck_board_post_weekly_hours CHECK (weekly_hours IS NULL OR weekly_hours BETWEEN 1 AND 60),
    CONSTRAINT ck_board_post_duration CHECK (duration_months IS NULL OR duration_months BETWEEN 1 AND 36)
);

ALTER SEQUENCE board_post_number_seq OWNED BY board_post.number;

-- al massimo un post attivo (bozza o pubblicato) per mansione
CREATE UNIQUE INDEX uq_board_post_active_job_slot ON board_post (job_slot_id) WHERE status IN ('DRAFT', 'PUBLISHED');
CREATE INDEX ix_board_post_project ON board_post (project_id);
CREATE INDEX ix_board_post_status ON board_post (status);

-- Storico dei cambi di stato: created_at/created_by sono istante e autore della transizione.
CREATE TABLE ticket_status_history (
    id           bigint       NOT NULL,
    ticket_id    bigint       NOT NULL,
    from_status  varchar(40),
    to_status    varchar(40)  NOT NULL,
    note         text,
    created_at   timestamptz  NOT NULL,
    created_by   varchar(100) NOT NULL,
    updated_at   timestamptz  NOT NULL,
    updated_by   varchar(100) NOT NULL,
    CONSTRAINT pk_ticket_status_history PRIMARY KEY (id),
    CONSTRAINT fk_ticket_status_history_ticket FOREIGN KEY (ticket_id) REFERENCES ticket (id),
    CONSTRAINT ck_ticket_status_history_from CHECK (from_status IN (
        'NUOVA', 'IN_ATTESA_APPROVAZIONE_ADMIN', 'IN_LAVORAZIONE', 'PROPOSTA_AZIENDA', 'PROPOSTA_ACCOLTA',
        'APPUNTAMENTO', 'IN_TIROCINIO', 'FORM_RESTITUZIONE', 'RIAPERTO')),
    CONSTRAINT ck_ticket_status_history_to CHECK (to_status IN (
        'NUOVA', 'IN_ATTESA_APPROVAZIONE_ADMIN', 'IN_LAVORAZIONE', 'PROPOSTA_AZIENDA', 'PROPOSTA_ACCOLTA',
        'APPUNTAMENTO', 'IN_TIROCINIO', 'FORM_RESTITUZIONE', 'RIAPERTO'))
);

CREATE INDEX ix_ticket_status_history_ticket ON ticket_status_history (ticket_id, created_at);

-- Aziende escluse per il ticket (non riproporre): ciascuna al massimo una volta.
CREATE TABLE ticket_company_blacklist (
    ticket_id   bigint       NOT NULL,
    company_id  bigint       NOT NULL,
    reason      varchar(500),
    created_at  timestamptz  NOT NULL,
    created_by  varchar(100) NOT NULL,
    CONSTRAINT pk_ticket_company_blacklist PRIMARY KEY (ticket_id, company_id),
    CONSTRAINT fk_ticket_company_blacklist_ticket FOREIGN KEY (ticket_id) REFERENCES ticket (id),
    CONSTRAINT fk_ticket_company_blacklist_company FOREIGN KEY (company_id) REFERENCES company (id)
);

CREATE INDEX ix_ticket_company_blacklist_company ON ticket_company_blacklist (company_id);

-- FK cicliche
ALTER TABLE ticket
    ADD CONSTRAINT fk_ticket_board_post FOREIGN KEY (board_post_id) REFERENCES board_post (id);
ALTER TABLE job_slot
    ADD CONSTRAINT fk_job_slot_blocked_by_ticket FOREIGN KEY (blocked_by_ticket_id) REFERENCES ticket (id);

CREATE INDEX ix_ticket_board_post ON ticket (board_post_id);
