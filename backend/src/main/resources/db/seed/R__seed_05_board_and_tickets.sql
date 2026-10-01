-- Dati DIMOSTRATIVI per lo sviluppo locale (profilo `seed`, nel gruppo `local`): mai in test o produzione.
-- Script ripetibile e idempotente: ON CONFLICT DO NOTHING (riesecuzioni non duplicano né sovrascrivono).
-- Bacheca e ticket in più stati (dati coerenti con i vincoli; la macchina a stati arriverà con la change successiva).
--  b_pubblico  PUBLISHED, visibile a tutti            (mansione s_pulizie) <- origine del ticket speciale t5
--  b_riservato PUBLISHED, riservato a GOL da 2 giorni (mansione s_sala)
--  b_bozza     DRAFT, rilancio dal ticket t7 (GOL)    (mansione s_mag_est)
--  t1 tutor1/GOL   NUOVA                          t5 tutor1/GOL   SPECIAL fast-track IN_LAVORAZIONE (da b_pubblico)
--  t2 tutor1/GOL   IN_ATTESA_APPROVAZIONE_ADMIN   t6 tutor2/POLIS PROPOSTA_ACCOLTA, timer avviato, blocca s_vendite
--  t3 tutor2/POLIS IN_LAVORAZIONE                 t7 tutor1/GOL   RIAPERTO dopo appuntamento fallito (blacklist + audit)
--  t4 tutor2/GOL   PROPOSTA_AZIENDA, blocca s_mag_nord
-- Ogni beneficiario ha al più una segnalazione aperta (vincolo uq_ticket_open_per_beneficiary): t6 → k7, t7 → k6.
-- I numeri progressivi (#post, n. ticket) li assegna il database nell'ordine di inserimento.

INSERT INTO board_post (id, job_slot_id, title, project_id, status, age_min, age_max, weekly_hours, duration_months, notes, published_at, created_at, created_by, updated_at, updated_by) VALUES
    (892398395572130086, 892398395572130065, 'Tirocinio addetto pulizie uffici', NULL, 'PUBLISHED', NULL, NULL, 25, 6, 'Turno del mattino.', now() - interval '10 days', now(), 'seed', now(), 'seed'),  -- b_pubblico
    (892398395572130087, 892398395572130064, 'Tirocinio cameriere di sala', (SELECT id FROM project WHERE code = 'GOL'), 'PUBLISHED', 18, 29, 30, 6, 'Requisito di età del programma (esempio).', now() - interval '2 days', now(), 'seed', now(), 'seed')  -- b_riservato
ON CONFLICT DO NOTHING;

INSERT INTO ticket (id, tutor_id, project_id, beneficiary_id, type, status, is_fast_track, requested_job_category_id, requested_job_free_text,
                    board_post_id, assigned_cc_operator_id, job_slot_id, timer_started_at, timer_deadline_at,
                    created_at, created_by, updated_at, updated_by) VALUES
    -- t1
    (892398395572130089, (SELECT id FROM app_user WHERE username = 'tutor1'), (SELECT id FROM project WHERE code = 'GOL'), 892398395572130071, 'NORMAL', 'NUOVA', false, (SELECT id FROM job_category WHERE code = 'MAGAZZINIERE'), NULL,
     NULL, NULL, NULL, NULL, NULL,
     now() - interval '20 days', 'seed', now() - interval '20 days', 'seed'),
    -- t2
    (892398395572130090, (SELECT id FROM app_user WHERE username = 'tutor1'), (SELECT id FROM project WHERE code = 'GOL'), 892398395572130072, 'NORMAL', 'IN_ATTESA_APPROVAZIONE_ADMIN', false, NULL, 'Tecnico del suono',
     NULL, NULL, NULL, NULL, NULL,
     now() - interval '18 days', 'seed', now() - interval '18 days', 'seed'),
    -- t3
    (892398395572130091, (SELECT id FROM app_user WHERE username = 'tutor2'), (SELECT id FROM project WHERE code = 'POLIS'), 892398395572130074, 'NORMAL', 'IN_LAVORAZIONE', false, (SELECT id FROM job_category WHERE code = 'ADDETTO_PULIZIE'), NULL,
     NULL, (SELECT id FROM app_user WHERE username = 'operatore.cc'), NULL, NULL, NULL,
     now() - interval '16 days', 'seed', now() - interval '16 days', 'seed'),
    -- t4
    (892398395572130092, (SELECT id FROM app_user WHERE username = 'tutor2'), (SELECT id FROM project WHERE code = 'GOL'), 892398395572130075, 'NORMAL', 'PROPOSTA_AZIENDA', false, (SELECT id FROM job_category WHERE code = 'MAGAZZINIERE'), NULL,
     NULL, (SELECT id FROM app_user WHERE username = 'operatore.cc'), 892398395572130061, NULL, NULL,
     now() - interval '14 days', 'seed', now() - interval '14 days', 'seed'),
    -- t5
    (892398395572130093, (SELECT id FROM app_user WHERE username = 'tutor1'), (SELECT id FROM project WHERE code = 'GOL'), 892398395572130073, 'SPECIAL', 'IN_LAVORAZIONE', true, (SELECT id FROM job_category WHERE code = 'ADDETTO_PULIZIE'), NULL,
     892398395572130086, (SELECT id FROM app_user WHERE username = 'operatore.cc'), NULL, NULL, NULL,
     now() - interval '12 days', 'seed', now() - interval '12 days', 'seed'),
    -- t6
    (892398395572130094, (SELECT id FROM app_user WHERE username = 'tutor2'), (SELECT id FROM project WHERE code = 'POLIS'), 892398395576324401, 'NORMAL', 'PROPOSTA_ACCOLTA', false, (SELECT id FROM job_category WHERE code = 'ADDETTO_VENDITE'), NULL,
     NULL, (SELECT id FROM app_user WHERE username = 'operatore.cc'), 892398395572130066, now() - interval '2 days', now() + interval '7 days',
     now() - interval '10 days', 'seed', now() - interval '10 days', 'seed'),
    -- t7
    (892398395576324284, (SELECT id FROM app_user WHERE username = 'tutor1'), (SELECT id FROM project WHERE code = 'GOL'), 892398395576324400, 'NORMAL', 'RIAPERTO', false, (SELECT id FROM job_category WHERE code = 'MAGAZZINIERE'), NULL,
     NULL, (SELECT id FROM app_user WHERE username = 'operatore.cc'), NULL, NULL, NULL,
     now() - interval '8 days', 'seed', now() - interval '8 days', 'seed')
ON CONFLICT DO NOTHING;

-- Rilancio in bacheca dal ticket t7 (appuntamento fallito): bozza riservata al progetto del ticket.
INSERT INTO board_post (id, job_slot_id, title, project_id, status, weekly_hours, duration_months, created_from_ticket_id, created_at, created_by, updated_at, updated_by) VALUES
    (892398395572130088, 892398395572130062, 'Tirocinio magazziniere carico e scarico', (SELECT id FROM project WHERE code = 'GOL'), 'DRAFT', 20, 4, 892398395576324284, now(), 'seed', now(), 'seed')  -- b_bozza
ON CONFLICT DO NOTHING;

-- Mansioni bloccate dai ticket che le hanno in carico (idempotente).
UPDATE job_slot SET status = 'BLOCCATA', blocked_by_ticket_id = 892398395572130092, updated_at = now(), updated_by = 'seed'
 WHERE id = 892398395572130061 AND blocked_by_ticket_id IS DISTINCT FROM 892398395572130092;  -- s_mag_nord <- t4
UPDATE job_slot SET status = 'BLOCCATA', blocked_by_ticket_id = 892398395572130094, updated_at = now(), updated_by = 'seed'
 WHERE id = 892398395572130066 AND blocked_by_ticket_id IS DISTINCT FROM 892398395572130094;  -- s_vendite <- t6

-- Storico degli stati (creazione ed eventuali transizioni).
INSERT INTO ticket_status_history (id, ticket_id, from_status, to_status, note, created_at, created_by, updated_at, updated_by) VALUES
    (892398395576324285, 892398395572130089, NULL, 'NUOVA', NULL, now() - interval '1 days', 'seed', now() - interval '1 days', 'seed'),
    (892398395576324286, 892398395572130090, NULL, 'IN_ATTESA_APPROVAZIONE_ADMIN', NULL, now() - interval '1 days', 'seed', now() - interval '1 days', 'seed'),
    (892398395576324287, 892398395572130091, NULL, 'NUOVA', NULL, now() - interval '2 days', 'seed', now() - interval '2 days', 'seed'),
    (892398395576324288, 892398395572130091, 'NUOVA', 'IN_LAVORAZIONE', NULL, now() - interval '1 days', 'seed', now() - interval '1 days', 'seed'),
    (892398395576324289, 892398395572130092, NULL, 'NUOVA', NULL, now() - interval '3 days', 'seed', now() - interval '3 days', 'seed'),
    (892398395576324290, 892398395572130092, 'NUOVA', 'IN_LAVORAZIONE', NULL, now() - interval '2 days', 'seed', now() - interval '2 days', 'seed'),
    (892398395576324291, 892398395572130092, 'IN_LAVORAZIONE', 'PROPOSTA_AZIENDA', NULL, now() - interval '1 days', 'seed', now() - interval '1 days', 'seed'),
    (892398395576324292, 892398395572130093, NULL, 'IN_LAVORAZIONE', NULL, now() - interval '1 days', 'seed', now() - interval '1 days', 'seed'),
    (892398395576324293, 892398395572130094, NULL, 'NUOVA', NULL, now() - interval '4 days', 'seed', now() - interval '4 days', 'seed'),
    (892398395576324294, 892398395572130094, 'NUOVA', 'IN_LAVORAZIONE', NULL, now() - interval '3 days', 'seed', now() - interval '3 days', 'seed'),
    (892398395576324295, 892398395572130094, 'IN_LAVORAZIONE', 'PROPOSTA_AZIENDA', NULL, now() - interval '2 days', 'seed', now() - interval '2 days', 'seed'),
    (892398395576324296, 892398395572130094, 'PROPOSTA_AZIENDA', 'PROPOSTA_ACCOLTA', NULL, now() - interval '1 days', 'seed', now() - interval '1 days', 'seed'),
    (892398395576324297, 892398395576324284, NULL, 'NUOVA', NULL, now() - interval '6 days', 'seed', now() - interval '6 days', 'seed'),
    (892398395576324298, 892398395576324284, 'NUOVA', 'IN_LAVORAZIONE', NULL, now() - interval '5 days', 'seed', now() - interval '5 days', 'seed'),
    (892398395576324299, 892398395576324284, 'IN_LAVORAZIONE', 'PROPOSTA_AZIENDA', NULL, now() - interval '4 days', 'seed', now() - interval '4 days', 'seed'),
    (892398395576324300, 892398395576324284, 'PROPOSTA_AZIENDA', 'PROPOSTA_ACCOLTA', NULL, now() - interval '3 days', 'seed', now() - interval '3 days', 'seed'),
    (892398395576324301, 892398395576324284, 'PROPOSTA_ACCOLTA', 'APPUNTAMENTO', NULL, now() - interval '2 days', 'seed', now() - interval '2 days', 'seed'),
    (892398395576324302, 892398395576324284, 'APPUNTAMENTO', 'RIAPERTO', 'Appuntamento non andato a buon fine: rilancio in bacheca.', now() - interval '1 days', 'seed', now() - interval '1 days', 'seed')
ON CONFLICT DO NOTHING;

-- Azienda esclusa per t7 dopo l'appuntamento fallito, ed evento nell'audit trail (in sola aggiunta).
INSERT INTO ticket_company_blacklist (ticket_id, company_id, reason, created_at, created_by) VALUES
    (892398395576324284, (SELECT id FROM company WHERE vat_code = '99000000001'), 'Appuntamento non andato a buon fine (dato fittizio).', now(), 'seed')
ON CONFLICT DO NOTHING;

INSERT INTO company_audit_event (id, company_id, ticket_id, event_type, details, created_at, created_by) VALUES
    (892398395576324303, (SELECT id FROM company WHERE vat_code = '99000000001'), 892398395576324284, 'APPUNTAMENTO_FALLITO', '{"rilancioInBacheca": true}', now(), 'seed')
ON CONFLICT DO NOTHING;
