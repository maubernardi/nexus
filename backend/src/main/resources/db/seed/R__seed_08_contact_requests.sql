-- Dati DIMOSTRATIVI per lo sviluppo locale e il collaudo (profilo `seed`): mai in test o produzione.
-- Richieste di contatto di un'azienda (US-507), entrambe di tutor1:
-- - una aperta, per un'azienda nuova (nome finto, zona Rifredi, categoria «Officine meccaniche»);
-- - una già in carico a operatore.cc, per «Demo Logistica S.r.l.».
-- Nel menu del Call Center il numero delle aperte è 1. Script ripetibile e idempotente: ON CONFLICT DO NOTHING, così
-- una richiesta cambiata a mano non torna com'era al riavvio.

INSERT INTO contact_request (id, requested_by_id, company_id, company_name, zone_id, business_category_id, status,
                             assigned_operator_id, taken_at, created_at, created_by, updated_at, updated_by) VALUES
    (892398395576324600, (SELECT id FROM app_user WHERE username = 'tutor1'), NULL, 'Officina Meccanica Demo del Ponte',
     (SELECT id FROM zone WHERE code = 'RIFREDI'), (SELECT id FROM business_category WHERE code = 'OFFICINE_MECCANICHE_SALDAT'),
     'APERTA', NULL, NULL, now() - interval '2 days', 'tutor1', now() - interval '2 days', 'tutor1'),
    (892398395576324601, (SELECT id FROM app_user WHERE username = 'tutor1'),
     (SELECT id FROM company WHERE vat_code = '99000000001'), NULL, NULL, NULL,
     'IN_CARICO', (SELECT id FROM app_user WHERE username = 'operatore.cc'), now() - interval '1 day',
     now() - interval '3 days', 'tutor1', now() - interval '1 day', 'operatore.cc')
ON CONFLICT DO NOTHING;
