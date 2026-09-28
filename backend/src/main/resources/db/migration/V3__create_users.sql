-- Utenti di NEXUS e assegnazione ai progetti.
-- external_id = soggetto (sub) dell'identity provider. Il ruolo è una copia di quello Keycloak,
-- riallineata a ogni richiesta. Gli utenti si disattivano, non si cancellano.

CREATE TABLE app_user (
    id           bigint       NOT NULL,
    external_id  varchar(100) NOT NULL,
    username     varchar(100) NOT NULL,
    first_name   varchar(100) NOT NULL,
    last_name    varchar(100) NOT NULL,
    email        varchar(254) NOT NULL,
    phone        varchar(30),
    role         varchar(20)  NOT NULL,
    active       boolean      NOT NULL DEFAULT true,
    created_at   timestamptz  NOT NULL,
    created_by   varchar(100) NOT NULL,
    updated_at   timestamptz  NOT NULL,
    updated_by   varchar(100) NOT NULL,
    CONSTRAINT pk_app_user PRIMARY KEY (id),
    CONSTRAINT uq_app_user_external_id UNIQUE (external_id),
    CONSTRAINT uq_app_user_username UNIQUE (username),
    CONSTRAINT ck_app_user_role CHECK (role IN ('TUTOR', 'CALL_CENTER', 'ADMIN'))
);

-- Email unica senza distinzione tra maiuscole e minuscole.
CREATE UNIQUE INDEX uq_app_user_email_lower ON app_user (lower(email));

CREATE TABLE user_project (
    user_id     bigint       NOT NULL,
    project_id  bigint       NOT NULL,
    created_at  timestamptz  NOT NULL,
    created_by  varchar(100) NOT NULL,
    CONSTRAINT pk_user_project PRIMARY KEY (user_id, project_id),
    CONSTRAINT fk_user_project_user FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT fk_user_project_project FOREIGN KEY (project_id) REFERENCES project (id)
);

CREATE INDEX ix_user_project_project ON user_project (project_id);
