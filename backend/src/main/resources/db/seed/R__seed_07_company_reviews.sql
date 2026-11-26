-- Dati DIMOSTRATIVI per lo sviluppo locale e il collaudo (profilo `seed`): mai in test o produzione.
-- Valutazioni delle aziende alla fine del tirocinio (US-1104):
-- - t8: segnalazione di tutor1 in tirocinio presso «Demo Servizi Pulizia», con il report di restituzione da compilare;
-- - t9, t10: tirocini già chiusi presso «Demo Logistica», valutati da tutor1 (4, 5, 4 con la nota) e da tutor2 (5, 5, 5).
-- Script ripetibile e idempotente: ON CONFLICT DO NOTHING; il report non si modifica dopo l'invio (V26).

INSERT INTO ticket (id, tutor_id, project_id, beneficiary_id, type, status, is_fast_track, requested_job_category_id, requested_job_free_text,
                    board_post_id, assigned_cc_operator_id, job_slot_id, job_slot_held, timer_started_at, timer_deadline_at,
                    created_at, created_by, updated_at, updated_by) VALUES
    -- t8: in tirocinio, mansione s_pulizie
    (892398395576324500, (SELECT id FROM app_user WHERE username = 'tutor1'), (SELECT id FROM project WHERE code = 'GOL'), 892398395576324402, 'NORMAL', 'IN_TIROCINIO', false, (SELECT id FROM job_category WHERE code = 'ADDETTO_PULIZIE'), NULL,
     NULL, (SELECT id FROM app_user WHERE username = 'operatore.cc'), 892398395572130065, true, NULL, NULL,
     now() - interval '90 days', 'seed', now() - interval '30 days', 'seed'),
    -- t9: chiuso, mansione s_mag_est di Demo Logistica
    (892398395576324501, (SELECT id FROM app_user WHERE username = 'tutor1'), (SELECT id FROM project WHERE code = 'GOL'), 892398395572130071, 'NORMAL', 'FORM_RESTITUZIONE', false, (SELECT id FROM job_category WHERE code = 'MAGAZZINIERE'), NULL,
     NULL, (SELECT id FROM app_user WHERE username = 'operatore.cc'), 892398395572130062, false, NULL, NULL,
     now() - interval '200 days', 'seed', now() - interval '60 days', 'seed'),
    -- t10: chiuso, stessa mansione, di tutor2
    (892398395576324502, (SELECT id FROM app_user WHERE username = 'tutor2'), (SELECT id FROM project WHERE code = 'GOL'), 892398395572130074, 'NORMAL', 'FORM_RESTITUZIONE', false, (SELECT id FROM job_category WHERE code = 'MAGAZZINIERE'), NULL,
     NULL, (SELECT id FROM app_user WHERE username = 'operatore.cc'), 892398395572130062, false, NULL, NULL,
     now() - interval '150 days', 'seed', now() - interval '30 days', 'seed')
ON CONFLICT DO NOTHING;

-- La mansione di t8 è bloccata dal suo tirocinio, solo finché t8 è in tirocinio (il collaudo lo chiude)
UPDATE job_slot SET status = 'BLOCCATA', blocked_by_ticket_id = 892398395576324500, updated_at = now(), updated_by = 'seed'
 WHERE id = 892398395572130065
   AND blocked_by_ticket_id IS DISTINCT FROM 892398395576324500
   AND EXISTS (SELECT 1 FROM ticket WHERE id = 892398395576324500 AND status = 'IN_TIROCINIO');

-- Report di restituzione di t9 e t10, con la valutazione di Demo Logistica (autori: i Tutor, per il nome nella scheda)
INSERT INTO closure_report (id, ticket_id, actual_end_date, closure_reason_id, company_satisfaction, beneficiary_satisfaction,
                            objectives, hired, no_hire_reason_id, job_slot_still_available, company_id,
                            company_interaction, company_welcome, company_training, company_review_note, created_at, created_by)
SELECT demo.id, demo.ticket_id, demo.actual_end_date, (SELECT id FROM closure_reason WHERE code = 'CHIUSURA_REGOLARE'),
       4, 4, 'Obiettivi dimostrativi raggiunti.', false, (SELECT min(id) FROM no_hire_reason), true,
       (SELECT id FROM company WHERE vat_code = '99000000001'),
       demo.interaction, demo.welcome, demo.training, demo.note, demo.created_at, demo.author
FROM (VALUES (892398395576324503, 892398395576324501, DATE '2026-09-30', 4, 5, 4,
              'Accoglienza ottima; la formazione è partita lenta il primo mese (nota dimostrativa).',
              now() - interval '60 days', 'tutor1'),
             (892398395576324504, 892398395576324502, DATE '2026-10-31', 5, 5, 5, NULL,
              now() - interval '30 days', 'tutor2'))
         AS demo (id, ticket_id, actual_end_date, interaction, welcome, training, note, created_at, author)
WHERE EXISTS (SELECT 1 FROM ticket t WHERE t.id = demo.ticket_id)
ON CONFLICT DO NOTHING;
