-- Dati DIMOSTRATIVI per lo sviluppo locale (profilo `seed`, nel gruppo `local`): mai in test o produzione.
-- Script ripetibile e idempotente: ON CONFLICT DO NOTHING (riesecuzioni non duplicano né sovrascrivono).
-- Utenti: external_id = id del realm Keycloak di sviluppo e degli utenti mock. Il primo ADMIN nasce qui (bootstrap).
-- Assegnazioni: tutor1 -> GOL; tutor2 -> GOL + POLIS (visibilità incrociata provabile). CC e ADMIN hanno visibilità globale.

INSERT INTO app_user (id, external_id, username, first_name, last_name, email, phone, role, active, created_at, created_by, updated_at, updated_by) VALUES
    (892398395572130057, '00000000-0000-4000-8000-000000000001', 'tutor1', 'Tutor', 'Uno', 'tutor1@nexus.local', '+39 000 0000001', 'TUTOR', true, now(), 'seed', now(), 'seed'),
    (892398395572130058, '00000000-0000-4000-8000-000000000002', 'tutor2', 'Tutor', 'Due', 'tutor2@nexus.local', '+39 000 0000002', 'TUTOR', true, now(), 'seed', now(), 'seed'),
    (892398395572130059, '00000000-0000-4000-8000-000000000003', 'operatore.cc', 'Operatore', 'Call Center', 'operatore.cc@nexus.local', '+39 000 0000003', 'CALL_CENTER', true, now(), 'seed', now(), 'seed'),
    (892398395572130060, '00000000-0000-4000-8000-000000000004', 'admin', 'Amministratore', 'Nexus', 'admin@nexus.local', NULL, 'ADMIN', true, now(), 'seed', now(), 'seed')
ON CONFLICT DO NOTHING;

INSERT INTO user_project (user_id, project_id, created_at, created_by) VALUES
    ((SELECT id FROM app_user WHERE username = 'tutor1'), (SELECT id FROM project WHERE code = 'GOL'), now(), 'seed'),
    ((SELECT id FROM app_user WHERE username = 'tutor2'), (SELECT id FROM project WHERE code = 'GOL'), now(), 'seed'),
    ((SELECT id FROM app_user WHERE username = 'tutor2'), (SELECT id FROM project WHERE code = 'POLIS'), now(), 'seed')
ON CONFLICT DO NOTHING;
