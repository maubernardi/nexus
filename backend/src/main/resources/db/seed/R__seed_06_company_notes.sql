-- Dati DIMOSTRATIVI per lo sviluppo locale (profilo `seed`, nel gruppo `local`): mai in test o produzione.
-- Script ripetibile e idempotente: ON CONFLICT DO NOTHING (riesecuzioni non duplicano né sovrascrivono).
-- Diario delle note delle aziende (US-505): testi fittizi, senza dati personali.

INSERT INTO company_note (id, company_id, text, created_at, created_by) VALUES
    (892398395576324601, 892398395572130067, 'Preferiscono essere contattati al mattino, prima delle 11.', now() - interval '20 days', 'operatore.cc'),
    (892398395576324602, 892398395572130067, 'Disponibili ad accogliere un tirocinante con patente B per le consegne in città.', now() - interval '6 days', 'operatore.cc'),
    (892398395576324603, 892398395572130068, 'Chiusura per ferie dal 10 al 25 agosto: niente proposte in quel periodo.', now() - interval '3 days', 'operatore.cc')
ON CONFLICT DO NOTHING;
