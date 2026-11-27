-- Dati DIMOSTRATIVI per lo sviluppo locale e il collaudo (profilo `seed`): mai in test o produzione.
-- Richieste di contatto di un'azienda (US-507), entrambe di tutor1 e per aziende non ancora in anagrafica (PO, 27/11):
-- - una aperta, per «Officina Meccanica Demo del Ponte» (zona Rifredi, categoria «Officine meccaniche»);
-- - una già in carico a operatore.cc, per «Bar Pasticceria Demo Stella» (zona San Lorenzo, senza categoria).
-- Sulla seconda c'è già un tentativo di contatto (US-512). Nel menu del Call Center il numero delle aperte è 1. Script ripetibile e idempotente: ON CONFLICT DO NOTHING, così
-- una richiesta cambiata a mano non torna com'era al riavvio.

INSERT INTO contact_request (id, requested_by_id, company_name, zone_id, business_category_id, status,
                             assigned_operator_id, taken_at, created_at, created_by, updated_at, updated_by) VALUES
    (892398395576324600, (SELECT id FROM app_user WHERE username = 'tutor1'), 'Officina Meccanica Demo del Ponte',
     (SELECT id FROM zone WHERE code = 'RIFREDI'), (SELECT id FROM business_category WHERE code = 'OFFICINE_MECCANICHE_SALDAT'),
     'APERTA', NULL, NULL, now() - interval '2 days', 'tutor1', now() - interval '2 days', 'tutor1'),
    (892398395576324601, (SELECT id FROM app_user WHERE username = 'tutor1'), 'Bar Pasticceria Demo Stella',
     (SELECT id FROM zone WHERE code = 'SAN_LORENZO'), NULL,
     'IN_CARICO', (SELECT id FROM app_user WHERE username = 'operatore.cc'), now() - interval '1 day',
     now() - interval '3 days', 'tutor1', now() - interval '1 day', 'operatore.cc')
ON CONFLICT DO NOTHING;

INSERT INTO contact_request_attempt (id, contact_request_id, attempt_date, outcome, note, created_at, created_by) VALUES
    (892398395576324610, 892398395576324601, current_date - 1, 'NON_RAGGIUNGIBILE',
     'Squilla a vuoto: riprovo nel pomeriggio.', now() - interval '1 day', 'operatore.cc')
ON CONFLICT DO NOTHING;
