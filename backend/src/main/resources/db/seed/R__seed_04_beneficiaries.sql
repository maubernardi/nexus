-- Dati DIMOSTRATIVI per lo sviluppo locale (profilo `seed`, nel gruppo `local`): mai in test o produzione.
-- Script ripetibile e idempotente: ON CONFLICT DO NOTHING (riesecuzioni non duplicano né sovrascrivono).
-- Beneficiari PALESEMENTE FITTIZI (nomi "Beneficiario/a Demo …", vincoli marcati "Dato fittizio") e lingue conosciute.

INSERT INTO beneficiary (id, owner_tutor_id, first_name, last_name, birth_year, gender, nationality, citizenship, residence_zone_id,
                       has_driving_license, license_types, has_vehicle, transport_mode, has_law68, education_level, constraints,
                       created_at, created_by, updated_at, updated_by) VALUES
    -- k1
    (892398395572130071, (SELECT id FROM app_user WHERE username = 'tutor1'), 'Beneficiario', 'Demo Uno', 1998, 'M', 'IT', 'IT', (SELECT id FROM zone WHERE code = 'ZONA_NORD'),
     true, '{B}', true, 'AUTO_PROPRIA', false, 'DIPLOMA', NULL,
     now(), 'seed', now(), 'seed'),
    -- k2
    (892398395572130072, (SELECT id FROM app_user WHERE username = 'tutor1'), 'Beneficiaria', 'Demo Due', 2001, 'F', 'IT', 'IT', (SELECT id FROM zone WHERE code = 'CENTRO'),
     false, '{}', false, 'MEZZI_PUBBLICI', true, 'LICENZA_MEDIA', 'Dato fittizio: necessita di postazione senza barriere architettoniche.',
     now(), 'seed', now(), 'seed'),
    -- k3
    (892398395572130073, (SELECT id FROM app_user WHERE username = 'tutor1'), 'Beneficiario', 'Demo Tre', 1990, 'ALTRO', 'MA', 'IT', (SELECT id FROM zone WHERE code = 'ZONA_EST'),
     true, '{AM,B}', false, 'BICICLETTA', false, 'QUALIFICA_PROFESSIONALE', NULL,
     now(), 'seed', now(), 'seed'),
    -- k4
    (892398395572130074, (SELECT id FROM app_user WHERE username = 'tutor2'), 'Beneficiaria', 'Demo Quattro', 1985, 'F', 'RO', 'RO', (SELECT id FROM zone WHERE code = 'ZONA_SUD'),
     false, '{}', false, 'A_PIEDI', false, 'DIPLOMA', NULL,
     now(), 'seed', now(), 'seed'),
    -- k5
    (892398395572130075, (SELECT id FROM app_user WHERE username = 'tutor2'), 'Beneficiario', 'Demo Cinque', 2003, 'NON_DICHIARATO', 'IT', 'IT', (SELECT id FROM zone WHERE code = 'ZONA_OVEST'),
     true, '{B,C}', true, 'AUTO_PROPRIA', false, 'ITS', 'Dato fittizio: allergia alle polveri.',
     now(), 'seed', now(), 'seed'),
    -- k6 (creato anche da V9 sui database con i dati demo precedenti: stessi valori)
    (892398395576324400, (SELECT id FROM app_user WHERE username = 'tutor1'), 'Beneficiaria', 'Demo Sei', 1994, 'F', 'IT', 'IT', (SELECT id FROM zone WHERE code = 'CENTRO'),
     true, '{B}', false, 'MEZZI_PUBBLICI', false, 'DIPLOMA', NULL,
     now(), 'seed', now(), 'seed'),
    -- k7 (come k6)
    (892398395576324401, (SELECT id FROM app_user WHERE username = 'tutor2'), 'Beneficiario', 'Demo Sette', 1988, 'M', 'IT', 'IT', (SELECT id FROM zone WHERE code = 'ZONA_OVEST'),
     false, '{}', false, 'BICICLETTA', false, 'LICENZA_MEDIA', NULL,
     now(), 'seed', now(), 'seed')
ON CONFLICT DO NOTHING;

INSERT INTO beneficiary_language (id, beneficiary_id, language, level, created_at, created_by, updated_at, updated_by) VALUES
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

-- Database demo creati prima del cambio di terminologia (candidato → beneficiario): nomi fittizi allineati.
UPDATE beneficiary SET first_name = CASE first_name WHEN 'Candidato' THEN 'Beneficiario' ELSE 'Beneficiaria' END,
                       updated_at = now(), updated_by = 'seed'
 WHERE created_by = 'seed' AND first_name IN ('Candidato', 'Candidata');
