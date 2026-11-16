## 1. Motore dei segnaposto

- [x] 1.1 `DocxTemplate`: sostituzione nei soli nodi di testo, con l'a capo nello stesso run (D1); test con
  tabulazione, simbolo, formattazioni diverse e segnaposto spezzato

## 2. Tipologia dell'ente

- [x] 2.1 `DocumentPlaceholders` e `DocumentValues`: `{TIPO_ENTE_…}` per ogni `CareAgencyKind` (D2); test in
  `InternshipDocumentIT`

## 3. Modelli di GOL (fuori dal repository)

- [x] 3.1 Script `documenti_base/modelli-nexus/prepara_modelli.py` e i 6 modelli (D3)
- [x] 3.2 Caricamento dei 6 modelli in locale senza segnaposto sconosciuti; documenti compilati con valori di prova e
  controllati pagina per pagina (LibreOffice)

## 4. Documentazione e verifica

- [x] 4.1 README («Modelli reali di GOL») e collaudo T-24.5
- [x] 4.2 `./mvnw verify` e controlli del frontend verdi
- [x] 4.3 `openspec validate prepare-real-templates` senza errori
