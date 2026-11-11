-- Dati DIMOSTRATIVI (profilo `seed`): comune della sede legale delle aziende dimostrative (US-1007). Gira a ogni avvio
-- dopo il caricamento dell'elenco dei comuni; tocca solo le aziende dimostrative (P.IVA 99…) ancora senza comune e
-- toglie dall'indirizzo il suffisso fittizio «, Città Demo». Le aziende corrette a mano non cambiano.
UPDATE company c
SET legal_municipality_id = m.id,
    legal_address = regexp_replace(c.legal_address, ', Città Demo$', ''),
    updated_at = now(),
    updated_by = 'seed'
FROM (VALUES ('99000000001', 'I684'), ('99000000002', 'D612'), ('99000000003', 'B962'), ('99000000004', 'D612'))
         AS demo (vat_code, cadastral_code)
JOIN municipality m ON m.cadastral_code = demo.cadastral_code AND m.valid_to IS NULL
WHERE c.vat_code = demo.vat_code
  AND c.legal_municipality_id IS NULL;
