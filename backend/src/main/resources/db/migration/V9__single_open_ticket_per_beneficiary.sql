-- Un beneficiario può avere una sola segnalazione aperta alla volta (US-301). "Aperta" = qualunque stato tranne
-- FORM_RESTITUZIONE, la fase conclusiva del percorso. Il vincolo nel database protegge anche dalle richieste concorrenti.

-- Riallineamento dei soli dati DIMOSTRATIVI (profilo `seed`): nello scenario demo t6 e t7 erano su beneficiari che hanno
-- già un'altra segnalazione aperta. Se quei ticket esistono, passano a due nuovi beneficiari fittizi; in un database
-- senza dati demo (test, produzione) le istruzioni non trovano righe e non fanno nulla. R__seed_04/05 sono allineati.
INSERT INTO beneficiary (id, owner_tutor_id, first_name, last_name, birth_year, gender, nationality, citizenship,
                         residence_zone_id, has_driving_license, license_types, has_vehicle, transport_mode, has_law68,
                         education_level, constraints, created_at, created_by, updated_at, updated_by)
SELECT 892398395576324400, (SELECT id FROM app_user WHERE username = 'tutor1'), 'Beneficiaria', 'Demo Sei', 1994, 'F', 'IT',
       'IT', (SELECT id FROM zone WHERE code = 'CENTRO'), true, '{B}', false, 'MEZZI_PUBBLICI', false, 'DIPLOMA', NULL,
       now(), 'seed', now(), 'seed'
WHERE EXISTS (SELECT 1 FROM ticket WHERE id = 892398395576324284 AND created_by = 'seed')
ON CONFLICT DO NOTHING;

INSERT INTO beneficiary (id, owner_tutor_id, first_name, last_name, birth_year, gender, nationality, citizenship,
                         residence_zone_id, has_driving_license, license_types, has_vehicle, transport_mode, has_law68,
                         education_level, constraints, created_at, created_by, updated_at, updated_by)
SELECT 892398395576324401, (SELECT id FROM app_user WHERE username = 'tutor2'), 'Beneficiario', 'Demo Sette', 1988, 'M', 'IT',
       'IT', (SELECT id FROM zone WHERE code = 'ZONA_OVEST'), false, '{}', false, 'BICICLETTA', false, 'LICENZA_MEDIA', NULL,
       now(), 'seed', now(), 'seed'
WHERE EXISTS (SELECT 1 FROM ticket WHERE id = 892398395572130094 AND created_by = 'seed')
ON CONFLICT DO NOTHING;

UPDATE ticket SET beneficiary_id = 892398395576324400
 WHERE id = 892398395576324284 AND created_by = 'seed' AND beneficiary_id <> 892398395576324400;  -- t7
UPDATE ticket SET beneficiary_id = 892398395576324401
 WHERE id = 892398395572130094 AND created_by = 'seed' AND beneficiary_id <> 892398395576324401;  -- t6

CREATE UNIQUE INDEX uq_ticket_open_per_beneficiary ON ticket (beneficiary_id) WHERE status <> 'FORM_RESTITUZIONE';
