-- Affidabilità e interesse al progetto dell'azienda (US-514, PO 25/11). Nel database attuale il Call Center li scriveva
-- all'inizio del nome: ogni $ è una stella di affidabilità, ogni £ un pollice di interesse; & e ^ iniziali sono refusi.
-- Le aziende importate (US-1309) li hanno ancora nel nome: qui passano nei campi nuovi, senza perdere il dato, e il nome
-- si ripulisce. La & dentro il nome resta («Rossi & C.»). La stessa regola è in it.nexus.legacy.LegacyCompanyMarks, per
-- le importazioni future: i due test usano gli stessi esempi.

ALTER TABLE company
    ADD COLUMN reliability      smallint,
    ADD COLUMN project_interest smallint,
    ADD CONSTRAINT ck_company_reliability CHECK (reliability BETWEEN 1 AND 4),
    ADD CONSTRAINT ck_company_project_interest CHECK (project_interest BETWEEN 1 AND 2);

-- Simboli in qualunque ordine, anche separati da spazi: si contano $ e £ (cinque $ valgono 4 stelle, al massimo due
-- pollici) e si toglie tutto il prefisso. Le valutazioni già scelte non cambiano. Un nome fatto solo di simboli diventa
-- il segnaposto dei nomi da completare (US-508), con il numero di Access per le aziende importate (id = base + numero,
-- LegacyIds.COMPANY). Ogni azienda convertita ha la sua voce nell'audit, con il nome di prima: il dato resta tracciato.
-- Gli id dell'audit stanno nell'intervallo riservato LegacyIds.MARKS_AUDIT, sotto gli id generati oggi.
WITH marked AS (
    SELECT c.id,
           c.name AS old_name,
           c.reliability AS old_reliability,
           c.project_interest AS old_project_interest,
           length(p.prefix) - length(replace(p.prefix, '$', '')) AS dollars,
           length(p.prefix) - length(replace(p.prefix, '£', '')) AS pounds,
           btrim(substr(c.name, length(p.prefix) + 1)) AS rest
    FROM company c
    CROSS JOIN LATERAL (SELECT substring(c.name FROM '^[[:space:]$£&^]+') AS prefix) p
    WHERE p.prefix ~ '[$£&^]'
), converted AS (
    UPDATE company c
    SET name = CASE
                   WHEN m.rest <> '' THEN m.rest
                   WHEN c.id BETWEEN 893700000010000001 AND 893700000019999999
                       THEN 'Azienda senza nome n. ' || (c.id - 893700000010000000)
                   ELSE 'Azienda senza nome n. ' || c.id
               END,
        reliability = coalesce(c.reliability, nullif(least(m.dollars, 4), 0)),
        project_interest = coalesce(c.project_interest, nullif(least(m.pounds, 2), 0)),
        version = c.version + 1,
        updated_at = now(),
        updated_by = 'database-attuale'
    FROM marked m
    WHERE c.id = m.id
    RETURNING c.id, m.old_name, c.name AS new_name, m.old_reliability, c.reliability AS new_reliability,
              m.old_project_interest, c.project_interest AS new_project_interest
)
INSERT INTO audit_event (id, entity_type, entity_id, action, changes, created_at, created_by)
SELECT 893700000060000000 + row_number() OVER (ORDER BY id),
       'COMPANY',
       id,
       'LEGACY_MARKS',
       jsonb_build_object('name', jsonb_build_object('before', old_name, 'after', new_name))
           || CASE WHEN new_reliability IS DISTINCT FROM old_reliability
                   THEN jsonb_build_object('reliability',
                                           jsonb_build_object('before', old_reliability, 'after', new_reliability))
                   ELSE '{}'::jsonb END
           || CASE WHEN new_project_interest IS DISTINCT FROM old_project_interest
                   THEN jsonb_build_object('projectInterest',
                                           jsonb_build_object('before', old_project_interest,
                                                              'after', new_project_interest))
                   ELSE '{}'::jsonb END,
       now(),
       'database-attuale'
FROM converted;

-- D'ora in poi il nome non può iniziare con i simboli: affidabilità e interesse si segnano con stelle e pollici
ALTER TABLE company ADD CONSTRAINT ck_company_name_legacy_marks CHECK (name !~ '^[[:space:]]*[$£&^]');
