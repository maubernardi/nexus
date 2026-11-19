-- US-508: le aziende del database attuale senza partita IVA arrivano con «00000000000» (PO, 19/11), che non conta per
-- l'unicità: il vincolo diventa un indice univoco parziale con lo stesso nome. Il programma non lascia salvare il
-- segnaposto dal modulo, quindi la release precedente, che non lo scrive mai, funziona anche con l'indice nuovo (A16).
ALTER TABLE company DROP CONSTRAINT uq_company_vat_code;
CREATE UNIQUE INDEX uq_company_vat_code ON company (vat_code) WHERE vat_code <> '00000000000';
