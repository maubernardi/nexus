#!/bin/sh
# Primo avvio del volume PostgreSQL (docker-entrypoint-initdb.d): utenti e database di NEXUS e Keycloak.
#  - nexus_owner: proprietario dello schema `nexus`, usato solo da Flyway (migrazioni e seed)
#  - nexus_app:   utente dell'applicazione, solo DML: non può alterare lo schema né i trigger (audit trail)
#  - keycloak:    proprietario del database di Keycloak
# Le password arrivano dall'ambiente (file .env sul server) e sono passate a psql come variabili quotate.
set -eu

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres \
  -v owner_pw="$NEXUS_OWNER_PASSWORD" -v app_pw="$NEXUS_APP_PASSWORD" -v kc_pw="$KEYCLOAK_DB_PASSWORD" <<'SQL'
CREATE ROLE nexus_owner LOGIN PASSWORD :'owner_pw';
CREATE ROLE nexus_app LOGIN PASSWORD :'app_pw';
CREATE ROLE keycloak LOGIN PASSWORD :'kc_pw';
CREATE DATABASE nexus OWNER nexus_owner;
CREATE DATABASE keycloak OWNER keycloak;
REVOKE ALL ON DATABASE nexus FROM PUBLIC;
REVOKE ALL ON DATABASE keycloak FROM PUBLIC;
GRANT CONNECT ON DATABASE nexus TO nexus_app;
SQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname nexus <<'SQL'
-- schema creato qui (e non da Flyway) per poter concedere subito l'uso all'utente applicativo
CREATE SCHEMA nexus AUTHORIZATION nexus_owner;
GRANT USAGE ON SCHEMA nexus TO nexus_app;
-- tabelle e sequenze che Flyway creerà come nexus_owner: all'app solo lettura/scrittura dei dati
ALTER DEFAULT PRIVILEGES FOR ROLE nexus_owner IN SCHEMA nexus
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO nexus_app;
ALTER DEFAULT PRIVILEGES FOR ROLE nexus_owner IN SCHEMA nexus
    GRANT USAGE, SELECT ON SEQUENCES TO nexus_app;
SQL
