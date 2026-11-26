-- Dati DIMOSTRATIVI per lo sviluppo locale (profilo `seed`, nel gruppo `local`): mai in test o produzione.
-- Script ripetibile e idempotente: ON CONFLICT DO NOTHING (riesecuzioni non duplicano né sovrascrivono).
-- Aziende fittizie (P.IVA 99…, email su dominio .invalid) e mansioni; tutte LIBERE all'inserimento:
-- i blocchi da parte dei ticket sono applicati in R__seed_05.

INSERT INTO company (id, name, vat_code, legal_address, phone, email, active, created_at, created_by, updated_at, updated_by) VALUES
    (892398395572130067, 'Demo Logistica S.r.l.', '99000000001', 'Via dell''Esempio 1, Città Demo', '+39 000 1000001', 'logistica@demo.invalid', true, now(), 'seed', now(), 'seed'),
    (892398395572130068, 'Demo Ristorazione S.n.c.', '99000000002', 'Piazza Fittizia 2, Città Demo', '+39 000 1000002', 'ristorazione@demo.invalid', true, now(), 'seed', now(), 'seed'),
    (892398395572130069, 'Demo Servizi Pulizia Soc. Coop.', '99000000003', 'Viale Prova 3, Città Demo', '+39 000 1000003', 'servizi@demo.invalid', true, now(), 'seed', now(), 'seed'),
    (892398395572130070, 'Demo Commercio al Dettaglio S.p.A.', '99000000004', 'Corso Simulato 4, Città Demo', '+39 000 1000004', 'negozio@demo.invalid', true, now(), 'seed', now(), 'seed')
ON CONFLICT DO NOTHING;

-- Aziende come arriveranno dal database attuale (US-508): senza partita IVA, una anche senza nome. Sono da completare
-- dalla scheda; il numero 90001 non esiste nel database attuale.
INSERT INTO company (id, name, vat_code, legal_address, phone, email, active, created_at, created_by, updated_at, updated_by) VALUES
    (892398395572130095, 'Demo Bar del Centro', '00000000000', 'Via Immaginaria 5', '+39 000 1000005', NULL, true, now(), 'seed', now(), 'seed'),
    (892398395572130096, 'Azienda senza nome n. 90001', '00000000000', NULL, '+39 000 1000006', NULL, true, now(), 'seed', now(), 'seed')
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

-- Categoria merceologica e attività delle aziende dimostrative (US-509): solo quelle ancora senza l'una e l'altra,
-- così le aziende modificate a mano non cambiano. Le categorie sono le voci iniziali di V46.
UPDATE company c
SET business_category_id = demo.category_id,
    activity = demo.activity,
    updated_at = now(),
    updated_by = 'seed'
FROM (VALUES (892398395572130067, 893700000000000429, 'Logistica e magazzino'),
             (892398395572130068, 893700000000000411, 'Trattoria'),
             (892398395572130069, 893700000000000416, 'Pulizie di uffici'),
             (892398395572130070, 893700000000000408, 'Supermercato'),
             (892398395572130095, 893700000000000414, 'Bar'),
             (892398395572130096, NULL, 'Pizzeria'))
         AS demo (company_id, category_id, activity)
WHERE c.id = demo.company_id
  AND c.business_category_id IS NULL
  AND c.activity IS NULL;

-- Referenti delle aziende dimostrative (US-510), con il ruolo: solo per le aziende ancora senza referenti, così quelle
-- modificate a mano (e quelle con il referente copiato da V47) non cambiano. Una ha due referenti, una il solo ruolo.
-- I ruoli sono le voci iniziali di V47.
INSERT INTO company_contact (id, company_id, position, name, role_id, phone, mobile, email, created_at, created_by)
SELECT demo.id, c.id, demo.position, demo.name, demo.role_id, demo.phone, demo.mobile, demo.email, now(), 'seed'
FROM (VALUES (892398395572130101, 892398395572130067, 0, 'Referente Logistica', 893700000000000503, '+39 000 2000001', NULL, 'personale@demo.invalid'),
             (892398395572130102, 892398395572130068, 0, 'Referente Ristorazione', 893700000000000513, NULL, '+39 300 0000002', NULL),
             (892398395572130103, 892398395572130068, 1, 'Referente Sala', 893700000000000525, NULL, NULL, 'sala@demo.invalid'),
             (892398395572130104, 892398395572130069, 0, 'Referente Servizi', 893700000000000501, '+39 000 2000003', NULL, NULL),
             (892398395572130105, 892398395572130070, 0, 'Referente Negozio', 893700000000000523, NULL, NULL, 'direzione@demo.invalid'),
             (892398395572130106, 892398395572130095, 0, 'Referente Bar', 893700000000000513, NULL, '+39 300 0000005', NULL),
             (892398395572130107, 892398395572130096, 0, NULL, 893700000000000526, '+39 000 1000006', NULL, NULL))
         AS demo (id, company_id, position, name, role_id, phone, mobile, email)
JOIN company c ON c.id = demo.company_id
WHERE NOT EXISTS (SELECT 1 FROM company_contact cc WHERE cc.company_id = demo.company_id)
ON CONFLICT DO NOTHING;

-- Affidabilità (stelle) e interesse al progetto (pollici) delle aziende dimostrative (US-514), come dopo la conversione
-- dei simboli del database attuale: una con stelle e pollici, una con le sole stelle, una con il solo pollice; le altre
-- senza. Solo per le aziende ancora senza l'una e l'altro, così quelle valutate a mano non cambiano.
UPDATE company c
SET reliability = demo.reliability,
    project_interest = demo.project_interest,
    updated_at = now(),
    updated_by = 'seed'
FROM (VALUES (892398395572130067, 3, 2),
             (892398395572130068, 2, NULL),
             (892398395572130095, NULL, 1))
         AS demo (company_id, reliability, project_interest)
WHERE c.id = demo.company_id
  AND c.reliability IS NULL
  AND c.project_interest IS NULL;

-- Zona delle aziende dimostrative (US-511): tre con la zona delle loro mansioni, le altre senza. Solo per le aziende
-- ancora senza zona, così quelle cambiate a mano non cambiano.
UPDATE company c
SET zone_id = (SELECT z.id FROM zone z WHERE z.code = demo.zone_code),
    updated_at = now(),
    updated_by = 'seed'
FROM (VALUES (892398395572130067, 'RIFREDI'),
             (892398395572130068, 'SAN_LORENZO'),
             (892398395572130069, 'GALLUZZO'))
         AS demo (company_id, zone_code)
WHERE c.id = demo.company_id
  AND c.zone_id IS NULL;

-- Nave scuola dimostrativa (US-515): Demo Ristorazione, per provare il 🚢 e il filtro. Solo se nessuno l'ha cambiata a
-- mano (l'ultimo a modificarla è il seed), così una scelta fatta dalla scheda non cambia al riavvio.
UPDATE company
SET training_ship = true,
    updated_at = now(),
    updated_by = 'seed'
WHERE id = 892398395572130068
  AND updated_by = 'seed'
  AND NOT training_ship;
