-- US-1011: dati dei documenti reali per progetto, modificabili dall'ADMIN nei cataloghi. Riferimento all'avviso
-- (vuoto = il testo predefinito nel codice, Avviso pubblico n. 10 di GOL) e riga di intestazione con codice e CUP
-- (es. «Firenze in Rete 2 – Codice 9014358 – CUP B81D24000440006»). Solo additiva (A16).
ALTER TABLE project ADD COLUMN notice_reference varchar(1000);
ALTER TABLE project ADD COLUMN header_line varchar(300);
ALTER TABLE project ADD CONSTRAINT ck_project_notice_reference
    CHECK (notice_reference IS NULL OR btrim(notice_reference) <> '');
ALTER TABLE project ADD CONSTRAINT ck_project_header_line CHECK (header_line IS NULL OR btrim(header_line) <> '');
