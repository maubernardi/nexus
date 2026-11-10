-- Dati DIMOSTRATIVI per lo sviluppo locale (profilo `seed`, nel gruppo `local`): mai in test o produzione.
-- Script ripetibile e idempotente: ON CONFLICT DO NOTHING (riesecuzioni non duplicano né sovrascrivono).
-- Progetti e tipologie di mansione (SEGNAPOSTO, da sostituire con i valori reali); le zone reali sono in V11.

INSERT INTO project (id, code, name, active, created_at, created_by, updated_at, updated_by) VALUES
    (892398395572130042, 'GOL', 'GOL - Garanzia Occupabilità dei Lavoratori', true, now(), 'seed', now(), 'seed'),
    (892398395572130043, 'POLIS', 'POLIS - Progetto di inclusione sociale', true, now(), 'seed', now(), 'seed')
ON CONFLICT DO NOTHING;

-- Zone: quelle reali arrivano dalla migrazione V11 (Firenze e hinterland). Le zone segnaposto dei database demo creati
-- prima vengono disattivate e i dati demo spostati su zone reali (idempotente).
WITH remap(old_code, new_code) AS (VALUES
    ('ZONA_NORD', 'RIFREDI'),
    ('ZONA_SUD', 'GALLUZZO'),
    ('ZONA_EST', 'CAMPO_DI_MARTE'),
    ('ZONA_OVEST', 'ISOLOTTO'),
    ('CENTRO', 'SAN_LORENZO')
)
UPDATE beneficiary b SET residence_zone_id = (SELECT id FROM zone WHERE code = r.new_code), updated_at = now(), updated_by = 'seed'
  FROM remap r, zone z
 WHERE z.code = r.old_code AND b.residence_zone_id = z.id;

WITH remap(old_code, new_code) AS (VALUES
    ('ZONA_NORD', 'RIFREDI'),
    ('ZONA_SUD', 'GALLUZZO'),
    ('ZONA_EST', 'CAMPO_DI_MARTE'),
    ('ZONA_OVEST', 'ISOLOTTO'),
    ('CENTRO', 'SAN_LORENZO')
)
UPDATE job_slot j SET zone_id = (SELECT id FROM zone WHERE code = r.new_code), updated_at = now(), updated_by = 'seed'
  FROM remap r, zone z
 WHERE z.code = r.old_code AND j.zone_id = z.id;

UPDATE zone SET active = false, updated_at = now(), updated_by = 'seed'
 WHERE code IN ('ZONA_NORD', 'ZONA_SUD', 'ZONA_EST', 'ZONA_OVEST', 'CENTRO') AND active;

INSERT INTO job_category (id, code, name, active, created_at, created_by, updated_at, updated_by) VALUES
    (892398395572130049, 'MAGAZZINIERE', 'Magazziniere', true, now(), 'seed', now(), 'seed'),
    (892398395572130050, 'ADDETTO_PULIZIE', 'Addetto alle pulizie', true, now(), 'seed', now(), 'seed'),
    (892398395572130051, 'AIUTO_CUOCO', 'Aiuto cuoco', true, now(), 'seed', now(), 'seed'),
    (892398395572130052, 'ADDETTO_VENDITE', 'Addetto alle vendite', true, now(), 'seed', now(), 'seed'),
    (892398395572130053, 'ADDETTO_SEGRETERIA', 'Addetto di segreteria', true, now(), 'seed', now(), 'seed'),
    (892398395572130054, 'GIARDINIERE', 'Giardiniere', true, now(), 'seed', now(), 'seed'),
    (892398395572130055, 'CAMERIERE', 'Cameriere di sala', true, now(), 'seed', now(), 'seed'),
    (892398395572130056, 'OPERAIO_GENERICO', 'Operaio generico', true, now(), 'seed', now(), 'seed')
ON CONFLICT DO NOTHING;

-- Cataloghi dei documenti (US-115): sedi e tipi ammessi di GOL anche su un database nuovo (la V34 li collega solo se
-- GOL esiste già); dati inventati degli enti di presa in carico, solo nella demo.
INSERT INTO competence_site_project (competence_site_id, project_id)
SELECT s.id, p.id FROM competence_site s CROSS JOIN project p
WHERE p.code = 'GOL' AND s.code IN ('FIRENZE', 'BORGO_SAN_LORENZO', 'SESTO_FIORENTINO')
ON CONFLICT DO NOTHING;

INSERT INTO project_internship_type (project_id, internship_type_id)
SELECT p.id, t.id FROM project p CROSS JOIN internship_type t
WHERE p.code = 'GOL' AND t.code IN ('EXTRACURRICULARE', 'INCLUSIONE')
ON CONFLICT DO NOTHING;

UPDATE care_agency SET fiscal_code = '00000000001', legal_address = 'Piazza della Prova 1, 50100 Firenze',
    representative_name = 'Anna Esempio', representative_role = 'Dirigente del servizio sociale',
    referent_name = 'Anna Esempio', referent_phone = '055 0000001', referent_email = 'referente.comune@esempio.it'
WHERE code = 'COMUNE_DI_FIRENZE' AND fiscal_code IS NULL;
UPDATE care_agency SET fiscal_code = '00000000002', legal_address = 'Via della Prova 2, 50100 Firenze',
    representative_name = 'Bruno Esempio', representative_role = 'Direttore', representative_birth_place = 'Firenze',
    representative_birth_date = '1965-04-13', referent_name = 'Bruno Esempio', referent_phone = '055 0000002',
    referent_email = 'referente.sdsfi@esempio.it'
WHERE code = 'SOCIETA_DELLA_SALUTE_DI_FIRENZE' AND fiscal_code IS NULL;
UPDATE care_agency SET fiscal_code = '00000000003', legal_address = 'Via della Prova 3, 50032 Borgo San Lorenzo',
    representative_name = 'Carla Esempio', representative_role = 'Direttrice', referent_name = 'Carla Esempio',
    referent_phone = '055 0000003', referent_email = 'referente.mugello@esempio.it'
WHERE code = 'SOCIETA_DELLA_SALUTE_MUGELLO' AND fiscal_code IS NULL;
UPDATE care_agency SET fiscal_code = '00000000004', legal_address = 'Via della Prova 4, 50019 Sesto Fiorentino',
    representative_name = 'Dario Esempio', representative_role = 'Direttore', representative_birth_place = 'Prato',
    representative_birth_date = '1968-05-18', referent_name = 'Elena Esempio', referent_phone = '055 0000004',
    referent_email = 'referente.nordovest@esempio.it'
WHERE code = 'SOCIETA_DELLA_SALUTE_FIORENTINA_NORD_OVEST' AND fiscal_code IS NULL;
