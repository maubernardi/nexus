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
