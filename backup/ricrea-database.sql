-- NEXUS — ricrea vuoti i database nexus e keycloak prima di un ripristino (US-1302, README «Backup del database»).
-- Come superutente, con backend e Keycloak fermi. I ruoli esistono già: li crea lo script del primo avvio
-- (infra/deploy/postgres/init/10-create-roles.sh). I permessi sui database non sono nel backup (pg_dump senza
-- --create): sono qui, uguali a quelli dello script del primo avvio. Schema, tabelle e permessi arrivano dal backup.
\set ON_ERROR_STOP on
DROP DATABASE IF EXISTS nexus WITH (FORCE);
DROP DATABASE IF EXISTS keycloak WITH (FORCE);
CREATE DATABASE nexus OWNER nexus_owner;
CREATE DATABASE keycloak OWNER keycloak;
REVOKE ALL ON DATABASE nexus FROM PUBLIC;
REVOKE ALL ON DATABASE keycloak FROM PUBLIC;
GRANT CONNECT ON DATABASE nexus TO nexus_app;
