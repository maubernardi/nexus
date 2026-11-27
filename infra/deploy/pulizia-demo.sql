-- NEXUS — pulizia della demo per il passaggio in produzione (US-1306). Decisione del PO del 20/11: si riporta tutto
-- dalla demo tranne segnalazioni, utenti e beneficiari; le aziende vengono dal database attuale (importazione di
-- US-1309, subito dopo questo script), quindi quelle della demo si tolgono. Restano configurazione e cataloghi.
--
-- Una volta sola, con il backend fermo, come proprietario dello schema (nexus_owner), in una sola transazione: se un
-- passo o un controllo non va, non cambia nulla. Le regole di sola aggiunta (EN-2: audit, storico e diario delle
-- aziende, report di chiusura) sono sospese solo dentro la transazione, ed è l'unica volta, prima dei dati veri.
-- Lo lancia passaggio-in-produzione.sh; a mano:
--   docker compose … exec -T postgres psql -v ON_ERROR_STOP=1 -U nexus_owner -d nexus <pulizia-demo.sql
-- Stampa solo numeri e codici della configurazione: nessun dato personale.
\set ON_ERROR_STOP on
\pset footer off
BEGIN;
SET LOCAL search_path = nexus;

-- regole di sola aggiunta sospese fino alla fine della transazione (i trigger dei vincoli restano)
ALTER TABLE audit_event DISABLE TRIGGER USER;
ALTER TABLE company_audit_event DISABLE TRIGGER USER;
ALTER TABLE company_note DISABLE TRIGGER USER;
ALTER TABLE closure_report DISABLE TRIGGER USER;

-- Segnalazioni (con bacheca, schede del tirocinio, report, storico, notifiche), beneficiari, utenti e aziende della
-- demo (con referenti, diario, storico e mansioni), e le richieste di contatto con i tentativi (US-507, US-512). TRUNCATE senza CASCADE: se una tabella che punta a queste mancasse
-- dall'elenco, PostgreSQL si fermerebbe invece di svuotarla di nascosto.
TRUNCATE contact_request_attempt, contact_request, closure_report, internship_training_project, internship_extension, internship_schedule_day,
    internship_suspension, internship_company_tutor, internship, notification, ticket_status_history,
    ticket_company_blacklist, company_audit_event, board_post, ticket, job_slot, company_contact, company_note,
    company_legal_representative, company_ateco_code, legacy_call, company, beneficiary_language, beneficiary,
    email_change_request, user_project, app_user;

-- curriculum dei beneficiari: i file che nessun modello dei documenti usa (il contenuto se ne va in cascata)
DELETE FROM stored_file f WHERE NOT EXISTS (SELECT 1 FROM document_template t WHERE t.file_id = f.id);

-- audit: via la storia di quello che si è tolto; resta quella della configurazione (PO, 20/11)
DELETE FROM audit_event
WHERE entity_type IN ('TICKET', 'BENEFICIARY', 'APP_USER', 'COMPANY', 'JOB_SLOT', 'BOARD_POST', 'CONTACT_REQUEST');

-- Flyway: via gli script dimostrativi (R__seed_*), che in produzione non ci sono; con le loro righe nella cronologia
-- il backend senza il profilo seed non partirebbe
DELETE FROM flyway_schema_history WHERE version IS NULL AND script LIKE 'R\_\_seed\_%';

ALTER TABLE audit_event ENABLE TRIGGER USER;
ALTER TABLE company_audit_event ENABLE TRIGGER USER;
ALTER TABLE company_note ENABLE TRIGGER USER;
ALTER TABLE closure_report ENABLE TRIGGER USER;

-- controlli: se uno non torna, la transazione si annulla e la demo resta com'era
DO $$
DECLARE
    t text;
    n bigint;
BEGIN
    FOREACH t IN ARRAY ARRAY['ticket', 'board_post', 'internship', 'closure_report', 'notification', 'beneficiary',
            'app_user', 'company', 'job_slot', 'company_contact', 'company_note', 'company_audit_event',
            'contact_request', 'contact_request_attempt'] LOOP
        EXECUTE format('SELECT count(*) FROM nexus.%I', t) INTO n;
        IF n <> 0 THEN
            RAISE EXCEPTION 'pulizia incompleta: % righe in %', n, t;
        END IF;
    END LOOP;
    IF EXISTS (SELECT 1 FROM nexus.audit_event
               WHERE entity_type IN ('TICKET', 'BENEFICIARY', 'APP_USER', 'COMPANY', 'JOB_SLOT', 'BOARD_POST',
                                     'CONTACT_REQUEST')) THEN
        RAISE EXCEPTION 'pulizia incompleta: audit di oggetti tolti';
    END IF;
    IF EXISTS (SELECT 1 FROM nexus.flyway_schema_history WHERE script LIKE 'R\_\_%') THEN
        RAISE EXCEPTION 'pulizia incompleta: script dimostrativi nella cronologia di Flyway';
    END IF;
    IF EXISTS (SELECT 1 FROM pg_trigger tg
               JOIN pg_class c ON c.oid = tg.tgrelid
               JOIN pg_namespace ns ON ns.oid = c.relnamespace
               WHERE ns.nspname = 'nexus' AND NOT tg.tgisinternal AND tg.tgenabled = 'D') THEN
        RAISE EXCEPTION 'regole di sola aggiunta ancora sospese';
    END IF;
END
$$;

COMMIT;

\echo
\echo 'Configurazione che resta (righe):'
SELECT 'comuni' AS voce, count(*) AS righe FROM nexus.municipality
UNION ALL SELECT 'progetti', count(*) FROM nexus.project
UNION ALL SELECT 'tipologie di mansione', count(*) FROM nexus.job_category
UNION ALL SELECT 'zone', count(*) FROM nexus.zone
UNION ALL SELECT 'sedi di competenza', count(*) FROM nexus.competence_site
UNION ALL SELECT 'enti', count(*) FROM nexus.care_agency
UNION ALL SELECT 'modelli dei documenti', count(*) FROM nexus.document_template
UNION ALL SELECT 'variabili dei progetti', count(*) FROM nexus.project_variable
UNION ALL SELECT 'categorie merceologiche', count(*) FROM nexus.business_category
UNION ALL SELECT 'ruoli dei referenti', count(*) FROM nexus.contact_role
UNION ALL SELECT 'voci dell''audit', count(*) FROM nexus.audit_event;

\echo
\echo 'Voci rimaste come le ha messe la demo (dati dimostrativi o segnaposto): da controllare dai Cataloghi'
SELECT 'progetto' AS catalogo, code AS codice FROM nexus.project WHERE created_by = 'seed' AND updated_by = 'seed'
UNION ALL
SELECT 'tipologia di mansione', code FROM nexus.job_category WHERE created_by = 'seed' AND updated_by = 'seed'
UNION ALL
SELECT 'ente, dati inventati', code FROM nexus.care_agency
WHERE fiscal_code IN ('00000000001', '00000000002') OR referent_email LIKE '%@esempio.it'
ORDER BY 1, 2;
