## 1. Database e dominio

- [x] 1.1 Migrazione V44: colonne `promoter_*` su `project` con `ck_project_promoter_not_blank`, tabella
  `project_variable` con vincoli con nome; test per nome in `DomainConstraintsIT`
- [x] 1.2 `ProjectPromoter` (`@Embeddable`) su `Project`, entità `ProjectVariable` e repository

## 2. Backend

- [x] 2.1 `GET /document-catalogs/project-documents` e `PUT /document-catalogs/projects/{id}/documents` con promotore
  e variabili (D3), validazioni, audit (D6); test di integrazione
- [x] 2.2 Variabile usata da un modello attivo: non si toglie; riattivazione con i segnaposto ricontrollati (D3); test
- [x] 2.3 Segnaposto ammessi con le variabili del progetto e gruppi nell'elenco dei segnaposto (D4); test
- [x] 2.4 `DocumentValues` con il promotore del progetto e le variabili (D5); via `nexus.promoter.*`; test
- [x] 2.5 Dati di prova del promotore di GOL nel seed locale

## 3. Frontend

- [x] 3.1 Modulo dei dati dei documenti del progetto con promotore e variabili; tabella dei Cataloghi; test con axe
- [x] 3.2 Tabella dei dati dei documenti come regione scorrevole raggiungibile da tastiera, come le altre tabelle
  (axe a 390 px con il modulo aperto)

## 4. Deploy, documentazione e verifica

- [x] 4.1 Via `NEXUS_PROMOTER_*` dal compose; README; collaudo (T-24.4, T-24.5 e la prova nuova T-24.6)
- [x] 4.2 `./mvnw verify` e controlli del frontend verdi; controllo in Chrome (tastiera, axe, tema scuro, 390 px)
- [x] 4.3 `openspec validate add-project-promoter-variables` senza errori
