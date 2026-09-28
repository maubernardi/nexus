-- Dati DIMOSTRATIVI per lo sviluppo locale (profilo `seed`, nel gruppo `local`): mai in test o produzione.
-- Script ripetibile e idempotente: ON CONFLICT DO NOTHING (riesecuzioni non duplicano né sovrascrivono).
-- Candidati PALESEMENTE FITTIZI (nomi "Candidato/a Demo …", vincoli marcati "Dato fittizio") e lingue conosciute.

INSERT INTO candidate (id, owner_tutor_id, first_name, last_name, birth_year, gender, nationality, citizenship, residence_zone_id,
                       has_driving_license, license_types, has_vehicle, transport_mode, has_law68, education_level, constraints,
                       created_at, created_by, updated_at, updated_by) VALUES
    -- k1
    (892398395572130071, (SELECT id FROM app_user WHERE username = 'tutor1'), 'Candidato', 'Demo Uno', 1998, 'M', 'IT', 'IT', (SELECT id FROM zone WHERE code = 'ZONA_NORD'),
     true, '{B}', true, 'AUTO_PROPRIA', false, 'DIPLOMA', NULL,
     now(), 'seed', now(), 'seed'),
    -- k2
    (892398395572130072, (SELECT id FROM app_user WHERE username = 'tutor1'), 'Candidata', 'Demo Due', 2001, 'F', 'IT', 'IT', (SELECT id FROM zone WHERE code = 'CENTRO'),
     false, '{}', false, 'MEZZI_PUBBLICI', true, 'LICENZA_MEDIA', 'Dato fittizio: necessita di postazione senza barriere architettoniche.',
     now(), 'seed', now(), 'seed'),
    -- k3
    (892398395572130073, (SELECT id FROM app_user WHERE username = 'tutor1'), 'Candidato', 'Demo Tre', 1990, 'ALTRO', 'MA', 'IT', (SELECT id FROM zone WHERE code = 'ZONA_EST'),
     true, '{AM,B}', false, 'BICICLETTA', false, 'QUALIFICA_PROFESSIONALE', NULL,
     now(), 'seed', now(), 'seed'),
    -- k4
    (892398395572130074, (SELECT id FROM app_user WHERE username = 'tutor2'), 'Candidata', 'Demo Quattro', 1985, 'F', 'RO', 'RO', (SELECT id FROM zone WHERE code = 'ZONA_SUD'),
     false, '{}', false, 'A_PIEDI', false, 'DIPLOMA', NULL,
     now(), 'seed', now(), 'seed'),
    -- k5
    (892398395572130075, (SELECT id FROM app_user WHERE username = 'tutor2'), 'Candidato', 'Demo Cinque', 2003, 'NON_DICHIARATO', 'IT', 'IT', (SELECT id FROM zone WHERE code = 'ZONA_OVEST'),
     true, '{B,C}', true, 'AUTO_PROPRIA', false, 'ITS', 'Dato fittizio: allergia alle polveri.',
     now(), 'seed', now(), 'seed')
ON CONFLICT DO NOTHING;

INSERT INTO candidate_language (id, candidate_id, language, level, created_at, created_by, updated_at, updated_by) VALUES
    (892398395572130076, 892398395572130071, 'it', 'MADRELINGUA', now(), 'seed', now(), 'seed'),
    (892398395572130077, 892398395572130071, 'en', 'B1', now(), 'seed', now(), 'seed'),
    (892398395572130078, 892398395572130072, 'it', 'MADRELINGUA', now(), 'seed', now(), 'seed'),
    (892398395572130079, 892398395572130073, 'ar', 'MADRELINGUA', now(), 'seed', now(), 'seed'),
    (892398395572130080, 892398395572130073, 'it', 'B2', now(), 'seed', now(), 'seed'),
    (892398395572130081, 892398395572130073, 'fr', 'A2', now(), 'seed', now(), 'seed'),
    (892398395572130082, 892398395572130074, 'ro', 'MADRELINGUA', now(), 'seed', now(), 'seed'),
    (892398395572130083, 892398395572130074, 'it', 'C1', now(), 'seed', now(), 'seed'),
    (892398395572130084, 892398395572130075, 'it', 'MADRELINGUA', now(), 'seed', now(), 'seed'),
    (892398395572130085, 892398395572130075, 'en', 'A2', now(), 'seed', now(), 'seed')
ON CONFLICT DO NOTHING;
