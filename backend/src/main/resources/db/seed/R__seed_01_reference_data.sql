-- Dati DIMOSTRATIVI per lo sviluppo locale (profilo `seed`, nel gruppo `local`): mai in test o produzione.
-- Script ripetibile e idempotente: ON CONFLICT DO NOTHING (riesecuzioni non duplicano né sovrascrivono).
-- Progetti, zone e tipologie di mansione. Zone e tipologie sono SEGNAPOSTO, da sostituire con i valori reali.

INSERT INTO project (id, code, name, active, created_at, created_by, updated_at, updated_by) VALUES
    (892398395572130042, 'GOL', 'GOL - Garanzia Occupabilità dei Lavoratori', true, now(), 'seed', now(), 'seed'),
    (892398395572130043, 'POLIS', 'POLIS - Progetto di inclusione sociale', true, now(), 'seed', now(), 'seed')
ON CONFLICT DO NOTHING;

INSERT INTO zone (id, code, name, active, created_at, created_by, updated_at, updated_by) VALUES
    (892398395572130044, 'ZONA_NORD', 'Zona Nord', true, now(), 'seed', now(), 'seed'),
    (892398395572130045, 'ZONA_SUD', 'Zona Sud', true, now(), 'seed', now(), 'seed'),
    (892398395572130046, 'ZONA_EST', 'Zona Est', true, now(), 'seed', now(), 'seed'),
    (892398395572130047, 'ZONA_OVEST', 'Zona Ovest', true, now(), 'seed', now(), 'seed'),
    (892398395572130048, 'CENTRO', 'Centro città', true, now(), 'seed', now(), 'seed')
ON CONFLICT DO NOTHING;

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
