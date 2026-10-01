-- Dati DIMOSTRATIVI per lo sviluppo locale (profilo `seed`, nel gruppo `local`): mai in test o produzione.
-- Script ripetibile e idempotente: ON CONFLICT DO NOTHING (riesecuzioni non duplicano né sovrascrivono).
-- Aziende fittizie (P.IVA 99…, email su dominio .invalid) e mansioni; tutte LIBERE all'inserimento:
-- i blocchi da parte dei ticket sono applicati in R__seed_05.

INSERT INTO company (id, name, vat_code, legal_address, contact_person, phone, email, active, created_at, created_by, updated_at, updated_by) VALUES
    (892398395572130067, 'Demo Logistica S.r.l.', '99000000001', 'Via dell''Esempio 1, Città Demo', 'Referente Logistica', '+39 000 1000001', 'logistica@demo.invalid', true, now(), 'seed', now(), 'seed'),
    (892398395572130068, 'Demo Ristorazione S.n.c.', '99000000002', 'Piazza Fittizia 2, Città Demo', 'Referente Ristorazione', '+39 000 1000002', 'ristorazione@demo.invalid', true, now(), 'seed', now(), 'seed'),
    (892398395572130069, 'Demo Servizi Pulizia Soc. Coop.', '99000000003', 'Viale Prova 3, Città Demo', 'Referente Servizi', '+39 000 1000003', 'servizi@demo.invalid', true, now(), 'seed', now(), 'seed'),
    (892398395572130070, 'Demo Commercio al Dettaglio S.p.A.', '99000000004', 'Corso Simulato 4, Città Demo', 'Referente Negozio', '+39 000 1000004', 'negozio@demo.invalid', true, now(), 'seed', now(), 'seed')
ON CONFLICT DO NOTHING;

INSERT INTO job_slot (id, company_id, job_category_id, zone_id, title, description, status, created_at, created_by, updated_at, updated_by) VALUES
    -- s_mag_nord
    (892398395572130061, (SELECT id FROM company WHERE vat_code = '99000000001'), (SELECT id FROM job_category WHERE code = 'MAGAZZINIERE'), (SELECT id FROM zone WHERE code = 'RIFREDI'), 'Magazziniere reparto spedizioni', 'Mansione dimostrativa.', 'LIBERA', now(), 'seed', now(), 'seed'),
    -- s_mag_est
    (892398395572130062, (SELECT id FROM company WHERE vat_code = '99000000001'), (SELECT id FROM job_category WHERE code = 'MAGAZZINIERE'), (SELECT id FROM zone WHERE code = 'CAMPO_DI_MARTE'), 'Magazziniere carico e scarico', 'Mansione dimostrativa.', 'LIBERA', now(), 'seed', now(), 'seed'),
    -- s_cuoco
    (892398395572130063, (SELECT id FROM company WHERE vat_code = '99000000002'), (SELECT id FROM job_category WHERE code = 'AIUTO_CUOCO'), (SELECT id FROM zone WHERE code = 'SAN_LORENZO'), 'Aiuto cuoco cucina tradizionale', 'Mansione dimostrativa.', 'LIBERA', now(), 'seed', now(), 'seed'),
    -- s_sala
    (892398395572130064, (SELECT id FROM company WHERE vat_code = '99000000002'), (SELECT id FROM job_category WHERE code = 'CAMERIERE'), (SELECT id FROM zone WHERE code = 'SAN_LORENZO'), 'Cameriere di sala pranzo', 'Mansione dimostrativa.', 'LIBERA', now(), 'seed', now(), 'seed'),
    -- s_pulizie
    (892398395572130065, (SELECT id FROM company WHERE vat_code = '99000000003'), (SELECT id FROM job_category WHERE code = 'ADDETTO_PULIZIE'), (SELECT id FROM zone WHERE code = 'GALLUZZO'), 'Addetto pulizie uffici', 'Mansione dimostrativa.', 'LIBERA', now(), 'seed', now(), 'seed'),
    -- s_vendite
    (892398395572130066, (SELECT id FROM company WHERE vat_code = '99000000004'), (SELECT id FROM job_category WHERE code = 'ADDETTO_VENDITE'), (SELECT id FROM zone WHERE code = 'ISOLOTTO'), 'Addetto vendite e scaffalatura', 'Mansione dimostrativa.', 'LIBERA', now(), 'seed', now(), 'seed')
ON CONFLICT DO NOTHING;
