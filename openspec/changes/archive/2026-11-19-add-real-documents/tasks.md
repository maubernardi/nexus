## 1. Database e dati dei progetti

- [x] 1.1 Migrazione V43: `project.notice_reference` e `project.header_line` facoltative, con vincoli con nome
  (non vuote); test per nome in `DomainConstraintsIT`
- [x] 1.2 `Project`: i due campi e il testo predefinito dell'avviso (D6)
- [x] 1.3 `GET /document-catalogs/project-documents` e `PUT /document-catalogs/projects/{id}/documents`, solo ADMIN,
  con audit; test di integrazione (salvataggio, ritorno al predefinito, audit, `403` al Tutor)

## 2. Backend dei documenti

- [x] 2.1 `nexus.promoter.*` in `ApplicationProperties` e `application.yml` (`NEXUS_PROMOTER_*`, D5)
- [x] 2.2 `DocumentPlaceholders` con nome, gruppo e descrizione (D7); `GET /document-templates/placeholders` con le
  voci; test
- [x] 2.3 `InternshipMonths`: mesi del tirocinio sulle convenzioni (D4); test unitari, con convenzioni consecutive e no
- [x] 2.4 `DocumentValues`: tutti i valori, con le regole di caselle e fasce (D2, D7); test di integrazione con un
  modello finto che usa i segnaposto nuovi
- [x] 2.5 Generazione con `extension` e `project`, nomi dei file e audit (D1); relazione finale a tirocinio chiuso con
  `409` prima (D3); test di integrazione in `InternshipDocumentIT`
- [x] 2.6 Modelli generici: relazione finale e nuovi segnaposto in convenzione e progetto formativo
  (`tools/genera-modelli-docx.py`); test che i modelli nel codice usino solo segnaposto noti

## 3. Frontend

- [x] 3.1 Sezione «Documenti»: un gruppo per documento, un pulsante per convenzione e progetto, relazione finale a
  tirocinio chiuso (D9); test con axe
- [x] 3.2 Cataloghi dell'ADMIN: avviso e intestazione per progetto; test con axe
- [x] 3.3 Pagina «Modelli dei documenti»: segnaposto per gruppo con la descrizione; test con axe

## 4. Deploy, documentazione e verifica

- [x] 4.1 `infra/deploy/compose.yaml` passa `NEXUS_PROMOTER_*` al backend; README con i dati da mettere nel `.env`
- [x] 4.2 Collaudo: prove T-24.3 e T-24.4 in `docs/test/collaudo.md`
- [x] 4.3 `./mvnw verify` e controlli del frontend verdi; controllo in Chrome (tastiera, axe, tema scuro, 390 px)
- [x] 4.4 `openspec validate add-real-documents` senza errori
